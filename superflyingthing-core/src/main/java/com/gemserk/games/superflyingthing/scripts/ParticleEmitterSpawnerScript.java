package com.gemserk.games.superflyingthing.scripts;

import com.gemserk.commons.artemis.events.Event;
import com.gemserk.commons.artemis.events.reflection.Handles;
import com.gemserk.commons.artemis.scripts.ScriptJavaImpl;
import com.gemserk.commons.artemis.templates.EntityFactory;
import com.gemserk.commons.artemis.templates.EntityTemplate;
import com.gemserk.commons.gdx.games.Spatial;
import com.gemserk.componentsengine.utils.Parameters;
import com.gemserk.componentsengine.utils.ParametersWrapper;

public class ParticleEmitterSpawnerScript extends ScriptJavaImpl {

	private final EntityFactory entityFactory;
	private final EntityTemplate emitterTemplate;

	private Parameters parameters = new ParametersWrapper();

	public ParticleEmitterSpawnerScript(EntityFactory entityFactory, EntityTemplate emitterTemplate) {
		this.entityFactory = entityFactory;
		this.emitterTemplate = emitterTemplate;
	}

	@Handles
	public void explosion(Event e) {
		Spatial spatial = (Spatial) e.getSource();

		parameters.clear();

		entityFactory.instantiate(emitterTemplate, parameters //
				.put("position", spatial.getPosition()) //
				.put("emitter", "ExplosionEmitter"));
	}

}