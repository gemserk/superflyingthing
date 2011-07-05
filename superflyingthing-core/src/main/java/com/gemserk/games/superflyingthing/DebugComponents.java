package com.gemserk.games.superflyingthing;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.gemserk.games.superflyingthing.Components.MovementComponent;

public class DebugComponents {
	
	public static class MovementComponentDebugWindow extends JFrame {
		
		private MovementComponent movementComponent;
		
		public void setMovementComponent(MovementComponent movementComponent) {
			this.movementComponent = movementComponent;
		}
		
		public MovementComponentDebugWindow() {
			setName("Movement Component");
			setSize(320, 480);
			setVisible(true);
			setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			setLayout(new BorderLayout());
			add(new JLabel("Maxmimum Linear Speed"));
			add(new JSlider(100, 600) {
				{
					addChangeListener(new ChangeListener() {
						@Override
						public void stateChanged(ChangeEvent e) {
							if (movementComponent != null)
								movementComponent.setMaxAngularVelocity(getValue());
						}
					});
				}
			});
		}
		
	}
	
	private static MovementComponentDebugWindow movementComponentDebugWindow;
	
	public static MovementComponentDebugWindow getMovementComponentDebugWindow() {
		if (movementComponentDebugWindow == null)
			movementComponentDebugWindow = new MovementComponentDebugWindow();
		return movementComponentDebugWindow;
	}
	
}
