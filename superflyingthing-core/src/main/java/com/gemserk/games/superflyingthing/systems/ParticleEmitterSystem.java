package com.gemserk.games.superflyingthing.systems;

import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.EntityProcessingSystem;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter.ScaledNumericValue;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.gemserk.commons.artemis.components.SpatialComponent;
import com.gemserk.commons.gdx.camera.Libgdx2dCamera;
import com.gemserk.commons.gdx.games.Spatial;
import com.gemserk.games.superflyingthing.Components.ParticleEmitterComponent;

public class ParticleEmitterSystem extends EntityProcessingSystem {

	ComponentMapper<ParticleEmitterComponent> particleEmitterComponentMapper;

	SpriteBatch spriteBatch;

	private final Libgdx2dCamera libgdx2dCamera;

	final Vector2 center = new Vector2(0.5f, 0.5f);

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
		float scale = particleEmitterComponent.getScale();

		Spatial spatial = spatialComponent.getSpatial();

		center.set(spatial.getPosition());

		scaleValue(emitter.getScale(), scale);
		scaleValue(emitter.getVelocity(), scale);
		scaleValue(emitter.getGravity(), scale);
		scaleValue(emitter.getWind(), scale);

		// camera.project(center);

		emitter.setPosition(center.x, center.y);
		emitter.draw(spriteBatch, deltaF);

		if (emitter.isComplete())
			world.deleteEntity(e);
	}

	private void scaleValue(ScaledNumericValue value, float s) {
		value.setHigh(value.getHighMin() * s, value.getHighMax() * s);
		value.setLow(value.getLowMin() * s, value.getLowMax() * s);
	}

	@Override
	protected void end() {
		super.end();
		spriteBatch.end();
	}

}