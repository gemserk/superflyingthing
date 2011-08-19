package com.gemserk.games.superflyingthing.scripts;

import com.artemis.Entity;
import com.gemserk.commons.artemis.events.EventManager;
import com.gemserk.commons.artemis.scripts.ScriptJavaImpl;
import com.gemserk.commons.gdx.GlobalTime;
import com.gemserk.games.superflyingthing.components.ComponentWrapper;
import com.gemserk.games.superflyingthing.components.Components.TimerComponent;

public class TimerScript extends ScriptJavaImpl {

	private final EventManager eventManager;
	private final String eventId;

	public TimerScript(EventManager eventManager, String eventId) {
		this.eventManager = eventManager;
		this.eventId = eventId;
	}

	public void update(com.artemis.World world, Entity e) {
		TimerComponent timerComponent = ComponentWrapper.getTimerComponent(e);

		float currentTime = timerComponent.getCurrentTime();
		timerComponent.setCurrentTime(currentTime - GlobalTime.getDelta());

		if (timerComponent.isFinished()) {
			eventManager.registerEvent(eventId, e);
			e.delete();
		}
	};

}
