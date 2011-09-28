package com.gemserk.games.superflyingthing.templates;

import com.artemis.Entity;
import com.gemserk.commons.artemis.components.ScriptComponent;
import com.gemserk.commons.artemis.components.TagComponent;
import com.gemserk.commons.artemis.events.EventManager;
import com.gemserk.commons.artemis.scripts.EventSystemScript;
import com.gemserk.commons.artemis.templates.EntityTemplateImpl;

public class EventManagerTemplate extends EntityTemplateImpl {

	EventManager eventManager;

	@Override
	public void apply(Entity entity) {
		entity.addComponent(new TagComponent("EventManager"));
		entity.addComponent(new ScriptComponent(new EventSystemScript(eventManager)));
	}

}