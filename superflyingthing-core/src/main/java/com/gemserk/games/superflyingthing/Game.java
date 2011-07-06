package com.gemserk.games.superflyingthing;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
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
import com.gemserk.commons.gdx.graphics.SpriteBatchUtils;
import com.gemserk.componentsengine.input.InputDevicesMonitorImpl;
import com.gemserk.componentsengine.input.LibgdxInputMappingBuilder;
import com.gemserk.games.superflyingthing.gamestates.LevelSelectionGameState;
import com.gemserk.games.superflyingthing.gamestates.MainMenuGameState;
import com.gemserk.games.superflyingthing.gamestates.PauseGameState;
import com.gemserk.games.superflyingthing.gamestates.PlayGameState;
import com.gemserk.games.superflyingthing.gamestates.SelectPlayModeGameState;
import com.gemserk.games.superflyingthing.gamestates.SplashGameState;
import com.gemserk.games.superflyingthing.resources.GameResources;
import com.gemserk.games.superflyingthing.transitions.FadeInTransition;
import com.gemserk.games.superflyingthing.transitions.FadeOutTransition;
import com.gemserk.resources.ResourceManager;
import com.gemserk.resources.ResourceManagerImpl;

public class Game extends com.gemserk.commons.gdx.Game {

	private static boolean debugMode;

	public static boolean isDebugMode() {
		return debugMode;
	}

	public static void setDebugMode(boolean debugMode) {
		Game.debugMode = debugMode;
	}

	private final AdWhirlViewHandler adWhirlViewHandler;
	private Screen splashScreen;
	private Screen mainMenuScreen;
	private Screen selectPlayModeScreen;
	private Screen playScreen;
	private Screen levelSelectionScreen;
	private Screen pauseScreen;
	private ResourceManager<String> resourceManager;
	private BitmapFont fpsFont;
	private SpriteBatch spriteBatch;
	private InputDevicesMonitorImpl<String> inputDevicesMonitor;

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

		resourceManager = new ResourceManagerImpl<String>();
		GameResources.load(resourceManager);

		fpsFont = resourceManager.getResourceValue("FpsFont");
		spriteBatch = new SpriteBatch();

		playScreen = new ScreenImpl(new PlayGameState(this));
		pauseScreen = new ScreenImpl(new PauseGameState(this)); 
		levelSelectionScreen = new ScreenImpl(new LevelSelectionGameState(this));
		splashScreen = new ScreenImpl(new SplashGameState(this));
		mainMenuScreen = new ScreenImpl(new MainMenuGameState(this));
		selectPlayModeScreen = new ScreenImpl(new SelectPlayModeGameState(this));

		setScreen(splashScreen);

		Analytics.traker.trackPageView("/start", "/start", null);
		
		inputDevicesMonitor = new InputDevicesMonitorImpl<String>();
		new LibgdxInputMappingBuilder<String>(inputDevicesMonitor, Gdx.input) {
			{
					monitorKey("toggleDebug", Keys.NUM_0);
			}
		};
	}

	public void transition(final Screen screen, int leaveTime, int enterTime) {
		final boolean shouldDisposeCurrentScreen = true;
		transition(screen, leaveTime, enterTime, shouldDisposeCurrentScreen);
	}

	public void transition(final Screen screen, int leaveTime, int enterTime, final boolean shouldDisposeCurrentScreen) {
		final Screen currentScreen = getScreen();
		setScreen(new TransitionScreen(new ScreenTransition( //
				new FadeOutTransition(currentScreen, leaveTime), //
				new FadeInTransition(screen, enterTime, new TransitionHandler() {
					public void onEnd() {
						// disposes current transition screen, not previous screen.
						setScreen(screen, true);
						if (shouldDisposeCurrentScreen)
							currentScreen.dispose();
					};
				}))));
	}

	@Override
	public void render() {
		inputDevicesMonitor.update();
		
		if (inputDevicesMonitor.getButton("toggleDebug").isReleased()) {
			Game.setDebugMode(!Game.isDebugMode());
			if (Game.isDebugMode())
				Gdx.app.log("SuperFlyingThing", "debug controls enabled");
			else
				Gdx.app.log("SuperFlyingThing", "debug controls disabled");
		}
		
		super.render();
		spriteBatch.begin();
		SpriteBatchUtils.drawMultilineText(spriteBatch, fpsFont, "FPS: " + Gdx.graphics.getFramesPerSecond(), Gdx.graphics.getWidth() * 0.02f, Gdx.graphics.getHeight() * 0.95f, 0f, 0.5f);
		if (Game.isDebugMode())
			SpriteBatchUtils.drawMultilineText(spriteBatch, fpsFont, "Debug", Gdx.graphics.getWidth() * 0.02f, Gdx.graphics.getHeight() * 0.90f, 0f, 0.5f);
		spriteBatch.end();
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
