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

package it.polito.appeal.traci;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * A Lock implementation that doesn't really lock anything. Used when locking is not needed,
 * i.e. to improve performance in single-threaded applications.
 * @author Enrico Gueli &lt;enrico.gueli@polito.it&gt;
 *
 */
public class NullLock implements Lock {

	@Override
	public void lock() {
	}

	@Override
	public void lockInterruptibly() throws InterruptedException {
	}

	@Override
	public boolean tryLock() {
		return true;
	}

	@Override
	public boolean tryLock(long time, TimeUnit unit)
			throws InterruptedException {
		return true;
	}

	@Override
	public void unlock() {
	}

	@Override
	public Condition newCondition() {
		throw new UnsupportedOperationException();
	}

}
