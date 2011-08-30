package com.gemserk.games.superflyingthing.gamestates;

import java.text.MessageFormat;

import com.artemis.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.gemserk.analytics.Analytics;
import com.gemserk.animation4j.transitions.sync.Synchronizers;
import com.gemserk.commons.artemis.EntityBuilder;
import com.gemserk.commons.artemis.WorldWrapper;
import com.gemserk.commons.artemis.components.ScriptComponent;
import com.gemserk.commons.artemis.components.TagComponent;
import com.gemserk.commons.artemis.events.Event;
import com.gemserk.commons.artemis.events.EventManager;
import com.gemserk.commons.artemis.events.EventManagerImpl;
import com.gemserk.commons.artemis.events.reflection.Handles;
import com.gemserk.commons.artemis.render.RenderLayers;
import com.gemserk.commons.artemis.scripts.EventSystemScript;
import com.gemserk.commons.artemis.scripts.ScriptJavaImpl;
import com.gemserk.commons.artemis.systems.ContainerSystem;
import com.gemserk.commons.artemis.systems.OwnerSystem;
import com.gemserk.commons.artemis.systems.PhysicsSystem;
import com.gemserk.commons.artemis.systems.ReflectionRegistratorEventSystem;
import com.gemserk.commons.artemis.systems.RenderLayerSpriteBatchImpl;
import com.gemserk.commons.artemis.systems.RenderableSystem;
import com.gemserk.commons.artemis.systems.ScriptSystem;
import com.gemserk.commons.artemis.systems.SpriteUpdateSystem;
import com.gemserk.commons.artemis.systems.TagSystem;
import com.gemserk.commons.artemis.templates.EntityFactory;
import com.gemserk.commons.artemis.templates.EntityFactoryImpl;
import com.gemserk.commons.artemis.templates.EntityTemplate;
import com.gemserk.commons.gdx.GameStateImpl;
import com.gemserk.commons.gdx.box2d.Box2DCustomDebugRenderer;
import com.gemserk.commons.gdx.camera.Camera;
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
import com.gemserk.games.superflyingthing.Events;
import com.gemserk.games.superflyingthing.Game;
import com.gemserk.games.superflyingthing.Layers;
import com.gemserk.games.superflyingthing.Screens;
import com.gemserk.games.superflyingthing.ShipController;
import com.gemserk.games.superflyingthing.components.ComponentWrapper;
import com.gemserk.games.superflyingthing.components.Components.CameraComponent;
import com.gemserk.games.superflyingthing.components.Components.ControllerComponent;
import com.gemserk.games.superflyingthing.components.Components.GameData;
import com.gemserk.games.superflyingthing.components.Components.GameDataComponent;
import com.gemserk.games.superflyingthing.components.Components.ReplayListComponent;
import com.gemserk.games.superflyingthing.components.ReplayList;
import com.gemserk.games.superflyingthing.levels.Level;
import com.gemserk.games.superflyingthing.levels.Levels;
import com.gemserk.games.superflyingthing.levels.RandomLevelGenerator;
import com.gemserk.games.superflyingthing.preferences.GamePreferences;
import com.gemserk.games.superflyingthing.preferences.PlayerProfile;
import com.gemserk.games.superflyingthing.preferences.PlayerProfile.LevelInformation;
import com.gemserk.games.superflyingthing.scripts.ReplayRecorderScript;
import com.gemserk.games.superflyingthing.scripts.Scripts;
import com.gemserk.games.superflyingthing.scripts.controllers.ControllerType;
import com.gemserk.games.superflyingthing.systems.ParticleEmitterSystem;
import com.gemserk.games.superflyingthing.systems.RenderLayerParticleEmitterImpl;
import com.gemserk.games.superflyingthing.systems.RenderLayerShapeImpl;
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
	private Text timerLabel;

	private EventManager eventManager;

	EntityTemplate userMessageTemplate;

	private GamePreferences gamePreferences;

	private Text tiltvalue;
	private final boolean tiltValueEnabled = false;
	int tilttime = 0;

	private boolean shouldDisposeWorldWrapper;

	public void setResourceManager(ResourceManager<String> resourceManager) {
		this.resourceManager = resourceManager;
	}

	public void setGamePreferences(GamePreferences gamePreferences) {
		this.gamePreferences = gamePreferences;
	}

	public PlayGameState(Game game) {
		this.game = game;
	}

	@Handles
	public void toggleFirstBackground(Event e) {
		if (renderLayers != null)
			renderLayers.toggle(Layers.FirstBackground);
	}

	@Handles
	public void toggleSecondBackground(Event e) {
		if (renderLayers != null)
			renderLayers.toggle(Layers.SecondBackground);
	}

	@Override
	public void init() {
		shouldDisposeWorldWrapper = true;

		spriteBatch = new SpriteBatch();

		eventManager = new EventManagerImpl();

		physicsWorld = new World(new Vector2(), false);

		worldCamera = new Libgdx2dCameraTransformImpl();
		worldCamera.center(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);

		guiCamera = new Libgdx2dCameraTransformImpl();

		Libgdx2dCamera backgroundLayerCamera = new Libgdx2dCameraTransformImpl();
		final Libgdx2dCamera secondBackgroundLayerCamera = new Libgdx2dCameraTransformImpl();
		secondBackgroundLayerCamera.center(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);

		renderLayers = new RenderLayers();

		renderLayers.add(Layers.FirstBackground, new RenderLayerSpriteBatchImpl(-10000, -500, backgroundLayerCamera), game.getGamePreferences().isFirstBackgroundEnabled());
		renderLayers.add(Layers.SecondBackground, new RenderLayerSpriteBatchImpl(-500, -100, secondBackgroundLayerCamera), game.getGamePreferences().isSecondBackgroundEnabled());
		renderLayers.add(Layers.StaticObstacles, new RenderLayerShapeImpl(-100, -50, worldCamera));
		renderLayers.add(Layers.World, new RenderLayerSpriteBatchImpl(-50, 100, worldCamera));
		renderLayers.add(Layers.Explosions, new RenderLayerParticleEmitterImpl(100, 200, worldCamera));

		// used by the controllers to draw stuff, could be removed later
		renderLayers.add(Layers.Controllers, new RenderLayerSpriteBatchImpl(200, 10000, guiCamera));

		world = new com.artemis.World();
		entityFactory = new EntityFactoryImpl(world);
		worldWrapper = new WorldWrapper(world);
		parameters = new ParametersWrapper();
		// add render and all stuff...

		worldWrapper.addUpdateSystem(new PhysicsSystem(physicsWorld));
		worldWrapper.addUpdateSystem(new ScriptSystem());
		worldWrapper.addUpdateSystem(new TagSystem());
		worldWrapper.addUpdateSystem(new ContainerSystem());
		worldWrapper.addUpdateSystem(new OwnerSystem());

		// testing event listener auto registration using reflection
		worldWrapper.addUpdateSystem(new ReflectionRegistratorEventSystem(eventManager));

		worldWrapper.addRenderSystem(new SpriteUpdateSystem());
		worldWrapper.addRenderSystem(new RenderableSystem(renderLayers));
		worldWrapper.addRenderSystem(new ParticleEmitterSystem());

		worldWrapper.init();

		entityBuilder = new EntityBuilder(world);

		box2dCustomDebugRenderer = new Box2DCustomDebugRenderer((Libgdx2dCameraTransformImpl) worldCamera, physicsWorld);

		container = new Container();

		entityTemplates = new EntityTemplates(physicsWorld, world, resourceManager, entityBuilder, entityFactory, eventManager);

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

		entityFactory.instantiate(entityTemplates.getStaticSpriteTemplate(), parameters //
				.put("color", Color.WHITE) //
				.put("layer", (-999)) //
				.put("spatial", new SpatialImpl(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), 0)) //
				.put("center", new Vector2(0, 0)) //
				.put("spriteId", "BackgroundSprite") //
				);

		if (GameInformation.gameMode == GameInformation.ChallengeGameMode) {
			level = loadLevelForChallengeMode();

			// play game state custom

			entityBuilder //
					.component(new TagComponent(Groups.NormalGameModeLogic)) //
					.component(new GameDataComponent()) //
					.component(new ScriptComponent(new Scripts.GameScript(eventManager, entityTemplates, entityFactory, gameData, false))) //
					.build();

			Analytics.traker.trackPageView("/challenge/" + (GameInformation.level + 1) + "/start", "/challenge/" + (GameInformation.level + 1) + "/start", null);
		} else if (GameInformation.gameMode == GameInformation.PracticeGameMode) {
			level = loadRandomLevelForRandomMode();

			entityBuilder //
					.component(new TagComponent(Groups.NormalGameModeLogic)) //
					.component(new GameDataComponent()) //
					.component(new ScriptComponent(new Scripts.GameScript(eventManager, entityTemplates, entityFactory, gameData, true))) //
					.build();

			Analytics.traker.trackPageView("/practice/start", "/practice/start", null);
		} else if (GameInformation.gameMode == GameInformation.RandomGameMode) {
			level = loadRandomLevelForRandomMode();

			entityBuilder //
					.component(new TagComponent(Groups.NormalGameModeLogic)) //
					.component(new GameDataComponent()) //
					.component(new ScriptComponent(new Scripts.GameScript(eventManager, entityTemplates, entityFactory, gameData, false))) //
					.build();

			Analytics.traker.trackPageView("/random/start", "/random/start", null);
		}

		final PlayerProfile playerProfile = game.getGamePreferences().getCurrentPlayerProfile();

		// creates controller the first time if no controller was created before...
		entityBuilder //
				.component(new ScriptComponent(new ScriptJavaImpl() {

					@Handles(ids = Events.gameStarted)
					public void createControllerWhenGameStarts(Event event) {
						Entity playerController = world.getTagManager().getEntity(Groups.PlayerController);
						if (playerController != null) {
							ControllerComponent controllerComponent = playerController.getComponent(ControllerComponent.class);
							// mark current controller to be deleted
							playerController.delete();
							// creates a new controller using new preferences
							createGameController(controllerComponent.getController());
						}
					}

				})) //
				.build();

		entityBuilder //
				.component(new TagComponent("EventManager")) //
				.component(new ScriptComponent(new EventSystemScript(eventManager))) //
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
				if (playerController != null)
					playerController.delete();

				Entity controllerSwitcher = world.getTagManager().getEntity(Groups.ControllerSwitcher);
				if (controllerSwitcher != null)
					controllerSwitcher.delete();

				Entity gameMode = world.getTagManager().getEntity(Groups.NormalGameModeLogic);
				gameMode.delete();

				parameters.clear();
				entityFactory.instantiate(entityTemplates.getTimerTemplate(), parameters //
						.put("time", 2.5f) //
						.put("eventId", Events.gameOver) //
						);
			}

			@Handles
			public void gameOver(Event e) {

				// game is over, move to replay screen

				// game.transition(game.getGameOverScreen(), 0, 300, false);

				Entity replayRecorder = world.getTagManager().getEntity(Groups.ReplayRecorder);

				if (!game.getGamePreferences().isShowReplay()) {

					shouldDisposeWorldWrapper = false;
					game.getGameData().put("worldWrapper", worldWrapper);
					game.transition(Screens.GameOver)
					// .disposeCurrent() //
							.start();

					return;
				}

				ReplayListComponent replayListComponent = replayRecorder.getComponent(ReplayListComponent.class);

				game.getGameData().put("replayList", replayListComponent.getReplayList());
				game.getGameData().put("level", level);
				// game.getGameData().put("worldWrapper", worldWrapper);

				game.transition(Screens.ReplayPlayer) //
						.leaveTime(0) //
						.enterTime(300) //
						.disposeCurrent() //
						.start();

			}

			private float seconds = -1;
			private StringBuilder timerLabelBuilder = new StringBuilder();

			@Override
			public void update(com.artemis.World world, Entity e) {

				if (incrementTimer)
					gameData.time += getDelta();

				if (seconds == seconds(gameData.time))
					return;

				timerLabelBuilder.delete(6, timerLabelBuilder.length());
				timerLabelBuilder.append(seconds(gameData.time));

				timerLabel.setText(timerLabelBuilder);

				seconds = seconds(gameData.time);
			}

			private int seconds(float seconds) {
				return (int) seconds;
			}

		})).build();

		inputDevicesMonitor = new InputDevicesMonitorImpl<String>();
		new LibgdxInputMappingBuilder<String>(inputDevicesMonitor, Gdx.input) {
			{
				monitorKeys("pause", Keys.BACK, Keys.ESCAPE);
				monitorKeys("switchControls", Keys.MENU, Keys.R);
			}
		};

		// entityBuilder //
		// .component(new TagComponent(Groups.ControllerSwitcher)) //
		// .component(new ScriptComponent(new ScriptJavaImpl() {
		//
		// private int current = 0;
		// private ShipController controller;
		//
		// private ControllerType[] controllerTypes = new ControllerType[] { ControllerType.KeyboardController, //
		// ControllerType.AnalogKeyboardController, ControllerType.ClassicController, ControllerType.AxisController, //
		// ControllerType.AnalogController, ControllerType.TiltController, ControllerType.TargetController };
		//
		// @Override
		// public void update(com.artemis.World world, Entity e) {
		//
		// Entity currentController = world.getTagManager().getEntity(Groups.PlayerController);
		//
		// if (currentController != null) {
		// if (controller == null)
		// controller = currentController.getComponent(ControllerComponent.class).getController();
		// }
		//
		// if (currentController == null) {
		//
		// current++;
		// if (current >= controllerTypes.length)
		// current = 0;
		//
		// ControllerType controllerType = controllerTypes[current];
		// String controllerName = controllerType.name();
		//
		// parameters.clear();
		// parameters.put("controller", controller);
		// entityFactory.instantiate(controllerTemplates.getControllerTemplate(controllerType), parameters);
		//
		// Gdx.app.log("SuperFlyingThing", "Changing controller to " + controllerName);
		// parameters.clear();
		// parameters.put("position", new Vector2(Gdx.graphics.getWidth() * 0.5f, Gdx.graphics.getHeight() * 0.8f));
		// parameters.put("text", controllerName);
		// parameters.put("time", 1000);
		// entityFactory.instantiate(userMessageTemplate, parameters);
		// }
		//
		// if (inputDevicesMonitor.getButton("switchControls").isReleased())
		// currentController.delete();
		//
		// }
		// })).build();

		entityBuilder //
				.component(new TagComponent(Groups.ReplayRecorder)) //
				.component(new ReplayListComponent(new ReplayList())) //
				.component(new ScriptComponent(new ReplayRecorderScript())) //
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

		// creates a new particle emitter spawner template which creates a new explosion when the ship dies.
		entityFactory.instantiate(entityTemplates.getParticleEmitterSpawnerTemplate());

	}

	Level loadLevelForChallengeMode() {
		if (Levels.hasLevel(GameInformation.level)) {
			Level level = Levels.level(GameInformation.level);
			new LevelLoader(entityTemplates, entityFactory, physicsWorld, worldCamera, false).loadLevel(level);

			gameData.totalItems = level.items.size();
			if (gameData.totalItems > 0)
				itemsTakenLabel.setText(MessageFormat.format("{0}/{1}", gameData.currentItems, gameData.totalItems));

			return level;
		}
		return null;
	}

	Level loadRandomLevelForRandomMode() {
		RandomLevelGenerator randomLevelGenerator = new RandomLevelGenerator();

		Level level = randomLevelGenerator.generateRandomLevel();

		new LevelLoader(entityTemplates, entityFactory, physicsWorld, worldCamera, true).loadLevel(level);

		// int starsCount = randomLevelGenerator.generateStars(level.w, level.h, 10);

		gameData.totalItems = level.items.size();
		if (gameData.totalItems > 0)
			itemsTakenLabel.setText(MessageFormat.format("{0}/{1}", gameData.currentItems, gameData.totalItems));

		return level;
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
	private RenderLayers renderLayers;
	private Level level;

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

		// GamePreferences gamePreferences = game.getGamePreferences();
		// if (gamePreferences.isTutorialEnabled()) {
		// game.transition(game.getInstructionsScreen()) //
		// .leaveTime(0) //
		// .enterTime(300) //
		// .disposeCurrent(false) //
		// .start();
		// return;
		// }

		inputDevicesMonitor.update();
		Synchronizers.synchronize(getDelta());
		container.update();

		if (inputDevicesMonitor.getButton("pause").isReleased())
			game.transition(Screens.Pause) //
					.disposeCurrent(false) //
					.start();

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
		if (shouldDisposeWorldWrapper)
			worldWrapper.dispose();
		spriteBatch.dispose();
	}

}
