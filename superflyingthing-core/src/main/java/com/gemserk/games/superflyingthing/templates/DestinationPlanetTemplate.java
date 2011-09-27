package com.gemserk.games.superflyingthing.templates;

import com.artemis.Entity;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.gemserk.commons.artemis.components.ContainerComponent;
import com.gemserk.commons.artemis.components.PhysicsComponent;
import com.gemserk.commons.artemis.components.RenderableComponent;
import com.gemserk.commons.artemis.components.ScriptComponent;
import com.gemserk.commons.artemis.components.SpatialComponent;
import com.gemserk.commons.artemis.components.SpriteComponent;
import com.gemserk.commons.artemis.events.EventManager;
import com.gemserk.commons.artemis.templates.EntityFactory;
import com.gemserk.commons.artemis.templates.EntityTemplateImpl;
import com.gemserk.commons.gdx.box2d.BodyBuilder;
import com.gemserk.commons.gdx.box2d.JointBuilder;
import com.gemserk.commons.gdx.games.PhysicsImpl;
import com.gemserk.commons.gdx.games.SpatialPhysicsImpl;
import com.gemserk.games.superflyingthing.components.Components.AttachmentComponent;
import com.gemserk.games.superflyingthing.scripts.Scripts.DestinationPlanetScript;
import com.gemserk.games.superflyingthing.templates.EntityTemplates.CategoryBits;
import com.gemserk.resources.ResourceManager;

public class DestinationPlanetTemplate extends EntityTemplateImpl {

	ResourceManager<String> resourceManager;
	BodyBuilder bodyBuilder;
	JointBuilder jointBuilder;
	EventManager eventManager;
	EntityFactory entityFactory;
	EntityTemplates entityTemplates;

	@Override
	public void apply(Entity entity) {
		Sprite sprite = resourceManager.getResourceValue("Planet");

		Float radius = parameters.get("radius");
		Float x = parameters.get("x");
		Float y = parameters.get("y");

		entity.setGroup(Groups.destinationPlanets);

		Body body = bodyBuilder //
				.fixture(bodyBuilder.fixtureDefBuilder() //
						.circleShape(radius * 0.1f) //
						.categoryBits(CategoryBits.MiniPlanetCategoryBits) //
						.restitution(0f)) //
				.fixture(bodyBuilder.fixtureDefBuilder() //
						.circleShape(radius * 1.5f) //
						.categoryBits(CategoryBits.AllCategoryBits) //
						.sensor()) //
				.position(x, y) //
				.mass(1f) //
				.type(BodyType.StaticBody) //
				.userData(entity) //
				.build();

		entity.addComponent(new PhysicsComponent(new PhysicsImpl(body)));
		entity.addComponent(new SpatialComponent(new SpatialPhysicsImpl(body, radius * 2, radius * 2)));
		entity.addComponent(new SpriteComponent(sprite, Color.WHITE));
		entity.addComponent(new RenderableComponent(-2));
		entity.addComponent(new AttachmentComponent());
		entity.addComponent(new ScriptComponent(new DestinationPlanetScript(eventManager, jointBuilder, entityFactory, entityTemplates.planetFillAnimationTemplate)));
		entity.addComponent(new ContainerComponent());

	}

}
