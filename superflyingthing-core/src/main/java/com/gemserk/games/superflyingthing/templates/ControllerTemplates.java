package com.gemserk.games.superflyingthing.templates;

import com.artemis.Entity;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.gemserk.commons.artemis.components.RenderableComponent;
import com.gemserk.commons.artemis.components.ScriptComponent;
import com.gemserk.commons.artemis.components.SpatialComponent;
import com.gemserk.commons.artemis.components.SpriteComponent;
import com.gemserk.commons.artemis.templates.EntityTemplate;
import com.gemserk.commons.artemis.templates.ParametersWithFallBack;
import com.gemserk.commons.gdx.games.Spatial;
import com.gemserk.commons.gdx.games.SpatialImpl;
import com.gemserk.componentsengine.utils.Parameters;
import com.gemserk.games.superflyingthing.ShipController;
import com.gemserk.games.superflyingthing.components.TagComponent;
import com.gemserk.games.superflyingthing.scripts.AndroidController1Script;
import com.gemserk.games.superflyingthing.scripts.AndroidController2Script;
import com.gemserk.games.superflyingthing.scripts.AndroidController3Script;
import com.gemserk.games.superflyingthing.scripts.KeyboardController1Script;
import com.gemserk.resources.ResourceManager;

public class ControllerTemplates {
	
	public static class KeyboardControllerTemplate implements EntityTemplate {
		ParametersWithFallBack parameters = new ParametersWithFallBack();

		@Override
		public void apply(Entity entity, Parameters parameters) {
			this.parameters.setParameters(parameters);
			apply(entity);
		}

		@Override
		public void apply(Entity entity) {
			ShipController controller = parameters.get("controller");
			String tag = parameters.get("tag", "PlayerController");
			entity.addComponent(new ScriptComponent(new KeyboardController1Script(controller)));
			entity.addComponent(new TagComponent(tag));
		}
	}
	
	public static class AndroidClassicControllerTemplate implements EntityTemplate {
		ParametersWithFallBack parameters = new ParametersWithFallBack();

		@Override
		public void apply(Entity entity, Parameters parameters) {
			this.parameters.setParameters(parameters);
			apply(entity);
		}

		@Override
		public void apply(Entity entity) {
			ShipController controller = parameters.get("controller");
			String tag = parameters.get("tag", "PlayerController");
			entity.addComponent(new ScriptComponent(new AndroidController1Script(controller)));
			entity.addComponent(new TagComponent(tag));
		}
	}
	
	public static class AxisControllerTemplate implements EntityTemplate {
		
		private final ResourceManager<String> resourceManager;

		ParametersWithFallBack parameters = new ParametersWithFallBack();
		{
			parameters.put("layer", new Integer(500));
		}
		
		public AxisControllerTemplate(ResourceManager<String> resourceManager) {
			this.resourceManager = resourceManager;
		}

		@Override
		public void apply(Entity entity, Parameters parameters) {
			this.parameters.setParameters(parameters);
			apply(entity);
		}

		@Override
		public void apply(Entity entity) {
			ShipController controller = parameters.get("controller");
			String tag = parameters.get("tag", "PlayerController");
			Integer layer = parameters.get("layer"); 

			String spriteId = parameters.get("spriteId", "WhiteRectangle"); 
			Spatial spatial = parameters.get("spatial", new SpatialImpl(0, 0, 2f, 1000f, 0f));
			
			Sprite sprite = resourceManager.getResourceValue(spriteId);
			
			entity.addComponent(new ScriptComponent(new AndroidController2Script(controller)));
			entity.addComponent(new TagComponent(tag));
			entity.addComponent(new SpriteComponent(sprite, new Color(1f, 1f, 1f, 0.3f)));
			entity.addComponent(new SpatialComponent(spatial));
			entity.addComponent(new RenderableComponent(layer));
		}
	}
	
	public static class AnalogControllerTemplate implements EntityTemplate {
		
		private final ResourceManager<String> resourceManager;

		ParametersWithFallBack parameters = new ParametersWithFallBack();
		{
			parameters.put("layer", new Integer(500));
		}
		
		public AnalogControllerTemplate(ResourceManager<String> resourceManager) {
			this.resourceManager = resourceManager;
		}

		@Override
		public void apply(Entity entity, Parameters parameters) {
			this.parameters.setParameters(parameters);
			apply(entity);
		}

		@Override
		public void apply(Entity entity) {
			ShipController controller = parameters.get("controller");
			String tag = parameters.get("tag", "PlayerController");
			Integer layer = parameters.get("layer"); 

			String spriteId = parameters.get("spriteId", "WhiteRectangle"); 
			Spatial spatial = parameters.get("spatial", new SpatialImpl(0, 0, 8f, 8f, 0f));
			
			Sprite sprite = resourceManager.getResourceValue(spriteId);
			
			entity.addComponent(new ScriptComponent(new AndroidController3Script(controller)));
			entity.addComponent(new TagComponent(tag));
			entity.addComponent(new SpriteComponent(sprite, new Color(1f, 1f, 1f, 0.5f)));
			entity.addComponent(new SpatialComponent(spatial));
			entity.addComponent(new RenderableComponent(layer));
		}
	}
	
	public EntityTemplate keyboardControllerTemplate;
	public EntityTemplate androidClassicControllerTemplate;
	public EntityTemplate axisControllerTemplate;
	public EntityTemplate analogControllerTemplate;

	
}
