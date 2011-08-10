package com.gemserk.games.superflyingthing;

import java.io.IOException;
import java.util.Properties;

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
import com.gemserk.commons.gdx.GameTransitions.ScreenTransition;
import com.gemserk.commons.gdx.GameTransitions.TransitionHandler;
import com.gemserk.commons.gdx.GameTransitions.TransitionScreen;
import com.gemserk.commons.gdx.Screen;
import com.gemserk.commons.gdx.ScreenImpl;
import com.gemserk.commons.gdx.graphics.ImmediateModeRendererUtils;
import com.gemserk.commons.gdx.graphics.SpriteBatchUtils;
import com.gemserk.componentsengine.input.InputDevicesMonitorImpl;
import com.gemserk.componentsengine.input.LibgdxInputMappingBuilder;
import com.gemserk.games.superflyingthing.gamestates.BackgroundGameState;
import com.gemserk.games.superflyingthing.gamestates.GameInformation;
import com.gemserk.games.superflyingthing.gamestates.GameOverGameState;
import com.gemserk.games.superflyingthing.gamestates.InstructionsGameState;
import com.gemserk.games.superflyingthing.gamestates.LevelSelectionGameState;
import com.gemserk.games.superflyingthing.gamestates.MainMenuGameState;
import com.gemserk.games.superflyingthing.gamestates.PauseGameState;
import com.gemserk.games.superflyingthing.gamestates.PlayGameState;
import com.gemserk.games.superflyingthing.gamestates.SelectPlayModeGameState;
import com.gemserk.games.superflyingthing.gamestates.SettingsGameState;
import com.gemserk.games.superflyingthing.gamestates.SplashGameState;
import com.gemserk.games.superflyingthing.preferences.GamePreferences;
import com.gemserk.games.superflyingthing.resources.GameResources;
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

	private final AdWhirlViewHandler adWhirlViewHandler;
	
	private Screen splashScreen;
	private Screen mainMenuScreen;
	private Screen selectPlayModeScreen;
	private Screen playScreen;
	private Screen levelSelectionScreen;
	private Screen pauseScreen;
	private Screen gameOverScreen;
	private Screen instructionsScreen;
	private Screen backgroundGameScreen;
	private Screen settingsScreen;
	
	private ResourceManager<String> resourceManager;
	private BitmapFont fpsFont;
	private SpriteBatch spriteBatch;
	private InputDevicesMonitorImpl<String> inputDevicesMonitor;

	private GamePreferences gamePreferences;

	private Rectangle adsMaxArea;
	
	public AdWhirlViewHandler getAdWhirlViewHandler() {
		return adWhirlViewHandler;
	}

	public Screen getPlayScreen() {
		return playScreen;
	}

	public Screen getSplashScreen() {
		return splashScreen;
	}

	public Screen getMainMenuScreen() {
		return mainMenuScreen;
	}

	public Screen getSelectPlayModeScreen() {
		return selectPlayModeScreen;
	}

	public Screen getLevelSelectionScreen() {
		return levelSelectionScreen;
	}

	public Screen getPauseScreen() {
		return pauseScreen;
	}

	public Screen getGameOverScreen() {
		return gameOverScreen;
	}

	public Screen getInstructionsScreen() {
		return instructionsScreen;
	}
	
	public Screen getBackgroundGameScreen() {
		return backgroundGameScreen;
	}

	public Screen getSettingsScreen() {
		return settingsScreen;
	}
	
	public GamePreferences getGamePreferences() {
		return gamePreferences;
	}

	public Game(AdWhirlViewHandler adWhirlViewHandler) {
		this.adWhirlViewHandler = adWhirlViewHandler;
	}

	public Game() {
		this(new AdWhirlViewHandler());
	}

	@Override
	public void create() {
		Converters.register(Vector2.class, LibgdxConverters.vector2());
		Converters.register(Color.class, LibgdxConverters.color());
		Converters.register(Float.class, Converters.floatValue());
		
		try {
			Properties properties = new Properties();
			properties.load(Gdx.files.classpath("version.properties").read());
			GameInformation.gameVersion = properties.getProperty("version");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		Preferences preferences = Gdx.app.getPreferences("gemserk-superflyingthing");
		gamePreferences = new GamePreferences(preferences);

		resourceManager = new ResourceManagerImpl<String>();
		GameResources.load(resourceManager);

		fpsFont = resourceManager.getResourceValue("FpsFont");
		spriteBatch = new SpriteBatch();

		PlayGameState playGameState = new PlayGameState(this);
		playGameState.setResourceManager(resourceManager);

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

		playScreen = new ScreenImpl(playGameState);
		pauseScreen = new ScreenImpl(pauseGameState);
		mainMenuScreen = new ScreenImpl(mainMenuGameState);
		selectPlayModeScreen = new ScreenImpl(selectPlayModeGameState);
		levelSelectionScreen = new ScreenImpl(levelSelectionGameState);
		gameOverScreen = new ScreenImpl(gameOverGameState);
		instructionsScreen = new ScreenImpl(instructionsGameState);
		splashScreen = new ScreenImpl(new SplashGameState(this));
		backgroundGameScreen = new ScreenImpl(backgroundGameState);
		settingsScreen = new ScreenImpl(settingsGameState);

		setScreen(splashScreen);

		Analytics.traker.trackPageView("/start", "/start", null);

		inputDevicesMonitor = new InputDevicesMonitorImpl<String>();
		new LibgdxInputMappingBuilder<String>(inputDevicesMonitor, Gdx.input) {
			{
				monitorKey("toggleDebug", Keys.NUM_0);
				monitorKey("grabScreenshot", Keys.NUM_9);
				monitorKey("toggleFps", Keys.NUM_8);
				monitorKey("toggleBox2dDebug", Keys.NUM_7);
			}
		};
		
		adsMaxArea = new Rectangle(1f, 1f, Gdx.graphics.getWidth() - 2f, Gdx.graphics.getHeight() * 0.105f - 2f);

		Gdx.graphics.getGL10().glClearColor(0, 0, 0, 1);
	}

	public static class TransitionBuilder {

		private final Screen screen;
		private final Game game;

		int leaveTime;
		int enterTime;
		boolean shouldDisposeCurrentScreen;

		TransitionHandler leaveTransitionHandler = new TransitionHandler();

		public TransitionBuilder leaveTime(int leaveTime) {
			this.leaveTime = leaveTime;
			return this;
		}

		public TransitionBuilder enterTime(int enterTime) {
			this.enterTime = enterTime;
			return this;
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
		inputDevicesMonitor.update();

		if (inputDevicesMonitor.getButton("toggleBox2dDebug").isReleased())
			showBox2dDebug = !showBox2dDebug;

		if (inputDevicesMonitor.getButton("toggleFps").isReleased())
			showFps = !showFps;

		if (inputDevicesMonitor.getButton("toggleDebug").isReleased())
			Game.setDebugMode(!Game.isDebugMode());

		super.render();

		spriteBatch.begin();
		if (showFps)
			SpriteBatchUtils.drawMultilineText(spriteBatch, fpsFont, "FPS: " + Gdx.graphics.getFramesPerSecond(), Gdx.graphics.getWidth() * 0.02f, Gdx.graphics.getHeight() * 0.95f, 0f, 0.5f);
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
