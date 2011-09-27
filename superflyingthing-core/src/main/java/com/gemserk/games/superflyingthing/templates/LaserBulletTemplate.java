package com.gemserk.games.superflyingthing.templates;

import com.artemis.Entity;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.gemserk.commons.artemis.components.OwnerComponent;
import com.gemserk.commons.artemis.components.RenderableComponent;
import com.gemserk.commons.artemis.components.ScriptComponent;
import com.gemserk.commons.artemis.components.SpatialComponent;
import com.gemserk.commons.artemis.components.SpriteComponent;
import com.gemserk.commons.artemis.components.TimerComponent;
import com.gemserk.commons.artemis.scripts.Script;
import com.gemserk.commons.artemis.templates.EntityFactory;
import com.gemserk.commons.artemis.templates.EntityTemplateImpl;
import com.gemserk.commons.gdx.games.Spatial;
import com.gemserk.commons.gdx.games.SpatialHierarchicalImpl;
import com.gemserk.games.superflyingthing.Colors;
import com.gemserk.games.superflyingthing.components.ComponentWrapper;
import com.gemserk.games.superflyingthing.components.Components;
import com.gemserk.games.superflyingthing.scripts.LaserBulletScript;
import com.gemserk.resources.ResourceManager;

public class LaserBulletTemplate extends EntityTemplateImpl {

	ResourceManager<String> resourceManager;
	EntityFactory entityFactory;
	World physicsWorld;
	EntityTemplates entityTemplates;

	// {
	// parameters.put("x", new Float(0f));
	// parameters.put("y", new Float(0f));
	// parameters.put("angle", new Float(0f));
	// parameters.put("damage", new Float(1f));
	// parameters.put("color", Colors.lightBlue);
	// }

	@Override
	public void apply(Entity entity) {
		Sprite sprite = parameters.get("sprite", (Sprite) resourceManager.getResourceValue("LaserSprite"));
		// Script script = parameters.get("script", new LaserBulletScript(physicsWorld, entityFactory, getParticleEmitterTemplate()));
		// EntityTemplate particleEmitterTemplate = parameters.get("particleEmitterTemplate");
		Script script = new LaserBulletScript(physicsWorld, entityFactory, entityTemplates.particleEmitterTemplate);

		Integer duration = parameters.get("duration", 1000);
		Color color = parameters.get("color", Colors.lightBlue);

		Float x = parameters.get("x", 0f);
		Float y = parameters.get("y", 0f);
		Float angle = parameters.get("angle", 0f);
		Float damage = parameters.get("damage", 1f);

		Entity owner = parameters.get("owner");

		Spatial ownerSpatial = ComponentWrapper.getSpatial(owner);
		SpatialHierarchicalImpl bulletSpatial = new SpatialHierarchicalImpl(ownerSpatial, 1f, 0.1f);

		bulletSpatial.setPosition(x + ownerSpatial.getX(), y + ownerSpatial.getY());
		bulletSpatial.setAngle(angle + ownerSpatial.getAngle());

		entity.addComponent(new SpatialComponent(bulletSpatial));
		entity.addComponent(new ScriptComponent(script));
		entity.addComponent(new SpriteComponent(sprite, new Vector2(0f, 0.5f), color));
		entity.addComponent(new RenderableComponent(5));
		entity.addComponent(new TimerComponent((float) duration * 0.001f));
		entity.addComponent(new Components.DamageComponent(damage));
		entity.addComponent(new OwnerComponent(owner));
	}

}
