package com.gemserk.games.superflyingthing;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;

public class DesktopApplication {

	public static void main(String[] argv) {
		new LwjglApplication(new Game(){
			@Override
			public void create() {
				Gdx.graphics.setVSync(true);
				super.create();
			}
		}, "Super Flying Thing Prototype", 800, 480, false);
	}

}
