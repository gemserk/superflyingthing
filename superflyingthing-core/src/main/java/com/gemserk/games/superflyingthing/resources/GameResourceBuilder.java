package com.gemserk.games.superflyingthing.resources;

import com.gemserk.commons.gdx.resources.LibgdxResourceBuilder;
import com.gemserk.resources.ResourceManager;

/**
 * Declares all resources needed for the game.
 */
public class GameResourceBuilder extends LibgdxResourceBuilder {
	
	public static void loadResources(ResourceManager<String> resourceManager) {
		new GameResourceBuilder(resourceManager);
	}

	private GameResourceBuilder(ResourceManager<String> resourceManager) {
		super(resourceManager);
		texture("GemserkLogoTexture", "data/images/logo-gemserk-512x128.png");
		texture("GemserkLogoTextureBlur", "data/images/logo-gemserk-512x128-blur.png");
		texture("LwjglLogoTexture", "data/images/logo-lwjgl-512x256-inverted.png");
		texture("LibgdxLogoTexture", "data/images/logo-libgdx-clockwork-512x256.png");
		texture("WhiteRectangleTexture", "data/images/white-rectangle.png");
		
		sprite("GemserkLogo", "GemserkLogoTexture");
		sprite("GemserkLogoBlur", "GemserkLogoTextureBlur");
		sprite("LwjglLogo", "LwjglLogoTexture", 0, 0, 512, 185);
		sprite("LibgdxLogo", "LibgdxLogoTexture", 0, 25, 512, 256 - 50);
		sprite("WhiteRectangle", "WhiteRectangleTexture");
	}

}