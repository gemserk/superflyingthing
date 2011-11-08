package com.gemserk.games.superflyingthing;

import java.io.IOException;
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
import com.gemserk.commons.gdx.GlobalTime;
import com.gemserk.commons.gdx.Screen;
import com.gemserk.commons.gdx.audio.SoundPlayer;
import com.gemserk.commons.gdx.graphics.ImmediateModeRendererUtils;
import com.gemserk.commons.gdx.graphics.SpriteBatchUtils;
import com.gemserk.commons.gdx.screens.transitions.TransitionBuilder;
import com.gemserk.commons.reflection.Injector;
import com.gemserk.commons.reflection.InjectorImpl;
import com.gemserk.commons.utils.BrowserUtils;
import com.gemserk.commons.utils.BrowserUtilsNullImpl;
import com.gemserk.componentsengine.input.InputDevicesMonitorImpl;
import com.gemserk.componentsengine.input.LibgdxInputMappingBuilder;
import com.gemserk.componentsengine.utils.Parameters;
import com.gemserk.componentsengine.utils.ParametersWrapper;
import com.gemserk.games.superflyingthing.audio.MusicPlayer;
import com.gemserk.games.superflyingthing.gamestates.AboutGameState;
import com.gemserk.games.superflyingthing.gamestates.BackgroundGameState;
import com.gemserk.games.superflyingthing.gamestates.ControllerSettingsGameState;
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
import com.gemserk.resources.Resource;
import com.gemserk.resources.monitor.FilesMonitor;
import com.gemserk.resources.monitor.FilesMonitorNullImpl;
import com.gemserk.util.ScreenshotSaver;

public class Game extends com.gemserk.commons.gdx.Game {

	public static final String blogUrl = "http://blog.gemserk.com/";
	public static final String desktopMoreGamesUrl = "http://blog.gemserk.com/games";
	public static final String androidMoreGamesUrl = "market://search?q=pub:Gemserk Studios";

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

	private AdWhirlViewHandler adWhirlViewHandler;

	private CustomResourceManager<String> resourceManager;
	private Resource<BitmapFont> fpsFontResource;
	private SpriteBatch spriteBatch;
	private InputDevicesMonitorImpl<String> inputDevicesMonitor;

	private EventManager eventManager;

	private Parameters gameData;

	private GamePreferences gamePreferences;

	private Rectangle adsMaxArea;

	private ScreenManager screenManager;
	private FilesMonitor filesMonitor;
	private BrowserUtils browserUtils = new BrowserUtilsNullImpl();

	private SoundPlayer soundPlayer;

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

	public Screen getScreen(String id) {
		return screenManager.get(id);
	}

	public GamePreferences getGamePreferences() {
		return gamePreferences;
	}

	/**
	 * Used to store global information about the game like version number and others.
	 */
	public Parameters getGameData() {
		return gameData;
	}

	public CustomResourceManager<String> getResourceManager() {
		return resourceManager;
	}

	public Rectangle getAdsMaxArea() {
		return adsMaxArea;
	}

	/**
	 * Used to communicate between gamestates.
	 */
	public EventManager getEventManager() {
		return eventManager;
	}

	public void setBrowserUtils(BrowserUtils browserUtils) {
		this.browserUtils = browserUtils;
	}

	public void setAdWhirlViewHandler(AdWhirlViewHandler adWhirlViewHandler) {
		this.adWhirlViewHandler = adWhirlViewHandler;
	}

	public void setFilesMonitor(FilesMonitor filesMonitor) {
		this.filesMonitor = filesMonitor;
	}

	public Game() {
		this(new AdWhirlViewHandler());
	}

	public Game(AdWhirlViewHandler adWhirlViewHandler) {
		this(adWhirlViewHandler, new FilesMonitorNullImpl());
	}

	public Game(AdWhirlViewHandler adWhirlViewHandler, FilesMonitor filesMonitor) {
		this.adWhirlViewHandler = adWhirlViewHandler;
		this.filesMonitor = filesMonitor;
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

		soundPlayer = new SoundPlayer();
		soundPlayer.setVolume(1f);
		
		eventManager = new EventManagerImpl();

		resourceManager = new CustomResourceManager<String>();
		screenManager = new ScreenManagerImpl(this);

		GameResources.load(resourceManager, filesMonitor);
		
		MusicPlayer musicPlayer = new MusicPlayer(soundPlayer, resourceManager.get(GameResources.MusicTracks.Game));

		fpsFontResource = resourceManager.get("FpsFont");
		spriteBatch = new SpriteBatch();

		Injector injector = new InjectorImpl();
		
		injector.bind("game", Game.this);
		injector.bind("soundPlayer", soundPlayer);
		injector.bind("musicPlayer", musicPlayer);
		injector.bind("resourceManager", resourceManager);
		injector.bind("adWhirlViewHandler", adWhirlViewHandler);
		injector.bind("gamePreferences", gamePreferences);
		injector.bind("browserUtils", browserUtils);
		injector.bind("screenManager", screenManager);
		injector.bind("eventManager", eventManager);

		GameState mainMenuGameState = injector.getInstance(MainMenuGameState.class);

		GameState selectPlayModeGameState = injector.getInstance(SelectPlayModeGameState.class);
		GameState levelSelectionGameState = injector.getInstance(LevelSelectionGameState.class);

		GameState playGameState = injector.getInstance(PlayGameState.class);
		GameState pauseGameState = injector.getInstance(PauseGameState.class);
		GameState replayPlayerGameState = injector.getInstance(ReplayPlayerGameState.class);
		GameState gameOverGameState = injector.getInstance(GameOverGameState.class);

		GameState instructionsGameState = injector.getInstance(InstructionsGameState.class);
		GameState backgroundGameState = injector.getInstance(BackgroundGameState.class);

		GameState settingsGameState = injector.getInstance(SettingsGameState.class);
		GameState controllerSettingsGameState = injector.getInstance(ControllerSettingsGameState.class);
		GameState controllerTestGameState = injector.getInstance(ControllerTestGameState.class);
		GameState aboutGameState = injector.getInstance(AboutGameState.class);

		GameState splashGameState = injector.getInstance(SplashGameState.class);

		screenManager.add(Screens.Pause, pauseGameState);
		screenManager.add(Screens.MainMenu, mainMenuGameState);
		screenManager.add(Screens.SelectPlayMode, selectPlayModeGameState);
		screenManager.add(Screens.LevelSelection, levelSelectionGameState);
		screenManager.add(Screens.GameOver, gameOverGameState);
		screenManager.add(Screens.Instructions, instructionsGameState);
		screenManager.add(Screens.Splash, splashGameState);
		screenManager.add(Screens.Settings, settingsGameState);
		screenManager.add(Screens.ControllersSettings, controllerSettingsGameState);
		screenManager.add(Screens.ControllersTest, controllerTestGameState);
		screenManager.add(Screens.ReplayPlayer, replayPlayerGameState);
		screenManager.add(Screens.Play, playGameState);

		screenManager.add(Screens.BackgroundGame, backgroundGameState);
		screenManager.add(Screens.About, aboutGameState);

		EventListenerReflectionRegistrator registrator = new EventListenerReflectionRegistrator(eventManager);

		registrator.registerEventListeners(playGameState);
		registrator.registerEventListeners(backgroundGameState);
		registrator.registerEventListeners(settingsGameState);
		registrator.registerEventListeners(replayPlayerGameState);
		registrator.registerEventListeners(this);

		setScreen(getSplashScreen());

		String version = getGameData().get("version");

		Analytics.traker.trackPageView("/start", "/start", null);
		Analytics.traker.trackPageView("/info/version/" + version, "/info/version/" + version, null);

		inputDevicesMonitor = new InputDevicesMonitorImpl<String>();
		new LibgdxInputMappingBuilder<String>(inputDevicesMonitor, Gdx.input) {
			{
				monitorKey("toggleDebug", Keys.NUM_0);
				monitorKey("grabScreenshot", Keys.NUM_9);
				monitorKey("toggleFps", Keys.NUM_8);
				monitorKey("toggleBox2dDebug", Keys.NUM_7);
				monitorKey("toggleBackground", Keys.NUM_6);
				monitorKey("reloadResources", Keys.NUM_1);
			}
		};

		float adsWidth = Gdx.graphics.getWidth() * 480f / 800f;
		float adsHeight = Gdx.graphics.getHeight() * 78f / 480f;

		adsMaxArea = new Rectangle(Gdx.graphics.getWidth() * 0.5f - adsWidth * 0.5f, 1f, adsWidth - 2f, adsHeight - 2f);

		Gdx.graphics.getGL10().glClearColor(0, 0, 0, 1);
	}

	public TransitionBuilder transition(String screen) {
		return screenManager.transition(screen);
		// return new TransitionBuilder(this, screenManager.get(screen));
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

		if (inputDevicesMonitor.getButton("reloadResources").isReleased()) {
//			ArrayList<String> registeredResources = resourceManager.getRegisteredResources();
//			for (int i = 0; i < registeredResources.size(); i++) {
//				String resourceId = registeredResources.get(i);
//				resourceManager.get(resourceId).reload();
//			}
			getScreen().restart();
		}

		if (inputDevicesMonitor.getButton("toggleBackground").isReleased()) {
			eventManager.registerEvent(Events.toggleFirstBackground, this);
			eventManager.registerEvent(Events.toggleSecondBackground, this);
		}

		super.render();

		spriteBatch.begin();
		if (showFps)
			SpriteBatchUtils.drawMultilineText(spriteBatch, fpsFontResource.get(), "FPS: " + Gdx.graphics.getFramesPerSecond(), Gdx.graphics.getWidth() * 0.02f, Gdx.graphics.getHeight() * 0.90f, 0f, 0.5f);
		if (Game.isDebugMode())
			SpriteBatchUtils.drawMultilineText(spriteBatch, fpsFontResource.get(), "Debug", Gdx.graphics.getWidth() * 0.02f, Gdx.graphics.getHeight() * 0.90f, 0f, 0.5f);
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

		filesMonitor.checkModifiedFiles();
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
