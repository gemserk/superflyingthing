package com.gemserk.games.superflyingthing.scripts;

import com.artemis.Entity;
import com.gemserk.commons.artemis.scripts.ScriptJavaImpl;
import com.gemserk.commons.gdx.camera.Camera;
import com.gemserk.commons.gdx.games.Spatial;
import com.gemserk.games.superflyingthing.components.ComponentWrapper;
import com.gemserk.games.superflyingthing.components.Components.CameraComponent;

public class UpdateCameraFromSpatialScript extends ScriptJavaImpl {

	@Override
	public void update(com.artemis.World world, Entity e) {
		Spatial spatial = ComponentWrapper.getSpatial(e);
		CameraComponent cameraComponent = ComponentWrapper.getCameraComponent(e);
		Camera camera = cameraComponent.getCamera();
		camera.setPosition(spatial.getX(), spatial.getY());
	}
	
}