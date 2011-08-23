package com.gemserk.games.superflyingthing.levels;

import java.io.InputStream;
import java.util.HashMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.gemserk.commons.gdx.games.SpatialImpl;
import com.gemserk.commons.svg.inkscape.DocumentParser;
import com.gemserk.commons.svg.inkscape.SvgInkscapePath;
import com.gemserk.componentsengine.utils.ParametersWrapper;
import com.gemserk.games.superflyingthing.Colors;
import com.gemserk.games.superflyingthing.levels.Level.DestinationPlanet;
import com.gemserk.games.superflyingthing.levels.Level.Item;
import com.gemserk.games.superflyingthing.levels.Level.Obstacle;
import com.gemserk.games.superflyingthing.levels.Level.StartPlanet;

public class Levels {

	private static final String[] levels = new String[] { //
	"data/levels/level01.svg", //
			"data/levels/level02.svg", //
			"data/levels/level03.svg", //
			"data/levels/level04.svg", //
			"data/levels/level05.svg", //
			"data/levels/level06.svg", //
			"data/levels/level07.svg", //
			"data/levels/level08.svg", //
			"data/levels/level09.svg", //
			"data/levels/level10.svg", //
			"data/levels/level11.svg", //
			"data/levels/level12.svg", //
			"data/levels/level13.svg", //
			"data/levels/level14.svg", //
			"data/levels/level15.svg", //
			"data/levels/level16.svg", //
			"data/levels/level17.svg", //
			"data/levels/level18.svg", //
	};

	private static Level[] cachedLevels = new Level[levels.length];

	public static Level level(int levelNumber) {

		if (cachedLevels[levelNumber] != null) {
			Gdx.app.log("SuperFlyingThing", "Loading level " + levelNumber + " from cache...");
			return cachedLevels[levelNumber];
		}

		Gdx.app.log("SuperFlyingThing", "Loading level " + levelNumber + " from file...");

		InputStream svg = Gdx.files.internal(levels[levelNumber]).read();
		Document document = new DocumentParser().parse(svg);

		final Level level = new Level();
		level.name = "name";

		final HashMap<String, Vector2[]> registeredPaths = new HashMap<String, Vector2[]>();

		new SvgLayerProcessor("Paths") {

			protected void handleDocument(com.gemserk.commons.svg.inkscape.SvgDocument document, Element element) {
				level.w = document.getWidth();
				level.h = document.getHeight();
				level.name = element.getAttribute("levelName");

				String zoom = element.getAttribute("zoom");
				if (!"".equals(zoom)) {
					level.zoom = Float.parseFloat(zoom);
				}

			};

			@Override
			protected void handlePathObject(SvgInkscapePath svgPath, Element element, Vector2[] vertices) {
				registeredPaths.put(svgPath.getId(), vertices);
			}
		}.process(document);

		new SvgLayerProcessor("Obstacles") {
			@Override
			protected void handlePathObject(SvgInkscapePath svgPath, Element element, Vector2[] vertices) {
				Obstacle obstacle = new Obstacle(vertices);

				obstacle.id = svgPath.getId();
				
				Vector2 center = new Vector2();
				calculateCenter(center, vertices);
				obstacle.x = center.x;
				obstacle.y = center.y;
				centerVertices(center, vertices);

				String bodyType = element.getAttribute("bodyType");
				if ("DynamicBody".equals(bodyType)) {
					obstacle.bodyType = BodyType.DynamicBody;

					String movementPath = element.getAttribute("movementPath");
					if (registeredPaths.containsKey(movementPath)) {
						obstacle.path = registeredPaths.get(movementPath);
					}

					String startPoint = element.getAttribute("startPoint");
					if (!"".equals(startPoint)) {
						obstacle.startPoint = Integer.parseInt(startPoint);
					}
				}

				level.obstacles.add(obstacle);
			}
		}.process(document);

		new SvgLayerProcessor("Items") {
			protected void handleImageObject(com.gemserk.commons.svg.inkscape.SvgInkscapeImage svgImage, Element element, float x, float y, float width, float height, float sx, float sy, float angle) {
				Item i = new Item();
				i.x = x;
				i.y = y;
				i.id = svgImage.getId();
				level.items.add(i);
			};
		}.process(document);

		new SvgLayerProcessor("Lasers") {
			protected void handleImageObject(com.gemserk.commons.svg.inkscape.SvgInkscapeImage svgImage, Element element, float x, float y, float width, float height, float sx, float sy, float angle) {
				int fireRate = 2000;
				int bulletDuration = 1000;
				int currentReloadTime = 0;

				// NamedNodeMap attributes = element.getAttributes();
				// Map<String, Object> properties = new HashMap<String, Object>(attributes.getLength());
				// for (int i = 0; i < attributes.getLength(); i++)
				// properties.put(attributes.item(i).getNodeName(), attributes.item(i).getNodeValue());
				// System.out.println(properties);

				String fireRateValue = element.getAttribute("fireRate");
				if (!"".equals(fireRateValue))
					fireRate = Integer.parseInt(fireRateValue);

				String bulletDurationValue = element.getAttribute("bulletDuration");
				if (!"".equals(bulletDurationValue))
					bulletDuration = Integer.parseInt(bulletDurationValue);

				String currentReloadTimeValue = element.getAttribute("currentReloadTime");
				if (!"".equals(currentReloadTimeValue))
					currentReloadTime = Integer.parseInt(currentReloadTimeValue);

				level.laserTurrets.add(new Level.LaserTurret(x, y, angle, fireRate, bulletDuration, currentReloadTime));
			};
		}.process(document);

		new SvgLayerProcessor("Portals") {
			protected void handleImageObject(com.gemserk.commons.svg.inkscape.SvgInkscapeImage svgImage, Element element, float x, float y, float width, float height, float sx, float sy, float angle) {

				String id = svgImage.getId();
				String targetPortalId = element.getAttribute("targetPortalId");

				level.portals.add(new Level.Portal(x, y, width, height, angle, id, targetPortalId));

			};
		}.process(document);

		new SvgLayerProcessor("World") {
			protected void handleImageObject(com.gemserk.commons.svg.inkscape.SvgInkscapeImage svgImage, Element element, float x, float y, float width, float height, float sx, float sy, float angle) {
				if (element.hasAttribute("startPlanet")) {
					level.startPlanet = new StartPlanet(x, y);
				} else if (element.hasAttribute("destinationPlanet")) {
					level.destinationPlanets.add(new DestinationPlanet(x, y));
				}
			};
		}.process(document);

		// generate random clouds once and for ever....

		generateRandomClouds(level, 4);

		cachedLevels[levelNumber] = level;

		return level;
	}

	public static void calculateCenter(Vector2 center, Vector2[] vertices) {
		center.set(0, 0);
		for (int i = 0; i < vertices.length; i++) {
			center.x += vertices[i].x;
			center.y += vertices[i].y;
		}
		center.x /= vertices.length;
		center.y /= vertices.length;
	}

	public static void centerVertices(Vector2 center, Vector2[] vertices) {
		for (int i = 0; i < vertices.length; i++) {
			vertices[i].x -= center.x;
			vertices[i].y -= center.y;
		}
	}

	public static boolean hasLevel(int level) {
		return level < levels.length;
	}

	public static int levelsCount() {
		return levels.length;
	}

	public static void generateRandomClouds(Level level, int count) {

		Color[] colors = new Color[] { Colors.yellow, Color.RED, Color.GREEN, Color.BLUE, Color.BLACK, Colors.magenta };

		for (int i = 0; i < count; i++) {

			float x = MathUtils.random(0, level.w);
			float y = MathUtils.random(0, level.h);

			float w = MathUtils.random(50, 80f);
			float h = w;

			float angle = MathUtils.random(0, 359f);

			Color color = new Color(colors[MathUtils.random(0, colors.length - 1)]);
			color.a = 0.5f;

			level.fogClouds.add(new ParametersWrapper() //
					.put("color", color) //
					.put("layer", -200) //
					.put("spatial", new SpatialImpl(x, y, w, h, angle)) //
					.put("spriteId", "FogSprite") //
					.put("center", new Vector2(0.5f, 0.5f)));
			
		}
	}
}
