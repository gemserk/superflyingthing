package com.gemserk.games.superflyingthing.scripts;

import com.artemis.Entity;
import com.artemis.World;
import com.gemserk.commons.artemis.events.Event;
import com.gemserk.commons.artemis.events.reflection.Handles;
import com.gemserk.commons.artemis.scripts.ScriptJavaImpl;
import com.gemserk.commons.gdx.GlobalTime;
import com.gemserk.games.superflyingthing.Events;
import com.gemserk.games.superflyingthing.components.Components.GameData;

public class CountTravelTimeScript extends ScriptJavaImpl {
	
	GameData gameData;
	boolean updateTimer;

	public CountTravelTimeScript(GameData gameData) {
		this.gameData = gameData;
	}
	
	@Handles(ids=Events.shipReleased)
	public void startTimerWhenShipReleased(Event e) {
		gameData.travelTime = 0f;
		this.updateTimer = true;
	}
	
	@Handles(ids=Events.destinationPlanetReached)
	public void stopTimerWhenShipReachedTarget(Event e) {
		this.updateTimer = false;
		gameData.travelTime -= gameData.travelTime % 0.001f; 
	}
	
	
	@Override
	public void update(World world, Entity e) {
		if (!updateTimer)
			return;
		gameData.travelTime += GlobalTime.getDelta();
	}

}
