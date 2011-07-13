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
import com.gemserk.commons.gdx.gui.Text;
import com.gemserk.commons.gdx.gui.TextButton;
import com.gemserk.commons.gdx.gui.TextButton.ButtonHandler;
import com.gemserk.componentsengine.input.InputDevicesMonitorImpl;
import com.gemserk.componentsengine.input.LibgdxInputMappingBuilder;
import com.gemserk.games.superflyingthing.Game;
import com.gemserk.games.superflyingthing.resources.GameResources;
import com.gemserk.resources.ResourceManager;
import com.gemserk.resources.ResourceManagerImpl;

public class SelectPlayModeGameState extends GameStateImpl {

	private final Game game;
	private SpriteBatch spriteBatch;
	private ResourceManager<String> resourceManager;
	
	Container container;
	private InputDevicesMonitorImpl<String> inputDevicesMonitor;
	private Sprite backgroundSprite;

	public SelectPlayModeGameState(Game game) {
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

		Text title = new Text("Select Mode", centerX, height * 0.9f).setColor(Color.GREEN);
		title.setFont(titleFont);

		TextButton challengeModeButton = new TextButton(buttonFont, "Challenge", centerX, height * 0.7f) //
				.setNotOverColor(Color.WHITE) //
				.setOverColor(Color.GREEN) //
				.setColor(Color.WHITE) //
				.setBoundsOffset(20f, 20f) //
				.setButtonHandler(new ButtonHandler(){
					@Override
					public void onReleased() {
						GameInformation.gameMode = GameInformation.ChallengeGameMode;
						game.transition(game.getLevelSelectionScreen(), 500, 500);	
					}
				});

		TextButton practiceModeButton = new TextButton(buttonFont, "Practice", centerX, height * 0.5f) //
				.setNotOverColor(Color.WHITE) //
				.setOverColor(Color.GREEN) //
				.setColor(Color.WHITE) //
				.setBoundsOffset(20f, 20f) //
				.setButtonHandler(new ButtonHandler(){
					@Override
					public void onReleased() {
						GameInformation.gameMode = GameInformation.PracticeGameMode;
						game.transition(game.getPlayScreen(), 500, 250);		
					}
				});

		TextButton randomModeButton = new TextButton(buttonFont, "Random", centerX, height * 0.3f) //
				.setNotOverColor(Color.WHITE) //
				.setOverColor(Color.GREEN) //
				.setColor(Color.WHITE) //
				.setBoundsOffset(20f, 20f) //
				.setButtonHandler(new ButtonHandler() {
					@Override
					public void onReleased() {
						GameInformation.gameMode = GameInformation.RandomGameMode;
						game.transition(game.getPlayScreen(), 500, 250);
					}
				});

		TextButton backButton = new TextButton(buttonFont, "Back", width * 0.95f, height * 0.05f) //
				.setNotOverColor(Color.WHITE) //
				.setOverColor(Color.GREEN) //
				.setColor(Color.WHITE) //
				.setBoundsOffset(20f, 20f) //
				.setAlignment(HAlignment.RIGHT) //
				.setButtonHandler(new ButtonHandler() {
					@Override
					public void onReleased() {
						game.transition(game.getMainMenuScreen(), 500, 500);
					}
				});

		container = new Container();
		
		container.add(title);
		container.add(challengeModeButton);
		container.add(practiceModeButton);
		container.add(randomModeButton);
		
		if (Gdx.app.getType() != ApplicationType.Android)
			container.add(backButton);
		
		inputDevicesMonitor = new InputDevicesMonitorImpl<String>();
		new LibgdxInputMappingBuilder<String>(inputDevicesMonitor, Gdx.input) {
			{
				if (Gdx.app.getType() == ApplicationType.Android)
					monitorKey("back", Keys.BACK);
				else
					monitorKey("back", Keys.ESCAPE);
			}
		};
		
		backgroundSprite = resourceManager.getResourceValue("BackgroundSprite");
		backgroundSprite.setPosition(0, 0);
		backgroundSprite.setSize(width, height);
	}

	@Override
	public void render(int delta) {
		Gdx.graphics.getGL10().glClear(GL10.GL_COLOR_BUFFER_BIT);
		spriteBatch.begin();
		backgroundSprite.draw(spriteBatch);
		container.draw(spriteBatch);
		spriteBatch.end();
	}

	@Override
	public void update(int delta) {
		Synchronizers.synchronize(delta);
		inputDevicesMonitor.update();
		container.update();
		if (inputDevicesMonitor.getButton("back").isReleased())
			game.transition(game.getMainMenuScreen(), 500, 500);
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
