package com.gemserk.games.superflyingthing.scripts;

import com.artemis.Entity;
import com.gemserk.commons.artemis.scripts.ScriptJavaImpl;
import com.gemserk.commons.gdx.camera.Camera;
import com.gemserk.commons.gdx.camera.Libgdx2dCamera;
import com.gemserk.games.superflyingthing.components.ComponentWrapper;
import com.gemserk.games.superflyingthing.components.Components.CameraComponent;

public class UpdateLibgdxCameraScript extends ScriptJavaImpl {

	@Override
	public void update(com.artemis.World world, Entity e) {
		CameraComponent cameraComponent = ComponentWrapper.getCameraComponent(e);
		Camera camera = cameraComponent.getCamera();
		Libgdx2dCamera libgdxCamera = cameraComponent.getLibgdx2dCamera();
		libgdxCamera.move(camera.getX(), camera.getY());
		libgdxCamera.zoom(camera.getZoom());
		libgdxCamera.rotate(camera.getAngle());
	}

	
}