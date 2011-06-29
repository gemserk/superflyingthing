package com.gemserk.tutorial.datadriven;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.gemserk.tutorial.datadriven.DataDrivenTest.Game.MovementComponent;

public class DataDrivenTest {

	/**
	 * First of all, I want to introduce a bit Data Driven design.
	 * 
	 * The main concept is, different data implies different behavior, it means -> Data drives logic.
	 * 
	 * How?
	 * 
	 * Having or not different stuff, for example, a game character having Health (commonly named a Component of health), could die.
	 * 
	 * Want to make him immortal, then change the Health for a Immune component, or add an Immune component, then you have some logic dependent of having or not those two.
	 * 
	 * Was it clear? I will try to make an example of how to develop something like this.... from Zero.
	 * 
	 * I will be making a game where you have a ship who travels from one point to another avoiding obstacles, it could die if it hits one but could become immortal if it get an specific item.
	 * 
	 * Concepts for now: Ship, Obstacle, ImmortalityItem, Destination.
	 * 
	 * Until now, I have a ship that moves across the world, if it gets an obstacle it dies, if it gets an invulneravility item it becomes invulnerable and doesn't die any more.
	 * 
	 * Nice, now I have also a destination, if the ship reaches the destination, the game should end, now the game ends when the ship gets the destination :D
	 * 
	 * So, I will try to improve a bit the design of the game by extracting common stuff.
	 * 
	 * Well, I extracted an Entity class with common functionality between the concepts and abstracted them on the game class, but now I have the problem of having to cast:
	 * 
	 * if (!(entity instanceof InvulneravilityItem)) ...
	 * 
	 * what is the difference of an Entity Ship and an Entity InvulnerableItem?
	 * 
	 * As you are starting to see, we can extract common Ship behaviors as separated classes which uses some values from the Ship, we will go one step further later.
	 * 
	 * For now, behaviors are Ship's dependent, cannot be used on other entities. Maybe, if we change a bit how are we storing data on Entities, we could reuse behaviors on other entities.
	 * 
	 * what is the difference now of an Obstacle entity and a Ship entity? only the components and behaviors they have. So we could define them outside.
	 * 
	 */

	/**
	 * Try encapsulating the component data and treat all components as the same class, registering them with a name...
	 * 
	 * something like this:
	 * 
	 * interface Health {
	 * 
	 * float current, total;
	 * 
	 * isAlive() : boolean
	 * 
	 * }
	 * 
	 * class Component {
	 * 
	 * private Object object;
	 * 
	 * <T> T get() { if (object == null) return null; return (T) object; }
	 * 
	 * }
	 * 
	 * Then on the game ->
	 * 
	 * Health health = entity.getComponent("health").get();
	 * 
	 * going further, we could create a custom wrapper for our game:
	 * 
	 * class ComponentWrapper {
	 * 
	 * static <T> T getHealth(e) { return e.getComponent("health").get(); }
	 * 
	 * }
	 * 
	 * Health health = ComponentWrapper.getHealth(e);
	 * 
	 * Same for other components.
	 * 
	 * So Health ends up being the real component and Component only a holder.
	 * 
	 * With this approach, how to make stuff like "if e has HealthComponent then ..."?
	 * 
	 * I could assume health component is registered with "health" string (or some other key type), and only that, if a sprite component is registered with that name, then it will fail.
	 * 
	 */

	class Entity {

		// This will allow only one component per class, another option is to use strings as keys.

		Map<String, Object> components;

		ArrayList<Behavior> behaviors;

		<T> T getComponent(Class<T> clazz) {
			return (T) components.get(clazz.getName());
		}

		public Entity() {
			components = new HashMap<String, Object>();
			behaviors = new ArrayList<Behavior>();
		}

		void addComponent(Object component) {
			components.put(component.getClass().getName(), component);
		}

		void addBehavior(Behavior behavior) {
			behaviors.add(behavior);
		}

		void init() {

		}

		void update(int delta) {
			for (int i = 0; i < behaviors.size(); i++)
				behaviors.get(i).update(delta, this);
		}

		void dispose() {

		}

	}

	/**
	 * Defines a behavior an Entity should have, for example, the behavior of moving across the screen.
	 */
	interface Behavior {

		void update(int delta, Entity entity);

	}
	
	static class ComponentWrapper {

		static <T> T get(Entity e, Class<T> clazz) {
			return (T) e.getComponent(clazz);
		}
		
		static MovementComponent getMovement(Entity e) {
			return e.getComponent(MovementComponent.class);
		}
		
	}

	class Game {
		
		/**
		 * Now both behaviors have a common interface, they are Ship behaviors, given a ship and a delta time they do something.
		 * 
		 * One thing we have to take care on behaviors is to do not do anything if the component is not in the entity.
		 * 
		 * Now that I have all behaviors implementing the same interface, I could put them in a collection on the entity class.
		 * 
		 * BEHAVIORS (LOGIC) ->
		 */

		class InputBehavior implements Behavior {

			/**
			 * This one calculates, given an input (keyboard, mouse, touch), the current ship direction. As you can see, this behavior depends only on the ship's direction.
			 */

			public void update(int delta, Entity entity) {
				MovementComponent movement = ComponentWrapper.getMovement(entity);
				if (movement == null)
					return;
				// process input in your own way to get movement direction.
				movement.direction.set(1f, 0f);
			}

		}

		/**
		 * This behavior depends on the ship's speed, direction and position.
		 * 
		 * As you can see, now this behavior depends on Spatial Component, not directly on the position, lets get one step further in this approach.
		 * 
		 * Nice, now it depends on Spatial and Movement Components only.
		 */

		class MovementBehavior implements Behavior {

			// It needs to access a property of the entity....

			// One option is to add the property as a parameter, we will see another option later.

			public void update(int delta, Entity entity) {
				MovementComponent movement = ComponentWrapper.get(entity, MovementComponent.class);
				SpatialComponent spatial = ComponentWrapper.get(entity, SpatialComponent.class);

				/**
				 * We will see later that this could be controlled outside this class, so whenever we receive the update is because we have all components we need.
				 */
				if (movement == null || spatial == null)
					return;

				// updates position using whatever you want, maybe a physics engine.
				// only to denote it is using ship speed for some calculations
				float realSpeed = movement.speed * delta * 0.001f;
				spatial.position.add(movement.direction.tmp().nor().mul(realSpeed));
			}

		}

		/**
		 * Using the same common API of the Behaviors, I create a new one for the collisions
		 */

		class CollisionBehavior implements Behavior {

			public void update(int delta, Entity entity) {
				InvulneravilityComponent invulneravility = ComponentWrapper.get(entity, InvulneravilityComponent.class);
				HealthComponent health = ComponentWrapper.get(entity, HealthComponent.class);

				if (invulneravility == null || health == null)
					return;

				if (invulneravility.invulnerable)
					return;

				if (!collidingWithObstacle(entity))
					return;

				health.dead = true;
			}

			private boolean collidingWithObstacle(Entity entity) {
				BoundingComponent entityBoundingComponent = ComponentWrapper.get(entity, BoundingComponent.class);
				if (entityBoundingComponent == null)
					return false;

				for (int i = 0; i < entities.size(); i++) {
					Entity e = entities.get(i);
					if (e == entity)
						continue;
					BoundingComponent boundingComponent = ComponentWrapper.get(e, BoundingComponent.class);
					if (boundingComponent == null)
						continue;

					Rectangle boundingA = entityBoundingComponent.rectangle;
					Rectangle boundingB = boundingComponent.rectangle;

					// check and return if bounding A is colliding with bounding B

				}

				return false;
			}

		}

		class GetItemBehavior implements Behavior {

			public void update(int delta, Entity e) {
				if (entities.isEmpty())
					return;

				InvulneravilityComponent invulneravility = ComponentWrapper.get(e, InvulneravilityComponent.class);
				if (invulneravility == null)
					return;

				for (int i = 0; i < entities.size(); i++) {
					Entity entity = entities.get(i);
					ItemComponent itemComponent = ComponentWrapper.get(entity, ItemComponent.class);
					if (itemComponent == null)
						continue;
					if (itemComponent.used)
						continue;
					if (!overInvulneravilityItem(entity, e))
						continue;
					itemComponent.used = true;
					invulneravility.invulnerable = true;
				}
			}

			private boolean overInvulneravilityItem(Entity entity, Entity e) {
				// calculate in some way if the ship is over the item to get it
				// use Spatials and Boundings maybe to calculate if they are in contact.
				return false;
			}

		}

		class DetectsShipOnDestinationBehavior implements Behavior {

			@Override
			public void update(int delta, Entity entity) {
				// NOTE it uses ship instance from Game, we could make this component more abstract later.
				if (!shipOverDestination(ship))
					return;
				ReachedComponent reachedComponent = ComponentWrapper.get(entity, ReachedComponent.class);
				reachedComponent.reached = true;
			}

			private boolean shipOverDestination(Entity entity) {
				// calculates the ship is over the destination in some way, maybe using box2d sensors or whatever you want to use.
				return false;
			}

		}

		/**
		 * Well, we have all behaviors separated, lets separate data in classes too.
		 * 
		 * COMPONENTS (DATA) ->
		 */

		class SpatialComponent {

			Vector2 position;

			public SpatialComponent(float x, float y) {
				this.position = new Vector2(x, y);
			}

		}

		class MovementComponent {

			Vector2 direction;
			float speed; // in meters per second

			public MovementComponent(float dx, float dy, float speed) {
				this.direction = new Vector2(dx, dy);
				this.speed = speed;
			}

		}

		class HealthComponent {

			boolean dead;

		}

		class InvulneravilityComponent {

			boolean invulnerable;

		}

		class BoundingComponent {

			Rectangle rectangle;

			public BoundingComponent(Rectangle rectangle) {
				this.rectangle = rectangle;
			}

		}

		class ReachedComponent {

			boolean reached;

		}

		class ItemComponent {

			boolean used;

		}

		/**
		 * Well, now, we have all data in components, as you can see, you could interpret in an easy way how is the Ship composed, it has an spatial, a movement, health and invulneravility.
		 * 
		 * Also, we know it has the input behavior, the movement behavior, the collision behavior and the get item behavior. Lets get one step further.
		 * 
		 * We will move all the components in a list/map
		 * 
		 * So, we changed all components to be in a map, and to be a bit independant if the entity is a ship or not, so we want to move the getComponent/addComponent to Entity class.
		 * 
		 * Now behaviors depend only on components of a given entity, not on directly on the Ship.
		 * 
		 * Nice huh? now all behaviors are ship independent and can be named without the Ship prefix.
		 * 
		 * Now lets get all behaviors in one collection.
		 * 
		 * As you can see now, init method is more like a description of an entity, I want an entity which should
		 * 
		 * - be able to move over the place and
		 * 
		 * - be able to get items if it is over them and become invulnerable
		 * 
		 * - die if it hits an obstacle, etc
		 * 
		 * I added a BounidingComponent to the ship so it can calculate on CollisionBehavior if it is in contact with other entities.
		 * 
		 * ENTITIES ->
		 */

		/**
		 * As you can see now, all entities have the same structure, the only difference between them is the components and behaviors they have.
		 * 
		 * Also, you can see that Components are part of the Entity, they describes it and are attached to the Entity life cycle.
		 * 
		 * However, behaviors are not, they are entity independent, they could work over any entity if it has the correct components the behavior needs.
		 * 
		 * The next step now is to remove each Entity, Ship, Obstacle, Destination and InvulnerableItem, by creating a Factory that will create them by specifying the correct
		 * 
		 * components and behaviors.
		 * 
		 * Like it? what is the cool part of this? I can now create different ships in an easy way, without having to create a lot of classes.
		 * 
		 * TADA!! we have now all entities here, with no classes, we could create entities even from an XML or other format now :D
		 * 
		 * As I said before, behaviors are independent of the Entity, so we could extract them.
		 */

		class EntityFactory {

			public Entity ship() {
				Entity entity = new Entity();
				entity.addComponent(new SpatialComponent(0f, 0f));
				entity.addComponent(new MovementComponent(1f, 0f, 5f));
				entity.addComponent(new HealthComponent());
				entity.addComponent(new InvulneravilityComponent());
				entity.addComponent(new BoundingComponent(new Rectangle(-1f, -1f, 2f, 2f)));

				entity.addBehavior(new InputBehavior());
				entity.addBehavior(new MovementBehavior());
				entity.addBehavior(new CollisionBehavior());
				entity.addBehavior(new GetItemBehavior());

				return entity;
			}

			// This ship can't become invulnerable and it can't get items :D
			public Entity ship2() {
				Entity entity = new Entity();
				entity.addComponent(new SpatialComponent(0f, 0f));
				entity.addComponent(new MovementComponent(1f, 0f, 5f));
				entity.addComponent(new HealthComponent());
				entity.addComponent(new BoundingComponent(new Rectangle(-1f, -1f, 2f, 2f)));

				entity.addBehavior(new InputBehavior());
				entity.addBehavior(new MovementBehavior());
				entity.addBehavior(new CollisionBehavior());

				return entity;
			}

			public Entity obstacle() {
				Entity entity = new Entity();
				entity.addComponent(new BoundingComponent(new Rectangle(-10f, -10f, 20f, 20f)));
				entity.addComponent(new SpatialComponent(30f, 30f));
				entity.addComponent(new MovementComponent(0f, -1f, 0.1f));
				entity.addBehavior(new MovementBehavior());
				return entity;
			}

			public Entity destination() {
				Entity entity = new Entity();
				entity.addComponent(new SpatialComponent(150f, 0f));
				entity.addComponent(new ReachedComponent());
				entity.addBehavior(new DetectsShipOnDestinationBehavior());
				return entity;
			}

			public Entity invulnerableItem() {
				Entity entity = new Entity();
				entity.addComponent(new SpatialComponent(150f, 0f));
				entity.addComponent(new ItemComponent());
				return entity;
			}

		}

		boolean done;

		ArrayList<Behavior> behaviors;
		ArrayList<Entity> entities;
		Entity destination;
		Entity ship;

		EntityFactory entityFactory = new EntityFactory();

		void init() {
			destination = entityFactory.destination();
			ship = entityFactory.ship();

			entities = new ArrayList<Entity>();
			entities.add(ship);
			entities.add(entityFactory.obstacle());
			entities.add(entityFactory.invulnerableItem());
			entities.add(destination);

			for (int i = 0; i < entities.size(); i++)
				entities.get(i).init();

			// behaviors = new ArrayList<Behavior>();
			// behaviors.add(new InputBehavior());
		}

		void update(int delta) {
			for (int i = 0; i < entities.size(); i++)
				entities.get(i).update(delta);
			ReachedComponent reachedComponent = ComponentWrapper.get(destination, ReachedComponent.class);
			if (reachedComponent.reached)
				done = true;
		}

		void dispose() {
			for (int i = 0; i < entities.size(); i++)
				entities.get(i).dispose();
		}

	}

	private int getDeltaTime() {
		// calculate delta in your own way using System.currentTime() or whatever.
		return 16;
	}

	@Test
	public void mainTest() {
		// Game game = new Game();
		// game.init();
		// while (!game.done)
		// game.update(getDeltaTime());
		// game.dispose();
	}

}
