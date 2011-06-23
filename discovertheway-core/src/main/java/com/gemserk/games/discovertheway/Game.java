package com.gemserk.games.discovertheway;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.World;
import com.gemserk.animation4j.converters.Converters;
import com.gemserk.animation4j.gdx.converters.LibgdxConverters;
import com.gemserk.commons.gdx.GameStateImpl;
import com.gemserk.commons.gdx.ScreenImpl;
import com.gemserk.commons.gdx.box2d.BodyBuilder;
import com.gemserk.commons.gdx.camera.Libgdx2dCamera;
import com.gemserk.commons.gdx.camera.Libgdx2dCameraTransformImpl;
import com.gemserk.commons.gdx.graphics.ImmediateModeRendererUtils;
import com.gemserk.commons.gdx.graphics.SpriteBatchUtils;

public class Game extends com.gemserk.commons.gdx.Game {

	class SuperSheepGameState extends GameStateImpl {

		private SpriteBatch spriteBatch;
		private Libgdx2dCamera camera;
		private Sprite whiteRectangleSprite;
		private World world;
		private Body body;
		private Vector2 direction;

		@Override
		public void init() {
			spriteBatch = new SpriteBatch();
			camera = new Libgdx2dCameraTransformImpl();
			world = new World(new Vector2(), false);

			camera.zoom(32f);

			Texture whiteRectangle = new Texture(Gdx.files.internal("data/images/white-rectangle.png"));
			whiteRectangleSprite = new Sprite(whiteRectangle);
			whiteRectangleSprite.setSize(1f, 1f);

			BodyBuilder bodyBuilder = new BodyBuilder(world);
			body = bodyBuilder.mass(50f) //
					.boxShape(0.5f, 0.5f) //
					.position(0f, 0f) //
					.restitution(0f) //
					.type(BodyType.DynamicBody) //
					.build();
			direction = new Vector2(1f, 0f);
		}

		@Override
		public void render(int delta) {
			Vector2 position = body.getTransform().getPosition();

			Gdx.graphics.getGL10().glClear(GL10.GL_COLOR_BUFFER_BIT);
			camera.apply(spriteBatch);
			spriteBatch.begin();
			SpriteBatchUtils.drawCentered(spriteBatch, whiteRectangleSprite, position.x, position.y, body.getAngle() * MathUtils.radiansToDegrees);
			spriteBatch.end();

			float x = position.x + direction.tmp().mul(2f).x;
			float y = position.y + direction.tmp().mul(2f).y;

			ImmediateModeRendererUtils.drawLine(position.x, position.y, x, y, Color.GREEN);
		}

		@Override
		public void update(int delta) {
			world.step(Gdx.app.getGraphics().getDeltaTime(), 3, 3);

			Vector2 position = body.getTransform().getPosition();

			if (Gdx.input.isKeyPressed(Keys.LEFT))
				direction.rotate(180f * delta * 0.001f);
			else if (Gdx.input.isKeyPressed(Keys.RIGHT))
				direction.rotate(-180f * delta * 0.001f);

			body.setTransform(position, direction.angle() * MathUtils.degreesToRadians);
			body.applyForce(direction.tmp().mul(5000f), position);

			Vector2 linearVelocity = body.getLinearVelocity();
			float speed = linearVelocity.len();
			float maxSpeed = 5f;
			if (speed > maxSpeed) {
				linearVelocity.mul(maxSpeed / speed);
				body.setLinearVelocity(linearVelocity);
			}

			whiteRectangleSprite.setPosition(position.x, position.y);
		}

		@Override
		public void dispose() {
			spriteBatch.dispose();
		}

	}

	private SuperSheepGameState accelerometerTestGameState;

	@Override
	public void create() {
		Converters.register(Vector2.class, LibgdxConverters.vector2());
		Converters.register(Color.class, LibgdxConverters.color());

		accelerometerTestGameState = new SuperSheepGameState();

		setScreen(new ScreenImpl(accelerometerTestGameState));
	}

}
