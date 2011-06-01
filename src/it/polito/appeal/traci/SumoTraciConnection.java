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

import it.polito.appeal.traci.query.ChangeEdgeStateQuery;
import it.polito.appeal.traci.query.ChangeLaneStateQuery;
import it.polito.appeal.traci.query.CloseQuery;
import it.polito.appeal.traci.query.MultiVehiclePositionQuery;
import it.polito.appeal.traci.query.NetBoundariesQuery;
import it.polito.appeal.traci.query.RetrieveEdgeStateQuery;
import it.polito.appeal.traci.query.RoadsQuery;
import it.polito.appeal.traci.query.SimStepQuery;
import it.polito.appeal.traci.query.SubscribeVehiclesLifecycle;
import it.polito.appeal.traci.query.VehicleCountMaxQuery;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import de.uniluebeck.itm.tcpip.Socket;

/**
 * Models a TCP/IP connection to a local SUMO server via the TraCI protocol.
 * <p>
 * It runs a SUMO instance as a subprocess with a given configuration file and a
 * random seed, and provides methods and objects to advance to the next
 * simulation step, to retrieve vehicles' info, to set vehicles' routes and to
 * get roads' info.
 * <p>
 * To use it, create an instance and call {@link #runServer()}, creating the
 * subprocess. Then, call {@link #getActiveVehicles()} to obtain the set of
 * integer vehicle IDs alive at t=0. By calling {@link #getVehicle(int)} with
 * the proper ID, you'll obtain a {@link Vehicle} instance to interact with a
 * specific vehicle. With {@link #queryRoads(boolean)}, {@link #getRoad(String)}, {@link #getRoadLength(String)} and {@link #getGeoOffset()} you can obtain
 * info about the street map.
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
 * <p>
 * It implements the {@link DoNotRewrite} interface because the TCP/IP code
 * should work with the system API, not the SWANS one.
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
//			BufferedReader br = new BufferedReader(
//					new InputStreamReader(stream));
//
//			String line;
//			try {
//			try {
//				while ((line = br.readLine()) != null) {
//					if (log.isInfoEnabled())
//						log.info(prefix + line);
//				}
//
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
			
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
	private final Set<String> activeVehicles;
	private int maxVehicleID;
	private final Set<VehicleLifecycleObserver> vehicleLifecycleObservers;
	private final Map<String, Vehicle> vehicles;

	private static final int CONNECT_RETRIES = 9;

	private Point2D geoOffset;

	private Map<String, Road> cachedRoads;

	private CloseQuery closeQuery;

	private boolean readInternalLinks = false;

	private SumoHttpRetriever httpRetriever;

	private boolean getVehiclesEdgeAtSimStep;
	
	/**
	 * Constuctor for the object.
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
		activeVehicles = new HashSet<String>();
		vehicleLifecycleObservers = new HashSet<VehicleLifecycleObserver>();
		vehicles = new HashMap<String, Vehicle>();

		if (useGeoOffset)
			geoOffset = lookForGeoOffset(configFile);
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

				socket = new Socket(remotePort);
				if (log.isDebugEnabled())
					log.debug("Connecting to local port " + remotePort);

				try {
					socket.connect();
					log.info("Connection to SUMO established.");
					break;
				} catch (ConnectException ce) {
					log.debug("Server not ready, retrying in " + waitTime
							+ "ms");
					Thread.sleep(waitTime);
					waitTime *= 2;
				}
			}

			if (!socket.has_client_connection()) {
				log.error("Couldn't connect to server");
				throw new IOException("can't connect to SUMO server");
			}

		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		currentSimStep = 0;

		subscribeVehiclesLifecycle();
		maxVehicleID = queryMaxVehicleCount();

		closeQuery = new CloseQuery(socket);
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

		String[] args;
		if (randomSeed != -1)
			args = new String[] { sumoEXE, "-c", configFile, "--srand",
					Integer.toString(randomSeed), "--remote-port",
					Integer.toString(remotePort) };
		else
			args = new String[] { sumoEXE, "-c", configFile, "--remote-port",
					Integer.toString(remotePort) };

		if (log.isDebugEnabled())
			log.debug("Executing SUMO with cmdline " + Arrays.toString(args));

		sumoProcess = Runtime.getRuntime().exec(args);

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
	 * Closes the connection and quits the simulator.
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
			closeQuery.doCommand();
			closeQuery = null;
			socket = null;
		}
		
		if (sumoProcess != null) {
			sumoProcess.waitFor();
			sumoProcess = null;
		}
		
		if (httpRetriever != null)
			httpRetriever.close();
		
		vehicles.clear();
		vehicleLifecycleObservers.clear();
		cachedRoads = null;
	}

	private int queryMaxVehicleCount() throws IOException {
		VehicleCountMaxQuery countMax = new VehicleCountMaxQuery(socket);
		return countMax.doCommand();
	}

	/**
	 * Returns the boundaries (bottom-left and top-right) of the network.
	 * 
	 * @return a two-element Point2D array, whose first element is the
	 *         bottom-left corner and the second element is the top-right corner
	 * 
	 * @throws IOException
	 *             if something wrong happened while sending the TraCI command.
	 */
	public Point2D[] queryBounds() throws IOException {
		return (new NetBoundariesQuery(socket)).doCommand();
	}

	/**
	 * the geographical coordinates (latitude/longitude) of the bottom-left
	 * corner, if the network description file is derived from a GIS map.
	 * 
	 * @return the geo coordinates of the bottom-left corner, or null if the
	 *         network description file didn't specify info about the geographic
	 *         map
	 */
	public Point2D getGeoOffset() {
		return geoOffset;
	}

	private void subscribeVehiclesLifecycle() throws IOException {
		(new SubscribeVehiclesLifecycle(socket)).doCommand();
	}

	/**
	 * Advance the SUMO simulation by a step. The method will also call
	 * {@link VehicleLifecycleObserver} instances for vehicle enter and exit.
	 * 
	 * @throws IOException
	 *             if something wrong happened while sending the TraCI command.
	 */
	public void nextSimStep() throws IOException {
		currentSimStep++;
		SimStepQuery ssQuery = new SimStepQuery(socket, currentSimStep);
		ssQuery.doCommand();

		Set<String> created = ssQuery.getCreatedVehicles();
		Set<String> destroyed = ssQuery.getDestroyedVehicles();

		activeVehicles.addAll(created);
		activeVehicles.removeAll(destroyed);

		notifyDestroyed(destroyed);
		notifyCreated(created);

		MultiVehiclePositionQuery mvpQuery = 
			new MultiVehiclePositionQuery(socket);
		
		Map<String, Point2D> vehiclesPosition = 
			mvpQuery.getVehiclesPosition(activeVehicles);

		for(Map.Entry<String, Point2D> entry : vehiclesPosition.entrySet()) {
			String id = entry.getKey();
			Point2D pos = entry.getValue();
			
			if(!activeVehicles.contains(id) || !vehicles.containsKey(id))
				throw new IllegalStateException("should never happen");
			
			vehicles.get(id).setPosition(pos);
		}
		
		if (getVehiclesEdgeAtSimStep) {
			Map<String, String> vehiclesEdge = getAllVehiclesEdge();
			for ( Vehicle v : vehicles.values() ) {
				String name = v.getID();
				if (vehiclesEdge.containsKey(name)) {
					v.setCurrentEdge(vehiclesEdge.get(name));
				}
			}
		}
	}

	protected void notifyCreated(Set<String> created) {
		for (String id : created) {
			vehicles.put(id, new Vehicle(id, socket, this));

			if (log.isDebugEnabled())
				log.debug("Vehicle " + id + " created");

			for (VehicleLifecycleObserver observer : vehicleLifecycleObservers)
				observer.vehicleCreated(id);
		}
	}

	protected void notifyDestroyed(Set<String> destroyed) {
		for (String id : destroyed) {
			for (VehicleLifecycleObserver observer : vehicleLifecycleObservers)
				observer.vehicleDestroyed(id);

			if (log.isDebugEnabled())
				log.debug("Vehicle " + id + " destroyed");

			vehicles.get(id).alive = false;
			vehicles.remove(id);
		}
	}

	/**
	 * Returns a set containing which vehicles are currently circulating.
	 * Vehicles are identified by their string ID.
	 */
	public Set<String> getActiveVehicles() {
		return activeVehicles;
	}

	/**
	 * Returns the maximum number of vehicles that participate into the
	 * simulation.
	 */
	public int getMaxVehicleCount() {
		return maxVehicleID;
	}

	/**
	 * Returns the current simulation step number.
	 * 
	 * @return
	 */
	public int getCurrentSimStep() {
		return currentSimStep;
	}

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
	 * Returns a {@link Vehicle} object with the given ID.
	 * 
	 * @param id
	 *            the internal ID of the vehicle
	 * @return the corresponding Vehicle object
	 * @throws IllegalArgumentException
	 *             if the given ID doesn't match any active vehicle
	 */
	public Vehicle getVehicle(String id) {
		if (vehicles.containsKey(id))
			return vehicles.get(id);
		else
			throw new IllegalArgumentException("Vehicle ID " + id
					+ " does not exist");
	}

	/**
	 * Returns a collection of {@link Road} objects, representing the entire
	 * traffic network.
	 * <p>
	 * NOTE: this command can require some time to complete.
	 * @return a collection of {@link Road}s
	 * @throws IOException
	 *             if something wrong happened while sending the TraCI command.
	 */
	public Collection<Road> queryRoads() throws IOException {
		if (cachedRoads == null) {
			log.info("Retrieving roads...");
			Set<Road> roads = (new RoadsQuery(socket)).queryRoads(readInternalLinks);
			log.info("... done, " + roads.size() + " roads read");
			
			cachedRoads = new HashMap<String, Road>();
			for (Road r : roads) {
				cachedRoads.put(r.externalID, r);
			}
			cachedRoads = Collections.unmodifiableMap(cachedRoads);
		}
			

		return cachedRoads.values();
	}
	
	public Map<String, Road> getRoadsMap() throws IOException {
		queryRoads();
		return cachedRoads;
	}

	/**
	 * Returns the length of a single road given its string ID.
	 * 
	 * @param roadID
	 * @return
	 * @throws IOException
	 *             if something wrong happened while sending the TraCI command.
	 * @throws NullPointerException
	 *             if the ID doesn't match any road
	 */
	public double getRoadLength(String roadID) throws IOException {
		Road r = getRoad(roadID);
		return r.getLength();
	}

	/**
	 * Returns a {@link Road} object matching the given ID
	 * 
	 * @param roadID
	 * @return the requested {@link Road} object, or null if such road doesn't
	 *         exist.
	 * @throws IOException
	 */
	public Road getRoad(String roadID) throws IOException {
		if (cachedRoads == null)
			queryRoads();

		return cachedRoads.get(roadID);
	}

	@SuppressWarnings("serial")
	private static class DataFoundException extends SAXException {
		public DataFoundException(String string) {
			super(string);
		}
	};

	/**
	 * Since SUMO can't yet provide geographical info about the map via TraCI,
	 * and it's needed to geo-localize city maps (e.g. with Google Maps), this
	 * method tries to read the geographic offset from an external file. This
	 * information is contained in the network description file whose path is
	 * specified in the config file. So this method will look there.
	 * <p>
	 * NOTE: I marked this as "deprecated" since it may not work with the HTTP
	 * retrieval feature. Testing needed.
	 * @param configFile
	 * @return
	 */
	@Deprecated
	private static Point2D lookForGeoOffset(String configFile) {
		try {
			ContentHandler sumoConfHandler = new DefaultHandler() {

				boolean doRead = false;

				@Override
				public void startElement(String namespaceURI, String localName,
						String qName, Attributes atts) throws SAXException {
					if (localName.equals("net-file"))
						doRead = true;
				}

				@Override
				public void characters(char[] ch, int start, int length)
						throws SAXException {
					if (doRead) {
						throw new DataFoundException(new String(ch, start,
								length));
					}
				}
			};

			String netFile = null;

			XMLReader xmlReader = XMLReaderFactory
					.createXMLReader("org.apache.xerces.parsers.SAXParser");

			xmlReader.setContentHandler(sumoConfHandler);
			try {
				xmlReader
						.parse(new InputSource(new FileInputStream(configFile)));
				return null;
			} catch (DataFoundException dfe) {
				netFile = dfe.getMessage();
			}

			String basePath = configFile.substring(0, configFile
					.lastIndexOf(File.separatorChar));
			String absNetFile = basePath + File.separatorChar + netFile;

			ContentHandler netDescHandler = new DefaultHandler() {

				boolean doRead = false;

				@Override
				public void startElement(String namespaceURI, String localName,
						String qName, Attributes atts) throws SAXException {
					if (localName.equals("net-offset"))
						doRead = true;
				}

				@Override
				public void characters(char[] ch, int start, int length)
						throws SAXException {
					if (doRead) {
						throw new DataFoundException(new String(ch, start,
								length));
					}
				}
			};

			xmlReader = XMLReaderFactory
					.createXMLReader("org.apache.xerces.parsers.SAXParser");

			xmlReader.setContentHandler(netDescHandler);
			try {
				xmlReader
						.parse(new InputSource(new FileInputStream(absNetFile)));
				return null;
			} catch (DataFoundException dfe) {
				String geoOffsetStr = dfe.getMessage();
				String[] geoOffsetStrFields = geoOffsetStr.split(",");
				return new Point2D.Float(Float
						.parseFloat(geoOffsetStrFields[0]), Float
						.parseFloat(geoOffsetStrFields[1]));
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new Point2D.Float(0, 0);
		}

	}

	/**
	 * Returns whether the inner-junction links will be read.
	 * @return
	 */
	public boolean isReadInternalLinks() {
		return readInternalLinks;
	}

	/**
	 * Enables or disable reading of internal, inner-junction links. If such
	 * feature is changed, the lanes will be re-read from SUMO.
	 * @param readInternalLinks
	 */
	public void setReadInternalLinks(boolean readInternalLinks) {
		if (this.readInternalLinks != readInternalLinks) {
			cachedRoads = null;
		}
		
		this.readInternalLinks = readInternalLinks;
	}

	/**
	 * Sets the maximum speed (in m/s) of a certain lane. Note that it doesn't
	 * affect subsequent rerouting (for that, use e.g.
	 * {@link #changeEdgeTravelTime(int, int, String, float)}).
	 * 
	 * @param laneID
	 * @param vmax
	 * @throws IOException
	 */
	public void changeLaneMaxVelocity(String laneID, float vmax) throws IOException {
		ChangeLaneStateQuery clsq = new ChangeLaneStateQuery(socket, laneID);
		clsq.changeMaxVelocity(vmax);
	}

	/**
	 * Sets the travel time of a given edge in the specified time frame.
	 * Subsequent rerouting of vehicles (either with {@link Vehicle#reroute()}
	 * or {@link Vehicle#changeRoute(String, Number)}) will be affected by this
	 * setting, if they don't have another specified travel time for this edge.
	 * 
	 * @param begin
	 * @param end
	 * @param edgeID
	 * @param travelTime
	 * @throws IOException
	 */
	public void changeEdgeTravelTime(int begin, int end, String edgeID, float travelTime) throws IOException {
		ChangeEdgeStateQuery cesq = new ChangeEdgeStateQuery(socket, edgeID);
		cesq.changeGlobalTravelTime(begin, end, travelTime);
	}

	/**
	 * Returns the globally-specified travel time of an edge in the current
	 * time step.
	 * @param edgeID
	 * @return
	 * @throws IOException
	 */
	public float getEdgeTravelTime(String edgeID) throws IOException {
		return getEdgeTravelTime(edgeID, currentSimStep);
	}

	/**
	 * Returns the globally-specified travel time of an edge in a given time
	 * step.
	 * @param edgeID
	 * @param time
	 * @return
	 * @throws IOException
	 */
	private float getEdgeTravelTime(String edgeID, int time) throws IOException {
		RetrieveEdgeStateQuery resq = new RetrieveEdgeStateQuery(socket, edgeID);
		return resq.getGlobalTravelTime(time);
	}

	public Map<String, String> getAllVehiclesEdge() throws IOException {
		MultiVehiclePositionQuery mvpq = new MultiVehiclePositionQuery(socket);
		
		Set<String> vehicleIDs = new HashSet<String>();
		for (Vehicle v : vehicles.values()) {
			vehicleIDs.add(v.getID());
		}
		
		return mvpq.getVehiclesEdge(vehicleIDs);
	}
	
	public void setGetVehiclesEdgeAtSimStep(boolean state) {
		getVehiclesEdgeAtSimStep = state;
	}
	
}

