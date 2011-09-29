package com.gemserk.games.superflyingthing.templates;

import com.artemis.Entity;
import com.gemserk.commons.artemis.components.CameraComponent;
import com.gemserk.commons.artemis.components.Components;
import com.gemserk.commons.artemis.components.ScriptComponent;
import com.gemserk.commons.artemis.scripts.ScriptJavaImpl;
import com.gemserk.commons.artemis.templates.EntityTemplateImpl;
import com.gemserk.commons.gdx.camera.Camera;
import com.gemserk.commons.gdx.camera.Libgdx2dCamera;

public class SecondCameraTemplate extends EntityTemplateImpl {

	@Override
	public void apply(Entity entity) {

		Libgdx2dCamera libgdx2dCamera = parameters.get("libgdx2dCamera");
		Camera camera = parameters.get("camera");

		entity.addComponent(new CameraComponent(libgdx2dCamera, camera));
		entity.addComponent(new ScriptComponent(new ScriptJavaImpl() {

			@Override
			public void update(com.artemis.World world, Entity e) {
				Entity mainCameraEntity = world.getTagManager().getEntity(Groups.MainCamera);
				CameraComponent mainCameraComponent = Components.getCameraComponent(mainCameraEntity);
				Camera mainCamera = mainCameraComponent.getCamera();

				CameraComponent cameraComponent = Components.getCameraComponent(e);

				Camera camera = cameraComponent.getCamera();

				camera.setPosition(mainCamera.getX(), mainCamera.getY());
				camera.setZoom(mainCamera.getZoom() * 0.25f);
				camera.setAngle(mainCamera.getAngle());
			}

		}));

	}

}
