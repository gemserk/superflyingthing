package com.gemserk.games.superflyingthing.templates;

import com.artemis.Entity;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.gemserk.animation4j.gdx.Animation;
import com.gemserk.commons.artemis.EntityBuilder;
import com.gemserk.commons.artemis.components.ContainerComponent;
import com.gemserk.commons.artemis.components.OwnerComponent;
import com.gemserk.commons.artemis.components.PhysicsComponent;
import com.gemserk.commons.artemis.components.RenderableComponent;
import com.gemserk.commons.artemis.components.ScriptComponent;
import com.gemserk.commons.artemis.components.SpatialComponent;
import com.gemserk.commons.artemis.components.SpriteComponent;
import com.gemserk.commons.artemis.events.EventManager;
import com.gemserk.commons.artemis.scripts.Script;
import com.gemserk.commons.artemis.templates.EntityFactory;
import com.gemserk.commons.artemis.templates.EntityTemplate;
import com.gemserk.commons.artemis.templates.EntityTemplateImpl;
import com.gemserk.commons.gdx.box2d.BodyBuilder;
import com.gemserk.commons.gdx.box2d.FixtureDefBuilder;
import com.gemserk.commons.gdx.camera.Camera;
import com.gemserk.commons.gdx.camera.Libgdx2dCamera;
import com.gemserk.commons.gdx.games.PhysicsImpl;
import com.gemserk.commons.gdx.games.Spatial;
import com.gemserk.commons.gdx.games.SpatialHierarchicalImpl;
import com.gemserk.commons.gdx.games.SpatialImpl;
import com.gemserk.commons.gdx.games.SpatialPhysicsImpl;
import com.gemserk.commons.gdx.graphics.Mesh2dBuilder;
import com.gemserk.commons.gdx.graphics.ParticleEmitterUtils;
import com.gemserk.commons.gdx.graphics.ShapeUtils;
import com.gemserk.commons.gdx.graphics.Triangulator;
import com.gemserk.componentsengine.utils.Container;
import com.gemserk.componentsengine.utils.Parameters;
import com.gemserk.componentsengine.utils.ParametersWrapper;
import com.gemserk.games.superflyingthing.Colors;
import com.gemserk.games.superflyingthing.ShipController;
import com.gemserk.games.superflyingthing.components.ComponentWrapper;
import com.gemserk.games.superflyingthing.components.Components;
import com.gemserk.games.superflyingthing.components.Components.AnimationComponent;
import com.gemserk.games.superflyingthing.components.Components.AttachableComponent;
import com.gemserk.games.superflyingthing.components.Components.AttachmentComponent;
import com.gemserk.games.superflyingthing.components.Components.ControllerComponent;
import com.gemserk.games.superflyingthing.components.Components.GrabbableComponent;
import com.gemserk.games.superflyingthing.components.Components.MovementComponent;
import com.gemserk.games.superflyingthing.components.Components.ParticleEmitterComponent;
import com.gemserk.games.superflyingthing.components.Components.ShapeComponent;
import com.gemserk.games.superflyingthing.components.Components.TargetComponent;
import com.gemserk.games.superflyingthing.components.Replay;
import com.gemserk.games.superflyingthing.components.TagComponent;
import com.gemserk.games.superflyingthing.scripts.Behaviors;
import com.gemserk.games.superflyingthing.scripts.LaserBulletScript;
import com.gemserk.games.superflyingthing.scripts.LaserGunScript;
import com.gemserk.games.superflyingthing.scripts.MovingObstacleScript;
import com.gemserk.games.superflyingthing.scripts.ParticleEmitterSpawnerScript;
import com.gemserk.games.superflyingthing.scripts.PortalScript;
import com.gemserk.games.superflyingthing.scripts.ReplayPlayerScript;
import com.gemserk.games.superflyingthing.scripts.Scripts;
import com.gemserk.games.superflyingthing.scripts.Scripts.ShipScript;
import com.gemserk.games.superflyingthing.scripts.TimerScript;
import com.gemserk.resources.ResourceManager;

public class EntityTemplates {

	public static class CategoryBits {

		public static short AllCategoryBits = 0xFF;
		public static short ShipCategoryBits = 1;
		public static short MiniPlanetCategoryBits = 2;
		public static short MovingObstacleCategoryBits = 4;
		public static short ObstacleCategoryBits = 8;

	}

	private final BodyBuilder bodyBuilder;
	private final ResourceManager<String> resourceManager;
	private final EntityBuilder entityBuilder;
	private final Mesh2dBuilder mesh2dBuilder;
	private final World physicsWorld;
	private final EntityFactory entityFactory;

	private final Parameters parameters = new ParametersWrapper();
	private final EventManager eventManager;

	public EntityTemplate userMessageTemplate;
	
	public EntityTemplate getAttachedShipTemplate() {
		return attachedShipTemplate;
	}

	public EntityTemplate getLaserBulletTemplate() {
		return laserBulletTemplate;
	}

	public EntityTemplate getShipTemplate() {
		return shipTemplate;
	}

	public EntityTemplate getParticleEmitterTemplate() {
		return particleEmitterTemplate;
	}

	public EntityTemplate getDeadShipTemplate() {
		return deadShipTemplate;
	}

	public EntityTemplate getLaserGunTemplate() {
		return laserGunTemplate;
	}

	public EntityTemplate getPortalTemplate() {
		return portalTemplate;
	}

	public EntityTemplate getPlanetFillAnimationTemplate() {
		return planetFillAnimationTemplate;
	}

	public EntityTemplate getCameraTemplate() {
		return cameraTemplate;
	}

	public EntityTemplate getReplayShipTemplate() {
		return replayShipTemplate;
	}

	public EntityTemplate getParticleEmitterSpawnerTemplate() {
		return particleEmitterSpawnerTemplate;
	}
	
	public EntityTemplate getTimerTemplate() {
		return timerTemplate;
	}

	public EntityTemplates(World physicsWorld, com.artemis.World world, ResourceManager<String> resourceManager, EntityBuilder entityBuilder, EntityFactory EntityFactory, EventManager eventManager) {
		this.physicsWorld = physicsWorld;
		this.resourceManager = resourceManager;
		this.entityBuilder = entityBuilder;
		this.entityFactory = EntityFactory;
		this.eventManager = eventManager;
		this.bodyBuilder = new BodyBuilder(physicsWorld);
		this.mesh2dBuilder = new Mesh2dBuilder();
	}

	public Entity staticSprite(Sprite sprite, float x, float y, float width, float height, float angle, int layer, float centerx, float centery, Color color) {
		return entityBuilder //
				.component(new SpatialComponent(new SpatialImpl(x, y, width, height, angle))) //
				.component(new SpriteComponent(sprite, new Vector2(centerx, centery), new Color(color))) //
				.component(new RenderableComponent(layer)) //
				.build();
	}

	private EntityTemplate cameraTemplate = new EntityTemplateImpl() {
		@Override
		public void apply(Entity entity) {

			Camera camera = parameters.get("camera");
			Libgdx2dCamera libgdxCamera = parameters.get("libgdxCamera");
			Script script = parameters.get("script");
			Spatial spatial = parameters.get("spatial");
			Entity target = parameters.get("target");

			entity.addComponent(new TagComponent(Groups.MainCamera));
			entity.addComponent(new TargetComponent(target));
			entity.addComponent(new Components.CameraComponent(camera, libgdxCamera));
			entity.addComponent(new SpatialComponent(spatial));
			entity.addComponent(new ScriptComponent(script));

		}
	};

	private EntityTemplate particleEmitterTemplate = new EntityTemplateImpl() {

		{
			// used to transform the emitter and particles to the world coordinates space
			parameters.put("scale", new Float(0.02f));
			parameters.put("position", new Vector2(0f, 0f));
		}

		@Override
		public void apply(Entity entity) {
			Vector2 position = parameters.get("position");
			Float scale = parameters.get("scale");
			String emitter = parameters.get("emitter");
			Script script = parameters.get("script", new Scripts.ParticleEmitterScript());

			ParticleEmitter particleEmitter = resourceManager.getResourceValue(emitter);
			particleEmitter.start();
			ParticleEmitterUtils.scaleEmitter(particleEmitter, scale);

			entity.addComponent(new SpatialComponent(new SpatialImpl(position.x, position.y, 1f, 1f, 0f)));
			entity.addComponent(new ParticleEmitterComponent(particleEmitter));
			entity.addComponent(new ScriptComponent(script));
			entity.addComponent(new RenderableComponent(150));
		}

	};

	private EntityTemplate shipTemplate = new EntityTemplateImpl() {

		private final Vector2 direction = new Vector2();

		{
			parameters.put("maxLinearSpeed", new Float(3.5f));
			parameters.put("maxAngularVelocity", new Float(360f));
		}

		@Override
		public void apply(Entity e) {
			Animation rotationAnimation = resourceManager.getResourceValue("ShipAnimation");

			Spatial spatial = parameters.get("spatial");

			Float maxLinearSpeed = parameters.get("maxLinearSpeed");
			Float maxAngularVelocity = parameters.get("maxAngularVelocity");

			float angle = spatial.getAngle();
			float width = spatial.getWidth();
			float height = spatial.getHeight();

			direction.set(1f, 0f).rotate(angle);

			ShipController controller = parameters.get("controller");
			Script script = parameters.get("script", new ShipScript());

			Body body = bodyBuilder //
					.fixture(bodyBuilder.fixtureDefBuilder() //
							.restitution(0f) //
							.categoryBits(CategoryBits.ShipCategoryBits) //
							.maskBits((short) (CategoryBits.AllCategoryBits & ~CategoryBits.MiniPlanetCategoryBits)) //
							.boxShape(width * 0.25f, height * 0.1f))//
					.mass(50f) //
					.position(spatial.getX(), spatial.getY()) //
					.type(BodyType.DynamicBody) //
					.userData(e) //
					.build();

			e.addComponent(new TagComponent(Groups.ship));
			e.addComponent(new PhysicsComponent(new PhysicsImpl(body)));
			e.addComponent(new SpatialComponent(new SpatialPhysicsImpl(body, width, height)));
			e.addComponent(new SpriteComponent(rotationAnimation.getCurrentFrame()));
			e.addComponent(new RenderableComponent(1));
			e.addComponent(new MovementComponent(direction.x, direction.y, maxLinearSpeed, maxAngularVelocity));
			e.addComponent(new AttachableComponent());
			e.addComponent(new ControllerComponent(controller));
			e.addComponent(new ScriptComponent(script));
			e.addComponent(new AnimationComponent(new Animation[] { rotationAnimation }));
			e.addComponent(new ContainerComponent());
			e.addComponent(new Components.HealthComponent(new Container(100f, 100f)));
		}
	};

	private EntityTemplate attachedShipTemplate = new EntityTemplateImpl() {

		{
			parameters.put("maxLinearSpeed", new Float(3.5f));
			parameters.put("maxAngularVelocity", new Float(360f));
		}

		@Override
		public void apply(Entity e) {
			float width = 0.8f;
			float height = 0.8f;

			Animation rotationAnimation = resourceManager.getResourceValue("ShipAnimation");

			Vector2 position = parameters.get("position");

			Float maxLinearSpeed = parameters.get("maxLinearSpeed");
			Float maxAngularVelocity = parameters.get("maxAngularVelocity");

			Body body = bodyBuilder //
					.fixture(bodyBuilder.fixtureDefBuilder() //
							.restitution(0f) //
							.categoryBits(CategoryBits.ShipCategoryBits) //
							.maskBits((short) 0) //
							.boxShape(width * 0.125f, height * 0.125f)) //
					.mass(50f) //
					.position(position.x, position.y) //
					.type(BodyType.DynamicBody) //
					.userData(e) //
					.build();

			e.addComponent(new PhysicsComponent(new PhysicsImpl(body)));
			e.addComponent(new SpatialComponent(new SpatialPhysicsImpl(body, width, height)));
			e.addComponent(new SpriteComponent(rotationAnimation.getCurrentFrame()));
			e.addComponent(new RenderableComponent(1));
			e.addComponent(new MovementComponent(1f, 0f, maxLinearSpeed, maxAngularVelocity));
			e.addComponent(new AttachableComponent());
			e.addComponent(new ScriptComponent(new Scripts.AttachedShipScript()));
			e.addComponent(new AnimationComponent(new Animation[] { rotationAnimation }));
		}
	};

	private EntityTemplate deadShipTemplate = new EntityTemplateImpl() {

		@Override
		public void apply(Entity entity) {
			Spatial spatial = parameters.get("spatial");
			Sprite sprite = parameters.get("sprite");

			SpriteComponent spriteComponent = new SpriteComponent(sprite, Colors.semiBlack);
			spriteComponent.setUpdateRotation(false);

			entity.addComponent(new SpatialComponent(spatial));
			entity.addComponent(spriteComponent);
			entity.addComponent(new RenderableComponent(1));
		}
	};

	private EntityTemplate replayShipTemplate = new EntityTemplateImpl() {

		@Override
		public void apply(Entity e) {
			Animation rotationAnimation = resourceManager.getResourceValue("ShipAnimation");

			Replay replay = parameters.get("replay");

			e.setGroup(Groups.ReplayShipGroup);

			e.addComponent(new SpatialComponent(new SpatialImpl(0f, 0f, 0.8f, 0.8f, 0f)));
			e.addComponent(new AnimationComponent(new Animation[] { rotationAnimation }));
			e.addComponent(new SpriteComponent(rotationAnimation.getCurrentFrame(), new Color(0.5f, 0.5f, 0.5f, 1f)));
			e.addComponent(new RenderableComponent(0));
			e.addComponent(new ScriptComponent( //
					new ReplayPlayerScript(replay, eventManager), //
					new Behaviors.UpdateSpriteFromAnimationScript() //
			));

		}
	};

	private EntityTemplate laserBulletTemplate = new EntityTemplateImpl() {

		{
			parameters.put("x", new Float(0f));
			parameters.put("y", new Float(0f));
			parameters.put("angle", new Float(0f));
			parameters.put("damage", new Float(1f));
			parameters.put("color", Colors.lightBlue);
		}

		@Override
		public void apply(Entity entity) {
			Sprite sprite = parameters.get("sprite", (Sprite) resourceManager.getResourceValue("LaserSprite"));
			Script script = parameters.get("script", new LaserBulletScript(physicsWorld, entityFactory, getParticleEmitterTemplate()));
			Integer duration = parameters.get("duration", 1000);
			Color color = parameters.get("color");

			Float x = parameters.get("x");
			Float y = parameters.get("y");
			Float angle = parameters.get("angle");
			Float damage = parameters.get("damage");

			Entity owner = parameters.get("owner");

			Spatial ownerSpatial = ComponentWrapper.getSpatial(owner);
			SpatialHierarchicalImpl bulletSpatial = new SpatialHierarchicalImpl(ownerSpatial, 1f, 0.1f);

			bulletSpatial.setPosition(x + ownerSpatial.getX(), y + ownerSpatial.getY());
			bulletSpatial.setAngle(angle + ownerSpatial.getAngle());

			entity.addComponent(new SpatialComponent(bulletSpatial));
			entity.addComponent(new ScriptComponent(script));
			entity.addComponent(new SpriteComponent(sprite, new Vector2(0f, 0.5f), color));
			entity.addComponent(new RenderableComponent(5));
			entity.addComponent(new Components.TimerComponent((float) duration * 0.001f));
			entity.addComponent(new Components.DamageComponent(damage));
			entity.addComponent(new OwnerComponent(owner));
		}

	};

	private EntityTemplate laserGunTemplate = new EntityTemplateImpl() {

		{
			parameters.put("position", new Vector2());
			parameters.put("angle", new Float(0f));
			parameters.put("fireRate", new Integer(1000));
			parameters.put("bulletDuration", new Integer(250));
			parameters.put("currentReloadTime", new Integer(0));
			parameters.put("color", Colors.lightGreen);
		}

		@Override
		public void apply(Entity entity) {
			Animation idleAnimation = resourceManager.getResourceValue("LaserGunAnimation");

			Vector2 position = parameters.get("position");
			Float angle = parameters.get("angle");
			Script script = parameters.get("script", new LaserGunScript(entityFactory));

			Integer fireRate = parameters.get("fireRate");
			Integer bulletDuration = parameters.get("bulletDuration");
			Integer currentReloadTime = parameters.get("currentReloadTime");

			Color color = parameters.get("color");

			Entity owner = parameters.get("owner");

			entity.addComponent(new SpatialComponent(new SpatialImpl(position.x, position.y, 1f, 1f, angle)));
			entity.addComponent(new ScriptComponent(script));
			entity.addComponent(new AnimationComponent(new Animation[] { idleAnimation }));
			entity.addComponent(new SpriteComponent(idleAnimation.getCurrentFrame(), color));
			entity.addComponent(new RenderableComponent(4));
			entity.addComponent(new Components.WeaponComponent(fireRate, bulletDuration, currentReloadTime, laserBulletTemplate));
			entity.addComponent(new OwnerComponent(owner));
			entity.addComponent(new ContainerComponent());
		}

	};

	private EntityTemplate portalTemplate = new EntityTemplateImpl() {

		{
			parameters.put("sprite", "PortalSprite");
		}

		@Override
		public void apply(Entity entity) {

			String id = parameters.get("id");
			String targetPortalId = parameters.get("targetPortalId");
			String spriteId = parameters.get("sprite");
			Spatial spatial = parameters.get("spatial");
			Script script = parameters.get("script", new PortalScript());

			Sprite sprite = resourceManager.getResourceValue(spriteId);

			entity.addComponent(new TagComponent(id));
			entity.addComponent(new SpriteComponent(sprite, Colors.darkBlue));
			entity.addComponent(new Components.PortalComponent(targetPortalId, spatial.getAngle()));
			entity.addComponent(new RenderableComponent(-5));
			entity.addComponent(new SpatialComponent(spatial));
			entity.addComponent(new ScriptComponent(script));

			Body body = bodyBuilder //
					.fixture(bodyBuilder.fixtureDefBuilder() //
							.circleShape(spatial.getWidth() * 0.35f) //
							.categoryBits(CategoryBits.ObstacleCategoryBits) //
							.maskBits((short) (CategoryBits.AllCategoryBits & ~CategoryBits.ObstacleCategoryBits)) //
							.sensor()) //
					.position(spatial.getX(), spatial.getY()) //
					.mass(1f) //
					.type(BodyType.StaticBody) //
					.userData(entity) //
					.build();

			entity.addComponent(new PhysicsComponent(new PhysicsImpl(body)));

		}

	};

	public Entity star(float x, float y, Script script) {
		Entity e = entityBuilder.build();

		float radius = 0.3f;

		Animation rotateAnimation = resourceManager.getResourceValue("StarAnimation");

		Body body = bodyBuilder //
				.fixture(bodyBuilder.fixtureDefBuilder() //
						.sensor() //
						.restitution(0f) //
						.circleShape(radius)) //
				.mass(50f) //
				.position(x, y) //
				.type(BodyType.DynamicBody) //
				.userData(e) //
				.build();

		e.addComponent(new PhysicsComponent(new PhysicsImpl(body)));
		e.addComponent(new SpatialComponent(new SpatialPhysicsImpl(body, radius * 2, radius * 2)));

		e.addComponent(new SpriteComponent(rotateAnimation.getCurrentFrame()));
		e.addComponent(new RenderableComponent(3));
		e.addComponent(new GrabbableComponent());
		e.addComponent(new AnimationComponent(new Animation[] { rotateAnimation }));
		e.addComponent(new ScriptComponent(script));

		e.refresh();
		return e;
	}

	private EntityTemplate planetFillAnimationTemplate = new EntityTemplateImpl() {

		private Color[] planetColors = new Color[] { Color.BLUE, Color.RED, Colors.darkGreen, Colors.darkMagenta, Colors.darkYellow };

		{
			parameters.put("animation", "PlanetFillAnimation");
			// parameters.put("color", Colors.yellow);
		}

		@Override
		public void apply(Entity entity) {
			Entity owner = parameters.get("owner");

			String animationId = parameters.get("animation");
			Color color = parameters.get("color");

			if (color == null)
				color = planetColors[MathUtils.random(planetColors.length - 1)];
			// color = new Color(MathUtils.random(0.3f, 1f), //
			// MathUtils.random(0.3f, 1f), //
			// MathUtils.random(0.3f, 1f), //
			// 1f);

			Animation planetFillAnimation = resourceManager.getResourceValue(animationId);
			Sprite sprite = planetFillAnimation.getCurrentFrame();

			Spatial ownerSpatial = ComponentWrapper.getSpatial(owner);

			entity.addComponent(new SpatialComponent(new SpatialHierarchicalImpl(ownerSpatial)));
			entity.addComponent(new SpriteComponent(sprite, color));
			entity.addComponent(new RenderableComponent(-1));
			entity.addComponent(new AnimationComponent(new Animation[] { planetFillAnimation }));
			entity.addComponent(new OwnerComponent(owner));
			entity.addComponent(new ScriptComponent(new Scripts.UpdateAnimationScript()));
		}

	};

	public Entity startPlanet(float x, float y, float radius, ShipController controller, Script script) {

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

		e.addComponent(new TagComponent(Groups.startPlanet));
		e.addComponent(new PhysicsComponent(new PhysicsImpl(body)));
		e.addComponent(new SpatialComponent(new SpatialPhysicsImpl(body, radius * 2, radius * 2)));
		e.addComponent(new AttachmentComponent());
		e.addComponent(new SpriteComponent(sprite, Color.WHITE));
		e.addComponent(new RenderableComponent(-2));
		e.addComponent(new ControllerComponent(controller));
		e.addComponent(new ScriptComponent(script));
		e.addComponent(new ContainerComponent());

		e.refresh();

		parameters.clear();
		parameters.put("owner", e);
		entityFactory.instantiate(getPlanetFillAnimationTemplate(), parameters);

		return e;
	}

	public Entity destinationPlanet(float x, float y, float radius, Script script) {
		Sprite sprite = resourceManager.getResourceValue("Planet");
		Entity e = entityBuilder.build();

		e.setGroup(Groups.destinationPlanets);

		Body body = bodyBuilder //
				.fixture(bodyBuilder.fixtureDefBuilder() //
						.circleShape(radius * 0.1f) //
						.categoryBits(CategoryBits.MiniPlanetCategoryBits) //
						.restitution(0f)) //
				.fixture(bodyBuilder.fixtureDefBuilder() //
						.circleShape(radius * 1.5f) //
						.categoryBits(CategoryBits.AllCategoryBits) //
						.sensor()) //
				.position(x, y) //
				.mass(1f) //
				.type(BodyType.StaticBody) //
				.userData(e) //
				.build();

		e.addComponent(new PhysicsComponent(new PhysicsImpl(body)));
		e.addComponent(new SpatialComponent(new SpatialPhysicsImpl(body, radius * 2, radius * 2)));
		e.addComponent(new SpriteComponent(sprite, Color.WHITE));
		e.addComponent(new RenderableComponent(-2));
		e.addComponent(new AttachmentComponent());
		e.addComponent(new ScriptComponent(script));
		e.addComponent(new ContainerComponent());

		e.refresh();
		return e;
	}

	public Entity obstacle(Vector2[] vertices, float x, float y, float angle) {
		Entity e = entityBuilder.build();

		Texture obstacleTexture = resourceManager.getResourceValue("ObstacleTexture");
		obstacleTexture.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);

		Triangulator triangulator = ShapeUtils.triangulate(vertices);

		FixtureDef[] fixtureDefs = new FixtureDef[triangulator.getTriangleCount()];
		FixtureDefBuilder fixtureDefBuilder = new FixtureDefBuilder();

		for (int i = 0; i < triangulator.getTriangleCount(); i++) {
			Vector2[] v = new Vector2[3];
			for (int p = 0; p < 3; p++) {
				float[] pt = triangulator.getTrianglePoint(i, p);
				v[p] = new Vector2(pt[0], pt[1]);

				mesh2dBuilder.color(0.85f, 0.7f, 0.5f, 1f);
				mesh2dBuilder.texCoord(pt[0] * 0.25f, pt[1] * 0.25f);
				// mesh2dBuilder.texCoord(pt[0], pt[1]);
				mesh2dBuilder.vertex(pt[0], pt[1]);

			}
			fixtureDefs[i] = fixtureDefBuilder //
					.polygonShape(v) //
					.restitution(0f) //
					.categoryBits(CategoryBits.ObstacleCategoryBits) //
					.maskBits((short) (CategoryBits.AllCategoryBits & ~CategoryBits.ObstacleCategoryBits)) //
					.build();
		}

		Body body = bodyBuilder.mass(1f) //
				.fixtures(fixtureDefs) //
				.position(x, y) //
				.type(BodyType.StaticBody) //
				.angle(angle) //
				.userData(e) //
				.build();

		e.addComponent(new PhysicsComponent(new PhysicsImpl(body)));
		e.addComponent(new SpatialComponent(new SpatialPhysicsImpl(body, 1f, 1f)));
		e.addComponent(new ShapeComponent(mesh2dBuilder.build(), obstacleTexture));
		e.addComponent(new RenderableComponent(-60));
		e.addComponent(new Components.DamageComponent(3000f));

		e.refresh();
		return e;
	}

	public Entity movingObstacle(Vector2[] vertices, final Vector2[] points, int startPoint, float x, float y, float angle) {
		Entity e = entityBuilder.build();

		Triangulator triangulator = ShapeUtils.triangulate(vertices);

		FixtureDef[] fixtureDefs = new FixtureDef[triangulator.getTriangleCount()];
		FixtureDefBuilder fixtureDefBuilder = new FixtureDefBuilder();

		for (int i = 0; i < triangulator.getTriangleCount(); i++) {
			Vector2[] v = new Vector2[3];
			for (int p = 0; p < 3; p++) {
				float[] pt = triangulator.getTrianglePoint(i, p);
				v[p] = new Vector2(pt[0], pt[1]);

				mesh2dBuilder.color(1f, 0f, 0f, 1f);
				// mesh2dBuilder.texCoord(pt[0] * 0.5f, pt[1] * 0.5f);
				mesh2dBuilder.vertex(pt[0], pt[1]);
			}
			fixtureDefs[i] = fixtureDefBuilder //
					.polygonShape(v) //
					.restitution(0f) //
					.categoryBits(CategoryBits.MovingObstacleCategoryBits) //
					.maskBits((short) (CategoryBits.AllCategoryBits & ~CategoryBits.ObstacleCategoryBits & ~CategoryBits.MovingObstacleCategoryBits)) //
					.build();
		}

		Body body = bodyBuilder //
				.mass(500f) //
				.inertia(1f) //
				.fixtures(fixtureDefs) //
				.position(x, y) //
				.type(BodyType.DynamicBody) //
				.angle(angle) //
				.userData(e) //
				.build();

		e.addComponent(new PhysicsComponent(new PhysicsImpl(body)));
		e.addComponent(new SpatialComponent(new SpatialPhysicsImpl(body, 1f, 1f)));
		e.addComponent(new ShapeComponent(mesh2dBuilder.build()));
		e.addComponent(new RenderableComponent(-59));
		e.addComponent(new Components.DamageComponent(6000f));
		e.addComponent(new ScriptComponent(new MovingObstacleScript(points, startPoint)));

		e.refresh();
		return e;
	}

	public Entity boxObstacle(float x, float y, float w, float h, float angle) {
		return obstacle(new Vector2[] { //
				new Vector2(w * 0.5f, h * 0.5f),//
						new Vector2(w * 0.5f, -h * 0.5f), //
						new Vector2(-w * 0.5f, -h * 0.5f),//
						new Vector2(-w * 0.5f, h * 0.5f), }, x, y, angle);
	}

	private EntityTemplate particleEmitterSpawnerTemplate = new EntityTemplateImpl() {
		@Override
		public void apply(Entity entity) {
			entity.addComponent(new ScriptComponent(new ParticleEmitterSpawnerScript(entityFactory, getParticleEmitterTemplate())));
		}
	};
	
	private EntityTemplate timerTemplate = new EntityTemplateImpl() {
		
		{
			parameters.put("time", new Float(0f));
		}
		
		@Override
		public void apply(Entity entity) {
			Float time = parameters.get("time");
			String eventId = parameters.get("eventId");
			
			entity.addComponent(new Components.TimerComponent(time));
			entity.addComponent(new ScriptComponent(new TimerScript(eventManager, eventId)));
		}
	};

}