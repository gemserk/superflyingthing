package com.gemserk.games.superflyingthing;

import java.util.HashMap;
import java.util.Map;

import com.gemserk.commons.gdx.GameState;
import com.gemserk.commons.gdx.Screen;
import com.gemserk.commons.gdx.ScreenImpl;
import com.gemserk.commons.gdx.screens.transitions.TransitionBuilder;

public class ScreenManagerImpl implements ScreenManager {

	Map<String, Screen> screens;
	Game game;

	public ScreenManagerImpl(Game game) {
		this.game = game;
		screens = new HashMap<String, Screen>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gemserk.games.superflyingthing.ScreenManager#add(java.lang.String, com.gemserk.commons.gdx.GameState)
	 */
	@Override
	public void add(String id, GameState gameState) {
		screens.put(id, screen(gameState));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gemserk.games.superflyingthing.ScreenManager#add(java.lang.String, com.gemserk.commons.gdx.Screen)
	 */
	@Override
	public void add(String id, Screen screen) {
		screens.put(id, screen);
	}

	/**
	 * Creates a new screen for the specified gamestate.
	 */
	private Screen screen(GameState gameState) {
		return new ScreenImpl(gameState);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gemserk.games.superflyingthing.ScreenManager#get(java.lang.String)
	 */
	@Override
	public Screen get(String id) {
		return screens.get(id);
	}

	@Override
	public TransitionBuilder transition(String screenId) {
		return new TransitionBuilder(game, get(screenId));
	}

}