package it.polito.appeal.traci;

import it.polito.appeal.traci.protocol.Constants;

import java.io.IOException;

import de.uniluebeck.itm.tcpip.Storage;

/**
 * Represents the set of links for every signal in a TLS.
 * @author Enrico Gueli &lt;enrico.gueli@polito.it&gt;
 *
 */
public class ControlledLinks {
	private final ControlledLink[][] links;
	ControlledLinks(Storage content, Repository<Lane> laneRepo) throws TraCIException, IOException {
		Utils.checkType(content, Constants.TYPE_INTEGER);
		int signals = content.readInt();
		links = new ControlledLink[signals][];
		for (int s=0; s < signals; s++) {
			Utils.checkType(content, Constants.TYPE_INTEGER);
			int linksOfSignal = content.readInt();
			links[s] = new ControlledLink[linksOfSignal];
			for (int l=0; l<linksOfSignal; l++) {
				links[s][l] = new ControlledLink(content, laneRepo);
			}
		}

	}
	
	/**
	 * @return an array describing the controlled links of a TLS.
	 * The first index points to a signal; the second index points to a link
	 * controlled by that signal.
	 */
	public ControlledLink[][] getLinks() {
		return links;
	}
}