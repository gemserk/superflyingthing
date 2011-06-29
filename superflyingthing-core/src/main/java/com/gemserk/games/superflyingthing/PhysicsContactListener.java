package com.gemserk.games.superflyingthing;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.gemserk.commons.gdx.games.Physics;
import com.gemserk.games.entities.Entity;

public class PhysicsContactListener implements ContactListener {

	@Override
	public void beginContact(Contact contact) {
		Body bodyA = contact.getFixtureA().getBody();
		Body bodyB = contact.getFixtureB().getBody();

		Entity entityA = (Entity) bodyA.getUserData();
		Entity entityB = (Entity) bodyB.getUserData();

		if (entityA != null) {
			Physics physics = ComponentWrapper.getPhysics(entityA);
			physics.getContact().addContact(contact, bodyB);
		}

		if (entityB != null) {
			Physics physics = ComponentWrapper.getPhysics(entityB);
			physics.getContact().addContact(contact, bodyA);
		}
	}

	@Override
	public void endContact(Contact contact) {
		Body bodyA = contact.getFixtureA().getBody();
		Body bodyB = contact.getFixtureB().getBody();

		Entity entityA = (Entity) bodyA.getUserData();
		Entity entityB = (Entity) bodyB.getUserData();

		if (entityA != null) {
			Physics physics = ComponentWrapper.getPhysics(entityB);
			physics.getContact().removeContact(bodyB);
		}

		if (entityB != null) {
			Physics physics = ComponentWrapper.getPhysics(entityB);
			physics.getContact().removeContact(bodyA);
		}
	}
	
}