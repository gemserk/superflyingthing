package com.gemserk.games.superflyingthing.gamestates;

import java.util.ArrayList;

import com.artemis.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.World;
import com.gemserk.animation4j.transitions.sync.Synchronizers;
import com.gemserk.commons.artemis.WorldWrapper;
import com.gemserk.commons.artemis.components.ScriptComponent;
import com.gemserk.commons.artemis.events.EventManager;
import com.gemserk.commons.artemis.events.EventManagerImpl;
import com.gemserk.commons.artemis.systems.PhysicsSystem;
import com.gemserk.commons.artemis.systems.RenderLayer;
import com.gemserk.commons.artemis.systems.ScriptSystem;
import com.gemserk.commons.artemis.systems.SpriteRendererSystem;
import com.gemserk.commons.artemis.systems.SpriteUpdateSystem;
import com.gemserk.commons.gdx.GameStateImpl;
import com.gemserk.commons.gdx.box2d.Box2DCustomDebugRenderer;
import com.gemserk.commons.gdx.box2d.JointBuilder;
import com.gemserk.commons.gdx.camera.Camera;
import com.gemserk.commons.gdx.camera.CameraRestrictedImpl;
import com.gemserk.commons.gdx.camera.Libgdx2dCamera;
import com.gemserk.commons.gdx.camera.Libgdx2dCameraTransformImpl;
import com.gemserk.games.entities.EntityBuilder;
import com.gemserk.games.superflyingthing.Components.GameData;
import com.gemserk.games.superflyingthing.Components.GameDataComponent;
import com.gemserk.games.superflyingthing.EntityTemplates;
import com.gemserk.games.superflyingthing.Game;
import com.gemserk.games.superflyingthing.Scripts;
import com.gemserk.games.superflyingthing.Scripts.CameraScript;
import com.gemserk.games.superflyingthing.Scripts.StarScript;
import com.gemserk.games.superflyingthing.Scripts.StartPlanetScript;
import com.gemserk.games.superflyingthing.Scripts.UpdateControllerScript;
import com.gemserk.games.superflyingthing.ShipController;
import com.gemserk.games.superflyingthing.ShipControllerImpl;
import com.gemserk.games.superflyingthing.Trigger;
import com.gemserk.games.superflyingthing.gamestates.Level.Obstacle;
import com.gemserk.games.superflyingthing.systems.ParticleEmitterSystem;
import com.gemserk.games.superflyingthing.systems.ShapeRenderSystem;
import com.gemserk.resources.ResourceManager;

public class BackgroundGameState extends GameStateImpl {

	SpriteBatch spriteBatch;
	Libgdx2dCamera worldCamera;

	EntityTemplates entityTemplates;
	World physicsWorld;
	Box2DCustomDebugRenderer box2dCustomDebugRenderer;
	ResourceManager<String> resourceManager;
	boolean done;

	EntityBuilder entityBuilder;
	private com.artemis.World world;
	private WorldWrapper worldWrapper;

	GameData gameData;

	private EventManager eventManager;
	private JointBuilder jointBuilder;

	public void setResourceManager(ResourceManager<String> resourceManager) {
		this.resourceManager = resourceManager;
	}

	@Override
	public void init() {
		spriteBatch = new SpriteBatch();

		eventManager = new EventManagerImpl();
		physicsWorld = new World(new Vector2(), false);

		jointBuilder = new JointBuilder(physicsWorld);

		worldCamera = new Libgdx2dCameraTransformImpl();
		worldCamera.center(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);

		Libgdx2dCamera backgroundLayerCamera = new Libgdx2dCameraTransformImpl();

		ArrayList<RenderLayer> renderLayers = new ArrayList<RenderLayer>();

		renderLayers.add(new RenderLayer(-1000, -100, backgroundLayerCamera));
		renderLayers.add(new RenderLayer(-100, 100, worldCamera));

		world = new com.artemis.World();
		worldWrapper = new WorldWrapper(world);
		// add render and all stuff...
		GameInformation.worldWrapper = worldWrapper;

		worldWrapper.addUpdateSystem(new PhysicsSystem(physicsWorld));
		worldWrapper.addUpdateSystem(new ScriptSystem());

		worldWrapper.addRenderSystem(new SpriteUpdateSystem());
		worldWrapper.addRenderSystem(new SpriteRendererSystem(renderLayers));

		worldWrapper.addRenderSystem(new ShapeRenderSystem(worldCamera));

		worldWrapper.addRenderSystem(new ParticleEmitterSystem(worldCamera));

		worldWrapper.init();

		entityBuilder = new EntityBuilder(world);

		box2dCustomDebugRenderer = new Box2DCustomDebugRenderer((Libgdx2dCameraTransformImpl) worldCamera, physicsWorld);

		entityTemplates = new EntityTemplates(physicsWorld, world, resourceManager, entityBuilder);

		gameData = new GameData();

		Sprite backgroundSprite = resourceManager.getResourceValue("BackgroundSprite");
		entityTemplates.staticSprite(backgroundSprite, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), 0, -999, 0, 0, Color.WHITE);

		loadLevel(entityTemplates, Levels.level(MathUtils.random(0, Levels.levelsCount() - 1)));

		worldWrapper.update(1);
		worldWrapper.update(1);
	}

	private void createWorldLimits(float worldWidth, float worldHeight) {
		createWorldLimits(worldWidth, worldHeight, 0.2f);
	}

	private void createWorldLimits(float worldWidth, float worldHeight, float offset) {
		float centerX = worldWidth * 0.5f;
		float centerY = worldHeight * 0.5f;
		float limitWidth = 0.1f;
		entityTemplates.boxObstacle(centerX, -offset, worldWidth, limitWidth, 0f);
		entityTemplates.boxObstacle(centerX, worldHeight + offset, worldWidth, limitWidth, 0f);
		entityTemplates.boxObstacle(-offset, centerY, limitWidth, worldHeight, 0f);
		entityTemplates.boxObstacle(worldWidth + offset, centerY, limitWidth, worldHeight, 0f);
	}

	void loadLevel(EntityTemplates templates, Level level) {
		float worldWidth = level.w;
		float worldHeight = level.h;

		float cameraZoom = Gdx.graphics.getWidth() * 24f / 800f;

		final Camera camera = new CameraRestrictedImpl(0f, 0f, cameraZoom, 0f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), new Rectangle(0f, 0f, worldWidth, worldHeight));

		final ShipController controller = new ShipControllerImpl(worldCamera);
		controller.setEnabled(false);

		Entity startPlanet = entityTemplates.startPlanet(level.startPlanet.x, level.startPlanet.y, 1f, controller, new StartPlanetScript(physicsWorld, jointBuilder, eventManager));

		entityTemplates.destinationPlanet(level.destinationPlanet.x, level.destinationPlanet.y, 1f, new Trigger() {
			@Override
			protected void onTrigger(Entity e) {
				triggered();
			}
		});

		Entity cameraEntity = entityTemplates.camera(camera, worldCamera, level.startPlanet.x, level.startPlanet.y, new CameraScript(eventManager));

		for (int i = 0; i < level.obstacles.size(); i++) {
			Obstacle o = level.obstacles.get(i);
			if (o.bodyType == BodyType.StaticBody)
				entityTemplates.obstacle(o.vertices, o.x, o.y, o.angle * MathUtils.degreesToRadians);
			else {
				entityTemplates.movingObstacle(o.vertices, o.path, o.x, o.y, o.angle * MathUtils.degreesToRadians);
			}
		}

		for (int i = 0; i < level.items.size(); i++) {
			Level.Item item = level.items.get(i);
			entityTemplates.star(item.x, item.y, 0.2f, new Trigger() {
				@Override
				protected void onTrigger(Entity e) {
					gameData.currentItems++;
				}
			}, new StarScript(eventManager));
		}

		gameData.totalItems = level.items.size();

		createWorldLimits(worldWidth, worldHeight);

		entityBuilder //
				.component(new ScriptComponent(new UpdateControllerScript(controller))) //
				.build();

		entityBuilder //
				.component(new GameDataComponent(null, startPlanet, cameraEntity)) //
				.component(new ScriptComponent(new Scripts.GameScript(eventManager, controller, entityTemplates, gameData, true))) //
				.build();

	}

	@Override
	public void render(int delta) {
		Gdx.graphics.getGL10().glClear(GL10.GL_COLOR_BUFFER_BIT);

		worldWrapper.render();

		if (Game.isShowBox2dDebug())
			box2dCustomDebugRenderer.render();
	}

	@Override
	public void update(int delta) {
		Synchronizers.synchronize(delta);
		worldWrapper.update(delta);
	}

	@Override
	public void dispose() {
		spriteBatch.dispose();
		physicsWorld.dispose();
	}

}