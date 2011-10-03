package com.gemserk.games.superflyingthing.templates;

import com.artemis.Component;
import com.artemis.Entity;
import com.artemis.World;
import com.badlogic.gdx.audio.Sound;
import com.gemserk.commons.artemis.components.ScriptComponent;
import com.gemserk.commons.artemis.events.Event;
import com.gemserk.commons.artemis.events.EventListener;
import com.gemserk.commons.artemis.events.EventManager;
import com.gemserk.commons.artemis.scripts.Script;
import com.gemserk.commons.artemis.scripts.ScriptJavaImpl;
import com.gemserk.commons.artemis.templates.EntityTemplateImpl;
import com.gemserk.commons.gdx.audio.SoundPlayer;
import com.gemserk.commons.reflection.Injector;
import com.gemserk.resources.Resource;
import com.gemserk.resources.ResourceManager;

/**
 * Plays a sound for the specified event.
 */
public class SoundSpawnerTemplate extends EntityTemplateImpl {

	static Class<SoundSpawnerComponent> soundSpawnerComponentClass = SoundSpawnerComponent.class;

	static class SoundSpawnerComponent extends Component {

		public String eventId;
		public Resource<Sound> sound;
		public EventListener listener;

		public SoundSpawnerComponent(String eventId, Resource<Sound> sound) {
			this.eventId = eventId;
			this.sound = sound;
		}

	}

	static class SoundSpawnerEventListener extends EventListener {

		SoundPlayer soundPlayer;
		Entity e;

		public SoundSpawnerEventListener(SoundPlayer soundPlayer, Entity e) {
			this.e = e;
			this.soundPlayer = soundPlayer;
		}

		@Override
		public void onEvent(Event event) {
			SoundSpawnerComponent soundSpawnerComponent = e.getComponent(soundSpawnerComponentClass);
			soundPlayer.play(soundSpawnerComponent.sound.get());
		}

	}

	public static class SoundSpawnerScript extends ScriptJavaImpl {

		SoundPlayer soundPlayer;
		EventManager eventManager;

		@Override
		public void init(World world, Entity e) {
			SoundSpawnerComponent soundSpawnerComponent = e.getComponent(soundSpawnerComponentClass);
			soundSpawnerComponent.listener = new SoundSpawnerEventListener(soundPlayer, e);
			eventManager.register(soundSpawnerComponent.eventId, soundSpawnerComponent.listener);
		}

		@Override
		public void dispose(World world, Entity e) {
			SoundSpawnerComponent soundSpawnerComponent = e.getComponent(soundSpawnerComponentClass);
			eventManager.unregister(soundSpawnerComponent.listener);
		}

	}

	ResourceManager<String> resourceManager;
	Injector injector;

	@Override
	public void apply(Entity entity) {
		String eventId = parameters.get("eventId");
		String soundId = parameters.get("soundId");

		Resource<Sound> sound = resourceManager.get(soundId);

		Script playSoundScript = injector.getInstance(SoundSpawnerScript.class);

		// injector.injectMembers(playSoundScript);

		entity.addComponent(new SoundSpawnerComponent(eventId, sound));
		entity.addComponent(new ScriptComponent(playSoundScript));
	}

}
