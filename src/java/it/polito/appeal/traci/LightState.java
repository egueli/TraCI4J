package it.polito.appeal.traci;

/**
 * Represents the states of each traffic light 
 * @author Enrico Gueli &lt;enrico.gueli@polito.it&gt;
 *
 */
public enum LightState {
	RED           ('r'),
	RED_NODECEL   ('R'),
	YELLOW        ('y'),
	YELLOW_NODECEL('Y'),
	GREEN         ('g'),
	GREEN_NODECEL ('G'),
	OFF           ('O');
	
	final char symbol;
	LightState(char symbol) {
		this.symbol = symbol;
	}
	public boolean isRed()    { return symbol=='R' || symbol=='r'; }
	public boolean isYellow() { return symbol=='Y' || symbol=='y'; }
	public boolean isGreen()  { return symbol=='G' || symbol=='g'; }
	public boolean isOff()    { return symbol=='O'; }
	public boolean willDecelerate() {return Character.isLowerCase(symbol); }
	static LightState fromSymbol(char symbol) {
		for (LightState ls : LightState.values())
			if (symbol == ls.symbol)
				return ls;
		return null;
	}
}