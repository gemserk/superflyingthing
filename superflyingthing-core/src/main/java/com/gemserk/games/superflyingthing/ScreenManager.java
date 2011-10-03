package com.gemserk.games.superflyingthing;

import java.util.HashMap;
import java.util.Map;

import com.gemserk.commons.gdx.GameState;
import com.gemserk.commons.gdx.Screen;
import com.gemserk.commons.gdx.ScreenImpl;

public class ScreenManager {

	private Map<String, Screen> screens;

	public ScreenManager() {
		screens = new HashMap<String, Screen>();
	}

	/**
	 * Adds a new screen for the specified game state.
	 * 
	 * @param id
	 *            The Screen identifier.
	 * @param gameState
	 *            The GameState instance to be used for the new Screen.
	 */
	public void add(String id, GameState gameState) {
		screens.put(id, screen(gameState));
	}

	/**
	 * Adds the specified screen with the specified id.
	 * 
	 * @param id
	 *            The Screen identifier.
	 * @param screen
	 *            The Screen instance.
	 */
	public void add(String id, Screen screen) {
		screens.put(id, screen);
	}

	/**
	 * Creates a new screen for the specified gamestate.
	 */
	private Screen screen(GameState gameState) {
		return new ScreenImpl(gameState);
	}

	public Screen get(String id) {
		return screens.get(id);
	}

}