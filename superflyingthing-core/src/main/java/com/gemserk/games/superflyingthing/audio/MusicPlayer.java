package com.gemserk.games.superflyingthing.audio;

import com.badlogic.gdx.audio.Music;
import com.gemserk.commons.gdx.audio.SoundPlayer;
import com.gemserk.resources.Resource;

public class MusicPlayer {

	SoundPlayer soundPlayer;
	Resource<Music> backgroundMusicResource;

	public MusicPlayer(SoundPlayer soundPlayer, Resource backgroundMusicResource) {
		this.soundPlayer = soundPlayer;
		this.backgroundMusicResource = backgroundMusicResource;
	}

	public void resumeGameMusic() {
		Music music = backgroundMusicResource.get();
		music.setVolume(soundPlayer.getVolume());
		music.setLooping(true);
		if (!music.isPlaying())
			music.play();
	}

	public void pauseGameMusic() {
		Music music = backgroundMusicResource.get();
		music.stop();
	}
	
}
