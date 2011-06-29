package com.gemserk.games.superflyingthing;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.World;
import com.gemserk.commons.gdx.box2d.BodyBuilder;
import com.gemserk.commons.gdx.box2d.JointBuilder;
import com.gemserk.commons.gdx.camera.Camera;
import com.gemserk.commons.gdx.games.Spatial;
import com.gemserk.commons.gdx.games.SpatialImpl;
import com.gemserk.commons.gdx.games.SpatialPhysicsImpl;
import com.gemserk.games.entities.Entity;
import com.gemserk.games.entities.EntityManager;
import com.gemserk.games.superflyingthing.Behaviors.AttachEntityBehavior;
import com.gemserk.games.superflyingthing.Behaviors.AttachedEntityDirectionBehavior;
import com.gemserk.games.superflyingthing.Behaviors.CameraFollowBehavior;
import com.gemserk.games.superflyingthing.Behaviors.ReleaseAttachmentBehavior;
import com.gemserk.games.superflyingthing.Behaviors.RemoveWhenGrabbedBehavior;
import com.gemserk.games.superflyingthing.Components.AliveComponent;
import com.gemserk.games.superflyingthing.Components.AttachableComponent;
import com.gemserk.games.superflyingthing.Components.AttachmentComponent;
import com.gemserk.games.superflyingthing.Components.GrabbableComponent;
import com.gemserk.games.superflyingthing.Components.InputDirectionComponent;
import com.gemserk.games.superflyingthing.Components.MovementComponent;
import com.gemserk.games.superflyingthing.Components.PhysicsComponent;
import com.gemserk.games.superflyingthing.Components.ReleaseEntityComponent;
import com.gemserk.games.superflyingthing.Components.SpatialComponent;
import com.gemserk.games.superflyingthing.Components.SpriteComponent;
import com.gemserk.games.superflyingthing.Components.TargetComponent;

public class EntityFactory {
	
	public static class CategoryBits {

		public static short AllCategoryBits = 0xFF;

		public static short ShipCategoryBits = 1;

		public static short MiniPlanetCategoryBits = 2;

	}

	private final World world;
	private final EntityManager entityManager;
	private final BodyBuilder bodyBuilder;
	private final JointBuilder jointBuilder;

	public EntityFactory(World world, EntityManager entityManager) {
		this.world = world;
		this.entityManager = entityManager;
		this.bodyBuilder = new BodyBuilder(world);
		this.jointBuilder = new JointBuilder(world);
	}

	public Entity camera(Camera camera) {
		Entity e = new Entity();
		e.addComponent(new Components.CameraComponent(camera));
		e.addComponent(new TargetComponent(null));
		e.addBehavior(new CameraFollowBehavior());
		return e;
	}

	public Entity ship(float x, float y, Sprite sprite, Vector2 direction) {
		float width = 0.4f;
		float height = 0.2f;

		Entity e = new Entity();

		Body body = bodyBuilder.mass(50f) //
				.boxShape(width * 0.3f, height * 0.3f) //
				.position(x, y) //
				.restitution(0f) //
				.type(BodyType.DynamicBody) //
				.categoryBits(CategoryBits.ShipCategoryBits) //
				.maskBits((short) (CategoryBits.AllCategoryBits & ~CategoryBits.MiniPlanetCategoryBits)) //
				.userData(e) //
				.build();

		e.addComponent(new PhysicsComponent(body));
		e.addComponent(new SpatialComponent(new SpatialPhysicsImpl(body, width, height)));
		e.addComponent(new SpriteComponent(sprite));
		e.addComponent(new MovementComponent(direction.x, direction.y));
		e.addComponent(new AliveComponent(false));
		e.addComponent(new AttachableComponent());
		e.addComponent(new InputDirectionComponent());

		e.addBehavior(new Behaviors.FixMovementBehavior());
		e.addBehavior(new Behaviors.FixDirectionFromInputBehavior());
		e.addBehavior(new Behaviors.CalculateInputDirectionBehavior());
		e.addBehavior(new Behaviors.CollisionHandlerBehavior());

		return e;
	}

	public Entity diamond(float x, float y, float radius, Sprite sprite) {
		Entity e = new Entity();

		Body body = bodyBuilder.mass(50f) //
				.circleShape(radius) //
				.sensor() //
				.position(x, y) //
				.restitution(0f) //
				.type(BodyType.StaticBody) //
				.userData(e) //
				.build();

		e.addComponent(new PhysicsComponent(body));
		e.addComponent(new SpatialComponent(new SpatialPhysicsImpl(body, radius * 2, radius * 2)));
		e.addComponent(new SpriteComponent(sprite));
		e.addComponent(new GrabbableComponent());
		e.addBehavior(new RemoveWhenGrabbedBehavior(entityManager));

		// e.addBehavior(new Behavior() {
		//
		// @Override
		// public void update(int delta, Entity e) {
		// GrabbableComponent grabbableComponent = e.getComponent(GrabbableComponent.class);
		// if (grabbableComponent == null)
		// return;
		// PhysicsComponent physicsComponent = e.getComponent(PhysicsComponent.class);
		// if (physicsComponent == null)
		// return;
		//
		// if (physicsComponent.getContact().isInContact())
		// grabbableComponent.grabbed = true;
		// }
		//
		// });

		return e;
	}

	public Entity deadShip(Spatial spatial, Sprite sprite) {
		Entity e = new Entity();
		e.addComponent(new SpatialComponent(new SpatialImpl(spatial)));
		e.addComponent(new SpriteComponent(sprite));
		return e;
	}

	public Entity startPlanet(float x, float y, float radius) {
		Entity e = new Entity();

		Body body = bodyBuilder.mass(1f) //
				.circleShape(radius * 0.1f) //
				.position(x, y) //
				.restitution(0f) //
				.type(BodyType.StaticBody) //
				.userData(e) //
				.categoryBits(CategoryBits.MiniPlanetCategoryBits).build();

		e.addComponent(new PhysicsComponent(body));
		e.addComponent(new SpatialComponent(new SpatialPhysicsImpl(body, radius * 2, radius * 2)));
		e.addComponent(new AttachmentComponent());
		e.addComponent(new ReleaseEntityComponent());

		e.addBehavior(new ReleaseAttachmentBehavior(world));
		e.addBehavior(new AttachEntityBehavior(jointBuilder));
		e.addBehavior(new AttachedEntityDirectionBehavior());
		return e;
	}

	public Entity destinationPlanet(float x, float y, float radius) {
		Entity e = new Entity();

		Body body = bodyBuilder.mass(1f) //
				.circleShape(radius * 0.1f) //
				.position(x, y) //
				.restitution(0f) //
				.type(BodyType.StaticBody) //
				.userData(e) //
				.categoryBits(CategoryBits.MiniPlanetCategoryBits).build();

		bodyBuilder.fixtureBuilder(body) //
				.circleShape(radius) //
				.categoryBits(CategoryBits.AllCategoryBits) //
				.sensor() //
				.build();

		e.addComponent(new PhysicsComponent(body));
		e.addComponent(new SpatialComponent(new SpatialPhysicsImpl(body, radius * 2, radius * 2)));
		e.addComponent(new AttachmentComponent());
		e.addComponent(new ReleaseEntityComponent());

		e.addBehavior(new AttachEntityBehavior(jointBuilder));
		e.addBehavior(new AttachedEntityDirectionBehavior());
		return e;
	}

	public Entity obstacle(Vector2[] vertices, float x, float y, float angle) {
		Entity e = new Entity();
		Body body = bodyBuilder.mass(1f) //
				.polygonShape(vertices) //
				.position(x, y) //
				.restitution(0f) //
				.type(BodyType.StaticBody) //
				.angle(angle) //
				.build();
		e.addComponent(new PhysicsComponent(body));
		return e;
	}

	public Entity boxObstacle(float x, float y, float w, float h, float angle) {
		Entity e = new Entity();
		Body body = bodyBuilder.type(BodyType.StaticBody) //
				.boxShape(w * 0.5f, h * 0.5f) //
				.restitution(1f) //
				.mass(1f)//
				.friction(0f) //
				.position(x, y) //
				.angle(angle) //
				.build();
		e.addComponent(new PhysicsComponent(body));
		return e;
	}

}