package com.gemserk.games.superflyingthing.scripts;

import com.artemis.Entity;
import com.gemserk.animation4j.gdx.Animation;
import com.gemserk.commons.artemis.components.SpriteComponent;
import com.gemserk.commons.artemis.scripts.ScriptJavaImpl;
import com.gemserk.commons.artemis.templates.EntityFactory;
import com.gemserk.commons.artemis.templates.EntityTemplate;
import com.gemserk.commons.gdx.GlobalTime;
import com.gemserk.componentsengine.utils.Parameters;
import com.gemserk.componentsengine.utils.ParametersWrapper;
import com.gemserk.games.superflyingthing.components.ComponentWrapper;
import com.gemserk.games.superflyingthing.components.Components.AnimationComponent;
import com.gemserk.games.superflyingthing.components.Components.WeaponComponent;

public class LaserGunScript extends ScriptJavaImpl {

	private final EntityFactory entityFactory;

	private final Parameters bulletParameters;

	public LaserGunScript(EntityFactory entityFactory) {
		this.entityFactory = entityFactory;
		this.bulletParameters = new ParametersWrapper();
	}

	@Override
	public void update(com.artemis.World world, Entity e) {
		WeaponComponent weaponComponent = ComponentWrapper.getWeaponComponent(e);
		float reloadTime = weaponComponent.getReloadTime();
		reloadTime -= GlobalTime.getDelta();

		if (reloadTime <= 0) {

			SpriteComponent spriteComponent = ComponentWrapper.getSpriteComponent(e);

			bulletParameters.put("owner", e);
			bulletParameters.put("x", 0.5f);
			bulletParameters.put("damage", 3000f);
			bulletParameters.put("duration", (int) (weaponComponent.getBulletDuration() * 1000));
			bulletParameters.put("color", spriteComponent.getColor());

			EntityTemplate bulletTemplate = weaponComponent.getBulletTemplate();
			entityFactory.instantiate(bulletTemplate, bulletParameters);

			reloadTime += weaponComponent.getFireRate();
		}
		weaponComponent.setReloadTime(reloadTime);

		AnimationComponent animationComponent = ComponentWrapper.getAnimationComponent(e);
		Animation currentAnimation = animationComponent.getCurrentAnimation();
		currentAnimation.update(GlobalTime.getDelta());

		SpriteComponent spriteComponent = ComponentWrapper.getSpriteComponent(e);
		spriteComponent.setSprite(currentAnimation.getCurrentFrame());
	}

}