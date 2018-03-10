package com.pk.eqa.jira;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadProperty {
	private static java.util.Properties jiraProps;

	private static final Logger log = LoggerFactory.getLogger(ReadProperty.class);

	public ReadProperty() {

	}

	static {
		FileInputStream in = null;
		jiraProps = new java.util.Properties();
		try {
			final File propertiesFile = new File("src/main/resources/jira.properties");
			if (propertiesFile.exists()) {
				in = new FileInputStream(propertiesFile);
				jiraProps.load(in);
			} else {
				log.info("Failed reading jira.properties");
				System.exit(1);
			}
		} catch (IOException ioe) {
			log.info(ioe.getMessage() + ioe);
		}
	}

	public static String getJiraProperty(String key) {
		return jiraProps.getProperty(key);
	}

}
