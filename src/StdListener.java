import java.io.BufferedReader;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.Semaphore;

/*
 *
 * Classe utilitzada com a socket listener per evitar congelar el thread principal
 *
 */
public class StdListener extends Thread{

	public BufferedReader stdIn;
	public Queue shared;
	public int id;
	public int type;
	public Semaphore semaphore;
	

	/*
	 *
	 * Constructor
	 *
	 */
	public StdListener(BufferedReader stdIn, Queue<String> shared, Semaphore semaphore, int id, int type){
		this.stdIn = stdIn;
		this.shared = shared;
		this.semaphore = semaphore;
		this.id = id;
		this.type = type;
	}
	
	/*
	 *
	 * Sobreescriu el metode Run de la classe Thread
	 *
	 */
	@Override
	public void run() {
		String inputLine = "InitValue";
		while (true){
			try {
				inputLine = stdIn.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
			synchronized (shared) {
				shared.add(inputLine);
			}
			semaphore.release();
			
		}
	}

}
