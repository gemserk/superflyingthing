package com.gemserk.games.superflyingthing.resources;

import com.badlogic.gdx.Gdx;
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
		
		texture("LevelButtonTexture", "data/images/level-button.png");
		sprite("LevelButtonSprite", "LevelButtonTexture");

		if (Gdx.graphics.getHeight() >= 480f) {
			font("FpsFont", "data/fonts/purisa-18.png", "data/fonts/purisa-18.fnt", false);
			font("TitleFont", "data/fonts/purisa-24.png", "data/fonts/purisa-24.fnt", false);
			font("ButtonFont", "data/fonts/purisa-18.png", "data/fonts/purisa-18.fnt", false);
			font("GameFont", "data/fonts/purisa-24.png", "data/fonts/purisa-24.fnt", false);
			font("LevelFont", "data/fonts/purisa-18-bold.png", "data/fonts/purisa-18-bold.fnt", false);
			font("InstructionsFont", "data/fonts/purisa-18-bold.png", "data/fonts/purisa-18-bold.fnt", false);
			font("VersionFont", "data/fonts/purisa-14.png", "data/fonts/purisa-14.fnt", false);
		} else if (Gdx.graphics.getHeight() >= 320){
			font("FpsFont", "data/fonts/purisa-14.png", "data/fonts/purisa-14.fnt", false);
			font("TitleFont", "data/fonts/purisa-14.png", "data/fonts/purisa-14.fnt", false);
			font("ButtonFont", "data/fonts/purisa-14.png", "data/fonts/purisa-14.fnt", false);
			font("GameFont", "data/fonts/purisa-14.png", "data/fonts/purisa-14.fnt", false);
			font("LevelFont", "data/fonts/purisa-14.png", "data/fonts/purisa-14.fnt", false);
			font("InstructionsFont", "data/fonts/purisa-12.png", "data/fonts/purisa-12.fnt", false);
			font("VersionFont", "data/fonts/purisa-10.png", "data/fonts/purisa-10.fnt", false);
		} else {
			font("FpsFont", "data/fonts/purisa-12.png", "data/fonts/purisa-12.fnt", false);
			font("TitleFont", "data/fonts/purisa-12.png", "data/fonts/purisa-12.fnt", false);
			font("ButtonFont", "data/fonts/purisa-12.png", "data/fonts/purisa-12.fnt", false);
			font("GameFont", "data/fonts/purisa-12.png", "data/fonts/purisa-12.fnt", false);
			font("LevelFont", "data/fonts/purisa-12.png", "data/fonts/purisa-12.fnt", false);
			font("InstructionsFont", "data/fonts/purisa-10.png", "data/fonts/purisa-10.fnt", false);
			font("VersionFont", "data/fonts/purisa-10.png", "data/fonts/purisa-10.fnt", false);
		}

		texture("PlanetTexture", "data/images/planet.png");
		sprite("Planet", "PlanetTexture");

		texture("PlanetBlurTexture", "data/images/planet_blur.png");
		sprite("PlanetBlur", "PlanetBlurTexture");

		texture("BackgroundTexture", "data/images/background01-1024x512.jpg", false);
		sprite("BackgroundSprite", "BackgroundTexture");

		particleEffect("ExplosionEffect", "data/particles/ExplosionEffect", "data/particles");
		particleEmitter("ExplosionEmitter", "ExplosionEffect", "Explosion");

		particleEffect("LaserHitEffect", "data/particles/LaserHitEffect", "data/particles");
		particleEmitter("LaserHitEmitter", "LaserHitEffect", "LaserHit");

		texture("ThrustTexture", "data/particles/particle.png");
		sprite("ThrustSprite", "ThrustTexture");

		particleEffect("ThrustEffect", "data/particles/ThrustEffect", "data/particles");
		particleEmitter("ThrustEmitter", "ThrustEffect", "Thrust");

		texture("ObstacleTexture", "data/images/tile01.jpg", true);

		texture("ItemTexture", "data/images/item.png");
		sprite("Item", "ItemTexture");

		texture("PortalTexture", "data/images/portal.png");
		sprite("PortalSprite", "PortalTexture");
		
		texture("LaserTexture", "data/images/laser-bullet.png");
		sprite("LaserSprite", "LaserTexture");

		texture("ShipSpriteSheet", "data/images/ship_animation.png");
		animation("ShipAnimation", "ShipSpriteSheet", 0, 0, 64, 64, 36, false, 0);

		texture("StarSpriteSheet", "data/images/star-animation.png");
		animation("StarAnimation", "StarSpriteSheet", 0, 0, 64, 64, 36, false, 0);

		texture("LaserGunSpriteSheet", "data/images/laser-gun-animation.png");
		animation("LaserGunAnimation", "LaserGunSpriteSheet", 0, 0, 64, 64, 36, true, 100);
		
		texture("PlanetFillAnimationSpriteSheet", "data/images/planet-fill-animation.png");
		animation("PlanetFillAnimation", "PlanetFillAnimationSpriteSheet", 0, 0, 128, 128, 11, false, 75);
		
		texture("IconsSpriteSheet", "data/images/icons.png", true);
		
		sprite("StarSprite", "IconsSpriteSheet", 0, 0, 64, 64);
		sprite("TickSprite", "IconsSpriteSheet", 0, 64, 64, 64);

	}

}