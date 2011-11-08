package com.gemserk.games.superflyingthing;

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
import com.gemserk.commons.adwhirl.AdWhirlViewHandler;
import com.gemserk.commons.artemis.events.Event;
import com.gemserk.commons.artemis.events.EventListener;
import com.gemserk.commons.utils.BrowserUtilsDesktopImpl;
import com.gemserk.games.superflyingthing.DebugComponents.MovementComponentDebugWindow;
import com.gemserk.games.superflyingthing.gamestates.PlayGameState;
import com.gemserk.resources.monitor.FilesMonitor;
import com.gemserk.resources.monitor.FilesMonitorImpl;
import com.gemserk.resources.monitor.FilesMonitorNullImpl;

public class DesktopApplication {

	protected static final Logger logger = LoggerFactory.getLogger(DesktopApplication.class);
	
	private static class Arguments {

		int width = 800;
		int height = 480;

		public void parse(String[] argv) {
			if (argv.length == 0)
				return;

			String displayString = argv[0];
			String[] displayValues = displayString.split("x");

			if (displayValues.length < 2)
				return;

			try {
				width = Integer.parseInt(displayValues[0]);
				height = Integer.parseInt(displayValues[1]);
			} catch (NumberFormatException e) {
				System.out.println("error when parsing resolution from arguments: " + displayString);
			}

		}

	}

	public static void main(String[] argv) {

		System.out.println(System.getProperty("java.version"));

		AnalyticsConfigData analyticsConfig = new AnalyticsConfigData("UA-23542248-4");
		DesktopAnalyticsAutoConfigurator.populateFromSystem(analyticsConfig);
		Analytics.traker = new JGoogleAnalyticsTracker(analyticsConfig, GoogleAnalyticsVersion.V_4_7_2);

		FilesMonitor filesMonitor = new FilesMonitorNullImpl();
		String runningInDebug = System.getProperty("runningInDebug");
		if (runningInDebug != null) {
			logger.info("Running in debug mode, Analytics disabled");
			Analytics.traker.setEnabled(false);

			logger.info("Running in debug mode, File monitoring enabled");
			filesMonitor = new FilesMonitorImpl();
		}
		
		Arguments arguments = new Arguments();
		arguments.parse(argv);

		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		
		config.width = arguments.width;
		config.height = arguments.height;

		config.fullscreen = false;
		config.title = "Super Flying Thing";
		config.useGL20 = false;

		config.useCPUSynch = true;
		config.vSyncEnabled = true;

		config.forceExit = true;

		Game game = new Game() {
			@Override
			public void create() {
				// Gdx.graphics.setVSync(true);
				// Display.setVSyncEnabled(false);
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
		};
		
		game.setAdWhirlViewHandler(new AdWhirlViewHandler());
		game.setFilesMonitor(filesMonitor);
		game.setBrowserUtils(new BrowserUtilsDesktopImpl());
		
		new LwjglApplication(game, config);
	}

}
