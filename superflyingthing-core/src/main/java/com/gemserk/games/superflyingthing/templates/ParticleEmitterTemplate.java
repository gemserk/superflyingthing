package com.gemserk.games.superflyingthing.templates;

import com.artemis.Entity;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
import com.badlogic.gdx.math.Vector2;
import com.gemserk.commons.artemis.components.RenderableComponent;
import com.gemserk.commons.artemis.components.ScriptComponent;
import com.gemserk.commons.artemis.components.SpatialComponent;
import com.gemserk.commons.artemis.scripts.Script;
import com.gemserk.commons.artemis.templates.EntityTemplateImpl;
import com.gemserk.commons.gdx.games.SpatialImpl;
import com.gemserk.commons.gdx.graphics.ParticleEmitterUtils;
import com.gemserk.games.superflyingthing.components.Components.ParticleEmitterComponent;
import com.gemserk.games.superflyingthing.scripts.Scripts;
import com.gemserk.resources.ResourceManager;

public class ParticleEmitterTemplate extends EntityTemplateImpl {
	
	ResourceManager<String> resourceManager;
	
	{
		// used to transform the emitter and particles to the world coordinates space
		parameters.put("scale", new Float(0.02f));
		parameters.put("position", new Vector2(0f, 0f));
	}

	@Override
	public void apply(Entity entity) {
		Vector2 position = parameters.get("position");
		Float scale = parameters.get("scale");
		String emitter = parameters.get("emitter");
		Script script = parameters.get("script", new Scripts.ParticleEmitterScript());

		ParticleEmitter particleEmitter = resourceManager.getResourceValue(emitter);
		particleEmitter.start();
		ParticleEmitterUtils.scaleEmitter(particleEmitter, scale);

		entity.addComponent(new SpatialComponent(new SpatialImpl(position.x, position.y, 1f, 1f, 0f)));
		entity.addComponent(new ParticleEmitterComponent(particleEmitter));
		entity.addComponent(new ScriptComponent(script));
		entity.addComponent(new RenderableComponent(150));
	}

}
