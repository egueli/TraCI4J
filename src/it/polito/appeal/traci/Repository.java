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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Represents a collection of TraCI objects, that may or may not be complete
 * w.r.t. its counterpart in SUMO.
 * <p>
 * It requires a {@link StringListQ} query that will be used to retrieve the
 * complete list of IDs to be able to check for correctness of requested IDs.
 * <p>
 * In order for a repository to be efficient, it should be the only class that
 * instantiates new items. 
 * @author Enrico Gueli &lt;enrico.gueli@polito.it&gt;
 *
 */
public class Repository<V extends TraciObject<?>> {
	private final Map<String, V> objects = new HashMap<String, V>();
	/*
	 * the factory is not final: there's a setter for those cases when the
	 * factory is the subclass or points to the subclass, that doesn't exist
	 * yet.
	 */
	private ObjectFactory<V> factory;
	private final StringListQ idListQuery;
	private Set<String> idSet;
	
	/**
	 * Constructor for the repository.
	 * @param factory the {@link ObjectFactory} that will be used to make new
	 * objects when requested the first time
	 * @param idListQuery a reference to a query of list of IDs. This query must
	 * be invariant, i.e. must not give different results at different simulation
	 * time steps.
	 */
	public Repository(ObjectFactory<V> factory, StringListQ idListQuery) {
		this.factory = factory;
		this.idListQuery = idListQuery;
	}
	
	protected void setObjectFactory(ObjectFactory<V> factory) {
		this.factory = factory;
	}

	/**
	 * If the repository already holds an object with the given ID, it returns
	 * that one; otherwise, it checks that the ID is contained in the query; if
	 * so, asks the {@link ObjectFactory} passed to the
	 * constructor to build one. That value is stored here for later requests
	 * and returned.
	 * 
	 * @param id
	 * @return
	 * @throws IOException if, in the first call of this method, a query is sent
	 * to SUMO but something bad happened
	 * @throws IllegalArgumentException if the given ID is not present in the
	 * repository
	 */
	public V getByID(String id) throws IOException {
		if (!getIDs().contains(id))
			throw new IllegalArgumentException("ID not found in repository: " + id);
		
		if (!objects.containsKey(id)) {
			objects.put(id, factory.newObject(id));
		}
		
		return objects.get(id);
	}
	
	public Set<String> getIDs() throws IOException {
		if (idSet == null) {
			idSet = new HashSet<String>(idListQuery.get());
		}
		return Collections.unmodifiableSet(idSet);
	}
	
	public Map<String, V> getAll() throws IOException {
		for (String id : getIDs()) {
			getByID(id); // used only for its collateral effects
		}
		return Collections.unmodifiableMap(objects);
	}
	
	static class Edges extends Repository<Edge> {
		Edges(final DataInputStream dis, final DataOutputStream dos, StringListQ idListQuery) {
			super(new ObjectFactory<Edge>() {
				@Override
				public Edge newObject(String objectID) {
					return new Edge(dis, dos, objectID);
				}
			}, idListQuery);
		}
	}
	
	static class Lanes extends Repository<Lane> {
		Lanes(final DataInputStream dis, final DataOutputStream dos, final Repository<Edge> edges, StringListQ idListQuery) {
			super(null, idListQuery);
			
			setObjectFactory(new ObjectFactory<Lane>() {
				@Override
				public Lane newObject(String objectID) {
					return new Lane(dis, dos, objectID, edges, Lanes.this);
				}
			});
		}
	}
	
	static class POIs extends Repository<POI> {
		public POIs(final DataInputStream dis, final DataOutputStream dos, StringListQ idListQuery) {
			super(new ObjectFactory<POI>() {
				@Override
				public POI newObject(String objectID) {
					return new POI(objectID, dis, dos);
				}
			}, idListQuery);
		}
	}
	
	/*
	 * TODO add repository definitions for other SUMO object classes 
	 */
}
