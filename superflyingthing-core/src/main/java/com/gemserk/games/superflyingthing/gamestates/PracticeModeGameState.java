package com.gemserk.games.superflyingthing.gamestates;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.gemserk.commons.gdx.GameStateImpl;
import com.gemserk.commons.gdx.box2d.BodyBuilder;
import com.gemserk.commons.gdx.box2d.Box2DCustomDebugRenderer;
import com.gemserk.commons.gdx.camera.Camera;
import com.gemserk.commons.gdx.camera.CameraRestrictedImpl;
import com.gemserk.commons.gdx.camera.Libgdx2dCamera;
import com.gemserk.commons.gdx.camera.Libgdx2dCameraTransformImpl;
import com.gemserk.commons.gdx.games.Physics;
import com.gemserk.commons.gdx.games.Spatial;
import com.gemserk.commons.gdx.graphics.ImmediateModeRendererUtils;
import com.gemserk.commons.gdx.graphics.SpriteBatchUtils;
import com.gemserk.games.entities.Entity;
import com.gemserk.games.entities.EntityLifeCycleHandler;
import com.gemserk.games.entities.EntityManager;
import com.gemserk.games.entities.EntityManagerImpl;
import com.gemserk.games.superflyingthing.ComponentWrapper;
import com.gemserk.games.superflyingthing.Components.AliveComponent;
import com.gemserk.games.superflyingthing.Components.AttachableComponent;
import com.gemserk.games.superflyingthing.Components.AttachmentComponent;
import com.gemserk.games.superflyingthing.Components.MovementComponent;
import com.gemserk.games.superflyingthing.Components.SpriteComponent;
import com.gemserk.games.superflyingthing.Components.TargetComponent;
import com.gemserk.games.superflyingthing.EntityFactory;
import com.gemserk.games.superflyingthing.Game;
import com.gemserk.games.superflyingthing.PhysicsContactListener;
import com.gemserk.games.superflyingthing.resources.GameResourceBuilder;
import com.gemserk.resources.ResourceManager;
import com.gemserk.resources.ResourceManagerImpl;

// All screens/gamestates should be the same, and have different game world instantiations...

public class PracticeModeGameState extends GameStateImpl {

	class RealGame implements EntityLifeCycleHandler {

		private Entity startPlanet;
		
		private World world;
		private EntityManager entityManager;
		
		public RealGame(World world) {
			this.entityManager = new EntityManagerImpl(this);
			this.world = world;
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
			Physics physics = ComponentWrapper.getPhysics(e);
			if (physics == null)
				return;

			Body body = physics.getBody();
			body.setUserData(null);

			com.gemserk.commons.gdx.box2d.Contact contact = physics.getContact();

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

				Physics otherPhysics = ComponentWrapper.getPhysics(otherEntity);
				otherPhysics.getContact().removeContact(body);
			}

			world.destroyBody(body);
			Gdx.app.log("SuperSheep", "removing body from physics world");
		}

		private void diposeJoints(Entity e) {
			AttachmentComponent entityAttachment = ComponentWrapper.getEntityAttachment(e);
			if (entityAttachment == null)
				return;
			if (entityAttachment.getJoint() == null)
				return;
			world.destroyJoint(entityAttachment.getJoint());
			Gdx.app.log("SuperSheep", "removing joints from physics world");
		}

		public void update(int delta) {
			world.step(Gdx.app.getGraphics().getDeltaTime(), 3, 3);

			entityManager.update(delta);

			updateCameraTarget(delta);
			updateHandleDeadShipBehavior(delta, getShip());
			updateCreateNewShipOnStartPlanet(delta, getShip());
		}
		
		boolean shouldCreateNewShip = false;
		private Entity ship;

		private void updateHandleDeadShipBehavior(int delta, Entity e) {
			AliveComponent aliveComponent = e.getComponent(AliveComponent.class);

			if (aliveComponent == null)
				return;
			if (!aliveComponent.isDead())
				return;

			entityManager.remove(e);
			
			Spatial superSheepSpatial = ComponentWrapper.getSpatial(e);

			Entity deadSuperSheepEntity = entityFactory.deadShip(superSheepSpatial);
			entityManager.add(deadSuperSheepEntity);
			
			shouldCreateNewShip = true;
		}

		private void updateCreateNewShipOnStartPlanet(int delta, Entity e) {
			if (!shouldCreateNewShip)
				return;
			
			Entity newSuperSheep = entityFactory.ship(5f, 6f, new Vector2(1f, 0f));
			entityManager.add(newSuperSheep);

			AttachmentComponent attachmentComponent = getStartPlanet().getComponent(AttachmentComponent.class);
			attachmentComponent.setEntity(newSuperSheep);

			setShip(newSuperSheep);
			
			shouldCreateNewShip = false;
		}

		private void updateCameraTarget(int delta) {
			AttachableComponent attachableComponent = getShip().getComponent(AttachableComponent.class);
			TargetComponent targetComponent = camera.getComponent(TargetComponent.class);

			if (attachableComponent.getOwner() != null)
				targetComponent.setTarget(attachableComponent.getOwner());
			else
				targetComponent.setTarget(getShip());
		}

		void setShip(Entity ship) {
			this.ship = ship;
		}

		Entity getShip() {
			return ship;
		}

		void setStartPlanet(Entity startPlanet) {
			this.startPlanet = startPlanet;
		}

		Entity getStartPlanet() {
			return startPlanet;
		}

	}

	private final Game game;
	SpriteBatch spriteBatch;
	Libgdx2dCamera libgdxCamera;
	World world;
	Box2DCustomDebugRenderer box2dCustomDebugRenderer;
	BodyBuilder bodyBuilder;

	EntityFactory entityFactory;
	EntityManager entityManager;

	Entity camera;

	RealGame realGame;

	public PracticeModeGameState(Game game) {
		this.game = game;
	}

	@Override
	public void init() {
		spriteBatch = new SpriteBatch();
		libgdxCamera = new Libgdx2dCameraTransformImpl();

		world = new World(new Vector2(), false);
		world.setContactListener(new PhysicsContactListener());

		realGame = new RealGame(world);
		entityManager = realGame.entityManager;
		
		ResourceManager<String> resourceManager = new ResourceManagerImpl<String>();
		GameResourceBuilder.loadResources(resourceManager);

		entityFactory = new EntityFactory(world, entityManager, resourceManager);

		libgdxCamera.center(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);
		// cameraData = new CameraImpl(0f, 0f, 32f, 0f);
		Camera cameraData = new CameraRestrictedImpl(0f, 0f, 42f, 0f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), new Rectangle(0f, 0f, 100f, 15f));

		// camera.zoom(32f);

		box2dCustomDebugRenderer = new Box2DCustomDebugRenderer((Libgdx2dCameraTransformImpl) libgdxCamera, world);

		bodyBuilder = new BodyBuilder(world);

		Vector2[] vertices = new Vector2[] { new Vector2(3f, 1.5f), new Vector2(1f, 4f), new Vector2(-2.5f, 1f), new Vector2(-1.5f, -2.5f), new Vector2(1f, -1.5f), };

		for (int i = 0; i < 10; i++) {
			entityManager.add(entityFactory.obstacle(vertices, 17f + i * 8f, MathUtils.random(0f, 15f), 0f));
			entityManager.add(entityFactory.obstacle(vertices, 12f + i * 8f, MathUtils.random(0f, 15f), 90f));
		}

		for (int i = 0; i < 10; i++) {
			float x = MathUtils.random(10f, 90f);
			float y = MathUtils.random(2f, 13f);
			entityManager.add(entityFactory.diamond(x, y, 0.2f));
		}

		camera = entityFactory.camera(cameraData);
		entityManager.add(camera);

		Entity ship = entityFactory.ship(5f, 7.5f, new Vector2(1f, 0f));
		entityManager.add(ship);

		Entity startPlanet = entityFactory.startPlanet(5f, 7.5f, 1f);

		AttachmentComponent attachmentComponent = startPlanet.getComponent(AttachmentComponent.class);
		attachmentComponent.setEntity(ship);

		entityManager.add(startPlanet);
		entityManager.add(entityFactory.destinationPlanet(95f, 7.5f, 1f));

		float worldWidth = 100f;
		float worldHeight = 20f;

		float x = worldWidth * 0.5f;
		float y = worldHeight * 0.5f;

		entityManager.add(entityFactory.boxObstacle(x, 0f, worldWidth, 0.1f, 0f));
		entityManager.add(entityFactory.boxObstacle(x, 15f, worldWidth, 0.1f, 0f));
		entityManager.add(entityFactory.boxObstacle(0, y, 0.1f, worldHeight, 0f));
		entityManager.add(entityFactory.boxObstacle(100f, y, 0.1f, worldHeight, 0f));

		realGame.setShip(ship);
		realGame.setStartPlanet(startPlanet);
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
			SpriteComponent spriteComponent = ComponentWrapper.getSprite(e);
			if (spriteComponent == null)
				continue;
			Sprite sprite = spriteComponent.getSprite();
			sprite.setSize(spatial.getWidth(), spatial.getHeight());
			sprite.setColor(spriteComponent.getColor());
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
		Vector2 direction = movementComponent.getDirection();
		float x = position.x + direction.tmp().mul(0.5f).x;
		float y = position.y + direction.tmp().mul(0.5f).y;
		ImmediateModeRendererUtils.drawLine(position.x, position.y, x, y, Color.GREEN);
	}

	@Override
	public void update(int delta) {

		if (Gdx.input.isKeyPressed(Keys.ESCAPE) || Gdx.input.isKeyPressed(Keys.BACK)) {
			game.transition(game.getMainMenuScreen(), 500, 500);
		}

		if (Gdx.input.isKeyPressed(Keys.R) || Gdx.input.isKeyPressed(Keys.MENU)) {
			dispose();
			init();
		}

		realGame.update(delta);
	}

	@Override
	public void resume() {
		// automatically handled in Game class and if no previous screen, then don't handle it (or system.exit())
		super.resume();
		Gdx.input.setCatchBackKey(true);
	}

	@Override
	public void pause() {
		super.pause();
		Gdx.input.setCatchBackKey(false);
	}

	@Override
	public void dispose() {
		spriteBatch.dispose();
		world.dispose();
	}
}