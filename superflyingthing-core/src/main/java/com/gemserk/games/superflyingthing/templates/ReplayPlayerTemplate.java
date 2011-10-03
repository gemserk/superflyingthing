package com.gemserk.games.superflyingthing.templates;

import com.artemis.Entity;
import com.gemserk.commons.artemis.components.ScriptComponent;
import com.gemserk.commons.artemis.events.EventManager;
import com.gemserk.commons.artemis.templates.EntityTemplateImpl;
import com.gemserk.games.superflyingthing.components.Replay;
import com.gemserk.games.superflyingthing.scripts.ReplayPlayerScript;

public class ReplayPlayerTemplate extends EntityTemplateImpl {

	EventManager eventManager;

	@Override
	public void apply(Entity e) {
		Replay replay = parameters.get("replay");
		Entity target = parameters.get("target");
		e.addComponent(new ScriptComponent(new ReplayPlayerScript(replay, eventManager, target)));
	}
}