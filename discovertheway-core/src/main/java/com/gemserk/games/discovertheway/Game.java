package com.gemserk.games.discovertheway;

import java.text.MessageFormat;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.gemserk.animation4j.converters.Converters;
import com.gemserk.animation4j.gdx.converters.LibgdxConverters;

public class Game extends com.gemserk.commons.gdx.Game {

	@Override
	public void create() {
		Converters.register(Vector2.class, LibgdxConverters.vector2());
		Converters.register(Color.class, LibgdxConverters.color());
	}

	@Override
	public void render() {
		Gdx.app.log("Discover The Name", MessageFormat.format("{0},{1},{2}", Gdx.input.getAccelerometerX(), Gdx.input.getAccelerometerY(), Gdx.input.getAccelerometerZ()));
	}

}
