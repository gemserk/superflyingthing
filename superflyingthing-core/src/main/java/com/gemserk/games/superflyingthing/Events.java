package com.gemserk.games.superflyingthing;

public class Events {

	public static final String enablePlanetReleaseShip = "enablePlanetReleaseShip";
	public static final String disablePlanetReleaseShip = "disablePlanetReleaseShip";
	public static final String moveCameraToEntity = "moveCameraToPlanet";
	public static final String cameraReachedTarget = "cameraReachedTarget";
	public static final String itemTaken = "itemTaken";
	public static final String shipDeath = "shipDeath";
	public static final String destinationPlanetReached = "destinationPlanetReached";
	
	// for effects
	
	public static final String explosion = "explosion".intern();

	/**
	 * Should be registered when the game mode has started.
	 */
	public static final String gameStarted = "gameStarted";

	/**
	 * Should be registered when the game mode has finished (example: the ship reached the planet).
	 */
	public static final String gameFinished = "gameFinished";
	
	public static final String gameOver = "gameOver";
	
	
	/**
	 * Sent when the ship was released from the planet.
	 */
	public static final String shipReleased = "shipReleased";
	
	/**
	 * Sent whenever a new ship is spawned.
	 */
	public static final String shipSpawned = "shipSpawned";
	
	// Gamestate events
	
	/**
	 * Used to send the event to all game states to toggle first background rendering.
	 */
	public static final String toggleFirstBackground = "toggleFirstBackground";
	
	/**
	 * Used to send the event to all game states to toggle second background rendering.
	 */
	public static final String toggleSecondBackground = "toggleSecondBackground";

	public static final String previewLevel = "previewLevel";
	public static final String previewRandomLevel = "previewRandomLevel";
	
}
