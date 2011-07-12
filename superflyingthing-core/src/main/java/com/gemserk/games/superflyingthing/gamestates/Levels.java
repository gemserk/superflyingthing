package com.gemserk.games.superflyingthing.gamestates;

import java.io.InputStream;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.gemserk.commons.svg.inkscape.DocumentParser;
import com.gemserk.commons.svg.inkscape.SvgInkscapePath;
import com.gemserk.games.superflyingthing.LayerProcessor;
import com.gemserk.games.superflyingthing.gamestates.Level.DestinationPlanet;
import com.gemserk.games.superflyingthing.gamestates.Level.Obstacle;
import com.gemserk.games.superflyingthing.gamestates.Level.StartPlanet;

public class Levels {

	private static final String[] levels = new String[] { // 
		"data/levels/level01.svg",  //
		"data/levels/level02.svg", //
		"data/levels/level03.svg", //
		"data/levels/level04.svg", //
		"data/levels/level03.svg", //
		"data/levels/level03.svg", //
		"data/levels/level03.svg", //
		"data/levels/level03.svg", //
		"data/levels/level03.svg", //
		"data/levels/level03.svg", //
		"data/levels/level03.svg", //
		};
	
	// TODO: cache levels...

	public static Level level(int levelNumber) {
		InputStream svg = Gdx.files.internal(levels[levelNumber]).read();
		Document document = new DocumentParser().parse(svg);

		final Level level = new Level();
		level.name = "name";

		new LayerProcessor("Obstacles") {
			protected void handleDocument(com.gemserk.commons.svg.inkscape.SvgDocument document, Element element) {
				level.w = document.getWidth();
				level.h = document.getHeight();
				level.name = element.getAttribute("levelName");
			};

			@Override
			protected void handlePathObject(SvgInkscapePath svgPath, Element element, Vector2[] vertices) {
				level.obstacles.add(new Obstacle(vertices));
			}
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

	public static boolean hasLevel(int level) {
		return level < levels.length;
	}

	public static int levelsCount() {
		return levels.length;
	}
}
