package com.gemserk.games.superflyingthing.systems;

import com.artemis.Entity;
import com.artemis.EntityProcessingSystem;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
import com.gemserk.commons.artemis.components.SpatialComponent;
import com.gemserk.commons.gdx.GlobalTime;
import com.gemserk.commons.gdx.games.Spatial;
import com.gemserk.games.superflyingthing.components.Components.ParticleEmitterComponent;
import com.gemserk.games.superflyingthing.components.GameComponents;

public class ParticleEmitterSystem extends EntityProcessingSystem {

	private static final Class<ParticleEmitterComponent> particleEmitterComponentClass = ParticleEmitterComponent.class;

	public ParticleEmitterSystem() {
		super(ParticleEmitterComponent.class);
	}

	@Override
	protected void initialize() {
		super.initialize();
	}

	@Override
	protected void process(Entity e) {
		ParticleEmitterComponent particleEmitterComponent = e.getComponent(particleEmitterComponentClass);

		SpatialComponent spatialComponent = GameComponents.getSpatialComponent(e);

		ParticleEmitter emitter = particleEmitterComponent.getParticleEmitter();

		Spatial spatial = spatialComponent.getSpatial();

		float hmin = emitter.getAngle().getHighMin();
		float hmax = emitter.getAngle().getHighMax();

		float lmin = emitter.getAngle().getLowMin();
		float lmax = emitter.getAngle().getLowMax();

		emitter.getAngle().setHigh(hmin + spatial.getAngle(), hmax + spatial.getAngle());
		emitter.getAngle().setLow(lmin + spatial.getAngle(), lmax + spatial.getAngle());

		emitter.setPosition(spatial.getX(), spatial.getY());

		emitter.update(GlobalTime.getDelta());
		// emitter.draw(spriteBatch);

		emitter.getAngle().setHigh(hmin, hmax);
		emitter.getAngle().setLow(lmin, lmax);
	}

}