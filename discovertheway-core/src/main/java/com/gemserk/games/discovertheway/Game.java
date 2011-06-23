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
		private World world;
		private Box2DCustomDebugRenderer box2dCustomDebugRenderer;
		private Camera cameraData;
		private SuperSheep superSheep;

		@Override
		public void init() {
			spriteBatch = new SpriteBatch();
			camera = new Libgdx2dCameraTransformImpl();
			world = new World(new Vector2(), false);

			camera.center(Gdx.graphics.getWidth() / 4, Gdx.graphics.getHeight() / 2);
			// cameraData = new CameraImpl(0f, 0f, 32f, 0f);
			cameraData = new CameraRestrictedImpl(0f, 0f, 32f, 0f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), new Rectangle(-5f, 0f, 400f, 15f));

			// camera.zoom(32f);

			box2dCustomDebugRenderer = new Box2DCustomDebugRenderer((Libgdx2dCameraTransformImpl) camera, world);

			Texture whiteRectangle = new Texture(Gdx.files.internal("data/images/white-rectangle.png"));
			Sprite sprite = new Sprite(whiteRectangle);
			sprite.setSize(0.5f, 0.5f);

			BodyBuilder bodyBuilder = new BodyBuilder(world);
			Body body = bodyBuilder.mass(50f) //
					.boxShape(0.25f, 0.25f) //
					.position(0f, 5f) //
					.restitution(0f) //
					.type(BodyType.DynamicBody) //
					.build();

			for (int i = 0; i < 50; i++) {

				float randomY = MathUtils.random(-5f, 5f);
				
				bodyBuilder.mass(1000f) //
						.polygonShape(new Vector2[] { new Vector2(3f, 1.5f), new Vector2(1f, 4f), new Vector2(-2.5f, 1f), new Vector2(-1.5f, -2.5f), new Vector2(1f, -1.5f), }) //
						.position(15f + i * 8f, 5f + randomY) //
						.restitution(0f) //
						.type(BodyType.StaticBody) //
						.build();

				bodyBuilder.mass(1000f) //
						.polygonShape(new Vector2[] { new Vector2(3f, 1.5f), new Vector2(1f, 4f), new Vector2(-2.5f, 1f), new Vector2(-1.5f, -2.5f), new Vector2(1f, -1.5f), }) //
						.restitution(0f) //
						.type(BodyType.StaticBody) //
						.position(12f + i * 8f, 11f + randomY) //
						.angle(90f)//
						.build();

			}

			superSheep = new SuperSheep(body, sprite, new Vector2(1f, 0f));
		}

		@Override
		public void render(int delta) {
			camera.move(cameraData.getX(), cameraData.getY());
			camera.zoom(cameraData.getZoom());
			camera.rotate(cameraData.getAngle());

			Gdx.graphics.getGL10().glClear(GL10.GL_COLOR_BUFFER_BIT);
			camera.apply(spriteBatch);
			spriteBatch.begin();
			superSheep.drawSuperSheep(spriteBatch);
			spriteBatch.end();

			superSheep.drawDebug();
			
			box2dCustomDebugRenderer.render();
		}

		@Override
		public void update(int delta) {
			world.step(Gdx.app.getGraphics().getDeltaTime(), 3, 3);

			calculateDirectionFromInput(delta, superSheep.direction);

			superSheep.updateSuperSheep();
			
			Vector2 position = superSheep.body.getTransform().getPosition();
			cameraData.setPosition(position.x, position.y);
		}
		
		class SuperSheep {
			
			Body body;
			
			Sprite sprite;
			
			Vector2 direction;
			
			public SuperSheep(Body body, Sprite sprite, Vector2 direction) {
				this.body = body;
				this.sprite = sprite;
				this.direction = direction;
			}
			
			public void updateSuperSheep() {
				Vector2 position = body.getTransform().getPosition();
				body.setTransform(position, direction.angle() * MathUtils.degreesToRadians);
				body.applyForce(direction.tmp().mul(5000f), position);

				Vector2 linearVelocity = body.getLinearVelocity();
				float speed = linearVelocity.len();
				float maxSpeed = 7f;
				if (speed > maxSpeed) {
					linearVelocity.mul(maxSpeed / speed);
					body.setLinearVelocity(linearVelocity);
				}
				
				sprite.setPosition(position.x, position.y);
			}
			
			public void drawSuperSheep(SpriteBatch spriteBatch) {
				Vector2 position = body.getTransform().getPosition();
				SpriteBatchUtils.drawCentered(spriteBatch, sprite, position.x, position.y, body.getAngle() * MathUtils.radiansToDegrees);
			}
			
			public void drawDebug() {
				Vector2 position = body.getTransform().getPosition();
				float x = position.x + superSheep.direction.tmp().mul(1f).x;
				float y = position.y + superSheep.direction.tmp().mul(1f).y;
				ImmediateModeRendererUtils.drawLine(position.x, position.y, x, y, Color.GREEN);
			}
			
		}
		
		private void calculateDirectionFromInput(int delta, Vector2 direction) {
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
		}

		@Override
		public void dispose() {
			superSheep.sprite.getTexture().dispose();
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
