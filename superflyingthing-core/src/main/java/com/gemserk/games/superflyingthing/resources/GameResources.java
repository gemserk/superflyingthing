package com.gemserk.games.superflyingthing.resources;

import com.gemserk.commons.gdx.resources.LibgdxResourceBuilder;
import com.gemserk.resources.ResourceManager;

/**
 * Declares all resources needed for the game.
 */
public class GameResources extends LibgdxResourceBuilder {

	public static void load(ResourceManager<String> resourceManager) {
		new GameResources(resourceManager);
	}

	private GameResources(ResourceManager<String> resourceManager) {
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

		font("FpsFont", "data/fonts/purisa-18.png", "data/fonts/purisa-18.fnt", false);
		font("TitleFont", "data/fonts/purisa-18.png", "data/fonts/purisa-18.fnt", false);
		font("ButtonFont", "data/fonts/purisa-18.png", "data/fonts/purisa-18.fnt", false);
		font("GameFont", "data/fonts/purisa-18.png", "data/fonts/purisa-18.fnt", false);
		font("LevelFont", "data/fonts/purisa-18-bold.png", "data/fonts/purisa-18-bold.fnt", false);
		font("InstructionsFont", "data/fonts/purisa-18-bold.png", "data/fonts/purisa-18-bold.fnt", false);

		texture("PlanetTexture", "data/images/planet.png");
		sprite("Planet", "PlanetTexture");

		texture("PlanetBlurTexture", "data/images/planet_blur.png");
		sprite("PlanetBlur", "PlanetBlurTexture");

		texture("BackgroundTexture", "data/images/background01-1024x512.jpg", false);
		sprite("BackgroundSprite", "BackgroundTexture");

		particleEffect("ExplosionEffect", "data/particles/ExplosionEffect", "data/particles");
		particleEmitter("ExplosionEmitter", "ExplosionEffect", "Explosion");
		
		particleEffect("ThrustEffect", "data/particles/ThrustEffect", "data/particles");
		particleEmitter("ThrustEmitter", "ThrustEffect", "Thrust");
		
		texture("ObstacleTexture", "data/images/tile01.jpg", true);
	}


}