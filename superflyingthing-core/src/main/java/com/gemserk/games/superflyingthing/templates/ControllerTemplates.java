package com.gemserk.games.superflyingthing.templates;

import com.artemis.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.input.RemoteInput;
import com.gemserk.commons.artemis.components.RenderableComponent;
import com.gemserk.commons.artemis.components.ScriptComponent;
import com.gemserk.commons.artemis.components.SpatialComponent;
import com.gemserk.commons.artemis.components.SpriteComponent;
import com.gemserk.commons.artemis.components.TagComponent;
import com.gemserk.commons.artemis.templates.EntityTemplate;
import com.gemserk.commons.artemis.templates.EntityTemplateImpl;
import com.gemserk.commons.gdx.games.Spatial;
import com.gemserk.commons.gdx.games.SpatialImpl;
import com.gemserk.games.superflyingthing.ShipController;
import com.gemserk.games.superflyingthing.components.Components.ControllerComponent;
import com.gemserk.games.superflyingthing.entities.Tags;
import com.gemserk.games.superflyingthing.scripts.controllers.AnalogControllerScript;
import com.gemserk.games.superflyingthing.scripts.controllers.AnalogKeyboardControllerScript;
import com.gemserk.games.superflyingthing.scripts.controllers.AndroidClassicControllerScript;
import com.gemserk.games.superflyingthing.scripts.controllers.AxisControllerScript;
import com.gemserk.games.superflyingthing.scripts.controllers.ControllerType;
import com.gemserk.games.superflyingthing.scripts.controllers.KeyboardControllerScript;
import com.gemserk.games.superflyingthing.scripts.controllers.TargetControllerScript;
import com.gemserk.games.superflyingthing.scripts.controllers.TiltAndroidControllerScript;
import com.gemserk.resources.ResourceManager;

public class ControllerTemplates {

	public static class KeyboardControllerTemplate extends EntityTemplateImpl {
		@Override
		public void apply(Entity entity) {
			ShipController controller = parameters.get("controller");
			String tag = parameters.get("tag", Tags.PlayerController);
			entity.addComponent(new ScriptComponent(new KeyboardControllerScript(controller)));
			entity.addComponent(new TagComponent(tag));
			entity.addComponent(new ControllerComponent(controller));
		}
	}

	public static class AnalogKeyboardControllerTemplate extends EntityTemplateImpl {
		@Override
		public void apply(Entity entity) {
			ShipController controller = parameters.get("controller");
			String tag = parameters.get("tag", Tags.PlayerController);
			entity.addComponent(new ScriptComponent(new AnalogKeyboardControllerScript(controller)));
			entity.addComponent(new TagComponent(tag));
			entity.addComponent(new ControllerComponent(controller));
		}
	}

	// touch classic controller
	public static class AndroidClassicControllerTemplate extends EntityTemplateImpl {
		@Override
		public void apply(Entity entity) {
			ShipController controller = parameters.get("controller");

			String tag = parameters.get("tag", Tags.PlayerController);
			Input input = parameters.get("input", Gdx.input);

			entity.addComponent(new ScriptComponent(new AndroidClassicControllerScript(controller, input)));
			entity.addComponent(new TagComponent(tag));
			entity.addComponent(new ControllerComponent(controller));
		}
	}

	public static class RemoteClassicControllerTemplate extends AndroidClassicControllerTemplate {

		// for now a static value, on a non default port to avoid conflicting
		private static Input remoteInput;

		private static Input getRemoteInputInstance() {
			if (remoteInput == null)
				remoteInput = new RemoteInput(8192);
			return remoteInput;
		}

		public RemoteClassicControllerTemplate() {
			parameters.put("input", getRemoteInputInstance());
		}

	}

	public static class AxisControllerTemplate extends EntityTemplateImpl {

		private final ResourceManager<String> resourceManager;

		{
			parameters.put("layer", new Integer(500));
		}

		public AxisControllerTemplate(ResourceManager<String> resourceManager) {
			this.resourceManager = resourceManager;
		}

		@Override
		public void apply(Entity entity) {
			ShipController controller = parameters.get("controller");
			String tag = parameters.get("tag", Tags.PlayerController);
			Integer layer = parameters.get("layer");

			String spriteId = parameters.get("spriteId", "WhiteRectangle");
			Spatial spatial = parameters.get("spatial", new SpatialImpl(0, 0, 2f, 1000f, 0f));

			Sprite sprite = resourceManager.getResourceValue(spriteId);

			entity.addComponent(new ScriptComponent(new AxisControllerScript(controller)));
			entity.addComponent(new TagComponent(tag));
			entity.addComponent(new SpriteComponent(sprite, new Color(1f, 1f, 1f, 0.3f)));
			entity.addComponent(new SpatialComponent(spatial));
			entity.addComponent(new RenderableComponent(layer));
			entity.addComponent(new ControllerComponent(controller));
		}
	}

	public static class AnalogControllerTemplate extends EntityTemplateImpl {

		private final ResourceManager<String> resourceManager;

		{
			parameters.put("layer", new Integer(500));
		}

		public AnalogControllerTemplate(ResourceManager<String> resourceManager) {
			this.resourceManager = resourceManager;
		}

		@Override
		public void apply(Entity entity) {
			ShipController controller = parameters.get("controller");
			String tag = parameters.get("tag", Tags.PlayerController);
			Integer layer = parameters.get("layer");

			String spriteId = parameters.get("spriteId", "WhiteRectangle");
			Spatial spatial = parameters.get("spatial", new SpatialImpl(0, 0, 8f, 8f, 0f));

			Sprite sprite = resourceManager.getResourceValue(spriteId);

			entity.addComponent(new ScriptComponent(new AnalogControllerScript(controller)));
			entity.addComponent(new TagComponent(tag));
			entity.addComponent(new SpriteComponent(sprite, new Color(1f, 1f, 1f, 0.5f)));
			entity.addComponent(new SpatialComponent(spatial));
			entity.addComponent(new RenderableComponent(layer));
			entity.addComponent(new ControllerComponent(controller));
		}
	}

	public static class TiltAndroidControllerTemplate extends EntityTemplateImpl {
		@Override
		public void apply(Entity entity) {
			ShipController controller = parameters.get("controller");
			String tag = parameters.get("tag", Tags.PlayerController);
			entity.addComponent(new ScriptComponent(new TiltAndroidControllerScript(controller)));
			entity.addComponent(new TagComponent(tag));
			entity.addComponent(new ControllerComponent(controller));
		}
	}

	public static class TargetControllerTemplate extends EntityTemplateImpl {
		@Override
		public void apply(Entity entity) {
			ShipController controller = parameters.get("controller");
			String tag = parameters.get("tag", Tags.PlayerController);
			entity.addComponent(new ScriptComponent(new TargetControllerScript(controller)));
			entity.addComponent(new TagComponent(tag));
			entity.addComponent(new ControllerComponent(controller));
		}
	}

	public EntityTemplate keyboardControllerTemplate;
	public EntityTemplate analogKeyboardControllerTemplate;
	public EntityTemplate androidClassicControllerTemplate;
	public EntityTemplate axisControllerTemplate;
	public EntityTemplate analogControllerTemplate;
	public EntityTemplate tiltAndroidControllerTemplate;
	public EntityTemplate targetControllerTemplate;
	public EntityTemplate remoteClassicControllerTemplate;

	public EntityTemplate getControllerTemplate(ControllerType controllerType) {
		switch (controllerType) {
		case KeyboardController:
			return keyboardControllerTemplate;
		case AnalogKeyboardController:
			return analogKeyboardControllerTemplate;
		case ClassicController:
			return androidClassicControllerTemplate;
		case AxisController:
			return axisControllerTemplate;
		case AnalogController:
			return analogControllerTemplate;
		case TiltController:
			return tiltAndroidControllerTemplate;
		case TargetController:
			return targetControllerTemplate;
		case RemoteClassicController:
			return remoteClassicControllerTemplate;
		}
		return keyboardControllerTemplate;
	}

}
