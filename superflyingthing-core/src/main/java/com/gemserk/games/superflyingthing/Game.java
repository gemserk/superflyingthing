package com.gemserk.games.superflyingthing;

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
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactListener;
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
import com.gemserk.commons.gdx.games.Spatial;
import com.gemserk.commons.gdx.graphics.ImmediateModeRendererUtils;
import com.gemserk.commons.gdx.graphics.SpriteBatchUtils;
import com.gemserk.games.entities.Entity;
import com.gemserk.games.entities.EntityLifeCycleHandler;
import com.gemserk.games.entities.EntityManager;
import com.gemserk.games.entities.EntityManagerImpl;
import com.gemserk.games.superflyingthing.Components.AliveComponent;
import com.gemserk.games.superflyingthing.Components.AttachableComponent;
import com.gemserk.games.superflyingthing.Components.AttachmentComponent;
import com.gemserk.games.superflyingthing.Components.EntityAttachment;
import com.gemserk.games.superflyingthing.Components.MovementComponent;
import com.gemserk.games.superflyingthing.Components.PhysicsComponent;
import com.gemserk.games.superflyingthing.Components.TargetComponent;

public class Game extends com.gemserk.commons.gdx.Game {

	public static short AllCategoryBits = 0xFF;

	public static short ShipCategoryBits = 1;

	public static short MiniPlanetCategoryBits = 2;

	public static class SuperSheepGameState extends GameStateImpl implements ContactListener, EntityLifeCycleHandler {

		SpriteBatch spriteBatch;
		Libgdx2dCamera libgdxCamera;
		World world;
		Box2DCustomDebugRenderer box2dCustomDebugRenderer;
		BodyBuilder bodyBuilder;

		EntityFactory entityFactory;
		EntityManager entityManager;
		Entity startPlanet;
		Entity ship;
		Entity camera;

		@Override
		public void init() {
			entityManager = new EntityManagerImpl(this);
			spriteBatch = new SpriteBatch();
			libgdxCamera = new Libgdx2dCameraTransformImpl();

			world = new World(new Vector2(), false);
			world.setContactListener(this);

			entityFactory = new EntityFactory(world, entityManager);

			libgdxCamera.center(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);
			// cameraData = new CameraImpl(0f, 0f, 32f, 0f);
			Camera cameraData = new CameraRestrictedImpl(0f, 0f, 42f, 0f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), new Rectangle(0f, 0f, 100f, 15f));

			// camera.zoom(32f);

			box2dCustomDebugRenderer = new Box2DCustomDebugRenderer((Libgdx2dCameraTransformImpl) libgdxCamera, world);

			Texture whiteRectangle = new Texture(Gdx.files.internal("data/images/white-rectangle.png"));
			Sprite sprite = new Sprite(whiteRectangle);
			sprite.setSize(0.5f, 0.5f);

			bodyBuilder = new BodyBuilder(world);

			Vector2[] vertices = new Vector2[] { new Vector2(3f, 1.5f), new Vector2(1f, 4f), new Vector2(-2.5f, 1f), new Vector2(-1.5f, -2.5f), new Vector2(1f, -1.5f), };

			for (int i = 0; i < 10; i++) {
				entityManager.add(entityFactory.obstacle(vertices, 17f + i * 8f, MathUtils.random(0f, 15f), 0f));
				entityManager.add(entityFactory.obstacle(vertices, 12f + i * 8f, MathUtils.random(0f, 15f), 90f));
			}

			for (int i = 0; i < 10; i++) {
				float x = MathUtils.random(10f, 90f);
				float y = MathUtils.random(2f, 13f);
				entityManager.add(entityFactory.diamond(x, y, 0.2f, sprite));
			}

			camera = entityFactory.camera(cameraData);
			entityManager.add(camera);

			ship = entityFactory.ship(5f, 7.5f, sprite, new Vector2(1f, 0f));
			entityManager.add(ship);

			startPlanet = entityFactory.startPlanet(5f, 7.5f, 1f);

			AttachmentComponent attachmentComponent = startPlanet.getComponent(AttachmentComponent.class);
			attachmentComponent.entityAttachment.entity = ship;
			// startMiniPlanet.attachSuperSheep(superSheep);

			entityManager.add(startPlanet);
			entityManager.add(entityFactory.destinationPlanet(95f, 7.5f, 1f));
			// entityManager.add(entityFactory.destinationPlanet(15f, 7.5f, 1f));

			float worldWidth = 100f;
			float worldHeight = 20f;

			float x = worldWidth * 0.5f;
			float y = worldHeight * 0.5f;

			entityManager.add(entityFactory.boxObstacle(x, 0f, worldWidth, 0.1f, 0f));
			entityManager.add(entityFactory.boxObstacle(x, 15f, worldWidth, 0.1f, 0f));
			entityManager.add(entityFactory.boxObstacle(0, y, 0.1f, worldHeight, 0f));
			entityManager.add(entityFactory.boxObstacle(100f, y, 0.1f, worldHeight, 0f));

		}

		@Override
		public void beginContact(Contact contact) {
			Body bodyA = contact.getFixtureA().getBody();
			Body bodyB = contact.getFixtureB().getBody();

			Entity entityA = (Entity) bodyA.getUserData();
			Entity entityB = (Entity) bodyB.getUserData();

			if (entityA != null) {
				PhysicsComponent physicsComponent = entityA.getComponent(PhysicsComponent.class);
				physicsComponent.getContact().addContact(contact, bodyB);
			}

			if (entityB != null) {
				PhysicsComponent physicsComponent = entityB.getComponent(PhysicsComponent.class);
				physicsComponent.getContact().addContact(contact, bodyA);
			}
		}

		@Override
		public void endContact(Contact contact) {
			Body bodyA = contact.getFixtureA().getBody();
			Body bodyB = contact.getFixtureB().getBody();

			Entity entityA = (Entity) bodyA.getUserData();
			Entity entityB = (Entity) bodyB.getUserData();

			if (entityA != null) {
				PhysicsComponent physicsComponent = entityA.getComponent(PhysicsComponent.class);
				physicsComponent.getContact().removeContact(bodyB);
			}

			if (entityB != null) {
				PhysicsComponent physicsComponent = entityB.getComponent(PhysicsComponent.class);
				physicsComponent.getContact().removeContact(bodyA);
			}
		}

		@Override
		public void init(Entity e) {

		}

		@Override
		public void dispose(Entity e) {
			diposeJoints(e);
			disposeBody(e);
		}

		private void disposeBody(Entity e) {
			Body body = ComponentWrapper.getBody(e);
			if (body == null)
				return;

			PhysicsComponent component = e.getComponent(PhysicsComponent.class);

			body.setUserData(null);

			com.gemserk.commons.gdx.box2d.Contact contact = component.getContact();

			// removes contact from the other entity
			for (int i = 0; i < contact.getContactCount(); i++) {
				if (!contact.isInContact(i))
					continue;

				Body otherBody = contact.getBody(i);
				if (otherBody == null)
					continue;

				Entity otherEntity = (Entity) otherBody.getUserData();
				if (otherEntity == null)
					continue;

				PhysicsComponent otherPhyiscsComponent = otherEntity.getComponent(PhysicsComponent.class);
				otherPhyiscsComponent.getContact().removeContact(body);
			}

			world.destroyBody(body);
			Gdx.app.log("SuperSheep", "removing body from physics world");
		}

		private void diposeJoints(Entity e) {
			EntityAttachment entityAttachment = ComponentWrapper.getEntityAttachment(e);
			if (entityAttachment == null)
				return;
			if (entityAttachment.joint == null)
				return;
			world.destroyJoint(entityAttachment.joint);
			Gdx.app.log("SuperSheep", "removing joints from physics world");
		}

		@Override
		public void render(int delta) {
			Camera cameraData = ComponentWrapper.getCamera(camera);

			libgdxCamera.move(cameraData.getX(), cameraData.getY());
			libgdxCamera.zoom(cameraData.getZoom());
			libgdxCamera.rotate(cameraData.getAngle());

			Gdx.graphics.getGL10().glClear(GL10.GL_COLOR_BUFFER_BIT);
			libgdxCamera.apply(spriteBatch);

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

			entityManager.update(delta);

			updateCameraTarget(delta);

			AliveComponent aliveComponent = ship.getComponent(AliveComponent.class);

			if (aliveComponent == null)
				return;

			if (!aliveComponent.dead)
				return;

			entityManager.remove(ship);

			Spatial superSheepSpatial = ComponentWrapper.getSpatial(ship);
			Sprite superSheepSprite = ComponentWrapper.getSprite(ship);

			Sprite deadSuperSheepSprite = new Sprite(superSheepSprite);
			deadSuperSheepSprite.setColor(0.7f, 0.7f, 0.7f, 1f);

			Entity deadSuperSheepEntity = entityFactory.deadShip(superSheepSpatial, deadSuperSheepSprite);
			entityManager.add(deadSuperSheepEntity);

			Entity newSuperSheep = entityFactory.ship(5f, 6f, new Sprite(superSheepSprite), new Vector2(1f, 0f));
			entityManager.add(newSuperSheep);

			AttachmentComponent attachmentComponent = startPlanet.getComponent(AttachmentComponent.class);
			attachmentComponent.entityAttachment.entity = newSuperSheep;

			this.ship = newSuperSheep;
		}

		private void updateCameraTarget(int delta) {
			AttachableComponent attachableComponent = ship.getComponent(AttachableComponent.class);
			TargetComponent targetComponent = camera.getComponent(TargetComponent.class);

			if (attachableComponent.owner != null) {
				targetComponent.target = attachableComponent.owner;
			} else {
				targetComponent.target = ship;
			}
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
