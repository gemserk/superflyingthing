package com.gemserk.games.superflyingthing;

import java.awt.GridLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.gemserk.games.superflyingthing.Components.MovementComponent;

public class DebugComponents {

	public static class MovementComponentDebugWindow extends JFrame {

		private MovementComponent movementComponent;
		private JSlider maxAngularVelocitySlider;
		private JSlider minAngularVelocitySlider;
		private JSlider angularAcceleration;
		private JSlider maxLinearSpeedSlider;

		public void setMovementComponent(MovementComponent movementComponent) {
			this.movementComponent = movementComponent;
			movementComponent.setMaxAngularVelocity(maxAngularVelocitySlider.getValue());
			// movementComponent.setMinAngularVelocity(minAngularVelocitySlider.getValue());
			// movementComponent.setAngularAcceleration(0.01f * (float) angularAcceleration.getValue());
			movementComponent.maxLinearSpeed = 0.01f * (float) maxLinearSpeedSlider.getValue();
		}

		public MovementComponentDebugWindow() {
			setName("Ship Movement Component");
			setSize(480, 480);
			setVisible(true);
			setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			setLayout(new GridLayout(10, 1));

			add(new JLabel("Linear speed - max (mts per second)"));
			maxLinearSpeedSlider = new JSlider(100, 600, 500) {
				{
					addChangeListener(new ChangeListener() {
						@Override
						public void stateChanged(ChangeEvent e) {
							if (movementComponent != null)
								movementComponent.maxLinearSpeed = 0.01f * (float) getValue();
						}
					});
					setMajorTickSpacing(100);
					setMinorTickSpacing(10);
					setPaintTicks(true);
					setPaintLabels(true);
				}
			};
			add(maxLinearSpeedSlider);

			// add(new JLabel("Angular acceleration"));
			// angularAcceleration = new JSlider(0, 400, 70) {
			// {
			// addChangeListener(new ChangeListener() {
			// @Override
			// public void stateChanged(ChangeEvent e) {
			// if (movementComponent != null)
			// movementComponent.setAngularAcceleration(0.01f * (float) getValue());
			// }
			// });
			// setMajorTickSpacing(100);
			// setMinorTickSpacing(10);
			// setPaintTicks(true);
			// setPaintLabels(true);
			// }
			// };
			// add(angularAcceleration);
			//
			// add(new JLabel("Anglular velocity - min (degrees per second)"));
			// minAngularVelocitySlider = new JSlider(0, 600, 200) {
			// {
			// addChangeListener(new ChangeListener() {
			// @Override
			// public void stateChanged(ChangeEvent e) {
			// if (movementComponent != null)
			// movementComponent.setMinAngularVelocity(getValue());
			// }
			// });
			// setMajorTickSpacing(100);
			// setMinorTickSpacing(10);
			// setPaintTicks(true);
			// setPaintLabels(true);
			// }
			// };
			// add(minAngularVelocitySlider);

			add(new JLabel("Anglular velocity - max (degrees per second)"));
			maxAngularVelocitySlider = new JSlider(0, 600, 300) {
				{
					addChangeListener(new ChangeListener() {
						@Override
						public void stateChanged(ChangeEvent e) {
							if (movementComponent != null)
								movementComponent.setMaxAngularVelocity(getValue());
						}
					});
					setMajorTickSpacing(100);
					setMinorTickSpacing(10);
					setPaintTicks(true);
					setPaintLabels(true);
				}
			};
			add(maxAngularVelocitySlider);

		}

	}

	private static MovementComponentDebugWindow movementComponentDebugWindow;

	public static MovementComponentDebugWindow getMovementComponentDebugWindow() {
		if (movementComponentDebugWindow == null)
			movementComponentDebugWindow = new MovementComponentDebugWindow();
		return movementComponentDebugWindow;
	}

}
