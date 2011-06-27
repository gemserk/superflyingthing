package com.gemserk.games.discovertheway;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;

public class DesktopApplication {

	public static void main(String[] argv) {
		new LwjglApplication(new Game(), "Discover The Way", 800, 480, false);
	}

}
