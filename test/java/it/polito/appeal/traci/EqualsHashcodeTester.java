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

import static org.junit.Assert.*;

public class EqualsHashcodeTester {
	public static <T> void testReflexive(T x) {
		assertTrue("x.equals(x)", x.equals(x));
	}

	public static <T> void testEqualToIncompatibleType(T x) {
		assertFalse("x.equals(something)", x.equals(new Object()));
	}
	
	public static <T> void testEqualToNull(T x) {
		assertFalse("x.equals(null)", x.equals(null));
	}
	
	public static <T> void testSymmetric(T x, T y) {
		assertEquals("x.equals(y) == y.equals(x)", x.equals(y), y.equals(x));
	}
	
	public static <T> void testHashcodeEqualIfEqual(T x, T y) {
		if (!x.equals(y))
			throw new IllegalArgumentException("x.equals(y) should return true");
		
		assertEquals("x.hashCode() == y.hashCode()", x.hashCode(), y.hashCode());
	}
	
	public static <T> void testNotEqualIfHashcodeDiffers(T x, T y) {
		if (x.hashCode() == y.hashCode())
			throw new IllegalArgumentException("x.equals(y) should return true");
		
		assertFalse("x.equals(y)", x.equals(y));
	}
	
	public static <T> void testAll(T same1, T same2, T different) {
		/*
		 * basic sanity checks on the input arguments
		 */
		if (same1 == same2)
			throw new IllegalArgumentException("same1 and same2 must not be the same object");
		
		if (same1 == different)
			throw new IllegalArgumentException("same1 and different are the same object");
		
		if (same2 == different)
			throw new IllegalArgumentException("same2 and different are the same object");
		
		/*
		 * basic semantic tests
		 */
		assertEquals(same1, same2);
		assertEquals(same2, same1);
		assertFalse(same1.equals(different));
		assertFalse(different.equals(same1));
		assertFalse(same2.equals(different));
		assertFalse(different.equals(same2));
		assertEquals(same1.hashCode(), same2.hashCode());
		assertFalse(same1.hashCode() == different.hashCode());
		assertFalse(same2.hashCode() == different.hashCode());
		
		testReflexive(same1);
		testEqualToIncompatibleType(same1);
		testEqualToNull(same1);
		testReflexive(same2);
		testEqualToIncompatibleType(same2);
		testEqualToNull(same2);
		testSymmetric(same1, same2);
		testHashcodeEqualIfEqual(same1, same2);
		testNotEqualIfHashcodeDiffers(same1, different);
	}
}
