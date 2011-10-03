package com.gemserk.games.superflyingthing.scripts.controllers;

import com.artemis.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.gemserk.commons.artemis.components.CameraComponent;
import com.gemserk.commons.artemis.components.Components;
import com.gemserk.commons.artemis.scripts.ScriptJavaImpl;
import com.gemserk.commons.gdx.camera.Libgdx2dCamera;
import com.gemserk.commons.gdx.games.Spatial;
import com.gemserk.componentsengine.utils.AngleUtils;
import com.gemserk.games.superflyingthing.ShipController;
import com.gemserk.games.superflyingthing.components.GameComponents;
import com.gemserk.games.superflyingthing.entities.Groups;

public class TargetControllerScript extends ScriptJavaImpl {

	// TODO: this control is like an analogic controller defining the desired direction like JSnakes, with the exception is relative to the first pressed position.

	private final ShipController controller;

	private Vector2 position = new Vector2();
	private Vector2 desiredDirection = new Vector2();

	private Vector2 shipPosition = new Vector2();
	private Vector2 shipDirection = new Vector2();

	public TargetControllerScript(ShipController controller) {
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

		Entity mainCamera = world.getTagManager().getEntity(Groups.MainCamera);
		if (mainCamera == null)
			return;

		CameraComponent cameraComponent = Components.getCameraComponent(mainCamera);
		Libgdx2dCamera libgdx2dCamera = cameraComponent.getLibgdx2dCamera();

		Spatial shipSpatial = GameComponents.getSpatial(ship);
		shipPosition.set(shipSpatial.getPosition());
		float angle = shipSpatial.getAngle();

		libgdx2dCamera.project(shipPosition);

		for (int i = 0; i < 3; i++) {

			if (Gdx.input.isTouched(i)) {

				position.set(Gdx.input.getX(i), Gdx.graphics.getHeight() - Gdx.input.getY(i));

				desiredDirection.set(position);
				desiredDirection.sub(shipPosition);

				if (desiredDirection.len() > 0f)
					desiredDirection.nor();

				shipDirection.set(1f, 0f);
				shipDirection.rotate(angle);

				float angleDifference = (float) AngleUtils.minimumDifference(desiredDirection.angle(), angle);

				controller.setMovementDirection(getMovementDirectionForAngle(angleDifference));

				return;
			}

		}

		controller.setMovementDirection(0f);
	}

	private float getMovementDirectionForAngle(float angleDifference) {
		if (angleDifference > 10f)
			return -angleDifference / 45f;
		else if (angleDifference < -10f)
			return -angleDifference / 45f;
		else
			return 0f;
	}

}