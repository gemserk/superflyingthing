package com.gemserk.games.discovertheway;

import java.text.MessageFormat;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.gemserk.animation4j.converters.Converters;
import com.gemserk.animation4j.gdx.converters.LibgdxConverters;
import com.gemserk.commons.gdx.GameStateImpl;
import com.gemserk.commons.gdx.ScreenImpl;
import com.gemserk.commons.gdx.camera.Libgdx2dCamera;
import com.gemserk.commons.gdx.camera.Libgdx2dCameraTransformImpl;

public class Game extends com.gemserk.commons.gdx.Game {

	class AccelerometerTest extends GameStateImpl {

		private SpriteBatch spriteBatch;
		// private Texture whiteRectangle;

		private Vector2 position;
		private Vector2 velocity;
		private Vector2 acceleration;

		private Libgdx2dCamera camera;
		private Sprite whiteRectangleSprite;

		@Override
		public void init() {
			spriteBatch = new SpriteBatch();
			position = new Vector2();
			velocity = new Vector2();
			acceleration = new Vector2();
			camera = new Libgdx2dCameraTransformImpl();
			camera.zoom(16f);

			Texture whiteRectangle = new Texture(Gdx.files.internal("data/images/white-rectangle.png"));
			whiteRectangleSprite = new Sprite(whiteRectangle);
			whiteRectangleSprite.setSize(2f, 2f);
		}

		@Override
		public void render(int delta) {
			Gdx.app.log("Discover The Name", MessageFormat.format("accelerometer - {0},{1},{2}", Gdx.input.getAccelerometerX(), Gdx.input.getAccelerometerY(), Gdx.input.getAccelerometerZ()));
			Gdx.app.log("Discover The Name", MessageFormat.format("rotation - {0},{1},{2}", Gdx.input.getPitch(), Gdx.input.getRoll(), Gdx.input.getAzimuth()));
			
			Gdx.graphics.getGL10().glClear(GL10.GL_COLOR_BUFFER_BIT);
			camera.apply(spriteBatch);
			spriteBatch.begin();
			// spriteBatch.draw(whiteRectangle, position.x, position.y);
			whiteRectangleSprite.draw(spriteBatch);
			spriteBatch.end();
		}

		@Override
		public void update(int delta) {

			if (Gdx.app.getType() == ApplicationType.Android) {
				acceleration.x = -Gdx.input.getAccelerometerX();
				acceleration.y = -Gdx.input.getAccelerometerY();
			} 

			velocity.x += acceleration.x * 0.001f * delta;
			velocity.y += acceleration.y * 0.001f * delta;

			position.x += velocity.x * 0.001f * delta;
			position.y += velocity.y * 0.001f * delta;

			if (position.x > Gdx.graphics.getWidth() - whiteRectangleSprite.getWidth()) {
				position.x = Gdx.graphics.getWidth() - whiteRectangleSprite.getWidth();
				velocity.x = 0f;
			}

			if (position.x < 0f) {
				position.x = 0f;
				velocity.x = 0f;
			}

			if (position.y > Gdx.graphics.getHeight() - whiteRectangleSprite.getHeight()) {
				position.y = Gdx.graphics.getHeight() - whiteRectangleSprite.getHeight();
				velocity.y = 0f;
			}

			if (position.y < 0f) {
				position.y = 0f;
				velocity.y = 0f;
			}

			whiteRectangleSprite.setPosition(position.x, position.y);
		}

		@Override
		public void dispose() {
			spriteBatch.dispose();
		}

	}

	private AccelerometerTest accelerometerTestGameState;

	@Override
	public void create() {
		Converters.register(Vector2.class, LibgdxConverters.vector2());
		Converters.register(Color.class, LibgdxConverters.color());

		accelerometerTestGameState = new AccelerometerTest();

		setScreen(new ScreenImpl(accelerometerTestGameState));
	}

}
