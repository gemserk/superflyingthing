package com.gemserk.games.superflyingthing;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.gemserk.commons.gdx.box2d.BodyBuilder;
import com.gemserk.commons.gdx.box2d.FixtureDefBuilder;
import com.gemserk.commons.gdx.box2d.JointBuilder;
import com.gemserk.commons.gdx.camera.Camera;
import com.gemserk.commons.gdx.camera.Libgdx2dCamera;
import com.gemserk.commons.gdx.games.Physics;
import com.gemserk.commons.gdx.games.PhysicsImpl;
import com.gemserk.commons.gdx.games.Spatial;
import com.gemserk.commons.gdx.games.SpatialImpl;
import com.gemserk.commons.gdx.games.SpatialPhysicsImpl;
import com.gemserk.commons.gdx.graphics.ShapeUtils;
import com.gemserk.commons.gdx.graphics.Triangulator;
import com.gemserk.games.entities.Behavior;
import com.gemserk.games.entities.Entity;
import com.gemserk.games.entities.EntityBuilder;
import com.gemserk.games.entities.EntityManager;
import com.gemserk.games.superflyingthing.Behaviors.CameraFollowBehavior;
import com.gemserk.games.superflyingthing.Components.AliveComponent;
import com.gemserk.games.superflyingthing.Components.AttachableComponent;
import com.gemserk.games.superflyingthing.Components.AttachmentComponent;
import com.gemserk.games.superflyingthing.Components.GrabbableComponent;
import com.gemserk.games.superflyingthing.Components.ReleaseEntityComponent;
import com.gemserk.games.superflyingthing.Components.ScriptComponent;
import com.gemserk.games.superflyingthing.Components.ScriptJavaImpl;
import com.gemserk.games.superflyingthing.Components.ShapeComponent;
import com.gemserk.games.superflyingthing.Components.ShipControllerComponent;
import com.gemserk.games.superflyingthing.Components.SpriteComponent;
import com.gemserk.games.superflyingthing.Components.TargetComponent;
import com.gemserk.resources.ResourceManager;

public class EntityTemplates {

	public static class CategoryBits {

		public static short AllCategoryBits = 0xFF;

		public static short ShipCategoryBits = 1;

		public static short MiniPlanetCategoryBits = 2;

	}

	private final World world;
	private final EntityManager entityManager;
	private final BodyBuilder bodyBuilder;
	private final JointBuilder jointBuilder;
	private final ResourceManager<String> resourceManager;
	private final EntityBuilder entityBuilder;

	public EntityTemplates(World world, EntityManager entityManager, ResourceManager<String> resourceManager, EntityBuilder entityBuilder) {
		this.world = world;
		this.entityManager = entityManager;
		this.resourceManager = resourceManager;
		this.entityBuilder = entityBuilder;
		this.bodyBuilder = new BodyBuilder(world);
		this.jointBuilder = new JointBuilder(world);
	}

	public Entity camera(Camera camera, final Libgdx2dCamera libgdxCamera) {
		return entityBuilder //
				.component(new Components.CameraComponent(camera)) //
				.component(new TargetComponent(null)) //
				.component(new ScriptComponent(new ScriptJavaImpl() {

					private Behavior cameraFollowBehavior = new CameraFollowBehavior();

					@Override
					public void update(EntityManager world, Entity e) {
						cameraFollowBehavior.update(world.getDelta(), e);

						Camera camera = ComponentWrapper.getCamera(e);
						libgdxCamera.move(camera.getX(), camera.getY());
						libgdxCamera.zoom(camera.getZoom());
						libgdxCamera.rotate(camera.getAngle());
					}

				})) //
				.build();
	}

	public Entity ship(float x, float y, Vector2 direction) {
		float width = 0.4f;
		float height = 0.2f;

		Sprite sprite = resourceManager.getResourceValue("WhiteRectangle");

		Entity e = entityBuilder.build();

		Body body = bodyBuilder //
				.fixture(bodyBuilder.fixtureDefBuilder() //
						.restitution(0f) //
						.categoryBits(CategoryBits.ShipCategoryBits) //
						.maskBits((short) (CategoryBits.AllCategoryBits & ~CategoryBits.MiniPlanetCategoryBits)) //
						.boxShape(width * 0.3f, height * 0.3f)) //
				.mass(50f) //
				.position(x, y) //
				.type(BodyType.DynamicBody) //
				.userData(e) //
				.build();

		e.addComponent(Physics.class, new PhysicsImpl(body));
		e.addComponent(Spatial.class, new SpatialPhysicsImpl(body, width, height));
		e.addComponent(new SpriteComponent(sprite));

		Components.MovementComponent movementComponent = new Components.MovementComponent(direction.x, direction.y);
		ComponentWrapper.addMovementComponent(e, movementComponent);

		if (Game.isDebugMode())
			DebugComponents.getMovementComponentDebugWindow().setMovementComponent(movementComponent);

		e.addComponent(new AliveComponent(false));
		e.addComponent(new AttachableComponent());
		e.addComponent(new ShipControllerComponent());

		e.addComponent(new ScriptComponent(new ScriptJavaImpl() {

			Behavior fixMovementBehavior = new Behaviors.FixMovementBehavior();
			Behavior fixDirectionFromControllerBehavior = new Behaviors.FixDirectionFromControllerBehavior();
			Behavior calculateInputDirectionBehavior = new Behaviors.CalculateInputDirectionBehavior();
			Behavior collisionHandlerBehavior = new Behaviors.CollisionHandlerBehavior();

			@Override
			public void update(EntityManager world, Entity e) {
				fixMovementBehavior.update(world.getDelta(), e);
				fixDirectionFromControllerBehavior.update(world.getDelta(), e);
				calculateInputDirectionBehavior.update(world.getDelta(), e);
				collisionHandlerBehavior.update(world.getDelta(), e);
			}

		}));

		return e;
	}

	public Entity diamond(float x, float y, float radius) {
		Entity e = entityBuilder.build();

		Sprite sprite = resourceManager.getResourceValue("WhiteRectangle");

		Body body = bodyBuilder //
				.fixture(bodyBuilder.fixtureDefBuilder() //
						.sensor() //
						.restitution(0f) //
						.circleShape(radius)) //
				.mass(50f) //
				.position(x, y) //
				.type(BodyType.StaticBody) //
				.userData(e) //
				.build();

		e.addComponent(Physics.class, new PhysicsImpl(body));
		e.addComponent(Spatial.class, new SpatialPhysicsImpl(body, radius * 2, radius * 2));
		e.addComponent(new SpriteComponent(sprite));
		e.addComponent(new GrabbableComponent());

		e.addComponent(new ScriptComponent(new ScriptJavaImpl() {

			Behavior removeWhenGrabbedBehavior = new Behaviors.RemoveWhenGrabbedBehavior(entityManager);

			@Override
			public void update(EntityManager world, Entity e) {
				removeWhenGrabbedBehavior.update(world.getDelta(), e);
			}

		}));

		return e;
	}

	public Entity deadShip(Spatial spatial) {
		Sprite sprite = resourceManager.getResourceValue("WhiteRectangle");
		return entityBuilder //
				.component(Spatial.class.getName(), new SpatialImpl(spatial)) //
				.component(new SpriteComponent(sprite, Color.RED)) //
				.build();
	}

	public Entity startPlanet(float x, float y, float radius) {
		Sprite sprite = resourceManager.getResourceValue("Planet");
		Entity e = entityBuilder.build();
		Body body = bodyBuilder //
				.fixture(bodyBuilder.fixtureDefBuilder() //
						.circleShape(radius * 0.1f) //
						.restitution(0f) //
						.categoryBits(CategoryBits.MiniPlanetCategoryBits)) //
				.position(x, y) //
				.mass(1f) //
				.type(BodyType.StaticBody) //
				.userData(e) //
				.build();

		e.addComponent(Physics.class, new PhysicsImpl(body));
		e.addComponent(Spatial.class, new SpatialPhysicsImpl(body, radius * 2, radius * 2));
		e.addComponent(new AttachmentComponent());
		e.addComponent(new ReleaseEntityComponent());
		e.addComponent(new SpriteComponent(sprite, Color.WHITE));

		e.addComponent(new ScriptComponent(new ScriptJavaImpl() {

			Behavior releaseAttachmentBehavior = new Behaviors.ReleaseAttachmentBehavior(world);
			Behavior attachEntityBehavior = new Behaviors.AttachEntityBehavior(jointBuilder);
			Behavior calculateInputDirectionBehavior = new Behaviors.AttachedEntityDirectionBehavior();

			@Override
			public void update(EntityManager world, Entity e) {
				releaseAttachmentBehavior.update(world.getDelta(), e);
				attachEntityBehavior.update(world.getDelta(), e);
				calculateInputDirectionBehavior.update(world.getDelta(), e);
			}

		}));

		return e;
	}

	public Entity destinationPlanet(float x, float y, float radius, Trigger destinationReachedTrigger) {
		Sprite sprite = resourceManager.getResourceValue("Planet");
		Entity e = entityBuilder.build();
		Body body = bodyBuilder //
				.fixture(bodyBuilder.fixtureDefBuilder() //
						.circleShape(radius * 0.1f) //
						.categoryBits(CategoryBits.MiniPlanetCategoryBits) //
						.restitution(0f)) //
				.fixture(bodyBuilder.fixtureDefBuilder() //
						.circleShape(radius) //
						.categoryBits(CategoryBits.AllCategoryBits) //
						.sensor()) //
				.position(x, y) //
				.mass(1f) //
				.type(BodyType.StaticBody) //
				.userData(e) //
				.build();

		e.addComponent(Physics.class, new PhysicsImpl(body));
		e.addComponent(Spatial.class, new SpatialPhysicsImpl(body, radius * 2, radius * 2));
		e.addComponent(new SpriteComponent(sprite, Color.WHITE));
		e.addComponent(new AttachmentComponent());
		e.addComponent(new ReleaseEntityComponent());

		e.addComponent("destinationReachedTrigger", destinationReachedTrigger);

		e.addComponent(new ScriptComponent(new ScriptJavaImpl() {

			Behavior attachEntityBehavior = new Behaviors.AttachEntityBehavior(jointBuilder);
			Behavior calculateInputDirectionBehavior = new Behaviors.AttachedEntityDirectionBehavior();

			@Override
			public void update(EntityManager world, Entity e) {
				attachEntityBehavior.update(world.getDelta(), e);
				calculateInputDirectionBehavior.update(world.getDelta(), e);

				AttachmentComponent attachmentComponent = ComponentWrapper.getEntityAttachment(e);
				if (attachmentComponent.entity == null)
					return;
				Trigger trigger = e.getComponent("destinationReachedTrigger");
				trigger.trigger(e);
			}

		}));

		return e;
	}

	public Entity obstacle(Vector2[] vertices, float x, float y, float angle) {
		Entity e = entityBuilder.build();

		Triangulator triangulator = ShapeUtils.triangulate(vertices);

		FixtureDef[] fixtureDefs = new FixtureDef[triangulator.getTriangleCount()];
		FixtureDefBuilder fixtureDefBuilder = new FixtureDefBuilder();

		for (int i = 0; i < triangulator.getTriangleCount(); i++) {
			Vector2[] v = new Vector2[3];
			for (int p = 0; p < 3; p++) {
				float[] pt = triangulator.getTrianglePoint(i, p);
				v[p] = new Vector2(pt[0], pt[1]);
			}
			fixtureDefs[i] = fixtureDefBuilder //
					.polygonShape(v) //
					.restitution(0f) //
					.build();
		}

		Body body = bodyBuilder.mass(1f) //
				.fixtures(fixtureDefs) //
				.position(x, y) //
				.type(BodyType.StaticBody) //
				.angle(angle) //
				.build();

		e.addComponent(Physics.class, new PhysicsImpl(body));
		e.addComponent(Spatial.class, new SpatialPhysicsImpl(body, 1f, 1f));
		e.addComponent(ShapeComponent.class, new ShapeComponent(vertices, Color.BLUE, triangulator));

		return e;
	}

	public Entity boxObstacle(float x, float y, float w, float h, float angle) {
		Entity e = entityBuilder.build();
		Body body = bodyBuilder //
				.fixture(bodyBuilder.fixtureDefBuilder() //
						.boxShape(w * 0.5f, h * 0.5f) //
						.restitution(1f) //
						.friction(0f) //
				) //
				.type(BodyType.StaticBody) //
				.mass(1f)//
				.position(x, y) //
				.angle(angle) //
				.build();
		e.addComponent(Physics.class, new PhysicsImpl(body));
		e.addComponent(Spatial.class, new SpatialPhysicsImpl(body, 1f, 1f));
		e.addComponent(ShapeComponent.class, new ShapeComponent(new Vector2[] { //
				new Vector2(w * 0.5f, h * 0.5f), //
						new Vector2(w * 0.5f, -h * 0.5f), //
						new Vector2(-w * 0.5f, -h * 0.5f), //
						new Vector2(-w * 0.5f, h * 0.5f), //
				}, Color.BLUE));
		return e;
	}

}