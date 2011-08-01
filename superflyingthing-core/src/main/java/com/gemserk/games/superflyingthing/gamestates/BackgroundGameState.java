package com.gemserk.games.superflyingthing.gamestates;

import java.util.ArrayList;

import com.artemis.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.gemserk.animation4j.transitions.sync.Synchronizers;
import com.gemserk.commons.artemis.EntityBuilder;
import com.gemserk.commons.artemis.WorldWrapper;
import com.gemserk.commons.artemis.components.ScriptComponent;
import com.gemserk.commons.artemis.events.EventManager;
import com.gemserk.commons.artemis.events.EventManagerImpl;
import com.gemserk.commons.artemis.systems.PhysicsSystem;
import com.gemserk.commons.artemis.systems.RenderLayer;
import com.gemserk.commons.artemis.systems.RenderLayerSpriteBatchImpl;
import com.gemserk.commons.artemis.systems.RenderableSystem;
import com.gemserk.commons.artemis.systems.ScriptSystem;
import com.gemserk.commons.artemis.systems.SpriteUpdateSystem;
import com.gemserk.commons.gdx.GameStateImpl;
import com.gemserk.commons.gdx.box2d.Box2DCustomDebugRenderer;
import com.gemserk.commons.gdx.box2d.JointBuilder;
import com.gemserk.commons.gdx.camera.Camera;
import com.gemserk.commons.gdx.camera.CameraRestrictedImpl;
import com.gemserk.commons.gdx.camera.Libgdx2dCamera;
import com.gemserk.commons.gdx.camera.Libgdx2dCameraTransformImpl;
import com.gemserk.commons.gdx.games.Spatial;
import com.gemserk.componentsengine.utils.AngleUtils;
import com.gemserk.games.superflyingthing.Game;
import com.gemserk.games.superflyingthing.ShipController;
import com.gemserk.games.superflyingthing.components.ComponentWrapper;
import com.gemserk.games.superflyingthing.components.Components.AttachmentComponent;
import com.gemserk.games.superflyingthing.components.Components.GameData;
import com.gemserk.games.superflyingthing.components.Components.GameDataComponent;
import com.gemserk.games.superflyingthing.components.Components.MovementComponent;
import com.gemserk.games.superflyingthing.levels.Level;
import com.gemserk.games.superflyingthing.levels.Level.DestinationPlanet;
import com.gemserk.games.superflyingthing.levels.Level.LaserTurret;
import com.gemserk.games.superflyingthing.levels.Level.Obstacle;
import com.gemserk.games.superflyingthing.levels.Level.Portal;
import com.gemserk.games.superflyingthing.levels.Levels;
import com.gemserk.games.superflyingthing.scripts.Scripts;
import com.gemserk.games.superflyingthing.scripts.Scripts.CameraScript;
import com.gemserk.games.superflyingthing.scripts.Scripts.DestinationPlanetScript;
import com.gemserk.games.superflyingthing.scripts.Scripts.StarScript;
import com.gemserk.games.superflyingthing.scripts.Scripts.StartPlanetScript;
import com.gemserk.games.superflyingthing.systems.ControllerSystem;
import com.gemserk.games.superflyingthing.systems.ParticleEmitterSystem;
import com.gemserk.games.superflyingthing.systems.RenderLayerShapeImpl;
import com.gemserk.games.superflyingthing.templates.EntityTemplates;
import com.gemserk.resources.ResourceManager;

public class BackgroundGameState extends GameStateImpl {

	static class BasicAIShipController implements ShipController, RayCastCallback {

		private final World physicsWorld;

		private boolean shouldReleaseShip;
		private float movementDirection;

		public BasicAIShipController(World physicsWorld) {
			this.physicsWorld = physicsWorld;
		}

		@Override
		public boolean shouldReleaseShip() {
			return shouldReleaseShip;
		}

		@Override
		public Vector2 getPosition() {
			return null;
		}

		@Override
		public float getMovementDirection() {
			return movementDirection;
		}

		@Override
		public void setEnabled(boolean enabled) {

		}

		@Override
		public void update(com.artemis.World world, Entity e) {
			updateShipInPlanetBehavior(world, e);
			updateShipBehavior(world, e);
		}

		private void updateShipBehavior(com.artemis.World world, Entity e) {

			AttachmentComponent attachmentComponent = e.getComponent(AttachmentComponent.class);
			if (attachmentComponent != null)
				return;

			MovementComponent movementComponent = ComponentWrapper.getMovementComponent(e);
			Vector2 direction = movementComponent.getDirection();
			
			Vector2 direction2 = new Vector2(direction).rotate(20 * randomDirection);
			Vector2 direction3 = new Vector2(direction).rotate(-20 * randomDirection);

			Spatial spatial = ComponentWrapper.getSpatial(e);
			Vector2 position = spatial.getPosition();
			
			Vector2 target1 = new Vector2(position).add(direction.tmp().nor().mul(3f));
			Vector2 target2 = new Vector2(position).add(direction2.tmp().nor().mul(3f));
			Vector2 target3 = new Vector2(position).add(direction3.tmp().nor().mul(3f));
			
			movementDirection = 0f;
			
			collides = false;
			physicsWorld.rayCast(this, position, target1);
			
			if (!collides)
				return;

			collides = false;
			physicsWorld.rayCast(this, position, target2);
			
			if (!collides) {
				movementDirection = 1f * randomDirection;
				return;
			}

			collides = false;
			physicsWorld.rayCast(this, position, target3);
			
			if (!collides) {
				movementDirection = -1f * randomDirection;
				return;
			}
			
			movementDirection = 1f * randomDirection;
		}
		
		boolean collides = false;
		float randomDirection = 1f;
		
		@Override
		public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
			collides = true;
			return 0;
		}

		private void updateShipInPlanetBehavior(com.artemis.World world, Entity e) {
			AttachmentComponent attachmentComponent = e.getComponent(AttachmentComponent.class);
			if (attachmentComponent == null)
				return;

			if (attachmentComponent.getEntity() == null)
				return;

			Entity ship = attachmentComponent.getEntity();

			MovementComponent movementComponent = ComponentWrapper.getMovementComponent(ship);
			Vector2 direction = movementComponent.getDirection();
			if (AngleUtils.minimumDifference(direction.angle(), 0) < 10)
				shouldReleaseShip = true;
			
			randomDirection = MathUtils.randomBoolean() ? 1f : -1f;
		}


	}

	SpriteBatch spriteBatch;
	Libgdx2dCamera worldCamera;

	EntityTemplates entityTemplates;
	World physicsWorld;
	Box2DCustomDebugRenderer box2dCustomDebugRenderer;
	ResourceManager<String> resourceManager;
	boolean done;

	EntityBuilder entityBuilder;
	private com.artemis.World world;
	private WorldWrapper worldWrapper;

	GameData gameData;

	private EventManager eventManager;
	private JointBuilder jointBuilder;

	public void setResourceManager(ResourceManager<String> resourceManager) {
		this.resourceManager = resourceManager;
	}

	@Override
	public void init() {
		spriteBatch = new SpriteBatch();

		eventManager = new EventManagerImpl();
		physicsWorld = new World(new Vector2(), false);

		jointBuilder = new JointBuilder(physicsWorld);

		worldCamera = new Libgdx2dCameraTransformImpl();
		worldCamera.center(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);

		Libgdx2dCamera backgroundLayerCamera = new Libgdx2dCameraTransformImpl();

		ArrayList<RenderLayer> renderLayers = new ArrayList<RenderLayer>();

		renderLayers.add(new RenderLayerSpriteBatchImpl(-1000, -100, backgroundLayerCamera, spriteBatch));
		renderLayers.add(new RenderLayerShapeImpl(-100, -50, worldCamera));
		renderLayers.add(new RenderLayerSpriteBatchImpl(-50, 100, worldCamera, spriteBatch));

		world = new com.artemis.World();
		worldWrapper = new WorldWrapper(world);
		// add render and all stuff...
		GameInformation.worldWrapper = worldWrapper;

		worldWrapper.addUpdateSystem(new PhysicsSystem(physicsWorld));
		worldWrapper.addUpdateSystem(new ControllerSystem());
		worldWrapper.addUpdateSystem(new ScriptSystem());

		worldWrapper.addRenderSystem(new SpriteUpdateSystem());
		worldWrapper.addRenderSystem(new RenderableSystem(renderLayers));

		// worldWrapper.addRenderSystem(new ShapeRenderSystem(worldCamera));

		worldWrapper.addRenderSystem(new ParticleEmitterSystem(worldCamera));

		worldWrapper.init();

		entityBuilder = new EntityBuilder(world);

		box2dCustomDebugRenderer = new Box2DCustomDebugRenderer((Libgdx2dCameraTransformImpl) worldCamera, physicsWorld);

		entityTemplates = new EntityTemplates(physicsWorld, world, resourceManager, entityBuilder);

		gameData = new GameData();

		Sprite backgroundSprite = resourceManager.getResourceValue("BackgroundSprite");
		entityTemplates.staticSprite(backgroundSprite, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), 0, -999, 0, 0, Color.WHITE);

		loadLevel(entityTemplates, Levels.level(MathUtils.random(0, Levels.levelsCount() - 1)));
		// loadLevel(entityTemplates, Levels.level(MathUtils.random(0, 5)));
		// loadLevel(entityTemplates, Levels.level(12));
	}

	private void createWorldLimits(float worldWidth, float worldHeight) {
		createWorldLimits(worldWidth, worldHeight, 0.2f);
	}

	private void createWorldLimits(float worldWidth, float worldHeight, float offset) {
		float centerX = worldWidth * 0.5f;
		float centerY = worldHeight * 0.5f;
		float limitWidth = 0.1f;
		entityTemplates.boxObstacle(centerX, -offset, worldWidth, limitWidth, 0f);
		entityTemplates.boxObstacle(centerX, worldHeight + offset, worldWidth, limitWidth, 0f);
		entityTemplates.boxObstacle(-offset, centerY, limitWidth, worldHeight, 0f);
		entityTemplates.boxObstacle(worldWidth + offset, centerY, limitWidth, worldHeight, 0f);
	}

	void loadLevel(EntityTemplates templates, Level level) {
		float worldWidth = level.w;
		float worldHeight = level.h;

		float cameraZoom = Gdx.graphics.getWidth() * 10f / 800f;

		final Camera camera = new CameraRestrictedImpl(worldWidth * 0.5f, worldHeight * 0.5f, //
				cameraZoom, 0f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), new Rectangle(0f, 0f, worldWidth, worldHeight));

		final ShipController controller = new BasicAIShipController(physicsWorld);

		Entity startPlanet = entityTemplates.startPlanet(level.startPlanet.x, level.startPlanet.y, 1f, controller, new StartPlanetScript(physicsWorld, jointBuilder, eventManager));

		for (int i = 0; i < level.destinationPlanets.size(); i++) {
			DestinationPlanet destinationPlanet = level.destinationPlanets.get(i);
			entityTemplates.destinationPlanet(destinationPlanet.x, destinationPlanet.y, 1f, new DestinationPlanetScript(eventManager, jointBuilder));
		}

		Entity cameraEntity = entityTemplates.camera(camera, worldCamera, level.startPlanet.x, level.startPlanet.y, new CameraScript(eventManager));

		for (int i = 0; i < level.obstacles.size(); i++) {
			Obstacle o = level.obstacles.get(i);
			if (o.bodyType == BodyType.StaticBody)
				entityTemplates.obstacle(o.vertices, o.x, o.y, o.angle * MathUtils.degreesToRadians);
			else {
				entityTemplates.movingObstacle(o.vertices, o.path, o.x, o.y, o.angle * MathUtils.degreesToRadians);
			}
		}

		for (int i = 0; i < level.items.size(); i++) {
			Level.Item item = level.items.get(i);
			entityTemplates.star(item.x, item.y, new StarScript(eventManager));
		}

		for (int i = 0; i < level.laserTurrets.size(); i++) {
			LaserTurret laserTurret = level.laserTurrets.get(i);
			entityTemplates.laserTurret(laserTurret.x, laserTurret.y, laserTurret.angle, new Scripts.LaserGunScript(entityTemplates, physicsWorld));
		}

		for (int i = 0; i < level.portals.size(); i++) {
			Portal portal = level.portals.get(i);
			entityTemplates.portal(portal.id, portal.targetPortalId, portal.x, portal.y, new Scripts.PortalScript());
		}

		gameData.totalItems = level.items.size();

		createWorldLimits(worldWidth, worldHeight);

		// entityBuilder //
		// .component(new ScriptComponent(new UpdateControllerScript(controller))) //
		// .build();

		entityBuilder //
				.component(new GameDataComponent(null, startPlanet, cameraEntity)) //
				.component(new ScriptComponent(new Scripts.GameScript(eventManager, controller, entityTemplates, gameData, false))) //
				.build();

	}

	@Override
	public void render(int delta) {
		Gdx.graphics.getGL10().glClear(GL10.GL_COLOR_BUFFER_BIT);

		worldWrapper.render();

		if (Game.isShowBox2dDebug())
			box2dCustomDebugRenderer.render();
	}

	@Override
	public void update(int delta) {
		Synchronizers.synchronize(delta);
		worldWrapper.update(delta);
	}

	@Override
	public void dispose() {
		spriteBatch.dispose();
		physicsWorld.dispose();
	}

}