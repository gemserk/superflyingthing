package com.gemserk.games.superflyingthing.scripts;

import com.artemis.Entity;
import com.gemserk.commons.artemis.components.CameraComponent;
import com.gemserk.commons.artemis.components.Components;
import com.gemserk.commons.artemis.components.PreviousStateCameraComponent;
import com.gemserk.commons.artemis.components.SpatialComponent;
import com.gemserk.commons.artemis.scripts.ScriptJavaImpl;
import com.gemserk.commons.gdx.camera.Camera;
import com.gemserk.commons.gdx.games.Spatial;

public class UpdateCameraFromSpatialScript extends ScriptJavaImpl {

	@Override
	public void update(com.artemis.World world, Entity e) {
		// Spatial spatial = ComponentWrapper.getSpatial(e);
		// CameraComponent cameraComponent = ComponentWrapper.getCameraComponent(e);
		// Camera camera = cameraComponent.getCamera();
		// camera.setPosition(spatial.getX(), spatial.getY());

		CameraComponent cameraComponent = Components.getCameraComponent(e);
		Camera camera = cameraComponent.getCamera();

		// store previous camera state, to be used for interpolation
		PreviousStateCameraComponent previousStateCameraComponent = Components.getPreviousStateCameraComponent(e);

		if (previousStateCameraComponent != null) {
			Camera previousCamera = previousStateCameraComponent.getCamera();
			previousCamera.setPosition(camera.getX(), camera.getY());
			previousCamera.setAngle(camera.getAngle());
			previousCamera.setZoom(camera.getZoom());
		}

		SpatialComponent spatialComponent = Components.getSpatialComponent(e);
		Spatial spatial = spatialComponent.getSpatial();

		camera.setPosition(spatial.getX(), spatial.getY());
	}

}