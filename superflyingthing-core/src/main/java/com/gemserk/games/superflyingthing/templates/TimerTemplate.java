package com.gemserk.games.superflyingthing.templates;

import com.artemis.Entity;
import com.gemserk.commons.artemis.components.ScriptComponent;
import com.gemserk.commons.artemis.components.TimerComponent;
import com.gemserk.commons.artemis.events.EventManager;
import com.gemserk.commons.artemis.templates.EntityTemplateImpl;
import com.gemserk.games.superflyingthing.scripts.TimerScript;

public class TimerTemplate extends EntityTemplateImpl {
	
	EventManager eventManager;
	
	{
		parameters.put("time", new Float(0f));
	}

	@Override
	public void apply(Entity entity) {
		Float time = parameters.get("time");
		String eventId = parameters.get("eventId");

		entity.addComponent(new TimerComponent(time));
		entity.addComponent(new ScriptComponent(new TimerScript(eventManager, eventId)));
	}
	
}