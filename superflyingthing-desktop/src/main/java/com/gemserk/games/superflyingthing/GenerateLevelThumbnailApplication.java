package com.gemserk.games.superflyingthing;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artemis.World;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.dmurph.tracking.AnalyticsConfigData;
import com.dmurph.tracking.JGoogleAnalyticsTracker;
import com.dmurph.tracking.JGoogleAnalyticsTracker.GoogleAnalyticsVersion;
import com.gemserk.analytics.Analytics;
import com.gemserk.analytics.googleanalytics.DesktopAnalyticsAutoConfigurator;
import com.gemserk.commons.artemis.WorldWrapper;
import com.gemserk.commons.gdx.Game;
import com.gemserk.commons.gdx.GameStateImpl;
import com.gemserk.commons.gdx.ScreenImpl;
import com.gemserk.commons.gdx.camera.Camera;
import com.gemserk.commons.gdx.camera.CameraRestrictedImpl;
import com.gemserk.commons.gdx.camera.Libgdx2dCamera;
import com.gemserk.commons.gdx.camera.Libgdx2dCameraTransformImpl;
import com.gemserk.games.entities.EntityBuilder;
import com.gemserk.games.superflyingthing.gamestates.Level;
import com.gemserk.games.superflyingthing.gamestates.Level.Obstacle;
import com.gemserk.games.superflyingthing.gamestates.Levels;
import com.gemserk.games.superflyingthing.resources.GameResources;
import com.gemserk.games.superflyingthing.systems.ShapeRenderSystem;
import com.gemserk.resources.ResourceManager;
import com.gemserk.resources.ResourceManagerImpl;
import com.gemserk.util.ScreenshotSaver;

public class GenerateLevelThumbnailApplication {

	protected static final Logger logger = LoggerFactory.getLogger(GenerateLevelThumbnailApplication.class);

	public static void main(String[] argv) {
		AnalyticsConfigData analyticsConfig = new AnalyticsConfigData("UA-23542248-4");
		DesktopAnalyticsAutoConfigurator.populateFromSystem(analyticsConfig);
		Analytics.traker = new JGoogleAnalyticsTracker(analyticsConfig, GoogleAnalyticsVersion.V_4_7_2);

		String runningInDebug = System.getProperty("runningInDebug");
		if (runningInDebug != null) {
			logger.info("Running in debug mode, Analytics disabled");
			Analytics.traker.setEnabled(false);
		}

		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = 800;
		config.height = 480;
		config.fullscreen = false;
		config.title = "Super Flying Thing";
		config.useGL20 = false;
		config.useCPUSynch = false;
		config.forceExit = true;

		new LwjglApplication(new Game() {
			
			@Override
			public void create() {
				super.create();
				
				Gdx.graphics.getGL10().glClearColor(0, 0, 0, 1);
				
				setScreen(new ScreenImpl(new GameStateImpl() {

					private EntityTemplates templates;
					private WorldWrapper worldWrapper;
					private Libgdx2dCamera worldCamera;
					
					private boolean done = false;
					private Camera camera;
		
					@Override
					public void init() {
						
						worldCamera = new Libgdx2dCameraTransformImpl();
						worldCamera.center(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);
						worldCamera.zoom(18f);
						
						ResourceManager<String> resourceManager = new ResourceManagerImpl<String>();
						
						GameResources.load(resourceManager);
		
						World world = new World();
						
						worldWrapper = new WorldWrapper(world);
						worldWrapper.addRenderSystem(new ShapeRenderSystem(worldCamera));
		
						worldWrapper.init();
		
						com.badlogic.gdx.physics.box2d.World physicsWorld = new com.badlogic.gdx.physics.box2d.World(new Vector2(), false);
		
						templates = new EntityTemplates(physicsWorld, world, resourceManager, new EntityBuilder(world));
						
						
						Level level = Levels.level(8);
						
						for (int i = 0; i < level.obstacles.size(); i++) {
							Obstacle o = level.obstacles.get(i);
							if (o.bodyType == BodyType.StaticBody) 
								templates.obstacle(o.vertices, o.x, o.y, o.angle * MathUtils.degreesToRadians);
						}
						
						// worldCamera.move(level.w * 0.5f, level.h * 0.5f);
						
						camera = new CameraRestrictedImpl(0f, 0f, 1f, 0f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), new Rectangle(0f, 0f, level.w, 10000f));
						camera.setPosition(level.w * 0.5f, level.h * 0.5f);
						
						worldWrapper.update(1);
						worldWrapper.update(1);
					}
					
					@Override
					public void render(int delta) {
						Gdx.graphics.getGL10().glClear(GL10.GL_COLOR_BUFFER_BIT);
						worldWrapper.render();
						
						if (!done) {
							done = true;
							try {
								ScreenshotSaver.saveScreenshot("level01");
							} catch (IOException e) {
								
							}
						}
					}
					
					@Override
					public void update(int delta) {
						worldCamera.move(camera.getX(), camera.getY());
						worldCamera.zoom(camera.getZoom());
						worldWrapper.update(delta);
					}
					
					
				}));
				
			}


		}, config);
	}

}
