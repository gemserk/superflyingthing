package com.gemserk.games.superflyingthing.templates;

import com.artemis.Entity;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.gemserk.animation4j.gdx.Animation;
import com.gemserk.commons.artemis.components.AnimationComponent;
import com.gemserk.commons.artemis.components.ContainerComponent;
import com.gemserk.commons.artemis.components.PhysicsComponent;
import com.gemserk.commons.artemis.components.PreviousStateSpatialComponent;
import com.gemserk.commons.artemis.components.RenderableComponent;
import com.gemserk.commons.artemis.components.ScriptComponent;
import com.gemserk.commons.artemis.components.SpatialComponent;
import com.gemserk.commons.artemis.components.SpriteComponent;
import com.gemserk.commons.artemis.components.TagComponent;
import com.gemserk.commons.artemis.templates.EntityTemplateImpl;
import com.gemserk.commons.gdx.box2d.BodyBuilder;
import com.gemserk.commons.gdx.games.PhysicsImpl;
import com.gemserk.commons.gdx.games.Spatial;
import com.gemserk.commons.gdx.games.SpatialPhysicsImpl;
import com.gemserk.componentsengine.utils.Container;
import com.gemserk.games.superflyingthing.ShipController;
import com.gemserk.games.superflyingthing.components.Components.AttachableComponent;
import com.gemserk.games.superflyingthing.components.Components.ControllerComponent;
import com.gemserk.games.superflyingthing.components.Components.HealthComponent;
import com.gemserk.games.superflyingthing.components.Components.MovementComponent;
import com.gemserk.games.superflyingthing.scripts.Behaviors.GrabGrabbableScript;
import com.gemserk.games.superflyingthing.scripts.Scripts.ShipScript;
import com.gemserk.games.superflyingthing.templates.EntityTemplates.CategoryBits;
import com.gemserk.resources.ResourceManager;

public class ShipTemplate extends EntityTemplateImpl {
	
	ResourceManager<String> resourceManager;
	BodyBuilder bodyBuilder;

	private final Vector2 direction = new Vector2();

	{
		parameters.put("maxLinearSpeed", new Float(4.5f));
		parameters.put("maxAngularVelocity", new Float(360f));
	}

	@Override
	public void apply(Entity e) {
		Animation rotationAnimation = resourceManager.getResourceValue("ShipAnimation");

		Spatial spatial = parameters.get("spatial");

		Float maxLinearSpeed = parameters.get("maxLinearSpeed");
		Float maxAngularVelocity = parameters.get("maxAngularVelocity");

		float angle = spatial.getAngle();
		float width = spatial.getWidth();
		float height = spatial.getHeight();

		direction.set(1f, 0f).rotate(angle);

		ShipController controller = parameters.get("controller");

		Body body = bodyBuilder //
				.fixture(bodyBuilder.fixtureDefBuilder() //
						.restitution(0f) //
						.categoryBits(CategoryBits.ShipCategoryBits) //
						.maskBits((short) (CategoryBits.AllCategoryBits & ~CategoryBits.MiniPlanetCategoryBits)) //
						.boxShape(width * 0.25f, height * 0.1f))//
				.mass(50f) //
				.position(spatial.getX(), spatial.getY()) //
				.type(BodyType.DynamicBody) //
				.userData(e) //
				.build();

		e.addComponent(new TagComponent(Groups.ship));
		e.addComponent(new PhysicsComponent(new PhysicsImpl(body)));

		e.addComponent(new SpatialComponent(new SpatialPhysicsImpl(body, width, height)));
		e.addComponent(new PreviousStateSpatialComponent());

		e.addComponent(new SpriteComponent(rotationAnimation.getCurrentFrame()));
		e.addComponent(new RenderableComponent(1));
		e.addComponent(new MovementComponent(direction.x, direction.y, maxLinearSpeed, maxAngularVelocity));
		e.addComponent(new AttachableComponent());
		e.addComponent(new ControllerComponent(controller));
		e.addComponent(new ScriptComponent(new ShipScript(), new GrabGrabbableScript()));
		e.addComponent(new AnimationComponent(new Animation[] { rotationAnimation }));
		e.addComponent(new ContainerComponent());
		e.addComponent(new HealthComponent(new Container(100f, 100f)));
	}
}