package com.gemserk.games.superflyingthing.systems;

import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.EntityProcessingSystem;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.gemserk.commons.artemis.components.SpatialComponent;
import com.gemserk.commons.gdx.camera.Libgdx2dCamera;
import com.gemserk.commons.gdx.games.Spatial;
import com.gemserk.games.superflyingthing.Components.ParticleEmitterComponent;

public class ParticleEmitterSystem extends EntityProcessingSystem {

	ComponentMapper<ParticleEmitterComponent> particleEmitterComponentMapper;

	SpriteBatch spriteBatch;

	private final Libgdx2dCamera libgdx2dCamera;

	public ParticleEmitterSystem(Libgdx2dCamera libgdx2dCamera) {
		super(ParticleEmitterComponent.class);
		this.libgdx2dCamera = libgdx2dCamera;
	}

	@Override
	protected void initialize() {
		super.initialize();
		particleEmitterComponentMapper = new ComponentMapper<ParticleEmitterComponent>(ParticleEmitterComponent.class, world.getEntityManager());
		spriteBatch = new SpriteBatch();
	}

	// dispose() { ... }

	@Override
	protected void begin() {
		super.begin();
		libgdx2dCamera.apply(spriteBatch);
		spriteBatch.begin();
	}

	@Override
	protected void process(Entity e) {
		float deltaF = 0.001f * (float) world.getDelta();
		ParticleEmitterComponent particleEmitterComponent = particleEmitterComponentMapper.get(e);

		SpatialComponent spatialComponent = e.getComponent(SpatialComponent.class);

		ParticleEmitter emitter = particleEmitterComponent.getParticleEmitter();

		Spatial spatial = spatialComponent.getSpatial();

		float hmin = emitter.getAngle().getHighMin();
		float hmax = emitter.getAngle().getHighMax();

		float lmin = emitter.getAngle().getLowMin();
		float lmax = emitter.getAngle().getLowMax();

		emitter.getAngle().setHigh(hmin + spatial.getAngle(), hmax + spatial.getAngle());
		emitter.getAngle().setLow(lmin + spatial.getAngle(), lmax + spatial.getAngle());

		emitter.setPosition(spatial.getX(), spatial.getY());

		emitter.draw(spriteBatch, deltaF);
		
		emitter.getAngle().setHigh(hmin, hmax);
		emitter.getAngle().setLow(lmin, lmax);
	}

	@Override
	protected void end() {
		super.end();
		spriteBatch.end();
	}

}