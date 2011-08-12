package com.gemserk.games.superflyingthing.gamestates;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.gemserk.animation4j.transitions.sync.Synchronizers;
import com.gemserk.commons.gdx.GameStateImpl;
import com.gemserk.commons.gdx.graphics.ImmediateModeRendererUtils;
import com.gemserk.commons.gdx.gui.ButtonHandler;
import com.gemserk.commons.gdx.gui.Container;
import com.gemserk.commons.gdx.gui.Control;
import com.gemserk.commons.gdx.gui.GuiControls;
import com.gemserk.commons.gdx.gui.Text;
import com.gemserk.commons.gdx.gui.TextButton;
import com.gemserk.componentsengine.input.InputDevicesMonitorImpl;
import com.gemserk.componentsengine.input.LibgdxInputMappingBuilder;
import com.gemserk.games.superflyingthing.Colors;
import com.gemserk.games.superflyingthing.Game;
import com.gemserk.games.superflyingthing.levels.Levels;
import com.gemserk.games.superflyingthing.preferences.PlayerProfile;
import com.gemserk.resources.ResourceManager;

public class LevelSelectionGameState extends GameStateImpl {

	private final Game game;
	private SpriteBatch spriteBatch;
	private ResourceManager<String> resourceManager;
	private Integer selectedLevel;

	Container container;
	private InputDevicesMonitorImpl<String> inputDevicesMonitor;
	private Sprite whiteRectangleSprite;

	private Rectangle selectionRectangle;
	private Sprite backgroundSprite;

	public void setResourceManager(ResourceManager<String> resourceManager) {
		this.resourceManager = resourceManager;
	}

	public LevelSelectionGameState(Game game) {
		this.game = game;
	}

	@Override
	public void init() {
		selectedLevel = null;
		selectionRectangle = new Rectangle();

		float width = Gdx.graphics.getWidth();
		float height = Gdx.graphics.getHeight();
		float centerX = width * 0.5f;

		spriteBatch = new SpriteBatch();

		BitmapFont titleFont = resourceManager.getResourceValue("TitleFont");
		BitmapFont buttonFont = resourceManager.getResourceValue("ButtonFont");

		BitmapFont levelFont = resourceManager.getResourceValue("LevelFont");

		Text title = new Text("Select Level", centerX, height * 0.90f).setColor(Color.GREEN);
		title.setFont(titleFont);

		container = new Container();
		container.add(title);

		Sprite levelThumbnail = resourceManager.getResourceValue("LevelButtonSprite");
		Sprite tickSprite = resourceManager.getResourceValue("TickSprite");

		backgroundSprite = resourceManager.getResourceValue("BackgroundSprite");
		backgroundSprite.setPosition(0f, 0f);
		backgroundSprite.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		final PlayerProfile playerProfile = game.getGamePreferences().getCurrentPlayerProfile();

		float x = 0f;
		float y = height * (0.80f + 0.12f);

		float w = width * 0.1f;
		float h = height * 0.15f;

		selectionRectangle.setWidth(w + 2f);
		selectionRectangle.setHeight(h + 2f);

		for (int i = 0; i < Levels.levelsCount(); i++) {

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
						public void onReleased(Control control) {
							// there is no point in forcing the player to play all the levels, at least for now.
							// if (!playerProfile.hasPlayedLevel(levelIndex))
							// return;
							if (selectedLevel != null && levelIndex == selectedLevel)
								play();
							else {
								select(levelIndex);
								selectionRectangle.setX(control.getX());
								selectionRectangle.setY(control.getY());
							}
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

		container.add(GuiControls.textButton() //
				.id("PlayButton") //
				.text("Play") //
				.font(titleFont) //
				.position(width * 0.5f, height * 0.25f) //
				.center(0.5f, 0.5f) //
				.notOverColor(0.7f, 0.7f, 0.7f, 0f) //
				.overColor(0.7f, 0.7f, 0.7f, 0f) //
				.boundsOffset(30f, 30f) //
				.handler(new ButtonHandler() {
					@Override
					public void onReleased(Control control) {
						if (selectedLevel != null)
							play();
					}
				}) //
				.build());

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
		game.getGameData().put("previewLevel", null);
	}

	private void play() {
		GameInformation.level = selectedLevel;
		game.transition(game.getPlayScreen()) //
				.leaveTime(250) //
				.enterTime(250) //
				.disposeCurrent() //
				.start();
		game.getGameData().put("previewLevel", null);
	}

	private void select(int level) {
		if (selectedLevel != null && level == selectedLevel)
			return;

		selectedLevel = level;
		Gdx.app.log("SuperFlyingThing", "Level " + (level + 1) + " selected");

		// load level in the background
		game.getGameData().put("previewLevel", level);
		game.getBackgroundGameScreen().restart();

		TextButton playButton = container.findControl("PlayButton");
		playButton.setOverColor(Color.GREEN);
		playButton.setNotOverColor(Color.WHITE);
	}

	@Override
	public void render(int delta) {
		Gdx.graphics.getGL10().glClear(GL10.GL_COLOR_BUFFER_BIT);

		if (selectedLevel != null)
			game.getBackgroundGameScreen().render(delta);

		spriteBatch.begin();

		if (selectedLevel == null)
			backgroundSprite.draw(spriteBatch);

		whiteRectangleSprite.draw(spriteBatch);
		container.draw(spriteBatch);
		spriteBatch.end();

		if (selectedLevel == null)
			return;

		ImmediateModeRendererUtils.drawRectangle(selectionRectangle, 0.5f, 0.5f, Colors.yellow);

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
