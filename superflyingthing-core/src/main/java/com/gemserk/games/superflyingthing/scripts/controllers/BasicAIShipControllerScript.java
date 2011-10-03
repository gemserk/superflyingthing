package com.gemserk.games.superflyingthing.scripts.controllers;

import com.artemis.Entity;
import com.artemis.utils.ImmutableBag;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.gemserk.commons.artemis.scripts.ScriptJavaImpl;
import com.gemserk.commons.gdx.games.Spatial;
import com.gemserk.componentsengine.utils.AngleUtils;
import com.gemserk.games.superflyingthing.ShipController;
import com.gemserk.games.superflyingthing.components.Components.AttachmentComponent;
import com.gemserk.games.superflyingthing.components.Components.ControllerComponent;
import com.gemserk.games.superflyingthing.components.Components.GrabbableComponent;
import com.gemserk.games.superflyingthing.components.Components.MovementComponent;
import com.gemserk.games.superflyingthing.components.GameComponents;
import com.gemserk.games.superflyingthing.entities.Groups;
import com.gemserk.games.superflyingthing.entities.Tags;

public class BasicAIShipControllerScript extends ScriptJavaImpl {

	static class RayCastHelper implements RayCastCallback {

		private boolean collides;
		private final World world;

		private final Vector2 position = new Vector2();
		private final Vector2 target = new Vector2();

		public RayCastHelper(World world) {
			this.world = world;
		}

		private void setPosition(Vector2 position) {
			this.position.set(position);
		}

		private void setTarget(float x, float y) {
			this.target.set(x, y);
		}

		public boolean checkCollision(Vector2 position, Vector2 target) {
			setPosition(position);
			setTarget(target.x, target.y);
			return checkCollision();
		}

		private boolean checkCollision() {
			collides = false;
			world.rayCast(this, position, target);
			return collides;
		}

		@Override
		public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
			Entity e = (Entity) fixture.getBody().getUserData();
			if (e != null) {
				GrabbableComponent grabbableComponent = GameComponents.getGrabbableComponent(e);
				if (grabbableComponent != null)
					return 1;

				AttachmentComponent attachmentComponent = GameComponents.getAttachmentComponent(e);
				if (attachmentComponent != null)
					return 1;
			}

			collides = true;
			return 1;
		}

	}

	private ShipController controller;

	float randomDirection = 1f;

	Vector2 target = new Vector2();
	Vector2 planetPosition = new Vector2();
	Vector2 direction = new Vector2();

	boolean wayToDestinationPlanet = false;
	private RayCastHelper rayCastHelper;

	public BasicAIShipControllerScript(World physicsWorld) {
		rayCastHelper = new RayCastHelper(physicsWorld);
	}

	@Override
	public void update(com.artemis.World world, Entity e) {

		Entity playerController = world.getTagManager().getEntity(Tags.PlayerController);
		if (playerController == null)
			return;
		ControllerComponent controllerComponent = GameComponents.getControllerComponent(playerController);
		controller = controllerComponent.getController();

		updateShipInPlanetBehavior(world, e);
		updateShipBehavior(world, e);
	}

	private void updateShipBehavior(com.artemis.World world, Entity e2) {

		float movementDirection = 0f;

		Entity ship = world.getTagManager().getEntity(Tags.Ship);
		if (ship == null)
			return;

		MovementComponent movementComponent = GameComponents.getMovementComponent(ship);

		Spatial spatial = GameComponents.getSpatial(ship);
		Vector2 position = spatial.getPosition();
		direction.set(movementComponent.getDirection());
		
		if (wayToDestinationPlanet) {
			
			direction.set(movementComponent.getDirection());
			
			float angle = direction.angle();
			float desiredAngle = planetPosition.tmp().sub(position).nor().angle();

			movementDirection = (float) AngleUtils.minimumDifference(angle, desiredAngle) / 1f;
			
			if (movementDirection > 1f)
				movementDirection = 1f;
			if (movementDirection < -1f)
				movementDirection = -1f;
			
			direction.set(movementComponent.getDirection()).rotate(15 * randomDirection);
			target.set(position).add(direction.tmp().nor().mul(2f));

			boolean collidesA = rayCastHelper.checkCollision(position, target);

			direction.set(movementComponent.getDirection()).rotate(-15 * randomDirection);
			target.set(position).add(direction.tmp().nor().mul(2f));

			boolean collidesB = rayCastHelper.checkCollision(position, target);
			
			if (collidesA)
				movementDirection += 2f * MathUtils.random();

			if (collidesB)
				movementDirection -= 2f * MathUtils.random();
			
			controller.setMovementDirection(movementDirection);

			return;
		} else {

			ImmutableBag<Entity> destinationPlanets = world.getGroupManager().getEntities(Groups.destinationPlanets);
			for (int i = 0; i < destinationPlanets.size(); i++) {
				Entity destinationPlanet = destinationPlanets.get(i);
				Spatial destinationPlanetSpatial = GameComponents.getSpatial(destinationPlanet);

				Vector2 desiredDirection = destinationPlanetSpatial.getPosition().tmp().sub(position);
				planetPosition.set(position).add(desiredDirection.mul(1f));

				boolean collides = rayCastHelper.checkCollision(position, planetPosition);
				// physicsWorld.rayCast(this, position, target);

				if (!collides) {
					wayToDestinationPlanet = true;
					planetPosition.set(destinationPlanetSpatial.getPosition());
					// Gdx.app.log("SuperFlyingThing", "Direct way to destination planet detected");
					return;
				}

			}
		}
		
		target.set(position).add(direction.tmp().nor().mul(2f));

		boolean collidesDirect = rayCastHelper.checkCollision(position, target);

		direction.set(movementComponent.getDirection()).rotate(30 * randomDirection);
		target.set(position).add(direction.tmp().nor().mul(2f));

		boolean collidesA = rayCastHelper.checkCollision(position, target);

		direction.set(movementComponent.getDirection()).rotate(-30 * randomDirection);
		target.set(position).add(direction.tmp().nor().mul(2f));

		boolean collidesB = rayCastHelper.checkCollision(position, target);

		if (!collidesDirect) {

			if (collidesA)
				movementDirection += 2f * MathUtils.random();

			if (collidesB)
				movementDirection -= 2f * MathUtils.random();

			controller.setMovementDirection(movementDirection);
		} else {
			movementDirection = collidesA ? -1f : 0f;
			movementDirection -= collidesB ? 1f : 0f;

			if (collidesA && collidesB)
				movementDirection = 1f * randomDirection;
			controller.setMovementDirection(movementDirection);
		}

	}

	private void updateShipInPlanetBehavior(com.artemis.World world, Entity e) {
		Entity startPlanet = world.getTagManager().getEntity(Tags.StartPlanet);
		controller.setShouldReleaseShip(false);

		AttachmentComponent attachmentComponent = GameComponents.getAttachmentComponent(startPlanet);
		if (attachmentComponent == null)
			return;

		if (attachmentComponent.getEntity() == null)
			return;

		Entity ship = attachmentComponent.getEntity();

		MovementComponent movementComponent = GameComponents.getMovementComponent(ship);
		Vector2 direction = movementComponent.getDirection();
		if (AngleUtils.minimumDifference(direction.angle(), 0) < 10)
			controller.setShouldReleaseShip(true);

		wayToDestinationPlanet = false;
		randomDirection = MathUtils.randomBoolean() ? 1f : -1f;
	}

}