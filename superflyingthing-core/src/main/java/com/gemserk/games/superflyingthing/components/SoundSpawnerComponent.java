package com.gemserk.games.superflyingthing.components;

import com.artemis.Component;
import com.badlogic.gdx.audio.Sound;
import com.gemserk.commons.artemis.events.EventListener;
import com.gemserk.resources.Resource;

public class SoundSpawnerComponent extends Component {

	public String eventId;
	public Resource<Sound> sound;
	public EventListener listener;

	public SoundSpawnerComponent(String eventId, Resource<Sound> sound) {
		this.eventId = eventId;
		this.sound = sound;
	}

}