package com.gemserk.games.superflyingthing.templates;

import com.artemis.Entity;
import com.badlogic.gdx.audio.Sound;
import com.gemserk.commons.artemis.templates.EntityTemplateImpl;
import com.gemserk.games.superflyingthing.components.SoundSpawnerComponent;
import com.gemserk.resources.ResourceManager;

/**
 * Plays a sound for the specified event.
 */
public class SoundSpawnerTemplate extends EntityTemplateImpl {

	ResourceManager<String> resourceManager;

	@Override
	public void apply(Entity entity) {
		String eventId = parameters.get("eventId");
		String soundId = parameters.get("soundId");

		Sound sound = resourceManager.getResourceValue(soundId);

		entity.addComponent(new SoundSpawnerComponent(eventId, sound));
	}

}
