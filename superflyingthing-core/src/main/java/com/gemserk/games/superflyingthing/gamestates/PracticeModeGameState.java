package com.gemserk.games.superflyingthing.gamestates;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.gemserk.commons.gdx.GameStateImpl;
import com.gemserk.commons.gdx.box2d.BodyBuilder;
import com.gemserk.commons.gdx.box2d.Box2DCustomDebugRenderer;
import com.gemserk.commons.gdx.camera.Camera;
import com.gemserk.commons.gdx.camera.CameraRestrictedImpl;
import com.gemserk.commons.gdx.camera.Libgdx2dCamera;
import com.gemserk.commons.gdx.camera.Libgdx2dCameraTransformImpl;
import com.gemserk.games.entities.Entity;
import com.gemserk.games.entities.EntityManager;
import com.gemserk.games.superflyingthing.Behaviors.CreateDeadShipBehavior;
import com.gemserk.games.superflyingthing.Behaviors.CreateNewShipBehavior;
import com.gemserk.games.superflyingthing.Behaviors.FixCameraTargetBehavior;
import com.gemserk.games.superflyingthing.Behaviors.RemoveDeadShipBehavior;
import com.gemserk.games.superflyingthing.ComponentWrapper;
import com.gemserk.games.superflyingthing.Components.AttachmentComponent;
import com.gemserk.games.superflyingthing.Components.GameDataComponent;
import com.gemserk.games.superflyingthing.EntityTemplates;
import com.gemserk.games.superflyingthing.Game;
import com.gemserk.games.superflyingthing.resources.GameResourceBuilder;
import com.gemserk.resources.ResourceManager;
import com.gemserk.resources.ResourceManagerImpl;

// All screens/gamestates should be the same, and have different game world instantiations...

public class PracticeModeGameState extends GameStateImpl {
	
	private final Game game;
	SpriteBatch spriteBatch;
	Libgdx2dCamera libgdxCamera;
	Box2DCustomDebugRenderer box2dCustomDebugRenderer;
	BodyBuilder bodyBuilder;

	EntityTemplates entityTemplates;

	Entity camera;

	RealGame realGame;

	public PracticeModeGameState(Game game) {
		this.game = game;
	}

	@Override
	public void init() {
		spriteBatch = new SpriteBatch();
		libgdxCamera = new Libgdx2dCameraTransformImpl();

		realGame = new RealGame();
		
		EntityManager entityManager = realGame.entityManager;
		World world = realGame.getWorld();
		
		ResourceManager<String> resourceManager = new ResourceManagerImpl<String>();
		GameResourceBuilder.loadResources(resourceManager);

		entityTemplates = new EntityTemplates(world, entityManager, resourceManager);

		libgdxCamera.center(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);
		// cameraData = new CameraImpl(0f, 0f, 32f, 0f);
		Camera cameraData = new CameraRestrictedImpl(0f, 0f, 42f, 0f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), new Rectangle(0f, 0f, 100f, 15f));

		// camera.zoom(32f);

		box2dCustomDebugRenderer = new Box2DCustomDebugRenderer((Libgdx2dCameraTransformImpl) libgdxCamera, world);

		bodyBuilder = new BodyBuilder(world);

		Vector2[] vertices = new Vector2[] { new Vector2(3f, 1.5f), new Vector2(1f, 4f), new Vector2(-2.5f, 1f), new Vector2(-1.5f, -2.5f), new Vector2(1f, -1.5f), };

		for (int i = 0; i < 10; i++) {
			entityManager.add(entityTemplates.obstacle(vertices, 17f + i * 8f, MathUtils.random(0f, 15f), 0f));
			entityManager.add(entityTemplates.obstacle(vertices, 12f + i * 8f, MathUtils.random(0f, 15f), 90f));
		}

		for (int i = 0; i < 10; i++) {
			float x = MathUtils.random(10f, 90f);
			float y = MathUtils.random(2f, 13f);
			entityManager.add(entityTemplates.diamond(x, y, 0.2f));
		}

		camera = entityTemplates.camera(cameraData);
		entityManager.add(camera);

		Entity ship = entityTemplates.ship(5f, 7.5f, new Vector2(1f, 0f));
		entityManager.add(ship);

		Entity startPlanet = entityTemplates.startPlanet(5f, 7.5f, 1f);

		AttachmentComponent attachmentComponent = startPlanet.getComponent(AttachmentComponent.class);
		attachmentComponent.setEntity(ship);

		entityManager.add(startPlanet);
		entityManager.add(entityTemplates.destinationPlanet(95f, 7.5f, 1f));

		float worldWidth = 100f;
		float worldHeight = 20f;

		float x = worldWidth * 0.5f;
		float y = worldHeight * 0.5f;

		entityManager.add(entityTemplates.boxObstacle(x, 0f, worldWidth, 0.1f, 0f));
		entityManager.add(entityTemplates.boxObstacle(x, 15f, worldWidth, 0.1f, 0f));
		entityManager.add(entityTemplates.boxObstacle(0, y, 0.1f, worldHeight, 0f));
		entityManager.add(entityTemplates.boxObstacle(100f, y, 0.1f, worldHeight, 0f));

		Entity e = new Entity();
		e.addComponent(new GameDataComponent(ship, startPlanet, camera));
		e.addBehavior(new CreateDeadShipBehavior(entityManager, entityTemplates));
		e.addBehavior(new RemoveDeadShipBehavior(entityManager));
		e.addBehavior(new CreateNewShipBehavior(entityManager, entityTemplates));
		e.addBehavior(new FixCameraTargetBehavior());
		entityManager.add(e);
		
		realGame.init();
	}

	@Override
	public void render(int delta) {
		Gdx.graphics.getGL10().glClear(GL10.GL_COLOR_BUFFER_BIT);

		Camera cameraData = ComponentWrapper.getCamera(camera);

		libgdxCamera.move(cameraData.getX(), cameraData.getY());
		libgdxCamera.zoom(cameraData.getZoom());
		libgdxCamera.rotate(cameraData.getAngle());

		libgdxCamera.apply(spriteBatch);

		realGame.renderEntities(spriteBatch);

		box2dCustomDebugRenderer.render();
	}

	@Override
	public void update(int delta) {
		if (Gdx.input.isKeyPressed(Keys.ESCAPE) || Gdx.input.isKeyPressed(Keys.BACK)) 
			game.transition(game.getMainMenuScreen(), 500, 500);

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
		realGame.dispose();
	}
}