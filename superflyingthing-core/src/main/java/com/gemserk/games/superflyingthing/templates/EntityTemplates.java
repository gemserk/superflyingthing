package com.gemserk.games.superflyingthing.templates;

import com.badlogic.gdx.physics.box2d.World;
import com.gemserk.commons.artemis.EntityBuilder;
import com.gemserk.commons.artemis.events.EventManager;
import com.gemserk.commons.artemis.templates.EntityFactory;
import com.gemserk.commons.artemis.templates.EntityTemplate;
import com.gemserk.commons.gdx.box2d.BodyBuilder;
import com.gemserk.commons.gdx.box2d.JointBuilder;
import com.gemserk.commons.gdx.graphics.Mesh2dBuilder;
import com.gemserk.commons.reflection.ObjectConfigurator;
import com.gemserk.commons.reflection.ProviderImpl;
import com.gemserk.resources.ResourceManager;

public class EntityTemplates {

	public static class CategoryBits {

		public static short AllCategoryBits = 0xFF;
		public static short ShipCategoryBits = 1;
		public static short MiniPlanetCategoryBits = 2;
		public static short MovingObstacleCategoryBits = 4;
		public static short ObstacleCategoryBits = 8;

	}

	private final EntityFactory entityFactory;

	private final EventManager eventManager;

	public EntityTemplate userMessageTemplate;

	public EntityTemplate getAttachedShipTemplate() {
		return attachedShipTemplate;
	}

	public EntityTemplate getShipTemplate() {
		return shipTemplate;
	}

	public EntityTemplate getParticleEmitterTemplate() {
		return particleEmitterTemplate;
	}

	public EntityTemplate getLaserGunTemplate() {
		return laserGunTemplate;
	}

	public EntityTemplate getPortalTemplate() {
		return portalTemplate;
	}

	public EntityTemplate getPlanetFillAnimationTemplate() {
		return planetFillAnimationTemplate;
	}

	public EntityTemplate getCameraTemplate() {
		return cameraTemplate;
	}

	public EntityTemplate getReplayShipTemplate() {
		return replayShipTemplate;
	}

	public EntityTemplate getParticleEmitterSpawnerTemplate() {
		return particleEmitterSpawnerTemplate;
	}

	public EntityTemplate getTimerTemplate() {
		return timerTemplate;
	}

	public EntityTemplate getReplayPlayerTemplate() {
		return replayPlayerTemplate;
	}

	public EntityTemplate getStaticSpriteTemplate() {
		return staticSpriteTemplate;
	}

	public EntityTemplates(final World physicsWorld, com.artemis.World world, final ResourceManager<String> resourceManager, final EntityBuilder entityBuilder, final EntityFactory entityFactory, final EventManager eventManager) {
		this.entityFactory = entityFactory;
		this.eventManager = eventManager;

		ProviderImpl templateProvider = new ProviderImpl(new ObjectConfigurator() {
			{
				add("physicsWorld", physicsWorld);
				add("resourceManager", resourceManager);
				add("entityBuilder", entityBuilder);
				add("entityFactory", entityFactory);
				add("eventManager", eventManager);
				add("bodyBuilder", new BodyBuilder(physicsWorld));
				add("mesh2dBuilder", new Mesh2dBuilder());
				add("jointBuilder", new JointBuilder(physicsWorld));
				add("entityTemplates", EntityTemplates.this);
			}
		});

		this.cameraTemplate = templateProvider.get(CameraTemplate.class);
		this.staticSpriteTemplate = templateProvider.get(StaticSpriteTemplate.class);
		this.starTemplate = templateProvider.get(StarTemplate.class);
		this.startPlanetTemplate = templateProvider.get(StartPlanetTemplate.class);
		this.destinationPlanetTemplate = templateProvider.get(DestinationPlanetTemplate.class);
		this.planetFillAnimationTemplate = templateProvider.get(PlanetFillAnimationTemplate.class);
		this.portalTemplate = templateProvider.get(PortalTemplate.class);
		this.replayShipTemplate = templateProvider.get(ReplayShipTemplate.class);
		this.replayPlayerTemplate = templateProvider.get(ReplayPlayerTemplate.class);
		this.laserBulletTemplate = templateProvider.get(LaserBulletTemplate.class);
		this.laserGunTemplate = templateProvider.get(LaserGunTemplate.class);
		this.attachedShipTemplate = templateProvider.get(AttachedShipTemplate.class);
		this.shipTemplate = templateProvider.get(ShipTemplate.class);
		this.particleEmitterTemplate = templateProvider.get(ParticleEmitterTemplate.class);
		this.staticObstacleTemplate = templateProvider.get(StaticObstacleTemplate.class);
		this.boxObstacleTemplate = templateProvider.get(BoxObstacleTemplate.class);
		this.movingObstacleTemplate = templateProvider.get(MovingObstacleTemplate.class);
		this.timerTemplate = templateProvider.get(TimerTemplate.class);
		this.particleEmitterSpawnerTemplate = templateProvider.get(ParticleEmitterSpawnerTemplate.class);

	}

	public EntityTemplate cameraTemplate;
	public EntityTemplate staticSpriteTemplate;
	public EntityTemplate starTemplate;
	public EntityTemplate startPlanetTemplate;
	public EntityTemplate destinationPlanetTemplate;
	public EntityTemplate planetFillAnimationTemplate;
	public EntityTemplate portalTemplate;

	public EntityTemplate replayShipTemplate;
	public EntityTemplate replayPlayerTemplate;

	public EntityTemplate laserBulletTemplate;
	public EntityTemplate laserGunTemplate;

	public EntityTemplate attachedShipTemplate;
	public EntityTemplate shipTemplate;

	public EntityTemplate particleEmitterTemplate;
	public EntityTemplate staticObstacleTemplate;
	public EntityTemplate boxObstacleTemplate;
	public EntityTemplate movingObstacleTemplate;
	public EntityTemplate timerTemplate;
	
	public EntityTemplate particleEmitterSpawnerTemplate;

}