package com.gemserk.games.superflyingthing;

import static org.junit.Assert.assertThat;

import org.hamcrest.core.IsEqual;
import org.junit.Test;


public class ProfileStatusTest {
	
	@Test
	public void latestLevelPlayedShouldBeZeroForNewProfile() {
		ProfileStatus profileStatus = new ProfileStatus();
		assertThat(profileStatus.getLatestLevelPlayed(), IsEqual.equalTo(0));
	}

	@Test
	public void shouldUpdateLatestLevelPlayedWithLastLevelTime() {
		ProfileStatus profileStatus = new ProfileStatus();
		profileStatus.setTimeForLevel(1, 500);
		assertThat(profileStatus.getLatestLevelPlayed(), IsEqual.equalTo(1));
	}

	@Test
	public void latestLevelPlayedShouldBeTheGreaterOne() {
		ProfileStatus profileStatus = new ProfileStatus();
		profileStatus.setTimeForLevel(1, 500);
		profileStatus.setTimeForLevel(2, 500);
		profileStatus.setTimeForLevel(1, 350);
		assertThat(profileStatus.getLatestLevelPlayed(), IsEqual.equalTo(2));
	}
	
	@Test
	public void shouldReturnFalseIfUserNotPlayedLevel() {
		ProfileStatus profileStatus = new ProfileStatus();
		profileStatus.setTimeForLevel(1, 500);
		assertThat(profileStatus.hasPlayedLevel(2), IsEqual.equalTo(false));
	}

	@Test
	public void shouldReturnTrueIfUserPlayedLevel() {
		ProfileStatus profileStatus = new ProfileStatus();
		profileStatus.setTimeForLevel(1, 500);
		assertThat(profileStatus.hasPlayedLevel(1), IsEqual.equalTo(true));
	}

	@Test
	public void shouldStoreAndReturnTimeForLevel() {
		ProfileStatus profileStatus = new ProfileStatus();
		profileStatus.setTimeForLevel(1, 500);
		assertThat(profileStatus.getTimeForLevel(1), IsEqual.equalTo(500));
	}
	
	@Test
	public void shouldStoreAndReturnBestTimeForLevel() {
		ProfileStatus profileStatus = new ProfileStatus();
		profileStatus.setTimeForLevel(1, 350);
		profileStatus.setTimeForLevel(1, 500);
		assertThat(profileStatus.getTimeForLevel(1), IsEqual.equalTo(350));
	}

}
