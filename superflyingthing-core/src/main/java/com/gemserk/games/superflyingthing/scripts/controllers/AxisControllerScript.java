package com.gemserk.games.superflyingthing.scripts.controllers;

import com.artemis.Entity;
import com.badlogic.gdx.Gdx;
import com.gemserk.animation4j.interpolator.function.InterpolationFunctions;
import com.gemserk.commons.artemis.scripts.ScriptJavaImpl;
import com.gemserk.commons.gdx.games.Spatial;
import com.gemserk.games.superflyingthing.ShipController;
import com.gemserk.games.superflyingthing.components.GameComponents;

public class AxisControllerScript extends ScriptJavaImpl {

	// TODO: this control defines the rotation direction based on the pressed position x coordinate and the next position x coordinate, is relative and it is normalized

	private final ShipController controller;
	private boolean isTouched = false;
	private float center = 0f;

	public AxisControllerScript(ShipController controller) {
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

		Spatial spatial = GameComponents.getSpatial(e);
		spatial.setPosition(-100, 0);

		if (!isTouched && Gdx.input.isTouched()) {
			isTouched = true;
			center = Gdx.input.getX();
		} else if (isTouched && !Gdx.input.isTouched()) {
			isTouched = false;
		}

		if (isTouched) {

			spatial.setPosition(center, Gdx.graphics.getHeight() * 0.5f);

			int value = Gdx.input.getX();
			float movementDirection = ControllerUtils.value(center, value, 0f, 2.5f,  60f, InterpolationFunctions.easeOut());
			controller.setMovementDirection(-movementDirection);
			return;
		}

		controller.setMovementDirection(0f);
	}

}