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

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXException;

import it.polito.appeal.traci.ReadObjectVarQuery.StringListQ;
import it.polito.appeal.traci.StreamLogger.StreamLoggerTyp;
import it.polito.appeal.traci.protocol.Constants;

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
 * The class looks for the system property
 * <code>{@value #SUMO_EXE_PROPERTY}</code> that should contain the full path of
 * the executable. If such property is not found, the "sumo" executable will be
 * searched in the system PATH instead.
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

	public static final String OS_ARCH_PROPERTY = "os.arch";
	public static final String OS_NAME_PROPERTY = "os.name";

	/**
	 * The system property name to get user defined tcp_nodedelay for the tcp
	 * socket.
	 */
	public static final String TCP_NODELAY_PROPERTY = "it.polito.appeal.traci.tcp_nodelay";

	private static final Logger log = LogManager.getLogger();

	private String configFile;
	private int randomSeed;
	private Socket socket;

	/** The current simulation step, in ms. */
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
	private Set<String> vehicleListBefore;

	private AddVehicleQuery addVehicleQuery;
	private RemoveVehicleQuery removeVehicleQuery;
	private AddRouteQuery addRouteQuery;

	private Repository.Edges edgeRepo;
	private Repository.Lanes laneRepo;
	private Repository.Vehicles vehicleRepo;
	private Repository.POIs poiRepo;
	private Repository.InductionLoops inductionLoopRepo;
	private Repository.TrafficLights trafficLightRepo;
	private Repository.VehicleTypes vehicleTypeRepo;
	private Repository.MeMeDetectors memeDetectorRepo;
	private Repository.LaArDetectors laarDetectorRepo;
	private Repository.Routes routeRepo;

	/**
	 * Duration of a step in ms
	 */
	private int steplength = 1000;

	/*
	 * TODO add repositories for remaining SUMO object classes
	 */

	private SimulationData simData;

	/**
	 * Creates an instance of this class that runs an own instance of SUMO. The
	 * constructor won't run SUMO immediately; for that, call
	 * {@link #runServer()}.
	 * 
	 * @param configFile
	 *            the file name of the SUMO XML configuration file
	 * @param randomSeed
	 *            the random seed for SUMO (passed with the --srand option); if
	 *            different to -1, it overrides the value specified in the
	 *            config file or, if absent, the system time
	 */
	public SumoTraciConnection(String configFile, int randomSeed) {
		this.randomSeed = randomSeed;
		this.configFile = configFile;
	}

	/**
	 * Creates an instance of this class that runs an own instance of SUMO. The
	 * constructor won't run SUMO immediately; for that, call
	 * {@link #runServer()}.
	 * 
	 * @deprecated the useGeoOffset flag refers to an old hack that returned all
	 *             locations with an offset that allowed them to be
	 *             geo-referenced on a map. Since this offset was not available
	 *             via TraCI, it was looked for in the .net.xml file. But the
	 *             format of the network file is not documented and subject to
	 *             changes over time.
	 * @param configFile
	 *            the file name of the SUMO XML configuration file
	 * @param randomSeed
	 *            the random seed for SUMO (passed with the --srand option); if
	 *            different to -1, it overrides the value specified in the
	 *            config file or, if absent, the system time
	 */
	@Deprecated
	public SumoTraciConnection(String configFile, int randomSeed, boolean useGeoOffset) {
		this(configFile, randomSeed);
	}

	/**
	 * Creates an instance of this class and connects to a running instance of
	 * SUMO.
	 * 
	 * <br/>
	 * <br/>
	 * Note that TCP_NODELAY on the socket connection to SUMO will be
	 * <b>enabled</b> by default. You can modify this behaviour later before the
	 * call to <code>runServer()</code>.
	 * 
	 * @param addr
	 *            the IP address of the machine where SUMO runs
	 * @param port
	 *            the TCP port SUMO is listening for commands (see --remote-port
	 *            option)
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public SumoTraciConnection(InetAddress addr, int port) throws IOException, InterruptedException {

		// Set TCP_NODELAY as enabled by default.
		enableTcpNoDelay();

		tryConnect(addr, port, null);
		postConnect();
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
	 * 
	 * @see {@link #runServer(boolean)}
	 */
	public void runServer() throws IOException, InterruptedException {
		runServer(false);
	}

	/**
	 * Runs a SUMO instance and tries to connect at it.
	 * 
	 * @param withGui
	 *            Start sumo with gui or not
	 * @throws IOException
	 *             if something wrong occurs while starting SUMO or connecting
	 *             at it.
	 * @throws InterruptedException
	 * 
	 * @see {@link #runServer()}
	 */
	public void runServer(boolean withGui) throws IOException, InterruptedException {
		retrieveFromURLs();

		int port = findAvailablePort();

		runSUMO(port, withGui);

		tryConnect(InetAddress.getLocalHost(), port, sumoProcess);
		postConnect();

	}

	/**
	 * Tries to connect to the TraCI server reachable at the given address and
	 * TCP port. If also specified, checks that the SUMO process is present.
	 * 
	 * @param addr
	 *            the address of the TraCI server
	 * @param port
	 *            the TCP port of the TraCI server
	 * @param process
	 *            a reference to a {@link Process} object representing SUMO
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private void tryConnect(InetAddress addr, int port, Process process) throws IOException, InterruptedException {
		int waitTime = 500; // milliseconds
		for (int i = 0; i < CONNECT_RETRIES; i++) {
			if (process != null) {
				try {
					int retVal = process.exitValue();
					throw new IOException("SUMO process terminated unexpectedly with value " + retVal);
				} catch (IllegalThreadStateException e) {
					log.debug("It's alive, go ahead", e);
				}
			}

			if (log.isDebugEnabled())
				log.debug("Connecting to " + addr + ":" + port);

			if (tryConnectOnce(addr, port)) {
				log.info("Connection to SUMO established.");
				break;
			} else {
				log.debug("Server not ready, retrying in " + waitTime + "ms");
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
	 * Is TCP_NODELAY activated for the socket connection to SUMO?
	 * 
	 * @return true if active and false otherwise.
	 */
	public boolean isTcpNoDelayActive() {
		return Boolean.getBoolean(TCP_NODELAY_PROPERTY);
	}

	/**
	 * Enables TCP_NODELAY in the socket connection to SUMO
	 */
	public void enableTcpNoDelay() {
		setTcpNoDelay(true);
	}

	/**
	 * Disables TCP_NODELAY in the socket connection to SUMO
	 */
	public void disableTcpNoDelay() {
		setTcpNoDelay(false);
	}

	/**
	 * Forcibly set TCP_NODELAY_PROPERTY
	 * 
	 * @param on
	 *            If true TCP_NODELAY will be turned on. Else turned off.
	 */
	public void setTcpNoDelay(boolean on) {
		System.setProperty(TCP_NODELAY_PROPERTY, String.valueOf(on));
	}

	private boolean tryConnectOnce(InetAddress addr, int port) throws UnknownHostException, IOException {
		boolean tcpNoDelay = Boolean.getBoolean(TCP_NODELAY_PROPERTY);

		socket = new Socket();
		socket.setTcpNoDelay(tcpNoDelay);

		try {
			socket.connect(new InetSocketAddress(addr, port));
			return true;
		} catch (ConnectException ce) {
			log.debug(ce);
			return false;
		}
	}

	private void postConnect() throws IOException {
		dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
		dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

		closeQuery = new CloseQuery(dis, dos);
		simData = new SimulationData(dis, dos);

		currentSimStep = simData.queryCurrentSimTime().get();

		vehicles = new HashMap<String, Vehicle>();

		edgeRepo = new Repository.Edges(dis, dos, newIDListQuery(Constants.CMD_GET_EDGE_VARIABLE));
		addStepAdvanceListener(edgeRepo);

		laneRepo = new Repository.Lanes(dis, dos, edgeRepo, newIDListQuery(Constants.CMD_GET_LANE_VARIABLE));

		vehicleListQuery = newIDListQuery(Constants.CMD_GET_VEHICLE_VARIABLE);
		addStepAdvanceListener(new StepAdvanceListener() {
			public void nextStep(double step) {
				vehicleListQuery.setObsolete();
			}
		});
		vehicleListBefore = new HashSet<String>(vehicleListQuery.get());

		vehicleRepo = new Repository.Vehicles(dis, dos, edgeRepo, laneRepo, vehicles, vehicleListQuery);
		addStepAdvanceListener(vehicleRepo);

		addVehicleQuery = new AddVehicleQuery(dis, dos, vehicleRepo);

		removeVehicleQuery = new RemoveVehicleQuery(dis, dos);

		poiRepo = new Repository.POIs(dis, dos, newIDListQuery(Constants.CMD_GET_POI_VARIABLE));

		inductionLoopRepo = new Repository.InductionLoops(dis, dos, laneRepo, vehicleRepo,
				newIDListQuery(Constants.CMD_GET_INDUCTIONLOOP_VARIABLE));
		addStepAdvanceListener(inductionLoopRepo);

		trafficLightRepo = new Repository.TrafficLights(dis, dos, laneRepo,
				newIDListQuery(Constants.CMD_GET_TL_VARIABLE));
		addStepAdvanceListener(trafficLightRepo);

		vehicleTypeRepo = new Repository.VehicleTypes(dis, dos, newIDListQuery(Constants.CMD_GET_VEHICLETYPE_VARIABLE));

		memeDetectorRepo = new Repository.MeMeDetectors(dis, dos, vehicleRepo,
				newIDListQuery(Constants.CMD_GET_MULTI_ENTRY_EXIT_DETECTOR_VARIABLE));
		addStepAdvanceListener(memeDetectorRepo);

		laarDetectorRepo = new Repository.LaArDetectors(dis, dos, laneRepo, vehicleRepo,
				newIDListQuery(Constants.CMD_GET_LANE_AREA_DETECTOR_VARIABLE));
		addStepAdvanceListener(laarDetectorRepo);

		routeRepo = new Repository.Routes(dis, dos, edgeRepo, newIDListQuery(Constants.CMD_GET_ROUTE_VARIABLE));

		addRouteQuery = new AddRouteQuery(dis, dos, routeRepo);

		/*
		 * TODO add initializers for remaining repositories
		 */

	}

	private StringListQ newIDListQuery(final int command) {
		return new StringListQ(dis, dos, command, "", Constants.ID_LIST);
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

	private void runSUMO(int remotePort, boolean withGui) throws IOException {
		String sumoEXE = System.getProperty(SUMO_EXE_PROPERTY);
		if (sumoEXE == null)
			sumoEXE = "sumo";

		if (withGui)
			sumoEXE += "-gui";

		String sumoEXE64 = sumoEXE;
		if (System.getProperty(OS_ARCH_PROPERTY).contains("64") && System.getProperty(OS_NAME_PROPERTY).contains("Win"))
			sumoEXE64 += "64";

		args.add(0, sumoEXE64);
		args.add(1, "-c");
		args.add(2, configFile);
		args.add(3, "--remote-port");
		args.add(4, Integer.toString(remotePort));
		args.add(5, "--step-length");

		args.add(6, String.format(Locale.ENGLISH, "%.3f", (double) steplength / 1000));

		if (randomSeed != -1) {
			args.add(7, "--seed");
			args.add(8, Integer.toString(randomSeed));
		}

		// this avoids validation of the input xml files; if SUMO_HOME is not set correctly,
		// sumo will try to download the schema files from the web and may wait 30 seconds at startup
		// for the connection to time out.
		args.add(9, "--xml-validation");
		args.add(10, "never");

		if (log.isDebugEnabled())
			log.debug("Executing SUMO with cmdline " + args);

		String[] argsArray = new String[args.size()];
		args.toArray(argsArray);
		try {
			sumoProcess = Runtime.getRuntime().exec(argsArray);
		} catch (IOException e) {
			if (!sumoEXE64.equals(sumoEXE)) {
				log.debug("Try it again (x64).");
				argsArray[0] = sumoEXE;
				sumoProcess = Runtime.getRuntime().exec(argsArray);
			} else {
				throw e;
			}
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}

		StreamLogger errStreamLogger = new StreamLogger(sumoProcess.getErrorStream(), "SUMO", log,
				StreamLoggerTyp.ERROR);
		StreamLogger outStreamLogger = new StreamLogger(sumoProcess.getInputStream(), "SUMO", log,
				StreamLoggerTyp.INFO);
		new Thread(errStreamLogger, "StreamLogger-SUMO-err").start();
		new Thread(outStreamLogger, "StreamLogger-SUMO-out").start();
	}

	private int findAvailablePort() throws IOException {
		ServerSocket testSock = new ServerSocket(0);
		int port = testSock.getLocalPort();
		testSock.close();
		return port;
	}

	/**
	 * Closes the connection, quits the simulator and frees any stale resources.
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
		 * it may occur in a remote RMI server after the RMI connection is
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
	 * 
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

		return getSimulationData().queryNetBoundaries().get();
	}

	/**
	 * Advance the SUMO simulation by a step. The method may also notify
	 * {@link VehicleLifecycleObserver} instances.
	 * 
	 * @throws IOException
	 *             if something wrong happened while sending the TraCI command.
	 * @throws IllegalStateException
	 *             if the method is called when the connection is closed
	 */
	public void nextSimStep() throws IOException, IllegalStateException {
		if (isClosed())
			throw new IllegalStateException("connection is closed");

		currentSimStep += steplength;

		/*
		 * forces querying of vehicle IDs when requested
		 */
		simData.nextStep(currentSimStep);

		/*
		 * constructs a multi-query that advances one step, reads the list of
		 * active vehicles, the list of teleport-starting and teleport-ending
		 */
		StringListQ teleportStartQ;
		StringListQ teleportEndQ;
		MultiQuery multi = new MultiQuery(dos, dis);
		{ // begin multi-query
			SimStepQuery ssq = new SimStepQuery(dis, dos);
			ssq.setTargetTime(currentSimStep);
			multi.add(ssq);

			multi.add(vehicleListQuery);

			teleportStartQ = new StringListQ(dis, dos, Constants.CMD_GET_SIM_VARIABLE, "",
					Constants.VAR_TELEPORT_STARTING_VEHICLES_IDS);
			multi.add(teleportStartQ);

			teleportEndQ = new StringListQ(dis, dos, Constants.CMD_GET_SIM_VARIABLE, "",
					Constants.VAR_TELEPORT_ENDING_VEHICLES_IDS);
			multi.add(teleportEndQ);
		} // end multi-query
		multi.run();

		/*
		 * now, compute the difference sets (departed/arrived) and teleports
		 */
		List<String> teleportStart = teleportStartQ.get();
		List<String> teleportEnd = teleportEndQ.get();

		Set<String> vehicleListAfter = new HashSet<String>(vehicleListQuery.get());

		Set<String> departedIDs = Utils.getAddedItems(vehicleListBefore, vehicleListAfter);
		departedIDs.removeAll(teleportEnd);

		Set<String> arrivedIDs = Utils.getRemovedItems(vehicleListBefore, vehicleListAfter);
		arrivedIDs.removeAll(teleportStart);

		/*
		 * now update the vehicles map and notify listeners
		 */

		for (String arrivedID : arrivedIDs) {
			Vehicle arrived = vehicles.remove(arrivedID);
			if (log.isDebugEnabled())
				log.debug(" arrivedID = " + arrivedID + " Vehicle = " + arrived);
			removeStepAdvanceListener(arrived);
			for (VehicleLifecycleObserver observer : vehicleLifecycleObservers) {
				observer.vehicleArrived(arrived);
			}
		}
		for (String departedID : departedIDs) {
			Vehicle departed = new Vehicle(dis, dos, departedID, edgeRepo, laneRepo);
			if (log.isDebugEnabled())
				log.debug(" departedID = " + departedID + " Vehicle = " + departed);
			addStepAdvanceListener(departed);
			vehicles.put(departedID, departed);
		}
		for (String departedID : departedIDs) {
			for (VehicleLifecycleObserver observer : vehicleLifecycleObservers) {
				observer.vehicleDeparted(vehicles.get(departedID));
			}
		}

		for (VehicleLifecycleObserver observer : vehicleLifecycleObservers) {

			for (String teleportStarting : teleportStart) {
				Vehicle vehicle = vehicles.get(teleportStarting);
				if (vehicle != null) {
					if (log.isDebugEnabled())
						log.debug(" Vehicle " + teleportStarting + " started teleporting.");
					observer.vehicleTeleportStarting(vehicle);
				} else
					log.warn(" Teleporting vehicle " + teleportStarting + " not found!");
			}
			for (String teleportEnding : teleportEnd) {
				Vehicle vehicle = vehicles.get(teleportEnding);
				if (vehicle != null) {
					if (log.isDebugEnabled())
						log.debug(" Vehicle " + teleportEnding + " ended teleporting.");
					observer.vehicleTeleportEnding(vehicle);
				} else
					log.warn(" Teleporting vehicle " + teleportEnding + " not found!");
			}
		}

		/*
		 * notify any interested listener that we advances one step
		 */
		for (StepAdvanceListener listener : stepAdvanceListeners)
			listener.nextStep(currentSimStep);

		vehicleListBefore = vehicleListAfter;
	}

	/**
	 * Returns the current simulation step time in ms.
	 */
	public int getCurrentSimTime() {
		return currentSimStep;
	}

	/**
	 * 
	 * @return the {@link Repository} containing all the active vehicles.
	 */
	public Repository<Vehicle> getVehicleRepository() {
		return vehicleRepo;
	}

	/**
	 * 
	 * @return an {@link AddVehicleQuery} that allows to add vehicles into the
	 *         simulation.
	 */
	public AddVehicleQuery queryAddVehicle() {
		return addVehicleQuery;
	}

	/**
	 * 
	 * @return a {@link RemoveVehicleQuery} that allows to remove a vehicle into
	 *         the simulation.
	 */
	public RemoveVehicleQuery queryRemoveVehicle() {
		return removeVehicleQuery;
	}

	/**
	 * 
	 * @return the {@link Repository} containing all the edges the network is
	 *         made of.
	 */
	public Repository<Edge> getEdgeRepository() {
		return edgeRepo;
	}

	/**
	 * 
	 * @return the {@link SimulationData} object that provides global info about
	 *         the simulation.
	 */
	public SimulationData getSimulationData() {
		return simData;
	}

	/**
	 * 
	 * @return the {@link Repository} containing all the lanes the network is
	 *         made of.
	 */
	public Repository<Lane> getLaneRepository() {
		return laneRepo;
	}

	/**
	 * 
	 * @return the {@link Repository} containing all the POIs in the network.
	 */
	public Repository<POI> getPOIRepository() {
		return poiRepo;
	}

	/**
	 * 
	 * @return the {@link Repository} containing all the induction loops in the
	 *         network.
	 */
	public Repository<InductionLoop> getInductionLoopRepository() {
		return inductionLoopRepo;
	}

	/**
	 * 
	 * @return the {@link Repository} containing all the traffic lights in the
	 *         network.
	 */
	public Repository<TrafficLight> getTrafficLightRepository() {
		return trafficLightRepo;
	}

	/**
	 * 
	 * @return the {@link Repository} containing all the known vehicle types in
	 *         the simulation.
	 */
	public Repository<VehicleType> getVehicleTypeRepository() {
		return vehicleTypeRepo;
	}

	/**
	 * 
	 * @return the {@link Repository} containing all the multi-entry/multi-exit
	 *         detectors in the network.
	 */
	public Repository<MeMeDetector> getMeMeDetectorRepository() {
		return memeDetectorRepo;
	}

	/**
	 * 
	 * @return the {@link Repository} containing all the lane area detectors in
	 *         the network.
	 */
	public Repository<LaArDetector> getLaArDetectorRepository() {
		return laarDetectorRepo;
	}

	/**
	 * 
	 * @return an {@link AddVehicleQuery} that allows to add vehicles into the
	 *         simulation.
	 */
	public AddRouteQuery queryAddRoute() {
		return addRouteQuery;
	}

	/**
	 * 
	 * @return the {@link Repository} containing all the known routes in the
	 *         simulation.
	 */
	public Repository<Route> getRouteRepository() {
		return routeRepo;
	}

	/*
	 * TODO add repository getters (in the form of getXXXXRepository()) for
	 * remaining SUMO object classes
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
	 * Registers a listener for any simulation step advancements.
	 * 
	 * @param listener
	 */
	public void addStepAdvanceListener(StepAdvanceListener listener) {
		stepAdvanceListeners.add(listener);
	}

	/**
	 * Un-registers a listener for any simulation step advancements.
	 * 
	 * @param listener
	 */
	public void removeStepAdvanceListener(StepAdvanceListener listener) {
		stepAdvanceListeners.remove(listener);
	}

	/**
	 * @return a new instance of {@link MultiQuery} bound to this server
	 *         connection.
	 */
	public MultiQuery makeMultiQuery() {
		return new MultiQuery(dos, dis);
	}

	/**
	 * @return a new instance of {@link PositionConversionQuery} that allows
	 *         converting between position types.
	 */
	public PositionConversionQuery queryPositionConversion() {
		return simData.queryPositionConversion();
	}

	/**
	 * Set the duration of a step
	 * 
	 * @param length
	 *            the time in ms
	 */
	public void setStepLength(int steplength) {
		this.steplength = steplength;
	}

	/**
	 * If set to true, the roadmap position of all vehicle is read at every
	 * simulation step. This will increase performance, since the query for all
	 * vehicles is made in a single TraCI query at the next sim step.
	 * 
	 * @deprecated this method will do nothing now. All the vehicles' positions
	 *             can be read using a {@link MultiQuery}.
	 * @param booleanProperty
	 */
	@Deprecated
	public void setGetVehiclesEdgeAtSimStep(boolean booleanProperty) {

	}

	/**
	 * @return the geo-coordinates (as longitude-latitude) of the network.
	 * @deprecated since the mechanism to obtain this data must be rewritten
	 *             from scratch after changes in the XML network file format;
	 *             it's better to obtain this data directly from TraCI, that is
	 *             currently not supported
	 */
	@Deprecated
	public Point2D getGeoOffset() {
		return null;
	}
}
