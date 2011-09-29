package com.gemserk.games.superflyingthing.templates;

import com.gemserk.commons.artemis.templates.EntityTemplate;
import com.gemserk.commons.reflection.Injector;

public class EntityTemplates {

	public EntityTemplates(Injector injector) {
		injector.bind("entityTemplates", this);
		
		this.cameraTemplate = injector.getInstance(CameraTemplate.class);
		this.staticSpriteTemplate = injector.getInstance(StaticSpriteTemplate.class);
		this.starTemplate = injector.getInstance(StarTemplate.class);
		this.startPlanetTemplate = injector.getInstance(StartPlanetTemplate.class);
		this.destinationPlanetTemplate = injector.getInstance(DestinationPlanetTemplate.class);
		this.planetFillAnimationTemplate = injector.getInstance(PlanetFillAnimationTemplate.class);
		this.portalTemplate = injector.getInstance(PortalTemplate.class);
		this.replayShipTemplate = injector.getInstance(ReplayShipTemplate.class);
		this.replayPlayerTemplate = injector.getInstance(ReplayPlayerTemplate.class);
		this.laserBulletTemplate = injector.getInstance(LaserBulletTemplate.class);
		this.laserGunTemplate = injector.getInstance(LaserGunTemplate.class);
		this.attachedShipTemplate = injector.getInstance(AttachedShipTemplate.class);
		this.shipTemplate = injector.getInstance(ShipTemplate.class);
		this.particleEmitterTemplate = injector.getInstance(ParticleEmitterTemplate.class);
		this.staticObstacleTemplate = injector.getInstance(StaticObstacleTemplate.class);
		this.boxObstacleTemplate = injector.getInstance(BoxObstacleTemplate.class);
		this.movingObstacleTemplate = injector.getInstance(MovingObstacleTemplate.class);
		this.timerTemplate = injector.getInstance(TimerTemplate.class);
		this.particleEmitterSpawnerTemplate = injector.getInstance(ParticleEmitterSpawnerTemplate.class);
		this.secondCameraTemplate = injector.getInstance(SecondCameraTemplate.class);
		this.replayRecorderTemplate = injector.getInstance(ReplayRecorderTemplate.class);
		
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
	public EntityTemplate secondCameraTemplate;
	
	public EntityTemplate replayRecorderTemplate;
	
}