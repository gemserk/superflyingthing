package com.gemserk.games.superflyingthing.transitions;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.gemserk.animation4j.transitions.Transitions;
import com.gemserk.animation4j.transitions.sync.Synchronizers;
import com.gemserk.commons.gdx.GameTransitions;
import com.gemserk.commons.gdx.GameTransitions.TransitionHandler;
import com.gemserk.commons.gdx.Screen;
import com.gemserk.games.superflyingthing.resources.GameResources;
import com.gemserk.resources.ResourceManager;
import com.gemserk.resources.ResourceManagerImpl;

public class FadeInTransition extends GameTransitions.EnterTransition {

	private float alpha = 1f;
	private Sprite whiteRectangle;
	ResourceManager<String> resourceManager;
	SpriteBatch spriteBatch;
	private final int time;

	public void setAlpha(float alpha) {
		this.alpha = alpha;
	}

	public FadeInTransition(Screen screen, int time) {
		super(screen, time);
		this.time = time;
	}

	public FadeInTransition(Screen screen, int time, TransitionHandler transitionHandler) {
		super(screen, time, transitionHandler);
		this.time = time;
	}

	@Override
	public void init() {
		super.init();
		resourceManager = new ResourceManagerImpl<String>();
		GameResources.load(resourceManager);
		whiteRectangle = resourceManager.getResourceValue("WhiteRectangle");
		spriteBatch = new SpriteBatch();
		Synchronizers.transition(this, "alpha", Transitions.transitionBuilder(1f).end(0f).time(time));
	}

	@Override
	public void dispose() {
		super.dispose();
		resourceManager.unloadAll();
	}

	@Override
	public void postRender(int delta) {
		whiteRectangle.setPosition(0, 0);
		whiteRectangle.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		whiteRectangle.setColor(0f, 0f, 0f, alpha);
		spriteBatch.begin();
		whiteRectangle.draw(spriteBatch);
		spriteBatch.end();
	}

	@Override
	public void internalUpdate(int delta) {
		super.internalUpdate(delta);
		Synchronizers.synchronize(delta);
	}
}