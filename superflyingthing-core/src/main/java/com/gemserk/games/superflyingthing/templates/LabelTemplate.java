package com.gemserk.games.superflyingthing.templates;

import com.artemis.Entity;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import com.gemserk.commons.artemis.components.RenderableComponent;
import com.gemserk.commons.artemis.components.SpatialComponent;
import com.gemserk.commons.artemis.components.TextComponent;
import com.gemserk.commons.artemis.templates.EntityTemplateImpl;
import com.gemserk.commons.gdx.games.SpatialImpl;
import com.gemserk.resources.ResourceManager;

public class LabelTemplate extends EntityTemplateImpl {

	ResourceManager<String> resourceManager;

	@Override
	public void apply(Entity entity) {
		String fontId = parameters.get("fontId");
		String text = parameters.get("text");
		Integer layer = parameters.get("layer");
		Vector2 position = parameters.get("position");
		Vector2 center = parameters.get("center", new Vector2(0.5f, 0.5f));

		BitmapFont font = resourceManager.getResourceValue(fontId);

		entity.addComponent(new SpatialComponent(new SpatialImpl(position.x, position.y)));
		entity.addComponent(new TextComponent(text, font, 0f, 0f, center.x, center.y));
		entity.addComponent(new RenderableComponent(layer));
	}

}
