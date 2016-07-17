package de.openflorian.trigger;

import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Buzzer Main (Executable)
 * 
 * Responsable for booting the Buzzer and the /dev/input/by-id/usb-*-kbd WatchDog Thread.
 * 
 * The class requests a configuration file which should be passed by commandline arg '-c <FILEPATH>'.
 * 
 * @author Bastian Kraus <bofh@k-hive.dee>
 */
public class Buzzer implements EventDelegate {

	/**
	 * Configuration property for Slotmachine API Trigger URL
	 */
	public static final String CONF_API_URL_TRIGGER = "slotmachine.api.trigger";

	/**
	 * Configuration property for trigger system device (usually /dev/input/event[0-9])
	 */
	public static final String CONF_TRIGGER_DEVICE = "slotmachine.trigger.device";

	/**
	 * Setter for Slotmachine API Trigger URL
	 * 
	 * @param url {@link String}
	 */
	public void setTriggerUrl(String o) {
		this.triggerUrl = o;
	}

	/**
	 * Getter for Slotmachine API Trigger URL
	 * 
	 * @return {@link String}
	 */
	public String getTriggerUrl() {
		return this.triggerUrl;
	}

	private String triggerUrl = null;

	private static Logger log = LoggerFactory.getLogger(Buzzer.class);

	/**
	 * Booting the Buzzer
	 * 
	 * @param arg
	 */
	public static void main(String[] arg) {
		log.info("Starting Trigger Finger!");

		final Options options = new Options();
		options.addOption("c", true, "Configuration file");

		final CommandLineParser cParser = new DefaultParser();

		final boolean startBuzzer = false;
		final String triggerDevice = BuzzerConfig.config().triggerDevice;
		final String apiTriggerUrl = BuzzerConfig.config().apiEndpoint;

		if (StringUtils.isEmpty(triggerDevice))
			throw new IllegalStateException("No trigger device set.");
		if (StringUtils.isEmpty(apiTriggerUrl))
			throw new IllegalStateException("No API URL set.");

		final Buzzer b = new Buzzer();

		b.setTriggerUrl(apiTriggerUrl);

		final Thread observerThread = new Thread(new BuzzerDeviceObserver(triggerDevice, b));
		observerThread.setDaemon(true);
		observerThread.start();

		try {
			while (true)
				Thread.sleep(200);
		}
		catch (final Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	@Override
	public void trigger() {
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
		final CloseableHttpClient hc = HttpClients.createDefault();
		// init connection
		final HttpPost post = new HttpPost(triggerUrl);
		// send post request
		hc.execute(post);
		log.info("Request successfuly sent to server! (URL: " + this.triggerUrl + ")");
	}

}
