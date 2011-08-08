package com.gemserk.games.superflyingthing.scripts;

import com.artemis.Entity;
import com.badlogic.gdx.Gdx;
import com.gemserk.commons.artemis.ScriptJavaImpl;
import com.gemserk.commons.gdx.games.Spatial;
import com.gemserk.componentsengine.utils.AngleUtils;
import com.gemserk.games.superflyingthing.ShipController;
import com.gemserk.games.superflyingthing.components.ComponentWrapper;
import com.gemserk.games.superflyingthing.templates.Groups;

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

		Entity ship = world.getTagManager().getEntity(Groups.ship);
		if (ship == null)
			return;

		Spatial spatial = ComponentWrapper.getSpatial(ship);
		float angle = spatial.getAngle();

		if (!isTouched && Gdx.input.isTouched()) {
			isTouched = true;
			center = Gdx.input.getY();
		} else if (isTouched && !Gdx.input.isTouched()) {
			isTouched = false;
		}

		if (isTouched) {
			// int value = Gdx.input.getY();
			// float movementDirection = ControllerUtils.value(center, value, 0f, Gdx.graphics.getHeight() * 0.2f);

			int value = Gdx.input.getX();
			float movementDirection = ControllerUtils.value(center, value, 0f, Gdx.graphics.getWidth() * 0.2f);

			double diff = AngleUtils.minimumDifference(0, angle);
			if (diff < 90 || diff > -90)
				controller.setMovementDirection(-movementDirection);
			else
				controller.setMovementDirection(movementDirection);

			return;
		}

		controller.setMovementDirection(0f);
	}

}