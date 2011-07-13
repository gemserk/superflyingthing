package com.gemserk.games.superflyingthing.gamestates;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.gemserk.animation4j.transitions.sync.Synchronizers;
import com.gemserk.commons.gdx.GameStateImpl;
import com.gemserk.commons.gdx.gui.Container;
import com.gemserk.commons.gdx.gui.GuiControls;
import com.gemserk.commons.gdx.gui.Text;
import com.gemserk.commons.gdx.gui.TextButton;
import com.gemserk.commons.gdx.gui.TextButton.ButtonHandler;
import com.gemserk.componentsengine.input.InputDevicesMonitorImpl;
import com.gemserk.componentsengine.input.LibgdxInputMappingBuilder;
import com.gemserk.games.superflyingthing.Game;
import com.gemserk.games.superflyingthing.resources.GameResources;
import com.gemserk.resources.ResourceManager;
import com.gemserk.resources.ResourceManagerImpl;

public class LevelSelectionGameState extends GameStateImpl {

	private final Game game;
	private SpriteBatch spriteBatch;
	private ResourceManager<String> resourceManager;
	
	Container container;
	private InputDevicesMonitorImpl<String> inputDevicesMonitor;

	public LevelSelectionGameState(Game game) {
		this.game = game;
	}

	@Override
	public void init() {
		float width = Gdx.graphics.getWidth();
		float height = Gdx.graphics.getHeight();
		float centerX = width * 0.5f;

		spriteBatch = new SpriteBatch();
		resourceManager = new ResourceManagerImpl<String>();

		GameResources.load(resourceManager);

		BitmapFont titleFont = resourceManager.getResourceValue("TitleFont");
		BitmapFont buttonFont = resourceManager.getResourceValue("ButtonFont");
		
		BitmapFont levelFont = resourceManager.getResourceValue("LevelFont");

		Text title = new Text("Select Level", centerX, height * 0.9f).setColor(Color.GREEN);
		title.setFont(titleFont);
		
		container = new Container();
		container.add(title);

		Sprite levelThumbnail = resourceManager.getResourceValue("WhiteRectangle");

		// TODO: generate the levels list automatically from an array...
		
		float x = 0f;
		float y = height * (0.75f + 0.12f) ;
		
		for (int i = 0; i < Levels.levelsCount(); i++) {

			float w = width * 0.1f;
			float h = height * 0.1f;
			
			final int levelIndex = i;
			
			if (i % 6 == 0)  {
				y -= height * 0.12f;
				x = 0f;
			}
			
			x += width * 0.15f;
			
			container.add(GuiControls.imageButton(levelThumbnail) //
					.color(0.8f, 0.8f, 0.8f, 1f) //
					.size(w, h) //
					.position(x, y) //
					.handler(new ButtonHandler() {
						@Override
						public void onReleased() {
							GameInformation.level = levelIndex;
							game.transition(game.getPlayScreen(), 500, 250);
						}
					}) //
					.build());
			container.add(GuiControls.label("" + i) //
					.position(x, y) //
					.color(Color.BLUE) //
					.font(levelFont) //
					.build());
			
		}
		
		if (Gdx.app.getType() != ApplicationType.Android)
			container.add(new TextButton(buttonFont, "Back", width * 0.95f, height * 0.05f) //
					.setNotOverColor(Color.WHITE) //
					.setOverColor(Color.GREEN) //
					.setColor(Color.WHITE) //
					.setBoundsOffset(20f, 20f) //
					.setAlignment(HAlignment.RIGHT) //
					.setButtonHandler(new ButtonHandler() {
						@Override
						public void onReleased() {
							game.transition(game.getSelectPlayModeScreen(), 500, 500);
						}
					}));
		
		inputDevicesMonitor = new InputDevicesMonitorImpl<String>();
		new LibgdxInputMappingBuilder<String>(inputDevicesMonitor, Gdx.input) {
			{
				if (Gdx.app.getType() == ApplicationType.Android)
					monitorKey("back", Keys.BACK);
				else
					monitorKey("back", Keys.ESCAPE);
			}
		};

	}

	@Override
	public void render(int delta) {
		Gdx.graphics.getGL10().glClear(GL10.GL_COLOR_BUFFER_BIT);
		spriteBatch.begin();
		container.draw(spriteBatch);
		spriteBatch.end();
		// GuiControls.debugRender(scene);
	}

	@Override
	public void update(int delta) {
		Synchronizers.synchronize(delta);
		inputDevicesMonitor.update();
		container.update();
		if (inputDevicesMonitor.getButton("back").isReleased())
			game.transition(game.getSelectPlayModeScreen(), 500, 500);
	}

	@Override
	public void resume() {
		Gdx.input.setCatchBackKey(true);
	}

	@Override
	public void pause() {
		Gdx.input.setCatchBackKey(false);
	}

	@Override
	public void dispose() {
		resourceManager.unloadAll();
		spriteBatch.dispose();
		spriteBatch = null;
	}

}
