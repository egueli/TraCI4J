package it.polito.appeal.traci;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.logging.log4j.Logger;

/**
 * Reads an InputStream object and logs each row in the containing class's
 * logger.
 * 
 * @author Enrico Gueli &lt;enrico.gueli@polito.it&gt;
 */
public class StreamLogger implements Runnable {

	public enum StreamLoggerTyp {
		ERROR, TRACE, DEBUG, WARN, INFO;
	}

	private final static String LOG_FORMAT = "%s: %s";

	private final InputStream stream;
	private final String prefix;
	private final Logger logger;
	private final StreamLoggerTyp typ;

	StreamLogger(InputStream stream, String prefix, Logger logger, StreamLoggerTyp typ) {
		this.stream = stream;
		this.prefix = prefix;
		this.logger = logger;
		this.typ = typ;
	}

	public void run() {
		StringBuilder buf = new StringBuilder();
		InputStreamReader isr = new InputStreamReader(stream);
		try {
			int ch;
			while ((ch = isr.read()) != -1) {
				if (isMyLogLevelOn()) {
					if (ch != '\r' && ch != '\n')
						buf.append((char) ch);
					else {
						logMsgInMyLogLevel(buf.toString());
						buf = new StringBuilder();
					}
				}
			}
		} catch (IOException e) {
			logger.error(e);
		}
	}

	private boolean isMyLogLevelOn() {
		switch (typ) {
		case ERROR:
			return logger.isErrorEnabled();
		case WARN:
			return logger.isWarnEnabled();
		case DEBUG:
			return logger.isDebugEnabled();
		case TRACE:
			return logger.isTraceEnabled();
		case INFO:
			return logger.isInfoEnabled();
		default:
			return false;
		}
	}

	private void logMsgInMyLogLevel(String msg) {
		msg = String.format(LOG_FORMAT, prefix, msg);
		switch (typ) {
		case ERROR:
			logger.error(msg);
			return;
		case WARN:
			logger.warn(msg);
			return;
		case DEBUG:
			logger.debug(msg);
			return;
		case TRACE:
			logger.trace(msg);
			return;
		case INFO:
		default:
			logger.info(msg);
			return;
		}
	}
}