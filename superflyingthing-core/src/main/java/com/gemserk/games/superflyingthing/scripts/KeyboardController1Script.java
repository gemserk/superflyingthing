package com.gemserk.games.superflyingthing.scripts;

import com.artemis.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.gemserk.commons.artemis.ScriptJavaImpl;
import com.gemserk.games.superflyingthing.ShipController;

public class KeyboardController1Script extends ScriptJavaImpl {
	
	// TODO: add some controller explanation 

	private final ShipController controller;

	public KeyboardController1Script(ShipController controller) {
		this.controller = controller;
	}

	@Override
	public void update(com.artemis.World world, Entity e) {
		updateMovementDirection(world, e);
		updateReleaseShip(world, e);
	}

	private void updateReleaseShip(com.artemis.World world, Entity e) {
		controller.setShouldReleaseShip(Gdx.input.isKeyPressed(Keys.SPACE));
	}

	public void updateMovementDirection(com.artemis.World world, Entity e) {
		float direction = 0f;

		if (Gdx.input.isKeyPressed(Keys.LEFT))
			direction = 1f;
		else if (Gdx.input.isKeyPressed(Keys.RIGHT))
			direction = -1f;

		float movementDirection = controller.getMovementDirection();
		movementDirection = ControllerUtils.calculateDirectionWithVariableSensibility(movementDirection, direction, 0.5f, (0.001f * world.getDelta()), 2f);
		controller.setMovementDirection(movementDirection);
	}

}