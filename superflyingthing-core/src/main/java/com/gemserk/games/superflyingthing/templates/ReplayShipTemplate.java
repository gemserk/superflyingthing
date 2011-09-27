package com.gemserk.games.superflyingthing.templates;

import com.artemis.Entity;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.gemserk.animation4j.gdx.Animation;
import com.gemserk.commons.artemis.components.AnimationComponent;
import com.gemserk.commons.artemis.components.PhysicsComponent;
import com.gemserk.commons.artemis.components.PreviousStateSpatialComponent;
import com.gemserk.commons.artemis.components.RenderableComponent;
import com.gemserk.commons.artemis.components.ScriptComponent;
import com.gemserk.commons.artemis.components.SpatialComponent;
import com.gemserk.commons.artemis.components.SpriteComponent;
import com.gemserk.commons.artemis.components.TagComponent;
import com.gemserk.commons.artemis.templates.EntityTemplateImpl;
import com.gemserk.commons.gdx.box2d.BodyBuilder;
import com.gemserk.commons.gdx.games.SpatialPhysicsImpl;
import com.gemserk.games.superflyingthing.components.Components.ReplayComponent;
import com.gemserk.games.superflyingthing.components.Replay;
import com.gemserk.games.superflyingthing.scripts.Behaviors.GrabGrabbableScript;
import com.gemserk.games.superflyingthing.scripts.Behaviors.ShipAnimationScript;
import com.gemserk.games.superflyingthing.templates.EntityTemplates.CategoryBits;
import com.gemserk.resources.ResourceManager;

public class ReplayShipTemplate extends EntityTemplateImpl {

	ResourceManager<String> resourceManager;
	BodyBuilder bodyBuilder;

	@Override
	public void apply(Entity entity) {

		float width = 0.8f;
		float height = 0.8f;

		Animation rotationAnimation = resourceManager.getResourceValue("ShipAnimation");

		Replay replay = parameters.get("replay");

		boolean mainReplay = replay.main;

		Color color = mainReplay ? Color.WHITE : new Color(0.5f, 0.5f, 0.5f, 1f);

		entity.setGroup(Groups.ReplayShipGroup);

		if (mainReplay)
			entity.addComponent(new TagComponent(Groups.MainReplayShip));

		Body body = bodyBuilder //
				.fixture(bodyBuilder.fixtureDefBuilder() //
						.restitution(0f) //
						.categoryBits(CategoryBits.ShipCategoryBits) //
						.maskBits((short) (CategoryBits.AllCategoryBits & ~CategoryBits.MiniPlanetCategoryBits)) //
						.boxShape(width * 0.25f, height * 0.1f))//
				.mass(50f) //
				.position(0f, 0f) //
				.type(BodyType.DynamicBody) //
				.userData(entity) //
				.build();

		entity.addComponent(new ReplayComponent(replay));

		entity.addComponent(new PhysicsComponent(body));
		entity.addComponent(new SpatialComponent(new SpatialPhysicsImpl(body, width, height)));
		entity.addComponent(new PreviousStateSpatialComponent());

		entity.addComponent(new AnimationComponent(new Animation[] { rotationAnimation }));
		entity.addComponent(new SpriteComponent(rotationAnimation.getCurrentFrame(), color));
		entity.addComponent(new RenderableComponent(0));

		entity.addComponent(new ScriptComponent( //
				new ShipAnimationScript(), //
				new GrabGrabbableScript()));

	}

}
