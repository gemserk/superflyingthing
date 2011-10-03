package com.gemserk.games.superflyingthing.templates;

import com.artemis.Entity;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.gemserk.animation4j.gdx.Animation;
import com.gemserk.commons.artemis.components.AnimationComponent;
import com.gemserk.commons.artemis.components.OwnerComponent;
import com.gemserk.commons.artemis.components.RenderableComponent;
import com.gemserk.commons.artemis.components.ScriptComponent;
import com.gemserk.commons.artemis.components.SpatialComponent;
import com.gemserk.commons.artemis.components.SpriteComponent;
import com.gemserk.commons.artemis.templates.EntityTemplateImpl;
import com.gemserk.commons.gdx.games.Spatial;
import com.gemserk.commons.gdx.games.SpatialHierarchicalImpl;
import com.gemserk.games.superflyingthing.Colors;
import com.gemserk.games.superflyingthing.components.GameComponents;
import com.gemserk.games.superflyingthing.scripts.Scripts;
import com.gemserk.resources.ResourceManager;

public class PlanetFillAnimationTemplate extends EntityTemplateImpl {
	
	ResourceManager<String> resourceManager;
	
	@Override
	public void apply(Entity entity) {
		Entity owner = parameters.get("owner");

		String animationId = parameters.get("animation", "PlanetFillAnimation");
		Color color = parameters.get("color", Colors.darkBlue);

		Animation planetFillAnimation = resourceManager.getResourceValue(animationId);
		Sprite sprite = planetFillAnimation.getCurrentFrame();

		Spatial ownerSpatial = GameComponents.getSpatial(owner);

		entity.addComponent(new SpatialComponent(new SpatialHierarchicalImpl(ownerSpatial)));
		entity.addComponent(new SpriteComponent(sprite, color));
		entity.addComponent(new RenderableComponent(-1));
		entity.addComponent(new AnimationComponent(new Animation[] { planetFillAnimation }));
		entity.addComponent(new OwnerComponent(owner));
		entity.addComponent(new ScriptComponent(new Scripts.UpdateAnimationScript()));
	}

}
