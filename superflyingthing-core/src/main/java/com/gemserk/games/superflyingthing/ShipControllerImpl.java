package com.gemserk.games.superflyingthing;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Vector2;
import com.gemserk.commons.gdx.camera.Libgdx2dCamera;

public class ShipControllerImpl implements ShipController {

	private final Libgdx2dCamera libgdx2dCamera;

	private final Vector2 position;

	public ShipControllerImpl(Libgdx2dCamera libgdx2dCamera) {
		this.libgdx2dCamera = libgdx2dCamera;
		this.position = new Vector2(0f, 0f);
	}

	@Override
	public boolean isDown() {
		return Gdx.input.isTouched();
	}

	@Override
	public Vector2 getPosition() {
		position.set(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY());
		libgdx2dCamera.unproject(position);
		return position;
	}

	@Override
	public float getMovementDirection() {
		if (Gdx.app.getType() == ApplicationType.Android)
			return getMovementDirectionAndroid();
		else
			return getMovementDirectionPC();
	}

	private float getMovementDirectionPC() {
		return movementDirection;
	}

	private float getMovementDirectionAndroid() {

		for (int i = 0; i < 5; i++) {
			if (!Gdx.input.isTouched(i))
				continue;

			float x = Gdx.input.getX(i);
			float d = Gdx.graphics.getWidth() * 0.5f - x;
			d /= Gdx.graphics.getWidth() * 0.5f;

			if (d > 0 && d < 0.2f)
				d = 0.2f;

			if (d < 0 && d > -0.2f)
				d = -0.2f;

			if (d > 1f)
				d = 1f;

			if (d < -1f)
				d = -1f;

			return d;
		}

		return 0f;
	}

	float movementDirection = 0f;

	@Override
	public void update(int delta) {

		if (Gdx.input.isKeyPressed(Keys.LEFT)) {
			if (movementDirection < 0)
				movementDirection = 0f;
			movementDirection += 0.008f * delta;
		} else if (Gdx.input.isKeyPressed(Keys.RIGHT)) {
			if (movementDirection > 0)
				movementDirection = 0f;
			movementDirection -= 0.008f * delta;
		} else {
			movementDirection = 0f;
		}
		
		if (movementDirection > 1f)
			movementDirection = 1f;

		if (movementDirection < -1f)
			movementDirection = -1f;

	}

}