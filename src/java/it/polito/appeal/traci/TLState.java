package it.polito.appeal.traci;

import java.util.Arrays;

/**
 * Represents the istantaneous state of a TLS, i.e. the status of each light in
 * the TLS.
 * 
 * @author Enrico Gueli &lt;enrico.gueli@polito.it&gt;
 * 
 */
public class TLState {
	/**
	 * The ordered list of traffic light states.
	 */
	
	public final LightState[] lightStates;
	
	/**
	 * Constructs a new instance from a string description of a phase (one
	 * letter for each signal).
	 * 
	 * @param phase
	 *            the description of a phase. It must contain only the following
	 *            characters: rRyYgGO
	 * @throws IllegalArgumentException
	 *             if the phase contains invalid characters
	 */
	public TLState(String phase) {
		final int len = phase.length();
		lightStates = new LightState[len];
		for (int i=0; i<len; i++) {
			final char ch = phase.charAt(i);
			final LightState ls = LightState.fromSymbol(ch);
			if (ls == null)
				throw new IllegalArgumentException("unknown TL symbol: " + ch);
			lightStates[i] = ls;
		}
	}
	
	/**
	 * Constructs an instance through an array of traffic light states.
	 * 
	 * @param lightStates
	 */
	public TLState(LightState[] lightStates) {
		this.lightStates = lightStates;
	}
	public String toString() {
		char[] desc = new char[lightStates.length];
		for (int i=0; i<desc.length; i++)
			desc[i] = lightStates[i].symbol;
		return new String(desc);
	}
	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (obj == this) return true;
		if (!(obj instanceof TLState)) return false;
		return Arrays.deepEquals(((TLState)obj).lightStates, lightStates);
	}
	@Override
	public int hashCode() {
		return Arrays.hashCode(lightStates);
	}
}