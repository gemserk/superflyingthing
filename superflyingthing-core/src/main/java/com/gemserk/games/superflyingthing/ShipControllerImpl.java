package com.gemserk.games.superflyingthing;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Vector2;
import com.gemserk.animation4j.interpolator.FloatInterpolator;
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
			return -value(Gdx.graphics.getWidth() * 0.5f, Gdx.input.getX(i), 0.2f, Gdx.graphics.getWidth() * 0.375f);
		}
		return 0f;
	}

	public float value(float center, int x, float minValue, float distanceToMax) {
		float distance = Math.abs(x - center);
		if (x > center)
			return FloatInterpolator.interpolate(minValue, 1f, distance / distanceToMax);
		else if (x < center)
			return FloatInterpolator.interpolate(-minValue, -1f, distance / distanceToMax);
		return 0;
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