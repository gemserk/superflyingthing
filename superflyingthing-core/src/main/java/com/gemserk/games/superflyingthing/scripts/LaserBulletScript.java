package com.gemserk.games.superflyingthing.scripts;

import com.artemis.Entity;
import com.artemis.World;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.gemserk.animation4j.timeline.Builders;
import com.gemserk.animation4j.timeline.TimelineAnimation;
import com.gemserk.commons.artemis.components.SpriteComponent;
import com.gemserk.commons.artemis.components.TimerComponent;
import com.gemserk.commons.artemis.scripts.ScriptJavaImpl;
import com.gemserk.commons.artemis.templates.EntityFactory;
import com.gemserk.commons.artemis.templates.EntityTemplate;
import com.gemserk.commons.gdx.GlobalTime;
import com.gemserk.commons.gdx.games.Spatial;
import com.gemserk.componentsengine.utils.Container;
import com.gemserk.componentsengine.utils.Parameters;
import com.gemserk.componentsengine.utils.ParametersWrapper;
import com.gemserk.games.superflyingthing.components.ComponentWrapper;
import com.gemserk.games.superflyingthing.components.Components.DamageComponent;
import com.gemserk.games.superflyingthing.components.Components.HealthComponent;

public class LaserBulletScript extends ScriptJavaImpl implements RayCastCallback {

	// used for internal calculations
	private static final Vector2 target = new Vector2();
	private static final Parameters parameters = new ParametersWrapper();

	private final com.badlogic.gdx.physics.box2d.World physicsWorld;
	private final EntityFactory entityFactory;
	private final EntityTemplate particleEmitterTemplate;

	private TimelineAnimation laserTimelineAnimation;

	private Entity laserHitParticleEmitter;
	private Fixture lastCollisionFixture;

	public LaserBulletScript(com.badlogic.gdx.physics.box2d.World physicsWorld, EntityFactory entityFactory, EntityTemplate particleEmitterTemplate) {
		this.physicsWorld = physicsWorld;
		this.entityFactory = entityFactory;
		this.particleEmitterTemplate = particleEmitterTemplate;
	}

	@Override
	public void init(World world, Entity e) {
		TimerComponent timerComponent = e.getComponent(TimerComponent.class);
		float totalTime = timerComponent.getTotalTime();

		Spatial spatial = ComponentWrapper.getSpatial(e);

		laserTimelineAnimation = Builders.animation(Builders.timeline() //
				.value(Builders.timelineValue("alpha") //
						.keyFrame(0f, 0f) //
						.keyFrame(0.2f, 1f) //
						.keyFrame(0.8f, 1f) //
						.keyFrame(1f, 0f)) //
				.value(Builders.timelineValue("width") //
						.keyFrame(0f, 0f) //
						.keyFrame(0.2f, spatial.getHeight()) //
						.keyFrame(0.8f, spatial.getHeight()) //
						.keyFrame(1f, 0f)) //
				) //
				.speed(1f / totalTime) //
				.build();
		laserTimelineAnimation.start(1);
	}

	@Override
	public void dispose(World world, Entity e) {
		world.deleteEntity(laserHitParticleEmitter);
	}

	@Override
	public void update(World world, Entity e) {

		laserTimelineAnimation.update(GlobalTime.getDelta());

		if (laserTimelineAnimation.isFinished()) {
			world.deleteEntity(e);
			return;
		}

		SpriteComponent spriteComponent = ComponentWrapper.getSpriteComponent(e);
		spriteComponent.getColor().a = (Float) laserTimelineAnimation.getValue("alpha");

		Spatial spatial = ComponentWrapper.getSpatial(e);
		// float width = spatial.getWidth();

		target.set(1f, 0f).mul(20f).rotate(spatial.getAngle()).add(spatial.getPosition());

		physicsWorld.rayCast(this, spatial.getPosition(), target);

		// only if it is the last one...
		if (lastCollisionFixture != null) {
			Body body = lastCollisionFixture.getBody();
			Entity entity = (Entity) body.getUserData();
			if (entity != null) {
				HealthComponent healthComponent = ComponentWrapper.getHealthComponent(entity);
				if (healthComponent != null) {
					DamageComponent damageComponent = ComponentWrapper.getDamageComponent(e);
					Container health = healthComponent.getHealth();
					health.remove(damageComponent.getDamage() * GlobalTime.getDelta());
				}
			}
		}

		float width = spatial.getPosition().dst(target);
		float height = (Float) laserTimelineAnimation.getValue("width");

		spatial.setSize(width, height);

		// parameters.put("position", target);
		parameters.put("emitter", "LaserHitEmitter");
		parameters.put("script", new ScriptJavaImpl());

		if (laserHitParticleEmitter == null)
			laserHitParticleEmitter = entityFactory.instantiate(particleEmitterTemplate, parameters);

		Spatial laserHitEmitterSpatial = ComponentWrapper.getSpatial(laserHitParticleEmitter);
		laserHitEmitterSpatial.setPosition(target.x, target.y);
	}

	@Override
	public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
		target.set(point);
		lastCollisionFixture = fixture;
		return fraction;
	}

}