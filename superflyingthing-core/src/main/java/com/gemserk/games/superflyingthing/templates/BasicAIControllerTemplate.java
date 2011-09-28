package com.gemserk.games.superflyingthing.templates;

import com.artemis.Entity;
import com.gemserk.commons.artemis.components.ScriptComponent;
import com.gemserk.commons.artemis.templates.EntityTemplateImpl;
import com.gemserk.games.superflyingthing.scripts.controllers.BasicAIShipControllerScript;

public class BasicAIControllerTemplate extends EntityTemplateImpl {

	com.badlogic.gdx.physics.box2d.World physicsWorld;

	@Override
	public void apply(Entity entity) {
		entity.addComponent(new ScriptComponent(new BasicAIShipControllerScript(physicsWorld)));
	}

}