package com.gemserk.games.superflyingthing.templates;

import com.artemis.Entity;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.gemserk.commons.artemis.components.RenderableComponent;
import com.gemserk.commons.artemis.components.SpatialComponent;
import com.gemserk.commons.artemis.components.SpriteComponent;
import com.gemserk.commons.artemis.templates.EntityTemplateImpl;
import com.gemserk.commons.gdx.games.Spatial;
import com.gemserk.resources.ResourceManager;

public class StaticSpriteTemplate extends EntityTemplateImpl {

	ResourceManager<String> resourceManager;

	@Override
	public void apply(Entity entity) {
		Color color = parameters.get("color", Color.WHITE);
		Integer layer = parameters.get("layer");
		Spatial spatial = parameters.get("spatial");
		String spriteId = parameters.get("spriteId");
		Vector2 center = parameters.get("center");
		Sprite sprite = resourceManager.getResourceValue(spriteId);
		entity.addComponent(new SpatialComponent(spatial));
		entity.addComponent(new SpriteComponent(sprite, new Vector2(center), color));
		entity.addComponent(new RenderableComponent(layer));
	}

}