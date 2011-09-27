package com.gemserk.games.superflyingthing.templates;

import com.artemis.Entity;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.gemserk.animation4j.gdx.Animation;
import com.gemserk.commons.artemis.components.AnimationComponent;
import com.gemserk.commons.artemis.components.PhysicsComponent;
import com.gemserk.commons.artemis.components.RenderableComponent;
import com.gemserk.commons.artemis.components.ScriptComponent;
import com.gemserk.commons.artemis.components.SpatialComponent;
import com.gemserk.commons.artemis.components.SpriteComponent;
import com.gemserk.commons.artemis.events.EventManager;
import com.gemserk.commons.artemis.templates.EntityTemplateImpl;
import com.gemserk.commons.gdx.box2d.BodyBuilder;
import com.gemserk.commons.gdx.games.PhysicsImpl;
import com.gemserk.commons.gdx.games.SpatialPhysicsImpl;
import com.gemserk.games.superflyingthing.components.Components.GrabbableComponent;
import com.gemserk.games.superflyingthing.scripts.Behaviors.RemoveWhenGrabbedScript;
import com.gemserk.games.superflyingthing.scripts.Scripts.StarAnimationScript;
import com.gemserk.games.superflyingthing.scripts.Scripts.StarScript;
import com.gemserk.resources.ResourceManager;

public class StarTemplate extends EntityTemplateImpl {

	ResourceManager<String> resourceManager;
	BodyBuilder bodyBuilder;
	EventManager eventManager;

	@Override
	public void apply(Entity entity) {
		float radius = 0.3f;

		Animation rotateAnimation = resourceManager.getResourceValue("StarAnimation");
		Float x = parameters.get("x");
		Float y = parameters.get("y");

		Body body = bodyBuilder //
				.fixture(bodyBuilder.fixtureDefBuilder() //
						.sensor() //
						.restitution(0f) //
						.circleShape(radius)) //
				.mass(50f) //
				.position(x, y) //
				.type(BodyType.StaticBody) //
				.userData(entity) //
				.build();

		entity.addComponent(new PhysicsComponent(new PhysicsImpl(body)));
		entity.addComponent(new SpatialComponent(new SpatialPhysicsImpl(body, radius * 2, radius * 2)));

		entity.addComponent(new SpriteComponent(rotateAnimation.getCurrentFrame()));
		entity.addComponent(new RenderableComponent(3));
		entity.addComponent(new GrabbableComponent());
		entity.addComponent(new AnimationComponent(new Animation[] { rotateAnimation }));
		entity.addComponent(new ScriptComponent(new StarScript(eventManager), new RemoveWhenGrabbedScript(), new StarAnimationScript()));
	}

}