package com.gemserk.games.superflyingthing;

import java.awt.GridLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.artemis.Entity;
import com.artemis.World;
import com.gemserk.commons.artemis.components.ScriptComponent;
import com.gemserk.commons.artemis.scripts.Script;
import com.gemserk.games.superflyingthing.scripts.controllers.KeyboardControllerScript;
import com.gemserk.games.superflyingthing.templates.Groups;

public class DebugComponents {

	public static class MovementComponentDebugWindow extends JFrame {

		private World world;

		public MovementComponentDebugWindow() {
			setName("Ship Movement Component");
			setSize(480, 480);
			setVisible(true);
			setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			setLayout(new GridLayout(10, 1));
		}

		public void setWorld(World world) {
			this.world = world;

			Entity playerController = world.getTagManager().getEntity(Groups.PlayerController);
			if (playerController == null)
				return;

			ScriptComponent scriptComponent = playerController.getComponent(ScriptComponent.class);
			Script[] scripts = scriptComponent.getScripts();

			for (int i = 0; i < scripts.length; i++) {
				Script script = scripts[i];

				if (script instanceof KeyboardControllerScript) {
					confitureKeyboardController((KeyboardControllerScript) script);
					return;
				}

			}

		}

		private void confitureKeyboardController(final KeyboardControllerScript script) {
			// removeAll();

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
