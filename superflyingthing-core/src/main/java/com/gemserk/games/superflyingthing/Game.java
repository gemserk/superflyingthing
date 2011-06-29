package com.gemserk.games.superflyingthing;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.gemserk.animation4j.converters.Converters;
import com.gemserk.animation4j.gdx.converters.LibgdxConverters;
import com.gemserk.commons.gdx.GameTransitions.ScreenTransition;
import com.gemserk.commons.gdx.GameTransitions.TransitionHandler;
import com.gemserk.commons.gdx.GameTransitions.TransitionScreen;
import com.gemserk.commons.gdx.Screen;
import com.gemserk.commons.gdx.ScreenImpl;
import com.gemserk.games.superflyingthing.gamestates.MainMenuGameState;
import com.gemserk.games.superflyingthing.gamestates.PlayingGameState;
import com.gemserk.games.superflyingthing.gamestates.SelectPlayModeGameState;
import com.gemserk.games.superflyingthing.gamestates.SplashGameState;
import com.gemserk.games.superflyingthing.transitions.FadeInTransition;
import com.gemserk.games.superflyingthing.transitions.FadeOutTransition;

public class Game extends com.gemserk.commons.gdx.Game {

	private Screen playingScreen;
	private Screen splashScreen;
	private Screen mainMenuScreen;
	private Screen selectPlayModeScreen;

	public Screen getPlayingScreen() {
		return playingScreen;
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

	@Override
	public void create() {
		Converters.register(Vector2.class, LibgdxConverters.vector2());
		Converters.register(Color.class, LibgdxConverters.color());
		Converters.register(Float.class, Converters.floatValue());

		playingScreen = new ScreenImpl(new PlayingGameState(this));
		splashScreen = new ScreenImpl(new SplashGameState(this));
		mainMenuScreen = new ScreenImpl(new MainMenuGameState(this));
		selectPlayModeScreen = new ScreenImpl(new SelectPlayModeGameState(this));

		setScreen(splashScreen);
	}

	public void transition(final Screen screen, int leaveTime, int enterTime) {
		final Screen currentScreen = getScreen();
		setScreen(new TransitionScreen(new ScreenTransition( //
				new FadeOutTransition(currentScreen, leaveTime), //
				new FadeInTransition(screen, enterTime, new TransitionHandler() {
					public void onEnd() {
						// disposes current transition screen, not previous screen.
						setScreen(screen, true);
						currentScreen.dispose();
					};
				}))));
	}

}
