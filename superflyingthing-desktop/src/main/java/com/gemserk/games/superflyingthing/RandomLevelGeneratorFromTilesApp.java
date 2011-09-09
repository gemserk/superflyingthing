package com.gemserk.games.superflyingthing;

import java.util.ArrayList;

import org.w3c.dom.Document;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer10;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.gemserk.commons.gdx.GameStateImpl;
import com.gemserk.commons.gdx.ScreenImpl;
import com.gemserk.commons.gdx.graphics.ImmediateModeRendererUtils;
import com.gemserk.games.superflyingthing.levels.RandomLevelTileBasedGenerator;
import com.gemserk.games.superflyingthing.resources.GameResources;
import com.gemserk.resources.Resource;
import com.gemserk.resources.ResourceManager;
import com.gemserk.resources.ResourceManagerImpl;
import com.gemserk.resources.monitor.FilesMonitorImpl;
import com.gemserk.vecmath.Vector3f;

public class RandomLevelGeneratorFromTilesApp {

	private static class Application extends com.gemserk.commons.gdx.Game {
		
		boolean restartPressed;

		public void create() {
			setScreen(new ScreenImpl(new GameState()));
			
		}

		@Override
		public void render() {
			super.render();

			if (Gdx.input.isKeyPressed(Keys.R)) {
				restartPressed = true;
			} else {
				if (restartPressed) {
					getScreen().restart();
					restartPressed = false;
				}
			}
		}

	}

	private static class GameState extends GameStateImpl {

		private GL10 gl;
		private ImmediateModeRenderer10 renderer;
		private Matrix4 projectionMatrix;

		private ArrayList<Shape> shapes;
		private ResourceManager<String> resourceManager;

		@Override
		public void init() {
			gl = Gdx.graphics.getGL10();

			resourceManager = new ResourceManagerImpl<String>();

			GameResources.load(resourceManager, new FilesMonitorImpl());

			projectionMatrix = new Matrix4();

			float worldWidth = 80;
			float worldHeight = 48;

			float zoom = 0.25f;

			projectionMatrix.setToOrtho2D(-worldWidth * 0.25f / zoom, -worldHeight * 0.5f / zoom, worldWidth / zoom, worldHeight / zoom);

			renderer = new ImmediateModeRenderer10();

			Resource<Document> documentResource = resourceManager.get("RandomLevelTilesDocument");

			int maxLevelDepth = MathUtils.random(2, 7);
			
			shapes = new RandomLevelTileBasedGenerator().generateLevel(documentResource.get(), maxLevelDepth);

		}

		Vector3f tmp = new Vector3f();

		@Override
		public void render() {
			gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

			ImmediateModeRendererUtils.getProjectionMatrix().set(projectionMatrix);
			ImmediateModeRendererUtils.drawLine(0, -1000, 0, 1000, Color.GREEN);
			ImmediateModeRendererUtils.drawLine(-1000, 0, 1000, 0, Color.GREEN);

			// for (int i = 0; i < shapes.size(); i++) {
			// Shape shape = shapes.get(i);
			// renderer.begin(projectionMatrix, GL10.GL_LINE_LOOP);
			// for (int k = 0; k < shape.vertices.length; k++) {
			// Vector2 vertex = shape.vertices[k];
			// renderer.color(1f, 1f, 1f, 1f);
			// renderer.vertex(vertex.x, vertex.y, 0f);
			// }
			// renderer.end();
			//
			// }

			for (int i = 0; i < shapes.size(); i++) {
				Shape shape = shapes.get(i);
				renderer.begin(projectionMatrix, GL10.GL_LINE_LOOP);
				for (int k = 0; k < shape.vertices.length; k++) {
					Vector2 vertex = shape.vertices[k];
					renderer.color(0f, 0f, 1f, 1f);
					renderer.vertex(vertex.x + shape.x, vertex.y + shape.y, 0f);
				}
				renderer.end();
			}

		}

		@Override
		public void update() {

		}

	}

	public static void main(String[] argv) {
		new LwjglApplication(new Application(), "Random Level Generator from tiles - Application", 800, 480, false);
	}
}
