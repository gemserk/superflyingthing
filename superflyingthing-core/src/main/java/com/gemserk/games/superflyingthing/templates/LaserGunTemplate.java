package com.gemserk.games.superflyingthing.templates;

import com.artemis.Entity;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.gemserk.animation4j.gdx.Animation;
import com.gemserk.commons.artemis.components.AnimationComponent;
import com.gemserk.commons.artemis.components.ContainerComponent;
import com.gemserk.commons.artemis.components.OwnerComponent;
import com.gemserk.commons.artemis.components.RenderableComponent;
import com.gemserk.commons.artemis.components.ScriptComponent;
import com.gemserk.commons.artemis.components.SpatialComponent;
import com.gemserk.commons.artemis.components.SpriteComponent;
import com.gemserk.commons.artemis.templates.EntityFactory;
import com.gemserk.commons.artemis.templates.EntityTemplateImpl;
import com.gemserk.commons.gdx.games.SpatialImpl;
import com.gemserk.games.superflyingthing.Colors;
import com.gemserk.games.superflyingthing.components.Components;
import com.gemserk.games.superflyingthing.scripts.LaserGunScript;
import com.gemserk.resources.ResourceManager;

public class LaserGunTemplate extends EntityTemplateImpl {

	ResourceManager<String> resourceManager;
	EntityFactory entityFactory;
	World physicsWorld;
	EntityTemplates entityTemplates;

	{
		parameters.put("position", new Vector2());
		parameters.put("angle", new Float(0f));
		parameters.put("fireRate", new Integer(1000));
		parameters.put("bulletDuration", new Integer(250));
		parameters.put("currentReloadTime", new Integer(0));
		parameters.put("color", Colors.lightGreen);
	}

	@Override
	public void apply(Entity entity) {
		Animation idleAnimation = resourceManager.getResourceValue("LaserGunAnimation");

		Vector2 position = parameters.get("position");
		Float angle = parameters.get("angle");

		Integer fireRate = parameters.get("fireRate");
		Integer bulletDuration = parameters.get("bulletDuration");
		Integer currentReloadTime = parameters.get("currentReloadTime");

		Color color = parameters.get("color");

		Entity owner = parameters.get("owner");

		entity.addComponent(new SpatialComponent(new SpatialImpl(position.x, position.y, 1f, 1f, angle)));
		entity.addComponent(new ScriptComponent(new LaserGunScript(entityFactory)));
		entity.addComponent(new AnimationComponent(new Animation[] { idleAnimation }));
		entity.addComponent(new SpriteComponent(idleAnimation.getCurrentFrame(), color));
		entity.addComponent(new RenderableComponent(4));
		entity.addComponent(new Components.WeaponComponent(fireRate, bulletDuration, currentReloadTime, entityTemplates.laserBulletTemplate));
		entity.addComponent(new OwnerComponent(owner));
		entity.addComponent(new ContainerComponent());
	}

}
