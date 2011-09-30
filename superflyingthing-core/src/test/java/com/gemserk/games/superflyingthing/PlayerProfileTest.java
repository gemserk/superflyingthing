package com.gemserk.games.superflyingthing;

import static org.junit.Assert.assertThat;

import org.hamcrest.core.IsEqual;
import org.junit.Test;

import com.gemserk.games.superflyingthing.preferences.PlayerProfile;
import com.gemserk.games.superflyingthing.preferences.PlayerProfile.LevelInformation;


public class PlayerProfileTest {
	
	@Test
	public void latestLevelPlayedShouldBeZeroForNewProfile() {
		PlayerProfile playerProfile = new PlayerProfile();
		assertThat(playerProfile.getLatestLevelPlayed(), IsEqual.equalTo(0));
	}

	@Test
	public void shouldUpdateLatestLevelPlayedWithLastLevelTime() {
		PlayerProfile playerProfile = new PlayerProfile();
		playerProfile.setLevelInformationForLevel(1, new LevelInformation(500, 0));
		assertThat(playerProfile.getLatestLevelPlayed(), IsEqual.equalTo(1));
	}

	@Test
	public void latestLevelPlayedShouldBeTheGreaterOne() {
		PlayerProfile playerProfile = new PlayerProfile();
		playerProfile.setLevelInformationForLevel(1, new LevelInformation(500, 0));
		playerProfile.setLevelInformationForLevel(2, new LevelInformation(500, 0));
		playerProfile.setLevelInformationForLevel(1, new LevelInformation(350, 0));
		assertThat(playerProfile.getLatestLevelPlayed(), IsEqual.equalTo(2));
	}
	
	@Test
	public void shouldReturnFalseIfUserNotPlayedLevel() {
		PlayerProfile playerProfile = new PlayerProfile();
		playerProfile.setLevelInformationForLevel(1, new LevelInformation(500, 0));
		assertThat(playerProfile.hasPlayedLevel(2), IsEqual.equalTo(false));
	}

	@Test
	public void shouldReturnTrueIfUserPlayedLevel() {
		PlayerProfile playerProfile = new PlayerProfile();
		playerProfile.setLevelInformationForLevel(1, new LevelInformation(500, 0));
		assertThat(playerProfile.hasPlayedLevel(1), IsEqual.equalTo(true));
	}
	
	@Test
	public void shouldReturnHasPlayedLevelForLevelZero() {
		PlayerProfile playerProfile = new PlayerProfile();
		assertThat(playerProfile.hasPlayedLevel(0), IsEqual.equalTo(true));
	}

	@Test
	public void shouldStoreAndReturnTimeForLevel() {
		PlayerProfile playerProfile = new PlayerProfile();
		playerProfile.setLevelInformationForLevel(1, new LevelInformation(500, 0));
		assertThat(playerProfile.getLevelInformation(1).time, IsEqual.equalTo(500f));
	}
	
	@Test
	public void shouldStoreAndReturnBestTimeForLevel() {
		PlayerProfile playerProfile = new PlayerProfile();
		playerProfile.setLevelInformationForLevel(1, new LevelInformation(350, 0));
		playerProfile.setLevelInformationForLevel(1, new LevelInformation(500, 0));
		assertThat(playerProfile.getLevelInformation(1).time, IsEqual.equalTo(350f));
	}

}
