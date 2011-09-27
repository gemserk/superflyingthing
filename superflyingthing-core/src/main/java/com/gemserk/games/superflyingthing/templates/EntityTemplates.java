package com.gemserk.games.superflyingthing.templates;

import com.artemis.Entity;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.gemserk.commons.artemis.EntityBuilder;
import com.gemserk.commons.artemis.components.PhysicsComponent;
import com.gemserk.commons.artemis.components.RenderableComponent;
import com.gemserk.commons.artemis.components.ScriptComponent;
import com.gemserk.commons.artemis.components.SpatialComponent;
import com.gemserk.commons.artemis.components.TimerComponent;
import com.gemserk.commons.artemis.events.EventManager;
import com.gemserk.commons.artemis.scripts.Script;
import com.gemserk.commons.artemis.templates.EntityFactory;
import com.gemserk.commons.artemis.templates.EntityTemplate;
import com.gemserk.commons.artemis.templates.EntityTemplateImpl;
import com.gemserk.commons.gdx.box2d.BodyBuilder;
import com.gemserk.commons.gdx.box2d.FixtureDefBuilder;
import com.gemserk.commons.gdx.box2d.JointBuilder;
import com.gemserk.commons.gdx.games.PhysicsImpl;
import com.gemserk.commons.gdx.games.SpatialImpl;
import com.gemserk.commons.gdx.games.SpatialPhysicsImpl;
import com.gemserk.commons.gdx.graphics.Mesh2dBuilder;
import com.gemserk.commons.gdx.graphics.ParticleEmitterUtils;
import com.gemserk.commons.gdx.graphics.ShapeUtils;
import com.gemserk.commons.gdx.graphics.Triangulator;
import com.gemserk.commons.reflection.ObjectConfigurator;
import com.gemserk.commons.reflection.ProviderImpl;
import com.gemserk.games.superflyingthing.components.Components;
import com.gemserk.games.superflyingthing.components.Components.LabelComponent;
import com.gemserk.games.superflyingthing.components.Components.ParticleEmitterComponent;
import com.gemserk.games.superflyingthing.components.Components.ShapeComponent;
import com.gemserk.games.superflyingthing.components.Replay;
import com.gemserk.games.superflyingthing.scripts.MovingObstacleScript;
import com.gemserk.games.superflyingthing.scripts.ParticleEmitterSpawnerScript;
import com.gemserk.games.superflyingthing.scripts.ReplayPlayerScript;
import com.gemserk.games.superflyingthing.scripts.Scripts;
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
	private final EntityFactory entityFactory;

	private final EventManager eventManager;

	public EntityTemplate userMessageTemplate;

	public EntityTemplate getAttachedShipTemplate() {
		return attachedShipTemplate;
	}

	public EntityTemplate getShipTemplate() {
		return shipTemplate;
	}

	public EntityTemplate getParticleEmitterTemplate() {
		return particleEmitterTemplate;
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

	public EntityTemplate getReplayPlayerTemplate() {
		return replayPlayerTemplate;
	}

	public EntityTemplate getStaticSpriteTemplate() {
		return staticSpriteTemplate;
	}

	public EntityTemplates(final World physicsWorld, com.artemis.World world, final ResourceManager<String> resourceManager, final EntityBuilder entityBuilder, final EntityFactory entityFactory, final EventManager eventManager) {
		this.resourceManager = resourceManager;
		this.entityBuilder = entityBuilder;
		this.entityFactory = entityFactory;
		this.eventManager = eventManager;
		this.bodyBuilder = new BodyBuilder(physicsWorld);
		this.mesh2dBuilder = new Mesh2dBuilder();
		this.jointBuilder = new JointBuilder(physicsWorld);

		ProviderImpl templateProvider = new ProviderImpl(new ObjectConfigurator() {
			{
				add("physicsWorld", physicsWorld);
				add("resourceManager", resourceManager);
				add("entityBuilder", entityBuilder);
				add("entityFactory", entityFactory);
				add("eventManager", eventManager);
				add("bodyBuilder", bodyBuilder);
				add("mesh2dBuilder", mesh2dBuilder);
				add("jointBuilder", jointBuilder);
				add("entityTemplates", EntityTemplates.this);
			}
		});

		this.cameraTemplate = templateProvider.get(CameraTemplate.class);
		this.staticSpriteTemplate = templateProvider.get(StaticSpriteTemplate.class);
		this.starTemplate = templateProvider.get(StarTemplate.class);
		this.startPlanetTemplate = templateProvider.get(StartPlanetTemplate.class);
		this.destinationPlanetTemplate = templateProvider.get(DestinationPlanetTemplate.class);
		this.planetFillAnimationTemplate = templateProvider.get(PlanetFillAnimationTemplate.class);
		this.portalTemplate = templateProvider.get(PortalTemplate.class);
		this.replayShipTemplate = templateProvider.get(ReplayShipTemplate.class);
		this.laserBulletTemplate = templateProvider.get(LaserBulletTemplate.class);
		this.laserGunTemplate = templateProvider.get(LaserGunTemplate.class);
		this.attachedShipTemplate = templateProvider.get(AttachedShipTemplate.class);
		this.shipTemplate = templateProvider.get(ShipTemplate.class);
		
	}

	public EntityTemplate cameraTemplate;
	public EntityTemplate staticSpriteTemplate;
	public EntityTemplate starTemplate;
	public EntityTemplate startPlanetTemplate;
	public EntityTemplate destinationPlanetTemplate;
	public EntityTemplate planetFillAnimationTemplate;
	public EntityTemplate portalTemplate;
	public EntityTemplate replayShipTemplate;
	
	public EntityTemplate laserBulletTemplate;
	public EntityTemplate laserGunTemplate;
	
	public EntityTemplate attachedShipTemplate;
	public EntityTemplate shipTemplate;

	public EntityTemplate particleEmitterTemplate = new EntityTemplateImpl() {

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

	private EntityTemplate replayPlayerTemplate = new EntityTemplateImpl() {

		@Override
		public void apply(Entity e) {
			Replay replay = parameters.get("replay");
			Entity target = parameters.get("target");
			e.setGroup(Groups.ReplayShipGroup);
			e.addComponent(new ScriptComponent(new ReplayPlayerScript(replay, eventManager, target)));

		}

	};

	public Entity obstacle(String id, Vector2[] vertices, float x, float y, float angle) {
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
				mesh2dBuilder.texCoord(x + pt[0], y + pt[1]);
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

		e.addComponent(new LabelComponent(id));
		e.addComponent(new PhysicsComponent(new PhysicsImpl(body)));
		e.addComponent(new SpatialComponent(new SpatialPhysicsImpl(body, 1f, 1f)));
		e.addComponent(new ShapeComponent(mesh2dBuilder.build(), obstacleTexture));
		e.addComponent(new RenderableComponent(-60));
		e.addComponent(new Components.DamageComponent(10000f));

		e.refresh();
		return e;
	}

	public Entity movingObstacle(String id, Vector2[] vertices, final Vector2[] points, int startPoint, float x, float y, float angle) {
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

	public Entity boxObstacle(String id, float x, float y, float w, float h, float angle) {
		return obstacle(id, new Vector2[] { //
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

			entity.addComponent(new TimerComponent(time));
			entity.addComponent(new ScriptComponent(new TimerScript(eventManager, eventId)));
		}
	};

	private JointBuilder jointBuilder;

}