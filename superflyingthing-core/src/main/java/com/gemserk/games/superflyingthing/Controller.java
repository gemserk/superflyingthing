package com.gemserk.games.superflyingthing;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Vector2;
import com.gemserk.commons.gdx.camera.Libgdx2dCamera;

public class Controller {
	
	private final Libgdx2dCamera libgdx2dCamera;
	
	private final Vector2 position;

	public Controller(Libgdx2dCamera libgdx2dCamera) {
		this.libgdx2dCamera = libgdx2dCamera;
		this.position = new Vector2(0f, 0f);
	}
	
	public boolean isDown() {
		return Gdx.input.isTouched();
	}
	
	public Vector2 getPosition() {
		position.set(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY());
		libgdx2dCamera.unproject(position);
		return position;
	}

	public float getMovementDirection() {
		if (Gdx.app.getType() == ApplicationType.Android)
			return getMovementDirectionAndroid();
		else
			return getMovementDirectionPC();
	}

	private float getMovementDirectionPC() {
		float movementDirection = 0f;

		if (Gdx.input.isKeyPressed(Keys.LEFT))
			movementDirection += 1f;

		if (Gdx.input.isKeyPressed(Keys.RIGHT))
			movementDirection -= 1f;

		return movementDirection;
	}

	private float getMovementDirectionAndroid() {
		float movementDirection = 0f;

		for (int i = 0; i < 5; i++) {
			if (!Gdx.input.isTouched(i))
				continue;
			float x = Gdx.input.getX(i);
			if (x < Gdx.graphics.getWidth() / 2)
				movementDirection += 1f;
			else
				movementDirection -= 1f;
		}

		return movementDirection;
	}

}