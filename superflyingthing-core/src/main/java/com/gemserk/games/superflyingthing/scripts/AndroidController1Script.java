package com.gemserk.games.superflyingthing.scripts;

import com.artemis.Entity;
import com.badlogic.gdx.Gdx;
import com.gemserk.commons.artemis.ScriptJavaImpl;
import com.gemserk.games.superflyingthing.ShipController;

public class AndroidController1Script extends ScriptJavaImpl {
	
	// TODO: add some controller explanation 

	private final ShipController controller;

	public AndroidController1Script(ShipController controller) {
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
		for (int i = 0; i < 5; i++) {
			if (!Gdx.input.isTouched(i))
				continue;

			float direction = 0f;

			if (Gdx.input.getX(i) < Gdx.graphics.getWidth() * 0.5f)
				direction = 1f;
			else if (Gdx.input.getX(i) > Gdx.graphics.getWidth() * 0.5f)
				direction = -1f;

			float movementDirection = controller.getMovementDirection();
			movementDirection = ControllerUtils.calculateDirectionWithVariableSensibility(movementDirection, direction, 0.1f, (0.001f * world.getDelta()), 5f);
			controller.setMovementDirection(movementDirection);
			
			return;
		}

		controller.setMovementDirection(0f);
	}

}