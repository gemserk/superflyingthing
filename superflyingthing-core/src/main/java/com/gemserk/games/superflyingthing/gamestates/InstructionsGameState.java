package com.gemserk.games.superflyingthing.gamestates;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.gemserk.analytics.Analytics;
import com.gemserk.animation4j.transitions.sync.Synchronizers;
import com.gemserk.commons.gdx.GameStateImpl;
import com.gemserk.commons.gdx.gui.Container;
import com.gemserk.commons.gdx.gui.GuiControls;
import com.gemserk.games.superflyingthing.Game;
import com.gemserk.games.superflyingthing.preferences.PlayerProfile;
import com.gemserk.games.superflyingthing.scripts.controllers.ControllerType;
import com.gemserk.resources.ResourceManager;

public class InstructionsGameState extends GameStateImpl {

	private final Game game;
	private SpriteBatch spriteBatch;
	private ResourceManager<String> resourceManager;
	private Sprite whiteRectangle;
	Container container;
	private InputAdapter inputProcessor;

	public void setResourceManager(ResourceManager<String> resourceManager) {
		this.resourceManager = resourceManager;
	}

	public InstructionsGameState(Game game) {
		this.game = game;
	}

	@Override
	public void init() {
		float width = Gdx.graphics.getWidth();
		float height = Gdx.graphics.getHeight();
		float centerX = width * 0.5f;

		spriteBatch = new SpriteBatch();

		container = new Container();

		BitmapFont font = resourceManager.getResourceValue("InstructionsFont");

		whiteRectangle = resourceManager.getResourceValue("WhiteRectangle");
		whiteRectangle.setSize(width, height);
		whiteRectangle.setColor(0f, 0f, 0f, 0.75f);

		container.add(GuiControls.label("Game Instructions") //
				.position(centerX, height * 0.9f) //
				.color(Color.WHITE) //
				.font(font)//
				.build());

		PlayerProfile playerProfile = game.getGamePreferences().getCurrentPlayerProfile();
		ControllerType controllerType = playerProfile.getControllerType();
		String instructionsForControllerType = getInstructions(controllerType);

		container.add(GuiControls.label(instructionsForControllerType + "\n" //
				+ "Objective: get to the next planet alive,\n" + //
				"if you hit an obstacle you die.") //
				.position(width * 0.1f, height * 0.55f) //
				.center(0f, 0.5f) //
				.color(Color.WHITE) //
				.font(font)//
				.build());
		container.add(GuiControls.label("Press screen to start") //
				.position(centerX, height * 0.1f) //
				.color(Color.WHITE) //
				.font(font)//
				.build());

		inputProcessor = new InputAdapter() {
			@Override
			public boolean keyUp(int keycode) {
				game.getGamePreferences().setTutorialEnabled(false);
				game.transition(game.getPlayScreen(), 0, 0, true);
				return true;
			}

			@Override
			public boolean touchUp(int x, int y, int pointer, int button) {
				game.getGamePreferences().setTutorialEnabled(false);
				game.transition(game.getPlayScreen(), 0, 0, true);
				return true;
			}
		};

		Analytics.traker.trackPageView("/instructions", "/instructions", null);

	}
	

	private String getInstructions(ControllerType controllerType) {

		switch (controllerType) {
		case KeyboardController:
			return "LEFT KEY - rotates the ship to the left\n" //
					+ "RIGHT KEY - rotates the ship to the right\n" //
					+ "SPACE KEY - releases the ship from the planet\n";
		case AnalogKeyboardController:
			return "LEFT KEY - rotates the ship to go the left\n" //
					+ "RIGHT KEY - rotates the ship to go the right\n" //
					+ "UP KEY - rotates the ship to go up\n" //
					+ "DOWN KEY - rotates the ship to go down\n" //
					+ "SPACE KEY - releases the ship from the planet\n";
		case ClassicController:
			return "LEFT HALF SCREEN - rotates the ship to the left\n" //
					+ "RIGHT HALF SCREEN - rotates the ship to the right\n" //
					+ "TOUCH SCREEN - releases the ship from the planet";
		case AxisController:
			return "Press screen to define the Axis and then, \n" //
					+ "LEFT SCREEN FROM AXIS - rotates the ship to the left\n" //
					+ "RIGHT SCREEN FROM AXIS - rotates the ship to the right\n" //
					+ "TOUCH SCREEN - releases the ship from the planet";
		case AnalogController:
			return "Press screen to define the CENTER of the Stick and then, \n" //
					+ "press near the CENTER to define the direction\n\n" //
					+ "TOUCH SCREEN - releases the ship from the planet";
		case TiltController:
			return "TILT LEFT - rotates the ship to the LEFT\n" //
					+ "TILT RIGHT - rotates the ship to the RIGHT\n\n" //
					+ "TOUCH SCREEN - releases the ship from the planet";
		}

		return "";
	}

	@Override
	public void show() {
		super.show();
		game.getPlayScreen().show();
	}

	@Override
	public void hide() {
		super.hide();
		game.getPlayScreen().hide();
	}

	@Override
	public void resume() {
		super.resume();
		Gdx.input.setCatchBackKey(true);
		game.getAdWhirlViewHandler().hide();
		Gdx.input.setInputProcessor(inputProcessor);
	}

	@Override
	public void pause() {
		Gdx.input.setInputProcessor(null);
	}

	@Override
	public void render(int delta) {
		Gdx.graphics.getGL10().glClear(GL10.GL_COLOR_BUFFER_BIT);

		game.getPlayScreen().render(delta);

		spriteBatch.begin();
		whiteRectangle.draw(spriteBatch);
		container.draw(spriteBatch);
		spriteBatch.end();
	}

	@Override
	public void update(int delta) {
		Synchronizers.synchronize(delta);
		container.update();
	}

	@Override
	public void dispose() {
		spriteBatch.dispose();
		spriteBatch = null;
	}

}
