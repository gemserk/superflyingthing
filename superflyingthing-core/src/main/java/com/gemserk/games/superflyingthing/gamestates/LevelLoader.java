package com.gemserk.games.superflyingthing.gamestates;

import com.artemis.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.gemserk.commons.artemis.components.TagComponent;
import com.gemserk.commons.artemis.templates.EntityFactory;
import com.gemserk.commons.artemis.templates.EntityTemplateImpl;
import com.gemserk.commons.gdx.camera.Camera;
import com.gemserk.commons.gdx.camera.CameraRestrictedImpl;
import com.gemserk.commons.gdx.camera.Libgdx2dCamera;
import com.gemserk.commons.gdx.games.SpatialImpl;
import com.gemserk.componentsengine.utils.Parameters;
import com.gemserk.componentsengine.utils.ParametersWrapper;
import com.gemserk.games.superflyingthing.ShipController;
import com.gemserk.games.superflyingthing.components.Components.ControllerComponent;
import com.gemserk.games.superflyingthing.levels.Level;
import com.gemserk.games.superflyingthing.levels.Level.DestinationPlanet;
import com.gemserk.games.superflyingthing.levels.Level.LaserTurret;
import com.gemserk.games.superflyingthing.levels.Level.Obstacle;
import com.gemserk.games.superflyingthing.levels.Level.Portal;
import com.gemserk.games.superflyingthing.templates.EntityTemplates;
import com.gemserk.games.superflyingthing.templates.Groups;

public class LevelLoader {

	private final Parameters parameters = new ParametersWrapper();
	private final EntityTemplates entityTemplates;
	private final World physicsWorld;
	private final EntityFactory entityFactory;
	private final Libgdx2dCamera worldCamera;

	// used to check if items are inside obstacles to remove them from the level.
	private boolean insideObstacle;

	public LevelLoader(EntityTemplates entityTemplates, EntityFactory entityFactory, World physicsWorld, Libgdx2dCamera worldCamera) {
		this.entityTemplates = entityTemplates;
		this.entityFactory = entityFactory;
		this.physicsWorld = physicsWorld;
		this.worldCamera = worldCamera;
	}

	private void createWorldLimits(float worldWidth, float worldHeight) {
		createWorldLimits(worldWidth, worldHeight, 0.2f);
	}

	private void createWorldLimits(float worldWidth, float worldHeight, float offset) {
		float centerX = worldWidth * 0.5f;
		float centerY = worldHeight * 0.5f;
		float limitWidth = 0.1f;
		entityTemplates.boxObstacle(centerX, -offset, worldWidth + 1, limitWidth, 0f);
		entityTemplates.boxObstacle(centerX, worldHeight + offset, worldWidth + 1, limitWidth, 0f);
		entityTemplates.boxObstacle(-offset, centerY, limitWidth, worldHeight + 1, 0f);
		entityTemplates.boxObstacle(worldWidth + offset, centerY, limitWidth, worldHeight + 1, 0f);
	}

	public void loadLevel(Level level) {
		float worldWidth = level.w;
		float worldHeight = level.h;

		float cameraZoom = Gdx.graphics.getWidth() * level.zoom / 800f;

		final Camera camera = new CameraRestrictedImpl(0f, 0f, cameraZoom, 0f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), new Rectangle(0f, 0f, worldWidth, worldHeight));

		final ShipController controller = new ShipController();

		entityTemplates.startPlanet(level.startPlanet.x, level.startPlanet.y, 1f, controller);

		for (int i = 0; i < level.destinationPlanets.size(); i++) {
			DestinationPlanet destinationPlanet = level.destinationPlanets.get(i);
			entityTemplates.destinationPlanet(destinationPlanet.x, destinationPlanet.y, 1f);
		}

		parameters.clear();
		entityFactory.instantiate(entityTemplates.getCameraTemplate(), parameters //
				.put("camera", camera) //
				.put("libgdxCamera", worldCamera) //
				.put("spatial", new SpatialImpl(level.startPlanet.x, level.startPlanet.y, 1f, 1f, 0f)) //
				);

		for (int i = 0; i < level.obstacles.size(); i++) {
			Obstacle o = level.obstacles.get(i);
			if (o.bodyType == BodyType.StaticBody)
				entityTemplates.obstacle(o.vertices, o.x, o.y, o.angle * MathUtils.degreesToRadians);
			else {
				entityTemplates.movingObstacle(o.vertices, o.path, o.startPoint, o.x, o.y, o.angle * MathUtils.degreesToRadians);
			}
		}

		int j = 0;
		while (j < level.items.size()) {
			Level.Item item = level.items.get(j);

			float x = item.x;
			float y = item.y;
			float w = 0.3f;
			float h = 0.3f;

			insideObstacle = false;

			physicsWorld.QueryAABB(new QueryCallback() {
				@Override
				public boolean reportFixture(Fixture fixture) {

					System.out.println(fixture.getBody().getType());

					insideObstacle = true;
					return false;
				}
			}, x - w * 0.5f, y - h * 0.5f, x + w * 0.5f, y + h * 0.5f);

			if (insideObstacle) {
				Gdx.app.log("SuperFlyingThing", "Removing item " + item.id + " because is colliding with obstacle");
				level.items.remove(j);
				continue;
			}

			entityTemplates.star(item.id, item.x, item.y);

			j++;
		}

		for (int i = 0; i < level.laserTurrets.size(); i++) {
			LaserTurret laserTurret = level.laserTurrets.get(i);

			parameters.clear();

			entityFactory.instantiate(entityTemplates.getLaserGunTemplate(), parameters //
					.put("position", new Vector2(laserTurret.x, laserTurret.y)) //
					.put("angle", laserTurret.angle) //
					.put("fireRate", laserTurret.fireRate) //
					.put("bulletDuration", laserTurret.bulletDuration) //
					.put("currentReloadTime", laserTurret.currentReloadTime) //
					);
		}

		for (int i = 0; i < level.portals.size(); i++) {
			Portal portal = level.portals.get(i);

			parameters.clear();

			entityFactory.instantiate(entityTemplates.getPortalTemplate(), parameters //
					.put("id", portal.id) //
					.put("targetPortalId", portal.targetPortalId) //
					.put("spatial", new SpatialImpl(portal.x, portal.y, portal.w, portal.h, portal.angle)) //
					);
		}

		for (int i = 0; i < level.fogClouds.size(); i++)
			entityFactory.instantiate(entityTemplates.getStaticSpriteTemplate(), level.fogClouds.get(i));

		createWorldLimits(worldWidth, worldHeight);

		// default Player controller (with no script)

		entityFactory.instantiate(new EntityTemplateImpl() {
			@Override
			public void apply(Entity entity) {
				entity.addComponent(new TagComponent(Groups.PlayerController));
				entity.addComponent(new ControllerComponent(controller));
			}
		});

	}

}