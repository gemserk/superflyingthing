package com.gemserk.games.superflyingthing.gamestates;

import java.io.InputStream;
import java.util.HashMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.gemserk.commons.svg.inkscape.DocumentParser;
import com.gemserk.commons.svg.inkscape.SvgInkscapePath;
import com.gemserk.games.superflyingthing.LayerProcessor;
import com.gemserk.games.superflyingthing.gamestates.Level.DestinationPlanet;
import com.gemserk.games.superflyingthing.gamestates.Level.Item;
import com.gemserk.games.superflyingthing.gamestates.Level.Obstacle;
import com.gemserk.games.superflyingthing.gamestates.Level.StartPlanet;

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
	};

	// TODO: cache levels...

	public static Level level(int levelNumber) {
		InputStream svg = Gdx.files.internal(levels[levelNumber]).read();
		Document document = new DocumentParser().parse(svg);

		final Level level = new Level();
		level.name = "name";

		final HashMap<String, Vector2[]> registeredPaths = new HashMap<String, Vector2[]>();

		new LayerProcessor("Paths") {

			protected void handleDocument(com.gemserk.commons.svg.inkscape.SvgDocument document, Element element) {
				level.w = document.getWidth();
				level.h = document.getHeight();
				level.name = element.getAttribute("levelName");
			};

			@Override
			protected void handlePathObject(SvgInkscapePath svgPath, Element element, Vector2[] vertices) {
				registeredPaths.put(svgPath.getId(), vertices);
			}
		}.process(document);

		new LayerProcessor("Obstacles") {
			@Override
			protected void handlePathObject(SvgInkscapePath svgPath, Element element, Vector2[] vertices) {
				Obstacle obstacle = new Obstacle(vertices);

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
				}

				level.obstacles.add(obstacle);
			}
		}.process(document);

		new LayerProcessor("Items") {
			protected void handleImageObject(com.gemserk.commons.svg.inkscape.SvgInkscapeImage svgImage, Element element, float x, float y, float width, float height, float sx, float sy, float angle) {
				Item i = new Item();
				i.x = x;
				i.y = y;
				level.items.add(i);
			};
		}.process(document);

		new LayerProcessor("World") {
			protected void handleImageObject(com.gemserk.commons.svg.inkscape.SvgInkscapeImage svgImage, Element element, float x, float y, float width, float height, float sx, float sy, float angle) {
				if (element.hasAttribute("startPlanet")) {
					level.startPlanet = new StartPlanet(x, y);
				} else if (element.hasAttribute("destinationPlanet")) {
					level.destinationPlanet = new DestinationPlanet(x, y);
				}
			};
		}.process(document);

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
}
