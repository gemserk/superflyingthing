package com.gemserk.games.superflyingthing.systems;

import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.EntityProcessingSystem;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.gemserk.commons.artemis.components.SpatialComponent;
import com.gemserk.commons.gdx.camera.Libgdx2dCamera;
import com.gemserk.commons.gdx.games.Spatial;
import com.gemserk.games.superflyingthing.Components.ParticleEmitterComponent;

public class ParticleEmitterSystem extends EntityProcessingSystem {
	
	ComponentMapper<ParticleEmitterComponent> particleEmitterComponentMapper;
	
	SpriteBatch spriteBatch;

	private final Libgdx2dCamera camera;
	
	final Vector2 center = new Vector2(0.5f, 0.5f);

	public ParticleEmitterSystem(Libgdx2dCamera camera) {
		super(ParticleEmitterComponent.class);
		this.camera = camera;
	}
	
	@Override
	protected void initialize() {
		super.initialize();
		particleEmitterComponentMapper  = new ComponentMapper<ParticleEmitterComponent>(ParticleEmitterComponent.class, world.getEntityManager());
		spriteBatch = new SpriteBatch();
	}
	
	// dispose() { ... }
	
	@Override
	protected void begin() {
		super.begin();
		spriteBatch.begin();
	}
	
	@Override
	protected void process(Entity e) {
		float deltaF = 0.001f * (float) world.getDelta();
		ParticleEmitterComponent particleEmitterComponent = particleEmitterComponentMapper.get(e);
		
		SpatialComponent spatialComponent = e.getComponent(SpatialComponent.class);

		ParticleEmitter particleEmitter = particleEmitterComponent.getParticleEmitter();
		
		Spatial spatial = spatialComponent.getSpatial();
		
		center.set(spatial.getPosition());
		camera.project(center);
		
		particleEmitter.setPosition(center.x, center.y);
		particleEmitter.draw(spriteBatch, deltaF);
		
		if (particleEmitter.isComplete())
			world.deleteEntity(e);
	}
	
	@Override
	protected void end() {
		super.end();
		spriteBatch.end();
	}
	
}