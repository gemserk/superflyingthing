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
import com.gemserk.commons.artemis.components.CameraComponent;
import com.gemserk.commons.artemis.components.Components;
import com.gemserk.commons.artemis.components.SpriteComponent;
import com.gemserk.commons.artemis.scripts.ScriptJavaImpl;
import com.gemserk.commons.gdx.GlobalTime;
import com.gemserk.commons.gdx.box2d.Contacts;
import com.gemserk.commons.gdx.box2d.Contacts.Contact;
import com.gemserk.commons.gdx.box2d.JointBuilder;
import com.gemserk.commons.gdx.camera.Camera;
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
import com.gemserk.games.superflyingthing.components.Components.TargetComponent;
import com.gemserk.games.superflyingthing.components.GameComponents;
import com.gemserk.games.superflyingthing.entities.Tags;

public class Behaviors {

	public static class CameraFollowScript extends ScriptJavaImpl {

		@Override
		public void update(World world, Entity e) {
			Spatial spatial = GameComponents.getSpatial(e);
			CameraComponent cameraComponent = Components.getCameraComponent(e);
			Camera camera = cameraComponent.getCamera();
			camera.setPosition(spatial.getX(), spatial.getY());
		}

	}

	public static class EntityFollowScript extends ScriptJavaImpl {

		@Override
		public void update(World world, Entity e) {
			TargetComponent targetComponent = GameComponents.getTargetComponent(e);
			Entity target = targetComponent.target;
			if (target == null)
				return;
			Spatial targetSpatial = GameComponents.getSpatial(target);
			if (targetSpatial == null)
				return;
			Spatial spatial = GameComponents.getSpatial(e);
			if (spatial == null)
				return;
			spatial.set(targetSpatial);
		}

	}

	public static class FixMovementScript extends ScriptJavaImpl {

		@Override
		public void update(World world, Entity e) {
			MovementComponent movementComponent = GameComponents.getMovementComponent(e);
			Vector2 direction = movementComponent.direction;

			direction.nor();

			Body body = GameComponents.getPhysics(e).getBody();

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
			AttachmentComponent entityAttachment = GameComponents.getAttachmentComponent(e);

			if (entityAttachment.entity == null)
				return;

			Spatial spatial = GameComponents.getSpatial(e);
			Vector2 position = spatial.getPosition();

			Entity attachedEntity = entityAttachment.entity;
			Spatial attachedEntitySpatial = GameComponents.getSpatial(attachedEntity);
			MovementComponent movementComponent = GameComponents.getMovementComponent(attachedEntity);

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
			AttachmentComponent entityAttachment = GameComponents.getAttachmentComponent(e);
			if (entityAttachment.entity == null)
				return;
			if (entityAttachment.joint != null)
				return;

			Gdx.app.log("SuperFlyingThing", "Building joint for ship with planet");

			AttachableComponent attachableComponent = GameComponents.getAttachableComponent(entityAttachment.entity);
			attachableComponent.owner = e;

			Spatial spatial = GameComponents.getSpatial(e);
			entityAttachment.joint = jointBuilder.distanceJoint() //
					.bodyA(GameComponents.getPhysics(entityAttachment.entity).getBody()) //
					.bodyB(GameComponents.getPhysics(e).getBody()) //
					.collideConnected(false) //
					.length(spatial.getWidth() * 0.5f * 1.5f) //
					.build();
		}

	}

	public static class RemoveWhenGrabbedScript extends ScriptJavaImpl {
		@Override
		public void update(World world, Entity e) {
			GrabbableComponent grabbableComponent = GameComponents.getGrabbableComponent(e);
			if (grabbableComponent.grabbed)
				world.deleteEntity(e);
		}
	}

	public static class FixDirectionFromControllerScript extends ScriptJavaImpl {

		@Override
		public void update(World world, Entity e) {
			MovementComponent movementComponent = GameComponents.getMovementComponent(e);
			ControllerComponent controllerComponent = GameComponents.getControllerComponent(e);

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
			AnimationComponent animationComponent = GameComponents.getAnimationComponent(e);
			SpriteComponent spriteComponent = GameComponents.getSpriteComponent(e);
			Spatial spatial = GameComponents.getSpatial(e);

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

	public static class AttachToAttachableScript extends ScriptJavaImpl {
		@Override
		public void update(World world, Entity e1) {
			Physics physics = GameComponents.getPhysics(e1);
			if (physics == null)
				return;
			Contacts contacts = physics.getContact();
			for (int i = 0; i < contacts.getContactCount(); i++) {
				Contact contact = contacts.getContact(i);
				Entity e2 = (Entity) contact.getOtherFixture().getBody().getUserData();
				updateAttachToAttachable(e1, e2);
				return;
			}

		}

		private void updateAttachToAttachable(Entity e1, Entity e2) {
			AttachmentComponent entityAttachment = GameComponents.getAttachmentComponent(e2);
			if (entityAttachment == null)
				return;
			if (entityAttachment.entity != null)
				return;
			Spatial spatial = GameComponents.getSpatial(e2);
			if (spatial == null)
				return;
			entityAttachment.entity = e1;
		}

	}
	
	public static class GrabGrabbableScript extends ScriptJavaImpl {
		
		@Override
		public void update(World world, Entity e1) {
			Physics physics = GameComponents.getPhysics(e1);
			if (physics == null)
				return;
			Contacts contacts = physics.getContact();
			for (int i = 0; i < contacts.getContactCount(); i++) {
				Contact contact = contacts.getContact(i);
				Entity e2 = (Entity) contact.getOtherFixture().getBody().getUserData();
				updateGrabGrabbable(e1, e2);
				return;
			}

		}

		private void updateGrabGrabbable(Entity e1, Entity e2) {
			if (e2 == null)
				return;
			GrabbableComponent grabbableComponent = GameComponents.getGrabbableComponent(e2);
			if (grabbableComponent == null)
				return;
			if (grabbableComponent.grabbed)
				return;
			grabbableComponent.grabbed = true;
		}

	}

	// game behaviors

	public static class FixCameraTargetScript extends ScriptJavaImpl {
		@Override
		public void update(World world, Entity e) {
			GameDataComponent gameDataComponent = GameComponents.getGameDataComponent(e);
			if (gameDataComponent == null)
				return;
			Entity camera = world.getTagManager().getEntity(Tags.MainCamera);
			if (camera == null)
				return;
			TargetComponent targetComponent = GameComponents.getTargetComponent(camera);

			Entity ship = gameDataComponent.ship;
			if (ship == null) {
				targetComponent.setTarget(null);
				return;
			}

			AttachableComponent attachableComponent = GameComponents.getAttachableComponent(ship);
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
			Physics physics = GameComponents.getPhysics(e);
			if (physics == null)
				return;
			Contacts contacts = physics.getContact();
			for (int i = 0; i < contacts.getContactCount(); i++) {
				Contact contact = contacts.getContact(i);
				Entity otherEntity = (Entity) contact.getOtherFixture().getBody().getUserData();
				if (otherEntity == null)
					continue;

				HealthComponent healthComponent = GameComponents.getHealthComponent(e);
				if (healthComponent == null)
					continue;

				Spatial spatial = GameComponents.getSpatial(e);

				aux.set(1f, 0f).rotate(spatial.getAngle());
				float dot = aux.dot(contact.getNormal());
				if (dot < 0)
					dot = -dot;

				DamageComponent damageComponent = GameComponents.getDamageComponent(otherEntity);
				if (damageComponent == null)
					continue;
				float damage = damageComponent.getDamage() * GlobalTime.getDelta() * dot;
				healthComponent.getHealth().remove(damage);
			}
		}

	}

}