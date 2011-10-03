package com.gemserk.games.superflyingthing.templates;

import com.artemis.Entity;
import com.gemserk.commons.artemis.components.CameraComponent;
import com.gemserk.commons.artemis.components.PreviousStateCameraComponent;
import com.gemserk.commons.artemis.components.ScriptComponent;
import com.gemserk.commons.artemis.components.SpatialComponent;
import com.gemserk.commons.artemis.components.TagComponent;
import com.gemserk.commons.artemis.events.EventManager;
import com.gemserk.commons.artemis.templates.EntityTemplateImpl;
import com.gemserk.commons.gdx.camera.Camera;
import com.gemserk.commons.gdx.camera.CameraImpl;
import com.gemserk.commons.gdx.camera.Libgdx2dCamera;
import com.gemserk.commons.gdx.games.Spatial;
import com.gemserk.games.superflyingthing.components.Components.TargetComponent;
import com.gemserk.games.superflyingthing.entities.Tags;
import com.gemserk.games.superflyingthing.scripts.CameraScript;
import com.gemserk.games.superflyingthing.scripts.UpdateCameraFromSpatialScript;
import com.gemserk.games.superflyingthing.scripts.UpdateLibgdxCameraScript;

public class CameraTemplate extends EntityTemplateImpl {

	EventManager eventManager;

	@Override
	public void apply(Entity entity) {

		Camera camera = parameters.get("camera");
		Libgdx2dCamera libgdxCamera = parameters.get("libgdxCamera");
		Spatial spatial = parameters.get("spatial");
		Entity target = parameters.get("target");

		entity.addComponent(new TagComponent(Tags.MainCamera));
		entity.addComponent(new TargetComponent(target));

		entity.addComponent(new CameraComponent(libgdxCamera, camera));
		entity.addComponent(new PreviousStateCameraComponent(new CameraImpl()));

		entity.addComponent(new SpatialComponent(spatial));
		entity.addComponent(new ScriptComponent(new CameraScript(eventManager), //
				new UpdateCameraFromSpatialScript(), new UpdateLibgdxCameraScript()));

	}
}