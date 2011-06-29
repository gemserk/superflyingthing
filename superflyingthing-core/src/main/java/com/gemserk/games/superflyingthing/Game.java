package com.gemserk.games.superflyingthing;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.gemserk.animation4j.converters.Converters;
import com.gemserk.animation4j.gdx.converters.LibgdxConverters;
import com.gemserk.commons.gdx.ScreenImpl;
import com.gemserk.games.superflyingthing.gamestates.PlayingGameState;

public class Game extends com.gemserk.commons.gdx.Game {
	
	@Override
	public void create() {
		Converters.register(Vector2.class, LibgdxConverters.vector2());
		Converters.register(Color.class, LibgdxConverters.color());
		setScreen(new ScreenImpl(new PlayingGameState()));
	}

	@Override
	public void render() {
		super.render();
		if (Gdx.input.isKeyPressed(Keys.R) || Gdx.input.isKeyPressed(Keys.MENU)) {
			getScreen().dispose();
			setScreen(new ScreenImpl(new PlayingGameState()));
		}
	}

}
