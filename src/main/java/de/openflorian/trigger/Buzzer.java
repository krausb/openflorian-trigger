package de.openflorian.trigger;

/*
 * This file is part of Openflorian.
 * 
 * Copyright (C) 2015  Bastian Kraus
 * 
 * Openflorian is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version)
 *     
 * Openflorian is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *     
 * You should have received a copy of the GNU General Public License
 * along with Openflorian.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
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

		boolean startBuzzer = false;
		String triggerDevice = null;
		String apiTriggerUrl = null;

		try {
			final CommandLine cLine = cParser.parse(options, arg);

			if (cLine.hasOption("c")) {
				log.info("Trying to load configuration from '" + cLine.getOptionValue("c") + "'...");
				final File configFile = new File(cLine.getOptionValue("c"));

				if (configFile.exists() && configFile.canRead()) {
					log.info("Configuration successfuly loaded.. trying to get configuration parameters...");
					final Properties prop = new Properties();
					prop.load(new FileInputStream(configFile));

					triggerDevice = String.valueOf(prop.get(CONF_TRIGGER_DEVICE));
					log.info("Value for '" + CONF_TRIGGER_DEVICE + "': " + triggerDevice);

					apiTriggerUrl = String.valueOf(prop.get(CONF_API_URL_TRIGGER));
					log.info("Value for '" + CONF_API_URL_TRIGGER + "': " + apiTriggerUrl);

					startBuzzer = true;
					log.info("Configuration loaded... Buzzer ready to start :-)");
				}
				else {
					throw new IOException("Requested config file '" + configFile.getAbsolutePath() + "' does not exist!");
				}
			}

		}
		catch (final ParseException e) {
			log.error(e.getMessage(), e);
		}
		catch (final IOException e) {
			log.error(e.getMessage(), e);
		}

		if (startBuzzer) {
			final Buzzer b = new Buzzer();

			b.setTriggerUrl(apiTriggerUrl);

			final Thread watchDogThread = new Thread(new WatchDog(triggerDevice, b));
			watchDogThread.setDaemon(true);
			watchDogThread.start();

			try {
				while (true)
					Thread.sleep(200);
			}
			catch (final Exception e) {
				log.error(e.getMessage(), e);
			}
		}
		else {
			final HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("java -jar slotmachine-trigger-<VERSION>.jar", options);
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
