/*
 *
 * Classe que emula un Lamport Clock
 *
 */
public class LamportClock {
	public int ticks;
	
	/*
	 *
	 * Constructor
	 *
	 */
	public LamportClock(){
		ticks = 0; 
	}

	public void sendAction(){
		ticks++;
	} 

	public void tick(){
		ticks++;
	}
	public void receiveAction(int receivedValue){ 
		ticks = ticks>receivedValue? ticks+1 : receivedValue+1; 
	}  
}
