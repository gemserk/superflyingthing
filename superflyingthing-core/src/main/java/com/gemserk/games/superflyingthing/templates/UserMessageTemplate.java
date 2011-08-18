package com.gemserk.games.superflyingthing.templates;

import com.artemis.Entity;
import com.artemis.World;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import com.gemserk.animation4j.animations.Animation;
import com.gemserk.animation4j.timeline.Builders;
import com.gemserk.animation4j.timeline.sync.MutableObjectSynchronizer;
import com.gemserk.animation4j.timeline.sync.SynchronizedAnimation;
import com.gemserk.animation4j.timeline.sync.TimelineSynchronizer;
import com.gemserk.commons.artemis.components.ScriptComponent;
import com.gemserk.commons.artemis.scripts.ScriptJavaImpl;
import com.gemserk.commons.artemis.templates.EntityTemplateWithDefaultParameters;
import com.gemserk.commons.gdx.gui.Container;
import com.gemserk.commons.gdx.gui.GuiControls;
import com.gemserk.commons.gdx.gui.Text;
import com.gemserk.games.superflyingthing.GlobalTime;
import com.gemserk.resources.ResourceManager;

public class UserMessageTemplate extends EntityTemplateWithDefaultParameters {

	private final Container guiContainer;
	private final ResourceManager<String> resourceManager;

	public UserMessageTemplate(Container guiContainer, ResourceManager<String> resourceManager) {
		this.guiContainer = guiContainer;
		this.resourceManager = resourceManager;

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
		
		float animationTime = (float) time  * 0.001f;

		final Text textControl = GuiControls.label(text) //
				.position(position.x, position.y) //
				.font(font) //
				.color(1f, 1f, 1f, 0f) //
				.build();

		final Animation animation = new SynchronizedAnimation(Builders.animation(Builders.timeline() //
				.value(Builders.timelineValue("color") //
						.keyFrame(0f, new Color(1f, 1f, 1f, 0f)) //
						.keyFrame(animationTime * 0.25f, Color.WHITE) //
						.keyFrame(animationTime * 0.75f, Color.WHITE) //
						.keyFrame(animationTime, new Color(1f, 1f, 1f, 0f)) //
				)) //
				.delay(0f) //
				.speed(1f) //
				.started(true) //
				.build(), //
				new TimelineSynchronizer(new MutableObjectSynchronizer(), textControl));

		animation.start(iterations);

		guiContainer.add(textControl);

		entity.addComponent(new ScriptComponent(new ScriptJavaImpl() {

			Text text = textControl;
			Animation internalAnimation = animation;

			@Override
			public void update(World world, Entity e) {
				internalAnimation.update(GlobalTime.getDelta());
				if (internalAnimation.isFinished()) {
					e.delete();
					guiContainer.remove(text);
				}
			}

		}));

	}

}
