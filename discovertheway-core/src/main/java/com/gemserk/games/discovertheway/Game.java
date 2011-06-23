package com.gemserk.games.discovertheway;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.World;
import com.gemserk.animation4j.converters.Converters;
import com.gemserk.animation4j.gdx.converters.LibgdxConverters;
import com.gemserk.commons.gdx.GameStateImpl;
import com.gemserk.commons.gdx.ScreenImpl;
import com.gemserk.commons.gdx.box2d.BodyBuilder;
import com.gemserk.commons.gdx.box2d.Box2DCustomDebugRenderer;
import com.gemserk.commons.gdx.camera.Camera;
import com.gemserk.commons.gdx.camera.CameraRestrictedImpl;
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
		private Box2DCustomDebugRenderer box2dCustomDebugRenderer;
		private Camera cameraData;

		@Override
		public void init() {
			spriteBatch = new SpriteBatch();
			camera = new Libgdx2dCameraTransformImpl();
			world = new World(new Vector2(), false);

			camera.center(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 4);
			// cameraData = new CameraImpl(0f, 0f, 32f, 0f);
			cameraData = new CameraRestrictedImpl(0f, 0f, 32f, 0f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), new Rectangle(0f, -10f, 30f, 400f));

			// camera.zoom(32f);

			box2dCustomDebugRenderer = new Box2DCustomDebugRenderer((Libgdx2dCameraTransformImpl) camera, world);

			Texture whiteRectangle = new Texture(Gdx.files.internal("data/images/white-rectangle.png"));
			whiteRectangleSprite = new Sprite(whiteRectangle);
			whiteRectangleSprite.setSize(0.5f, 0.5f);

			BodyBuilder bodyBuilder = new BodyBuilder(world);
			body = bodyBuilder.mass(50f) //
					.boxShape(0.25f, 0.25f) //
					.position(15f, 0f) //
					.restitution(0f) //
					.type(BodyType.DynamicBody) //
					.build();

			for (int i = 0; i < 50; i++) {

				float randomY = MathUtils.random(-5f, 15f);
				
				bodyBuilder.mass(1000f) //
						.polygonShape(new Vector2[] { new Vector2(3f, 1.5f), new Vector2(1f, 4f), new Vector2(-2.5f, 1f), new Vector2(-1.5f, -2.5f), new Vector2(1f, -1.5f), }) //
						.position(5f + randomY, 15f + i * 8f) //
						.restitution(0f) //
						.type(BodyType.StaticBody) //
						.build();

				bodyBuilder.mass(1000f) //
						.polygonShape(new Vector2[] { new Vector2(3f, 1.5f), new Vector2(1f, 4f), new Vector2(-2.5f, 1f), new Vector2(-1.5f, -2.5f), new Vector2(1f, -1.5f), }) //
						.restitution(0f) //
						.type(BodyType.StaticBody) //
						.position(11f + randomY, 12f + i * 8f) //
						.angle(90f)//
						.build();

			}

			direction = new Vector2(0f, 1f);
		}

		@Override
		public void render(int delta) {
			Vector2 position = body.getTransform().getPosition();

			camera.move(cameraData.getX(), cameraData.getY());
			camera.zoom(cameraData.getZoom());
			camera.rotate(cameraData.getAngle());

			Gdx.graphics.getGL10().glClear(GL10.GL_COLOR_BUFFER_BIT);
			camera.apply(spriteBatch);
			spriteBatch.begin();
			SpriteBatchUtils.drawCentered(spriteBatch, whiteRectangleSprite, position.x, position.y, body.getAngle() * MathUtils.radiansToDegrees);
			spriteBatch.end();

			float x = position.x + direction.tmp().mul(1f).x;
			float y = position.y + direction.tmp().mul(1f).y;

			ImmediateModeRendererUtils.drawLine(position.x, position.y, x, y, Color.GREEN);

			box2dCustomDebugRenderer.render();
		}

		@Override
		public void update(int delta) {
			world.step(Gdx.app.getGraphics().getDeltaTime(), 3, 3);

			Vector2 position = body.getTransform().getPosition();

			if (Gdx.app.getType() == ApplicationType.Android) {

				if (Gdx.input.isTouched()) {

					float x = Gdx.input.getX();

					if (x < Gdx.graphics.getWidth() / 2) {
						direction.rotate(360f * delta * 0.001f);
					} else {
						direction.rotate(-360f * delta * 0.001f);
					}

				}

			} else {
				if (Gdx.input.isKeyPressed(Keys.LEFT))
					direction.rotate(360f * delta * 0.001f);
				else if (Gdx.input.isKeyPressed(Keys.RIGHT))
					direction.rotate(-360f * delta * 0.001f);
			}

			body.setTransform(position, direction.angle() * MathUtils.degreesToRadians);
			body.applyForce(direction.tmp().mul(5000f), position);

			Vector2 linearVelocity = body.getLinearVelocity();
			float speed = linearVelocity.len();
			float maxSpeed = 7f;
			if (speed > maxSpeed) {
				linearVelocity.mul(maxSpeed / speed);
				body.setLinearVelocity(linearVelocity);
			}

			whiteRectangleSprite.setPosition(position.x, position.y);
			cameraData.setPosition(position.x, position.y);
		}

		@Override
		public void dispose() {
			whiteRectangleSprite.getTexture().dispose();
			spriteBatch.dispose();
			world.dispose();
		}

	}

	@Override
	public void create() {
		Converters.register(Vector2.class, LibgdxConverters.vector2());
		Converters.register(Color.class, LibgdxConverters.color());
		setScreen(new ScreenImpl(new SuperSheepGameState()));
	}

	@Override
	public void render() {
		super.render();
		if (Gdx.input.isKeyPressed(Keys.R)) {
			getScreen().dispose();
			setScreen(new ScreenImpl(new SuperSheepGameState()));
		}
	}

}
