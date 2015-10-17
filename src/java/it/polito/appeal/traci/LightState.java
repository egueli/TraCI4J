package it.polito.appeal.traci;

/**
 * Represents the states of each traffic light 
 * @author Enrico Gueli &lt;enrico.gueli@polito.it&gt;
 *
 */
public enum LightState {
	/** red light */                           RED           ('r'),
	/** red light w/o deceleration (?) */      RED_NODECEL   ('R'),
	/** yellow light */                        YELLOW        ('y'),
	/** yellow light w/out deceleration (?) */ YELLOW_NODECEL('Y'),
	/** green light */                         GREEN         ('g'),
	/** green light w/out deceleration */      GREEN_NODECEL ('G'),
	/** light off */                           OFF           ('O');
	
	final char symbol;
	LightState(char symbol) {
		this.symbol = symbol;
	}
	/** @return <code>true</code> if it's a red light */
	public boolean isRed()    { return symbol=='R' || symbol=='r'; }
	
	/** @return <code>true</code> if it's a yellow light */
	public boolean isYellow() { return symbol=='Y' || symbol=='y'; }
	
	/** @return <code>true</code> if it's a green light */
	public boolean isGreen()  { return symbol=='G' || symbol=='g'; }
	
	/** @return <code>true</code> if this light is off */
	public boolean isOff()    { return symbol=='O'; }
	
	/** @return <code>true</code> if vehicles will decelerate with this light  */
	public boolean willDecelerate() {return Character.isLowerCase(symbol); }
	
	static LightState fromSymbol(char symbol) {
		for (LightState ls : LightState.values())
			if (symbol == ls.symbol)
				return ls;
		return null;
	}
}