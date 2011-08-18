package com.gemserk.games.superflyingthing.gamestates;

import java.text.MessageFormat;
import java.util.ArrayList;

import com.artemis.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.gemserk.analytics.Analytics;
import com.gemserk.animation4j.transitions.sync.Synchronizers;
import com.gemserk.commons.artemis.EntityBuilder;
import com.gemserk.commons.artemis.WorldWrapper;
import com.gemserk.commons.artemis.components.ScriptComponent;
import com.gemserk.commons.artemis.events.Event;
import com.gemserk.commons.artemis.events.EventListenerManager;
import com.gemserk.commons.artemis.events.EventListenerManagerImpl;
import com.gemserk.commons.artemis.events.EventManager;
import com.gemserk.commons.artemis.events.EventManagerImpl;
import com.gemserk.commons.artemis.events.reflection.Handles;
import com.gemserk.commons.artemis.scripts.EventSystemScript;
import com.gemserk.commons.artemis.scripts.ScriptJavaImpl;
import com.gemserk.commons.artemis.systems.ContainerSystem;
import com.gemserk.commons.artemis.systems.OwnerSystem;
import com.gemserk.commons.artemis.systems.PhysicsSystem;
import com.gemserk.commons.artemis.systems.ReflectionRegistratorEventSystem;
import com.gemserk.commons.artemis.systems.RenderLayer;
import com.gemserk.commons.artemis.systems.RenderLayerSpriteBatchImpl;
import com.gemserk.commons.artemis.systems.RenderableSystem;
import com.gemserk.commons.artemis.systems.ScriptSystem;
import com.gemserk.commons.artemis.systems.SpriteUpdateSystem;
import com.gemserk.commons.artemis.templates.EntityFactory;
import com.gemserk.commons.artemis.templates.EntityFactoryImpl;
import com.gemserk.commons.artemis.templates.EntityTemplate;
import com.gemserk.commons.gdx.GameStateImpl;
import com.gemserk.commons.gdx.box2d.Box2DCustomDebugRenderer;
import com.gemserk.commons.gdx.box2d.JointBuilder;
import com.gemserk.commons.gdx.camera.Camera;
import com.gemserk.commons.gdx.camera.CameraRestrictedImpl;
import com.gemserk.commons.gdx.camera.Libgdx2dCamera;
import com.gemserk.commons.gdx.camera.Libgdx2dCameraTransformImpl;
import com.gemserk.commons.gdx.games.SpatialImpl;
import com.gemserk.commons.gdx.gui.Container;
import com.gemserk.commons.gdx.gui.GuiControls;
import com.gemserk.commons.gdx.gui.Text;
import com.gemserk.componentsengine.input.InputDevicesMonitorImpl;
import com.gemserk.componentsengine.input.LibgdxInputMappingBuilder;
import com.gemserk.componentsengine.utils.Parameters;
import com.gemserk.componentsengine.utils.ParametersWrapper;
import com.gemserk.componentsengine.utils.timers.CountDownTimer;
import com.gemserk.games.superflyingthing.Colors;
import com.gemserk.games.superflyingthing.Game;
import com.gemserk.games.superflyingthing.Shape;
import com.gemserk.games.superflyingthing.ShipController;
import com.gemserk.games.superflyingthing.components.ComponentWrapper;
import com.gemserk.games.superflyingthing.components.Components.CameraComponent;
import com.gemserk.games.superflyingthing.components.Components.ControllerComponent;
import com.gemserk.games.superflyingthing.components.Components.GameData;
import com.gemserk.games.superflyingthing.components.Components.GameDataComponent;
import com.gemserk.games.superflyingthing.components.Components.ReplayComponent;
import com.gemserk.games.superflyingthing.components.ReplayList;
import com.gemserk.games.superflyingthing.components.TagComponent;
import com.gemserk.games.superflyingthing.levels.Level;
import com.gemserk.games.superflyingthing.levels.Level.DestinationPlanet;
import com.gemserk.games.superflyingthing.levels.Level.LaserTurret;
import com.gemserk.games.superflyingthing.levels.Level.Obstacle;
import com.gemserk.games.superflyingthing.levels.Level.Portal;
import com.gemserk.games.superflyingthing.levels.Level.StartPlanet;
import com.gemserk.games.superflyingthing.levels.Levels;
import com.gemserk.games.superflyingthing.preferences.GamePreferences;
import com.gemserk.games.superflyingthing.preferences.PlayerProfile;
import com.gemserk.games.superflyingthing.preferences.PlayerProfile.LevelInformation;
import com.gemserk.games.superflyingthing.scripts.CameraScript;
import com.gemserk.games.superflyingthing.scripts.LaserGunScript;
import com.gemserk.games.superflyingthing.scripts.ReplayRecorderScript;
import com.gemserk.games.superflyingthing.scripts.Scripts;
import com.gemserk.games.superflyingthing.scripts.Scripts.DestinationPlanetScript;
import com.gemserk.games.superflyingthing.scripts.Scripts.StarScript;
import com.gemserk.games.superflyingthing.scripts.Scripts.StartPlanetScript;
import com.gemserk.games.superflyingthing.scripts.controllers.ControllerType;
import com.gemserk.games.superflyingthing.systems.ParticleEmitterSystem;
import com.gemserk.games.superflyingthing.systems.RenderLayerShapeImpl;
import com.gemserk.games.superflyingthing.systems.TagSystem;
import com.gemserk.games.superflyingthing.templates.ControllerTemplates;
import com.gemserk.games.superflyingthing.templates.EntityTemplates;
import com.gemserk.games.superflyingthing.templates.Groups;
import com.gemserk.games.superflyingthing.templates.UserMessageTemplate;
import com.gemserk.resources.ResourceManager;

public class PlayGameState extends GameStateImpl {

	private final Game game;
	SpriteBatch spriteBatch;
	Libgdx2dCamera worldCamera;

	World physicsWorld;
	Box2DCustomDebugRenderer box2dCustomDebugRenderer;
	ResourceManager<String> resourceManager;
	boolean resetPressed;
	boolean done;
	Container container;
	private Libgdx2dCamera guiCamera;
	private InputDevicesMonitorImpl<String> inputDevicesMonitor;

	EntityTemplates entityTemplates;
	ControllerTemplates controllerTemplates;

	EntityBuilder entityBuilder;
	private com.artemis.World world;
	private WorldWrapper worldWrapper;
	private EntityFactory entityFactory;
	private Parameters parameters;

	GameData gameData;
	private Text itemsTakenLabel;
	private JointBuilder jointBuilder;
	private Text timerLabel;

	private EventManager eventManager;
	private EventListenerManager eventListenerManager;

	private CountDownTimer gameOverTimer;

	EntityTemplate userMessageTemplate;

	private GamePreferences gamePreferences;

	private Text tiltvalue;
	private final boolean tiltValueEnabled = false;
	int tilttime = 0;

	public void setResourceManager(ResourceManager<String> resourceManager) {
		this.resourceManager = resourceManager;
	}

	public void setGamePreferences(GamePreferences gamePreferences) {
		this.gamePreferences = gamePreferences;
	}

	public PlayGameState(Game game) {
		this.game = game;
	}

	@Override
	public void init() {
		gameOverTimer = null;

		resetPressed = false;
		spriteBatch = new SpriteBatch();

		eventManager = new EventManagerImpl();
		eventListenerManager = new EventListenerManagerImpl();

		physicsWorld = new World(new Vector2(), false);

		jointBuilder = new JointBuilder(physicsWorld);

		worldCamera = new Libgdx2dCameraTransformImpl();
		worldCamera.center(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);

		guiCamera = new Libgdx2dCameraTransformImpl();

		Libgdx2dCamera backgroundLayerCamera = new Libgdx2dCameraTransformImpl();
		final Libgdx2dCamera secondBackgroundLayerCamera = new Libgdx2dCameraTransformImpl();
		secondBackgroundLayerCamera.center(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);

		ArrayList<RenderLayer> renderLayers = new ArrayList<RenderLayer>();

		renderLayers.add(new RenderLayerSpriteBatchImpl(-10000, -500, backgroundLayerCamera, spriteBatch));
		renderLayers.add(new RenderLayerSpriteBatchImpl(-500, -100, secondBackgroundLayerCamera, spriteBatch));
		renderLayers.add(new RenderLayerShapeImpl(-100, -50, worldCamera));
		renderLayers.add(new RenderLayerSpriteBatchImpl(-50, 100, worldCamera));
		renderLayers.add(new RenderLayerSpriteBatchImpl(100, 10000, guiCamera));

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

		// testing event listener auto registration using reflection
		worldWrapper.addUpdateSystem(new ReflectionRegistratorEventSystem(eventListenerManager));

		worldWrapper.addRenderSystem(new SpriteUpdateSystem());
		worldWrapper.addRenderSystem(new RenderableSystem(renderLayers));
		worldWrapper.addRenderSystem(new ParticleEmitterSystem(worldCamera));

		worldWrapper.init();

		entityBuilder = new EntityBuilder(world);

		box2dCustomDebugRenderer = new Box2DCustomDebugRenderer((Libgdx2dCameraTransformImpl) worldCamera, physicsWorld);

		container = new Container();

		entityTemplates = new EntityTemplates(physicsWorld, world, resourceManager, entityBuilder, entityFactory);

		// creates and registers all the controller templates
		controllerTemplates = new ControllerTemplates();
		controllerTemplates.keyboardControllerTemplate = new ControllerTemplates.KeyboardControllerTemplate();
		controllerTemplates.androidClassicControllerTemplate = new ControllerTemplates.AndroidClassicControllerTemplate();
		controllerTemplates.axisControllerTemplate = new ControllerTemplates.AxisControllerTemplate(resourceManager);
		controllerTemplates.analogControllerTemplate = new ControllerTemplates.AnalogControllerTemplate(resourceManager);
		controllerTemplates.tiltAndroidControllerTemplate = new ControllerTemplates.TiltAndroidControllerTemplate();
		controllerTemplates.analogKeyboardControllerTemplate = new ControllerTemplates.AnalogKeyboardControllerTemplate();
		controllerTemplates.targetControllerTemplate = new ControllerTemplates.TargetControllerTemplate();

		gameData = new GameData();
		GameInformation.gameData = gameData;

		userMessageTemplate = new UserMessageTemplate(container, resourceManager);

		BitmapFont font = resourceManager.getResourceValue("GameFont");

		itemsTakenLabel = GuiControls.label("") //
				.position(Gdx.graphics.getWidth() * 0.5f, Gdx.graphics.getHeight() * 0.95f) //
				.font(font) //
				.color(1f, 1f, 1f, 1f) //
				.build();

		timerLabel = GuiControls.label("") //
				.position(Gdx.graphics.getWidth() * 0.05f, Gdx.graphics.getHeight() * 0.95f) //
				.center(0f, 0.5f) //
				.font(font) //
				.color(1f, 1f, 1f, 1f) //
				.build();

		container.add(itemsTakenLabel);
		container.add(timerLabel);

		if (tiltValueEnabled) {
			tiltvalue = GuiControls.label("") //
					.position(Gdx.graphics.getWidth() * 0.5f, Gdx.graphics.getHeight() * 0.90f) //
					.font(font) //
					.color(1f, 1f, 1f, 1f) //
					.build();
			container.add(tiltvalue);
		}

		Sprite backgroundSprite = resourceManager.getResourceValue("BackgroundSprite");
		entityTemplates.staticSprite(backgroundSprite, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), 0, -999, 0, 0, Color.WHITE);

		if (GameInformation.gameMode == GameInformation.ChallengeGameMode) {
			loadLevelForChallengeMode();
			Analytics.traker.trackPageView("/challenge/" + (GameInformation.level + 1) + "/start", "/challenge/" + (GameInformation.level + 1) + "/start", null);
		} else if (GameInformation.gameMode == GameInformation.PracticeGameMode) {
			loadRandomLevelForRandomMode(true);
			Analytics.traker.trackPageView("/practice/start", "/practice/start", null);
		} else if (GameInformation.gameMode == GameInformation.RandomGameMode) {
			loadRandomLevelForRandomMode(false);
			Analytics.traker.trackPageView("/random/start", "/random/start", null);
		}

		final PlayerProfile playerProfile = game.getGamePreferences().getCurrentPlayerProfile();

		entityBuilder //
				.component(new TagComponent("EventManager")) //
				.component(new ScriptComponent(new EventSystemScript(eventManager, eventListenerManager))) //
				.build();

		// entity with some game logic
		entityBuilder.component(new ScriptComponent(new ScriptJavaImpl() {

			boolean incrementTimer = true;
			private com.artemis.World world;

			@Override
			public void init(com.artemis.World world, Entity e) {
				this.world = world;
				timerLabelBuilder.append("Time: ");
			}

			@Handles
			public void itemTaken(Event e) {
				gameData.currentItems++;
				itemsTakenLabel.setText(MessageFormat.format("{0}/{1}", gameData.currentItems, gameData.totalItems));
			}

			@Handles
			public void gameStarted(Event e) {
				if (GameInformation.gameMode != GameInformation.ChallengeGameMode)
					return;
				Level level = Levels.level(GameInformation.level);
				parameters.clear();
				parameters.put("position", new Vector2(Gdx.graphics.getWidth() * 0.5f, Gdx.graphics.getHeight() * 0.8f));
				parameters.put("text", "Level " + (GameInformation.level + 1) + ": " + level.name);
				entityFactory.instantiate(userMessageTemplate, parameters);
			}

			@Handles
			public void gameFinished(Event e) {

				PlayGameState.this.gameFinished();
				incrementTimer = false;
				if (GameInformation.gameMode == GameInformation.ChallengeGameMode) {
					playerProfile.setLevelInformationForLevel(GameInformation.level + 1, new LevelInformation(seconds(gameData.time), gameData.currentItems));
					game.getGamePreferences().updatePlayerProfile(playerProfile);
				}

				Entity playerController = world.getTagManager().getEntity(Groups.PlayerController);
				playerController.delete();

				Entity controllerSwitcher = world.getTagManager().getEntity("ControllerSwitcher");
				controllerSwitcher.delete();
			}

			private int seconds = -1;
			private StringBuilder timerLabelBuilder = new StringBuilder();

			@Override
			public void update(com.artemis.World world, Entity e) {

				if (incrementTimer)
					gameData.time += world.getDelta();

				if (seconds == seconds(gameData.time))
					return;

				timerLabelBuilder.delete(6, timerLabelBuilder.length());
				timerLabelBuilder.append(seconds(gameData.time));

				timerLabel.setText(timerLabelBuilder);

				seconds = seconds(gameData.time);
			}

			private int seconds(int ms) {
				return ms / 1000;
			}

		})).build();

		done = false;

		inputDevicesMonitor = new InputDevicesMonitorImpl<String>();
		new LibgdxInputMappingBuilder<String>(inputDevicesMonitor, Gdx.input) {
			{
				monitorKeys("pause", Keys.BACK, Keys.ESCAPE);
				monitorKeys("switchControls", Keys.MENU, Keys.R);
			}
		};

		entityBuilder //
				.component(new TagComponent(Groups.ControllerSwitcher)) //
				.component(new ScriptComponent(new ScriptJavaImpl() {

					private int current = 0;
					private ShipController controller;

					private ControllerType[] controllerTypes = new ControllerType[] { ControllerType.KeyboardController, //
							ControllerType.AnalogKeyboardController, ControllerType.ClassicController, ControllerType.AxisController, //
							ControllerType.AnalogController, ControllerType.TiltController, ControllerType.TargetController };

					@Override
					public void update(com.artemis.World world, Entity e) {

						Entity currentController = world.getTagManager().getEntity(Groups.PlayerController);

						if (currentController != null) {
							if (controller == null)
								controller = currentController.getComponent(ControllerComponent.class).getController();
						}

						if (currentController == null) {

							current++;
							if (current >= controllerTypes.length)
								current = 0;

							ControllerType controllerType = controllerTypes[current];
							String controllerName = controllerType.name();

							parameters.clear();
							parameters.put("controller", controller);
							entityFactory.instantiate(controllerTemplates.getControllerTemplate(controllerType), parameters);

							Gdx.app.log("SuperFlyingThing", "Changing controller to " + controllerName);
							parameters.clear();
							parameters.put("position", new Vector2(Gdx.graphics.getWidth() * 0.5f, Gdx.graphics.getHeight() * 0.8f));
							parameters.put("text", controllerName);
							parameters.put("time", 1000);
							entityFactory.instantiate(userMessageTemplate, parameters);
						}

						if (inputDevicesMonitor.getButton("switchControls").isReleased())
							currentController.delete();

					}
				})).build();

		ReplayList replayList = new ReplayList();

		entityBuilder //
				.component(new TagComponent(Groups.ReplayRecorder)) //
				.component(new ReplayComponent(replayList)) //
				.component(new ScriptComponent(new ReplayRecorderScript(eventListenerManager, entityFactory, entityTemplates.getReplayShipTemplate()))) //
				.build();

		entityBuilder //
				.component(new ScriptComponent(new ScriptJavaImpl() {

					@Override
					public void update(com.artemis.World world, Entity e) {
						Entity mainCamera = world.getTagManager().getEntity(Groups.MainCamera);
						CameraComponent cameraComponent = ComponentWrapper.getCameraComponent(mainCamera);

						Camera camera = cameraComponent.getCamera();

						secondBackgroundLayerCamera.move(camera.getX(), camera.getY());
						secondBackgroundLayerCamera.zoom(camera.getZoom() * 0.25f);
						secondBackgroundLayerCamera.rotate(camera.getAngle());
					}

				})) //
				.build();

	}

	private void createWorldLimits(float worldWidth, float worldHeight) {
		createWorldLimits(worldWidth, worldHeight, 0.2f);
	}

	private void generateRandomClouds(float width, float height, int count) {
		Sprite sprite = resourceManager.getResourceValue("FogSprite");

		Color[] colors = new Color[] { Colors.yellow, Color.RED, Color.GREEN, Color.BLUE, Color.BLACK, Colors.magenta };

		for (int i = 0; i < count; i++) {

			float x = MathUtils.random(0, width);
			float y = MathUtils.random(0, height);

			float w = MathUtils.random(50, 80f);
			float h = w;

			float angle = MathUtils.random(0, 359f);

			Color color = new Color(colors[MathUtils.random(0, colors.length - 1)]);
			color.a = 0.3f;

			entityTemplates.staticSprite(new Sprite(sprite), x, y, w, h, angle, -200, 0.5f, 0.5f, color);
		}
	}

	private void createWorldLimits(float worldWidth, float worldHeight, float offset) {
		float centerX = worldWidth * 0.5f;
		float centerY = worldHeight * 0.5f;
		float limitWidth = 0.1f;
		entityTemplates.boxObstacle(centerX, -offset, worldWidth + 1, limitWidth, 0f);
		entityTemplates.boxObstacle(centerX, worldHeight + offset, worldWidth + 1, limitWidth, 0f);
		entityTemplates.boxObstacle(-offset, centerY, limitWidth, worldHeight + 1, 0f);
		entityTemplates.boxObstacle(worldWidth + offset, centerY, limitWidth, worldHeight + 1, 0f);
	}

	void loadLevel(Level level, boolean shipInvulnerable) {
		float worldWidth = level.w;
		float worldHeight = level.h;

		float cameraZoom = Gdx.graphics.getWidth() * level.zoom / 800f;

		final Camera camera = new CameraRestrictedImpl(0f, 0f, cameraZoom, 0f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), new Rectangle(0f, 0f, worldWidth, worldHeight));

		final ShipController controller = new ShipController();

		Entity startPlanet = entityTemplates.startPlanet(level.startPlanet.x, level.startPlanet.y, 1f, controller, new StartPlanetScript(physicsWorld, jointBuilder, eventListenerManager));

		for (int i = 0; i < level.destinationPlanets.size(); i++) {
			DestinationPlanet destinationPlanet = level.destinationPlanets.get(i);
			entityTemplates.destinationPlanet(destinationPlanet.x, destinationPlanet.y, 1f, new DestinationPlanetScript(eventManager, jointBuilder, entityFactory, entityTemplates.getPlanetFillAnimationTemplate()));
		}

		parameters.clear();
		Entity cameraEntity = entityFactory.instantiate(entityTemplates.getCameraTemplate(), parameters //
				.put("camera", camera) //
				.put("libgdxCamera", worldCamera) //
				.put("script", new CameraScript(eventManager, eventListenerManager)) //
				.put("spatial", new SpatialImpl(level.startPlanet.x, level.startPlanet.y, 1f, 1f, 0f)) //
				);

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

			entityFactory.instantiate(entityTemplates.getLaserGunTemplate(), parameters //
					.put("position", new Vector2(laserTurret.x, laserTurret.y)) //
					.put("angle", laserTurret.angle) //
					.put("fireRate", laserTurret.fireRate) //
					.put("bulletDuration", laserTurret.bulletDuration) //
					.put("currentReloadTime", laserTurret.currentReloadTime) //
					.put("script", new LaserGunScript(entityFactory)) //
					);
		}

		for (int i = 0; i < level.portals.size(); i++) {
			Portal portal = level.portals.get(i);

			parameters.clear();

			entityFactory.instantiate(entityTemplates.getPortalTemplate(), parameters //
					.put("id", portal.id) //
					.put("targetPortalId", portal.targetPortalId) //
					.put("spatial", new SpatialImpl(portal.x, portal.y, portal.w, portal.h, portal.angle)) //
					);
		}

		createWorldLimits(worldWidth, worldHeight);

		createGameController(controller);

		entityBuilder //
				.component(new GameDataComponent(null, startPlanet, cameraEntity)) //
				.component(new ScriptComponent(new Scripts.GameScript(eventManager, eventListenerManager, entityTemplates, entityFactory, gameData, controller, shipInvulnerable))) //
				.build();

		generateRandomClouds(worldWidth, worldHeight, 6);
	}

	void loadLevelForChallengeMode() {
		if (Levels.hasLevel(GameInformation.level)) {
			Level level = Levels.level(GameInformation.level);
			loadLevel(level, false);

			gameData.totalItems = level.items.size();
			if (gameData.totalItems > 0)
				itemsTakenLabel.setText(MessageFormat.format("{0}/{1}", gameData.currentItems, gameData.totalItems));
		}
	}

	void loadRandomLevelForRandomMode(boolean invulnerable) {

		RandomLevelGenerator randomLevelGenerator = new RandomLevelGenerator();

		Level level = randomLevelGenerator.generateRandomLevel();
		loadLevel(level, invulnerable);

		int starsCount = randomLevelGenerator.generateStars(level.w, level.h, 10);

		gameData.totalItems = starsCount;
		if (gameData.totalItems > 0)
			itemsTakenLabel.setText(MessageFormat.format("{0}/{1}", gameData.currentItems, gameData.totalItems));
	}

	class RandomLevelGenerator {

		boolean insideObstacle;

		private final Shape[] shapes = new Shape[] { //
		new Shape(new Vector2[] { new Vector2(3f, 1.5f), new Vector2(1f, 4f), new Vector2(-2.5f, 1f), new Vector2(-1.5f, -2.5f), new Vector2(1f, -1.5f), }), //
				new Shape(new Vector2[] { new Vector2(1.5f, 0f), new Vector2(0.5f, 2f), new Vector2(-1.5f, 1f), new Vector2(-0.5f, -2.5f) }), //
				new Shape(new Vector2[] { new Vector2(2f, 1f), new Vector2(-3f, 1.2f), new Vector2(-2.5f, -0.8f), new Vector2(2.5f, -2f) }), //
		};

		private Shape getRandomShape() {
			return shapes[MathUtils.random(shapes.length - 1)];
		}

		Level generateRandomLevel() {

			Level level = new Level();

			level.w = MathUtils.random(30f, 250f);
			level.h = MathUtils.random(10f, 30f);

			level.startPlanet = new StartPlanet(5f, level.h * 0.5f);
			level.destinationPlanets.add(new DestinationPlanet(level.w - 5f, level.h * 0.5f));

			float obstacleX = 12f;

			while (obstacleX < level.w - 17f) {
				level.obstacles.add(new Obstacle(getRandomShape().vertices, obstacleX + 5f, MathUtils.random(0f, level.h), MathUtils.random(0f, 359f)));
				level.obstacles.add(new Obstacle(getRandomShape().vertices, obstacleX, MathUtils.random(0f, level.h), MathUtils.random(0f, 359f)));
				obstacleX += 8f;
			}

			return level;
		}

		private int generateStars(float worldWidth, float worldHeight, int maxStars) {
			// I need the bodies already loaded to call the query callback to know if I can create a new star in the position or not...
			// don't know another solution for now.
			int itemsCount = 0;

			for (int i = 0; i < maxStars; i++) {
				float x = MathUtils.random(10f, worldWidth - 10f);
				float y = MathUtils.random(2f, worldHeight - 2f);
				float w = 0.2f;
				float h = 0.2f;

				insideObstacle = false;

				physicsWorld.QueryAABB(new QueryCallback() {
					@Override
					public boolean reportFixture(Fixture fixture) {
						insideObstacle = true;
						return false;
					}
				}, x - w, y - h, x + w, y + h);

				if (insideObstacle)
					continue;

				entityTemplates.star(x, y, new StarScript(eventManager));

				itemsCount++;
			}
			return itemsCount;
		}
	}

	private void createGameController(ShipController controller) {
		Parameters parameters = new ParametersWrapper();

		PlayerProfile playerProfile = gamePreferences.getCurrentPlayerProfile();
		ControllerType controllerType = playerProfile.getControllerType();

		parameters.put("controller", controller);
		entityFactory.instantiate(controllerTemplates.getControllerTemplate(controllerType), parameters);
	}

	private void gameFinished() {

		parameters.clear();

		parameters.put("position", new Vector2(Gdx.graphics.getWidth() * 0.5f, Gdx.graphics.getHeight() * 0.8f));
		parameters.put("text", getRandomEndMessage());

		entityFactory.instantiate(userMessageTemplate, parameters);

		gameOverTimer = new CountDownTimer(2500, true);

		// start replays when game ends...

		// container.add(message);

		if (GameInformation.gameMode == GameInformation.ChallengeGameMode) {
			Analytics.traker.trackPageView("/challenge/" + (GameInformation.level + 1) + "/finish", "/challenge/" + (GameInformation.level + 1) + "/finish", null);
		} else if (GameInformation.gameMode == GameInformation.PracticeGameMode) {
			Analytics.traker.trackPageView("/practice/finish", "/practice/finish", null);
		} else if (GameInformation.gameMode == GameInformation.RandomGameMode) {
			Analytics.traker.trackPageView("/random/finish", "/random/finish", null);
		}

	}

	private static String[] endMessages = new String[] { "Great Job!", "Nicely Done!", "You made it!", "Good Work!", "You Rock!", };

	private String getRandomEndMessage() {
		return endMessages[MathUtils.random(endMessages.length - 1)];
	}

	@Override
	public void render() {
		Gdx.graphics.getGL10().glClear(GL10.GL_COLOR_BUFFER_BIT);

		worldWrapper.render();

		if (Game.isShowBox2dDebug())
			box2dCustomDebugRenderer.render();

		guiCamera.apply(spriteBatch);
		spriteBatch.begin();
		container.draw(spriteBatch);
		spriteBatch.end();
	}

	@Override
	public void update() {
		GamePreferences gamePreferences = game.getGamePreferences();
		if (gamePreferences.isTutorialEnabled()) {
			game.transition(game.getInstructionsScreen(), 0, 300, false);
			return;
		}

		inputDevicesMonitor.update();
		Synchronizers.synchronize(getDelta());
		container.update();

		if (gameOverTimer != null) {
			if (gameOverTimer.update(getDeltaInMs()))
				done = true;
		}

		if (inputDevicesMonitor.getButton("pause").isReleased())
			game.transition(game.getPauseScreen(), 200, 300, false);

		if (done) {
			if (GameInformation.gameMode == GameInformation.ChallengeGameMode) {
				game.transition(game.getGameOverScreen(), 0, 300, false);
			} else {
				game.transition(game.getGameOverScreen(), 0, 300, false);
			}
		}

		worldWrapper.update(getDeltaInMs());
		if (tiltValueEnabled) {
			float pitch = Gdx.input.getPitch();
			tilttime += getDeltaInMs();
			if (tilttime > 200) {
				tiltvalue.setText(String.format("%8.4f", pitch));
				tilttime -= 200;
			}
		}
	}

	@Override
	public void resume() {
		// automatically handled in Game class and if no previous screen, then don't handle it (or system.exit())
		game.getAdWhirlViewHandler().hide();
		super.resume();
		Gdx.input.setCatchBackKey(true);
		game.getBackgroundGameScreen().dispose();

		// recreate controller ...

		Entity playerController = world.getTagManager().getEntity(Groups.PlayerController);
		if (playerController != null) {

			ControllerComponent controllerComponent = playerController.getComponent(ControllerComponent.class);
			// mark current controller to be deleted
			playerController.delete();

			// creates a new controller using new preferences
			createGameController(controllerComponent.getController());
		}
	}

	@Override
	public void pause() {
		super.pause();
	}

	@Override
	public void dispose() {
		worldWrapper.dispose();
		spriteBatch.dispose();
		physicsWorld.dispose();
	}

}
