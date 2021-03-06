package com.gemserk.games.superflyingthing.levels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Stack;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.badlogic.gdx.math.Vector2;
import com.gemserk.commons.svg.inkscape.SvgDocument;
import com.gemserk.commons.svg.inkscape.SvgDocumentHandler;
import com.gemserk.commons.svg.inkscape.SvgInkscapeGroup;
import com.gemserk.commons.svg.inkscape.SvgInkscapeGroupHandler;
import com.gemserk.commons.svg.inkscape.SvgInkscapePathHandler;
import com.gemserk.commons.svg.inkscape.SvgInkscapeUtils;
import com.gemserk.commons.svg.inkscape.SvgParser;
import com.gemserk.commons.svg.inkscape.SvgPath;
import com.gemserk.commons.vecmath.VecmathUtils;
import com.gemserk.games.superflyingthing.Shape;
import com.gemserk.vecmath.Matrix3f;
import com.gemserk.vecmath.Vector2f;
import com.gemserk.vecmath.Vector3f;

public class RandomLevelTileBasedGenerator {

	private static class TileLink {

		private boolean nullLink;
		private String tileType;

		public TileLink() {
			nullLink = true;
		}

		public TileLink(String tileType) {
			if ("".equals(tileType) || "0".equals(tileType))
				nullLink = true;
			else {
				nullLink = false;
				this.tileType = tileType;
			}
		}

		public boolean isNull() {
			return nullLink;
		}

		public String getTileType() {
			return tileType;
		}

		public boolean match(String tileType) {
			if (nullLink)
				return false;
			return this.tileType.equals(tileType);
		}

	}

	private static class Generator {

		private final Random random = new Random();

		Map<String, Tile> tileMap;

		public ArrayList<String> generate(int depth) {
			ArrayList<String> generatedTiles = new ArrayList<String>();

			Stack<String> tiles = new Stack<String>();

			String startTile = getStartTile();
			if (startTile == null)
				return generatedTiles;

			tiles.add(startTile);

			depth -= 1;

			while (!tiles.isEmpty()) {
				String tileId = tiles.pop();
				generatedTiles.add(tileId);

				Tile tile = tileMap.get(tileId);

				if (tile.rightTileLink.isNull())
					continue;

				Tile nextTile = getRandomTileWithLeftLink(tile.rightTileLink.getTileType(), depth == 0);
				
				if (nextTile == null)
					continue;
				
				tiles.add(nextTile.id);

				depth -= 1;
			}

			return generatedTiles;
		}

		private String getStartTile() {
			Set<String> keySet = tileMap.keySet();
			for (String key : keySet) {
				Tile tile = tileMap.get(key);
				if (tile.type == Tile.Type.Start)
					return key;
			}
			return null;
		}

		private Tile getRandomTileWithLeftLink(String leftLinkType, boolean end) {
			ArrayList<Tile> tiles = new ArrayList<Tile>();

			Set<String> keySet = tileMap.keySet();
			for (String key : keySet) {
				Tile tile = tileMap.get(key);
				if (tile.matchLeft(leftLinkType)) {
					if (end && tile.type != Tile.Type.End)
						continue;
					if (!end && tile.type == Tile.Type.End)
						continue;
					tiles.add(tile);
				}
			}

			if (tiles.isEmpty())
				return null;

			return tiles.get(random.nextInt(tiles.size()));
		}

	}

	private static class Path {

		/**
		 * Centered to the origin points.
		 */
		ArrayList<Vector2f> points = new ArrayList<Vector2f>();
		Matrix3f transform = new Matrix3f();

		public Path() {
			transform.setIdentity();
		}

		public Path(RandomLevelTileBasedGenerator.Path path) {
			this.transform.set(path.transform);
			for (int i = 0; i < path.points.size(); i++)
				points.add(new Vector2f(path.points.get(i)));
		}

		public void transform(Matrix3f transform) {
			this.transform.mul(transform);
		}

		public void move(float tx, float ty) {
			VecmathUtils.setToTranslation(transform, tx, ty);
		}

		@Override
		public Path clone() {
			return new Path(this);
		}

	}

	private static class Tile {

		public enum Type {
			Start, Normal, End
		};

		String id;

		ArrayList<Path> paths = new ArrayList<Path>();
		Vector2f size = new Vector2f();
		Vector2f position = new Vector2f();

		Type type = Type.Normal;

		TileLink rightTileLink, leftTileLink;

		public Tile(String id) {
			this.id = id;
		}

		public boolean matchLeft(String tileType) {
			return leftTileLink.match(tileType);
		}

		public Tile(Tile tile) {
			this.id = tile.id;
			this.size.set(tile.size);
			this.position.set(tile.position);
			for (int i = 0; i < tile.paths.size(); i++)
				paths.add(tile.paths.get(i).clone());

			// TODO: copy links...
		}

		public Tile translate(float tx, float ty) {
			this.position.set(tx, ty);
			for (int i = 0; i < paths.size(); i++) {
				Path path = paths.get(i);
				path.move(tx, ty);
			}
			return this;
		}

		@Override
		public Tile clone() {
			return new Tile(this);
		}

	}

	private static class TilesProcessor {

		Tile currentTile;
		Map<String, Tile> tileMap;

		Matrix3f transform = new Matrix3f();
		Matrix3f documentMatrix = new Matrix3f();
		int pointsCount;

		Vector2f maxPoint = new Vector2f();
		Vector2f minPoint = new Vector2f();

		public void process(Document document) {

			tileMap = new HashMap<String, RandomLevelTileBasedGenerator.Tile>();

			SvgParser svgParser = new SvgParser();

			svgParser.addHandler(new SvgDocumentHandler() {
				@Override
				protected void handle(SvgParser svgParser, SvgDocument svgDocument, Element element) {
					Matrix3f scale = new Matrix3f();
					scale.setIdentity();
					scale.setM11(-1f);

					documentMatrix.setIdentity();
					documentMatrix.setM12(svgDocument.getHeight());

					documentMatrix.mul(scale);

				}
			});
			svgParser.addHandler(new SvgInkscapeGroupHandler() {

				@Override
				protected void handle(SvgParser svgParser, SvgInkscapeGroup svgInkscapeGroup, Element element) {
					if (SvgInkscapeUtils.isLayer(element))
						return;
					currentTile = new Tile(svgInkscapeGroup.getId());

					String attribute = element.getAttribute("type");

					if ("start".equals(attribute))
						currentTile.type = Tile.Type.Start;

					if ("end".equals(attribute))
						currentTile.type = Tile.Type.End;

					currentTile.leftTileLink = new TileLink();
					currentTile.rightTileLink = new TileLink();

					String linksString = element.getAttribute("links");
					if (!"".equals(linksString)) {
						String[] linkTileTypes = linksString.split(",");

						if (linkTileTypes.length > 0) {
							currentTile.leftTileLink = new TileLink(linkTileTypes[0]);
							currentTile.rightTileLink = new TileLink(linkTileTypes[1]);
						}
					}

					transform.set(documentMatrix);
					transform.mul(svgInkscapeGroup.getTransform());

					maxPoint.set(-10000, -10000);
					minPoint.set(10000, 10000);

					pointsCount = 0;
				}

				@Override
				protected void postHandle(SvgParser svgParser, SvgInkscapeGroup svgInkscapeGroup, Element element) {
					if (SvgInkscapeUtils.isLayer(element))
						return;

					currentTile.size.set(maxPoint);
					currentTile.size.sub(minPoint);

					Vector2f center = new Vector2f(currentTile.size);
					center.scale(0.5f);

					// center current tile paths
					ArrayList<Path> paths = currentTile.paths;
					for (int j = 0; j < paths.size(); j++) {
						Path path = paths.get(j);

						ArrayList<Vector2f> points = path.points;
						for (int k = 0; k < points.size(); k++) {
							Vector2f point = points.get(k);
							point.sub(minPoint);
							point.sub(center);
						}

					}

					// System.out.println("NEW TILE: " + svgInkscapeGroup.getId() + ", " + currentTile.size + ", " + currentTile.position);

					tileMap.put(svgInkscapeGroup.getId(), currentTile);
				}
			});

			svgParser.addHandler(new SvgInkscapePathHandler() {

				Vector3f tmp = new Vector3f();

				@Override
				protected void handle(SvgParser svgParser, SvgPath svgPath, Element element) {
					Path path = new Path();

					Vector2f[] points = svgPath.getPoints();
					for (int i = 0; i < points.length; i++) {

						Vector2f point = points[i];
						tmp.set(point.x, point.y, 1f);
						transform.transform(tmp);

						path.points.add(new Vector2f(tmp.x, tmp.y));

						if (tmp.x > maxPoint.x)
							maxPoint.x = tmp.x;

						if (tmp.y > maxPoint.y)
							maxPoint.y = tmp.y;

						if (tmp.x < minPoint.x)
							minPoint.x = tmp.x;

						if (tmp.y < minPoint.y)
							minPoint.y = tmp.y;

					}

					pointsCount += points.length;

					currentTile.paths.add(path);
				}
			});

			svgParser.parse(document);

		}

	}

	private ArrayList<Tile> levelTiles;
	private Tile lastTile;

	public ArrayList<Shape> generateLevel(Document document, int maxDepth) {
		lastTile = new Tile("");

		TilesProcessor tilesProcessor = new TilesProcessor();
		tilesProcessor.process(document);

		levelTiles = new ArrayList<Tile>();

		Map<String, Tile> tileMap = tilesProcessor.tileMap;

		Generator generator = new Generator();

		generator.tileMap = tileMap;

		// System.out.println("max level depth "+ maxDepth);

		ArrayList<String> generatedTiles = generator.generate(maxDepth);

		for (int i = 0; i < generatedTiles.size(); i++) {
			String tileId = generatedTiles.get(i);
			addRight(tileMap.get(tileId));
		}

		ArrayList<Shape> shapes = new ArrayList<Shape>();

		for (int i = 0; i < levelTiles.size(); i++) {
			Tile tile = levelTiles.get(i);
			for (int j = 0; j < tile.paths.size(); j++) {
				Path path = tile.paths.get(j);
				shapes.add(convertPathToShape(path));
			}

		}

		return shapes;
	}

	private void addRight(Tile tile) {
		addTile(tile, lastTile.position.x + lastTile.size.x, lastTile.position.y);
	}

	private void addTile(Tile tile, float x, float y) {
		Tile newTile = tile.clone();
		newTile.translate(x, y);
		levelTiles.add(newTile);
		lastTile = newTile;
	}

	private Shape convertPathToShape(Path path) {
		float x = path.transform.m02;
		float y = path.transform.m12;

		Vector2[] vertices = new Vector2[path.points.size()];
		for (int i = 0; i < path.points.size(); i++) {
			Vector2f point = path.points.get(i);
			vertices[i] = new Vector2(point.x, point.y);
		}
		Shape shape = new Shape(vertices);

		shape.x = shape.cx + x;
		shape.y = shape.cy + y;

		shape.centerVertices();

		return shape;
	}

}