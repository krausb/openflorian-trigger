package de.openflorian.trigger;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;

/**
 * Buzzer Main (Executable)
 * 
 * Responsable for booting the Buzzer and the /dev/input/by-id/usb-*-kbd WatchDog Thread.
 * 
 * The class requests a configuration file which should be passed by commandline arg '-c <FILEPATH>'.
 * 
 * @author Bastian Kraus <bofh@k-hive.dee>
 */
public class Buzzer extends AbstractVerticle {
	private static Logger log = LoggerFactory.getLogger(Buzzer.class);

	/**
	 * Configuration property for Slotmachine API Trigger URL
	 */
	public static final String CONF_API_URL_TRIGGER = "slotmachine.api.trigger";

	/**
	 * Configuration property for trigger system device (usually /dev/input/event[0-9])
	 */
	public static final String CONF_TRIGGER_DEVICE = "slotmachine.trigger.device";

	public static final String BUZZER_EVENTBUS_ADDRESS = "Buzzer.triggerApi";

	private final String triggerUrl;

	private final String triggerDevice;

	public Buzzer() {
		this.triggerDevice = BuzzerConfig.config().triggerDevice;
		this.triggerUrl = BuzzerConfig.config().apiEndpoint;

		if (StringUtils.isEmpty(triggerDevice))
			throw new IllegalStateException("No trigger device set.");
		if (StringUtils.isEmpty(triggerUrl))
			throw new IllegalStateException("No API URL set.");
	}

	/**
	 * Booting the Buzzer
	 * 
	 * @param arg
	 */
	public static void main(String[] arg) {
		log.info("Starting Trigger Finger ;-) (I I follow... i follow you... ;-)");

		final Vertx vertx = Vertx.vertx();
		vertx.deployVerticle(new Buzzer());
	}

	@Override
	public void start() throws Exception {
		log.info("Deploy Buzzer Verticle...");

		vertx.eventBus().consumer(BUZZER_EVENTBUS_ADDRESS, message -> trigger(message));

		vertx.deployVerticle(new BuzzerDeviceObserverVerticle(this.triggerDevice),
				new DeploymentOptions().setWorker(true));
	}

	private void trigger(Message<Object> msg) {
		try {
			log.info("Trigger pushed :-)");
			sendToServer();
		}
		catch (final Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	/**
	 * Helper: Send a POST request without body to the in the configuration property {@link Buzzer#CONF_API_URL_TRIGGER}
	 * defined URL.
	 * 
	 * @throws Exception
	 */
	private synchronized void sendToServer() throws Exception {
		try (final CloseableHttpClient hc = HttpClients.createDefault();) {
			// init connection
			final HttpDelete deleteRequest = new HttpDelete(triggerUrl);
			// send post request
			hc.execute(deleteRequest);
			log.info("Request successfuly sent to server! (URL: " + this.triggerUrl + ")");
		}
		catch (final Exception e) {
			log.error(e.getMessage(), e);
		}
	}

}
