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
import com.gemserk.animation4j.interpolator.FloatInterpolator;
import com.gemserk.commons.artemis.EntityBuilder;
import com.gemserk.commons.artemis.Script;
import com.gemserk.commons.artemis.ScriptJavaImpl;
import com.gemserk.commons.artemis.components.PhysicsComponent;
import com.gemserk.commons.artemis.components.RenderableComponent;
import com.gemserk.commons.artemis.components.ScriptComponent;
import com.gemserk.commons.artemis.components.SpatialComponent;
import com.gemserk.commons.artemis.components.SpriteComponent;
import com.gemserk.commons.gdx.box2d.BodyBuilder;
import com.gemserk.commons.gdx.box2d.FixtureDefBuilder;
import com.gemserk.commons.gdx.camera.Camera;
import com.gemserk.commons.gdx.camera.Libgdx2dCamera;
import com.gemserk.commons.gdx.games.PhysicsImpl;
import com.gemserk.commons.gdx.games.Spatial;
import com.gemserk.commons.gdx.games.SpatialImpl;
import com.gemserk.commons.gdx.games.SpatialPhysicsImpl;
import com.gemserk.commons.gdx.graphics.Mesh2dBuilder;
import com.gemserk.commons.gdx.graphics.ParticleEmitterUtils;
import com.gemserk.commons.gdx.graphics.ShapeUtils;
import com.gemserk.commons.gdx.graphics.Triangulator;
import com.gemserk.games.superflyingthing.Colors;
import com.gemserk.games.superflyingthing.ShipController;
import com.gemserk.games.superflyingthing.components.ComponentWrapper;
import com.gemserk.games.superflyingthing.components.Components;
import com.gemserk.games.superflyingthing.components.Components.AliveComponent;
import com.gemserk.games.superflyingthing.components.Components.AnimationComponent;
import com.gemserk.games.superflyingthing.components.Components.AttachableComponent;
import com.gemserk.games.superflyingthing.components.Components.AttachmentComponent;
import com.gemserk.games.superflyingthing.components.Components.ControllerComponent;
import com.gemserk.games.superflyingthing.components.Components.GrabbableComponent;
import com.gemserk.games.superflyingthing.components.Components.MovementComponent;
import com.gemserk.games.superflyingthing.components.Components.ParticleEmitterComponent;
import com.gemserk.games.superflyingthing.components.Components.ShapeComponent;
import com.gemserk.games.superflyingthing.components.Components.ShipControllerComponent;
import com.gemserk.games.superflyingthing.components.Components.TargetComponent;
import com.gemserk.games.superflyingthing.scripts.Scripts;
import com.gemserk.games.superflyingthing.scripts.Scripts.MovingObstacleScript;
import com.gemserk.games.superflyingthing.scripts.Scripts.ShipScript;
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

	public EntityTemplates(World physicsWorld, com.artemis.World world, ResourceManager<String> resourceManager, EntityBuilder entityBuilder) {
		this.resourceManager = resourceManager;
		this.entityBuilder = entityBuilder;
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

	public Entity explosionEffect(float x, float y) {
		ParticleEmitter explosionEmitter = resourceManager.getResourceValue("ExplosionEmitter");
		explosionEmitter.start();
		ParticleEmitterUtils.scaleEmitter(explosionEmitter, 0.02f);
		return entityBuilder //
				.component(new SpatialComponent(new SpatialImpl(x, y, 1f, 1f, 0f))) //
				.component(new ParticleEmitterComponent(explosionEmitter)) //
				.component(new ScriptComponent(new ScriptJavaImpl() {
					@Override
					public void update(com.artemis.World world, Entity e) {
						ParticleEmitterComponent particleEmitterComponent = ComponentWrapper.getParticleEmitter(e);
						ParticleEmitter particleEmitter = particleEmitterComponent.getParticleEmitter();
						if (particleEmitter.isComplete())
							world.deleteEntity(e);
					}
				})) //
				.build();
	}

	public Entity camera(Camera camera, final Libgdx2dCamera libgdxCamera, final float x, final float y, Script script) {
		return entityBuilder //
				.component(new Components.CameraComponent(camera, libgdxCamera)) //
				.component(new TargetComponent(null)) //
				.component(new SpatialComponent(new SpatialImpl(x, y, 0f, 0f, 0f))) //
				.component(new ScriptComponent(script)) //
				.build();
	}

	public Entity ship(float x, float y, Vector2 direction, ShipController shipControllerImpl) {
		float width = 0.8f;
		float height = 0.8f;

		Animation rotationAnimation = resourceManager.getResourceValue("ShipAnimation");

		Entity e = entityBuilder.build();

		Body body = bodyBuilder //
				.fixture(bodyBuilder.fixtureDefBuilder() //
						.restitution(0f) //
						.categoryBits(CategoryBits.ShipCategoryBits) //
						.maskBits((short) (CategoryBits.AllCategoryBits & ~CategoryBits.MiniPlanetCategoryBits)) //
						.circleShape(width * 0.125f)) //
				.mass(50f) //
				.position(x, y) //
				.type(BodyType.DynamicBody) //
				.userData(e) //
				.build();

		e.addComponent(new PhysicsComponent(new PhysicsImpl(body)));
		e.addComponent(new SpatialComponent(new SpatialPhysicsImpl(body, width, height)));
		e.addComponent(new SpriteComponent(rotationAnimation.getCurrentFrame()));
		e.addComponent(new RenderableComponent(1));
		e.addComponent(new MovementComponent(direction.x, direction.y));
		e.addComponent(new AliveComponent(false));
		e.addComponent(new AttachableComponent());
		e.addComponent(new ShipControllerComponent());
		e.addComponent(new ControllerComponent(shipControllerImpl));
		e.addComponent(new ScriptComponent(new ShipScript()));
		e.addComponent(new AnimationComponent(new Animation[] { rotationAnimation }));

		e.refresh();
		return e;
	}

	public Entity attachedShip(float x, float y, Vector2 direction) {
		float width = 0.8f;
		float height = 0.8f;

		Animation rotationAnimation = resourceManager.getResourceValue("ShipAnimation");

		Entity e = entityBuilder.build();

		Body body = bodyBuilder //
				.fixture(bodyBuilder.fixtureDefBuilder() //
						.restitution(0f) //
						.categoryBits(CategoryBits.ShipCategoryBits) //
						.maskBits((short) 0) //
						.boxShape(width * 0.125f, height * 0.125f)) //
				.mass(50f) //
				.position(x, y) //
				.type(BodyType.DynamicBody) //
				.userData(e) //
				.build();

		e.addComponent(new PhysicsComponent(new PhysicsImpl(body)));
		e.addComponent(new SpatialComponent(new SpatialPhysicsImpl(body, width, height)));
		e.addComponent(new SpriteComponent(rotationAnimation.getCurrentFrame()));
		e.addComponent(new RenderableComponent(1));
		e.addComponent(new MovementComponent(direction.x, direction.y));
		e.addComponent(new AttachableComponent());
		e.addComponent(new ScriptComponent(new Scripts.AttachedShipScript()));
		e.addComponent(new AnimationComponent(new Animation[] { rotationAnimation }));

		e.refresh();
		return e;
	}

	public Entity thrustParticle(float x, float y) {
		Sprite sprite = resourceManager.getResourceValue("ThrustSprite");
		return entityBuilder //
				.component(new SpatialComponent(new SpatialImpl(x, y, 0.2f, 0.2f, 0f))) //
				.component(new SpriteComponent(sprite)) //
				.component(new RenderableComponent(-1)).component(new ScriptComponent(new ScriptJavaImpl() {

					float aliveTime = 100;

					@Override
					public void update(com.artemis.World world, Entity e) {
						aliveTime -= world.getDelta();

						if (aliveTime <= 0) {
							world.deleteEntity(e);
							return;
						}

						SpriteComponent spriteComponent = ComponentWrapper.getSpriteComponent(e);
						spriteComponent.getColor().a = FloatInterpolator.interpolate(0f, 1f, aliveTime / 100);

						Spatial spatial = ComponentWrapper.getSpatial(e);
						spatial.setSize(FloatInterpolator.interpolate(0.1f, 0.2f, aliveTime / 100), FloatInterpolator.interpolate(0.1f, 0.2f, aliveTime / 100));
					}
				})) //
				.build();
	}

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
		e.addComponent(new RenderableComponent(0));
		e.addComponent(new GrabbableComponent());
		e.addComponent(new AnimationComponent(new Animation[] { rotateAnimation }));
		e.addComponent(new ScriptComponent(script));

		e.refresh();
		return e;
	}

	public Entity deadShip(Spatial spatial, Sprite sprite) {
		return entityBuilder //
				.component(new SpatialComponent(new SpatialImpl(spatial))) //
				.component(new SpriteComponent(sprite, Colors.semiBlack)) //
				.component(new RenderableComponent(-1))//
				.build();
	}

	public Entity startPlanet(float x, float y, float radius, ShipController shipControllerImpl, Script script) {
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

		e.addComponent(new PhysicsComponent(new PhysicsImpl(body)));
		e.addComponent(new SpatialComponent(new SpatialPhysicsImpl(body, radius * 2, radius * 2)));
		e.addComponent(new AttachmentComponent());
		e.addComponent(new SpriteComponent(sprite, Color.WHITE));
		e.addComponent(new RenderableComponent(-2));
		e.addComponent(new ControllerComponent(shipControllerImpl));
		e.addComponent(new ScriptComponent(script));

		e.refresh();
		return e;
	}

	public Entity destinationPlanet(float x, float y, float radius, Script script) {
		Sprite sprite = resourceManager.getResourceValue("Planet");
		Entity e = entityBuilder.build();
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

				mesh2dBuilder.color(0.8f, 0.8f, 0.8f, 1f);
				mesh2dBuilder.texCoord(pt[0] * 0.5f, pt[1] * 0.5f);
				// mesh2dBuilder.texCoord(pt[0], pt[1]);
				mesh2dBuilder.vertex(pt[0], pt[1]);

			}
			fixtureDefs[i] = fixtureDefBuilder //
					.polygonShape(v) //
					.restitution(0f) //
					.categoryBits(CategoryBits.ObstacleCategoryBits) //
					.build();
		}

		Body body = bodyBuilder.mass(1f) //
				.fixtures(fixtureDefs) //
				.position(x, y) //
				.type(BodyType.StaticBody) //
				.angle(angle) //
				.build();

		e.addComponent(new PhysicsComponent(new PhysicsImpl(body)));
		e.addComponent(new SpatialComponent(new SpatialPhysicsImpl(body, 1f, 1f)));

		e.addComponent(new ShapeComponent(mesh2dBuilder.build(), obstacleTexture));
		e.addComponent(new RenderableComponent(-60));

		e.refresh();
		return e;
	}

	public Entity movingObstacle(Vector2[] vertices, final Vector2[] points, float x, float y, float angle) {
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
					.maskBits((short) (CategoryBits.AllCategoryBits & ~CategoryBits.ObstacleCategoryBits)) //
					.build();
		}

		Body body = bodyBuilder //
				.mass(500f) //
				.inertia(1f) //
				.fixtures(fixtureDefs) //
				.position(x, y) //
				.type(BodyType.DynamicBody) //
				.angle(angle) //
				.build();

		e.addComponent(new PhysicsComponent(new PhysicsImpl(body)));
		e.addComponent(new SpatialComponent(new SpatialPhysicsImpl(body, 1f, 1f)));
		e.addComponent(new ShapeComponent(mesh2dBuilder.build()));
		e.addComponent(new RenderableComponent(-59));
		e.addComponent(new ScriptComponent(new MovingObstacleScript(points)));

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

	public Entity laserTurret(float x, float y, float angle, Script script) {
		Animation idleAnimation = resourceManager.getResourceValue("LaserTurretAnimation");
		return entityBuilder //
				.component(new SpatialComponent(new SpatialImpl(x, y, 1f, 1f, angle))) //
				.component(new ScriptComponent(script)) //
				.component(new AnimationComponent(new Animation[] { idleAnimation })) //
				.component(new SpriteComponent(idleAnimation.getCurrentFrame(), Color.WHITE)) //
				.component(new RenderableComponent(3))//
				.build();
	}

	public Entity laser(float x, float y, float length, float angle, Script script) {
		Sprite sprite = resourceManager.getResourceValue("LaserSprite");

		Entity e = entityBuilder //
				.component(new SpatialComponent(new SpatialImpl(x, y, length, 0.1f, angle))) //
				.component(new ScriptComponent(script)) //
				.component(new SpriteComponent(sprite, new Vector2(0f, 0.5f), Colors.lightBlue)) //
				.component(new RenderableComponent(2)) //
				.build();

		Vector2[] vertices = new Vector2[] { new Vector2(0f, 0f), new Vector2(length, 0f) };

		Body body = bodyBuilder //
				.fixture(bodyBuilder.fixtureDefBuilder() //
						.polygonShape(vertices) //
						.categoryBits(CategoryBits.AllCategoryBits) //
						.sensor()) //
				.position(x, y) //
				.angle(angle * MathUtils.degreesToRadians) //
				.mass(1f) //
				.type(BodyType.StaticBody) //
				.userData(e) //
				.build();

		e.addComponent(new PhysicsComponent(new PhysicsImpl(body)));
		e.refresh();

		return e;
	}

	public Entity portal(String id, String targetPortalId, float x, float y, Script script) {
		Sprite sprite = resourceManager.getResourceValue("PortalSprite");

		Entity e = entityBuilder //
				.tag(id) //
				.component(new SpriteComponent(sprite)) //
				.component(new Components.PortalComponent(targetPortalId)) //
				.component(new RenderableComponent(4)) //
				.component(new SpatialComponent(new SpatialImpl(x, y, 1.5f, 1.5f, 0f))) //
				.component(new ScriptComponent(script)) //
				.build();

		Body body = bodyBuilder //
				.fixture(bodyBuilder.fixtureDefBuilder() //
						.circleShape(0.5f) //
						.categoryBits(CategoryBits.AllCategoryBits) //
						.sensor()) //
				.position(x, y) //
				.mass(1f) //
				.type(BodyType.StaticBody) //
				.userData(e) //
				.build();

		e.addComponent(new PhysicsComponent(new PhysicsImpl(body)));
		e.refresh();

		return e;
	}

}