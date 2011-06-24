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
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.DistanceJointDef;
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

	class SuperSheepGameState extends GameStateImpl implements ContactListener {

		class SuperSheep {

			Body body;
			Sprite sprite;
			Vector2 direction;
			Camera camera;
			boolean dead;

			public SuperSheep(Body body, Sprite sprite, Vector2 direction, Camera camera) {
				this.body = body;
				this.sprite = sprite;
				this.direction = direction;
				this.camera = camera;
				this.dead = false;
			}

			public void update(int delta) {
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

				// updates camera
				if (camera == null)
					return;
				
				camera.setPosition(position.x, position.y);
			}

			public void draw(SpriteBatch spriteBatch) {
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

		class MiniPlanet {

			float radius;
			Body body;
			SuperSheep superSheep;

			public MiniPlanet(Body body, float radius, SuperSheep superSheep) {
				this.body = body;
				this.radius = radius;
				this.superSheep = superSheep;
			}

			public void update(int delta) {
				if (superSheep == null)
					return;

				Vector2 superSheepPosition = superSheep.body.getTransform().getPosition();
				Vector2 position = body.getTransform().getPosition();

				Vector2 diff = superSheepPosition.sub(position).nor();
				diff.rotate(-90f);

				superSheep.direction.set(diff);
			}

			public void drawDebug() {
				Vector2 position = body.getTransform().getPosition();
				ImmediateModeRendererUtils.drawSolidCircle(position, radius, Color.BLUE);
			}

		}

		private SpriteBatch spriteBatch;
		private Libgdx2dCamera camera;
		private World world;
		private Box2DCustomDebugRenderer box2dCustomDebugRenderer;
		private SuperSheep superSheep;
		private MiniPlanet miniPlanet;
		private Joint joint;
		private BodyBuilder bodyBuilder;

		@Override
		public void init() {
			spriteBatch = new SpriteBatch();
			camera = new Libgdx2dCameraTransformImpl();
			world = new World(new Vector2(), false);
			world.setContactListener(this);

			camera.center(Gdx.graphics.getWidth() / 4, Gdx.graphics.getHeight() / 2);
			// cameraData = new CameraImpl(0f, 0f, 32f, 0f);
			Camera cameraData = new CameraRestrictedImpl(0f, 0f, 32f, 0f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), new Rectangle(-5f, 0f, 400f, 15f));

			// camera.zoom(32f);

			box2dCustomDebugRenderer = new Box2DCustomDebugRenderer((Libgdx2dCameraTransformImpl) camera, world);

			Texture whiteRectangle = new Texture(Gdx.files.internal("data/images/white-rectangle.png"));
			Sprite sprite = new Sprite(whiteRectangle);
			sprite.setSize(0.5f, 0.5f);

			bodyBuilder = new BodyBuilder(world);
			Body body = bodyBuilder.mass(50f) //
					.boxShape(0.25f, 0.25f) //
					.position(5f, 2f) //
					.restitution(0f) //
					.type(BodyType.DynamicBody) //
					.build();

			for (int i = 0; i < 50; i++) {

				float randomY = MathUtils.random(-5f, 5f);

				bodyBuilder.mass(1000f) //
						.polygonShape(new Vector2[] { new Vector2(3f, 1.5f), new Vector2(1f, 4f), new Vector2(-2.5f, 1f), new Vector2(-1.5f, -2.5f), new Vector2(1f, -1.5f), }) //
						.position(17f + i * 8f, 5f + randomY) //
						.restitution(0f) //
						.type(BodyType.StaticBody) //
						.build();

				bodyBuilder.mass(1000f) //
						.polygonShape(new Vector2[] { new Vector2(3f, 1.5f), new Vector2(1f, 4f), new Vector2(-2.5f, 1f), new Vector2(-1.5f, -2.5f), new Vector2(1f, -1.5f), }) //
						.restitution(0f) //
						.type(BodyType.StaticBody) //
						.position(14f + i * 8f, 11f + randomY) //
						.angle(90f)//
						.build();

			}

			Body miniPlanetBody = bodyBuilder.mass(1000f) //
					.circleShape(1.5f) //
					.position(5f, 5f) //
					.restitution(0f) //
					.type(BodyType.StaticBody) //
					.build();

			DistanceJointDef jointDef = new DistanceJointDef();
			jointDef.bodyA = body;
			jointDef.bodyB = miniPlanetBody;
			jointDef.collideConnected = false;
			jointDef.length = 3f;
			joint = world.createJoint(jointDef);

			superSheep = new SuperSheep(body, sprite, new Vector2(1f, 0f), cameraData);
			miniPlanet = new MiniPlanet(miniPlanetBody, 1.5f, superSheep);

		}

		@Override
		public void beginContact(Contact contact) {
			checkContactSuperSheep(contact.getFixtureA());
			checkContactSuperSheep(contact.getFixtureB());
		}

		private void checkContactSuperSheep(Fixture fixture) {
			if (fixture.getBody() != superSheep.body)
				return;
			Gdx.app.log("SuperSheep", "die!");
			superSheep.dead = true;
		}

		@Override
		public void endContact(Contact contact) {

		}

		@Override
		public void render(int delta) {
			Camera cameraData = superSheep.camera;

			camera.move(cameraData.getX(), cameraData.getY());
			camera.zoom(cameraData.getZoom());
			camera.rotate(cameraData.getAngle());

			Gdx.graphics.getGL10().glClear(GL10.GL_COLOR_BUFFER_BIT);
			camera.apply(spriteBatch);
			spriteBatch.begin();
			superSheep.draw(spriteBatch);
			spriteBatch.end();

			superSheep.drawDebug();
			miniPlanet.drawDebug();

			box2dCustomDebugRenderer.render();
		}

		@Override
		public void update(int delta) {
			world.step(Gdx.app.getGraphics().getDeltaTime(), 3, 3);

			calculateDirectionFromInput(delta, superSheep.direction);
			inputReleaseSheep(delta);

			superSheep.update(delta);
			miniPlanet.update(delta);

			//

			if (!superSheep.dead)
				return;
			
			Body body = bodyBuilder.mass(50f) //
					.boxShape(0.25f, 0.25f) //
					.position(5f, 2f) //
					.restitution(0f) //
					.type(BodyType.DynamicBody) //
					.build();
			
			Camera camera = superSheep.camera;
			superSheep.camera = null;
			superSheep.body.setType(BodyType.StaticBody);
			
			superSheep = new SuperSheep(body, new Sprite(superSheep.sprite), new Vector2(1f, 0f), camera);
			miniPlanet.superSheep = superSheep;
			
			DistanceJointDef jointDef = new DistanceJointDef();
			jointDef.bodyA = superSheep.body;
			jointDef.bodyB = miniPlanet.body;
			jointDef.collideConnected = false;
			jointDef.length = 3f;
			joint = world.createJoint(jointDef);
		}

		private void inputReleaseSheep(int delta) {
			if (miniPlanet.superSheep == null)
				return;

			if (Gdx.app.getType() == ApplicationType.Android) {
				if (!Gdx.input.isTouched())
					return;
			} else {
				if (!Gdx.input.isKeyPressed(Keys.SPACE))
					return;
			}

			miniPlanet.superSheep = null;
			world.destroyJoint(joint);
		}

		private void calculateDirectionFromInput(int delta, Vector2 direction) {
			if (Gdx.app.getType() == ApplicationType.Android) {

				for (int i = 0; i < 5; i++) {

					if (!Gdx.input.isTouched(i))
						continue;

					float x = Gdx.input.getX(i);
					if (x < Gdx.graphics.getWidth() / 2)
						direction.rotate(360f * delta * 0.001f);
					else
						direction.rotate(-360f * delta * 0.001f);
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
