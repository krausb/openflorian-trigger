package de.openflorian.trigger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AbstractVerticle;

/**
 * Trigger Pushed WatchDog<br/>
 * <br/>
 * WatchDog opens
 * 
 * @author Bastian Kraus <bofh@k-hive.de>
 */
public class BuzzerDeviceObserverVerticle extends AbstractVerticle {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final String triggerDevice;

	private static Long timerId = null;

	private final Executor threadPool = Executors.newSingleThreadExecutor();

	public BuzzerDeviceObserverVerticle(String triggerDevice) {
		this.triggerDevice = triggerDevice;
	}

	@Override
	public void start() throws Exception {
		log.info("Deploy BuzzerDeviceObserverVerticle ...");

		threadPool.execute(() -> {
			BufferedReader in = null;
			try {
				log.debug("Opening device '" + this.triggerDevice + "' for key logging...");
				in = new BufferedReader(new FileReader(this.triggerDevice));

				final char[] b = new char[125];
				while (in.read(b, 0, 125) != -1) {
					if (BuzzerDeviceObserverVerticle.timerId == null) {
						timerId = vertx.setTimer(3000, tId -> {
							log.debug("Trigger event finished :-) Propagating to delegate...");
							vertx.eventBus().send(Buzzer.BUZZER_EVENTBUS_ADDRESS, "trigger");
							BuzzerDeviceObserverVerticle.timerId = null;
						});
						log.debug("Trigger event start...");
					}
					Thread.sleep(100);
				}
			}
			catch (final Exception e) {
				log.error(e.getMessage(), e);
			}
			finally {
				try {
					if (in != null)
						in.close();
				}
				catch (final IOException e) {
					log.error(e.getMessage(), e);
				}
			}
		});

		log.info("... BuzzerDeviceObserverVerticle deployed!");
	}
}
