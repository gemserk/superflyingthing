package com.gemserk.games.superflyingthing.templates;

import com.artemis.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.gemserk.animation4j.gdx.Animation;
import com.gemserk.commons.artemis.components.AnimationComponent;
import com.gemserk.commons.artemis.components.PhysicsComponent;
import com.gemserk.commons.artemis.components.RenderableComponent;
import com.gemserk.commons.artemis.components.ScriptComponent;
import com.gemserk.commons.artemis.components.SpatialComponent;
import com.gemserk.commons.artemis.components.SpriteComponent;
import com.gemserk.commons.artemis.components.TagComponent;
import com.gemserk.commons.artemis.templates.EntityTemplateImpl;
import com.gemserk.commons.gdx.box2d.BodyBuilder;
import com.gemserk.commons.gdx.games.PhysicsImpl;
import com.gemserk.commons.gdx.games.SpatialPhysicsImpl;
import com.gemserk.games.superflyingthing.components.Components.AttachableComponent;
import com.gemserk.games.superflyingthing.components.Components.MovementComponent;
import com.gemserk.games.superflyingthing.entities.Groups;
import com.gemserk.games.superflyingthing.scripts.Scripts;
import com.gemserk.resources.ResourceManager;

public class AttachedShipTemplate extends EntityTemplateImpl {
	
	ResourceManager<String> resourceManager;
	BodyBuilder bodyBuilder;

	public AttachedShipTemplate() {
		parameters.put("maxLinearSpeed", new Float(4f));
		parameters.put("maxAngularVelocity", new Float(400f));
	}

	@Override
	public void apply(Entity e) {
		float width = 0.8f;
		float height = 0.8f;

		Animation rotationAnimation = resourceManager.getResourceValue("ShipAnimation");

		Vector2 position = parameters.get("position");

		Float maxLinearSpeed = parameters.get("maxLinearSpeed");
		Float maxAngularVelocity = parameters.get("maxAngularVelocity");

		Gdx.app.log("SuperFlyingThing", "Building new attached ship with " + maxLinearSpeed + ", " + maxAngularVelocity);

		Body body = bodyBuilder //
				.fixture(bodyBuilder.fixtureDefBuilder() //
						.restitution(0f) //
						.categoryBits(CategoryBits.ShipCategoryBits) //
						.maskBits((short) 0) //
						.boxShape(width * 0.125f, height * 0.125f)) //
				.mass(50f) //
				.position(position.x, position.y) //
				.type(BodyType.DynamicBody) //
				.userData(e) //
				.build();

		e.addComponent(new TagComponent(Groups.ship));
		e.addComponent(new PhysicsComponent(new PhysicsImpl(body)));
		e.addComponent(new SpatialComponent(new SpatialPhysicsImpl(body, width, height)));
		e.addComponent(new SpriteComponent(rotationAnimation.getCurrentFrame()));
		e.addComponent(new RenderableComponent(1));
		e.addComponent(new MovementComponent(1f, 0f, maxLinearSpeed, maxAngularVelocity));
		e.addComponent(new AttachableComponent());
		e.addComponent(new ScriptComponent(new Scripts.AttachedShipScript()));
		e.addComponent(new AnimationComponent(new Animation[] { rotationAnimation }));
	}
}