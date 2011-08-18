package com.gemserk.games.superflyingthing.scripts.controllers;

import com.artemis.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.gemserk.commons.artemis.scripts.ScriptJavaImpl;
import com.gemserk.commons.gdx.GlobalTime;
import com.gemserk.games.superflyingthing.ShipController;

public class KeyboardControllerScript extends ScriptJavaImpl {

	// TODO: add some controller explanation

	private final ShipController controller;

	private int releaseShipKey = Keys.SPACE;
	private int rotateLeftKey = Keys.LEFT;
	private int rotateRightKey = Keys.RIGHT;

	// private int updateCount = 0;

	public KeyboardControllerScript(ShipController controller) {
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
		float direction = 0f;

		if (Gdx.input.isKeyPressed(rotateLeftKey))
			direction = 1f;
		else if (Gdx.input.isKeyPressed(rotateRightKey))
			direction = -1f;

		// if (direction != 0) {
		// updateCount++;
		// } else {
		// if (updateCount != 0)
		// Gdx.app.log("SuperFlyingThing", "updateCount: " + updateCount);
		// updateCount = 0;
		// }

		float movementDirection = controller.getMovementDirection();
		movementDirection = ControllerUtils.calculateDirectionWithVariableSensibility(movementDirection, direction, 0.05f, (GlobalTime.getDelta()), 5f);
		controller.setMovementDirection(movementDirection);
	}

}