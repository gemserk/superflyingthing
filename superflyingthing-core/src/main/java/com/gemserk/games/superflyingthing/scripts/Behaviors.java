package com.gemserk.games.superflyingthing.scripts;

import com.artemis.Entity;
import com.artemis.World;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.gemserk.animation4j.gdx.Animation;
import com.gemserk.commons.artemis.components.AnimationComponent;
import com.gemserk.commons.artemis.components.SpriteComponent;
import com.gemserk.commons.artemis.scripts.ScriptJavaImpl;
import com.gemserk.commons.gdx.GlobalTime;
import com.gemserk.commons.gdx.box2d.Contact;
import com.gemserk.commons.gdx.box2d.JointBuilder;
import com.gemserk.commons.gdx.camera.Camera;
import com.gemserk.commons.gdx.games.Physics;
import com.gemserk.commons.gdx.games.Spatial;
import com.gemserk.games.superflyingthing.components.ComponentWrapper;
import com.gemserk.games.superflyingthing.components.Components.AttachableComponent;
import com.gemserk.games.superflyingthing.components.Components.AttachmentComponent;
import com.gemserk.games.superflyingthing.components.Components.CameraComponent;
import com.gemserk.games.superflyingthing.components.Components.ControllerComponent;
import com.gemserk.games.superflyingthing.components.Components.DamageComponent;
import com.gemserk.games.superflyingthing.components.Components.GameDataComponent;
import com.gemserk.games.superflyingthing.components.Components.GrabbableComponent;
import com.gemserk.games.superflyingthing.components.Components.HealthComponent;
import com.gemserk.games.superflyingthing.components.Components.MovementComponent;
import com.gemserk.games.superflyingthing.components.Components.TargetComponent;

public class Behaviors {

	public static class CameraFollowScript extends ScriptJavaImpl {

		@Override
		public void update(World world, Entity e) {
			Spatial spatial = ComponentWrapper.getSpatial(e);
			CameraComponent cameraComponent = ComponentWrapper.getCameraComponent(e);
			Camera camera = cameraComponent.getCamera();
			camera.setPosition(spatial.getX(), spatial.getY());
		}

	}

	public static class EntityFollowScript extends ScriptJavaImpl {

		@Override
		public void update(World world, Entity e) {
			TargetComponent targetComponent = ComponentWrapper.getTargetComponent(e);
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

	public static class FixMovementScript extends ScriptJavaImpl {

		@Override
		public void update(World world, Entity e) {
			MovementComponent movementComponent = ComponentWrapper.getMovementComponent(e);
			Vector2 direction = movementComponent.direction;

			direction.nor();

			Body body = ComponentWrapper.getPhysics(e).getBody();

			Vector2 position = body.getTransform().getPosition();
			float desiredAngle = direction.angle();

			// body.getTransform().getPosition().set(position);
			body.setTransform(position, desiredAngle * MathUtils.degreesToRadians);
			body.applyForce(direction.tmp().mul(500000f * GlobalTime.getDelta()), position);

			Vector2 linearVelocity = body.getLinearVelocity();

			float speed = linearVelocity.len();

			linearVelocity.set(direction.tmp().mul(speed));

			float maxSpeed = movementComponent.getMaxLinearSpeed();
			if (speed > maxSpeed) {
				linearVelocity.mul(maxSpeed / speed);
				body.setLinearVelocity(linearVelocity);
			}
		}

	}

	public static class AttachedEntityDirectionScript extends ScriptJavaImpl {

		@Override
		public void update(World world, Entity e) {
			AttachmentComponent entityAttachment = ComponentWrapper.getAttachmentComponent(e);

			if (entityAttachment.entity == null)
				return;

			Spatial spatial = ComponentWrapper.getSpatial(e);
			Vector2 position = spatial.getPosition();

			Entity attachedEntity = entityAttachment.entity;
			Spatial attachedEntitySpatial = ComponentWrapper.getSpatial(attachedEntity);
			MovementComponent movementComponent = ComponentWrapper.getMovementComponent(attachedEntity);

			Vector2 superSheepPosition = attachedEntitySpatial.getPosition();

			Vector2 diff = superSheepPosition.sub(position).nor();
			diff.rotate(-90f);

			movementComponent.getDirection().set(diff);
		}

	}

	public static class AttachEntityScript extends ScriptJavaImpl {

		private final JointBuilder jointBuilder;

		public AttachEntityScript(JointBuilder jointBuilder) {
			this.jointBuilder = jointBuilder;
		}

		@Override
		public void update(World world, Entity e) {
			AttachmentComponent entityAttachment = ComponentWrapper.getAttachmentComponent(e);
			if (entityAttachment.entity == null)
				return;
			if (entityAttachment.joint != null)
				return;

			Gdx.app.log("SuperFlyingThing", "Building joint for ship with planet");

			AttachableComponent attachableComponent = ComponentWrapper.getAttachableComponent(entityAttachment.entity);
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

	public static class RemoveWhenGrabbedScript extends ScriptJavaImpl {
		@Override
		public void update(World world, Entity e) {
			GrabbableComponent grabbableComponent = ComponentWrapper.getGrabbableComponent(e);
			if (grabbableComponent.grabbed)
				world.deleteEntity(e);
		}
	}

	public static class FixDirectionFromControllerScript extends ScriptJavaImpl {

		@Override
		public void update(World world, Entity e) {
			MovementComponent movementComponent = ComponentWrapper.getMovementComponent(e);
			ControllerComponent controllerComponent = ComponentWrapper.getControllerComponent(e);

			if (movementComponent == null)
				return;
			if (controllerComponent == null)
				return;

			float movementDirection = controllerComponent.getController().getMovementDirection();
			Vector2 direction = movementComponent.direction;

			float rotationAngle = 0f;
			float angularVelocity = movementComponent.angularVelocity;

			float maxAngularVelocity = movementComponent.getMaxAngularVelocity();
			float minAngularVelocity = 0f;

			angularVelocity = (1 - movementDirection) * minAngularVelocity + movementDirection * maxAngularVelocity;
			rotationAngle = angularVelocity * GlobalTime.getDelta();

			movementComponent.angularVelocity = angularVelocity;
			direction.rotate(rotationAngle);
		}

	}

	public static class ShipAnimationScript extends ScriptJavaImpl {

		@Override
		public void update(World world, Entity e) {
			AnimationComponent animationComponent = ComponentWrapper.getAnimationComponent(e);
			SpriteComponent spriteComponent = ComponentWrapper.getSpriteComponent(e);
			Spatial spatial = ComponentWrapper.getSpatial(e);

			float angle = spatial.getAngle();

			Animation animation = animationComponent.getCurrentAnimation();

			int framesCount = animation.getFramesCount();
			float frameAngle = 360f / framesCount;

			int frameIndex = getAnimationForAngle(angle - (frameAngle * 0.5f), framesCount);
			Sprite frame = animation.getFrame(frameIndex);

			spriteComponent.setSprite(frame);
			spriteComponent.setUpdateRotation(false);
		}

		private int getAnimationForAngle(float angle, int animationFrames) {
			if (angle < 360f)
				angle += 360f;
			angle %= 360f;
			float anglePerFrame = (float) animationFrames / 360f;
			double floor = Math.floor(angle * anglePerFrame);
			return (int) (floor);
		}

	}

	public static class CollisionHandlerScript extends ScriptJavaImpl {
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
				return;
			}

		}

		private void updateGrabGrabbable(Entity e1, Entity e2) {
			if (e2 == null)
				return;
			GrabbableComponent grabbableComponent = ComponentWrapper.getGrabbableComponent(e2);
			if (grabbableComponent == null)
				return;
			if (grabbableComponent.grabbed)
				return;
			grabbableComponent.grabbed = true;
		}

		private void updateAttachToAttachable(Entity e1, Entity e2) {
			AttachmentComponent entityAttachment = ComponentWrapper.getAttachmentComponent(e2);
			if (entityAttachment == null)
				return;
			if (entityAttachment.entity != null)
				return;
			Spatial spatial = ComponentWrapper.getSpatial(e2);
			if (spatial == null)
				return;
			entityAttachment.entity = e1;
		}

	}

	// game behaviors

	public static class FixCameraTargetScript extends ScriptJavaImpl {
		@Override
		public void update(World world, Entity e) {
			GameDataComponent gameDataComponent = ComponentWrapper.getGameDataComponent(e);
			if (gameDataComponent == null)
				return;
			TargetComponent targetComponent = ComponentWrapper.getTargetComponent(gameDataComponent.camera);

			Entity ship = gameDataComponent.ship;
			if (ship == null) {
				targetComponent.setTarget(null);
				return;
			}

			AttachableComponent attachableComponent = ComponentWrapper.getAttachableComponent(ship);
			if (attachableComponent.getOwner() != null)
				targetComponent.setTarget(attachableComponent.getOwner());
			else
				targetComponent.setTarget(ship);
		}
	}

	public static class PerformDamageFromCollidingEntityScript extends ScriptJavaImpl {

		private final Vector2 aux = new Vector2();

		@Override
		public void update(World world, Entity e) {
			Physics physics = ComponentWrapper.getPhysics(e);
			if (physics == null)
				return;
			Contact contact = physics.getContact();
			for (int i = 0; i < contact.getContactCount(); i++) {
				if (!contact.isInContact(i))
					continue;
				Entity otherEntity = (Entity) contact.getUserData(i);
				if (otherEntity == null)
					continue;

				HealthComponent healthComponent = ComponentWrapper.getHealthComponent(e);
				if (healthComponent == null)
					continue;

				Spatial spatial = ComponentWrapper.getSpatial(e);

				aux.set(1f, 0f).rotate(spatial.getAngle());
				float dot = aux.dot(contact.getNormal(i));
				if (dot < 0)
					dot = -dot;

				DamageComponent damageComponent = ComponentWrapper.getDamageComponent(otherEntity);
				if (damageComponent == null)
					continue;
				float damage = damageComponent.getDamage() * GlobalTime.getDelta() * dot;
				healthComponent.getHealth().remove(damage);
			}
		}

	}

}