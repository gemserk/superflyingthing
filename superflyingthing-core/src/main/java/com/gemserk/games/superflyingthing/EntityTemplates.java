package com.gemserk.games.superflyingthing;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.World;
import com.gemserk.commons.gdx.box2d.BodyBuilder;
import com.gemserk.commons.gdx.box2d.JointBuilder;
import com.gemserk.commons.gdx.camera.Camera;
import com.gemserk.commons.gdx.games.Physics;
import com.gemserk.commons.gdx.games.PhysicsImpl;
import com.gemserk.commons.gdx.games.Spatial;
import com.gemserk.commons.gdx.games.SpatialImpl;
import com.gemserk.commons.gdx.games.SpatialPhysicsImpl;
import com.gemserk.games.entities.Behavior;
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
import com.gemserk.games.superflyingthing.Components.ReleaseEntityComponent;
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

	public EntityTemplates(World world, EntityManager entityManager, ResourceManager<String> resourceManager) {
		this.world = world;
		this.entityManager = entityManager;
		this.resourceManager = resourceManager;
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

	public Entity ship(float x, float y, Vector2 direction) {
		float width = 0.4f;
		float height = 0.2f;

		Sprite sprite = resourceManager.getResourceValue("WhiteRectangle");

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

		e.addBehavior(new Behaviors.FixMovementBehavior());
		e.addBehavior(new Behaviors.FixDirectionFromControllerBehavior());
		e.addBehavior(new Behaviors.CalculateInputDirectionBehavior());
		e.addBehavior(new Behaviors.CollisionHandlerBehavior());

		return e;
	}

	public Entity diamond(float x, float y, float radius) {
		Entity e = new Entity();

		Sprite sprite = resourceManager.getResourceValue("WhiteRectangle");

		Body body = bodyBuilder.mass(50f) //
				.circleShape(radius) //
				.sensor() //
				.position(x, y) //
				.restitution(0f) //
				.type(BodyType.StaticBody) //
				.userData(e) //
				.build();

		e.addComponent(Physics.class, new PhysicsImpl(body));
		e.addComponent(Spatial.class, new SpatialPhysicsImpl(body, radius * 2, radius * 2));
		e.addComponent(new SpriteComponent(sprite));
		e.addComponent(new GrabbableComponent());
		e.addBehavior(new RemoveWhenGrabbedBehavior(entityManager));

		return e;
	}

	public Entity deadShip(Spatial spatial) {
		Sprite sprite = resourceManager.getResourceValue("WhiteRectangle");
		Entity e = new Entity();
		e.addComponent(Spatial.class, new SpatialImpl(spatial));
		e.addComponent(new SpriteComponent(sprite, Color.RED));
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

		e.addComponent(Physics.class, new PhysicsImpl(body));
		e.addComponent(Spatial.class, new SpatialPhysicsImpl(body, radius * 2, radius * 2));
		e.addComponent(new AttachmentComponent());
		e.addComponent(new ReleaseEntityComponent());

		e.addBehavior(new ReleaseAttachmentBehavior(world));
		e.addBehavior(new AttachEntityBehavior(jointBuilder));
		e.addBehavior(new AttachedEntityDirectionBehavior());
		return e;
	}

	public Entity destinationPlanet(float x, float y, float radius, Trigger destinationReachedTrigger) {
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

		e.addComponent(Physics.class, new PhysicsImpl(body));
		e.addComponent(Spatial.class, new SpatialPhysicsImpl(body, radius * 2, radius * 2));
		e.addComponent(new AttachmentComponent());
		e.addComponent(new ReleaseEntityComponent());
		e.addBehavior(new AttachEntityBehavior(jointBuilder));
		e.addBehavior(new AttachedEntityDirectionBehavior());

		e.addComponent("destinationReachedTrigger", destinationReachedTrigger);
		e.addBehavior(new Behavior() {
			@Override
			public void update(int delta, Entity e) {
				AttachmentComponent attachmentComponent = ComponentWrapper.getEntityAttachment(e);
				if (attachmentComponent.entity == null)
					return;
				Trigger trigger = e.getComponent("destinationReachedTrigger");
				trigger.trigger(e);
			}
		});

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
		e.addComponent(Physics.class, new PhysicsImpl(body));
		e.addComponent(Spatial.class, new SpatialPhysicsImpl(body, 1f, 1f));
		e.addComponent(ShapeComponent.class, new ShapeComponent(vertices, Colors.darkBlue));
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
		e.addComponent(Physics.class, new PhysicsImpl(body));
		e.addComponent(Spatial.class, new SpatialPhysicsImpl(body, 1f, 1f));
		e.addComponent(ShapeComponent.class, new ShapeComponent(new Vector2[] { //
				new Vector2(w * 0.5f, h * 0.5f), //
						new Vector2(w * 0.5f, -h * 0.5f), //
						new Vector2(-w * 0.5f, -h * 0.5f), //
						new Vector2(-w * 0.5f, h * 0.5f), //
				}, Colors.darkRed));
		return e;
	}

}