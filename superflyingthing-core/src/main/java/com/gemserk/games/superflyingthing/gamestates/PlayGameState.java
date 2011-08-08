package com.gemserk.games.superflyingthing.gamestates;

import java.text.MessageFormat;
import java.util.ArrayList;

import com.artemis.Entity;
import com.badlogic.gdx.Application.ApplicationType;
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
import com.gemserk.animation4j.animations.Animation;
import com.gemserk.animation4j.timeline.Builders;
import com.gemserk.animation4j.timeline.sync.MutableObjectSynchronizer;
import com.gemserk.animation4j.timeline.sync.SynchronizedAnimation;
import com.gemserk.animation4j.timeline.sync.TimelineSynchronizer;
import com.gemserk.animation4j.transitions.sync.Synchronizers;
import com.gemserk.commons.artemis.EntityBuilder;
import com.gemserk.commons.artemis.ScriptJavaImpl;
import com.gemserk.commons.artemis.WorldWrapper;
import com.gemserk.commons.artemis.components.ScriptComponent;
import com.gemserk.commons.artemis.events.Event;
import com.gemserk.commons.artemis.events.EventManager;
import com.gemserk.commons.artemis.events.EventManagerImpl;
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
import com.gemserk.commons.gdx.gui.Container;
import com.gemserk.commons.gdx.gui.GuiControls;
import com.gemserk.commons.gdx.gui.Text;
import com.gemserk.componentsengine.input.InputDevicesMonitorImpl;
import com.gemserk.componentsengine.input.LibgdxInputMappingBuilder;
import com.gemserk.componentsengine.utils.Parameters;
import com.gemserk.componentsengine.utils.ParametersWrapper;
import com.gemserk.games.superflyingthing.Events;
import com.gemserk.games.superflyingthing.Game;
import com.gemserk.games.superflyingthing.Shape;
import com.gemserk.games.superflyingthing.ShipController;
import com.gemserk.games.superflyingthing.components.Components.GameData;
import com.gemserk.games.superflyingthing.components.Components.GameDataComponent;
import com.gemserk.games.superflyingthing.levels.Level;
import com.gemserk.games.superflyingthing.levels.Level.DestinationPlanet;
import com.gemserk.games.superflyingthing.levels.Level.LaserTurret;
import com.gemserk.games.superflyingthing.levels.Level.Obstacle;
import com.gemserk.games.superflyingthing.levels.Level.Portal;
import com.gemserk.games.superflyingthing.levels.Levels;
import com.gemserk.games.superflyingthing.preferences.GamePreferences;
import com.gemserk.games.superflyingthing.preferences.PlayerProfile;
import com.gemserk.games.superflyingthing.preferences.PlayerProfile.LevelInformation;
import com.gemserk.games.superflyingthing.scripts.AndroidController1Script;
import com.gemserk.games.superflyingthing.scripts.AndroidController2Script;
import com.gemserk.games.superflyingthing.scripts.AndroidController3Script;
import com.gemserk.games.superflyingthing.scripts.KeyboardController1Script;
import com.gemserk.games.superflyingthing.scripts.LaserGunScript;
import com.gemserk.games.superflyingthing.scripts.Scripts;
import com.gemserk.games.superflyingthing.scripts.Scripts.CameraScript;
import com.gemserk.games.superflyingthing.scripts.Scripts.DestinationPlanetScript;
import com.gemserk.games.superflyingthing.scripts.Scripts.StarScript;
import com.gemserk.games.superflyingthing.scripts.Scripts.StartPlanetScript;
import com.gemserk.games.superflyingthing.systems.ParticleEmitterSystem;
import com.gemserk.games.superflyingthing.systems.RenderLayerShapeImpl;
import com.gemserk.games.superflyingthing.systems.TagSystem;
import com.gemserk.games.superflyingthing.templates.EntityTemplates;
import com.gemserk.resources.ResourceManager;

public class PlayGameState extends GameStateImpl {

	private final Game game;
	SpriteBatch spriteBatch;
	Libgdx2dCamera worldCamera;

	EntityTemplates entityTemplates;
	World physicsWorld;
	Box2DCustomDebugRenderer box2dCustomDebugRenderer;
	ResourceManager<String> resourceManager;
	boolean resetPressed;
	boolean done;
	Container container;
	private Libgdx2dCamera guiCamera;
	private InputDevicesMonitorImpl<String> inputDevicesMonitor;

	EntityBuilder entityBuilder;
	private com.artemis.World world;
	private WorldWrapper worldWrapper;
	private EntityFactory entityFactory;
	private Parameters parameters;

	GameData gameData;
	private Text itemsTakenLabel;
	private EventManager eventManager;
	private JointBuilder jointBuilder;
	private Text timerLabel;

	private Animation finalMessageAnimation;
	private Animation levelNameAnimation;

	public void setResourceManager(ResourceManager<String> resourceManager) {
		this.resourceManager = resourceManager;
	}

	public PlayGameState(Game game) {
		this.game = game;
	}

	@Override
	public void init() {
		finalMessageAnimation = null;
		levelNameAnimation = null;

		resetPressed = false;
		spriteBatch = new SpriteBatch();

		eventManager = new EventManagerImpl();
		physicsWorld = new World(new Vector2(), false);

		jointBuilder = new JointBuilder(physicsWorld);

		worldCamera = new Libgdx2dCameraTransformImpl();
		worldCamera.center(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);

		Libgdx2dCamera backgroundLayerCamera = new Libgdx2dCameraTransformImpl();

		ArrayList<RenderLayer> renderLayers = new ArrayList<RenderLayer>();

		renderLayers.add(new RenderLayerSpriteBatchImpl(-1000, -100, backgroundLayerCamera));
		renderLayers.add(new RenderLayerShapeImpl(-100, -50, worldCamera));
		renderLayers.add(new RenderLayerSpriteBatchImpl(-50, 100, worldCamera));

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

		guiCamera = new Libgdx2dCameraTransformImpl();

		box2dCustomDebugRenderer = new Box2DCustomDebugRenderer((Libgdx2dCameraTransformImpl) worldCamera, physicsWorld);

		container = new Container();

		entityTemplates = new EntityTemplates(physicsWorld, world, resourceManager, entityBuilder, entityFactory);

		gameData = new GameData();
		GameInformation.gameData = gameData;

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

		Sprite backgroundSprite = resourceManager.getResourceValue("BackgroundSprite");
		entityTemplates.staticSprite(backgroundSprite, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), 0, -999, 0, 0, Color.WHITE);

		if (GameInformation.gameMode == GameInformation.ChallengeGameMode) {
			new ChallengeMode().create(this);
			Analytics.traker.trackPageView("/challenge/" + (GameInformation.level + 1) + "/start", "/challenge/" + (GameInformation.level + 1) + "/start", null);
		} else if (GameInformation.gameMode == GameInformation.PracticeGameMode) {
			new PracticeMode().create(this);
			Analytics.traker.trackPageView("/practice/start", "/practice/start", null);
		} else if (GameInformation.gameMode == GameInformation.RandomGameMode) {
			new RandomMode().create(this);
			Analytics.traker.trackPageView("/random/start", "/random/start", null);
		}

		final PlayerProfile playerProfile = game.getGamePreferences().getCurrentPlayerProfile();

		// entity with some game logic
		entityBuilder.component(new ScriptComponent(new ScriptJavaImpl() {

			boolean incrementTimer = true;

			@Override
			public void update(com.artemis.World world, Entity e) {

				if (incrementTimer)
					gameData.time += world.getDelta();

				Event event = eventManager.getEvent(Events.itemTaken);
				if (event != null) {
					gameData.currentItems++;
					itemsTakenLabel.setText(MessageFormat.format("{0}/{1}", gameData.currentItems, gameData.totalItems));
					eventManager.handled(event);
				}

				event = eventManager.getEvent(Events.destinationPlanetReached);
				if (event != null) {
					gameFinished();
					eventManager.handled(event);
					incrementTimer = false;

					if (GameInformation.gameMode == GameInformation.ChallengeGameMode) {

						// playerProfile.getLevelInformation(GameInformation.level + 1);
						// li.update(newTime, stars)

						playerProfile.setLevelInformationForLevel(GameInformation.level + 1, new LevelInformation(seconds(gameData.time), gameData.currentItems));
						game.getGamePreferences().updatePlayerProfile(playerProfile);
					}

				}

				event = eventManager.getEvent(Events.shipDeath);
				if (event != null) {
					eventManager.handled(event);
					// gameData.deaths++
				}

				timerLabel.setText("Time: " + seconds(gameData.time));
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
				monitorKeys("restart", Keys.MENU, Keys.R);
			}
		};

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

	class ChallengeMode {

		void loadLevel(EntityTemplates templates, Level level) {
			float worldWidth = level.w;
			float worldHeight = level.h;

			// float cameraZoom = Gdx.graphics.getWidth() * 48f / 800f;
			float cameraZoom = Gdx.graphics.getWidth() * level.zoom / 800f;

			final Camera camera = new CameraRestrictedImpl(0f, 0f, cameraZoom, 0f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), new Rectangle(0f, 0f, worldWidth, worldHeight));

			final ShipController controller = new ShipController();

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
					entityTemplates.movingObstacle(o.vertices, o.path, o.startPoint, o.x, o.y, o.angle * MathUtils.degreesToRadians);
				}
			}

			for (int i = 0; i < level.items.size(); i++) {
				Level.Item item = level.items.get(i);
				entityTemplates.star(item.x, item.y, new StarScript(eventManager));
			}

			for (int i = 0; i < level.laserTurrets.size(); i++) {
				LaserTurret laserTurret = level.laserTurrets.get(i);

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
				entityTemplates.portal(portal.id, portal.targetPortalId, portal.x, portal.y, new Scripts.PortalScript());
			}

			gameData.totalItems = level.items.size();
			if (gameData.totalItems > 0)
				itemsTakenLabel.setText(MessageFormat.format("{0}/{1}", gameData.currentItems, gameData.totalItems));

			createWorldLimits(worldWidth, worldHeight);

			if (Gdx.app.getType() == ApplicationType.Android)
				entityBuilder.component(new ScriptComponent(new AndroidController1Script(controller))).build();
			else
				entityBuilder.component(new ScriptComponent(new KeyboardController1Script(controller))).build();

			entityBuilder //
					.component(new GameDataComponent(null, startPlanet, cameraEntity)) //
					.component(new ScriptComponent(new Scripts.GameScript(eventManager, entityTemplates, entityFactory, gameData, controller, false))) //
					.build();

			BitmapFont font = resourceManager.getResourceValue("GameFont");

			Text levelNameText = GuiControls.label("Level " + (GameInformation.level + 1) + ": " + level.name) //
					.position(Gdx.graphics.getWidth() * 0.5f, Gdx.graphics.getHeight() * 0.75f) //
					.font(font) //
					.color(1f, 1f, 1f, 0f) //
					.build();

			levelNameAnimation = new SynchronizedAnimation(Builders.animation(Builders.timeline() //
					.value(Builders.timelineValue("color") //
							.keyFrame(0, new Color(1f, 1f, 1f, 0f)) //
							.keyFrame(250, Color.WHITE) //
							.keyFrame(750, Color.WHITE) //
							.keyFrame(1000, new Color(1f, 1f, 1f, 0f)) //
					)) //
					.delay(0f) //
					.speed(0.4f) //
					.started(true) //
					.build(), //
					new TimelineSynchronizer(new MutableObjectSynchronizer(), levelNameText));

			container.add(levelNameText);

		}

		void create(PlayGameState p) {

			if (Levels.hasLevel(GameInformation.level)) {
				Level level = Levels.level(GameInformation.level);
				loadLevel(entityTemplates, level);
			}

		}
	}

	class RandomMode {

		boolean insideObstacle;

		private final Shape[] shapes = new Shape[] { //
		new Shape(new Vector2[] { new Vector2(3f, 1.5f), new Vector2(1f, 4f), new Vector2(-2.5f, 1f), new Vector2(-1.5f, -2.5f), new Vector2(1f, -1.5f), }), //
				new Shape(new Vector2[] { new Vector2(1.5f, 0f), new Vector2(0.5f, 2f), new Vector2(-1.5f, 1f), new Vector2(-0.5f, -2.5f) }), //
				new Shape(new Vector2[] { new Vector2(2f, 1f), new Vector2(-3f, 1.2f), new Vector2(-2.5f, -0.8f), new Vector2(2.5f, -2f) }), //
		};

		private Shape getRandomShape() {
			return shapes[MathUtils.random(shapes.length - 1)];
		}

		void create(PlayGameState p) {
			World physicsWorld = p.physicsWorld;

			float worldWidth = MathUtils.random(30f, 150f);
			float worldHeight = MathUtils.random(10f, 20f);

			Gdx.app.log("SuperFlyingThing", "new world generated with size " + worldWidth + ", " + worldHeight);

			float cameraZoom = Gdx.graphics.getWidth() * 48f / 800f;

			final Camera camera = new CameraRestrictedImpl(0f, 0f, cameraZoom, 0f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), new Rectangle(0f, 0f, worldWidth, worldHeight));

			float obstacleX = 12f;

			while (obstacleX < worldWidth - 17f) {
				entityTemplates.obstacle(getRandomShape().vertices, obstacleX + 5f, MathUtils.random(0f, worldHeight), MathUtils.random(0f, 359f));
				entityTemplates.obstacle(getRandomShape().vertices, obstacleX, MathUtils.random(0f, worldHeight), MathUtils.random(0f, 359f));
				obstacleX += 8f;
			}

			int itemsCount = 0;

			for (int i = 0; i < 10; i++) {
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

			gameData.totalItems = itemsCount;
			itemsTakenLabel.setText(MessageFormat.format("{0}/{1}", gameData.currentItems, gameData.totalItems));

			Entity cameraEntity = entityTemplates.camera(camera, worldCamera, 5f, worldHeight * 0.5f, new CameraScript(eventManager));

			final ShipController controller = new ShipController();
			Entity startPlanet = entityTemplates.startPlanet(5f, worldHeight * 0.5f, 1f, controller, new StartPlanetScript(physicsWorld, jointBuilder, eventManager));

			entityTemplates.destinationPlanet(worldWidth - 5f, worldHeight * 0.5f, 1f, new DestinationPlanetScript(eventManager, jointBuilder));

			createWorldLimits(worldWidth, worldHeight, 0f);

			if (Gdx.app.getType() == ApplicationType.Android)
				entityBuilder.component(new ScriptComponent(new AndroidController1Script(controller))).build();
			else
				entityBuilder.component(new ScriptComponent(new KeyboardController1Script(controller))).build();

			entityBuilder //
					.component(new GameDataComponent(null, startPlanet, cameraEntity)) //
					.component(new ScriptComponent(new Scripts.GameScript(eventManager, entityTemplates, entityFactory, //
							gameData, controller, false))).build();

		}
	}

	class PracticeMode {

		boolean insideObstacle;

		private final Shape[] shapes = new Shape[] { //
		new Shape(new Vector2[] { new Vector2(3f, 1.5f), new Vector2(1f, 4f), new Vector2(-2.5f, 1f), new Vector2(-1.5f, -2.5f), new Vector2(1f, -1.5f), }), //
				new Shape(new Vector2[] { new Vector2(1.5f, 0f), new Vector2(0.5f, 2f), new Vector2(-1.5f, 1f), new Vector2(-0.5f, -2.5f) }), //
				new Shape(new Vector2[] { new Vector2(2f, 1f), new Vector2(-3f, 1.2f), new Vector2(-2.5f, -0.8f), new Vector2(2.5f, -2f) }), //
		};

		private Shape getRandomShape() {
			return shapes[MathUtils.random(shapes.length - 1)];
		}

		void create(PlayGameState p) {
			World physicsWorld = p.physicsWorld;

			p.entityTemplates = entityTemplates;

			float worldWidth = MathUtils.random(40f, 40f);
			float worldHeight = MathUtils.random(15f, 15f);

			Gdx.app.log("SuperFlyingThing", "new world generated with size " + worldWidth + ", " + worldHeight);

			float cameraZoom = Gdx.graphics.getWidth() * 48f / 800f;

			Camera camera = new CameraRestrictedImpl(0f, 0f, cameraZoom, 0f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), new Rectangle(0f, 0f, worldWidth, worldHeight));

			float obstacleX = 12f;

			while (obstacleX < worldWidth - 17f) {
				entityTemplates.obstacle(getRandomShape().vertices, obstacleX + 5f, MathUtils.random(0f, worldHeight), MathUtils.random(0f, 359f));
				entityTemplates.obstacle(getRandomShape().vertices, obstacleX, MathUtils.random(0f, worldHeight), MathUtils.random(0f, 359f));
				obstacleX += 8f;
			}

			int itemsCount = 0;

			for (int i = 0; i < 10; i++) {
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

			gameData.totalItems = itemsCount;
			itemsTakenLabel.setText(MessageFormat.format("{0}/{1}", gameData.currentItems, gameData.totalItems));

			Entity cameraEntity = entityTemplates.camera(camera, worldCamera, 5f, worldHeight * 0.5f, new CameraScript(eventManager));
			final ShipController controller = new ShipController();
			Entity startPlanet = entityTemplates.startPlanet(5f, worldHeight * 0.5f, 1f, controller, new StartPlanetScript(physicsWorld, jointBuilder, eventManager));

			entityTemplates.destinationPlanet(worldWidth - 5f, worldHeight * 0.5f, 1f, new DestinationPlanetScript(eventManager, jointBuilder));

			createWorldLimits(worldWidth, worldHeight, 0f);

			if (Gdx.app.getType() == ApplicationType.Android)
				entityBuilder.component(new ScriptComponent(new AndroidController3Script(controller))).build();
			else
				entityBuilder.component(new ScriptComponent(new AndroidController2Script(controller))).build();
//				entityBuilder.component(new ScriptComponent(new KeyboardController1Script(controller))).build();

			entityBuilder //
					.component(new GameDataComponent(null, startPlanet, cameraEntity)) //
					.component(new ScriptComponent(new Scripts.GameScript(eventManager, entityTemplates, entityFactory, //
							gameData, controller, true))).build();

		}
	}

	private void gameFinished() {
		BitmapFont font = resourceManager.getResourceValue("GameFont");

		Text message = GuiControls.label("Great Job!").position(Gdx.graphics.getWidth() * 0.5f, Gdx.graphics.getHeight() * 0.8f) //
				.font(font) //
				.color(1f, 1f, 1f, 0f) //
				.build();

		finalMessageAnimation = new SynchronizedAnimation(Builders.animation(Builders.timeline() //
				.value(Builders.timelineValue("color") //
						.keyFrame(0, new Color(1f, 1f, 1f, 0f)) //
						.keyFrame(250, Color.WHITE) //
						.keyFrame(750, Color.WHITE) //
						.keyFrame(1000, new Color(1f, 1f, 1f, 0f)) //
				)) //
				.delay(0f) //
				.speed(0.4f) //
				.started(true) //
				.build(), //
				new TimelineSynchronizer(new MutableObjectSynchronizer(), message));

		container.add(message);

		if (GameInformation.gameMode == GameInformation.ChallengeGameMode) {
			Analytics.traker.trackPageView("/challenge/" + (GameInformation.level + 1) + "/finish", "/challenge/" + (GameInformation.level + 1) + "/finish", null);
		} else if (GameInformation.gameMode == GameInformation.PracticeGameMode) {
			Analytics.traker.trackPageView("/practice/finish", "/practice/finish", null);
		} else if (GameInformation.gameMode == GameInformation.RandomGameMode) {
			Analytics.traker.trackPageView("/random/finish", "/random/finish", null);
		}

	}

	@Override
	public void render(int delta) {
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
	public void update(int delta) {

		GamePreferences gamePreferences = game.getGamePreferences();
		if (gamePreferences.isTutorialEnabled()) {
			// gamePreferences.setTutorialEnabled(false);
			game.transition(game.getInstructionsScreen(), 0, 300, false);
			return;
		}

		inputDevicesMonitor.update();
		Synchronizers.synchronize(delta);
		container.update();

		if (finalMessageAnimation != null) {
			finalMessageAnimation.update(delta);
			done = finalMessageAnimation.isFinished();
		}

		if (levelNameAnimation != null) {
			levelNameAnimation.update(delta);
		}

		if (inputDevicesMonitor.getButton("restart").isReleased())
			done = true;

		if (inputDevicesMonitor.getButton("pause").isReleased())
			game.transition(game.getPauseScreen(), 200, 300, false);

		if (done) {
			if (GameInformation.gameMode == GameInformation.ChallengeGameMode) {
				game.transition(game.getGameOverScreen(), 0, 300, false);
			} else {
				game.transition(game.getGameOverScreen(), 0, 300, false);
			}
		}

		worldWrapper.update(delta);
	}

	@Override
	public void resume() {
		// automatically handled in Game class and if no previous screen, then don't handle it (or system.exit())
		game.getAdWhirlViewHandler().hide();
		super.resume();
		Gdx.input.setCatchBackKey(true);
		game.getBackgroundGameScreen().dispose();
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