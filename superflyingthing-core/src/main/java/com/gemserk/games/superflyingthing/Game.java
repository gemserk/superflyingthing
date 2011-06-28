package com.gemserk.games.superflyingthing;


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
import com.gemserk.games.entities.Behavior;
import com.gemserk.games.entities.Component;
import com.gemserk.games.entities.Entity;
import com.gemserk.games.entities.EntityLifeCycleHandler;
import com.gemserk.games.entities.EntityManager;
import com.gemserk.games.entities.EntityManagerImpl;

public class Game extends com.gemserk.commons.gdx.Game {

	public static short AllCategoryBits = 0xFF;

	public static short ShipCategoryBits = 1;

	public static short MiniPlanetCategoryBits = 2;

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

	class AttachmentComponent implements Component {

		EntityAttachment entityAttachment;

		public AttachmentComponent() {
			entityAttachment = new EntityAttachment();
		}

	}

	class AttachableComponent implements Component {

		Entity owner;

	}

	class ReleaseEntityComponent implements Component {

		int releaseTime;

	}

	class GrabbableComponent implements Component {

		boolean grabbed;

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

		public static EntityAttachment getEntityAttachment(Entity e) {
			AttachmentComponent component = getComponent(e, AttachmentComponent.class);
			if (component == null)
				return null;
			return component.entityAttachment;
		}

		@SuppressWarnings("unchecked")
		private static <T> T getComponent(Entity e, Class<? extends Component> clazz) {
			if (e == null)
				return null;
			return (T) e.getComponent(clazz);
		}

	}

	class CameraFollowBehavior extends Behavior {

		@Override
		public void update(int delta, Entity entity) {
			TargetComponent targetComponent = entity.getComponent(TargetComponent.class);
			Entity target = targetComponent.target;
			if (target == null)
				return;
			SpatialComponent spatialComponent = target.getComponent(SpatialComponent.class);
			if (spatialComponent == null)
				return;
			Camera camera = ComponentWrapper.getCamera(entity);
			camera.setPosition(spatialComponent.spatial.getX(), spatialComponent.spatial.getY());
		}

	}

	class FixMovementBehavior extends Behavior {

		@Override
		public void update(int delta, Entity e) {
			MovementComponent movementComponent = e.getComponent(MovementComponent.class);
			Vector2 direction = movementComponent.direction;

			direction.nor();

			Body body = ComponentWrapper.getBody(e);

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
		}

	}

	class AttachedEntityDirectionBehavior extends Behavior {

		@Override
		public void update(int delta, Entity e) {
			EntityAttachment entityAttachment = ComponentWrapper.getEntityAttachment(e);

			if (entityAttachment.entity == null)
				return;

			Spatial spatial = ComponentWrapper.getSpatial(e);
			Vector2 position = spatial.getPosition();

			Entity attachedEntity = entityAttachment.entity;
			Spatial attachedEntitySpatial = ComponentWrapper.getSpatial(attachedEntity);
			MovementComponent movementComponent = ComponentWrapper.getComponent(attachedEntity, MovementComponent.class);

			Vector2 superSheepPosition = attachedEntitySpatial.getPosition();

			Vector2 diff = superSheepPosition.sub(position).nor();
			diff.rotate(-90f);

			movementComponent.direction.set(diff);
		}

	}

	class AttachEntityBehavior extends Behavior {

		private final JointBuilder jointBuilder;

		public AttachEntityBehavior(JointBuilder jointBuilder) {
			this.jointBuilder = jointBuilder;
		}

		@Override
		public void update(int delta, Entity e) {
			EntityAttachment entityAttachment = ComponentWrapper.getEntityAttachment(e);
			if (entityAttachment.entity == null)
				return;
			if (entityAttachment.joint != null)
				return;

			AttachableComponent attachableComponent = entityAttachment.entity.getComponent(AttachableComponent.class);
			attachableComponent.owner = e;

			Spatial spatial = ComponentWrapper.getSpatial(e);
			entityAttachment.joint = jointBuilder.distanceJoint() //
					.bodyA(ComponentWrapper.getBody(entityAttachment.entity)) //
					.bodyB(ComponentWrapper.getBody(e)) //
					.collideConnected(false) //
					.length(spatial.getWidth() * 0.5f * 1.5f) //
					.build();
		}

	}

	class ReleaseAttachmentBehavior extends Behavior {

		private final World world;

		public ReleaseAttachmentBehavior(World world) {
			this.world = world;
		}

		@Override
		public void update(int delta, Entity e) {
			EntityAttachment entityAttachment = ComponentWrapper.getEntityAttachment(e);
			Entity attachedEntity = entityAttachment.entity;

			if (attachedEntity == null)
				return;

			ReleaseEntityComponent releaseEntityComponent = e.getComponent(ReleaseEntityComponent.class);

			if (releaseEntityComponent.releaseTime > 0)
				releaseEntityComponent.releaseTime -= delta;

			if (Gdx.app.getType() == ApplicationType.Android) {
				if (!Gdx.input.isTouched()) {
					return;
				}
			} else if (!Gdx.input.isKeyPressed(Keys.SPACE))
				return;

			if (releaseEntityComponent.releaseTime > 0)
				return;

			if (entityAttachment.joint != null)
				world.destroyJoint(entityAttachment.joint);

			AttachableComponent attachableComponent = attachedEntity.getComponent(AttachableComponent.class);
			attachableComponent.owner = null;

			entityAttachment.joint = null;
			entityAttachment.entity = null;

			releaseEntityComponent.releaseTime = 300;
		}

	}

	class NullBehavior extends Behavior {

		@Override
		public void update(int delta, Entity entity) {

		}

	}

	class RemoveWhenGrabbedBehavior extends Behavior {

		private final EntityManager entityManager;

		public RemoveWhenGrabbedBehavior(EntityManager entityManager) {
			this.entityManager = entityManager;
		}

		@Override
		public void update(int delta, Entity e) {
			GrabbableComponent grabbableComponent = e.getComponent(GrabbableComponent.class);
			if (grabbableComponent.grabbed)
				entityManager.remove(e);
		}
	}

	// starts the game...

	class SuperSheepGameState extends GameStateImpl implements ContactListener, EntityLifeCycleHandler {

		class EntityFactory {

			public Entity camera(Camera camera) {
				Entity e = new Entity();
				e.addComponent(new CameraComponent(camera));
				e.addComponent(new TargetComponent(null));
				e.addBehavior(new CameraFollowBehavior());
				return e;
			}

			public Entity ship(float x, float y, Sprite sprite, Vector2 direction) {
				float width = 0.4f;
				float height = 0.2f;

				Entity e = new Entity();

				Body body = bodyBuilder.mass(50f) //
						.boxShape(width * 0.3f, height * 0.3f) //
						.position(x, y) //
						.restitution(0f) //
						.type(BodyType.DynamicBody) //
						.categoryBits(ShipCategoryBits) //
						.maskBits((short) (AllCategoryBits & ~MiniPlanetCategoryBits)) //
						.userData(e) //
						.build();

				e.addComponent(new PhysicsComponent(body));
				e.addComponent(new SpatialComponent(new SpatialPhysicsImpl(body, width, height)));
				e.addComponent(new SpriteComponent(sprite));
				e.addComponent(new MovementComponent(direction.x, direction.y));
				e.addComponent(new AliveComponent(false));
				e.addComponent(new AttachableComponent());
				e.addBehavior(new FixMovementBehavior());
				return e;
			}

			public Entity diamond(float x, float y, float radius, Sprite sprite) {
				Entity e = new Entity();

				Body body = bodyBuilder.mass(50f) //
						.circleShape(radius) //
						.sensor() //
						.position(x, y) //
						.restitution(0f) //
						.type(BodyType.StaticBody) //
						.userData(e) //
						.build();

				e.addComponent(new PhysicsComponent(body));
				e.addComponent(new SpatialComponent(new SpatialPhysicsImpl(body, radius * 2, radius * 2)));
				e.addComponent(new SpriteComponent(sprite));
				e.addComponent(new GrabbableComponent());
				e.addBehavior(new RemoveWhenGrabbedBehavior(entityManager));
				return e;
			}

			public Entity deadShip(Spatial spatial, Sprite sprite) {
				Entity e = new Entity();
				e.addComponent(new SpatialComponent(new SpatialImpl(spatial)));
				e.addComponent(new SpriteComponent(sprite));
				return e;
			}

			public Entity startPlanet(float x, float y, float radius) {
				Entity e = new Entity();

				Body body = bodyBuilder.mass(1f) //
						.circleShape(radius * 0.1f) //
						.position(x, y) //
						.restitution(0f) //
						.type(BodyType.StaticBody) //
						.userData(e) //
						.categoryBits(MiniPlanetCategoryBits).build();

				e.addComponent(new PhysicsComponent(body));
				e.addComponent(new SpatialComponent(new SpatialPhysicsImpl(body, radius * 2, radius * 2)));
				e.addComponent(new AttachmentComponent());
				e.addComponent(new ReleaseEntityComponent());

				e.addBehavior(new ReleaseAttachmentBehavior(world));
				e.addBehavior(new AttachEntityBehavior(jointBuilder));
				e.addBehavior(new AttachedEntityDirectionBehavior());
				return e;
			}

			public Entity destinationPlanet(float x, float y, float radius) {
				Entity e = new Entity() ;

				Body body = bodyBuilder.mass(1f) //
						.circleShape(radius * 0.1f) //
						.position(x, y) //
						.restitution(0f) //
						.type(BodyType.StaticBody) //
						.userData(e) //
						.categoryBits(MiniPlanetCategoryBits).build();

				bodyBuilder.fixtureBuilder(body) //
						.circleShape(radius) //
						.categoryBits(AllCategoryBits) //
						.sensor() //
						.build();

				e.addComponent(new PhysicsComponent(body));
				e.addComponent(new SpatialComponent(new SpatialPhysicsImpl(body, radius * 2, radius * 2)));
				e.addComponent(new AttachmentComponent());
				e.addComponent(new ReleaseEntityComponent());

				e.addBehavior(new AttachEntityBehavior(jointBuilder));
				e.addBehavior(new AttachedEntityDirectionBehavior());
				return e;
			}

		}

		private SpriteBatch spriteBatch;
		private Libgdx2dCamera camera;
		private World world;
		private Box2DCustomDebugRenderer box2dCustomDebugRenderer;
		private BodyBuilder bodyBuilder;
		private JointBuilder jointBuilder;

		EntityFactory entityFactory;
		EntityManager entityManager;

		private Entity startMiniPlanet;
		private Entity superSheep;
		private Entity cameraFollowEntity;

		@Override
		public void init() {
			entityManager = new EntityManagerImpl(this);
			spriteBatch = new SpriteBatch();
			camera = new Libgdx2dCameraTransformImpl();
			entityFactory = new EntityFactory();

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

			for (int i = 0; i < 10; i++) {
				float x = MathUtils.random(10f, 90f);
				float y = MathUtils.random(2f, 13f);
				entityManager.add(entityFactory.diamond(x, y, 0.2f, sprite));
			}

			cameraFollowEntity = entityFactory.camera(cameraData);
			entityManager.add(cameraFollowEntity);

			superSheep = entityFactory.ship(5f, 7.5f, sprite, new Vector2(1f, 0f));
			entityManager.add(superSheep);

			startMiniPlanet = entityFactory.startPlanet(5f, 7.5f, 1f);

			AttachmentComponent attachmentComponent = startMiniPlanet.getComponent(AttachmentComponent.class);
			attachmentComponent.entityAttachment.entity = superSheep;
			// startMiniPlanet.attachSuperSheep(superSheep);

			entityManager.add(startMiniPlanet);
			entityManager.add(entityFactory.destinationPlanet(95f, 7.5f, 1f));
			// entityManager.add(entityFactory.destinationPlanet(15f, 7.5f, 1f));

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
			checkContactSuperSheep(contact.getFixtureA(), contact.getFixtureB());
			checkContactSuperSheep(contact.getFixtureB(), contact.getFixtureA());
		}

		private void checkContactSuperSheep(Fixture fixtureA, Fixture fixtureB) {
			if (fixtureA.getBody() != ComponentWrapper.getBody(superSheep))
				return;
			Entity e = (Entity) fixtureB.getBody().getUserData();
			if (e != null) {
				updateGrabbableEntity(e);
				updateAttachEntity(e);
			} else {
				Gdx.app.log("SuperSheep", "die!");
				AliveComponent aliveComponent = superSheep.getComponent(AliveComponent.class);
				aliveComponent.dead = true;
			}
		}

		private void updateAttachEntity(Entity e) {
			EntityAttachment entityAttachment = ComponentWrapper.getEntityAttachment(e);
			if (entityAttachment == null)
				return;
			if (entityAttachment.entity != null)
				return;
			Spatial spatial = ComponentWrapper.getSpatial(e);
			if (spatial == null)
				return;
			entityAttachment.entity = superSheep;
		}

		private void updateGrabbableEntity(Entity e) {
			GrabbableComponent grabbableComponent = e.getComponent(GrabbableComponent.class);
			if (grabbableComponent == null)
				return;
			if (grabbableComponent.grabbed)
				return;
			grabbableComponent.grabbed = true;
			Gdx.app.log("SuperSheep", "grabbed diamond!");
		}

		@Override
		public void endContact(Contact contact) {

		}

		@Override
		public void init(Entity e) {

		}

		@Override
		public void dispose(Entity e) {
			Body body = ComponentWrapper.getBody(e);
			if (body == null)
				return;
			world.destroyBody(body);
			Gdx.app.log("SuperSheep", "removing body from physics world");
		}

		@Override
		public void render(int delta) {
			Camera cameraData = ComponentWrapper.getCamera(cameraFollowEntity);

			camera.move(cameraData.getX(), cameraData.getY());
			camera.zoom(cameraData.getZoom());
			camera.rotate(cameraData.getAngle());

			Gdx.graphics.getGL10().glClear(GL10.GL_COLOR_BUFFER_BIT);
			camera.apply(spriteBatch);

			renderEntities();

			box2dCustomDebugRenderer.render();

			for (int i = 0; i < entityManager.entitiesCount(); i++) {
				Entity e = entityManager.get(i);
				renderMovementDebug(e);
				renderAttachmentDebug(e);
			}

		}

		private void renderEntities() {
			spriteBatch.begin();
			for (int i = 0; i < entityManager.entitiesCount(); i++) {
				Entity e = entityManager.get(i);
				Spatial spatial = ComponentWrapper.getSpatial(e);
				if (spatial == null)
					continue;
				Sprite sprite = ComponentWrapper.getSprite(e);
				if (sprite == null)
					continue;
				sprite.setSize(spatial.getWidth(), spatial.getHeight());
				Vector2 position = spatial.getPosition();
				SpriteBatchUtils.drawCentered(spriteBatch, sprite, position.x, position.y, spatial.getAngle());
			}
			spriteBatch.end();
		}

		private void renderAttachmentDebug(Entity e) {
			Spatial spatial = ComponentWrapper.getSpatial(e);
			if (spatial == null)
				return;
			AttachmentComponent attachmentComponent = e.getComponent(AttachmentComponent.class);
			if (attachmentComponent == null)
				return;
			Vector2 position = spatial.getPosition();
			ImmediateModeRendererUtils.drawSolidCircle(position, spatial.getWidth() * 0.5f, Color.BLUE);
		}

		private void renderMovementDebug(Entity e) {
			Spatial spatial = ComponentWrapper.getSpatial(e);
			if (spatial == null)
				return;
			Vector2 position = spatial.getPosition();
			MovementComponent movementComponent = e.getComponent(MovementComponent.class);
			if (movementComponent == null)
				return;
			Vector2 direction = movementComponent.direction;
			float x = position.x + direction.tmp().mul(0.5f).x;
			float y = position.y + direction.tmp().mul(0.5f).y;
			ImmediateModeRendererUtils.drawLine(position.x, position.y, x, y, Color.GREEN);
		}

		@Override
		public void update(int delta) {
			world.step(Gdx.app.getGraphics().getDeltaTime(), 3, 3);

			MovementComponent movementComponent = superSheep.getComponent(MovementComponent.class);
			calculateDirectionFromInput(delta, movementComponent.direction);

			entityManager.update(delta);

			AttachableComponent attachableComponent = superSheep.getComponent(AttachableComponent.class);
			TargetComponent targetComponent = cameraFollowEntity.getComponent(TargetComponent.class);

			if (attachableComponent.owner != null) {
				targetComponent.target = attachableComponent.owner;
			} else {
				targetComponent.target = superSheep;
			}

			AliveComponent aliveComponent = superSheep.getComponent(AliveComponent.class);
			if (!aliveComponent.dead)
				return;

			entityManager.remove(superSheep);

			Spatial superSheepSpatial = ComponentWrapper.getSpatial(superSheep);
			Sprite superSheepSprite = ComponentWrapper.getSprite(superSheep);

			Sprite deadSuperSheepSprite = new Sprite(superSheepSprite);
			deadSuperSheepSprite.setColor(0.7f, 0.7f, 0.7f, 1f);

			Entity deadSuperSheepEntity = entityFactory.deadShip(superSheepSpatial, deadSuperSheepSprite);
			entityManager.add(deadSuperSheepEntity);

			Entity newSuperSheep = entityFactory.ship(5f, 6f, new Sprite(superSheepSprite), new Vector2(1f, 0f));
			entityManager.add(newSuperSheep);

			AttachmentComponent attachmentComponent = startMiniPlanet.getComponent(AttachmentComponent.class);
			attachmentComponent.entityAttachment.entity = newSuperSheep;

			this.superSheep = newSuperSheep;
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
