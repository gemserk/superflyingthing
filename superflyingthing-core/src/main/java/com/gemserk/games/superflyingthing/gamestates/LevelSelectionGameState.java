package com.gemserk.games.superflyingthing.gamestates;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.gemserk.animation4j.transitions.sync.Synchronizers;
import com.gemserk.commons.gdx.GameStateImpl;
import com.gemserk.commons.gdx.gui.Container;
import com.gemserk.commons.gdx.gui.GuiControls;
import com.gemserk.commons.gdx.gui.Text;
import com.gemserk.commons.gdx.gui.TextButton.ButtonHandler;
import com.gemserk.componentsengine.input.InputDevicesMonitorImpl;
import com.gemserk.componentsengine.input.LibgdxInputMappingBuilder;
import com.gemserk.games.superflyingthing.Game;
import com.gemserk.games.superflyingthing.levels.Levels;
import com.gemserk.games.superflyingthing.preferences.PlayerProfile;
import com.gemserk.resources.ResourceManager;

public class LevelSelectionGameState extends GameStateImpl {

	private final Game game;
	private SpriteBatch spriteBatch;
	private ResourceManager<String> resourceManager;
	private int selectedLevel;

	Container container;
	private InputDevicesMonitorImpl<String> inputDevicesMonitor;
	private Sprite whiteRectangleSprite;

	public void setResourceManager(ResourceManager<String> resourceManager) {
		this.resourceManager = resourceManager;
	}

	public LevelSelectionGameState(Game game) {
		this.game = game;
	}

	@Override
	public void init() {
		float width = Gdx.graphics.getWidth();
		float height = Gdx.graphics.getHeight();
		float centerX = width * 0.5f;

		spriteBatch = new SpriteBatch();

		BitmapFont titleFont = resourceManager.getResourceValue("TitleFont");
		BitmapFont buttonFont = resourceManager.getResourceValue("ButtonFont");

		BitmapFont levelFont = resourceManager.getResourceValue("LevelFont");

		Text title = new Text("Select Level", centerX, height * 0.9f).setColor(Color.GREEN);
		title.setFont(titleFont);

		container = new Container();
		container.add(title);

		Sprite levelThumbnail = resourceManager.getResourceValue("LevelButtonSprite");
		Sprite tickSprite = resourceManager.getResourceValue("TickSprite");

		final PlayerProfile playerProfile = game.getGamePreferences().getCurrentPlayerProfile();

		float x = 0f;
		float y = height * (0.75f + 0.12f);

		for (int i = 0; i < Levels.levelsCount(); i++) {

			float w = width * 0.1f;
			float h = height * 0.15f;
			// float h = w;

			final int levelIndex = i;

			if (i % 6 == 0) {
				// y -= height * 0.12f;
				y -= h * 1.2f;
				x = 0f;
			}

			x += width * 0.15f;

			Color color = new Color(Color.WHITE);
			// if (!playerProfile.hasPlayedLevel(levelIndex + 1))
			// color.set(0.5f, 0.5f, 0.5f, 1f);

			container.add(GuiControls.imageButton(levelThumbnail) //
					.color(color) //
					.size(w, h) //
					.position(x, y) //
					.handler(new ButtonHandler() {
						@Override
						public void onReleased() {
							// there is no point in forcing the player to play all the levels, at least for now.
							// if (!playerProfile.hasPlayedLevel(levelIndex))
							// return;
							if (selectedLevel == levelIndex)
								play(levelIndex);
							else
								select(levelIndex);
						}
					}) //
					.build());
			container.add(GuiControls.label("" + (i + 1)) //
					.position(x, y) //
					.color(color) //
					.font(levelFont) //
					.build());

			if (playerProfile.hasPlayedLevel(levelIndex + 1))
				container.add(GuiControls.imageButton(tickSprite) //
						.position(x + w * 0.5f, y - h * 0.5f) //
						.center(1f, 0f) //
						.size(w * 0.5f, h * 0.5f) //
						.build());

		}

		if (Gdx.app.getType() != ApplicationType.Android)
			container.add(GuiControls.textButton() //
					.text("Back") //
					.font(buttonFont) //
					.position(width * 0.98f, height * 0.05f) //
					.center(1f, 0.5f) //
					.notOverColor(Color.WHITE) //
					.overColor(Color.GREEN) //
					.boundsOffset(30f, 30f) //
					.handler(new ButtonHandler() {
						@Override
						public void onReleased() {
							back();
						}
					}) //
					.build());

		inputDevicesMonitor = new InputDevicesMonitorImpl<String>();
		new LibgdxInputMappingBuilder<String>(inputDevicesMonitor, Gdx.input) {
			{
				monitorKeys("back", Keys.BACK, Keys.ESCAPE);
			}
		};

		whiteRectangleSprite = resourceManager.getResourceValue("WhiteRectangle");
		whiteRectangleSprite.setPosition(0, 0);
		whiteRectangleSprite.setSize(width, height);
		whiteRectangleSprite.setColor(0.2f, 0.2f, 0.2f, 0.3f);

		game.getBackgroundGameScreen().init();
	}

	private void back() {
		game.transition(game.getSelectPlayModeScreen()) //
				.leaveTime(250) //
				.enterTime(250) //
				.disposeCurrent() //
				.start();
	}

	private void play(int level) {
		GameInformation.level = level;
		game.transition(game.getPlayScreen()) //
				.leaveTime(250) //
				.enterTime(250) //
				.disposeCurrent() //
				.start();
	}

	private void select(int level) {
		selectedLevel = level;
		Gdx.app.log("SuperFlyingThing", "Level " + (level + 1) + " selected");
		
		// load level in the background
		game.getGameData().put("previewLevel", level);
		game.getBackgroundGameScreen().restart();
	}

	@Override
	public void render(int delta) {
		Gdx.graphics.getGL10().glClear(GL10.GL_COLOR_BUFFER_BIT);
		game.getBackgroundGameScreen().render(delta);
		spriteBatch.begin();
		whiteRectangleSprite.draw(spriteBatch);
		container.draw(spriteBatch);
		spriteBatch.end();
	}

	@Override
	public void update(int delta) {
		Synchronizers.synchronize(delta);
		inputDevicesMonitor.update();
		container.update();
		if (inputDevicesMonitor.getButton("back").isReleased())
			back();
		game.getBackgroundGameScreen().update(delta);
	}

	@Override
	public void show() {
		super.show();
		game.getBackgroundGameScreen().show();
	}

	@Override
	public void resume() {
		super.resume();
		Gdx.input.setCatchBackKey(true);
		game.getAdWhirlViewHandler().show();
		game.getBackgroundGameScreen().resume();
	}

	@Override
	public void dispose() {
		spriteBatch.dispose();
		spriteBatch = null;
	}

}
