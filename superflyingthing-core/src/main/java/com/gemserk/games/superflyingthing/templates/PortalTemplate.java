package com.gemserk.games.superflyingthing.templates;

import com.artemis.Entity;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.gemserk.commons.artemis.components.PhysicsComponent;
import com.gemserk.commons.artemis.components.RenderableComponent;
import com.gemserk.commons.artemis.components.ScriptComponent;
import com.gemserk.commons.artemis.components.SpatialComponent;
import com.gemserk.commons.artemis.components.SpriteComponent;
import com.gemserk.commons.artemis.components.TagComponent;
import com.gemserk.commons.artemis.scripts.Script;
import com.gemserk.commons.artemis.templates.EntityTemplateImpl;
import com.gemserk.commons.gdx.box2d.BodyBuilder;
import com.gemserk.commons.gdx.games.PhysicsImpl;
import com.gemserk.commons.gdx.games.Spatial;
import com.gemserk.games.superflyingthing.Colors;
import com.gemserk.games.superflyingthing.components.Components;
import com.gemserk.games.superflyingthing.scripts.PortalScript;
import com.gemserk.resources.ResourceManager;

public class PortalTemplate extends EntityTemplateImpl {
	
	ResourceManager<String> resourceManager;
	BodyBuilder bodyBuilder;
	
	@Override
	public void apply(Entity entity) {

		String id = parameters.get("id");
		String targetPortalId = parameters.get("targetPortalId");
		String spriteId = parameters.get("sprite", "PortalSprite");
		Spatial spatial = parameters.get("spatial");
		Script script = parameters.get("script", new PortalScript());

		Sprite sprite = resourceManager.getResourceValue(spriteId);

		entity.addComponent(new TagComponent(id));
		entity.addComponent(new SpriteComponent(sprite, Colors.darkBlue));
		entity.addComponent(new Components.PortalComponent(targetPortalId, spatial.getAngle()));
		entity.addComponent(new RenderableComponent(-5));
		entity.addComponent(new SpatialComponent(spatial));
		entity.addComponent(new ScriptComponent(script));

		Body body = bodyBuilder //
				.fixture(bodyBuilder.fixtureDefBuilder() //
						.circleShape(spatial.getWidth() * 0.35f) //
						.categoryBits(CategoryBits.ObstacleCategoryBits) //
						.maskBits((short) (CategoryBits.AllCategoryBits & ~CategoryBits.ObstacleCategoryBits)) //
						.sensor()) //
				.position(spatial.getX(), spatial.getY()) //
				.mass(1f) //
				.type(BodyType.StaticBody) //
				.userData(entity) //
				.build();

		entity.addComponent(new PhysicsComponent(new PhysicsImpl(body)));

	}

}
