package com.gemserk.games.superflyingthing.gamestates;

import java.util.ArrayList;

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
import com.gemserk.commons.gdx.gui.ButtonHandler;
import com.gemserk.commons.gdx.gui.Container;
import com.gemserk.commons.gdx.gui.Control;
import com.gemserk.commons.gdx.gui.GuiControls;
import com.gemserk.commons.gdx.gui.ImageButton;
import com.gemserk.componentsengine.input.InputDevicesMonitorImpl;
import com.gemserk.componentsengine.input.LibgdxInputMappingBuilder;
import com.gemserk.games.superflyingthing.Events;
import com.gemserk.games.superflyingthing.Game;
import com.gemserk.games.superflyingthing.Screens;
import com.gemserk.games.superflyingthing.levels.Levels;
import com.gemserk.games.superflyingthing.preferences.PlayerProfile;
import com.gemserk.games.superflyingthing.resources.GameResources;
import com.gemserk.resources.ResourceManager;

public class LevelSelectionGameState extends GameStateImpl {

	Game game;
	ResourceManager<String> resourceManager;

	SpriteBatch spriteBatch;
	Integer selectedLevel;

	Container screen;
	ArrayList<ImageButton> allLevelButtons;

	InputDevicesMonitorImpl<String> inputDevicesMonitor;

	Sprite backgroundSprite;

	@Override
	public void init() {
		selectedLevel = null;

		float width = Gdx.graphics.getWidth();
		float height = Gdx.graphics.getHeight();
		float centerX = width * 0.5f;

		spriteBatch = new SpriteBatch();

		BitmapFont titleFont = resourceManager.getResourceValue("TitleFont");
		BitmapFont buttonFont = resourceManager.getResourceValue("ButtonFont");

		BitmapFont levelFont = resourceManager.getResourceValue(GameResources.Fonts.Level);

		// Text title = new Text("Select Level", centerX, height * 0.90f).setColor(Color.GREEN);
		// title.setFont(titleFont);

		screen = new Container();

		// container.add(title);

		Sprite levelThumbnail = resourceManager.getResourceValue(GameResources.Sprites.LevelButton);
		Sprite tickSprite = resourceManager.getResourceValue("TickSprite");

		backgroundSprite = resourceManager.getResourceValue("BackgroundSprite");
		backgroundSprite.setPosition(0f, 0f);
		backgroundSprite.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		final PlayerProfile playerProfile = game.getGamePreferences().getCurrentPlayerProfile();

		float x = 0f;
		float y = height * (0.80f + 0.12f);

		float w = width * 0.1f;
		float h = height * 0.15f;

		allLevelButtons = new ArrayList<ImageButton>();

		for (int i = 0; i < Levels.levelsCount(); i++) {

			// float h = w;

			final int levelIndex = i;

			if (i % 6 == 0) {
				// y -= height * 0.12f;
				y -= h * 1.2f;
				x = 0f;
			}

			x += width * 0.15f;

			Color color = new Color(1f, 1f, 1f, 0.5f);

			ImageButton selectLevelButton = GuiControls.imageButton(levelThumbnail) //
					.color(color) //
					.size(w, h) //
					.position(x, y) //
					.handler(new ButtonHandler() {
						@Override
						public void onReleased(Control control) {
							// there is no point in forcing the player to play all the levels, at least for now.
							// if (!playerProfile.hasPlayedLevel(levelIndex))
							// return;
							if (selectedLevel != null && levelIndex + 1 == selectedLevel)
								play();
							else {
								select(levelIndex + 1);

								for (int j = 0; j < allLevelButtons.size(); j++) {
									ImageButton imageButton = allLevelButtons.get(j);
									imageButton.setColor(1f, 1f, 1f, 0.5f);
									if (control == imageButton)
										imageButton.setColor(1f, 1f, 1f, 1f);
								}

							}
						}
					}) //
					.build();

			allLevelButtons.add(selectLevelButton);

			screen.add(selectLevelButton);

			screen.add(GuiControls.label("" + (i + 1)) //
					.position(x, y) //
					.color(color) //
					.font(levelFont) //
					.build());

			if (playerProfile.hasPlayedLevel(levelIndex + 1))
				screen.add(GuiControls.imageButton(tickSprite) //
						.position(x + w * 0.5f, y - h * 0.5f) //
						.center(1f, 0f) //
						.size(w * 0.5f, h * 0.5f) //
						.build());

		}

		if (Gdx.app.getType() != ApplicationType.Android)
			screen.add(GuiControls.textButton() //
					.text("Back") //
					.font(buttonFont) //
					.position(width * 0.98f, height * 0.05f) //
					.center(1f, 0.5f) //
					.notOverColor(Color.WHITE) //
					.overColor(Color.GREEN) //
					.boundsOffset(30f, 30f) //
					.handler(new ButtonHandler() {
						@Override
						public void onReleased(Control control) {
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

		game.getBackgroundGameScreen().init();
	}

	private void back() {
		game.transition(Screens.SelectPlayMode) //
				.disposeCurrent() //
				.start();
	}

	private void play() {
		// GameInformation.level = selectedLevel;
		game.transition(Screens.Play) //
				.parameter("level", selectedLevel) //
				.disposeCurrent() //
				.start();
	}

	private void select(int level) {
		if (selectedLevel != null && level == selectedLevel)
			return;

		selectedLevel = level;
		Gdx.app.log("SuperFlyingThing", "Level " + selectedLevel + " selected");

		game.getEventManager().registerEvent(Events.previewLevel, level);
	}

	@Override
	public void render() {
		Gdx.graphics.getGL10().glClear(GL10.GL_COLOR_BUFFER_BIT);

		game.getBackgroundGameScreen().setDelta(getDelta());
		if (selectedLevel != null)
			game.getBackgroundGameScreen().render();

		spriteBatch.begin();

		if (selectedLevel == null)
			backgroundSprite.draw(spriteBatch);

		screen.draw(spriteBatch);
		spriteBatch.end();

		if (selectedLevel == null)
			return;

	}

	@Override
	public void update() {
		Synchronizers.synchronize(getDelta());
		inputDevicesMonitor.update();
		screen.update();
		if (inputDevicesMonitor.getButton("back").isReleased())
			back();
		game.getBackgroundGameScreen().setDelta(getDelta());
		game.getBackgroundGameScreen().update();
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
