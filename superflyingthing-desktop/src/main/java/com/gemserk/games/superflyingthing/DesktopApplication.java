package com.gemserk.games.superflyingthing;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.gemserk.commons.adwhirl.AdWhirlViewHandler;

public class DesktopApplication {

	public static void main(String[] argv) {
		new LwjglApplication(new Game(new AdWhirlViewHandler()), "Super Flying Thing Prototype", 800, 480, false);
	}

}
