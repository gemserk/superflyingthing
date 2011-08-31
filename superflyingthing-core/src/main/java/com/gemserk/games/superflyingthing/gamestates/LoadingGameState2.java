package com.gemserk.games.superflyingthing.gamestates;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.gemserk.commons.gdx.GameState;
import com.gemserk.commons.gdx.graphics.SpriteBatchUtils;
import com.gemserk.resources.progress.tasks.SimulateLoadingTimeRunnable;

public class LoadingGameState2 extends com.gemserk.commons.gdx.gamestates.LoadingGameState2 {

	private GL10 gl;
	private SpriteBatch spriteBatch;
	private BitmapFont font;

	public LoadingGameState2(GameState gameState) {
		super(gameState);
	}

	@Override
	protected void internalInit() {
		gl = Gdx.graphics.getGL10();
		spriteBatch = new SpriteBatch();
		font = new BitmapFont();
		font.setColor(1f, 1f, 0f, 1f);
		getTaskQueue().add(new SimulateLoadingTimeRunnable(0));
	}

	@Override
	protected void internalRender() {
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		spriteBatch.begin();
		float percentage = getTaskQueue().getProgress().getPercentage();
		SpriteBatchUtils.drawMultilineText(spriteBatch, font, "Loading " + (int) (percentage) + "%...", Gdx.graphics.getWidth() * 0.05f, Gdx.graphics.getHeight() * 0.05f, 0f, 0.5f);
		spriteBatch.end();
	}

	@Override
	protected void internalDispose() {
		spriteBatch.dispose();
		font.dispose();
	}

}
