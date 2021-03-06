package com.gemserk.games.superflyingthing.scripts.controllers;

import com.artemis.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.gemserk.commons.artemis.scripts.ScriptJavaImpl;
import com.gemserk.commons.gdx.GlobalTime;
import com.gemserk.games.superflyingthing.ShipController;

public class AndroidClassicControllerScript extends ScriptJavaImpl {

	// TODO: add some controller explanation

	private final ShipController controller;
	
	public float minValue = 0.4f;
	public float speed = 2f;

	private final Input input;

	public AndroidClassicControllerScript(ShipController controller, Input input) {
		this.controller = controller;
		this.input = input;
	}

	@Override
	public void update(com.artemis.World world, Entity e) {
		updateMovementDirection(world, e);
		updateReleaseShip(world, e);
	}

	private void updateReleaseShip(com.artemis.World world, Entity e) {
		controller.setShouldReleaseShip(input.isTouched());
	}

	public void updateMovementDirection(com.artemis.World world, Entity e) {
		for (int i = 0; i < 5; i++) {
			if (!input.isTouched(i))
				continue;

			float direction = 0f;

			if (input.getX(i) < Gdx.graphics.getWidth() * 0.5f)
				direction = 1f;
			else if (input.getX(i) > Gdx.graphics.getWidth() * 0.5f)
				direction = -1f;

			// updateCount++;

			float movementDirection = controller.getMovementDirection();
			movementDirection = ControllerUtils.calculateDirectionWithVariableSensibility(movementDirection, direction, minValue, (GlobalTime.getDelta()), speed);
			controller.setMovementDirection(movementDirection);

			return;
		}

		// if (updateCount != 0)
		// Gdx.app.log("SuperFlyingThing", "updateCount: " + updateCount);
		// updateCount = 0;

		controller.setMovementDirection(0f);
	}

}