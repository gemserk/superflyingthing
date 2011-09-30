package com.gemserk.games.superflyingthing.templates;

import com.artemis.Entity;
import com.artemis.World;
import com.badlogic.gdx.audio.Sound;
import com.gemserk.commons.artemis.components.ScriptComponent;
import com.gemserk.commons.artemis.events.Event;
import com.gemserk.commons.artemis.events.EventListener;
import com.gemserk.commons.artemis.events.EventManager;
import com.gemserk.commons.artemis.scripts.ScriptJavaImpl;
import com.gemserk.commons.artemis.templates.EntityTemplateImpl;
import com.gemserk.commons.gdx.audio.SoundPlayer;
import com.gemserk.commons.reflection.Injector;
import com.gemserk.resources.Resource;
import com.gemserk.resources.ResourceManager;

/**
 * Plays a sound for the specified event.
 */
public class SoundTemplate extends EntityTemplateImpl {

	// static class SoundComponent extends Component {
	//
	// public Resource<Sound> sound;
	//
	// public SoundComponent(Resource<Sound> sound) {
	// this.sound = sound;
	// }
	//
	// }
	
	// could this be transformed to a system registering the stuff all from the component?

	static class PlaySoundScript extends ScriptJavaImpl {

		SoundPlayer soundPlayer;
		EventManager eventManager;

		private final String eventId;
		private final Resource<Sound> sound;

		public PlaySoundScript(String eventId, Resource<Sound> sound) {
			this.eventId = eventId;
			this.sound = sound;
		}

		@Override
		public void init(World world, Entity e) {
			eventManager.register(eventId, new EventListener() {
				@Override
				public void onEvent(Event event) {
					playSound(event);
				}
			});
		}

		public void playSound(Event e) {
			soundPlayer.play(sound.get());
		}

	}

	ResourceManager<String> resourceManager;
	Injector injector;

	@Override
	public void apply(Entity entity) {

		String eventId = parameters.get("eventId");
		String soundId = parameters.get("soundId");

		Resource<Sound> sound = resourceManager.get(soundId);

		PlaySoundScript playSoundScript = new PlaySoundScript(eventId, sound);

		injector.injectMembers(playSoundScript);

		entity.addComponent(new ScriptComponent(playSoundScript));

	}

}
