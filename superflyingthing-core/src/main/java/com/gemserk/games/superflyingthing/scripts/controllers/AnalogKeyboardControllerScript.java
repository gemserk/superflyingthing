package com.gemserk.games.superflyingthing.scripts.controllers;

import com.artemis.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Vector2;
import com.gemserk.commons.artemis.scripts.ScriptJavaImpl;
import com.gemserk.commons.gdx.games.Spatial;
import com.gemserk.componentsengine.utils.AngleUtils;
import com.gemserk.games.superflyingthing.ShipController;
import com.gemserk.games.superflyingthing.components.GameComponents;
import com.gemserk.games.superflyingthing.entities.Groups;

public class AnalogKeyboardControllerScript extends ScriptJavaImpl {

	// TODO: add some controller explanation

	private final ShipController controller;
	private final Vector2 desiredDirection = new Vector2();
	private final Vector2 direction = new Vector2();

	private int releaseShipKey = Keys.SPACE;

	private int leftKey = Keys.LEFT;
	private int rightKey = Keys.RIGHT;
	private int upKey = Keys.UP;
	private int downKey = Keys.DOWN;

	public AnalogKeyboardControllerScript(ShipController controller) {
		this.controller = controller;
	}

	@Override
	public void update(com.artemis.World world, Entity e) {
		updateMovementDirection(world, e);
		updateReleaseShip(world, e);
	}

	private void updateReleaseShip(com.artemis.World world, Entity e) {
		controller.setShouldReleaseShip(Gdx.input.isKeyPressed(releaseShipKey));
	}

	public void updateMovementDirection(com.artemis.World world, Entity e) {
		Entity ship = world.getTagManager().getEntity(Groups.ship);
		if (ship == null)
			return;

		Spatial shipSpatial = GameComponents.getSpatial(ship);
		float angle = shipSpatial.getAngle();

		desiredDirection.set(0f, 0f);

		if (Gdx.input.isKeyPressed(leftKey))
			desiredDirection.x = -1f;
		else if (Gdx.input.isKeyPressed(rightKey))
			desiredDirection.x = 1f;

		if (Gdx.input.isKeyPressed(upKey))
			desiredDirection.y = 1f;
		else if (Gdx.input.isKeyPressed(downKey))
			desiredDirection.y = -1f;

		if (desiredDirection.len() == 0) {
			controller.setMovementDirection(0f);
			return;
		}

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

	}

}