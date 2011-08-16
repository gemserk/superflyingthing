package com.gemserk.games.superflyingthing;

public class Events {

	public static final String enablePlanetReleaseShip = "enablePlanetReleaseShip".intern();
	public static final String disablePlanetReleaseShip = "disablePlanetReleaseShip".intern();
	public static final String moveCameraToEntity = "moveCameraToPlanet".intern();
	public static final String cameraReachedTarget = "cameraReachedTarget".intern();
	public static final String itemTaken = "itemTaken".intern();
	public static final String shipDeath = "shipDeath".intern();
	public static final String destinationPlanetReached = "destinationPlanetReached".intern();

	/**
	 * Should be registered when the game mode has started.
	 */
	public static final String gameStarted = "gameStarted";

	/**
	 * Should be registered when the game mode has finished (example: the ship reached the planet).
	 */
	public static final String gameFinished = "gameFinished";
	
	
	/**
	 * Sent when the ship was released from the planet.
	 */
	public static final String shipReleased = "shipReleased";

}
