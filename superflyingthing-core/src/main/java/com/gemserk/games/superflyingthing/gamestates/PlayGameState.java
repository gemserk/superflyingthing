package com.gemserk.games.superflyingthing.gamestates;

import java.text.MessageFormat;

import org.w3c.dom.Document;

import com.artemis.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
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
import com.gemserk.commons.gdx.gui.Container;
import com.gemserk.commons.gdx.gui.GuiControls;
import com.gemserk.commons.gdx.gui.Text;
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
import com.gemserk.games.superflyingthing.levels.Level;
import com.gemserk.games.superflyingthing.levels.Levels;
import com.gemserk.games.superflyingthing.levels.RandomLevelGenerator;
import com.gemserk.games.superflyingthing.preferences.GamePreferences;
import com.gemserk.games.superflyingthing.preferences.PlayerProfile;
import com.gemserk.games.superflyingthing.preferences.PlayerProfile.LevelInformation;
import com.gemserk.games.superflyingthing.scenes.NormalModeSceneTemplate;
import com.gemserk.games.superflyingthing.scenes.SceneTemplate;
import com.gemserk.games.superflyingthing.scripts.controllers.ControllerType;
import com.gemserk.games.superflyingthing.templates.ControllerTemplates;
import com.gemserk.games.superflyingthing.templates.EntityTemplates;
import com.gemserk.games.superflyingthing.templates.Groups;
import com.gemserk.games.superflyingthing.templates.UserMessageTemplate;
import com.gemserk.resources.Resource;
import com.gemserk.resources.ResourceManager;

public class PlayGameState extends GameStateImpl {

	private final Game game;
	private SpriteBatch spriteBatch;
	// private Libgdx2dCamera worldCamera;

	// private Box2DCustomDebugRenderer box2dCustomDebugRenderer;
	private ResourceManager<String> resourceManager;
	Container container;
	// private Libgdx2dCamera guiCamera;
	private InputDevicesMonitorImpl<String> inputDevicesMonitor;

	private EntityTemplates entityTemplates;
	private ControllerTemplates controllerTemplates;

	EntityBuilder entityBuilder;
	private WorldWrapper worldWrapper;
	private EntityFactory entityFactory;
	private Parameters parameters = new ParametersWrapper();

	GameData gameData;
	private Text itemsTakenLabel;
	private Text timerLabel;

	EntityTemplate userMessageTemplate;

	private GamePreferences gamePreferences;

	private Text tiltvalue;
	private final boolean tiltValueEnabled = false;
	int tilttime = 0;

	private boolean shouldDisposeWorldWrapper;

	private static String[] endMessages = new String[] { "Great Job!", "Nicely Done!", "You made it!", "Good Work!", "You Rock!", };
	private RenderLayers renderLayers;
	private Level level;
	private Integer levelNumber;
	private AverageFPS averageFPS;
	private Injector injector;

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
		averageFPS = new AverageFPS();
		createALotOfStuff();
		createWorld();
	}

	private void createALotOfStuff() {
		
		levelNumber = getParameters().get("level", 1);
		
		shouldDisposeWorldWrapper = true;

		spriteBatch = new SpriteBatch();

		injector = new InjectorImpl();
		injector.configureField("resourceManager", resourceManager);
		injector.configureField("timeStepProvider", new TimeStepProviderGameStateImpl(this));

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

		BitmapFont font = resourceManager.getResourceValue("GameFont");
		
		itemsTakenLabel = GuiControls.label("") //
				.position(Gdx.graphics.getWidth() * 0.5f, Gdx.graphics.getHeight() * 0.95f) //
				.font(font) //
				.color(1f, 1f, 1f, 1f) //
				.build();

		gameData.totalItems = level.items.size();
		if (gameData.totalItems > 0)
			itemsTakenLabel.setText(MessageFormat.format("{0}/{1}", gameData.currentItems, gameData.totalItems));

		worldWrapper = new WorldWrapper(new com.artemis.World());

		SceneTemplate sceneTemplate = injector.getInstance(NormalModeSceneTemplate.class);
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

		container = new Container();

		userMessageTemplate = new UserMessageTemplate(container, resourceManager);

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

		// entity with some game logic
		entityBuilder.component(new ScriptComponent(new ScriptJavaImpl() {

			boolean incrementTimer = true;
			private com.artemis.World world;
			private Parameters parameters = new ParametersWrapper();

			@Override
			public void init(com.artemis.World world, Entity e) {
				this.world = world;
				timerLabelBuilder.append("Time: ");
			}

			@Handles(ids = Events.shipDeath)
			public void shipDeath(Event e) {
				gameData.deaths++;
				System.out.println(gameData.deaths);
			}

			@Handles
			public void itemTaken(Event e) {
				gameData.currentItems++;
				itemsTakenLabel.setText(MessageFormat.format("{0}/{1}", gameData.currentItems, gameData.totalItems));
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
				incrementTimer = false;
				if (GameInformation.gameMode == GameInformation.ChallengeGameMode) {
					playerProfile.setLevelInformationForLevel(levelNumber, new LevelInformation(seconds(gameData.time), gameData.currentItems));
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
				entityFactory.instantiate(entityTemplates.timerTemplate, parameters //
						.put("time", 2.5f) //
						.put("eventId", Events.gameOver) //
						);
			}

			@Handles
			public void gameOver(Event e) {

				// game is over, move to replay screen

				// game.transition(game.getGameOverScreen(), 0, 300, false);

				Entity replayRecorder = world.getTagManager().getEntity(Groups.ReplayRecorder);

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

		parameters.put("position", new Vector2(Gdx.graphics.getWidth() * 0.5f, Gdx.graphics.getHeight() * 0.8f));
		parameters.put("text", getRandomEndMessage());

		entityFactory.instantiate(userMessageTemplate, parameters);
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

		// if (Game.isShowBox2dDebug())
		// box2dCustomDebugRenderer.render();

		spriteBatch.begin();
		container.draw(spriteBatch);
		spriteBatch.end();
	}

	@Override
	public void update() {
		averageFPS.update();

		inputDevicesMonitor.update();
		Synchronizers.synchronize(getDelta());
		container.update();

		if (inputDevicesMonitor.getButton("pause").isReleased()) {
			gameData.averageFPS = averageFPS.getFPS();
			game.transition(Screens.Pause) //
					.disposeCurrent(false) //
					.parameter("level", levelNumber) //
					.start();
			Gdx.app.log("SuperFlyingThing", "Pausing level " + levelNumber);
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
		game.getAdWhirlViewHandler().hide();
		super.resume();
		Gdx.input.setCatchBackKey(true);
		game.getBackgroundGameScreen().dispose();

		// recreate controller ...

		Entity playerController = worldWrapper.getWorld().getTagManager().getEntity(Groups.PlayerController);
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
