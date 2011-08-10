package com.gemserk.games.superflyingthing.scripts.controllers;

import com.artemis.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.gemserk.commons.artemis.scripts.ScriptJavaImpl;
import com.gemserk.games.superflyingthing.ShipController;
import com.gemserk.games.superflyingthing.scripts.ControllerUtils;

public class KeyboardControllerScript extends ScriptJavaImpl {
	
	// TODO: add some controller explanation 

	private final ShipController controller;
	
	private int releaseShipKey = Keys.SPACE;
	private int rotateLeftKey = Keys.LEFT;
	private int rotateRightKey = Keys.RIGHT;

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

		float movementDirection = controller.getMovementDirection();
		movementDirection = ControllerUtils.calculateDirectionWithVariableSensibility(movementDirection, direction, 0.1f, (0.001f * world.getDelta()), 5f);
		controller.setMovementDirection(movementDirection);
	}

}