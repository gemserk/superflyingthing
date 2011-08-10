package com.gemserk.games.superflyingthing.gamestates;

import com.gemserk.commons.artemis.WorldWrapper;
import com.gemserk.games.superflyingthing.components.Components.GameData;
import com.gemserk.games.superflyingthing.scripts.controllers.ControllerType;

public class GameInformation {
	
	public static final int RandomGameMode = 0;
	public static final int PracticeGameMode = 1;
	public static final int ChallengeGameMode = 2;

	public static int gameMode = 0;
	public static int level = 0;

	public static WorldWrapper worldWrapper;
	
	public static String gameVersion;
	public static GameData gameData;
	
	public static ControllerType controllerType;
	
}
