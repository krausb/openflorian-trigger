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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Trigger Pushed WatchDog<br/>
 * <br/>
 * WatchDog opens
 * 
 * @author Bastian Kraus <bofh@k-hive.de>
 */
public class WatchDog implements Runnable {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final EventDelegate delegate;
	private String triggerDevice;

	public WatchDog(String triggerDevice, EventDelegate delegate) {
		this.delegate = delegate;
	}

	@Override
	public void run() {
		BufferedReader in = null;
		try {
			log.debug("Opening device '" + this.triggerDevice + "' for key logging...");
			in = new BufferedReader(new FileReader(this.triggerDevice));

			int cycle = 0;
			final char[] b = new char[125];
			while (in.read(b, 0, 125) != -1) {
				if (cycle == 1) {
					log.debug("Trigger event finished :-) Propagating to delegate...");
					delegate.trigger();
					cycle = 0;
				}
				else {
					log.debug("Trigger event start...");
					cycle++;
				}
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

	}
}
