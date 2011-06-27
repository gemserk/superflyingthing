package com.gemserk.games.superflyingthing;

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
import com.gemserk.animation4j.converters.Converters;
import com.gemserk.animation4j.gdx.converters.LibgdxConverters;
import com.gemserk.commons.gdx.GameStateImpl;
import com.gemserk.commons.gdx.ScreenImpl;
import com.gemserk.commons.gdx.box2d.BodyBuilder;
import com.gemserk.commons.gdx.box2d.Box2DCustomDebugRenderer;
import com.gemserk.commons.gdx.box2d.JointBuilder;
import com.gemserk.commons.gdx.camera.Camera;
import com.gemserk.commons.gdx.camera.CameraRestrictedImpl;
import com.gemserk.commons.gdx.camera.Libgdx2dCamera;
import com.gemserk.commons.gdx.camera.Libgdx2dCameraTransformImpl;
import com.gemserk.commons.gdx.games.Spatial;
import com.gemserk.commons.gdx.games.SpatialImpl;
import com.gemserk.commons.gdx.games.SpatialPhysicsImpl;
import com.gemserk.commons.gdx.graphics.ImmediateModeRendererUtils;
import com.gemserk.commons.gdx.graphics.SpriteBatchUtils;

public class Game extends com.gemserk.commons.gdx.Game {

	public static short AllCategoryBits = 0xFF;

	public static short ShipCategoryBits = 1;

	public static short MiniPlanetCategoryBits = 2;

	class Entity {

		/**
		 * Called before the first world update and after the Entity was added to the world.
		 */
		void init() {

		}

		/**
		 * Called in each world update iteration.
		 * 
		 * @param delta
		 *            The time since the last update call.
		 */
		void update(int delta) {

		}

		/**
		 * Called before the entity is removed from the world.
		 */
		void dispose() {

		}

	}

	interface Component {

	}

	// TODO: change it
	interface SpatialComponent {

		Spatial getSpatial();

	}

	class PhysicsComponent implements Component {

		Body body;

		public PhysicsComponent(Body body) {
			this.body = body;
		}

	}

	class SuperSheepGameState extends GameStateImpl implements ContactListener {

		class CameraFollowEntity extends Entity {

			Camera camera;
			SpatialComponent spatialComponent;

			public Camera getCamera() {
				return camera;
			}

			public CameraFollowEntity(Camera camera) {
				this.camera = camera;
			}

			public void update(int delta) {
				if (spatialComponent == null)
					return;
				Spatial spatial = spatialComponent.getSpatial();
				camera.setPosition(spatial.getX(), spatial.getY());
			}

			public void follow(SpatialComponent spatialComponent) {
				this.spatialComponent = spatialComponent;
			}

		}

		class SuperSheep extends Entity implements SpatialComponent {

			PhysicsComponent physicsComponent;
			
			Sprite sprite;
			Vector2 direction;
			boolean dead;

			Spatial spatial;

			public Spatial getSpatial() {
				return spatial;
			}
			
			public Body getBody() {
				return physicsComponent.body;
			}

			public SuperSheep(float x, float y, Sprite sprite, Vector2 direction) {
				float width = 0.4f;
				float height = 0.2f;
				physicsComponent = new PhysicsComponent(bodyBuilder.mass(50f) //
						.boxShape(width * 0.3f, height * 0.3f) //
						.position(x, y) //
						.restitution(0f) //
						.type(BodyType.DynamicBody) //
						.categoryBits(ShipCategoryBits).maskBits((short) (AllCategoryBits & ~MiniPlanetCategoryBits)).build());
				this.sprite = sprite;
				this.direction = direction;
				this.dead = false;
				this.spatial = new SpatialPhysicsImpl(bodyBuilder.mass(50f) //
						.boxShape(width * 0.3f, height * 0.3f) //
						.position(x, y) //
						.restitution(0f) //
						.type(BodyType.DynamicBody) //
						.categoryBits(ShipCategoryBits).maskBits((short) (AllCategoryBits & ~MiniPlanetCategoryBits)).build(), width, height);
			}

			public void update(int delta) {
				direction.nor();

				Body body = physicsComponent.body;
				
				Vector2 position = body.getTransform().getPosition();
				float desiredAngle = direction.angle();

				body.setTransform(position, desiredAngle * MathUtils.degreesToRadians);
				body.applyForce(direction.tmp().mul(5000f), position);

				Vector2 linearVelocity = body.getLinearVelocity();

				float speed = linearVelocity.len();

				linearVelocity.set(direction.tmp().mul(speed));

				float maxSpeed = 6f;
				if (speed > maxSpeed) {
					linearVelocity.mul(maxSpeed / speed);
					body.setLinearVelocity(linearVelocity);
				}

				sprite.setPosition(position.x, position.y);
				sprite.setSize(spatial.getWidth(), spatial.getHeight());
			}

			public void draw(SpriteBatch spriteBatch) {
				Vector2 position = getSpatial().getPosition();
				SpriteBatchUtils.drawCentered(spriteBatch, sprite, position.x, position.y, getSpatial().getAngle());
			}

			public void drawDebug() {
				Vector2 position = getSpatial().getPosition();
				float x = position.x + direction.tmp().mul(0.5f).x;
				float y = position.y + direction.tmp().mul(0.5f).y;
				ImmediateModeRendererUtils.drawLine(position.x, position.y, x, y, Color.GREEN);
			}

			public void dispose() {
				world.destroyBody(physicsComponent.body);
			}

		}

		class DeadSuperSheepEntity extends Entity implements SpatialComponent {

			Sprite sprite;
			Spatial spatial;

			public Spatial getSpatial() {
				return spatial;
			}

			public DeadSuperSheepEntity(Spatial spatial, Sprite sprite) {
				this.spatial = new SpatialImpl(spatial);
				this.sprite = new Sprite(sprite);
			}

			public void update(int delta) {

			}

			public void draw(SpriteBatch spriteBatch) {
				sprite.setColor(0.7f, 0.7f, 0.7f, 1f);
				Vector2 position = spatial.getPosition();
				SpriteBatchUtils.drawCentered(spriteBatch, sprite, position.x, position.y, spatial.getAngle());
			}

			public void drawDebug() {

			}

		}

		class MiniPlanet extends Entity implements SpatialComponent {

			float radius;
			Body body;
			ArrayList<SuperSheep> superSheeps;
			Joint joint;

			Spatial spatial;

			int releaseTime = 0;

			public Spatial getSpatial() {
				return spatial;
			}

			public MiniPlanet(float x, float y, float radius) {
				this.body = bodyBuilder.mass(1000f) //
						.circleShape(radius * 0.1f) //
						.position(x, y) //
						.restitution(0f) //
						.type(BodyType.StaticBody) //
						.categoryBits(MiniPlanetCategoryBits).build();
				this.radius = radius;
				this.superSheeps = new ArrayList<SuperSheep>();
				this.joint = null;
				this.spatial = new SpatialPhysicsImpl(body, radius * 2, radius * 2);
			}

			public void update(int delta) {
				processInput(delta);

				if (this.superSheeps.isEmpty())
					return;

				if (releaseTime > 0)
					releaseTime -= delta;

				for (int i = 0; i < superSheeps.size(); i++) {
					SuperSheep superSheep = superSheeps.get(i);
					Vector2 superSheepPosition = superSheep.getSpatial().getPosition();
					Vector2 position = body.getTransform().getPosition();

					Vector2 diff = superSheepPosition.sub(position).nor();
					diff.rotate(-90f);

					superSheep.direction.set(diff);
				}

			}

			protected void processInput(int delta) {
				if (this.superSheeps.isEmpty())
					return;

				if (Gdx.app.getType() == ApplicationType.Android) {
					if (!Gdx.input.isTouched()) {
						return;
					}
				} else if (!Gdx.input.isKeyPressed(Keys.SPACE))
					return;

				if (releaseTime > 0)
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

				joint = jointBuilder.distanceJoint() //
						.bodyA(superSheep.getBody()) //
						.bodyB(this.body) //
						.collideConnected(false) //
						.length(1.5f) //
						.build();

				cameraFollowEntity.follow(this);
				releaseTime = 500;
			}

			public boolean containsSuperSheep(SuperSheep superSheep) {
				return this.superSheeps.contains(superSheep);
			}

		}

		class DestinationPlanet extends MiniPlanet {

			public DestinationPlanet(float x, float y, float radius) {
				super(x, y, radius);
			}

			@Override
			public void update(int delta) {
				for (int i = 0; i < SuperSheepGameState.this.superSheeps.size(); i++) {
					SuperSheep superSheep = SuperSheepGameState.this.superSheeps.get(i);
					if (getSpatial().getPosition().dst(superSheep.getSpatial().getPosition()) < 1.2f && !containsSuperSheep(superSheep)) {
						attachSuperSheep(superSheep);
						break;
					}
				}
				super.update(delta);
			}

			@Override
			protected void processInput(int delta) {

			}

		}

		private SpriteBatch spriteBatch;
		private Libgdx2dCamera camera;
		private World world;
		private Box2DCustomDebugRenderer box2dCustomDebugRenderer;
		private BodyBuilder bodyBuilder;
		private JointBuilder jointBuilder;

		private ArrayList<MiniPlanet> miniPlanets;
		private MiniPlanet startMiniPlanet;

		private ArrayList<SuperSheep> superSheeps;
		private CameraFollowEntity cameraFollowEntity;

		private ArrayList<DeadSuperSheepEntity> deadSuperSheeps;
		private ArrayList<SuperSheep> superSheepsToRemove;

		@Override
		public void init() {
			spriteBatch = new SpriteBatch();
			camera = new Libgdx2dCameraTransformImpl();
			miniPlanets = new ArrayList<MiniPlanet>();
			superSheeps = new ArrayList<SuperSheep>();
			superSheepsToRemove = new ArrayList<SuperSheep>();
			deadSuperSheeps = new ArrayList<DeadSuperSheepEntity>();
			world = new World(new Vector2(), false);
			world.setContactListener(this);

			camera.center(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);
			// cameraData = new CameraImpl(0f, 0f, 32f, 0f);
			Camera cameraData = new CameraRestrictedImpl(0f, 0f, 42f, 0f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), new Rectangle(0f, 0f, 100f, 15f));

			// camera.zoom(32f);

			box2dCustomDebugRenderer = new Box2DCustomDebugRenderer((Libgdx2dCameraTransformImpl) camera, world);

			Texture whiteRectangle = new Texture(Gdx.files.internal("data/images/white-rectangle.png"));
			Sprite sprite = new Sprite(whiteRectangle);
			sprite.setSize(0.5f, 0.5f);

			bodyBuilder = new BodyBuilder(world);
			jointBuilder = new JointBuilder(world);

			for (int i = 0; i < 10; i++) {
				float randomY = MathUtils.random(0f, 15f);

				bodyBuilder.mass(1000f) //
						.polygonShape(new Vector2[] { new Vector2(3f, 1.5f), new Vector2(1f, 4f), new Vector2(-2.5f, 1f), new Vector2(-1.5f, -2.5f), new Vector2(1f, -1.5f), }) //
						.position(17f + i * 8f, randomY) //
						.restitution(0f) //
						.type(BodyType.StaticBody) //
						.build();

				randomY = MathUtils.random(0f, 15f);

				bodyBuilder.mass(1000f) //
						.polygonShape(new Vector2[] { new Vector2(3f, 1.5f), new Vector2(1f, 4f), new Vector2(-2.5f, 1f), new Vector2(-1.5f, -2.5f), new Vector2(1f, -1.5f), }) //
						.restitution(0f) //
						.type(BodyType.StaticBody) //
						.position(12f + i * 8f, randomY) //
						.angle(90f)//
						.build();
			}

			cameraFollowEntity = new CameraFollowEntity(cameraData);

			SuperSheep superSheep = new SuperSheep(5f, 7.5f, sprite, new Vector2(1f, 0f));
			superSheeps.add(superSheep);

			startMiniPlanet = new MiniPlanet(5f, 7.5f, 1f);
			startMiniPlanet.attachSuperSheep(superSheep);
			miniPlanets.add(startMiniPlanet);

			MiniPlanet miniPlanet = new DestinationPlanet(95f, 7.5f, 1f);
			miniPlanets.add(miniPlanet);

			float worldWidth = 100f;
			float worldHeight = 20f;

			float x = worldWidth * 0.5f;
			float y = worldHeight * 0.5f;

			bodyBuilder.type(BodyType.StaticBody) //
					.boxShape(worldWidth * 0.5f, 0.1f * 0.5f) //
					.restitution(1f) //
					.mass(1f)//
					.friction(0f) //
					.position(x, 0f) //
					.build();

			bodyBuilder.type(BodyType.StaticBody) //
					.boxShape(worldWidth * 0.5f, 0.1f * 0.5f) //
					.restitution(1f) //
					.mass(1f)//
					.friction(0f) //
					.position(x, 15f) //
					.build();

			bodyBuilder.type(BodyType.StaticBody) //
					.boxShape(0.1f * 0.5f, worldHeight * 0.5f) //
					.restitution(1f) //
					.mass(1f)//
					.friction(0f) //
					.position(0f, y) //
					.build();

			bodyBuilder.type(BodyType.StaticBody) //
					.boxShape(0.1f * 0.5f, worldHeight * 0.5f) //
					.restitution(1f) //
					.mass(1f)//
					.friction(0f) //
					.position(100f, y) //
					.build();
		}

		@Override
		public void beginContact(Contact contact) {
			checkContactSuperSheep(contact.getFixtureA());
			checkContactSuperSheep(contact.getFixtureB());
		}

		private void checkContactSuperSheep(Fixture fixture) {
			for (int i = 0; i < superSheeps.size(); i++) {
				SuperSheep superSheep = superSheeps.get(i);
				if (fixture.getBody() != superSheep.getBody())
					return;
				Gdx.app.log("SuperSheep", "die!");
				superSheep.dead = true;
			}
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

			for (int i = 0; i < superSheeps.size(); i++)
				superSheeps.get(i).draw(spriteBatch);

			for (int i = 0; i < deadSuperSheeps.size(); i++)
				deadSuperSheeps.get(i).draw(spriteBatch);

			spriteBatch.end();

			box2dCustomDebugRenderer.render();

			for (int i = 0; i < superSheeps.size(); i++) {
				SuperSheep superSheep = superSheeps.get(i);
				superSheep.drawDebug();
			}

			for (int i = 0; i < miniPlanets.size(); i++)
				miniPlanets.get(i).drawDebug();
		}

		@Override
		public void update(int delta) {
			world.step(Gdx.app.getGraphics().getDeltaTime(), 3, 3);

			// inputReleaseSheep(delta);

			for (int i = 0; i < superSheeps.size(); i++) {
				SuperSheep superSheep = superSheeps.get(i);
				calculateDirectionFromInput(delta, superSheep.direction);
				superSheep.update(delta);
			}

			for (int i = 0; i < deadSuperSheeps.size(); i++)
				deadSuperSheeps.get(i).update(delta);

			for (int i = 0; i < miniPlanets.size(); i++)
				miniPlanets.get(i).update(delta);

			cameraFollowEntity.update(delta);

			for (int i = 0; i < superSheeps.size(); i++) {
				SuperSheep superSheep = superSheeps.get(i);

				if (!superSheep.dead)
					continue;

				DeadSuperSheepEntity deadSuperSheepEntity = new DeadSuperSheepEntity(superSheep.getSpatial(), superSheep.sprite);
				deadSuperSheeps.add(deadSuperSheepEntity);

				superSheepsToRemove.add(superSheep);

				// superSheep.body.setType(BodyType.StaticBody);
				//
				// // create a new body?
				// Filter filterData = superSheep.body.getFixtureList().get(0).getFilterData();
				// filterData.categoryBits = DeadShipCategoryBits;
				// superSheep.body.getFixtureList().get(0).setFilterData(filterData);

				// world.destroyBody(superSheep.body);
				superSheep.dispose();

				SuperSheep newSuperSheep = new SuperSheep(5f, 2f, new Sprite(superSheep.sprite), new Vector2(1f, 0f));
				startMiniPlanet.attachSuperSheep(newSuperSheep);
				superSheeps.add(newSuperSheep);

			}
			superSheeps.removeAll(superSheepsToRemove);
			superSheepsToRemove.clear();

			// cameraFollowEntity.follow(startMiniPlanet);
		}

		private void calculateDirectionFromInput(int delta, Vector2 direction) {
			processInputSuperSheep(delta, direction);
		}

		float angularVelocity = 100f;

		private void processInputSuperSheep(int delta, Vector2 direction) {

			float rotationAngle = 0f;
			float maxAngularVelocity = 600f;
			float movementDirection = getMovementDirection();
			float acceleration = 1f;

			float minimumAngularVelocity = 100f;

			if (movementDirection > 0) {
				if (angularVelocity < 0)
					angularVelocity = minimumAngularVelocity;
				angularVelocity += acceleration * delta;
				if (angularVelocity > maxAngularVelocity)
					angularVelocity = maxAngularVelocity;
				rotationAngle = angularVelocity * delta * 0.001f;
			} else if (movementDirection < 0) {
				if (angularVelocity > 0)
					angularVelocity = -minimumAngularVelocity;
				angularVelocity -= acceleration * delta;
				if (angularVelocity < -maxAngularVelocity)
					angularVelocity = -maxAngularVelocity;
				rotationAngle = angularVelocity * delta * 0.001f;
			} else {
				if (angularVelocity > 0)
					angularVelocity = minimumAngularVelocity;
				if (angularVelocity < 0)
					angularVelocity = -minimumAngularVelocity;
			}

			direction.rotate(rotationAngle);
		}

		private float getMovementDirection() {
			if (Gdx.app.getType() == ApplicationType.Android)
				return getMovementDirectionAndroid();
			else
				return getMovementDirectionPC();
		}

		private float getMovementDirectionPC() {
			float movementDirection = 0f;

			if (Gdx.input.isKeyPressed(Keys.LEFT))
				movementDirection += 1f;

			if (Gdx.input.isKeyPressed(Keys.RIGHT))
				movementDirection -= 1f;

			return movementDirection;
		}

		private float getMovementDirectionAndroid() {
			float movementDirection = 0f;

			for (int i = 0; i < 5; i++) {
				if (!Gdx.input.isTouched(i))
					continue;
				float x = Gdx.input.getX(i);
				if (x < Gdx.graphics.getWidth() / 2)
					movementDirection += 1f;
				else
					movementDirection -= 1f;
			}

			return movementDirection;
		}

		@Override
		public void dispose() {
			spriteBatch.dispose();
			world.dispose();
		}

	}

	@Override
	public void create() {
		Converters.register(Vector2.class, LibgdxConverters.vector2());
		Converters.register(Color.class, LibgdxConverters.color());
		setScreen(new ScreenImpl(new SuperSheepGameState()));
		// Gdx.input.setCatchBackKey(true);
	}

	@Override
	public void render() {
		super.render();
		if (Gdx.input.isKeyPressed(Keys.R) || Gdx.input.isKeyPressed(Keys.MENU)) {
			getScreen().dispose();
			setScreen(new ScreenImpl(new SuperSheepGameState()));
		}
	}

}
