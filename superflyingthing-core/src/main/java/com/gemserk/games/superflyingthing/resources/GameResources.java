package com.gemserk.games.superflyingthing.resources;

import com.badlogic.gdx.Gdx;
import com.gemserk.commons.gdx.resources.LibgdxResourceBuilder;
import com.gemserk.games.superflyingthing.levels.Levels;
import com.gemserk.resources.ResourceManager;
import com.gemserk.resources.datasources.DataSourceFactory;
import com.gemserk.resources.monitor.FilesMonitor;

/**
 * Declares all resources needed for the game.
 */
public class GameResources extends LibgdxResourceBuilder {
	
	public static class Sprites {
		
		public static final String LeftButton = "LeftButtonSprite";
		public static final String RightButton = "RightButtonSprite";
		
		public static final String RestartButton = "RestartButtonSprite";
		public static final String MenuButton = "MenuButtonSprite";
		public static final String NextLevelButton = "NextLevelButtonSprite";
		
		public static final String SquareButton = "SquareButtonSprite";
		public static final String ToggleBackgroundButton = "ToggleBackgroundButtonSprite";
		public static final String ToggleSoundsButton = "ToggleSoundsButtonSprite";
		public static final String DisabledButtonOverlay = "DisabledButtonOverlaySprite";
		public static final String LeftPanelBackground = "LeftPanelBackgroundSprite";
		
	}

	private final FilesMonitor filesMonitor;

	public static void load(ResourceManager<String> resourceManager, FilesMonitor filesMonitor) {
		new GameResources(resourceManager, filesMonitor);
	}

	private GameResources(ResourceManager<String> resourceManager, FilesMonitor filesMonitor) {
		super(resourceManager);
		this.filesMonitor = filesMonitor;
		texture("GemserkLogoTexture", "data/images/logos/logo-gemserk-512x128.png");
		texture("GemserkLogoTextureBlur", "data/images/logos/logo-gemserk-512x128-blur.png");
		texture("LwjglLogoTexture", "data/images/logos/logo-lwjgl-512x256-inverted.png");
		texture("LibgdxLogoTexture", "data/images/logos/logo-libgdx-clockwork-512x256.png");

		sprite("GemserkLogo", "GemserkLogoTexture");
		sprite("GemserkLogoBlur", "GemserkLogoTextureBlur");
		sprite("LwjglLogo", "LwjglLogoTexture", 0, 0, 512, 185);
		sprite("LibgdxLogo", "LibgdxLogoTexture", 0, 25, 512, 256 - 50);

		if (Gdx.graphics.getHeight() >= 480f) {
			font("FpsFont", "data/fonts/purisa-18.png", "data/fonts/purisa-18.fnt", false);
			font("TitleFont", "data/fonts/purisa-24.png", "data/fonts/purisa-24.fnt", false);
			font("ButtonFont", "data/fonts/purisa-18.png", "data/fonts/purisa-18.fnt", false);
			font("GameFont", "data/fonts/purisa-24.png", "data/fonts/purisa-24.fnt", false);
			font("LevelFont", "data/fonts/purisa-18-bold.png", "data/fonts/purisa-18-bold.fnt", false);
			font("InstructionsFont", "data/fonts/purisa-18-bold.png", "data/fonts/purisa-18-bold.fnt", false);
			font("VersionFont", "data/fonts/purisa-14.png", "data/fonts/purisa-14.fnt", false);
		} else if (Gdx.graphics.getHeight() >= 320) {
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

		texture("BackgroundTexture", "data/images/background01-1024x512.jpg", false);
		sprite("BackgroundSprite", "BackgroundTexture");

		particleEffect("ExplosionEffect", "data/particles/ExplosionEffect", "data/particles");
		particleEmitter("ExplosionEmitter", "ExplosionEffect", "Explosion");

		particleEffect("LaserHitEffect", "data/particles/LaserHitEffect", "data/particles");
		particleEmitter("LaserHitEmitter", "LaserHitEffect", "LaserHit");

		texture("ObstacleTexture", "data/images/tile01.png", true);

		texture("ShipSpriteSheet", "data/images/spritesheets/ship-animation.png");
		animation("ShipAnimation", "ShipSpriteSheet", 0, 0, 57, 57, 72, false, 0);

		texture("StarSpriteSheet", "data/images/spritesheets/star-animation.png");
		animation("StarAnimation", "StarSpriteSheet", 0, 0, 64, 64, 36, false, 0);

		texture("LaserGunSpriteSheet", "data/images/spritesheets/laser-gun-animation.png");
		animation("LaserGunAnimation", "LaserGunSpriteSheet", 0, 0, 64, 64, 36, true, 100);

		texture("PlanetFillAnimationSpriteSheet", "data/images/spritesheets/planet-fill-animation.png");
		animation("PlanetFillAnimation", "PlanetFillAnimationSpriteSheet", 0, 0, 128, 128, 16, false, 65);

		textureAtlas("TextureAtlas", "data/images/packs/pack");
		spriteAtlas("WhiteRectangle", "TextureAtlas", "white-rectangle");
		spriteAtlas("Planet", "TextureAtlas", "planet");
		spriteAtlas("FogSprite", "TextureAtlas", "fog");
		spriteAtlas("PortalSprite", "TextureAtlas", "portal");
		spriteAtlas("LaserSprite", "TextureAtlas", "laser-bullet");
		spriteAtlas("TickSprite", "TextureAtlas", "tick");
		spriteAtlas("LevelButtonSprite", "TextureAtlas", "level-button");
		
		spriteAtlas(Sprites.LeftButton, "TextureAtlas", "button-left");
		spriteAtlas(Sprites.RightButton, "TextureAtlas", "button-right");
		spriteAtlas(Sprites.RestartButton, "TextureAtlas", "button-restart");
		spriteAtlas(Sprites.NextLevelButton, "TextureAtlas", "button-next");
		spriteAtlas(Sprites.MenuButton, "TextureAtlas", "button-menu");
		spriteAtlas(Sprites.SquareButton, "TextureAtlas", "squarebutton");

		spriteAtlas(Sprites.LeftPanelBackground, "TextureAtlas", "left-panel");
		spriteAtlas(Sprites.ToggleBackgroundButton, "TextureAtlas", "button-togglebackground");
		spriteAtlas(Sprites.ToggleSoundsButton, "TextureAtlas", "button-togglesounds");
		spriteAtlas(Sprites.DisabledButtonOverlay, "TextureAtlas", "button-disabled-overlay");

		xmlDocument("RandomLevelTilesDocument", "data/levels/level-tiles-template.svg");
		
		sound("ButtonReleasedSound", "data/audio/button01.wav");
		sound("ExplosionSound", "data/audio/explosion.ogg");

		Levels.declareLevelResources(resourceManager, filesMonitor);
	}
	
	@Override
	public void xmlDocument(String id, String file) {
		super.xmlDocument(id, file);
		filesMonitor.monitor(DataSourceFactory.classPathDataSource(file), resourceManager.get(id));
	}

}