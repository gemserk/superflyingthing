package com.gemserk.games.superflyingthing.templates;

import com.artemis.Entity;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.World;
import com.gemserk.commons.artemis.components.ContainerComponent;
import com.gemserk.commons.artemis.components.PhysicsComponent;
import com.gemserk.commons.artemis.components.RenderableComponent;
import com.gemserk.commons.artemis.components.ScriptComponent;
import com.gemserk.commons.artemis.components.SpatialComponent;
import com.gemserk.commons.artemis.components.SpriteComponent;
import com.gemserk.commons.artemis.components.TagComponent;
import com.gemserk.commons.artemis.events.EventManager;
import com.gemserk.commons.artemis.templates.EntityTemplateImpl;
import com.gemserk.commons.gdx.box2d.BodyBuilder;
import com.gemserk.commons.gdx.box2d.JointBuilder;
import com.gemserk.commons.gdx.games.PhysicsImpl;
import com.gemserk.commons.gdx.games.SpatialPhysicsImpl;
import com.gemserk.games.superflyingthing.ShipController;
import com.gemserk.games.superflyingthing.components.Components.AttachmentComponent;
import com.gemserk.games.superflyingthing.components.Components.ControllerComponent;
import com.gemserk.games.superflyingthing.entities.Groups;
import com.gemserk.games.superflyingthing.scripts.Scripts.StartPlanetScript;
import com.gemserk.resources.ResourceManager;

public class StartPlanetTemplate extends EntityTemplateImpl {
	
	ResourceManager<String> resourceManager;
	BodyBuilder bodyBuilder;
	World physicsWorld;
	JointBuilder jointBuilder;
	EventManager eventManager;

	@Override
	public void apply(Entity entity) {

		Sprite sprite = resourceManager.getResourceValue("Planet");
		
		Float radius = parameters.get("radius");
		Float x = parameters.get("x");
		Float y = parameters.get("y");
		
		ShipController controller = parameters.get("controller");

		Body body = bodyBuilder //
				.fixture(bodyBuilder.fixtureDefBuilder() //
						.circleShape(radius * 0.1f) //
						.restitution(0f) //
						.categoryBits(CategoryBits.MiniPlanetCategoryBits)) //
				.position(x, y) //
				.mass(1f) //
				.type(BodyType.StaticBody) //
				.userData(entity) //
				.build();

		entity.addComponent(new TagComponent(Groups.startPlanet));
		entity.addComponent(new PhysicsComponent(new PhysicsImpl(body)));
		entity.addComponent(new SpatialComponent(new SpatialPhysicsImpl(body, radius * 2, radius * 2)));
		entity.addComponent(new AttachmentComponent());
		entity.addComponent(new SpriteComponent(sprite, Color.WHITE));
		entity.addComponent(new RenderableComponent(-2));
		entity.addComponent(new ControllerComponent(controller));
		entity.addComponent(new ScriptComponent(new StartPlanetScript(physicsWorld, jointBuilder, eventManager)));
		entity.addComponent(new ContainerComponent());
		
	}

}
