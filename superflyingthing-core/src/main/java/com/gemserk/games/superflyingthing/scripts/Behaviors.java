package com.gemserk.games.superflyingthing.scripts;

import com.artemis.Entity;
import com.artemis.World;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.gemserk.animation4j.gdx.Animation;
import com.gemserk.commons.artemis.components.SpriteComponent;
import com.gemserk.commons.gdx.box2d.Contact;
import com.gemserk.commons.gdx.box2d.JointBuilder;
import com.gemserk.commons.gdx.camera.Camera;
import com.gemserk.commons.gdx.games.Physics;
import com.gemserk.commons.gdx.games.Spatial;
import com.gemserk.games.superflyingthing.ComponentWrapper;
import com.gemserk.games.superflyingthing.Components.AliveComponent;
import com.gemserk.games.superflyingthing.Components.AnimationComponent;
import com.gemserk.games.superflyingthing.Components.AttachableComponent;
import com.gemserk.games.superflyingthing.Components.AttachmentComponent;
import com.gemserk.games.superflyingthing.Components.ControllerComponent;
import com.gemserk.games.superflyingthing.Components.GameDataComponent;
import com.gemserk.games.superflyingthing.Components.GrabbableComponent;
import com.gemserk.games.superflyingthing.Components.MovementComponent;
import com.gemserk.games.superflyingthing.Components.ShipControllerComponent;
import com.gemserk.games.superflyingthing.Components.TargetComponent;

class Behaviors {

	public static class CameraFollowBehavior extends Behavior {

		@Override
		public void update(World world, Entity e) {
			Spatial spatial = ComponentWrapper.getSpatial(e);
			Camera camera = ComponentWrapper.getCamera(e);
			camera.setPosition(spatial.getX(), spatial.getY());
		}

	}

	public static class EntityFollowBehavior extends Behavior {

		@Override
		public void update(World world, Entity e) {
			TargetComponent targetComponent = e.getComponent(TargetComponent.class);
			Entity target = targetComponent.target;
			if (target == null)
				return;
			Spatial targetSpatial = ComponentWrapper.getSpatial(target);
			if (targetSpatial == null)
				return;
			Spatial spatial = ComponentWrapper.getSpatial(e);
			if (spatial == null)
				return;
			spatial.set(targetSpatial);
		}

	}

	public static class FixMovementBehavior extends Behavior {

		@Override
		public void update(World world, Entity e) {
			MovementComponent movementComponent = e.getComponent(MovementComponent.class);
			Vector2 direction = movementComponent.direction;

			direction.nor();

			Body body = ComponentWrapper.getPhysics(e).getBody();

			Vector2 position = body.getTransform().getPosition();
			float desiredAngle = direction.angle();

			body.getTransform().getPosition().set(position);
			// body.setTransform(position, desiredAngle * MathUtils.degreesToRadians);
			body.applyForce(direction.tmp().mul(5000f), position);

			Vector2 linearVelocity = body.getLinearVelocity();

			float speed = linearVelocity.len();

			linearVelocity.set(direction.tmp().mul(speed));

			float maxSpeed = movementComponent.maxLinearSpeed;
			if (speed > maxSpeed) {
				linearVelocity.mul(maxSpeed / speed);
				body.setLinearVelocity(linearVelocity);
			}
		}

	}

	public static class AttachedEntityDirectionBehavior extends Behavior {

		@Override
		public void update(World world, Entity e) {
			AttachmentComponent entityAttachment = ComponentWrapper.getEntityAttachment(e);

			if (entityAttachment.entity == null)
				return;

			Spatial spatial = ComponentWrapper.getSpatial(e);
			Vector2 position = spatial.getPosition();

			Entity attachedEntity = entityAttachment.entity;
			Spatial attachedEntitySpatial = ComponentWrapper.getSpatial(attachedEntity);
			MovementComponent movementComponent = ComponentWrapper.getComponent(attachedEntity, MovementComponent.class);

			Vector2 superSheepPosition = attachedEntitySpatial.getPosition();

			Vector2 diff = superSheepPosition.sub(position).nor();
			diff.rotate(-90f);

			movementComponent.getDirection().set(diff);
		}

	}

	public static class AttachEntityBehavior extends Behavior {

		private final JointBuilder jointBuilder;

		public AttachEntityBehavior(JointBuilder jointBuilder) {
			this.jointBuilder = jointBuilder;
		}

		@Override
		public void update(World world, Entity e) {
			AttachmentComponent entityAttachment = ComponentWrapper.getEntityAttachment(e);
			if (entityAttachment.entity == null)
				return;
			if (entityAttachment.joint != null)
				return;

			Gdx.app.log("SuperFlyingThing", "Building joint for ship with planet");

			AttachableComponent attachableComponent = entityAttachment.entity.getComponent(AttachableComponent.class);
			attachableComponent.owner = e;

			Spatial spatial = ComponentWrapper.getSpatial(e);
			entityAttachment.joint = jointBuilder.distanceJoint() //
					.bodyA(ComponentWrapper.getPhysics(entityAttachment.entity).getBody()) //
					.bodyB(ComponentWrapper.getPhysics(e).getBody()) //
					.collideConnected(false) //
					.length(spatial.getWidth() * 0.5f * 1.5f) //
					.build();
		}

	}

	public static class RemoveWhenGrabbedBehavior extends Behavior {

		private final com.artemis.World world;

		public RemoveWhenGrabbedBehavior(com.artemis.World world) {
			this.world = world;
		}

		@Override
		public void update(World world, Entity e) {
			GrabbableComponent grabbableComponent = e.getComponent(GrabbableComponent.class);
			if (grabbableComponent.grabbed)
				world.deleteEntity(e);
		}
	}

	public static class FixDirectionFromControllerBehavior extends Behavior {

		@Override
		public void update(World world, Entity e) {
			MovementComponent movementComponent = e.getComponent(MovementComponent.class);
			ShipControllerComponent shipControllerComponent = e.getComponent(ShipControllerComponent.class);

			if (movementComponent == null)
				return;
			if (shipControllerComponent == null)
				return;

			float movementDirection = shipControllerComponent.direction;
			Vector2 direction = movementComponent.direction;

			float rotationAngle = 0f;
			float angularVelocity = movementComponent.angularVelocity;

			float maxAngularVelocity = movementComponent.getMaxAngularVelocity();
			float minAngularVelocity = 0f;

			angularVelocity = (1 - movementDirection) * minAngularVelocity + movementDirection * maxAngularVelocity;
			rotationAngle = angularVelocity * world.getDelta() * 0.001f;

			movementComponent.angularVelocity = angularVelocity;
			direction.rotate(rotationAngle);
		}

	}

	public static class CalculateInputDirectionBehavior extends Behavior {

		@Override
		public void update(World world, Entity e) {
			ShipControllerComponent shipControllerComponent = e.getComponent(ShipControllerComponent.class);
			if (shipControllerComponent == null)
				return;
			ControllerComponent controllerComponent = ComponentWrapper.getControllerComponent(e);
			if (controllerComponent == null)
				return;
			shipControllerComponent.direction = controllerComponent.getController().getMovementDirection();
		}

	}

	public static class UpdateSpriteFromAnimation extends Behavior {

		@Override
		public void update(World world, Entity e) {
			AnimationComponent animationComponent = ComponentWrapper.getAnimation(e);
			MovementComponent movementComponent = ComponentWrapper.getMovementComponent(e);
			SpriteComponent spriteComponent = ComponentWrapper.getSpriteComponent(e);

			// float angle = spatial.getAngle();
			float angle = movementComponent.getDirection().angle();
			Animation animation = animationComponent.getCurrentAnimation();

			int frameIndex = getAnimationForAngle(angle - 5f);
			Sprite frame = animation.getFrame(frameIndex);

			spriteComponent.setSprite(frame);
		}

		private int getAnimationForAngle(float angle) {
			// return 0;
			if (angle < 360f)
				angle += 360f;
			angle %= 360f;
			double floor = Math.floor(angle * 0.1f);
			return (int) (floor);
		}

	}

	public static class CollisionHandlerBehavior extends Behavior {
		@Override
		public void update(World world, Entity e1) {
			Physics physics = ComponentWrapper.getPhysics(e1);
			if (physics == null)
				return;
			Contact contact = physics.getContact();
			for (int i = 0; i < contact.getContactCount(); i++) {
				if (!contact.isInContact(i))
					continue;
				Entity e2 = (Entity) contact.getUserData(i);
				updateGrabGrabbable(e1, e2);
				updateAttachToAttachable(e1, e2);
				updateAliveCollision(e1, e2);
				return;
			}

		}

		private void updateGrabGrabbable(Entity e1, Entity e2) {
			if (e2 == null)
				return;
			GrabbableComponent grabbableComponent = e2.getComponent(GrabbableComponent.class);
			if (grabbableComponent == null)
				return;
			if (grabbableComponent.grabbed)
				return;
			grabbableComponent.grabbed = true;
		}

		private void updateAttachToAttachable(Entity e1, Entity e2) {
			AttachmentComponent entityAttachment = ComponentWrapper.getEntityAttachment(e2);
			if (entityAttachment == null)
				return;
			if (entityAttachment.entity != null)
				return;
			Spatial spatial = ComponentWrapper.getSpatial(e2);
			if (spatial == null)
				return;
			entityAttachment.entity = e1;
		}

		// TODO: change it for a trigger instead... and decide to kill the entity outside

		private void updateAliveCollision(Entity e, Entity e2) {
			if (e2 != null)
				return;
			AliveComponent aliveComponent = e.getComponent(AliveComponent.class);
			AttachableComponent attachableComponent = e.getComponent(AttachableComponent.class);
			if (attachableComponent.owner != null)
				return;
			if (aliveComponent == null)
				return;
			aliveComponent.setDead(true);
		}
	}

	// game behaviors

	public static class FixCameraTargetBehavior extends Behavior {
		@Override
		public void update(World world, Entity e) {
			GameDataComponent gameDataComponent = ComponentWrapper.getGameData(e);
			if (gameDataComponent == null)
				return;
			TargetComponent targetComponent = gameDataComponent.camera.getComponent(TargetComponent.class);

			Entity ship = gameDataComponent.ship;
			if (ship == null) {
				targetComponent.setTarget(gameDataComponent.startPlanet);
				return;
			}

			AttachableComponent attachableComponent = ship.getComponent(AttachableComponent.class);
			if (attachableComponent.getOwner() != null)
				targetComponent.setTarget(attachableComponent.getOwner());
			else
				targetComponent.setTarget(ship);
		}
	}

}