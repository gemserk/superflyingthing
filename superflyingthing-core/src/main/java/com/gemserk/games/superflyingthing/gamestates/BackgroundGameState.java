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
import com.badlogic.gdx.physics.box2d.World;
import com.gemserk.animation4j.transitions.sync.Synchronizers;
import com.gemserk.commons.artemis.EntityBuilder;
import com.gemserk.commons.artemis.WorldWrapper;
import com.gemserk.commons.artemis.components.ScriptComponent;
import com.gemserk.commons.artemis.events.Event;
import com.gemserk.commons.artemis.events.EventListener;
import com.gemserk.commons.artemis.events.EventListenerManager;
import com.gemserk.commons.artemis.events.EventListenerManagerImpl;
import com.gemserk.commons.artemis.events.EventManager;
import com.gemserk.commons.artemis.events.EventManagerImpl;
import com.gemserk.commons.artemis.scripts.EventSystemScript;
import com.gemserk.commons.artemis.scripts.ScriptJavaImpl;
import com.gemserk.commons.artemis.systems.ContainerSystem;
import com.gemserk.commons.artemis.systems.OwnerSystem;
import com.gemserk.commons.artemis.systems.PhysicsSystem;
import com.gemserk.commons.artemis.systems.RenderLayer;
import com.gemserk.commons.artemis.systems.RenderLayerSpriteBatchImpl;
import com.gemserk.commons.artemis.systems.RenderableSystem;
import com.gemserk.commons.artemis.systems.ScriptSystem;
import com.gemserk.commons.artemis.systems.SpriteUpdateSystem;
import com.gemserk.commons.artemis.templates.EntityFactory;
import com.gemserk.commons.artemis.templates.EntityFactoryImpl;
import com.gemserk.commons.gdx.GameStateImpl;
import com.gemserk.commons.gdx.box2d.Box2DCustomDebugRenderer;
import com.gemserk.commons.gdx.box2d.JointBuilder;
import com.gemserk.commons.gdx.camera.Camera;
import com.gemserk.commons.gdx.camera.CameraRestrictedImpl;
import com.gemserk.commons.gdx.camera.Libgdx2dCamera;
import com.gemserk.commons.gdx.camera.Libgdx2dCameraTransformImpl;
import com.gemserk.commons.gdx.games.SpatialImpl;
import com.gemserk.componentsengine.utils.Parameters;
import com.gemserk.componentsengine.utils.ParametersWrapper;
import com.gemserk.componentsengine.utils.timers.CountDownTimer;
import com.gemserk.componentsengine.utils.timers.Timer;
import com.gemserk.games.superflyingthing.Events;
import com.gemserk.games.superflyingthing.Game;
import com.gemserk.games.superflyingthing.ShipController;
import com.gemserk.games.superflyingthing.components.Components.GameData;
import com.gemserk.games.superflyingthing.components.Components.GameDataComponent;
import com.gemserk.games.superflyingthing.components.TagComponent;
import com.gemserk.games.superflyingthing.levels.Level;
import com.gemserk.games.superflyingthing.levels.Level.DestinationPlanet;
import com.gemserk.games.superflyingthing.levels.Level.LaserTurret;
import com.gemserk.games.superflyingthing.levels.Level.Obstacle;
import com.gemserk.games.superflyingthing.levels.Level.Portal;
import com.gemserk.games.superflyingthing.levels.Levels;
import com.gemserk.games.superflyingthing.scripts.CameraScript;
import com.gemserk.games.superflyingthing.scripts.LaserGunScript;
import com.gemserk.games.superflyingthing.scripts.Scripts;
import com.gemserk.games.superflyingthing.scripts.Scripts.DestinationPlanetScript;
import com.gemserk.games.superflyingthing.scripts.Scripts.StarScript;
import com.gemserk.games.superflyingthing.scripts.Scripts.StartPlanetScript;
import com.gemserk.games.superflyingthing.scripts.controllers.BasicAIShipControllerScript;
import com.gemserk.games.superflyingthing.systems.ParticleEmitterSystem;
import com.gemserk.games.superflyingthing.systems.RenderLayerShapeImpl;
import com.gemserk.games.superflyingthing.systems.TagSystem;
import com.gemserk.games.superflyingthing.templates.EntityTemplates;
import com.gemserk.resources.ResourceManager;

public class BackgroundGameState extends GameStateImpl {

	private final Game game;
	SpriteBatch spriteBatch;
	Libgdx2dCamera worldCamera;

	EntityTemplates entityTemplates;
	World physicsWorld;
	Box2DCustomDebugRenderer box2dCustomDebugRenderer;
	ResourceManager<String> resourceManager;
	boolean done;

	EntityBuilder entityBuilder;
	private com.artemis.World world;
	private EntityFactory entityFactory;
	private WorldWrapper worldWrapper;
	private Parameters parameters;

	GameData gameData;

	private JointBuilder jointBuilder;

	private EventManager eventManager;
	private EventListenerManager eventListenerManager;

	private Timer timer;

	public void setResourceManager(ResourceManager<String> resourceManager) {
		this.resourceManager = resourceManager;
	}

	public BackgroundGameState(Game game) {
		this.game = game;
	}

	@Override
	public void init() {
		spriteBatch = new SpriteBatch();

		timer = new CountDownTimer(4000, false);

		eventManager = new EventManagerImpl();
		eventListenerManager = new EventListenerManagerImpl();

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
		entityFactory = new EntityFactoryImpl(world);
		worldWrapper = new WorldWrapper(world);
		parameters = new ParametersWrapper();
		// add render and all stuff...
		GameInformation.worldWrapper = worldWrapper;

		worldWrapper.addUpdateSystem(new PhysicsSystem(physicsWorld));
		worldWrapper.addUpdateSystem(new ScriptSystem());
		worldWrapper.addUpdateSystem(new TagSystem());
		worldWrapper.addUpdateSystem(new ContainerSystem());
		worldWrapper.addUpdateSystem(new OwnerSystem());

		worldWrapper.addRenderSystem(new SpriteUpdateSystem());
		worldWrapper.addRenderSystem(new RenderableSystem(renderLayers));

		// worldWrapper.addRenderSystem(new ShapeRenderSystem(worldCamera));

		worldWrapper.addRenderSystem(new ParticleEmitterSystem(worldCamera));

		worldWrapper.init();

		entityBuilder = new EntityBuilder(world);

		box2dCustomDebugRenderer = new Box2DCustomDebugRenderer((Libgdx2dCameraTransformImpl) worldCamera, physicsWorld);

		entityTemplates = new EntityTemplates(physicsWorld, world, resourceManager, entityBuilder, entityFactory);

		gameData = new GameData();

		Sprite backgroundSprite = resourceManager.getResourceValue("BackgroundSprite");
		entityTemplates.staticSprite(backgroundSprite, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), 0, -999, 0, 0, Color.WHITE);

		loadLevel(entityTemplates, Levels.level(MathUtils.random(0, Levels.levelsCount() - 1)));
		// loadLevel(entityTemplates, Levels.level(MathUtils.random(0, 3)));
		// loadLevel(entityTemplates, Levels.level(13));

		entityBuilder //
				.component(new TagComponent("EventManager")) //
				.component(new ScriptComponent(new EventSystemScript(eventManager, eventListenerManager))) //
				.build();

		// entity with some game logic
		entityBuilder.component(new ScriptComponent(new ScriptJavaImpl() {

			@Override
			public void init(com.artemis.World world, Entity e) {
				eventListenerManager.register(Events.destinationPlanetReached, new EventListener() {
					@Override
					public void onEvent(Event event) {
						destinationPlanetReached(event);
					}
				});
			}

			// @EventListener(Events.destinationPlanetReached)
			public void destinationPlanetReached(Event e) {
				gameFinished();
			}

		})).build();
	}

	private void gameFinished() {
		// game.getBackgroundGameScreen().restart();
		timer.reset();
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

		final ShipController controller = new ShipController();
		// final BasicAIShipController controller = new BasicAIShipController(physicsWorld);

		Entity startPlanet = entityTemplates.startPlanet(level.startPlanet.x, level.startPlanet.y, 1f, controller, new StartPlanetScript(physicsWorld, jointBuilder, eventListenerManager));

		for (int i = 0; i < level.destinationPlanets.size(); i++) {
			DestinationPlanet destinationPlanet = level.destinationPlanets.get(i);
			entityTemplates.destinationPlanet(destinationPlanet.x, destinationPlanet.y, 1f, new DestinationPlanetScript(eventManager, jointBuilder, entityFactory, entityTemplates.getPlanetFillAnimationTemplate()));
		}

		parameters.clear();
		parameters.put("camera", camera);
		parameters.put("libgdxCamera", worldCamera);
		parameters.put("script", new CameraScript(eventManager, eventListenerManager));
		parameters.put("spatial", new SpatialImpl(level.startPlanet.x, level.startPlanet.y, 1f, 1f, 0f));
		Entity cameraEntity = entityFactory.instantiate(entityTemplates.getCameraTemplate(), parameters);

		for (int i = 0; i < level.obstacles.size(); i++) {
			Obstacle o = level.obstacles.get(i);
			if (o.bodyType == BodyType.StaticBody)
				entityTemplates.obstacle(o.vertices, o.x, o.y, o.angle * MathUtils.degreesToRadians);
			else {
				entityTemplates.movingObstacle(o.vertices, o.path, o.startPoint, o.x, o.y, o.angle * MathUtils.degreesToRadians);
			}
		}

		for (int i = 0; i < level.items.size(); i++) {
			Level.Item item = level.items.get(i);
			entityTemplates.star(item.x, item.y, new StarScript(eventManager));
		}

		for (int i = 0; i < level.laserTurrets.size(); i++) {
			LaserTurret laserTurret = level.laserTurrets.get(i);

			parameters.clear();

			parameters.put("position", new Vector2(laserTurret.x, laserTurret.y));
			parameters.put("angle", laserTurret.angle);
			parameters.put("fireRate", laserTurret.fireRate);
			parameters.put("bulletDuration", laserTurret.bulletDuration);
			parameters.put("currentReloadTime", laserTurret.currentReloadTime);
			parameters.put("script", new LaserGunScript(entityFactory));

			entityFactory.instantiate(entityTemplates.getLaserGunTemplate(), parameters);
		}

		for (int i = 0; i < level.portals.size(); i++) {
			Portal portal = level.portals.get(i);

			parameters.clear();

			parameters.put("id", portal.id);
			parameters.put("targetPortalId", portal.targetPortalId);
			parameters.put("spatial", new SpatialImpl(portal.x, portal.y, portal.w, portal.h, portal.angle));

			entityFactory.instantiate(entityTemplates.getPortalTemplate(), parameters);

			// entityTemplates.portal(portal.id, portal.targetPortalId, portal.x, portal.y, new PortalScript());
		}

		gameData.totalItems = level.items.size();

		createWorldLimits(worldWidth, worldHeight);

		entityBuilder.component(new ScriptComponent(new BasicAIShipControllerScript(physicsWorld, controller))).build();

		entityBuilder //
				.component(new GameDataComponent(null, startPlanet, cameraEntity)) //
				.component(new ScriptComponent(new Scripts.GameScript(eventManager, eventListenerManager, //
						entityTemplates, entityFactory, gameData, controller, false))) //
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

		if (timer.update(delta))
			game.getBackgroundGameScreen().restart();
	}

	@Override
	public void dispose() {
		spriteBatch.dispose();
		physicsWorld.dispose();
	}

}