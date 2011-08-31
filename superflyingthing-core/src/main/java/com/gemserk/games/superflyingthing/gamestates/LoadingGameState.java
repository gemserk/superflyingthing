package com.gemserk.games.superflyingthing.gamestates;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.gemserk.commons.gdx.graphics.SpriteBatchUtils;

public class LoadingGameState extends com.gemserk.commons.gdx.screens.LoadingGameState {

	private GL10 gl;
	private SpriteBatch spriteBatch;
	private BitmapFont font;

	@Override
	public void init() {
		super.init();
		gl = Gdx.graphics.getGL10();
		spriteBatch = new SpriteBatch();
		font = new BitmapFont();
		font.setColor(1f, 1f, 0f, 1f);
	}

	@Override
	public void render() {
		super.render();
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		spriteBatch.begin();
		float percentage = getTaskQueue().getProgress().getPercentage();
		// font.draw(spriteBatch, "Loading " + (int) (percentage) + "%...", 30, 30);
		SpriteBatchUtils.drawMultilineTextCentered(spriteBatch, font, "Loading " + (int) (percentage) + "%...", Gdx.graphics.getWidth() * 0.5f, Gdx.graphics.getHeight() * 0.5f);
		spriteBatch.end();
	}

	@Override
	public void dispose() {
		super.dispose();
		spriteBatch.dispose();
		font.dispose();
	}

}
