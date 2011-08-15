package com.gemserk.games.superflyingthing.scripts.controllers;

import com.artemis.Entity;
import com.artemis.utils.ImmutableBag;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.gemserk.commons.artemis.scripts.ScriptJavaImpl;
import com.gemserk.commons.gdx.games.Spatial;
import com.gemserk.componentsengine.utils.AngleUtils;
import com.gemserk.games.superflyingthing.ShipController;
import com.gemserk.games.superflyingthing.components.ComponentWrapper;
import com.gemserk.games.superflyingthing.components.Components.AttachmentComponent;
import com.gemserk.games.superflyingthing.components.Components.GrabbableComponent;
import com.gemserk.games.superflyingthing.components.Components.MovementComponent;
import com.gemserk.games.superflyingthing.templates.Groups;

public class BasicAIShipControllerScript extends ScriptJavaImpl implements RayCastCallback {

	private final World physicsWorld;
	private final ShipController shipController;

	boolean collides = false;
	float randomDirection = 1f;

	Vector2 target = new Vector2();
	Vector2 direction = new Vector2();

	boolean wayToDestinationPlanet = false;
	
	public BasicAIShipControllerScript(World physicsWorld, ShipController shipController) {
		this.physicsWorld = physicsWorld;
		this.shipController = shipController;
	}

	@Override
	public void update(com.artemis.World world, Entity e) {
		updateShipInPlanetBehavior(world, e);
		updateShipBehavior(world, e);
	}

	private void updateShipBehavior(com.artemis.World world, Entity e2) {

		Entity ship = world.getTagManager().getEntity(Groups.ship);
		if (ship == null)
			return;

		MovementComponent movementComponent = ComponentWrapper.getMovementComponent(ship);

		Spatial spatial = ComponentWrapper.getSpatial(ship);
		Vector2 position = spatial.getPosition();
		direction.set(movementComponent.getDirection());

		if (wayToDestinationPlanet) {

			float angle = direction.angle();
			float desiredAngle = target.tmp().sub(position).nor().angle();

			shipController.setMovementDirection((float) AngleUtils.minimumDifference(angle, desiredAngle) / 90f);

			return;
		}

		ImmutableBag<Entity> destinationPlanets = world.getGroupManager().getEntities(Groups.destinationPlanets);
		for (int i = 0; i < destinationPlanets.size(); i++) {
			Entity destinationPlanet = destinationPlanets.get(i);
			Spatial destinationPlanetSpatial = ComponentWrapper.getSpatial(destinationPlanet);

			Vector2 desiredDirection = destinationPlanetSpatial.getPosition().tmp().sub(position);
			target.set(position).add(desiredDirection.mul(1f));

			collides = false;
			physicsWorld.rayCast(this, position, target);

			if (!collides) {
				wayToDestinationPlanet = true;
				target.set(destinationPlanetSpatial.getPosition());
				Gdx.app.log("SuperFlyingThing", "Direct way to destination planet detected");
				return;
			}

		}

		target.set(position).add(direction.tmp().nor().mul(3f));

		shipController.setMovementDirection(0f);

		collides = false;
		physicsWorld.rayCast(this, position, target);

		if (!collides)
			return;

		direction.set(movementComponent.getDirection()).rotate(20 * randomDirection);
		target.set(position).add(direction.tmp().nor().mul(3f));

		collides = false;
		physicsWorld.rayCast(this, position, target);

		if (!collides) {
			shipController.setMovementDirection(1f * randomDirection);
			return;
		}

		direction.set(movementComponent.getDirection()).rotate(-20 * randomDirection);
		target.set(position).add(direction.tmp().nor().mul(3f));

		collides = false;
		physicsWorld.rayCast(this, position, target);

		if (!collides) {
			shipController.setMovementDirection(-1f * randomDirection);
			return;
		}

		shipController.setMovementDirection(1f * randomDirection);
	}

	@Override
	public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {

		Entity e = (Entity) fixture.getBody().getUserData();
		if (e != null) {
			GrabbableComponent grabbableComponent = ComponentWrapper.getGrabbableComponent(e);
			if (grabbableComponent != null)
				return 1;

			AttachmentComponent attachmentComponent = ComponentWrapper.getAttachmentComponent(e);
			if (attachmentComponent != null)
				return 1;
		}

		collides = true;
		return 1;
	}

	private void updateShipInPlanetBehavior(com.artemis.World world, Entity e) {
		Entity startPlanet = world.getTagManager().getEntity(Groups.startPlanet);
		shipController.setShouldReleaseShip(false);

		AttachmentComponent attachmentComponent = ComponentWrapper.getAttachmentComponent(startPlanet);
		if (attachmentComponent == null)
			return;

		if (attachmentComponent.getEntity() == null)
			return;

		Entity ship = attachmentComponent.getEntity();

		MovementComponent movementComponent = ComponentWrapper.getMovementComponent(ship);
		Vector2 direction = movementComponent.getDirection();
		if (AngleUtils.minimumDifference(direction.angle(), 0) < 10)
			shipController.setShouldReleaseShip(true);

		wayToDestinationPlanet = false;
		randomDirection = MathUtils.randomBoolean() ? 1f : -1f;
	}

}