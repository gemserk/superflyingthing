package com.gemserk.games.superflyingthing.systems;

import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.EntityProcessingSystem;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
import com.gemserk.commons.artemis.components.SpatialComponent;
import com.gemserk.commons.gdx.GlobalTime;
import com.gemserk.commons.gdx.games.Spatial;
import com.gemserk.games.superflyingthing.components.ComponentWrapper;
import com.gemserk.games.superflyingthing.components.Components.ParticleEmitterComponent;

public class ParticleEmitterSystem extends EntityProcessingSystem {

	// protected static final Logger logger = LoggerFactory.getLogger(ParticleEmitterSystem.class);

	ComponentMapper<ParticleEmitterComponent> particleEmitterComponentMapper;

	public ParticleEmitterSystem() {
		super(ParticleEmitterComponent.class);
	}

	@Override
	protected void initialize() {
		super.initialize();
		particleEmitterComponentMapper = new ComponentMapper<ParticleEmitterComponent>(ParticleEmitterComponent.class, world.getEntityManager());
	}

	@Override
	protected void added(Entity e) {
		// if (logger.isDebugEnabled())
		// logger.debug("Entity with particle emitter added");
	}

	@Override
	protected void removed(Entity e) {
		// if (logger.isDebugEnabled())
		// logger.debug("Entity with particle emitter removed");
	}

	@Override
	protected void process(Entity e) {
		float deltaF = GlobalTime.getDelta();
		ParticleEmitterComponent particleEmitterComponent = particleEmitterComponentMapper.get(e);

		SpatialComponent spatialComponent = ComponentWrapper.getSpatialComponent(e);

		ParticleEmitter emitter = particleEmitterComponent.getParticleEmitter();

		Spatial spatial = spatialComponent.getSpatial();

		float hmin = emitter.getAngle().getHighMin();
		float hmax = emitter.getAngle().getHighMax();

		float lmin = emitter.getAngle().getLowMin();
		float lmax = emitter.getAngle().getLowMax();

		emitter.getAngle().setHigh(hmin + spatial.getAngle(), hmax + spatial.getAngle());
		emitter.getAngle().setLow(lmin + spatial.getAngle(), lmax + spatial.getAngle());

		emitter.setPosition(spatial.getX(), spatial.getY());

		emitter.update(deltaF);
		// emitter.draw(spriteBatch);

		emitter.getAngle().setHigh(hmin, hmax);
		emitter.getAngle().setLow(lmin, lmax);
	}

}