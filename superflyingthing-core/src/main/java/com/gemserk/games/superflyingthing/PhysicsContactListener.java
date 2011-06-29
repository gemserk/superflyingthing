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

		addBodyToContacts(entityA, bodyB, contact);
		addBodyToContacts(entityB, bodyA, contact);
	}

	/**
	 * Adds the body to the entity contacts.
	 * 
	 * @param e
	 *            The entity to add the contact to.
	 * @param body
	 *            The body to add to the contacts.
	 * @param contact
	 *            The real contact, used internally to get some data like normals and stuff.
	 */
	private void addBodyToContacts(Entity e, Body body, Contact contact) {
		if (e != null) {
			Physics physics = ComponentWrapper.getPhysics(e);
			if (physics != null)
				physics.getContact().addContact(contact, body);
		}
	}

	@Override
	public void endContact(Contact contact) {
		Body bodyA = contact.getFixtureA().getBody();
		Body bodyB = contact.getFixtureB().getBody();

		Entity entityA = (Entity) bodyA.getUserData();
		Entity entityB = (Entity) bodyB.getUserData();

		removeBodyFromContacts(entityA, bodyB);
		removeBodyFromContacts(entityB, bodyA);
	}

	/**
	 * Removes body from entity contacts.
	 * 
	 * @param e
	 *            The entity to remove the contact from.
	 * @param body
	 *            The body to be removed from contacts.
	 */
	private void removeBodyFromContacts(Entity e, Body body) {
		if (e == null)
			return;
		Physics physics = ComponentWrapper.getPhysics(e);
		if (physics == null)
			return;
		physics.getContact().removeContact(body);
	}

}