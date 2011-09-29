package com.gemserk.games.superflyingthing.scenes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.gemserk.commons.artemis.EntityBuilder;
import com.gemserk.commons.artemis.WorldWrapper;
import com.gemserk.commons.artemis.events.EventManager;
import com.gemserk.commons.artemis.events.EventManagerImpl;
import com.gemserk.commons.artemis.render.RenderLayers;
import com.gemserk.commons.artemis.systems.CameraUpdateSystem;
import com.gemserk.commons.artemis.systems.ContainerSystem;
import com.gemserk.commons.artemis.systems.OwnerSystem;
import com.gemserk.commons.artemis.systems.PhysicsSystem;
import com.gemserk.commons.artemis.systems.PreviousStateSpatialSystem;
import com.gemserk.commons.artemis.systems.ReflectionRegistratorEventSystem;
import com.gemserk.commons.artemis.systems.RenderLayerSpriteBatchImpl;
import com.gemserk.commons.artemis.systems.RenderableSystem;
import com.gemserk.commons.artemis.systems.ScriptSystem;
import com.gemserk.commons.artemis.systems.SpriteUpdateSystem;
import com.gemserk.commons.artemis.systems.TagSystem;
import com.gemserk.commons.artemis.templates.EntityFactory;
import com.gemserk.commons.artemis.templates.EntityFactoryImpl;
import com.gemserk.commons.artemis.templates.EntityTemplate;
import com.gemserk.commons.gdx.box2d.BodyBuilder;
import com.gemserk.commons.gdx.box2d.JointBuilder;
import com.gemserk.commons.gdx.camera.CameraImpl;
import com.gemserk.commons.gdx.camera.Libgdx2dCamera;
import com.gemserk.commons.gdx.camera.Libgdx2dCameraTransformImpl;
import com.gemserk.commons.gdx.games.SpatialImpl;
import com.gemserk.commons.gdx.graphics.Mesh2dBuilder;
import com.gemserk.commons.gdx.time.TimeStepProvider;
import com.gemserk.commons.reflection.Injector;
import com.gemserk.componentsengine.utils.ParametersWrapper;
import com.gemserk.games.superflyingthing.Layers;
import com.gemserk.games.superflyingthing.gamestates.GameInformation;
import com.gemserk.games.superflyingthing.gamestates.LevelLoader;
import com.gemserk.games.superflyingthing.levels.Level;
import com.gemserk.games.superflyingthing.systems.ParticleEmitterSystem;
import com.gemserk.games.superflyingthing.systems.RenderLayerParticleEmitterImpl;
import com.gemserk.games.superflyingthing.systems.RenderLayerShapeImpl;
import com.gemserk.games.superflyingthing.templates.EntityTemplates;
import com.gemserk.games.superflyingthing.templates.EventManagerTemplate;
import com.gemserk.games.superflyingthing.templates.NormalModeGameLogicTemplate;

public class NormalModeSceneTemplate extends SceneTemplateImpl {

	TimeStepProvider timeStepProvider;
	Injector injector;

	public void apply(WorldWrapper worldWrapper) {
		
		Boolean backgroundEnabled = getParameters().get("backgroundEnabled", false);
		Boolean shouldRemoveItems = getParameters().get("shouldRemoveItems", true);
		Level level = getParameters().get("level");
		
		EventManager eventManager = new EventManagerImpl();

		World physicsWorld = new World(new Vector2(), false);

		Libgdx2dCamera worldCamera = new Libgdx2dCameraTransformImpl(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);

		Libgdx2dCamera backgroundLayerCamera = new Libgdx2dCameraTransformImpl();
		Libgdx2dCamera secondBackgroundLayerCamera = new Libgdx2dCameraTransformImpl(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);

		RenderLayers renderLayers = new RenderLayers();

		renderLayers.add(Layers.FirstBackground, new RenderLayerSpriteBatchImpl(-10000, -500, backgroundLayerCamera), backgroundEnabled);
		renderLayers.add(Layers.SecondBackground, new RenderLayerSpriteBatchImpl(-500, -100, secondBackgroundLayerCamera), backgroundEnabled);
		renderLayers.add(Layers.StaticObstacles, new RenderLayerShapeImpl(-100, -50, worldCamera));
		renderLayers.add(Layers.World, new RenderLayerSpriteBatchImpl(-50, 100, worldCamera));
		renderLayers.add(Layers.Explosions, new RenderLayerParticleEmitterImpl(100, 200, worldCamera));

		com.artemis.World world = worldWrapper.getWorld();
		EntityFactory entityFactory = new EntityFactoryImpl(world);
		parameters = new ParametersWrapper();
		// add render and all stuff...

		worldWrapper.addUpdateSystem(new PreviousStateSpatialSystem());
		worldWrapper.addUpdateSystem(new PhysicsSystem(physicsWorld));
		worldWrapper.addUpdateSystem(new ScriptSystem());
		worldWrapper.addUpdateSystem(new TagSystem());
		worldWrapper.addUpdateSystem(new ContainerSystem());
		worldWrapper.addUpdateSystem(new OwnerSystem());

		// testing event listener auto registration using reflection
		worldWrapper.addUpdateSystem(new ReflectionRegistratorEventSystem(eventManager));

		worldWrapper.addRenderSystem(new CameraUpdateSystem(timeStepProvider));
		worldWrapper.addRenderSystem(new SpriteUpdateSystem(timeStepProvider));
		worldWrapper.addRenderSystem(new RenderableSystem(renderLayers));
		worldWrapper.addRenderSystem(new ParticleEmitterSystem());

		worldWrapper.init();

		injector.configureField("physicsWorld", physicsWorld);
		injector.configureField("entityBuilder", new EntityBuilder(world));
		injector.configureField("entityFactory", new EntityFactoryImpl(world));
		injector.configureField("eventManager", eventManager);
		injector.configureField("bodyBuilder", new BodyBuilder(physicsWorld));
		injector.configureField("mesh2dBuilder", new Mesh2dBuilder());
		injector.configureField("jointBuilder", new JointBuilder(physicsWorld));

		EntityTemplates entityTemplates = new EntityTemplates(injector);

		entityFactory.instantiate(entityTemplates.staticSpriteTemplate, parameters //
				.put("color", Color.WHITE) //
				.put("layer", -999) //
				.put("spatial", new SpatialImpl(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), 0)) //
				.put("center", new Vector2(0, 0)) //
				.put("spriteId", "BackgroundSprite") //
				);

		EntityTemplate eventManagerTemplate = injector.getInstance(EventManagerTemplate.class);
		EntityTemplate normalModeGameLogicTemplate = injector.getInstance(NormalModeGameLogicTemplate.class);

		entityFactory.instantiate(eventManagerTemplate);

		entityFactory.instantiate(normalModeGameLogicTemplate, new ParametersWrapper() //
				.put("invulnerable", GameInformation.gameMode == GameInformation.PracticeGameMode) //
				);

		entityFactory.instantiate(entityTemplates.replayRecorderTemplate);

		entityFactory.instantiate(entityTemplates.secondCameraTemplate, new ParametersWrapper() //
				.put("camera", new CameraImpl()) //
				.put("libgdx2dCamera", secondBackgroundLayerCamera)//
				);

		// creates a new particle emitter spawner template which creates a new explosion when the ship dies.
		entityFactory.instantiate(entityTemplates.particleEmitterSpawnerTemplate);
		
		new LevelLoader(entityTemplates, entityFactory, physicsWorld, worldCamera, shouldRemoveItems).loadLevel(level);

	}

}
