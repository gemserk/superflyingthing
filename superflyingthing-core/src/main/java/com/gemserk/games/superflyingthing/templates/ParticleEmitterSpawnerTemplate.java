package com.gemserk.games.superflyingthing.templates;

import com.artemis.Entity;
import com.gemserk.commons.artemis.components.ScriptComponent;
import com.gemserk.commons.artemis.templates.EntityFactory;
import com.gemserk.commons.artemis.templates.EntityTemplateImpl;
import com.gemserk.games.superflyingthing.scripts.ParticleEmitterSpawnerScript;

public class ParticleEmitterSpawnerTemplate extends EntityTemplateImpl {
	
	EntityFactory entityFactory;
	EntityTemplates entityTemplates;
	
	@Override
	public void apply(Entity entity) {
		entity.addComponent(new ScriptComponent(new ParticleEmitterSpawnerScript(entityFactory, entityTemplates.particleEmitterTemplate)));
	}
	
}