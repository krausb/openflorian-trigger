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
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * JSON Configuration Wrapper for Openflorian
 * 
 * @author Bastian Kraus <bofh@k-hive.de>
 */
public class BuzzerConfig {

	private static final Logger log = LoggerFactory.getLogger(BuzzerConfig.class);

	public static final String CONFIG_FILE_PARAM = "configfile";

	private static final String CONFIG_FILE = "/config.json";

	private static Config instance = null;

	public synchronized static Config config() {
		if (instance == null) {
			InputStream fis = null;

			try {
				final String externalConfig = System.getProperty(CONFIG_FILE_PARAM);
				if (externalConfig != null && !externalConfig.isEmpty()) {
					final File externalConfigFile = new File(externalConfig);
					log.info("Use external config: " + externalConfigFile.getAbsolutePath());
					fis = new FileInputStream(externalConfigFile);
				}
				else {
					log.info("Use internal config: " + CONFIG_FILE);
					fis = BuzzerConfig.class.getResourceAsStream(CONFIG_FILE);
				}
				final ObjectMapper mapper = new ObjectMapper();
				instance = mapper.readValue(fis, Config.class);
			}
			catch (final Exception e) {
				log.error(e.getMessage(), e);
				throw new IllegalStateException(e);
			}
		}
		return instance;
	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static class Config {
		@JsonProperty("trigger_device")
		public String triggerDevice;
		@JsonProperty("api_endpoint")
		public String apiEndpoint;
	}

}