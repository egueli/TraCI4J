/*   
    Copyright (C) 2011 ApPeAL Group, Politecnico di Torino

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

import it.polito.appeal.traci.ReadObjectVarQuery.StringListQ;
import it.polito.appeal.traci.protocol.Constants;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

/**
 * Models a TCP/IP connection to a local or remote SUMO server via the TraCI
 * protocol.
 * <p>
 * It runs a SUMO instance as a subprocess with a given configuration file and a
 * random seed, and provides methods and objects to advance to the next
 * simulation step, to retrieve vehicles' info, to set vehicles' routes and to
 * get roads' info.
 * <p>
 * To use it, create an instance and call {@link #runServer()}, that will start
 * the subprocess. From there, you can use all the other methods to interact
 * with the simulator.
 * <p>
 * The method {@link #nextSimStep()} will advance SUMO by a time step (one
 * second). The methods
 * {@link #addVehicleLifecycleObserver(VehicleLifecycleObserver)} and
 * {@link #removeVehicleLifecycleObserver(VehicleLifecycleObserver)} allow you
 * to register and unregister objects that will be notified whenever a vehicle
 * enters or exits the simulation.
 * <p>
 * The executable path must be specified via the system property specified in
 * {@link #SUMO_EXE_PROPERTY}.
 * <p>
 * At simulation end, one should call {@link #close()} to gracefully close the
 * simulator and free any resources.
 * 
 * @author Enrico Gueli &lt;enrico.gueli@gmail.com&gt;
 * 
 */
public class SumoTraciConnection {

	/**
	 * The system property name to get the executable path and name to run.
	 */
	public static final String SUMO_EXE_PROPERTY = "it.polito.appeal.traci.sumo_exe";

	/**
	 * Reads an InputStream object and logs each row in the containing class's
	 * logger.
	 * 
	 * @author Enrico
	 * 
	 */
	private static class StreamLogger implements Runnable {
		final InputStream stream;
		final String prefix;

		public StreamLogger(InputStream stream, String prefix) {
			this.stream = stream;
			this.prefix = prefix;
		}

		public void run() {
			StringBuilder buf = new StringBuilder();
			InputStreamReader isr = new InputStreamReader(stream);
			try {
				int ch;
				while((ch = isr.read()) != -1) {
					if(log.isInfoEnabled()) {
						if(ch != '\r' && ch != '\n')
							buf.append((char)ch);
						else {
							log.info(prefix + buf.toString());
							buf = new StringBuilder();
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static final Logger log = Logger.getLogger(SumoTraciConnection.class);

	private String configFile;
	private int randomSeed;
	private int remotePort;
	private Socket socket;
	
	private int currentSimStep;
	private Process sumoProcess;
	
	private static final int CONNECT_RETRIES = 9;

	private CloseQuery closeQuery;

	private SumoHttpRetriever httpRetriever;

	private List<String> args = new ArrayList<String>();
	
	private DataInputStream dis;
	private DataOutputStream dos;
	
	private final Set<StepAdvanceListener> stepAdvanceListeners = new HashSet<StepAdvanceListener>();

	private final Set<VehicleLifecycleObserver> vehicleLifecycleObservers = new HashSet<VehicleLifecycleObserver>();
	
	private Map<String, Vehicle> vehicles;
	private StringListQ vehicleListQuery;
	
	private Repository<Edge> edgeRepo;
	private Repository<Lane> laneRepo;
	private Repository<Vehicle> vehicleRepo;
	private Repository<POI> poiRepo;
	private Repository<InductionLoop> inductionLoopRepo;

	/*
	 * TODO add repositories for remaining SUMO object classes
	 */
	
	private SimulationData simData;

	
	/**
	 * Constructor for the object.
	 * 
	 * @param configFile
	 *            the file name of the SUMO XML configuration file
	 * @param randomSeed
	 *            the random seed for SUMO (passed with the --srand option); if
	 *            different to -1, it overrides the value specified in the
	 *            config file or, if absent, the system time
	 * @param useGeoOffset
	 *            if true, scans the network description file specified in the
	 *            config file to search the latitude/longitude of a
	 *            georeferenced map
	 */
	public SumoTraciConnection(String configFile, int randomSeed,
			boolean useGeoOffset) {
		this.randomSeed = randomSeed;
		this.configFile = configFile;
	}
	
	public SumoTraciConnection(SocketAddress sockAddr) throws IOException,
			InterruptedException {
		
		socket = new Socket();
		if (log.isDebugEnabled())
			log.debug("Connecting to remote TraCI server at " + socket.toString());

		int waitTime = 500; // milliseconds
		for (int i = 0; i < CONNECT_RETRIES; i++) {

			try {
				socket.connect(sockAddr);
				log.info("Connection to SUMO established.");
				break;
			} catch (ConnectException ce) {
				log.debug("Server not ready, retrying in " + waitTime
						+ "ms");
				Thread.sleep(waitTime);
				waitTime *= 2;
			}
		}

		if (!socket.isConnected()) {
			log.error("Couldn't connect to server");
			throw new IOException("can't connect to SUMO server");
		}
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
	 */
	public void runServer() throws IOException {
		retrieveFromURLs();		
		
		findAvailablePort();

		runSUMO();

		/*
		 * wait for simulator's loading before connecting.
		 */
		int waitTime = 500; // milliseconds
		try {
			for (int i = 0; i < CONNECT_RETRIES; i++) {
				/*
				 * first, check that the SUMO process is still alive.
				 */
				try {
					int retVal = sumoProcess.exitValue();
					throw new IOException(
							"SUMO process terminated unexpectedly with value "
									+ retVal);
				} catch (IllegalThreadStateException e) {
					// it's alive, go ahead.
				}

				socket = new Socket();
				if (log.isDebugEnabled())
					log.debug("Connecting to local port " + remotePort);

				try {
					socket.connect(new InetSocketAddress(InetAddress
							.getLocalHost(), remotePort));
					log.info("Connection to SUMO established.");
					break;
				} catch (ConnectException ce) {
					log.debug("Server not ready, retrying in " + waitTime
							+ "ms");
					Thread.sleep(waitTime);
					waitTime *= 2;
				}
			}

			if (!socket.isConnected()) {
				log.error("Couldn't connect to server");
				throw new IOException("can't connect to SUMO server");
			}

		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		postConnect();
	}

	private void postConnect() throws IOException {
		currentSimStep = 0;

		dis = new DataInputStream(socket.getInputStream());
		dos = new DataOutputStream(socket.getOutputStream());

		closeQuery = new CloseQuery(dis, dos);
		simData = new SimulationData(dis, dos);
		
		vehicles = new HashMap<String, Vehicle>();
		
		edgeRepo = new Repository.Edges(dis, dos, 
				newIDListQuery(Constants.CMD_GET_EDGE_VARIABLE));
		
		laneRepo = new Repository.Lanes(dis, dos, edgeRepo, 
				newIDListQuery(Constants.CMD_GET_LANE_VARIABLE));
		
		vehicleListQuery = newIDListQuery(Constants.CMD_GET_VEHICLE_VARIABLE);
		
		vehicleRepo = new Repository.Vehicles(dis, dos, edgeRepo, laneRepo,
				vehicles, vehicleListQuery);
		
		poiRepo = new Repository.POIs(dis, dos,
				newIDListQuery(Constants.CMD_GET_POI_VARIABLE));
		
		inductionLoopRepo = new Repository.InductionLoops(dis, dos, laneRepo,
				vehicleRepo,
				newIDListQuery(Constants.CMD_GET_INDUCTIONLOOP_VARIABLE));
		
		/*
		 * TODO add initializers for remaining repositories
		 */
		
	}

	private StringListQ newIDListQuery(final int command) {
		return new StringListQ(dis, dos,
				command, "", Constants.ID_LIST);
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

	private void runSUMO() throws IOException {
		final String sumoEXE = System.getProperty(SUMO_EXE_PROPERTY);
		if (sumoEXE == null)
			throw new RuntimeException("System property " + SUMO_EXE_PROPERTY
					+ " must be set");

		args.add(0, sumoEXE);
		
		args.add("-c");
		args.add(configFile);
		args.add("--remote-port");
		args.add(Integer.toString(remotePort));
		
		if (randomSeed != -1) {
			args.add("--seed");
			args.add(Integer.toString(randomSeed));
		}

		if (log.isDebugEnabled())
			log.debug("Executing SUMO with cmdline " + args);

		String[] argsArray = new String[args.size()];
		args.toArray(argsArray);
		sumoProcess = Runtime.getRuntime().exec(argsArray);

		// String logProcessName = SUMO_EXE.substring(SUMO_EXE.lastIndexOf("\\")
		// + 1);

		StreamLogger errStreamLogger = new StreamLogger(sumoProcess.getErrorStream(), "SUMO-err:");
		StreamLogger outStreamLogger = new StreamLogger(sumoProcess.getInputStream(), "SUMO-out:");
		new Thread(errStreamLogger, "StreamLogger-SUMO-err").start();
		new Thread(outStreamLogger, "StreamLogger-SUMO-out").start();
	}

	private void findAvailablePort() throws IOException {
		ServerSocket testSock = new ServerSocket(0);
		remotePort = testSock.getLocalPort();
		testSock.close();
		testSock = null;
	}

	/**
	 * Closes the connection and quits the simulator and frees any stale
	 * resource.
	 * <p>
	 * NOTE: this method must be called when any of the methods throw an
	 * exception, to allow to free all resources.
	 * 
	 * @throws IOException
	 *             if the close command wasn't sent successfully
	 * @throws InterruptedException
	 *             if the current thread was interrupted while waiting for SUMO
	 *             to close.
	 */
	public void close() throws IOException, InterruptedException {
		/*
		 * Unlike other command methods that instantiate a new TraCIQuery
		 * instance in the method itself, for CloseQuery we must instantiate the
		 * corresponding CloseQuery object before the effective execution, since
		 * it may occur in a remote JiST server after the RMI connection is
		 * closed. This would make the loading of the CloseQuery class
		 * impossible at this point.
		 */
		if (socket != null) {
			if (closeQuery != null) {
				closeQuery.run();
				closeQuery = null;
			}
			socket = null;
		}
		
		if (sumoProcess != null) {
			sumoProcess.waitFor();
			sumoProcess = null;
		}
		
		if (httpRetriever != null)
			httpRetriever.close();
	}
	
	/**
	 * Returns <code>true</code> if the connection was closed by the user.
	 * <p>
	 * NOTE: it may not return <code>true</code> if an error occurred and the
	 * connection with SUMO is broken.
	 * @see #close()
	 */
	public boolean isClosed() {
		return socket == null || socket.isClosed();
	}

	/**
	 * Returns the boundaries of the network.
	 * 
	 * @return the boundaries of the network
	 * 
	 * @throws IOException
	 *             if something wrong happened while sending the TraCI command.
	 */
	public Rectangle2D queryBounds() throws IOException {
		if (isClosed())
			throw new IllegalStateException("connection is closed");
	
		throw new UnsupportedOperationException("to be done");
	}

	/**
	 * Advance the SUMO simulation by a step. The method will also call
	 * {@link VehicleLifecycleObserver} instances for vehicle enter and exit.
	 * 
	 * @throws IOException
	 *             if something wrong happened while sending the TraCI command.
	 * @throws IllegalStateException
	 *             if the method is called when the connection is closed
	 */
	public void nextSimStep() throws IOException, IllegalStateException {
		if (isClosed())
			throw new IllegalStateException("connection is closed");
		
		currentSimStep++;

		/*
		 * forces querying of vehicle IDs when requested
		 */
		simData.nextStep(currentSimStep);
		
		/*
		 * save the old set of vehicles in order to compute the difference
		 * sets
		 */
		Set<String> vehicleListBefore = new HashSet<String>(vehicleListQuery.get());
		
		/*
		 * constructs a multi-query that advances one step, reads the list of
		 * active vehicles, the list of teleport-starting and teleport-ending
		 */
		StringListQ teleportStartQ;
		StringListQ teleportEndQ;
		MultiQuery multi = new MultiQuery(dos, dis);
		{ // begin multi-query
			SimStepQuery ssq = new SimStepQuery(dis, dos);
			ssq.setTargetTime(currentSimStep * 1000);
			multi.add(ssq);

			multi.add(vehicleListQuery);

			teleportStartQ = new StringListQ(dis, dos,
					Constants.CMD_GET_SIM_VARIABLE, "",
					Constants.VAR_TELEPORT_STARTING_VEHICLES_IDS);
			multi.add(teleportStartQ);

			teleportEndQ = new StringListQ(dis, dos,
					Constants.CMD_GET_SIM_VARIABLE, "",
					Constants.VAR_TELEPORT_ENDING_VEHICLES_IDS);
			multi.add(teleportEndQ);
		} // end multi-query
		multi.run();

		/*
		 * now, compute the difference sets (departed/arrived)
		 */
		Set<String> vehicleListAfter = new HashSet<String>(vehicleListQuery.get());
		Set<String> departedIDs = new HashSet<String>(vehicleListAfter);
		departedIDs.removeAll(vehicleListBefore);
		Set<String> arrivedIDs = new HashSet<String>(vehicleListBefore);
		arrivedIDs.removeAll(vehicleListAfter);
		
		/*
		 * now update the vehicles map, notify listeners and add/remove vehicles
		 * from the step advance listeners
		 */
		
		for (String arrivedID : arrivedIDs) {
			Vehicle arrived = vehicles.remove(arrivedID);
			stepAdvanceListeners.remove(arrived);
			for (VehicleLifecycleObserver observer : vehicleLifecycleObservers) {
				observer.vehicleArrived(arrived);
			}
		}
		for (String departedID : departedIDs) {
			Vehicle departed = new Vehicle(dis, dos, departedID, edgeRepo, laneRepo);
			vehicles.put(departedID, departed);
			stepAdvanceListeners.add(departed);
			for (VehicleLifecycleObserver observer : vehicleLifecycleObservers) {
				observer.vehicleDeparted(departed);
			}
		}
		
		for (VehicleLifecycleObserver observer : vehicleLifecycleObservers) {
			for (String teleportStarting : teleportStartQ.get()) {
				observer.vehicleTeleportStarting(vehicles.get(teleportStarting));
			}
			for (String teleportEnding : teleportEndQ.get()) {
				observer.vehicleTeleportEnding(vehicles.get(teleportEnding));
			}
		}
		
		/*
		 * finally, notify any interested listener that we advances one step
		 */
		for (StepAdvanceListener listener : stepAdvanceListeners)
			listener.nextStep(currentSimStep);
		
	}

	/**
	 * Returns the current simulation step number.
	 */
	public int getCurrentSimStep() {
		return currentSimStep;
	}

	public Collection<Vehicle> getVehicles() {
		return Collections.unmodifiableCollection(vehicles.values());
	}
	
	public Vehicle getVehicleByID(String vehicleID) {
		return vehicles.get(vehicleID);
	}
	
	public Repository<Edge> getEdgeRepository() {
		return edgeRepo;
	}
	
	public SimulationData getSimulationData() {
		return simData;
	}
	
	public Repository<Lane> getLaneRepository() {
		return laneRepo;
	}
	
	public Repository<POI> getPOIRepository() {
		return poiRepo;
	}
	
	public Repository<InductionLoop> getInductionLoopRepository() {
		return inductionLoopRepo;
	}
	
	/*
	 * TODO add repository getters (in the form of getXXXXRepository())
	 * for remaining SUMO object classes
	 */
	
	/**
	 * Allows a {@link VehicleLifecycleObserver}-implementing object to be
	 * notified about vehicles' creation and destruction.
	 */
	public void addVehicleLifecycleObserver(VehicleLifecycleObserver observer) {
		vehicleLifecycleObservers.add(observer);
	}

	/**
	 * Prevents a {@link VehicleLifecycleObserver}-implementing object to be
	 * notified about vehicles' creation and destruction.
	 */
	public void removeVehicleLifecycleObserver(VehicleLifecycleObserver observer) {
		vehicleLifecycleObservers.remove(observer);
	}

	/**
	 * Creates a {@link MultiQuery} bound to this server connection.
	 * @return
	 */
	public MultiQuery makeMultiQuery() {
		return new MultiQuery(dos, dis);
	}

	/**
	 * If set to true, the roadmap position of all vehicle is read at every
	 * simulation step. This will increase performance, since the query for all
	 * vehicles is made in a single TraCI query at the next sim step.
	 * 
	 * @deprecated this method will do nothing now. All the vehicles' positions
	 * can be read using a {@link MultiQuery}.
	 * @param booleanProperty
	 */
	@Deprecated
	public void setGetVehiclesEdgeAtSimStep(boolean booleanProperty) {
		
	}

	/**
	 * Returns the geo-coordinates (as longitude-latitude) of the network.
	 * @deprecated since the mechanism to obtain this data must be rewritten
	 * from scratch after changes in the XML network file format; it's better
	 * to obtain this data directly from TraCI, that is currently not supported
	 * @return
	 */
	@Deprecated
	public Point2D getGeoOffset() {
		return null;
	}
}

