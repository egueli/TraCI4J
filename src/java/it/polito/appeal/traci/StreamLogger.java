package it.polito.appeal.traci;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.logging.log4j.Logger;

/**
 * Reads an InputStream object and logs each row in the containing class's
 * logger.
 * 
 * @author Enrico
 * 
 */
public class StreamLogger implements Runnable {
	private final InputStream stream;
	private final String prefix;
	private final Logger logger;
	
	StreamLogger(InputStream stream, String prefix, Logger logger) {
		this.stream = stream;
		this.prefix = prefix;
		this.logger = logger;
	}

	public void run() {
		StringBuilder buf = new StringBuilder();
		InputStreamReader isr = new InputStreamReader(stream);
		try {
			int ch;
			while((ch = isr.read()) != -1) {
				if(logger.isInfoEnabled()) {
					if(ch != '\r' && ch != '\n')
						buf.append((char)ch);
					else {
						logger.info(prefix + buf.toString());
						buf = new StringBuilder();
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}