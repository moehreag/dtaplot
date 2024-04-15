package io.github.moehreag.dtaplot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

	static {
		System.setProperty("org.slf4j.simpleLogger.logFile", "System.out");
		if (Constants.NAME.contains("Dev")){
			System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "DEBUG");
		}
	}

	public static void init(){
		/*
			Class loading no-op
		*/
	}

	public static void main(String[] args) {
		Logger log = LoggerFactory.getLogger("DtaPlot");
		log.info("Invoke a GUI Entrypoint directly to use the program!");
		log.info("If this invocation was unintentional this indicates a bug in the program, please report it!");
	}
}