package com.gemserk.games.superflyingthing.scripts;

import com.artemis.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.gemserk.animation4j.gdx.Animation;
import com.gemserk.commons.artemis.components.AnimationComponent;
import com.gemserk.commons.artemis.components.PhysicsComponent;
import com.gemserk.commons.artemis.components.SpriteComponent;
import com.gemserk.commons.artemis.events.Event;
import com.gemserk.commons.artemis.events.EventListener;
import com.gemserk.commons.artemis.events.EventManager;
import com.gemserk.commons.artemis.scripts.Script;
import com.gemserk.commons.artemis.scripts.ScriptJavaImpl;
import com.gemserk.commons.artemis.templates.EntityFactory;
import com.gemserk.commons.artemis.templates.EntityTemplate;
import com.gemserk.commons.gdx.GlobalTime;
import com.gemserk.commons.gdx.box2d.JointBuilder;
import com.gemserk.commons.gdx.games.Spatial;
import com.gemserk.componentsengine.utils.Parameters;
import com.gemserk.componentsengine.utils.ParametersWrapper;
import com.gemserk.games.superflyingthing.Events;
import com.gemserk.games.superflyingthing.ShipController;
import com.gemserk.games.superflyingthing.components.GameComponents;
import com.gemserk.games.superflyingthing.components.Components.AttachableComponent;
import com.gemserk.games.superflyingthing.components.Components.AttachmentComponent;
import com.gemserk.games.superflyingthing.components.Components.ControllerComponent;
import com.gemserk.games.superflyingthing.components.Components.GameDataComponent;
import com.gemserk.games.superflyingthing.components.Components.GrabbableComponent;
import com.gemserk.games.superflyingthing.components.Components.HealthComponent;
import com.gemserk.games.superflyingthing.components.Components.MovementComponent;
import com.gemserk.games.superflyingthing.components.Components.ParticleEmitterComponent;
import com.gemserk.games.superflyingthing.scripts.Behaviors.FixCameraTargetScript;
import com.gemserk.games.superflyingthing.templates.EntityTemplates;
import com.gemserk.games.superflyingthing.templates.Groups;

public class Scripts {

	public static class ShipScript extends ScriptJavaImpl {

		Script fixMovementBehavior = new Behaviors.FixMovementScript();
		Script fixDirectionFromControllerBehavior = new Behaviors.FixDirectionFromControllerScript();
		Script collisionHandlerBehavior = new Behaviors.AttachToAttachableScript();
		Script updateSpriteFromAnimation = new Behaviors.ShipAnimationScript();

		Script performDamageFromCollidingEntityScript = new Behaviors.PerformDamageFromCollidingEntityScript();

		@Override
		public void update(com.artemis.World world, Entity e) {
			fixMovementBehavior.update(world, e);
			fixDirectionFromControllerBehavior.update(world, e);
			collisionHandlerBehavior.update(world, e);
			updateSpriteFromAnimation.update(world, e);
			performDamageFromCollidingEntityScript.update(world, e);
		}

	}

	public static class AttachedShipScript extends ScriptJavaImpl {

		Script fixMovementBehavior = new Behaviors.FixMovementScript();
		Script updateSpriteFromAnimation = new Behaviors.ShipAnimationScript();

		@Override
		public void update(com.artemis.World world, Entity e) {
			fixMovementBehavior.update(world, e);
			updateSpriteFromAnimation.update(world, e);
		}

	}

	public static class StarScript extends ScriptJavaImpl {

		private final EventManager eventManager;

		public StarScript(EventManager eventManager) {
			this.eventManager = eventManager;
		}

		@Override
		public void update(com.artemis.World world, Entity e) {
			updateGrabbable(e);
		}

		private void updateGrabbable(Entity e) {
			GrabbableComponent grabbableComponent = GameComponents.getGrabbableComponent(e);
			if (!grabbableComponent.grabbed)
				return;
			Gdx.app.log("SuperFlyingThing", "Registering event for item taken");
			eventManager.registerEvent(Events.itemTaken, e);
		}
	}

	public static class StarAnimationScript extends ScriptJavaImpl {

		float rotationSpeed = 150f;
		float angle = 0f;

		@Override
		public void update(com.artemis.World world, Entity e) {
			AnimationComponent animationComponent = GameComponents.getAnimationComponent(e);
			SpriteComponent spriteComponent = GameComponents.getSpriteComponent(e);

			angle += rotationSpeed * GlobalTime.getDelta();

			Animation animation = animationComponent.getCurrentAnimation();
			int frameIndex = getAnimationForAngle(angle - 5f);

			Sprite frame = animation.getFrame(frameIndex);
			spriteComponent.setSprite(frame);
		}

		private int getAnimationForAngle(float angle) {
			if (angle < 360f)
				angle += 360f;
			angle %= 360f;
			double floor = Math.floor(angle * 0.1f);
			return (int) (floor);
		}

	}

	public static class StartPlanetScript extends ScriptJavaImpl {

		private final EventManager eventManager;
		private final World physicsWorld;

		Script attachEntityBehavior;
		Script calculateInputDirectionBehavior;

		boolean enabled = true;

		public StartPlanetScript(World physicsWorld, JointBuilder jointBuilder, EventManager eventManager) {
			this.physicsWorld = physicsWorld;
			this.eventManager = eventManager;
			attachEntityBehavior = new Behaviors.AttachEntityScript(jointBuilder);
			calculateInputDirectionBehavior = new Behaviors.AttachedEntityDirectionScript();
		}

		@Override
		public void init(com.artemis.World world, Entity e) {
			eventManager.register(Events.disablePlanetReleaseShip, new EventListener() {
				@Override
				public void onEvent(Event event) {
					disablePlanetReleaseShip(event);
				}
			});
			eventManager.register(Events.enablePlanetReleaseShip, new EventListener() {
				@Override
				public void onEvent(Event event) {
					enablePlanetReleaseShip(event);
				}
			});
		}

		private void disablePlanetReleaseShip(Event event) {
			enabled = false;
		}

		private void enablePlanetReleaseShip(Event event) {
			enabled = true;
		}

		@Override
		public void update(com.artemis.World world, Entity e) {
			updateReleaseAttachment(world, e);
			attachEntityBehavior.update(world, e);
			calculateInputDirectionBehavior.update(world, e);
		}

		private void updateReleaseAttachment(com.artemis.World world, Entity e) {
			if (!enabled)
				return;

			AttachmentComponent entityAttachment = GameComponents.getAttachmentComponent(e);
			Entity attachedEntity = entityAttachment.entity;

			if (attachedEntity == null)
				return;

			if (!shouldReleaseShip(world, e))
				return;

			if (entityAttachment.joint != null)
				physicsWorld.destroyJoint(entityAttachment.joint);

			AttachableComponent attachableComponent = GameComponents.getAttachableComponent(attachedEntity);
			attachableComponent.owner = null;

			entityAttachment.joint = null;
			entityAttachment.entity = null;
		}

		private boolean shouldReleaseShip(com.artemis.World world, Entity e) {
			ControllerComponent controllerComponent = GameComponents.getControllerComponent(e);
			ShipController shipController = controllerComponent.getController();
			return shipController.shouldReleaseShip();
		}

	}

	public static class DestinationPlanetScript extends ScriptJavaImpl {
		private final EventManager eventManager;
		private final EntityTemplate planetFillAnimationTemplate;
		private final EntityFactory entityFactory;

		Parameters parameters = new ParametersWrapper();

		Script attachEntityBehavior;
		Script calculateInputDirectionBehavior;

		boolean destinationReached;

		public DestinationPlanetScript(EventManager eventManager, JointBuilder jointBuilder, EntityFactory entityFactory, EntityTemplate planetFillAnimationTemplate) {
			this.eventManager = eventManager;
			this.entityFactory = entityFactory;
			this.planetFillAnimationTemplate = planetFillAnimationTemplate;
			this.attachEntityBehavior = new Behaviors.AttachEntityScript(jointBuilder);
			calculateInputDirectionBehavior = new Behaviors.AttachedEntityDirectionScript();
		}

		@Override
		public void init(com.artemis.World world, Entity e) {
			destinationReached = false;
		}

		@Override
		public void update(com.artemis.World world, Entity e) {
			attachEntityBehavior.update(world, e);
			calculateInputDirectionBehavior.update(world, e);

			AttachmentComponent attachmentComponent = GameComponents.getAttachmentComponent(e);
			if (attachmentComponent.entity == null)
				return;

			if (destinationReached)
				return;

			eventManager.registerEvent(Events.destinationPlanetReached, e);
			destinationReached = true;

			parameters.put("owner", e);

			entityFactory.instantiate(planetFillAnimationTemplate, parameters);
		}
	}

	public static class GameScript extends ScriptJavaImpl {

		private final EntityFactory entityFactory;
		private final EventManager eventManager;

		private boolean invulnerable;

		Script fixCameraTargetBehavior = new FixCameraTargetScript();

		private Parameters parameters = new ParametersWrapper();

		private EntityTemplate shipTemplate;
		private EntityTemplate attachedShipTemplate;
		// private EntityTemplate particleEmitterTemplate;
		private Entity owner;

		public GameScript(EventManager eventListenerManager, EntityTemplates entityTemplates, EntityFactory entityFactory, boolean invulnerable) {

			this.eventManager = eventListenerManager;
			this.invulnerable = invulnerable;
			this.entityFactory = entityFactory;

			shipTemplate = entityTemplates.shipTemplate;
			attachedShipTemplate = entityTemplates.attachedShipTemplate;
			// particleEmitterTemplate = entityTemplates.particleEmitterTemplate;
		}

		@Override
		public void init(com.artemis.World world, Entity e) {
			this.owner = e;
			eventManager.register(Events.cameraReachedTarget, new EventListener() {
				@Override
				public void onEvent(Event event) {
					cameraReachedTarget(event);
				}
			});
			eventManager.register(Events.destinationPlanetReached, new EventListener() {
				@Override
				public void onEvent(Event event) {
					destinationPlanetReached(event);
				}
			});
			eventManager.registerEvent(Events.gameStarted, owner);
		}

		private void cameraReachedTarget(Event event) {
			eventManager.registerEvent(Events.enablePlanetReleaseShip, owner);
		}

		private void destinationPlanetReached(Event event) {
			eventManager.registerEvent(Events.gameFinished, owner);
		}

		@Override
		public void update(com.artemis.World world, Entity e) {
			regenerateShipIfNoShip(world, e);
			removeShipIfDead(world, e);
			generateShipIfAttachedShipReleased(world, e);
			fixCameraTargetBehavior.update(world, e);
		}

		private void removeShipIfDead(com.artemis.World world, Entity e) {
			if (invulnerable)
				return;

			GameDataComponent gameDataComponent = GameComponents.getGameDataComponent(e);

			Entity ship = gameDataComponent.ship;
			if (ship == null)
				return;

			HealthComponent healthComponent = GameComponents.getHealthComponent(ship);
			if (healthComponent == null)
				return;
			if (!healthComponent.getHealth().isEmpty())
				return;

			Spatial spatial = GameComponents.getSpatial(gameDataComponent.ship);

			world.deleteEntity(gameDataComponent.ship);
			gameDataComponent.ship = null;

			eventManager.registerEvent(Events.shipDeath, spatial);
			eventManager.registerEvent(Events.explosion, spatial);
			eventManager.registerEvent(Events.disablePlanetReleaseShip, e);

			Entity startPlanet = world.getTagManager().getEntity(Groups.startPlanet);
			if (startPlanet != null)
				eventManager.registerEvent(Events.moveCameraToEntity, startPlanet);
		}

		private void regenerateShipIfNoShip(com.artemis.World world, Entity e) {
			GameDataComponent gameDataComponent = GameComponents.getGameDataComponent(e);

			if (gameDataComponent.attachedShip != null)
				return;

			if (gameDataComponent.ship != null)
				return;

			Entity startPlanet = world.getTagManager().getEntity(Groups.startPlanet);
			if (startPlanet == null)
				return;

			Spatial spatial = GameComponents.getSpatial(startPlanet);

			parameters.put("position", spatial.getPosition().tmp().add(0f, 2f));

			Entity attachedShip = entityFactory.instantiate(attachedShipTemplate, parameters);

			AttachmentComponent attachmentComponent = GameComponents.getAttachmentComponent(startPlanet);
			attachmentComponent.setEntity(attachedShip);
			attachmentComponent.setJoint(null);

			AttachableComponent attachableComponent = GameComponents.getAttachableComponent(attachedShip);
			attachableComponent.setOwner(startPlanet);

			gameDataComponent.attachedShip = attachedShip;

			eventManager.registerEvent(Events.shipSpawned, gameDataComponent.attachedShip);
		}

		private void generateShipIfAttachedShipReleased(com.artemis.World world, Entity e) {
			GameDataComponent gameDataComponent = GameComponents.getGameDataComponent(e);

			if (gameDataComponent.attachedShip == null)
				return;

			if (gameDataComponent.ship != null)
				return;

			AttachableComponent attachableComponent = GameComponents.getAttachableComponent(gameDataComponent.attachedShip);
			if (attachableComponent.getOwner() != null)
				return;

			Spatial spatial = GameComponents.getSpatial(gameDataComponent.attachedShip);
			MovementComponent movementComponent = GameComponents.getMovementComponent(gameDataComponent.attachedShip);

			PhysicsComponent attachedShipPhysicsComponent = GameComponents.getPhysicsComponent(gameDataComponent.attachedShip);

			Entity contollerEntity = world.getTagManager().getEntity(Groups.PlayerController);
			if (contollerEntity == null) {
				Gdx.app.log("SuperFlyingThing", "Failed to get controller for released ship");
				return;
			}

			ControllerComponent controllerComponent = GameComponents.getControllerComponent(contollerEntity);

			parameters.put("spatial", spatial);
			parameters.put("controller", controllerComponent.getController());
			parameters.put("maxLinearSpeed", movementComponent.getMaxLinearSpeed());
			parameters.put("maxAngularVelocity", movementComponent.getMaxAngularVelocity());

			gameDataComponent.ship = entityFactory.instantiate(shipTemplate, parameters);

			PhysicsComponent shipPhysicsComponent = GameComponents.getPhysicsComponent(gameDataComponent.ship);

			Vector2 linearVelocity = attachedShipPhysicsComponent.getBody().getLinearVelocity();
			shipPhysicsComponent.getBody().setLinearVelocity(linearVelocity);

			world.deleteEntity(gameDataComponent.attachedShip);
			gameDataComponent.attachedShip = null;

			eventManager.registerEvent(Events.shipReleased, gameDataComponent.ship);
		}
	}

	public static class ParticleEmitterScript extends ScriptJavaImpl {

		@Override
		public void update(com.artemis.World world, Entity e) {
			ParticleEmitterComponent particleEmitterComponent = GameComponents.getParticleEmitter(e);
			ParticleEmitter particleEmitter = particleEmitterComponent.getParticleEmitter();
			if (particleEmitter.isComplete())
				world.deleteEntity(e);
		}

	}

	public static class UpdateAnimationScript extends ScriptJavaImpl {

		@Override
		public void update(com.artemis.World world, Entity e) {
			SpriteComponent spriteComponent = GameComponents.getSpriteComponent(e);
			AnimationComponent animationComponent = GameComponents.getAnimationComponent(e);
			Animation animation = animationComponent.getCurrentAnimation();
			animation.update(GlobalTime.getDelta());
			Sprite sprite = animation.getCurrentFrame();
			spriteComponent.setSprite(sprite);
		}

	}
}
