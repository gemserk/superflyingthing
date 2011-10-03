package com.gemserk.games.superflyingthing.gamestates;

import java.util.ArrayList;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.gemserk.animation4j.transitions.Transitions;
import com.gemserk.animation4j.transitions.sync.Synchronizers;
import com.gemserk.commons.adwhirl.AdWhirlViewHandler;
import com.gemserk.commons.gdx.graphics.SpriteBatchUtils;
import com.gemserk.commons.gdx.graphics.SpriteUtils;
import com.gemserk.games.superflyingthing.CustomResourceManager;
import com.gemserk.games.superflyingthing.ScreenManager;
import com.gemserk.games.superflyingthing.Screens;
import com.gemserk.resources.Resource;
import com.gemserk.resources.progress.TaskQueue;
import com.gemserk.resources.progress.tasks.SimulateLoadingTimeRunnable;

public class SplashGameState extends com.gemserk.commons.gdx.gamestates.LoadingGameState {

	CustomResourceManager<String> resourceManager;
	AdWhirlViewHandler adWhirlViewHandler;
	ScreenManager screenManager;

	private SpriteBatch spriteBatch;
	private BitmapFont font;

	private Sprite gemserkLogo;
	private Sprite lwjglLogo;
	private Sprite libgdxLogo;
	private Sprite gemserkLogoBlur;

	private Color blurColor = new Color();
	
	@Override
	public void init() {
		super.init();
		int width = Gdx.graphics.getWidth();
		int height = Gdx.graphics.getHeight();

		int centerX = width / 2;
		int centerY = height / 2;

		spriteBatch = new SpriteBatch();
		font = new BitmapFont();
		font.setColor(1f, 1f, 0f, 1f);
		
		gemserkLogo = resourceManager.getResourceValue("GemserkLogo");
		gemserkLogoBlur = resourceManager.getResourceValue("GemserkLogoBlur");
		lwjglLogo = resourceManager.getResourceValue("LwjglLogo");
		libgdxLogo = resourceManager.getResourceValue("LibgdxLogo");

		SpriteUtils.resize(gemserkLogo, width * 0.8f);
		SpriteUtils.resize(gemserkLogoBlur, width * 0.8f);
		SpriteUtils.resize(lwjglLogo, width * 0.2f);
		SpriteUtils.resize(libgdxLogo, width * 0.2f);

		SpriteUtils.centerOn(gemserkLogo, centerX, centerY);
		SpriteUtils.centerOn(gemserkLogoBlur, centerX, centerY);
		SpriteUtils.centerOn(lwjglLogo, width * 0.85f, lwjglLogo.getHeight() * 0.5f);
		SpriteUtils.centerOn(libgdxLogo, width * 0.15f, libgdxLogo.getHeight() * 0.5f);

		Synchronizers.transition(blurColor, Transitions.transitionBuilder(new Color(0f, 0f, 1f, 0f)).end(new Color(0f, 0f, 1f, 1f)).time(1f));

		TaskQueue taskQueue = super.getTaskQueue();

		taskQueue.add(new SimulateLoadingTimeRunnable(0));

		ArrayList<String> registeredResources = resourceManager.getRegisteredResources();
		for (int i = 0; i < registeredResources.size(); i++) {
			final String resourceId = registeredResources.get(i);
			taskQueue.add(new Runnable() {
				@Override
				public void run() {
					Gdx.app.log("SuperFlyingThing", "Loading resource: " + resourceId);
					Resource resource = resourceManager.get(resourceId);
					resource.load();
				}
			}, "Loading assets");
		}

		taskQueue.add(new Runnable() {
			@Override
			public void run() {
				mainMenu();
			}
		});
	}

	private void mainMenu() {
		screenManager.transition(Screens.MainMenu) //
				.disposeCurrent() //
				.start();
	}

	@Override
	public void render() {
		// super.render();

		Gdx.graphics.getGL10().glClear(GL10.GL_COLOR_BUFFER_BIT);
		spriteBatch.begin();
		gemserkLogoBlur.setColor(blurColor);
		gemserkLogoBlur.draw(spriteBatch);
		gemserkLogo.draw(spriteBatch);
		if (Gdx.app.getType() != ApplicationType.Android)
			lwjglLogo.draw(spriteBatch);
		libgdxLogo.draw(spriteBatch);

		float percentage = getTaskQueue().getProgress().getPercentage();
		String currentTaskName = getTaskQueue().getCurrentTaskName();
		if ("".equals(currentTaskName))
			currentTaskName = "Loading ";
		SpriteBatchUtils.drawMultilineTextCentered(spriteBatch, font, currentTaskName + " - " + (int) (percentage) + "%...", //
				Gdx.graphics.getWidth() * 0.5f, Gdx.graphics.getHeight() * 0.25f);

		spriteBatch.end();

		super.render();
	}

	@Override
	public void update() {
		Synchronizers.synchronize(getDelta());
	}

	@Override
	public void resume() {
		adWhirlViewHandler.hide();
		Gdx.input.setCatchBackKey(false);
	}

	@Override
	public void dispose() {
		spriteBatch.dispose();
		spriteBatch = null;
	}

}
