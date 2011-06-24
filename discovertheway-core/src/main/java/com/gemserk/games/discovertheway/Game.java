package com.gemserk.games.discovertheway;

import java.util.ArrayList;

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
import com.gemserk.commons.gdx.games.Spatial;
import com.gemserk.commons.gdx.games.SpatialPhysicsImpl;
import com.gemserk.commons.gdx.graphics.ImmediateModeRendererUtils;
import com.gemserk.commons.gdx.graphics.SpriteBatchUtils;

public class Game extends com.gemserk.commons.gdx.Game {

	interface SpatialEntity {

		Spatial getSpatial();

	}

	class SuperSheepGameState extends GameStateImpl implements ContactListener {

		class CameraFollowEntity {

			Camera camera;
			SpatialEntity spatialEntity;

			public Camera getCamera() {
				return camera;
			}

			public CameraFollowEntity(Camera camera) {
				this.camera = camera;
			}

			public void update(int delta) {
				if (spatialEntity == null)
					return;
				Spatial spatial = spatialEntity.getSpatial();
				camera.setPosition(spatial.getX(), spatial.getY());
			}

			public void follow(SpatialEntity spatialEntity) {
				this.spatialEntity = spatialEntity;
			}

		}

		class SuperSheep implements SpatialEntity {

			Body body;
			Sprite sprite;
			Vector2 direction;
			boolean dead;

			Spatial spatial;

			public Spatial getSpatial() {
				return spatial;
			}

			public SuperSheep(float x, float y, Sprite sprite, Vector2 direction) {
				body = bodyBuilder.mass(50f) //
						.boxShape(0.25f, 0.25f) //
						.position(x, y) //
						.restitution(0f) //
						.type(BodyType.DynamicBody) //
						.build();
				this.sprite = sprite;
				this.direction = direction;
				this.dead = false;
				this.spatial = new SpatialPhysicsImpl(body, 0.5f, 0.5f);
			}

			public void update(int delta) {
				Vector2 position = body.getTransform().getPosition();
				body.setTransform(position, direction.angle() * MathUtils.degreesToRadians);
				body.applyForce(direction.tmp().mul(5000f), position);

				Vector2 linearVelocity = body.getLinearVelocity();
				float speed = linearVelocity.len();
				float maxSpeed = 6f;
				if (speed > maxSpeed) {
					linearVelocity.mul(maxSpeed / speed);
					body.setLinearVelocity(linearVelocity);
				}

				sprite.setPosition(position.x, position.y);

				for (int i = 0; i < miniPlanets.size(); i++) {
					MiniPlanet miniPlanet = miniPlanets.get(i);
					if (miniPlanet.getPosition().dst(position) < 3f && !miniPlanet.containsSuperSheep(this)) {
						miniPlanet.attachSuperSheep(this);
						break;
					}
				}
			}

			public void draw(SpriteBatch spriteBatch) {
				Vector2 position = body.getTransform().getPosition();
				SpriteBatchUtils.drawCentered(spriteBatch, sprite, position.x, position.y, body.getAngle() * MathUtils.radiansToDegrees);
			}

			public void drawDebug() {
				Vector2 position = body.getTransform().getPosition();
				float x = position.x + direction.tmp().mul(1f).x;
				float y = position.y + direction.tmp().mul(1f).y;
				ImmediateModeRendererUtils.drawLine(position.x, position.y, x, y, Color.GREEN);
			}

		}

		class MiniPlanet implements SpatialEntity {

			float radius;
			Body body;
			ArrayList<SuperSheep> superSheeps;
			Joint joint;

			Spatial spatial;

			public Spatial getSpatial() {
				return spatial;
			}

			public Vector2 getPosition() {
				return body.getTransform().getPosition();
			}

			public MiniPlanet(float x, float y, float radius) {
				this.body = bodyBuilder.mass(1000f) //
						.circleShape(radius) //
						.position(x, y) //
						.restitution(0f) //
						.type(BodyType.StaticBody) //
						.build();
				this.radius = radius;
				this.superSheeps = new ArrayList<SuperSheep>();
				this.joint = null;
				this.spatial = new SpatialPhysicsImpl(body, radius * 2, radius * 2);
			}

			public void update(int delta) {
				processInput();

				if (this.superSheeps.isEmpty())
					return;

				for (int i = 0; i < superSheeps.size(); i++) {
					SuperSheep superSheep = superSheeps.get(i);
					Vector2 superSheepPosition = superSheep.body.getTransform().getPosition();
					Vector2 position = body.getTransform().getPosition();

					Vector2 diff = superSheepPosition.sub(position).nor();
					diff.rotate(-90f);

					superSheep.direction.set(diff);
				}

			}

			private void processInput() {
				if (this.superSheeps.isEmpty())
					return;

				if (Gdx.app.getType() == ApplicationType.Android) {
					if (!Gdx.input.isTouched())
						return;
				} else if (!Gdx.input.isKeyPressed(Keys.SPACE))
					return;

				SuperSheep superSheep = this.superSheeps.remove(0);
				cameraFollowEntity.follow(superSheep);

				world.destroyJoint(joint);
				joint = null;
			}

			public void drawDebug() {
				Vector2 position = body.getTransform().getPosition();
				ImmediateModeRendererUtils.drawSolidCircle(position, radius, Color.BLUE);
			}

			public void attachSuperSheep(SuperSheep superSheep) {
				this.superSheeps.add(superSheep);
				DistanceJointDef jointDef = new DistanceJointDef();
				jointDef.bodyA = superSheep.body;
				jointDef.bodyB = this.body;
				jointDef.collideConnected = false;
				jointDef.length = 3f;
				joint = world.createJoint(jointDef);

				cameraFollowEntity.follow(this);
			}

			public boolean containsSuperSheep(SuperSheep superSheep) {
				return this.superSheeps.contains(superSheep);
			}

		}

		private SpriteBatch spriteBatch;
		private Libgdx2dCamera camera;
		private World world;
		private Box2DCustomDebugRenderer box2dCustomDebugRenderer;
		private BodyBuilder bodyBuilder;

		private ArrayList<MiniPlanet> miniPlanets;
		private MiniPlanet startMiniPlanet;

		private ArrayList<SuperSheep> superSheeps;
		private SuperSheep superSheep;
		private CameraFollowEntity cameraFollowEntity;

		@Override
		public void init() {
			spriteBatch = new SpriteBatch();
			camera = new Libgdx2dCameraTransformImpl();
			miniPlanets = new ArrayList<MiniPlanet>();
			superSheeps = new ArrayList<SuperSheep>();
			world = new World(new Vector2(), false);
			world.setContactListener(this);

			camera.center(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);
			// cameraData = new CameraImpl(0f, 0f, 32f, 0f);
			Camera cameraData = new CameraRestrictedImpl(0f, 0f, 42f, 0f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), new Rectangle(-2.5f, 0f, 73f, 15f));

			// camera.zoom(32f);

			box2dCustomDebugRenderer = new Box2DCustomDebugRenderer((Libgdx2dCameraTransformImpl) camera, world);

			Texture whiteRectangle = new Texture(Gdx.files.internal("data/images/white-rectangle.png"));
			Sprite sprite = new Sprite(whiteRectangle);
			sprite.setSize(0.5f, 0.5f);

			bodyBuilder = new BodyBuilder(world);

			float lastX = 0f;

			for (int i = 0; i < 5; i++) {

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

				lastX = 17f + i * 8f;
			}

			cameraFollowEntity = new CameraFollowEntity(cameraData);

			superSheep = new SuperSheep(5f, 2f, sprite, new Vector2(1f, 0f));

			startMiniPlanet = new MiniPlanet(2.5f, 7.5f, 1.5f);
			startMiniPlanet.attachSuperSheep(superSheep);
			miniPlanets.add(startMiniPlanet);

			MiniPlanet miniPlanet = new MiniPlanet(lastX + 9f, 7.5f, 1.5f);
			miniPlanets.add(miniPlanet);

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
			Camera cameraData = cameraFollowEntity.getCamera();

			camera.move(cameraData.getX(), cameraData.getY());
			camera.zoom(cameraData.getZoom());
			camera.rotate(cameraData.getAngle());

			Gdx.graphics.getGL10().glClear(GL10.GL_COLOR_BUFFER_BIT);
			camera.apply(spriteBatch);
			spriteBatch.begin();
			superSheep.draw(spriteBatch);
			spriteBatch.end();

			box2dCustomDebugRenderer.render();

			superSheep.drawDebug();

			for (int i = 0; i < miniPlanets.size(); i++)
				miniPlanets.get(i).drawDebug();
		}

		@Override
		public void update(int delta) {
			world.step(Gdx.app.getGraphics().getDeltaTime(), 3, 3);

			calculateDirectionFromInput(delta, superSheep.direction);
			// inputReleaseSheep(delta);

			superSheep.update(delta);
			for (int i = 0; i < miniPlanets.size(); i++)
				miniPlanets.get(i).update(delta);

			cameraFollowEntity.update(delta);

			if (!superSheep.dead)
				return;

			superSheep.body.setType(BodyType.StaticBody);
			superSheep = new SuperSheep(5f, 2f, new Sprite(superSheep.sprite), new Vector2(1f, 0f));
			startMiniPlanet.attachSuperSheep(superSheep);

			// cameraFollowEntity.follow(startMiniPlanet);
		}

		private void calculateDirectionFromInput(int delta, Vector2 direction) {
			processInputSuperSheepAndroid(delta, direction);
			processInputSuperSheepPC(delta, direction);
		}

		private void processInputSuperSheepPC(int delta, Vector2 direction) {
			if (Gdx.app.getType() == ApplicationType.Android)
				return;
			if (Gdx.input.isKeyPressed(Keys.LEFT))
				direction.rotate(360f * delta * 0.001f);
			else if (Gdx.input.isKeyPressed(Keys.RIGHT))
				direction.rotate(-360f * delta * 0.001f);
		}

		private void processInputSuperSheepAndroid(int delta, Vector2 direction) {
			if (Gdx.app.getType() != ApplicationType.Android)
				return;
			for (int i = 0; i < 5; i++) {
				if (!Gdx.input.isTouched(i))
					continue;
				float x = Gdx.input.getX(i);
				if (x < Gdx.graphics.getWidth() / 2)
					direction.rotate(360f * delta * 0.001f);
				else
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
