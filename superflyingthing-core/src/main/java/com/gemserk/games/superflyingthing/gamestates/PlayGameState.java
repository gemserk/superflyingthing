package com.gemserk.games.superflyingthing.gamestates;

import org.w3c.dom.Document;

import com.artemis.Entity;
import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.gemserk.analytics.Analytics;
import com.gemserk.animation4j.transitions.sync.Synchronizers;
import com.gemserk.commons.artemis.EntityBuilder;
import com.gemserk.commons.artemis.WorldWrapper;
import com.gemserk.commons.artemis.components.ScriptComponent;
import com.gemserk.commons.artemis.events.Event;
import com.gemserk.commons.artemis.events.reflection.Handles;
import com.gemserk.commons.artemis.render.RenderLayers;
import com.gemserk.commons.artemis.scripts.ScriptJavaImpl;
import com.gemserk.commons.artemis.templates.EntityFactory;
import com.gemserk.commons.artemis.templates.EntityTemplate;
import com.gemserk.commons.gdx.AverageFPS;
import com.gemserk.commons.gdx.GameStateImpl;
import com.gemserk.commons.gdx.audio.SoundPlayer;
import com.gemserk.commons.gdx.gui.Container;
import com.gemserk.commons.gdx.gui.GuiControls;
import com.gemserk.commons.gdx.time.TimeStepProviderGameStateImpl;
import com.gemserk.commons.reflection.Injector;
import com.gemserk.commons.reflection.InjectorImpl;
import com.gemserk.componentsengine.input.InputDevicesMonitorImpl;
import com.gemserk.componentsengine.input.LibgdxInputMappingBuilder;
import com.gemserk.componentsengine.utils.Parameters;
import com.gemserk.componentsengine.utils.ParametersWrapper;
import com.gemserk.games.superflyingthing.Events;
import com.gemserk.games.superflyingthing.Game;
import com.gemserk.games.superflyingthing.Layers;
import com.gemserk.games.superflyingthing.Screens;
import com.gemserk.games.superflyingthing.ShipController;
import com.gemserk.games.superflyingthing.components.Components.ControllerComponent;
import com.gemserk.games.superflyingthing.components.Components.GameData;
import com.gemserk.games.superflyingthing.components.Components.ReplayListComponent;
import com.gemserk.games.superflyingthing.entities.Tags;
import com.gemserk.games.superflyingthing.levels.Level;
import com.gemserk.games.superflyingthing.levels.Levels;
import com.gemserk.games.superflyingthing.levels.RandomLevelGenerator;
import com.gemserk.games.superflyingthing.preferences.GamePreferences;
import com.gemserk.games.superflyingthing.preferences.PlayerProfile;
import com.gemserk.games.superflyingthing.preferences.PlayerProfile.LevelInformation;
import com.gemserk.games.superflyingthing.resources.GameResources;
import com.gemserk.games.superflyingthing.scenes.NormalModeSceneTemplate;
import com.gemserk.games.superflyingthing.scenes.SceneTemplate;
import com.gemserk.games.superflyingthing.scripts.CountTravelTimeScript;
import com.gemserk.games.superflyingthing.scripts.controllers.ControllerType;
import com.gemserk.games.superflyingthing.templates.ControllerTemplates;
import com.gemserk.games.superflyingthing.templates.EntityTemplates;
import com.gemserk.games.superflyingthing.templates.UserMessageTemplate;
import com.gemserk.resources.Resource;
import com.gemserk.resources.ResourceManager;

public class PlayGameState extends GameStateImpl {

	private static class Hud {

		public static final String Screen = "Screen";

		public static final String LeftButton = "LeftButton";
		public static final String RightButton = "RightButton";

	}

	Game game;
	ResourceManager<String> resourceManager;
	GamePreferences gamePreferences;
	SoundPlayer soundPlayer;

	private SpriteBatch spriteBatch;

	// private Box2DCustomDebugRenderer box2dCustomDebugRenderer;
	private InputDevicesMonitorImpl<String> inputDevicesMonitor;

	private EntityTemplates entityTemplates;
	private ControllerTemplates controllerTemplates;

	EntityBuilder entityBuilder;
	private WorldWrapper worldWrapper;
	private EntityFactory entityFactory;
	private Parameters parameters = new ParametersWrapper();

	GameData gameData;

	EntityTemplate userMessageTemplate;

	private boolean shouldDisposeWorldWrapper;

	private static String[] endMessages = new String[] { "Great Job!", "Nicely Done!", "You made it!", "Good Work!", "You Rock!", };
	private Level level;
	private Integer levelNumber;
	private AverageFPS averageFPS;
	private Injector injector;

	RenderLayers renderLayers;

	Container screen;

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
		averageFPS = new AverageFPS();
		createALotOfStuff();
		createWorld();

		screen = new Container(Hud.Screen);

		float scale = Gdx.graphics.getHeight() / 480f;

		if (Gdx.graphics.getHeight() > 480f)
			scale = 1f;

		if (Gdx.app.getType() == ApplicationType.Android) {
			Sprite leftButtonSprite = resourceManager.getResourceValue(GameResources.Sprites.LeftButton);

			screen.add(GuiControls.imageButton(leftButtonSprite) //
					.id(Hud.LeftButton) //
					.center(0f, 0f) //
					.size(leftButtonSprite.getWidth() * scale, leftButtonSprite.getHeight() * scale) //
					.position(10f * scale, 10f * scale) //
					.build());

			Sprite rightButtonSprite = resourceManager.getResourceValue(GameResources.Sprites.RightButton);

			screen.add(GuiControls.imageButton(rightButtonSprite) //
					.id(Hud.RightButton) //
					.center(1f, 0f) //
					.size(leftButtonSprite.getWidth() * scale, leftButtonSprite.getHeight() * scale) //
					.position(Gdx.graphics.getWidth() - 10f * scale, 10f * scale) //
					.build());
		}

	}

	private void createALotOfStuff() {

		levelNumber = getParameters().get("level", 1);

		shouldDisposeWorldWrapper = true;

		spriteBatch = new SpriteBatch();

		injector = new InjectorImpl();

		injector.bind("resourceManager", resourceManager);
		injector.bind("timeStepProvider", new TimeStepProviderGameStateImpl(this));
		injector.bind("gamePreferences", gamePreferences);
		injector.bind("soundPlayer", soundPlayer);

		gameData = new GameData();
		GameInformation.gameData = gameData;

		if (GameInformation.gameMode == GameInformation.ChallengeGameMode) {
			level = loadLevelForChallengeMode();
			Analytics.traker.trackPageView("/challenge/" + levelNumber + "/start", "/challenge/" + levelNumber + "/start", null);
		} else if (GameInformation.gameMode == GameInformation.PracticeGameMode) {
			level = loadRandomLevelForRandomMode();
			Analytics.traker.trackPageView("/practice/start", "/practice/start", null);
		} else if (GameInformation.gameMode == GameInformation.RandomGameMode) {
			level = loadRandomLevelForRandomMode();
			Analytics.traker.trackPageView("/random/start", "/random/start", null);
		}

		// gameData.totalItems = level.items.size();

		PlayerProfile playerProfile = gamePreferences.getCurrentPlayerProfile();

		worldWrapper = new WorldWrapper(new com.artemis.World());

		SceneTemplate sceneTemplate = injector.getInstance(NormalModeSceneTemplate.class);
		sceneTemplate.getParameters().put("gameData", gameData);

		if (playerProfile.hasPlayedLevel(levelNumber))
			sceneTemplate.getParameters().put("bestTime", playerProfile.getLevelInformation(levelNumber).time);

		sceneTemplate.getParameters().put("level", level);
		sceneTemplate.getParameters().put("backgroundEnabled", game.getGamePreferences().isFirstBackgroundEnabled());
		sceneTemplate.getParameters().put("shouldRemoveItems", GameInformation.gameMode != GameInformation.ChallengeGameMode);
		sceneTemplate.apply(worldWrapper);

		injector.injectMembers(this);

		// creates and registers all the controller templates
		controllerTemplates = new ControllerTemplates();
		controllerTemplates.keyboardControllerTemplate = new ControllerTemplates.KeyboardControllerTemplate();
		controllerTemplates.androidClassicControllerTemplate = new ControllerTemplates.AndroidClassicControllerTemplate();
		controllerTemplates.axisControllerTemplate = new ControllerTemplates.AxisControllerTemplate(resourceManager);
		controllerTemplates.analogControllerTemplate = new ControllerTemplates.AnalogControllerTemplate(resourceManager);
		controllerTemplates.tiltAndroidControllerTemplate = new ControllerTemplates.TiltAndroidControllerTemplate();
		controllerTemplates.analogKeyboardControllerTemplate = new ControllerTemplates.AnalogKeyboardControllerTemplate();
		controllerTemplates.targetControllerTemplate = new ControllerTemplates.TargetControllerTemplate();
		controllerTemplates.remoteClassicControllerTemplate = new ControllerTemplates.RemoteClassicControllerTemplate();

		userMessageTemplate = injector.getInstance(UserMessageTemplate.class);

		inputDevicesMonitor = new InputDevicesMonitorImpl<String>();
		new LibgdxInputMappingBuilder<String>(inputDevicesMonitor, Gdx.input) {
			{
				monitorKeys("pause", Keys.BACK, Keys.ESCAPE);
				monitorKeys("switchControls", Keys.MENU, Keys.R);
				monitorKeys("showCustomizeControls", Keys.NUM_5);
			}
		};

	}

	private void createWorld() {
		final PlayerProfile playerProfile = game.getGamePreferences().getCurrentPlayerProfile();

		// creates controller the first time if no controller was created before...
		entityBuilder //
				.component(new ScriptComponent(new ScriptJavaImpl() {

					private com.artemis.World world;

					@Override
					public void init(com.artemis.World world, Entity e) {
						this.world = world;
					}

					@Handles(ids = Events.gameStarted)
					public void createControllerWhenGameStarts(Event event) {
						Entity playerController = world.getTagManager().getEntity(Tags.PlayerController);
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

		// entity with some game logic
		entityBuilder.component(new ScriptComponent(new CountTravelTimeScript(gameData), new ScriptJavaImpl() {

			private com.artemis.World world;
			private Parameters parameters = new ParametersWrapper();

			@Override
			public void init(com.artemis.World world, Entity e) {
				this.world = world;
			}

			@Handles(ids = Events.shipDeath)
			public void shipDeath(Event e) {
				gameData.deaths++;
			}

			@Handles
			public void itemTaken(Event e) {
				gameData.currentItems++;
			}

			@Handles(ids = Events.gameStarted)
			public void gameStarted(Event e) {
				if (GameInformation.gameMode != GameInformation.ChallengeGameMode)
					return;
				parameters.clear();
				parameters.put("position", new Vector2(Gdx.graphics.getWidth() * 0.5f, Gdx.graphics.getHeight() * 0.8f));
				parameters.put("text", "Level " + levelNumber + ": " + level.name);
				entityFactory.instantiate(userMessageTemplate, parameters);
			}

			@Handles
			public void gameFinished(Event e) {

				PlayGameState.this.gameFinished();
				if (GameInformation.gameMode == GameInformation.ChallengeGameMode) {
					playerProfile.setLevelInformationForLevel(levelNumber, new LevelInformation(gameData.travelTime, gameData.currentItems));
					game.getGamePreferences().updatePlayerProfile(playerProfile);
				}

				Entity playerController = world.getTagManager().getEntity(Tags.PlayerController);
				if (playerController != null)
					playerController.delete();

				Entity controllerSwitcher = world.getTagManager().getEntity(Tags.ControllerSwitcher);
				if (controllerSwitcher != null)
					controllerSwitcher.delete();

				Entity gameMode = world.getTagManager().getEntity(Tags.NormalGameModeLogic);
				gameMode.delete();

				parameters.clear();
				entityFactory.instantiate(entityTemplates.timerTemplate, parameters //
						.put("time", 2f) //
						.put("eventId", Events.gameOver) //
						);
			}

			@Handles
			public void gameOver(Event e) {

				// game is over, move to replay screen

				// game.transition(game.getGameOverScreen(), 0, 300, false);

				Entity replayRecorder = world.getTagManager().getEntity(Tags.ReplayRecorder);

				game.getGameOverScreen().getParameters().put("level", levelNumber);

				if (!game.getGamePreferences().isShowReplay()) {

					shouldDisposeWorldWrapper = false;
					game.transition(Screens.GameOver) //
							.parameter("worldWrapper", worldWrapper) //
							.start();

					return;
				}

				ReplayListComponent replayListComponent = replayRecorder.getComponent(ReplayListComponent.class);

				game.transition(Screens.ReplayPlayer) //
						.leaveTime(0) //
						.enterTime(300) //
						.parameter("replayList", replayListComponent.getReplayList()) //
						.parameter("level", level) //
						.disposeCurrent() //
						.start();

			}

		})).build();

	}

	Level loadLevelForChallengeMode() {
		if (Levels.hasLevel(levelNumber)) {
			// Level level = Levels.level(levelNumber);

			Resource<Level> levelResource = resourceManager.get(Levels.levelId(levelNumber));
			Level level = levelResource.get();

			return level;
		}
		return null;
	}

	Level loadRandomLevelForRandomMode() {
		Resource<Document> resource = resourceManager.get("RandomLevelTilesDocument");

		RandomLevelGenerator randomLevelGenerator = new RandomLevelGenerator(resource.get());

		Level level = randomLevelGenerator.generateRandomLevel();

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

		entityFactory.instantiate(userMessageTemplate, new ParametersWrapper() //
				.put("position", new Vector2(Gdx.graphics.getWidth() * 0.5f, Gdx.graphics.getHeight() * 0.8f)) //
				.put("text", getRandomEndMessage()) //
				);

		gameData.averageFPS = averageFPS.getFPS();

		if (GameInformation.gameMode == GameInformation.ChallengeGameMode) {
			Analytics.traker.trackPageView("/challenge/" + levelNumber + "/finish", "/challenge/" + levelNumber + "/finish", null);
		} else if (GameInformation.gameMode == GameInformation.PracticeGameMode) {
			Analytics.traker.trackPageView("/practice/finish", "/practice/finish", null);
		} else if (GameInformation.gameMode == GameInformation.RandomGameMode) {
			Analytics.traker.trackPageView("/random/finish", "/random/finish", null);
		}
	}

	private String getRandomEndMessage() {
		return endMessages[MathUtils.random(endMessages.length - 1)];
	}

	@Override
	public void render() {
		Gdx.graphics.getGL10().glClear(GL10.GL_COLOR_BUFFER_BIT);

		worldWrapper.render();

		spriteBatch.begin();
		screen.draw(spriteBatch);
		spriteBatch.end();

		// if (Game.isShowBox2dDebug())
		// box2dCustomDebugRenderer.render();

	}

	@Override
	public void update() {
		averageFPS.update();

		screen.update();
		inputDevicesMonitor.update();

		Synchronizers.synchronize(getDelta());

		if (inputDevicesMonitor.getButton("pause").isReleased()) {
			gameData.averageFPS = averageFPS.getFPS();
			game.transition(Screens.Pause) //
					.disposeCurrent(false) //
					.parameter("level", levelNumber) //
					.start();
			Gdx.app.log("SuperFlyingThing", "Pausing level " + levelNumber);
		}

		worldWrapper.update(getDeltaInMs());
	}

	@Override
	public void resume() {
		game.getAdWhirlViewHandler().hide();
		super.resume();
		Gdx.input.setCatchBackKey(true);
		game.getBackgroundGameScreen().dispose();

		// recreate controller ...

		Entity playerController = worldWrapper.getWorld().getTagManager().getEntity(Tags.PlayerController);
		if (playerController != null) {

			ControllerComponent controllerComponent = playerController.getComponent(ControllerComponent.class);
			// mark current controller to be deleted
			playerController.delete();

			// creates a new controller using new preferences
			createGameController(controllerComponent.getController());
		}

		if (Game.isDebugMode())
			game.getEventManager().registerEvent(Events.showCustomizeControls, PlayGameState.this);
	}

	@Override
	public void dispose() {
		if (shouldDisposeWorldWrapper)
			worldWrapper.dispose();
		spriteBatch.dispose();
	}

}
