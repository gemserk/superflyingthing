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
import com.gemserk.resources.ResourceManager;

public class UserMessageTemplate extends EntityTemplateWithDefaultParameters {

	private final Container guiContainer;
	private final ResourceManager<String> resourceManager;

	public UserMessageTemplate(Container guiContainer, ResourceManager<String> resourceManager) {
		this.guiContainer = guiContainer;
		this.resourceManager = resourceManager;

		parameters.put("time", 2500);
		parameters.put("fontId", "GameFont");
	}

	@Override
	public void apply(Entity entity) {

		String fontId = parameters.get("fontId");
		String text = parameters.get("text");
		Vector2 position = parameters.get("position");

		final Integer time = parameters.get("time");
		BitmapFont font = resourceManager.getResourceValue(fontId);

		final Text textControl = GuiControls.label(text) //
				.position(position.x, position.y) //
				.font(font) //
				.color(1f, 1f, 1f, 0f) //
				.build();

		final Animation colorAnimation = new SynchronizedAnimation(Builders.animation(Builders.timeline() //
				.value(Builders.timelineValue("color") //
						.keyFrame(0, new Color(1f, 1f, 1f, 0f)) //
						.keyFrame(time * 0.25f, Color.WHITE) //
						.keyFrame(time * 0.75f, Color.WHITE) //
						.keyFrame(time, new Color(1f, 1f, 1f, 0f)) //
				)) //
				.delay(0f) //
				.speed(1f) //
				.started(true) //
				.build(), //
				new TimelineSynchronizer(new MutableObjectSynchronizer(), textControl));

		guiContainer.add(textControl);

		entity.addComponent(new ScriptComponent(new ScriptJavaImpl() {

			int aliveTime = time;
			Text text = textControl;
			Animation animation = colorAnimation;

			@Override
			public void update(World world, Entity e) {
				animation.update(world.getDelta());
				aliveTime -= world.getDelta();
				if (aliveTime <= 0) {
					e.delete();
					guiContainer.remove(text);
				}
			}

		}));

	}

}
