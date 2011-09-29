package com.gemserk.games.superflyingthing.templates;

import com.artemis.Entity;
import com.artemis.World;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import com.gemserk.animation4j.timeline.Builders;
import com.gemserk.animation4j.timeline.TimelineAnimation;
import com.gemserk.commons.artemis.components.Components;
import com.gemserk.commons.artemis.components.RenderableComponent;
import com.gemserk.commons.artemis.components.ScriptComponent;
import com.gemserk.commons.artemis.components.SpatialComponent;
import com.gemserk.commons.artemis.components.TextComponent;
import com.gemserk.commons.artemis.scripts.ScriptJavaImpl;
import com.gemserk.commons.artemis.templates.EntityTemplateImpl;
import com.gemserk.commons.gdx.GlobalTime;
import com.gemserk.commons.gdx.games.SpatialImpl;
import com.gemserk.resources.ResourceManager;

public class UserMessageTemplate extends EntityTemplateImpl {

	ResourceManager<String> resourceManager;

	public UserMessageTemplate() {
		parameters.put("time", new Integer(2500));
		parameters.put("fontId", "GameFont");
		parameters.put("iterations", new Integer(1));
	}

	@Override
	public void apply(Entity entity) {

		String fontId = parameters.get("fontId");
		String text = parameters.get("text");
		Vector2 position = parameters.get("position");

		final Integer iterations = parameters.get("iterations");
		final Integer time = parameters.get("time");
		BitmapFont font = resourceManager.getResourceValue(fontId);

		float animationTime = (float) time * 0.001f;

		final TimelineAnimation animation = Builders.animation(Builders.timeline() //
				.value(Builders.timelineValue("color") //
						.keyFrame(0f, new Color(1f, 1f, 1f, 0f)) //
						.keyFrame(animationTime * 0.25f, Color.WHITE) //
						.keyFrame(animationTime * 0.75f, Color.WHITE) //
						.keyFrame(animationTime, new Color(1f, 1f, 1f, 0f)) //
				)) //
				.delay(0f) //
				.speed(1f) //
				.started(true) //
				.build();

		animation.start(iterations);

		entity.addComponent(new SpatialComponent(new SpatialImpl(position.x, position.y)));
		entity.addComponent(new TextComponent(text, font, 0f, 0f, 0.5f, 0.5f));
		entity.addComponent(new RenderableComponent(260));

		entity.addComponent(new ScriptComponent(new ScriptJavaImpl() {

			TimelineAnimation internalAnimation = animation;

			@Override
			public void update(World world, Entity e) {
				internalAnimation.update(GlobalTime.getDelta());
				if (internalAnimation.isFinished())
					e.delete();
				TextComponent textComponent = Components.getTextComponent(e);
				Color color = internalAnimation.getValue("color");
				textComponent.color.set(color);
			}

		}));

	}

}
