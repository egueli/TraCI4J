/*   
    Copyright (C) 2015 ApPeAL Group, Politecnico di Torino

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

package it.polito.appeal.traci.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.PropertyConfigurator;
import org.junit.Test;

import it.polito.appeal.traci.Lane;
import it.polito.appeal.traci.ReadObjectVarQuery;
import it.polito.appeal.traci.Repository;

public class MultiThreadTest extends SingleSimTraCITest {

	static {
		// Log4j configuration must be done only once, otherwise output will be duplicated for each test
		
		// Basic configuration that outputs everything		
		//org.apache.log4j.BasicConfigurator.configure();
		
		// Configuration specified by a properties file
		PropertyConfigurator.configure("test/log4j.properties");
	}

	
	@Test
	public void testSingleThreadBunchOfQueries() throws IOException {
		runABunchOfQueries();
	}
	
	@Test
	public void testDoubleThreadQueries() throws IOException {
		final int TASKS = 2;
		ExecutorService execService = Executors.newFixedThreadPool(TASKS);
		CompletionService<Void> completionService = new ExecutorCompletionService<Void>(execService);
		for (int i=0; i<TASKS; i++) {
			Callable<Void> task = new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					runABunchOfQueries();
					return null;
				}
				
			};
			completionService.submit(task);
		}
		
		for (int i=0; i<TASKS; i++) {
			try {
				Future<Void> future = completionService.take();
				future.get();
			}
			catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			catch (ExecutionException e) {
				if (e.getCause() instanceof IOException) {
					throw (IOException) e.getCause();
				}
				else {
					throw new RuntimeException(e.getCause());
				}
			}
		}
	}
	
	@Override
	protected String getSimConfigFileLocation() {
		return "test/sumo_maps/variable_speed_signs/test.sumo.cfg";
	}

	private void runABunchOfQueries() throws IOException {
		Repository<Lane> laneRepo = conn.getLaneRepository();
		List<Lane> lanes = new ArrayList<Lane>(laneRepo.getAll().values());
		Random random = new Random();
		for (int i=0; i<2000; i++) {
			Lane lane = lanes.get(random.nextInt(lanes.size()));
			lane.clearCache();
			for (ReadObjectVarQuery<?> query : lane.getAllReadQueries().values()) {
				query.get();
			}
		}
	}
}
