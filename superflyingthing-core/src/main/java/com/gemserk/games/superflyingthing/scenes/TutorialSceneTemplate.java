package com.gemserk.games.superflyingthing.scenes;

import com.artemis.Entity;
import com.artemis.World;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.gemserk.animation4j.gdx.Animation;
import com.gemserk.commons.artemis.WorldWrapper;
import com.gemserk.commons.artemis.components.AnimationComponent;
import com.gemserk.commons.artemis.components.CameraComponent;
import com.gemserk.commons.artemis.components.PhysicsComponent;
import com.gemserk.commons.artemis.components.PreviousStateCameraComponent;
import com.gemserk.commons.artemis.components.RenderableComponent;
import com.gemserk.commons.artemis.components.ScriptComponent;
import com.gemserk.commons.artemis.components.SpatialComponent;
import com.gemserk.commons.artemis.components.SpriteComponent;
import com.gemserk.commons.artemis.components.TagComponent;
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
import com.gemserk.commons.artemis.templates.EntityTemplateImpl;
import com.gemserk.commons.gdx.box2d.BodyBuilder;
import com.gemserk.commons.gdx.camera.Camera;
import com.gemserk.commons.gdx.camera.CameraImpl;
import com.gemserk.commons.gdx.camera.Libgdx2dCamera;
import com.gemserk.commons.gdx.camera.Libgdx2dCameraTransformImpl;
import com.gemserk.commons.gdx.games.PhysicsImpl;
import com.gemserk.commons.gdx.games.Spatial;
import com.gemserk.commons.gdx.games.SpatialImpl;
import com.gemserk.commons.gdx.games.SpatialPhysicsImpl;
import com.gemserk.commons.gdx.time.TimeStepProvider;
import com.gemserk.commons.reflection.ObjectConfigurator;
import com.gemserk.commons.reflection.Provider;
import com.gemserk.commons.reflection.ProviderImpl;
import com.gemserk.componentsengine.utils.ParametersWrapper;
import com.gemserk.games.superflyingthing.Layers;
import com.gemserk.games.superflyingthing.components.Components.GrabbableComponent;
import com.gemserk.games.superflyingthing.components.Components.TargetComponent;
import com.gemserk.games.superflyingthing.scripts.Behaviors.RemoveWhenGrabbedScript;
import com.gemserk.games.superflyingthing.scripts.CameraScript;
import com.gemserk.games.superflyingthing.scripts.Scripts.StarAnimationScript;
import com.gemserk.games.superflyingthing.scripts.Scripts.StarScript;
import com.gemserk.games.superflyingthing.scripts.UpdateCameraFromSpatialScript;
import com.gemserk.games.superflyingthing.scripts.UpdateLibgdxCameraScript;
import com.gemserk.games.superflyingthing.systems.ParticleEmitterSystem;
import com.gemserk.games.superflyingthing.systems.RenderLayerParticleEmitterImpl;
import com.gemserk.games.superflyingthing.systems.RenderLayerShapeImpl;
import com.gemserk.games.superflyingthing.templates.Groups;
import com.gemserk.resources.ResourceManager;

public class TutorialSceneTemplate extends SceneTemplateImpl {

	public static class StarTemplate extends EntityTemplateImpl {

		ResourceManager<String> resourceManager;
		BodyBuilder bodyBuilder;
		EventManager eventManager;

		@Override
		public void apply(Entity entity) {
			float radius = 0.3f;

			Animation rotateAnimation = resourceManager.getResourceValue("StarAnimation");
			Float x = parameters.get("x");
			Float y = parameters.get("y");

			Body body = bodyBuilder //
					.fixture(bodyBuilder.fixtureDefBuilder() //
							.sensor() //
							.restitution(0f) //
							.circleShape(radius)) //
					.mass(50f) //
					.position(x, y) //
					.type(BodyType.StaticBody) //
					.userData(entity) //
					.build();

			entity.addComponent(new PhysicsComponent(new PhysicsImpl(body)));
			entity.addComponent(new SpatialComponent(new SpatialPhysicsImpl(body, radius * 2, radius * 2)));

			entity.addComponent(new SpriteComponent(rotateAnimation.getCurrentFrame()));
			entity.addComponent(new RenderableComponent(3));
			entity.addComponent(new GrabbableComponent());
			entity.addComponent(new AnimationComponent(new Animation[] { rotateAnimation }));
			entity.addComponent(new ScriptComponent(new StarScript(eventManager), new RemoveWhenGrabbedScript(), new StarAnimationScript()));
		}

	}

	public static class CameraTemplate extends EntityTemplateImpl {

		EventManager eventManager;

		@Override
		public void apply(Entity entity) {

			Camera camera = parameters.get("camera");
			Libgdx2dCamera libgdxCamera = parameters.get("libgdxCamera");
			Spatial spatial = parameters.get("spatial");
			Entity target = parameters.get("target");

			entity.addComponent(new TagComponent(Groups.MainCamera));
			entity.addComponent(new TargetComponent(target));

			entity.addComponent(new CameraComponent(libgdxCamera, camera));
			entity.addComponent(new PreviousStateCameraComponent(new CameraImpl()));

			entity.addComponent(new SpatialComponent(spatial));
			entity.addComponent(new ScriptComponent(new CameraScript(eventManager), //
					new UpdateCameraFromSpatialScript(), new UpdateLibgdxCameraScript()));

		}
	};

	public static class StaticSpriteTemplate extends EntityTemplateImpl {

		ResourceManager<String> resourceManager;

		@Override
		public void apply(Entity entity) {
			Color color = parameters.get("color", Color.WHITE);
			Integer layer = parameters.get("layer");
			Spatial spatial = parameters.get("spatial");
			String spriteId = parameters.get("spriteId");
			Vector2 center = parameters.get("center");
			Sprite sprite = resourceManager.getResourceValue(spriteId);
			entity.addComponent(new SpatialComponent(spatial));
			entity.addComponent(new SpriteComponent(sprite, new Vector2(center), color));
			entity.addComponent(new RenderableComponent(layer));
		}

	};

	private final ResourceManager<String> resourceManager;

	public TutorialSceneTemplate(ResourceManager<String> resourceManager) {
		this.resourceManager = resourceManager;
	}

	public void apply(WorldWrapper worldWrapper) {
		int screenWidth = Gdx.graphics.getWidth();
		int screenHeight = Gdx.graphics.getHeight();

		TimeStepProvider timeStepProvider = getParameters().get("timeStepProvider");

		Libgdx2dCamera worldCamera = new Libgdx2dCameraTransformImpl(screenWidth * 0.5f, screenHeight * 0.5f);
		Libgdx2dCamera backgroundLayerCamera = new Libgdx2dCameraTransformImpl();

		Libgdx2dCamera secondBackgroundLayerCamera = new Libgdx2dCameraTransformImpl(screenWidth * 0.5f, screenHeight * 0.5f);

		RenderLayers renderLayers = new RenderLayers();

		renderLayers.add(Layers.FirstBackground, new RenderLayerSpriteBatchImpl(-10000, -500, backgroundLayerCamera), true);
		renderLayers.add(Layers.SecondBackground, new RenderLayerSpriteBatchImpl(-500, -100, secondBackgroundLayerCamera), true);
		renderLayers.add(Layers.StaticObstacles, new RenderLayerShapeImpl(-100, -50, worldCamera));
		renderLayers.add(Layers.World, new RenderLayerSpriteBatchImpl(-50, 100, worldCamera));
		renderLayers.add(Layers.Explosions, new RenderLayerParticleEmitterImpl(100, 200, worldCamera));

		World world = worldWrapper.getWorld();
		EntityFactory entityFactory = new EntityFactoryImpl(world);

		final com.badlogic.gdx.physics.box2d.World physicsWorld = new com.badlogic.gdx.physics.box2d.World(new Vector2(), false);

		final EventManager eventManager = new EventManagerImpl();

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

		Provider templateProvider = new ProviderImpl(new ObjectConfigurator() {
			{
				add("resourceManager", resourceManager);
				add("bodyBuilder", new BodyBuilder(physicsWorld));
				add("eventManager", eventManager);
			}
		});

		float cameraZoom = Gdx.graphics.getWidth() * 48f / 800f;
		Camera camera = new CameraImpl(0f, 0f, cameraZoom, 0f);

		EntityTemplate starTemplate = templateProvider.get(StarTemplate.class);
		EntityTemplate cameraTemplate = templateProvider.get(CameraTemplate.class);
		EntityTemplate staticSpriteTemplate = templateProvider.get(StaticSpriteTemplate.class);

		entityFactory.instantiate(staticSpriteTemplate, new ParametersWrapper() //
				.put("color", Color.WHITE) //
				.put("layer", (-999)) //
				.put("spatial", new SpatialImpl(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), 0)) //
				.put("center", new Vector2(0, 0)) //
				.put("spriteId", "BackgroundSprite") //
				);

		// for (int i = 0; i < 5; i++) {
		// entityFactory.instantiate(starTemplate, new ParametersWrapper() //
		// .put("x", MathUtils.random(-10f, 10f)) //
		// .put("y", MathUtils.random(-7f, 7f)) //
		// );
		// }

		entityFactory.instantiate(cameraTemplate, new ParametersWrapper() //
				.put("camera", camera) //
				.put("libgdxCamera", worldCamera) //
				.put("spatial", new SpatialImpl(0f, 0f, 1f, 1f, 0f))//
				);

	}

}
