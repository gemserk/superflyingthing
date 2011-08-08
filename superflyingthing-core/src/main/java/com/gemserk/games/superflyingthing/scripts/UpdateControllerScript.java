package com.gemserk.games.superflyingthing.scripts;

import com.artemis.Entity;
import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.gemserk.commons.artemis.ScriptJavaImpl;
import com.gemserk.games.superflyingthing.ShipController;

public class UpdateControllerScript extends ScriptJavaImpl {
	
	private final ShipController controller;

	public UpdateControllerScript(ShipController controller) {
		this.controller = controller;
	}

	@Override
	public void update(com.artemis.World world, Entity e) {
		updateMovementDirection(world, e);
		updateReleaseShip(world, e);
	}
	
	private void updateReleaseShip(com.artemis.World world, Entity e) {
		if (Gdx.app.getType() == ApplicationType.Android)
			controller.setShouldReleaseShip(Gdx.input.isTouched());
		else
			controller.setShouldReleaseShip(Gdx.input.isKeyPressed(Keys.SPACE));
	}

	public void updateMovementDirection(com.artemis.World world, Entity e) {
		if (Gdx.app.getType() != ApplicationType.Android) {
			float direction = 0f;

			if (Gdx.input.isKeyPressed(Keys.LEFT))
				direction = 1f;
			else if (Gdx.input.isKeyPressed(Keys.RIGHT))
				direction = -1f;
			
			float movementDirection = controller.getMovementDirection();
			movementDirection = ControllerUtils.calculateDirectionWithVariableSensibility(movementDirection, direction, 0.5f, (0.001f * world.getDelta()), 2f);
			controller.setMovementDirection(movementDirection);
		} else {

			for (int i = 0; i < 5; i++) {
				if (!Gdx.input.isTouched(i))
					continue;

				float direction = 0f;

				if (Gdx.input.getX(i) < Gdx.graphics.getWidth() * 0.5f)
					direction = 1f;
				else if (Gdx.input.getX(i) > Gdx.graphics.getWidth() * 0.5f)
					direction = -1f;

				float movementDirection = controller.getMovementDirection();
				movementDirection = ControllerUtils.calculateDirectionWithVariableSensibility(movementDirection, direction, 0.5f, (0.001f * world.getDelta()), 2f);
				controller.setMovementDirection(movementDirection);

				return;
			}

			controller.setMovementDirection(0f);

		}
	}

}