package com.gemserk.games.superflyingthing.scripts.controllers;

import com.artemis.Entity;
import com.badlogic.gdx.Gdx;
import com.gemserk.animation4j.interpolator.function.InterpolationFunctions;
import com.gemserk.commons.artemis.scripts.ScriptJavaImpl;
import com.gemserk.commons.gdx.games.Spatial;
import com.gemserk.games.superflyingthing.ShipController;
import com.gemserk.games.superflyingthing.components.ComponentWrapper;

public class TiltAndroidControllerScript extends ScriptJavaImpl {
	
	// TODO: add some controller explanation 

	private final ShipController controller;

	public TiltAndroidControllerScript(ShipController controller) {
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
		
			float roll = Gdx.input.getPitch();
			
		
		
			int value = Gdx.input.getX();
			float movementDirection = ControllerUtils.value(0, (int)roll, 0f, 3f,  30f, InterpolationFunctions.easeOut());
			controller.setMovementDirection(movementDirection);
			return;
	}

}