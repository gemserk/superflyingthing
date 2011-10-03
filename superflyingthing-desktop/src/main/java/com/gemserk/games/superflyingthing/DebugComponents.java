package com.gemserk.games.superflyingthing;

import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.Field;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.artemis.Entity;
import com.artemis.World;
import com.badlogic.gdx.Gdx;
import com.gemserk.commons.artemis.events.Event;
import com.gemserk.commons.artemis.events.EventManager;
import com.gemserk.commons.artemis.events.reflection.EventListenerReflectionRegistrator;
import com.gemserk.commons.artemis.events.reflection.Handles;
import com.gemserk.games.superflyingthing.components.Components.MovementComponent;
import com.gemserk.games.superflyingthing.entities.Groups;
import com.gemserk.games.superflyingthing.gamestates.PlayGameState;
import com.gemserk.games.superflyingthing.scripts.controllers.KeyboardControllerScript;

public class DebugComponents {

	public static class MovementComponentDebugWindow extends JFrame {

		private PlayGameState playGameState;
		private World world;

		private JSlider maxLinearSpeedSlider;
		private JSlider maxAngularSpeedSlider;

		public MovementComponentDebugWindow() {
			setName("Ship Movement Component");
			setSize(480, 480);
			setVisible(true);
			setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			setLayout(new GridLayout(10, 1));

			addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosed(WindowEvent e) {
					super.windowClosed(e);
					DebugComponents.movementComponentDebugWindow = null;
				}

				@Override
				public void windowClosing(WindowEvent e) {
					super.windowClosing(e);
					DebugComponents.movementComponentDebugWindow = null;
				}
			});

			configureWindow();
		}

		public void setWorld(PlayGameState playGameState) {
			this.playGameState = playGameState;
			this.world = getWorld(playGameState);

			EventManager eventManager = getEventManager(playGameState);

			Gdx.app.log("SuperFlyingThing", "Registering controller window to game EventManager");

			new EventListenerReflectionRegistrator(eventManager).registerEventListeners(this);

		}

		private World getWorld(PlayGameState playGameState) {
			try {
				Field field = playGameState.getClass().getDeclaredField("world");
				field.setAccessible(true);
				return (World) field.get(playGameState);
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}

		private EventManager getEventManager(PlayGameState playGameState) {
			try {
				Field field = playGameState.getClass().getDeclaredField("eventManager");
				field.setAccessible(true);
				return (EventManager) field.get(playGameState);
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}

		private void configureWindow() {

			add(new JLabel("Ship - Maximum Linear Speed"));
			maxLinearSpeedSlider = new JSlider(0, 100, 40) {
				{
					addChangeListener(new ChangeListener() {
						@Override
						public void stateChanged(ChangeEvent e) {
							// script.minValue = 0.01f * ((float) getValue());
							setShipLinearSpeed(0.1f * (float) getValue());
						}
					});
					setMajorTickSpacing(100);
					setMinorTickSpacing(10);
					setPaintTicks(true);
					setPaintLabels(true);
				}
			};
			add(maxLinearSpeedSlider);
			
			add(new JLabel("Ship - Maximum Angular Speed"));
			maxAngularSpeedSlider = new JSlider(0, 720, 400) {
				{
					addChangeListener(new ChangeListener() {
						@Override
						public void stateChanged(ChangeEvent e) {
							setShipAngularSpeed((float) getValue());
						}
					});
					setMajorTickSpacing(100);
					setMinorTickSpacing(10);
					setPaintTicks(true);
					setPaintLabels(true);
				}
			};
			add(maxAngularSpeedSlider);

			validate();
		}

		@Handles(ids = Events.shipSpawned)
		public void setValuesToShipWhenSpawned(Event e) {
			// Entity shipEntity = world.getTagManager().getEntity(Groups.ship);
			Entity shipEntity = (Entity) e.getSource();
			if (shipEntity == null)
				return;
			Gdx.app.log("SuperFlyingThing", "Set debug control values to new spawned ship");

			float speed = 0.1f * (float) maxLinearSpeedSlider.getValue();
			float angularSpeed = (float) maxAngularSpeedSlider.getValue();

			MovementComponent movementComponent = shipEntity.getComponent(MovementComponent.class);
			movementComponent.setMaxLinearSpeed(speed);
			movementComponent.setMaxAngularVelocity(angularSpeed);
		}

		// void configureController(World world) {
		// Entity playerController = world.getTagManager().getEntity(Groups.PlayerController);
		// if (playerController == null)
		// return;
		//
		// ScriptComponent scriptComponent = playerController.getComponent(ScriptComponent.class);
		// Script[] scripts = scriptComponent.getScripts();
		//
		// for (int i = 0; i < scripts.length; i++) {
		// Script script = scripts[i];
		//
		// if (script instanceof KeyboardControllerScript) {
		// configureKeyboardController((KeyboardControllerScript) script);
		// return;
		// }
		//
		// }
		// }

		private void setShipLinearSpeed(float speed) {

			Entity shipEntity = world.getTagManager().getEntity(Groups.ship);
			if (shipEntity == null)
				return;

			// speed = 0.1f * (float) maxLinearSpeedSlider.getValue();
			// e.addComponent(new MovementComponent(1f, 0f, maxLinearSpeed, maxAngularVelocity));

			MovementComponent movementComponent = shipEntity.getComponent(MovementComponent.class);
			movementComponent.setMaxLinearSpeed(speed);

		}
		
		private void setShipAngularSpeed(float speed) {

			Entity shipEntity = world.getTagManager().getEntity(Groups.ship);
			if (shipEntity == null)
				return;

			// speed = 0.1f * (float) maxLinearSpeedSlider.getValue();
			// e.addComponent(new MovementComponent(1f, 0f, maxLinearSpeed, maxAngularVelocity));

			MovementComponent movementComponent = shipEntity.getComponent(MovementComponent.class);
			movementComponent.setMaxAngularVelocity(speed);

		}

		private void configureKeyboardController(final KeyboardControllerScript script) {
			removeAll();
			setLayout(new GridLayout(10, 1));

			add(new JSlider(0, 100, 50) {
				{
					addChangeListener(new ChangeListener() {
						@Override
						public void stateChanged(ChangeEvent e) {
							// script.minValue = 0.01f * ((float) getValue());
							setShipLinearSpeed(0.1f * (float) getValue());
						}
					});
					setMajorTickSpacing(100);
					setMinorTickSpacing(10);
					setPaintTicks(true);
					setPaintLabels(true);
				}
			});

			add(new JLabel("Keyboard Script values"));
			add(new JLabel("Min value - 0 to 100"));

			JSlider minValueSlider = new JSlider(0, 100, (int) (script.minValue * 100)) {
				{
					addChangeListener(new ChangeListener() {
						@Override
						public void stateChanged(ChangeEvent e) {
							script.minValue = 0.01f * ((float) getValue());
						}
					});
					setMajorTickSpacing(100);
					setMinorTickSpacing(10);
					setPaintTicks(true);
					setPaintLabels(true);
				}
			};
			add(minValueSlider);

			add(new JLabel("Speed - 0 to 100"));

			JSlider maxSpeedSlider = new JSlider(0, 100, (int) (script.speed * 10)) {
				{
					addChangeListener(new ChangeListener() {
						@Override
						public void stateChanged(ChangeEvent e) {
							script.speed = 0.1f * ((float) getValue());
						}
					});
					setMajorTickSpacing(100);
					setMinorTickSpacing(10);
					setPaintTicks(true);
					setPaintLabels(true);
				}
			};
			add(maxSpeedSlider);

		}

	}

	private static MovementComponentDebugWindow movementComponentDebugWindow;

	public static MovementComponentDebugWindow getMovementComponentDebugWindow() {
		if (movementComponentDebugWindow == null)
			movementComponentDebugWindow = new MovementComponentDebugWindow();
		return movementComponentDebugWindow;
	}

}
