package com.gemserk.games.superflyingthing.gamestates;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.gemserk.commons.gdx.graphics.SpriteBatchUtils;
import com.gemserk.games.superflyingthing.CustomResourceManager;
import com.gemserk.games.superflyingthing.Game;
import com.gemserk.games.superflyingthing.Screens;
import com.gemserk.games.superflyingthing.levels.Levels;
import com.gemserk.resources.progress.TaskQueue;
import com.gemserk.resources.progress.tasks.SimulateLoadingTimeRunnable;

public class LoadingGameState extends com.gemserk.commons.gdx.screens.LoadingGameState {

	private GL10 gl;
	private SpriteBatch spriteBatch;
	private BitmapFont font;
	
	private final Game game;
	
	public LoadingGameState(Game game) {
		this.game = game;
	}

	@Override
	public void init() {
		
		TaskQueue taskQueue = new TaskQueue();
		
		gl = Gdx.graphics.getGL10();
		spriteBatch = new SpriteBatch();
		font = new BitmapFont();
		font.setColor(1f, 1f, 0f, 1f);
		
		taskQueue.add(new SimulateLoadingTimeRunnable(0));
		taskQueue.add(new Runnable() {
			@Override
			public void run() {
				CustomResourceManager<String> resourceManager = game.getResourceManager();
				ArrayList<String> registeredResources = resourceManager.getRegisteredResources();
				for (int i = 0; i < registeredResources.size(); i++) {
					String resourceId = registeredResources.get(i);
					resourceManager.get(resourceId).reload();
					// it would be nicer to have load/unload in the resource API, so I could call load() here, not reload()
				}
				
			}
		}, "Loading assets");
		taskQueue.add(new Runnable() {
			@Override
			public void run() {
				int levelsCount = Levels.levelsCount();
				for (int i = 1; i <= levelsCount; i++) {
					Levels.level(i);
				}
			}
		}, "Loading levels");
		
		taskQueue.add(new Runnable() {
			@Override
			public void run() {
				game.transition(Screens.MainMenu).start();
			}
		});
		
		getParameters().put("taskQueue", taskQueue);
		super.init();
	}

	@Override
	public void render() {
		super.render();
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		spriteBatch.begin();
		float percentage = getTaskQueue().getProgress().getPercentage();
		String currentTaskName = getTaskQueue().getCurrentTaskName();
		if ("".equals(currentTaskName))
			currentTaskName = "Loading ";
		SpriteBatchUtils.drawMultilineTextCentered(spriteBatch, font, currentTaskName + " - " + (int) (percentage) + "%...", Gdx.graphics.getWidth() * 0.5f, Gdx.graphics.getHeight() * 0.5f);
		spriteBatch.end();
	}

	@Override
	public void dispose() {
		super.dispose();
		spriteBatch.dispose();
		font.dispose();
	}

}
