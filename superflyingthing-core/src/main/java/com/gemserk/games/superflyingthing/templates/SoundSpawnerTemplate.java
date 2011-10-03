package com.gemserk.games.superflyingthing.templates;

import com.artemis.Entity;
import com.badlogic.gdx.audio.Sound;
import com.gemserk.commons.artemis.templates.EntityTemplateImpl;
import com.gemserk.commons.reflection.Injector;
import com.gemserk.games.superflyingthing.components.SoundSpawnerComponent;
import com.gemserk.resources.Resource;
import com.gemserk.resources.ResourceManager;

/**
 * Plays a sound for the specified event.
 */
public class SoundSpawnerTemplate extends EntityTemplateImpl {

	// static class SoundSpawnerEventListener extends EventListener {
	//
	// SoundPlayer soundPlayer;
	// Entity e;
	//
	// public SoundSpawnerEventListener(SoundPlayer soundPlayer, Entity e) {
	// this.e = e;
	// this.soundPlayer = soundPlayer;
	// }
	//
	// @Override
	// public void onEvent(Event event) {
	// SoundSpawnerComponent soundSpawnerComponent = GameComponents.getSoundSpawnerComponent(e);
	// soundPlayer.play(soundSpawnerComponent.sound.get());
	// }
	//
	// }
	//
	// public static class SoundSpawnerScript extends ScriptJavaImpl {
	//
	// SoundPlayer soundPlayer;
	// EventManager eventManager;
	//
	// @Override
	// public void init(World world, Entity e) {
	// SoundSpawnerComponent soundSpawnerComponent = GameComponents.getSoundSpawnerComponent(e);
	// soundSpawnerComponent.listener = new SoundSpawnerEventListener(soundPlayer, e);
	// eventManager.register(soundSpawnerComponent.eventId, soundSpawnerComponent.listener);
	// }
	//
	// @Override
	// public void dispose(World world, Entity e) {
	// SoundSpawnerComponent soundSpawnerComponent = GameComponents.getSoundSpawnerComponent(e);
	// eventManager.unregister(soundSpawnerComponent.listener);
	// }
	//
	// }

	ResourceManager<String> resourceManager;
	Injector injector;

	@Override
	public void apply(Entity entity) {
		String eventId = parameters.get("eventId");
		String soundId = parameters.get("soundId");

		Resource<Sound> sound = resourceManager.get(soundId);

		// Script playSoundScript = injector.getInstance(SoundSpawnerScript.class);

		entity.addComponent(new SoundSpawnerComponent(eventId, sound));
		// entity.addComponent(new ScriptComponent(playSoundScript));
	}

}
