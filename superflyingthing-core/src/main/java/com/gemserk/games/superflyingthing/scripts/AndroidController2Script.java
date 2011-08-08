package com.gemserk.games.superflyingthing.scripts;

import com.artemis.Entity;
import com.badlogic.gdx.Gdx;
import com.gemserk.commons.artemis.ScriptJavaImpl;
import com.gemserk.games.superflyingthing.ShipController;

public class AndroidController2Script extends ScriptJavaImpl {

	// TODO: this control defines the rotation direction based on the pressed position x coordinate and the next position x coordinate, is relative and it is normalized

	private final ShipController controller;
	private boolean isTouched = false;
	private float center = 0f;

	public AndroidController2Script(ShipController controller) {
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

		if (!isTouched && Gdx.input.isTouched()) {
			isTouched = true;
			center = Gdx.input.getX();
		} else if (isTouched && !Gdx.input.isTouched()) {
			isTouched = false;
		}

		if (isTouched) {
			int value = Gdx.input.getX();
			float movementDirection = ControllerUtils.value(center, value, 0f, Gdx.graphics.getWidth() * 0.05f, Gdx.graphics.getWidth() * 0.2f);
			controller.setMovementDirection(-movementDirection);
			return;
		}

		controller.setMovementDirection(0f);
	}

}