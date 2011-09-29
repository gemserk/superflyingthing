package com.gemserk.games.superflyingthing.templates;

import com.gemserk.commons.artemis.templates.EntityTemplate;
import com.gemserk.commons.reflection.Injector;
import com.gemserk.commons.reflection.Provider;
import com.gemserk.commons.reflection.ProviderImpl;

public class EntityTemplates {

	public EntityTemplates(Injector injector) {
		injector.configureField("entityTemplates", this);
		
		Provider templateProvider = new ProviderImpl(injector);
		
		this.cameraTemplate = templateProvider.getInstance(CameraTemplate.class);
		this.staticSpriteTemplate = templateProvider.getInstance(StaticSpriteTemplate.class);
		this.starTemplate = templateProvider.getInstance(StarTemplate.class);
		this.startPlanetTemplate = templateProvider.getInstance(StartPlanetTemplate.class);
		this.destinationPlanetTemplate = templateProvider.getInstance(DestinationPlanetTemplate.class);
		this.planetFillAnimationTemplate = templateProvider.getInstance(PlanetFillAnimationTemplate.class);
		this.portalTemplate = templateProvider.getInstance(PortalTemplate.class);
		this.replayShipTemplate = templateProvider.getInstance(ReplayShipTemplate.class);
		this.replayPlayerTemplate = templateProvider.getInstance(ReplayPlayerTemplate.class);
		this.laserBulletTemplate = templateProvider.getInstance(LaserBulletTemplate.class);
		this.laserGunTemplate = templateProvider.getInstance(LaserGunTemplate.class);
		this.attachedShipTemplate = templateProvider.getInstance(AttachedShipTemplate.class);
		this.shipTemplate = templateProvider.getInstance(ShipTemplate.class);
		this.particleEmitterTemplate = templateProvider.getInstance(ParticleEmitterTemplate.class);
		this.staticObstacleTemplate = templateProvider.getInstance(StaticObstacleTemplate.class);
		this.boxObstacleTemplate = templateProvider.getInstance(BoxObstacleTemplate.class);
		this.movingObstacleTemplate = templateProvider.getInstance(MovingObstacleTemplate.class);
		this.timerTemplate = templateProvider.getInstance(TimerTemplate.class);
		this.particleEmitterSpawnerTemplate = templateProvider.getInstance(ParticleEmitterSpawnerTemplate.class);
		this.secondCameraTemplate = templateProvider.getInstance(SecondCameraTemplate.class);
		this.replayRecorderTemplate = templateProvider.getInstance(ReplayRecorderTemplate.class);
		
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