package com.gemserk.games.superflyingthing.components;

import com.artemis.Entity;
import com.gemserk.commons.artemis.components.AnimationComponent;
import com.gemserk.commons.artemis.components.PhysicsComponent;
import com.gemserk.commons.artemis.components.SpatialComponent;
import com.gemserk.commons.artemis.components.SpriteComponent;
import com.gemserk.commons.artemis.components.TimerComponent;
import com.gemserk.commons.gdx.games.Physics;
import com.gemserk.commons.gdx.games.Spatial;
import com.gemserk.games.superflyingthing.components.Components.AttachableComponent;
import com.gemserk.games.superflyingthing.components.Components.AttachmentComponent;
import com.gemserk.games.superflyingthing.components.Components.ControllerComponent;
import com.gemserk.games.superflyingthing.components.Components.DamageComponent;
import com.gemserk.games.superflyingthing.components.Components.GameDataComponent;
import com.gemserk.games.superflyingthing.components.Components.GrabbableComponent;
import com.gemserk.games.superflyingthing.components.Components.HealthComponent;
import com.gemserk.games.superflyingthing.components.Components.MovementComponent;
import com.gemserk.games.superflyingthing.components.Components.ParticleEmitterComponent;
import com.gemserk.games.superflyingthing.components.Components.ReplayListComponent;
import com.gemserk.games.superflyingthing.components.Components.TargetComponent;
import com.gemserk.games.superflyingthing.components.Components.WeaponComponent;

public class GameComponents {
	
	public static final Class<PhysicsComponent> physicsComponentClass = PhysicsComponent.class;
	public static final Class<SpatialComponent> spatialComponentClass = SpatialComponent.class;
	public static final Class<SpriteComponent> spriteComponentClass = SpriteComponent.class;
	public static final Class<AnimationComponent> animationComponentClass = AnimationComponent.class;
	public static final Class<AttachmentComponent> attachmentComponentClass = AttachmentComponent.class;
	public static final Class<MovementComponent> movementComponentClass = MovementComponent.class;
	public static final Class<ParticleEmitterComponent> particleEmitterComponentClass = ParticleEmitterComponent.class;
	public static final Class<ControllerComponent> controllerComponentClass = ControllerComponent.class;
	public static final Class<GameDataComponent> gameDataComponentClass = GameDataComponent.class;
	public static final Class<TargetComponent> targetComponentClass = TargetComponent.class;
	public static final Class<HealthComponent> healthComponentClass = HealthComponent.class;
	public static final Class<DamageComponent> damageComponentClass = DamageComponent.class;
	public static final Class<WeaponComponent> weaponComponentClass = WeaponComponent.class;
	public static final Class<GrabbableComponent> grabbableComponentClass = GrabbableComponent.class;
	public static final Class<AttachableComponent> attachableComponentClass = AttachableComponent.class;
	public static final Class<ReplayListComponent> replayComponentClass = ReplayListComponent.class;
	public static final Class<TimerComponent> timerComponentClass = TimerComponent.class;
	
	public static final Class<PropertiesComponent> propertiesComponentClass = PropertiesComponent.class;
	
	public static Physics getPhysics(Entity e) {
		return getPhysicsComponent(e).getPhysics();
	}

	public static Spatial getSpatial(Entity e) {
		return getSpatialComponent(e).getSpatial();
	}
	
	public static PhysicsComponent getPhysicsComponent(Entity e) {
		return e.getComponent(physicsComponentClass);
	}
	
	public static SpatialComponent getSpatialComponent(Entity e) {
		return e.getComponent(spatialComponentClass);
	}

	public static SpriteComponent getSpriteComponent(Entity e) {
		return e.getComponent(spriteComponentClass);
	}
	
	public static AnimationComponent getAnimationComponent(Entity e) {
		return e.getComponent(animationComponentClass);
	}
	
	public static AttachmentComponent getAttachmentComponent(Entity e) {
		return e.getComponent(attachmentComponentClass);
	}
	
	public static AttachableComponent getAttachableComponent(Entity e) {
		return e.getComponent(attachableComponentClass);
	}
	
	public static MovementComponent getMovementComponent(Entity e) {
		return e.getComponent(movementComponentClass);
	}
	
	public static ParticleEmitterComponent getParticleEmitter(Entity e) {
		return e.getComponent(particleEmitterComponentClass);
	}
	
	public static ControllerComponent getControllerComponent(Entity e) {
		return e.getComponent(controllerComponentClass);
	}
	
	public static GameDataComponent getGameDataComponent(Entity e) {
		return e.getComponent(gameDataComponentClass);
	}

	public static TargetComponent getTargetComponent(Entity e) {
		return e.getComponent(targetComponentClass);
	}
	
	public static HealthComponent getHealthComponent(Entity e) {
		return e.getComponent(healthComponentClass);
	}
	
	public static DamageComponent getDamageComponent(Entity e) {
		return e.getComponent(damageComponentClass);
	}
	
	public static WeaponComponent getWeaponComponent(Entity e) {
		return e.getComponent(weaponComponentClass);
	}
	
	public static GrabbableComponent getGrabbableComponent(Entity e) {
		return e.getComponent(grabbableComponentClass);
	}
	
	public static ReplayListComponent getReplayComponent(Entity e) {
		return e.getComponent(replayComponentClass);
	}

	public static TimerComponent getTimerComponent(Entity e) {
		return e.getComponent(timerComponentClass);
	}
	
	public static PropertiesComponent getPropertiesComponent(Entity e) {
		return e.getComponent(propertiesComponentClass);
	}
	
}