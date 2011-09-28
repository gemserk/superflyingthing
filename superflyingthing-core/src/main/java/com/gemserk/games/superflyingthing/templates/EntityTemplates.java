package com.gemserk.games.superflyingthing.templates;

import com.gemserk.commons.artemis.templates.EntityTemplate;
import com.gemserk.commons.reflection.Injector;
import com.gemserk.commons.reflection.Provider;
import com.gemserk.commons.reflection.ProviderImpl;

public class EntityTemplates {

	public EntityTemplates(Injector injector) {
		injector.add("entityTemplates", this);
		
		Provider templateProvider = new ProviderImpl(injector);
		
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
		this.secondCameraTemplate = templateProvider.get(SecondCameraTemplate.class);
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
	
}