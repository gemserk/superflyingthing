package com.gemserk.games.superflyingthing.templates;

import com.artemis.Entity;
import com.gemserk.commons.artemis.components.ScriptComponent;
import com.gemserk.commons.artemis.events.EventManager;
import com.gemserk.commons.artemis.templates.EntityFactory;
import com.gemserk.commons.artemis.templates.EntityTemplateImpl;
import com.gemserk.games.superflyingthing.components.Components.GameData;
import com.gemserk.games.superflyingthing.components.Components.GameDataComponent;
import com.gemserk.games.superflyingthing.scripts.Scripts;

public class NormalModeGameLogicTemplate extends EntityTemplateImpl {

	EventManager eventManager;
	EntityFactory entityFactory;
	EntityTemplates entityTemplates;

	@Override
	public void apply(Entity entity) {
		GameData gameData = parameters.get("gameData");
		Boolean invulnerable = parameters.get("invulnerable", false);

		entity.addComponent(new GameDataComponent());
		entity.addComponent(new ScriptComponent(new Scripts.GameScript(eventManager, entityTemplates, entityFactory, gameData, invulnerable)));
	}

}