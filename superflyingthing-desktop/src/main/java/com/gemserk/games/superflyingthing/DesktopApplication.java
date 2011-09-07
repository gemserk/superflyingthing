package com.gemserk.games.superflyingthing;

import org.lwjgl.opengl.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.input.RemoteInput;
import com.dmurph.tracking.AnalyticsConfigData;
import com.dmurph.tracking.JGoogleAnalyticsTracker;
import com.dmurph.tracking.JGoogleAnalyticsTracker.GoogleAnalyticsVersion;
import com.gemserk.analytics.Analytics;
import com.gemserk.analytics.googleanalytics.DesktopAnalyticsAutoConfigurator;
import com.gemserk.commons.artemis.events.Event;
import com.gemserk.commons.artemis.events.EventListener;
import com.gemserk.games.superflyingthing.DebugComponents.MovementComponentDebugWindow;
import com.gemserk.games.superflyingthing.gamestates.PlayGameState;

public class DesktopApplication {

	protected static final Logger logger = LoggerFactory.getLogger(DesktopApplication.class);

	public static void main(String[] argv) {

		System.out.println(System.getProperty("java.version"));

		AnalyticsConfigData analyticsConfig = new AnalyticsConfigData("UA-23542248-4");
		DesktopAnalyticsAutoConfigurator.populateFromSystem(analyticsConfig);
		Analytics.traker = new JGoogleAnalyticsTracker(analyticsConfig, GoogleAnalyticsVersion.V_4_7_2);

		String runningInDebug = System.getProperty("runningInDebug");
		if (runningInDebug != null) {
			logger.info("Running in debug mode, Analytics disabled");
			Analytics.traker.setEnabled(false);
		}

		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = 800;
		config.height = 480;

		// config.width = 320;
		// config.height = 240;
		// config.width = 480;
		// config.height = 320;
		// config.width = 1024;
		// config.height = 768;

		config.fullscreen = false;
		config.title = "Super Flying Thing";
		config.useGL20 = false;
		config.useCPUSynch = true;
		config.forceExit = true;

		new LwjglApplication(new Game() {
			@Override
			public void create() {
				// Gdx.graphics.setVSync(true);
				Display.setVSyncEnabled(false);
				String remoteInput = System.getProperty("remoteInput");
				if (remoteInput != null)
					Gdx.input = new RemoteInput(8190);
				super.create();

				getEventManager().register(Events.showCustomizeControls, new EventListener() {
					@Override
					public void onEvent(Event event) {
						PlayGameState playGameState = (PlayGameState) event.getSource();
						MovementComponentDebugWindow movementComponentDebugWindow = DebugComponents.getMovementComponentDebugWindow();
						// if (movementComponentDebugWindow == null)
						// MovementComponentDebugWindow movementComponentDebugWindow = new MovementComponentDebugWindow();
						movementComponentDebugWindow.setWorld(playGameState);
					}
				});

			}

		}, config);
	}

}
