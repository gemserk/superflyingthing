package com.gemserk.games.superflyingthing;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.gemserk.games.superflyingthing.Game;

public class DesktopApplication {

	public static void main(String[] argv) {
		new LwjglApplication(new Game(), "Super Flying Thing Prototype", 800, 480, false);
	}

}