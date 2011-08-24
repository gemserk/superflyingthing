package com.gemserk.games.superflyingthing;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.gemserk.analytics.Analytics;
import com.gemserk.animation4j.converters.Converters;
import com.gemserk.animation4j.gdx.converters.LibgdxConverters;
import com.gemserk.commons.adwhirl.AdWhirlViewHandler;
import com.gemserk.commons.artemis.events.Event;
import com.gemserk.commons.artemis.events.EventManager;
import com.gemserk.commons.artemis.events.EventManagerImpl;
import com.gemserk.commons.artemis.events.reflection.EventListenerReflectionRegistrator;
import com.gemserk.commons.artemis.events.reflection.Handles;
import com.gemserk.commons.gdx.GameState;
import com.gemserk.commons.gdx.GameTransitions.ScreenTransition;
import com.gemserk.commons.gdx.GameTransitions.TransitionHandler;
import com.gemserk.commons.gdx.GameTransitions.TransitionScreen;
import com.gemserk.commons.gdx.GlobalTime;
import com.gemserk.commons.gdx.Screen;
import com.gemserk.commons.gdx.ScreenImpl;
import com.gemserk.commons.gdx.graphics.ImmediateModeRendererUtils;
import com.gemserk.commons.gdx.graphics.SpriteBatchUtils;
import com.gemserk.componentsengine.input.InputDevicesMonitorImpl;
import com.gemserk.componentsengine.input.LibgdxInputMappingBuilder;
import com.gemserk.componentsengine.utils.Parameters;
import com.gemserk.componentsengine.utils.ParametersWrapper;
import com.gemserk.games.superflyingthing.gamestates.BackgroundGameState;
import com.gemserk.games.superflyingthing.gamestates.ControllerTestGameState;
import com.gemserk.games.superflyingthing.gamestates.GameOverGameState;
import com.gemserk.games.superflyingthing.gamestates.InstructionsGameState;
import com.gemserk.games.superflyingthing.gamestates.LevelSelectionGameState;
import com.gemserk.games.superflyingthing.gamestates.MainMenuGameState;
import com.gemserk.games.superflyingthing.gamestates.PauseGameState;
import com.gemserk.games.superflyingthing.gamestates.PlayGameState;
import com.gemserk.games.superflyingthing.gamestates.ReplayPlayerGameState;
import com.gemserk.games.superflyingthing.gamestates.SelectPlayModeGameState;
import com.gemserk.games.superflyingthing.gamestates.SettingsGameState;
import com.gemserk.games.superflyingthing.gamestates.SplashGameState;
import com.gemserk.games.superflyingthing.preferences.GamePreferences;
import com.gemserk.games.superflyingthing.preferences.PlayerProfile;
import com.gemserk.games.superflyingthing.resources.GameResources;
import com.gemserk.games.superflyingthing.scripts.controllers.ControllerType;
import com.gemserk.games.superflyingthing.transitions.FadeInTransition;
import com.gemserk.games.superflyingthing.transitions.FadeOutTransition;
import com.gemserk.resources.ResourceManager;
import com.gemserk.resources.ResourceManagerImpl;
import com.gemserk.util.ScreenshotSaver;

public class Game extends com.gemserk.commons.gdx.Game {

	private static boolean debugMode;
	private static boolean showFps = false;
	private static boolean showBox2dDebug = false;

	public static boolean isDebugMode() {
		return debugMode;
	}

	public static void setDebugMode(boolean debugMode) {
		Game.debugMode = debugMode;
	}

	public static boolean isShowBox2dDebug() {
		return showBox2dDebug;
	}

	public static void setShowFps(boolean showFps) {
		Game.showFps = showFps;
	}

	public static boolean isShowFps() {
		return showFps;
	}

	private final AdWhirlViewHandler adWhirlViewHandler;

	private ResourceManager<String> resourceManager;
	private BitmapFont fpsFont;
	private SpriteBatch spriteBatch;
	private InputDevicesMonitorImpl<String> inputDevicesMonitor;

	private EventManager eventManager;

	/**
	 * Used to store global information about the game and to send data between GameStates and Screens.
	 */
	private Parameters gameData;

	private GamePreferences gamePreferences;

	private Rectangle adsMaxArea;

//	private Map<String, Screen> screens;

	class ScreenManager {

		private Map<String, Screen> screens;
		
		public ScreenManager() {
			screens = new HashMap<String, Screen>();
		}

		/**
		 * Adds a new screen for the specified game state.
		 * 
		 * @param id
		 *            The Screen identifier.
		 */
		public void add(String id, GameState gameState) {
			screens.put(id, new ScreenImpl(gameState));
		}

		public Screen get(String id) {
			return screens.get(id);
		}

	}
	
	private ScreenManager screenManager;

	public AdWhirlViewHandler getAdWhirlViewHandler() {
		return adWhirlViewHandler;
	}

	public Screen getPlayScreen() {
		return screenManager.get(Screens.Play);
	}

	public Screen getSplashScreen() {
		return screenManager.get(Screens.Splash);
	}

	public Screen getMainMenuScreen() {
		return screenManager.get(Screens.MainMenu);
	}

	public Screen getSelectPlayModeScreen() {
		return screenManager.get(Screens.SelectPlayMode);
	}

	public Screen getLevelSelectionScreen() {
		return screenManager.get(Screens.LevelSelection);
	}

	public Screen getPauseScreen() {
		return screenManager.get(Screens.Pause);
	}

	public Screen getGameOverScreen() {
		return screenManager.get(Screens.GameOver);
	}

	public Screen getInstructionsScreen() {
		return screenManager.get(Screens.Instructions);
	}

	public Screen getBackgroundGameScreen() {
		return screenManager.get(Screens.BackgroundGame);
	}

	public Screen getSettingsScreen() {
		return screenManager.get(Screens.Settings);
	}

	public Screen getControllersTestScreen() {
		return screenManager.get(Screens.ControllersTest);
	}

	public Screen getReplayPlayerScreen() {
		return screenManager.get(Screens.ReplayPlayer);
	}

	public GamePreferences getGamePreferences() {
		return gamePreferences;
	}

	public Parameters getGameData() {
		return gameData;
	}

	public Game(AdWhirlViewHandler adWhirlViewHandler) {
		this.adWhirlViewHandler = adWhirlViewHandler;
	}

	/**
	 * Used to communicate between gamestates.
	 */
	public EventManager getEventManager() {
		return eventManager;
	}

	public Game() {
		this(new AdWhirlViewHandler());
	}

	@Override
	public void create() {
		Converters.register(Vector2.class, LibgdxConverters.vector2());
		Converters.register(Color.class, LibgdxConverters.color());
		Converters.register(Float.class, Converters.floatValue());

		gameData = new ParametersWrapper();

		try {
			Properties properties = new Properties();
			properties.load(Gdx.files.classpath("version.properties").read());
			getGameData().put("version", properties.getProperty("version"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		Preferences preferences = Gdx.app.getPreferences("gemserk-superflyingthing");
		gamePreferences = new GamePreferences(preferences);

		PlayerProfile playerProfile = gamePreferences.getCurrentPlayerProfile();
		if (playerProfile.getControllerType() == null) {
			if (Gdx.app.getType() == ApplicationType.Android)
				playerProfile.setControllerType(ControllerType.ClassicController);
			else
				playerProfile.setControllerType(ControllerType.KeyboardController);
			gamePreferences.updatePlayerProfile(playerProfile);
		}

		eventManager = new EventManagerImpl();

		resourceManager = new ResourceManagerImpl<String>();
		GameResources.load(resourceManager);

		fpsFont = resourceManager.getResourceValue("FpsFont");
		spriteBatch = new SpriteBatch();

		PlayGameState playGameState = new PlayGameState(this);
		playGameState.setResourceManager(resourceManager);
		playGameState.setGamePreferences(gamePreferences);

		PauseGameState pauseGameState = new PauseGameState(this);
		pauseGameState.setResourceManager(resourceManager);

		MainMenuGameState mainMenuGameState = new MainMenuGameState(this);
		mainMenuGameState.setResourceManager(resourceManager);

		SelectPlayModeGameState selectPlayModeGameState = new SelectPlayModeGameState(this);
		selectPlayModeGameState.setResourceManager(resourceManager);

		LevelSelectionGameState levelSelectionGameState = new LevelSelectionGameState(this);
		levelSelectionGameState.setResourceManager(resourceManager);

		GameOverGameState gameOverGameState = new GameOverGameState(this);
		gameOverGameState.setResourceManager(resourceManager);

		InstructionsGameState instructionsGameState = new InstructionsGameState(this);
		instructionsGameState.setResourceManager(resourceManager);

		BackgroundGameState backgroundGameState = new BackgroundGameState(this);
		backgroundGameState.setResourceManager(resourceManager);

		SettingsGameState settingsGameState = new SettingsGameState(this);
		settingsGameState.setResourceManager(resourceManager);
		settingsGameState.setGamePreferences(gamePreferences);

		ControllerTestGameState controllerTestGameState = new ControllerTestGameState(this);
		controllerTestGameState.setResourceManager(resourceManager);

		ReplayPlayerGameState replayPlayerGameState = new ReplayPlayerGameState(this);
		replayPlayerGameState.setResourceManager(resourceManager);

		screenManager = new ScreenManager();

		screenManager.add(Screens.Play, playGameState);
		screenManager.add(Screens.Pause, pauseGameState);
		screenManager.add(Screens.MainMenu, mainMenuGameState);
		screenManager.add(Screens.SelectPlayMode, selectPlayModeGameState);
		screenManager.add(Screens.LevelSelection, levelSelectionGameState);
		screenManager.add(Screens.GameOver, gameOverGameState);
		screenManager.add(Screens.Instructions, instructionsGameState);
		screenManager.add(Screens.Splash, new SplashGameState(this));
		screenManager.add(Screens.BackgroundGame, backgroundGameState);
		screenManager.add(Screens.Settings, settingsGameState);
		screenManager.add(Screens.ControllersTest, controllerTestGameState);
		screenManager.add(Screens.ReplayPlayer, replayPlayerGameState);

		EventListenerReflectionRegistrator registrator = new EventListenerReflectionRegistrator(eventManager);

		registrator.registerEventListeners(playGameState);
		registrator.registerEventListeners(backgroundGameState);
		registrator.registerEventListeners(settingsGameState);
		registrator.registerEventListeners(replayPlayerGameState);
		registrator.registerEventListeners(this);

		setScreen(getSplashScreen());

		Analytics.traker.trackPageView("/start", "/start", null);

		inputDevicesMonitor = new InputDevicesMonitorImpl<String>();
		new LibgdxInputMappingBuilder<String>(inputDevicesMonitor, Gdx.input) {
			{
				monitorKey("toggleDebug", Keys.NUM_0);
				monitorKey("grabScreenshot", Keys.NUM_9);
				monitorKey("toggleFps", Keys.NUM_8);
				monitorKey("toggleBox2dDebug", Keys.NUM_7);
				monitorKey("toggleBackground", Keys.NUM_6);
			}
		};

		adsMaxArea = new Rectangle(1f, 1f, Gdx.graphics.getWidth() - 2f, Gdx.graphics.getHeight() * 0.105f - 2f);

		Gdx.graphics.getGL10().glClearColor(0, 0, 0, 1);
	}

	public static class TransitionBuilder {

		private final Screen screen;
		private final Game game;

		float leaveTime;
		float enterTime;
		boolean shouldDisposeCurrentScreen;

		TransitionHandler leaveTransitionHandler = new TransitionHandler();

		public TransitionBuilder leaveTime(float leaveTime) {
			this.leaveTime = leaveTime;
			return this;
		}

		public TransitionBuilder enterTime(float enterTime) {
			this.enterTime = enterTime;
			return this;
		}

		public TransitionBuilder leaveTime(int leaveTime) {
			return leaveTime((float) leaveTime * 0.001f);
		}

		public TransitionBuilder enterTime(int enterTime) {
			return enterTime((float) enterTime * 0.001f);
		}

		public TransitionBuilder disposeCurrent() {
			this.shouldDisposeCurrentScreen = true;
			return this;
		}

		public TransitionBuilder disposeCurrent(boolean disposeCurrent) {
			this.shouldDisposeCurrentScreen = disposeCurrent;
			return this;
		}

		public TransitionBuilder leaveTransitionHandler(TransitionHandler transitionHandler) {
			this.leaveTransitionHandler = transitionHandler;
			return this;
		}

		public TransitionBuilder(final Game game, final Screen screen) {
			this.game = game;
			this.screen = screen;
			this.leaveTransitionHandler = new TransitionHandler();
		}

		public void start() {
			final Screen currentScreen = game.getScreen();
			game.setScreen(new TransitionScreen(new ScreenTransition( //
					new FadeOutTransition(currentScreen, leaveTime, leaveTransitionHandler), //
					new FadeInTransition(screen, enterTime, new TransitionHandler() {
						public void onEnd() {
							// disposes current transition screen, not previous screen.
							game.setScreen(screen, true);
							if (shouldDisposeCurrentScreen)
								currentScreen.dispose();
						};
					}))) {
				@Override
				public void resume() {
					super.resume();
					Gdx.input.setCatchBackKey(true);
				}
			});
		}

	}

	public TransitionBuilder transition(Screen screen) {
		return new TransitionBuilder(this, screen);
	}

	public void transition(final Screen screen, int leaveTime, int enterTime) {
		transition(screen) //
				.leaveTime(leaveTime) //
				.enterTime(enterTime) //
				.disposeCurrent() //
				.start();
	}

	public void transition(final Screen screen, int leaveTime, int enterTime, final boolean shouldDisposeCurrentScreen) {
		transition(screen) //
				.leaveTime(leaveTime) //
				.enterTime(enterTime) //
				.disposeCurrent(shouldDisposeCurrentScreen) //
				.start();
	}

	@Override
	public void render() {

		GlobalTime.setDelta(Gdx.graphics.getDeltaTime());

		inputDevicesMonitor.update();

		if (inputDevicesMonitor.getButton("toggleBox2dDebug").isReleased())
			showBox2dDebug = !showBox2dDebug;

		if (inputDevicesMonitor.getButton("toggleFps").isReleased())
			showFps = !showFps;

		if (inputDevicesMonitor.getButton("toggleDebug").isReleased())
			Game.setDebugMode(!Game.isDebugMode());

		if (inputDevicesMonitor.getButton("toggleBackground").isReleased()) {
			eventManager.registerEvent(Events.toggleFirstBackground, this);
			eventManager.registerEvent(Events.toggleSecondBackground, this);
		}

		super.render();

		spriteBatch.begin();
		if (showFps)
			SpriteBatchUtils.drawMultilineText(spriteBatch, fpsFont, "FPS: " + Gdx.graphics.getFramesPerSecond(), Gdx.graphics.getWidth() * 0.02f, Gdx.graphics.getHeight() * 0.90f, 0f, 0.5f);
		if (Game.isDebugMode())
			SpriteBatchUtils.drawMultilineText(spriteBatch, fpsFont, "Debug", Gdx.graphics.getWidth() * 0.02f, Gdx.graphics.getHeight() * 0.90f, 0f, 0.5f);
		spriteBatch.end();

		if (inputDevicesMonitor.getButton("grabScreenshot").isReleased()) {
			try {
				ScreenshotSaver.saveScreenshot("superflyingthing");
			} catch (IOException e) {
				Gdx.app.log("SuperFlyingThing", "Can't save screenshot");
			}
		}

		if (Game.isDebugMode()) {
			ImmediateModeRendererUtils.drawRectangle(adsMaxArea, Color.GREEN);
		}

		eventManager.process();
	}

	@Handles
	public void toggleFirstBackground(Event e) {
		boolean oldBackgroundState = gamePreferences.isFirstBackgroundEnabled();
		String pageView = "/settings/background/" + (oldBackgroundState ? "hide" : "show");
		Analytics.traker.trackPageView(pageView, pageView, null);
		gamePreferences.setFirstBackgroundEnabled(!oldBackgroundState);
	}

	@Handles
	public void toggleSecondBackground(Event e) {
		gamePreferences.setSecondBackgroundEnabled(!gamePreferences.isSecondBackgroundEnabled());
	}

	@Override
	public void pause() {
		super.pause();
		Gdx.app.log("SuperFlyingThing", "game paused via ApplicationListner.pause()");
		adWhirlViewHandler.hide();
	}

	@Override
	public void resume() {
		super.resume();
		Gdx.app.log("SuperFlyingThing", "game resumed via ApplicationListner.resume()");
		adWhirlViewHandler.show();
	}

	@Override
	public void dispose() {
		super.dispose();
		resourceManager.unloadAll();
		spriteBatch.dispose();
	}

}
