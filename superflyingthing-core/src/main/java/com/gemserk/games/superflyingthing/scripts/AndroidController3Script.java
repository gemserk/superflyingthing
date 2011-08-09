package com.gemserk.games.superflyingthing.scripts;

import com.artemis.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.gemserk.commons.artemis.ScriptJavaImpl;
import com.gemserk.commons.gdx.games.Spatial;
import com.gemserk.componentsengine.utils.AngleUtils;
import com.gemserk.games.superflyingthing.ShipController;
import com.gemserk.games.superflyingthing.components.ComponentWrapper;
import com.gemserk.games.superflyingthing.templates.Groups;

public class AndroidController3Script extends ScriptJavaImpl {

	// TODO: this control is like an analogic controller defining the desired direction like JSnakes, with the exception is relative to the first pressed position.

	private final ShipController controller;
	private boolean isTouched = false;

	private Vector2 position = new Vector2();

	private Vector2 desiredDirection = new Vector2();
	private Vector2 direction = new Vector2();

	public AndroidController3Script(ShipController controller) {
		this.controller = controller;
	}

	@Override
	public void update(com.artemis.World world, Entity e) {
		updateMovementDirection(world, e);
		updateReleaseShip(world, e);
	}

	private void updateReleaseShip(com.artemis.World world, Entity e) {
		controller.setShouldReleaseShip(Gdx.input.isTouched());
	}

	public void updateMovementDirection(com.artemis.World world, Entity e) {

		Entity ship = world.getTagManager().getEntity(Groups.ship);
		if (ship == null)
			return;

		Spatial shipSpatial = ComponentWrapper.getSpatial(ship);
		float angle = shipSpatial.getAngle();
		
		Spatial spatial = ComponentWrapper.getSpatial(e);
		spatial.setPosition(-100, 0);
		
		if (!isTouched && Gdx.input.isTouched()) {
			isTouched = true;
			position.x = Gdx.input.getX();
			position.y = Gdx.graphics.getHeight() - Gdx.input.getY();
		} else if (isTouched && !Gdx.input.isTouched()) {
			isTouched = false;
		}

		if (isTouched) {
			
			spatial.setPosition(position.x, position.y);

			float newY = Gdx.graphics.getHeight() - Gdx.input.getY();
			float newX = Gdx.input.getX();

			desiredDirection.set(newX, newY);
			desiredDirection.sub(position);

			if (desiredDirection.len() > 0f)
				desiredDirection.nor();

			direction.set(1f, 0f);
			direction.rotate(angle);

			double angleDifference = AngleUtils.minimumDifference(desiredDirection.angle(), angle);

			if (angleDifference > 10f) {
				controller.setMovementDirection(-(float) angleDifference / 45f);
			} else if (angleDifference < -10f) {
				controller.setMovementDirection(-(float) angleDifference / 45f);
			} else {
				controller.setMovementDirection(0f);
			}

			return;
		}

		controller.setMovementDirection(0f);
	}

}