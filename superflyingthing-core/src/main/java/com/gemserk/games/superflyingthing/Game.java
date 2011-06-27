package com.gemserk.games.superflyingthing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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

		Map<Class<? extends Component>, Component> components;

		@SuppressWarnings("unchecked")
		<T extends Component> T getComponent(Class<T> clazz) {
			return (T) components.get(clazz);
		}

		public Entity() {
			components = new HashMap<Class<? extends Component>, Component>();
		}

		void addComponent(Component component) {
			addComponent(component.getClass(), component);
		}

		void addComponent(Class<? extends Component> clazz, Component component) {
			components.put(clazz, component);
		}

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

	class PhysicsComponent implements Component {

		Body body;

		public PhysicsComponent(Body body) {
			this.body = body;
		}

	}

	class SpatialComponent implements Component {

		Spatial spatial;

		public SpatialComponent(Spatial spatial) {
			this.spatial = spatial;
		}

	}

	class CameraComponent implements Component {

		Camera camera;

		public CameraComponent(Camera camera) {
			this.camera = camera;
		}

	}

	class SpriteComponent implements Component {

		Sprite sprite;

		public SpriteComponent(Sprite sprite) {
			this.sprite = sprite;
		}

	}

	class MovementComponent implements Component {

		final Vector2 direction = new Vector2();

		public MovementComponent(float dx, float dy) {
			direction.set(dx, dy);
		}

	}

	class AliveComponent implements Component {

		boolean dead;

		public AliveComponent(boolean dead) {
			this.dead = dead;
		}

	}

	class TargetComponent implements Component {

		Entity target;

		public TargetComponent(Entity target) {
			this.target = target;
		}

	}

	class EntityAttachment {

		Entity entity;

		Joint joint;

	}

	class EntityAttachmentComponent implements Component {

		EntityAttachment entityAttachment;

		public EntityAttachmentComponent() {
			entityAttachment = new EntityAttachment();
		}

	}

	// custom for this game, only works fine if each component has only one value.

	static class ComponentWrapper {

		public static Body getBody(Entity e) {
			PhysicsComponent component = getComponent(e, PhysicsComponent.class);
			if (component == null)
				return null;
			return component.body;
		}

		public static Spatial getSpatial(Entity e) {
			SpatialComponent component = getComponent(e, SpatialComponent.class);
			if (component == null)
				return null;
			return component.spatial;
		}

		public static Sprite getSprite(Entity e) {
			SpriteComponent component = getComponent(e, SpriteComponent.class);
			if (component == null)
				return null;
			return component.sprite;
		}

		public static Camera getCamera(Entity e) {
			CameraComponent component = getComponent(e, CameraComponent.class);
			if (component == null)
				return null;
			return component.camera;
		}

		private static <T> T getComponent(Entity e, Class<? extends Component> clazz) {
			if (e == null)
				return null;
			return (T) e.getComponent(clazz);
		}

	}

	class SuperSheepGameState extends GameStateImpl implements ContactListener {

		class CameraFollowEntity extends Entity {

			public Camera getCamera() {
				return ComponentWrapper.getCamera(this);
			}

			public CameraFollowEntity(Camera camera) {
				addComponent(new CameraComponent(camera));
				addComponent(new TargetComponent(null));
			}

			public void update(int delta) {
				TargetComponent targetComponent = getComponent(TargetComponent.class);
				Entity entity = targetComponent.target;
				if (entity == null)
					return;
				SpatialComponent spatialComponent = entity.getComponent(SpatialComponent.class);
				if (spatialComponent == null)
					return;
				getCamera().setPosition(spatialComponent.spatial.getX(), spatialComponent.spatial.getY());
			}

			public void follow(Entity entity) {
				TargetComponent targetComponent = getComponent(TargetComponent.class);
				targetComponent.target = entity;
			}

		}

		class SuperSheep extends Entity {

			public Spatial getSpatial() {
				return ComponentWrapper.getSpatial(this);
			}

			public Body getBody() {
				return ComponentWrapper.getBody(this);
			}

			Sprite getSprite() {
				return ComponentWrapper.getSprite(this);
			}

			Vector2 getDirection() {
				return getMovementComponent().direction;
			}

			void setDead(boolean dead) {
				getAliveComponent().dead = dead;
			}

			boolean isDead() {
				return getAliveComponent().dead;
			}

			public MovementComponent getMovementComponent() {
				return getComponent(MovementComponent.class);
			}

			public AliveComponent getAliveComponent() {
				return getComponent(AliveComponent.class);
			}

			public SuperSheep(float x, float y, Sprite sprite, Vector2 direction) {
				float width = 0.4f;
				float height = 0.2f;

				PhysicsComponent physicsComponent = new PhysicsComponent(bodyBuilder.mass(50f) //
						.boxShape(width * 0.3f, height * 0.3f) //
						.position(x, y) //
						.restitution(0f) //
						.type(BodyType.DynamicBody) //
						.categoryBits(ShipCategoryBits).maskBits((short) (AllCategoryBits & ~MiniPlanetCategoryBits)).build());
				SpatialComponent spatialComponent = new SpatialComponent(new SpatialPhysicsImpl(physicsComponent.body, width, height));

				addComponent(physicsComponent);
				addComponent(spatialComponent);
				addComponent(new SpriteComponent(sprite));

				addComponent(new MovementComponent(direction.x, direction.y));
				addComponent(new AliveComponent(false));
			}

			public void update(int delta) {
				getDirection().nor();

				Body body = getBody();

				Vector2 position = body.getTransform().getPosition();
				float desiredAngle = getDirection().angle();

				body.setTransform(position, desiredAngle * MathUtils.degreesToRadians);
				body.applyForce(getDirection().tmp().mul(5000f), position);

				Vector2 linearVelocity = body.getLinearVelocity();

				float speed = linearVelocity.len();

				linearVelocity.set(getDirection().tmp().mul(speed));

				float maxSpeed = 6f;
				if (speed > maxSpeed) {
					linearVelocity.mul(maxSpeed / speed);
					body.setLinearVelocity(linearVelocity);
				}

				getSprite().setPosition(position.x, position.y);
				getSprite().setSize(getSpatial().getWidth(), getSpatial().getHeight());
			}

			public void draw(SpriteBatch spriteBatch) {
				Vector2 position = getSpatial().getPosition();
				SpriteBatchUtils.drawCentered(spriteBatch, getSprite(), position.x, position.y, getSpatial().getAngle());
			}

			public void drawDebug() {
				Vector2 position = getSpatial().getPosition();
				float x = position.x + getDirection().tmp().mul(0.5f).x;
				float y = position.y + getDirection().tmp().mul(0.5f).y;
				ImmediateModeRendererUtils.drawLine(position.x, position.y, x, y, Color.GREEN);
			}

			public void dispose() {
				world.destroyBody(getBody());
			}

		}

		class DeadSuperSheepEntity extends Entity {

			public DeadSuperSheepEntity(Spatial spatial, Sprite sprite) {
				addComponent(new SpatialComponent(new SpatialImpl(spatial)));
				addComponent(new SpriteComponent(sprite));
			}

			public void draw(SpriteBatch spriteBatch) {
				Sprite sprite = ComponentWrapper.getSprite(this);
				Spatial spatial = ComponentWrapper.getSpatial(this);
				sprite.setColor(0.7f, 0.7f, 0.7f, 1f);
				Vector2 position = spatial.getPosition();
				SpriteBatchUtils.drawCentered(spriteBatch, sprite, position.x, position.y, spatial.getAngle());
			}

		}

		class MiniPlanet extends Entity {

			float radius;

			int releaseTime = 0;

			private EntityAttachment getEntityAttachment() {
				EntityAttachmentComponent entityAttachmentComponent = getComponent(EntityAttachmentComponent.class);
				return entityAttachmentComponent.entityAttachment;
			}

			public MiniPlanet(float x, float y, float radius) {
				this.radius = radius;

				Body body = bodyBuilder.mass(1000f) //
						.circleShape(radius * 0.1f) //
						.position(x, y) //
						.restitution(0f) //
						.type(BodyType.StaticBody) //
						.categoryBits(MiniPlanetCategoryBits).build();

				addComponent(new PhysicsComponent(body));
				addComponent(new SpatialComponent(new SpatialPhysicsImpl(body, radius * 2, radius * 2)));
				addComponent(new EntityAttachmentComponent());
			}

			public void update(int delta) {
				processInput(delta);

				if (getEntityAttachment().entity == null)
					return;

				if (releaseTime > 0)
					releaseTime -= delta;

				Spatial spatial = ComponentWrapper.getSpatial(this);
				Vector2 position = spatial.getPosition();

				Entity attachedEntity = getEntityAttachment().entity;
				Spatial attachedEntitySpatial = ComponentWrapper.getSpatial(attachedEntity);
				MovementComponent movementComponent = ComponentWrapper.getComponent(attachedEntity, MovementComponent.class);

				Vector2 superSheepPosition = attachedEntitySpatial.getPosition();

				Vector2 diff = superSheepPosition.sub(position).nor();
				diff.rotate(-90f);

				movementComponent.direction.set(diff);

			}

			protected void processInput(int delta) {
				EntityAttachment entityAttachment = getEntityAttachment();
				Entity attachedEntity = entityAttachment.entity;

				if (attachedEntity == null)
					return;

				if (Gdx.app.getType() == ApplicationType.Android) {
					if (!Gdx.input.isTouched()) {
						return;
					}
				} else if (!Gdx.input.isKeyPressed(Keys.SPACE))
					return;

				if (releaseTime > 0)
					return;

				cameraFollowEntity.follow(attachedEntity);

				world.destroyJoint(entityAttachment.joint);

				entityAttachment.joint = null;
				entityAttachment.entity = null;
			}

			public void drawDebug() {
				Spatial spatial = ComponentWrapper.getSpatial(this);
				Vector2 position = spatial.getPosition();
				ImmediateModeRendererUtils.drawSolidCircle(position, radius, Color.BLUE);
			}

			public void attachSuperSheep(SuperSheep superSheep) {
				EntityAttachment entityAttachment = getEntityAttachment();

				entityAttachment.entity = superSheep;
				entityAttachment.joint = jointBuilder.distanceJoint() //
						.bodyA(superSheep.getBody()) //
						.bodyB(ComponentWrapper.getBody(this)) //
						.collideConnected(false) //
						.length(1.5f) //
						.build();

				cameraFollowEntity.follow(this);
				releaseTime = 500;
			}

			public boolean containsSuperSheep(SuperSheep superSheep) {
				EntityAttachment entityAttachment = getEntityAttachment();
				return entityAttachment.entity == superSheep;
			}

		}

		class DestinationPlanet extends MiniPlanet {

			public DestinationPlanet(float x, float y, float radius) {
				super(x, y, radius);
			}

			@Override
			public void update(int delta) {
				Spatial spatial = ComponentWrapper.getSpatial(this);
				for (int i = 0; i < SuperSheepGameState.this.superSheeps.size(); i++) {
					SuperSheep superSheep = SuperSheepGameState.this.superSheeps.get(i);
					if (spatial.getPosition().dst(superSheep.getSpatial().getPosition()) < 1.2f && !containsSuperSheep(superSheep)) {
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
				superSheep.setDead(true);
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
				calculateDirectionFromInput(delta, superSheep.getDirection());
				superSheep.update(delta);
			}

			for (int i = 0; i < deadSuperSheeps.size(); i++)
				deadSuperSheeps.get(i).update(delta);

			for (int i = 0; i < miniPlanets.size(); i++)
				miniPlanets.get(i).update(delta);

			cameraFollowEntity.update(delta);

			for (int i = 0; i < superSheeps.size(); i++) {
				SuperSheep superSheep = superSheeps.get(i);

				if (!superSheep.isDead())
					continue;

				DeadSuperSheepEntity deadSuperSheepEntity = new DeadSuperSheepEntity(superSheep.getSpatial(), superSheep.getSprite());
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

				SuperSheep newSuperSheep = new SuperSheep(5f, 7.5f, new Sprite(superSheep.getSprite()), new Vector2(1f, 0f));
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
