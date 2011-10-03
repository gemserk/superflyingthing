package com.gemserk.games.superflyingthing;

import com.gemserk.commons.gdx.GameState;
import com.gemserk.commons.gdx.Screen;
import com.gemserk.commons.gdx.screens.transitions.TransitionBuilder;

public interface ScreenManager {

	/**
	 * Adds a new screen for the specified game state.
	 * 
	 * @param id
	 *            The Screen identifier.
	 * @param gameState
	 *            The GameState instance to be used for the new Screen.
	 */
	void add(String id, GameState gameState);

	/**
	 * Adds the specified screen with the specified id.
	 * 
	 * @param id
	 *            The Screen identifier.
	 * @param screen
	 *            The Screen instance.
	 */
	void add(String id, Screen screen);

	/**
	 * Returns the Screen registered with that identifier.
	 */
	Screen get(String id);

	/**
	 * Returns a TransitionBuilder to build game screen transitions.
	 * 
	 * @param screenId
	 *            The Screen identifier.
	 */
	TransitionBuilder transition(String screenId);

}