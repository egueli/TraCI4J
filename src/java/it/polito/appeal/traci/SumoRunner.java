/*   
    Copyright (C) 2013 ApPeAL Group, Politecnico di Torino

    This file is part of TraCI4J.

    TraCI4J is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    TraCI4J is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with TraCI4J.  If not, see <http://www.gnu.org/licenses/>.
*/

package it.polito.appeal.traci;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

/**
 * Handles the execution and termination of the SUMO executable, given a simulation
 * configuration file name.
 * <p>
 * If the file name is an HTTP URL, this class will download all the required files
 * from the specified URL before starting the server. 
 * 
 * @author Enrico Gueli &lt;enrico.gueli@polito.it&gt;
 *
 */
public class SumoRunner {
	
	private static final Logger log = Logger.getLogger(SumoRunner.class);

	
	/**
	 * The system property name to get the executable path and name to run.
	 */
	public static final String SUMO_EXE_PROPERTY = "it.polito.appeal.traci.sumo_exe";

	private Process sumoProcess;

	private String configFile;
	private boolean withGUI;


	private List<String> args = new ArrayList<String>();


	private SumoHttpRetriever httpRetriever;

	
	@Deprecated
	public SumoRunner(int randomSeed, String configFile) {
		this(configFile, false);
		addOption("seed", Integer.toString(randomSeed));
	}
	
	/**
	 * Creates an instance of this class using default executable locations.
	 * 
	 * @param configFile
	 *            the simulation configuration file (usually ends with
	 *            <code>.sumo.cfg</code>)
	 * @param withGUI
	 *            if <code>true</code>, executes the SUMO GUI version (
	 *            <code>sumo-gui</code>). The options <code>--start</code> and
	 *            <code>--quit-on-end</code> are set to 1 in order to avoid any
	 *            user interaction.
	 */
	public SumoRunner(String configFile, boolean withGUI) {
		this.configFile = configFile;
		this.withGUI = withGUI;
		addOption("start", "1");
		addOption("quit-on-end", "1");
	}


	/**
	 * Adds a custom option to the SUMO command line before executing it.
	 * 
	 * @param option
	 *            the option name, in long form (e.g. &quot;no-warnings&quot;
	 *            instead of &quot;W&quot;) and without initial dashes
	 * @param value
	 *            the option value, or <code>null</code> if the option has no
	 *            value
	 */
	public void addOption(String option, String value) {
		args.add("--" + option);
		if (value != null)
			args.add(value);
	}
	
	
	/**
	 * Runs a SUMO instance and tries to connect at it.
	 * 
	 * @throws IOException
	 *             if something wrong occurs while starting SUMO or connecting
	 *             at it.
	 * @throws InterruptedException 
	 */
	public int runServer() throws IOException, InterruptedException {
		retrieveFromURLs();		
		
		int port = findAvailablePort();

		runSUMO(port);

		return port;
	}
	


	private int findAvailablePort() throws IOException {
		ServerSocket testSock = new ServerSocket(0);
		int port = testSock.getLocalPort();
		testSock.close();
		return port;
	}
	
	private void retrieveFromURLs() throws IOException {
		if (configFile.startsWith("http://")) {
			
			httpRetriever = new SumoHttpRetriever(configFile);

			log.info("Downloading config file from " + configFile);
			try {
				httpRetriever.fetchAndParse();
			} catch (SAXException e) {
				throw new IOException(e);
			}
			
			configFile = httpRetriever.getConfigFileName();
		}
			
	}

	
	private void runSUMO(int remotePort) throws IOException {
		String sumoEXE = System.getProperty(SUMO_EXE_PROPERTY);
		if (sumoEXE == null)
			sumoEXE = withGUI ? "sumo-gui" : "sumo";

		args.add(0, sumoEXE);
		
		args.add("-c");
		args.add(configFile);
		args.add("--remote-port");
		args.add(Integer.toString(remotePort));
		
		if (log.isDebugEnabled())
			log.debug("Executing SUMO with cmdline " + args);

		String[] argsArray = new String[args.size()];
		args.toArray(argsArray);
		sumoProcess = Runtime.getRuntime().exec(argsArray);

		// String logProcessName = SUMO_EXE.substring(SUMO_EXE.lastIndexOf("\\")
		// + 1);

		StreamLogger errStreamLogger = new StreamLogger(sumoProcess.getErrorStream(), "SUMO-err:", log);
		StreamLogger outStreamLogger = new StreamLogger(sumoProcess.getInputStream(), "SUMO-out:", log);
		new Thread(errStreamLogger, "StreamLogger-SUMO-err").start();
		new Thread(outStreamLogger, "StreamLogger-SUMO-out").start();
	}
	
	public void close() throws InterruptedException {
		if (sumoProcess != null) {
			sumoProcess.waitFor();
			sumoProcess = null;
		}
		
		if (httpRetriever != null)
			httpRetriever.close();
	}


	public int getSumoExitValue() {
		return sumoProcess.exitValue();
	}

}
