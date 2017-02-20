import java.io.BufferedReader;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.Semaphore;


public class StdListener extends Thread{

	public BufferedReader stdIn;
	public Queue shared;
	public int id;
	public int type;
	public Semaphore semaphore;
	
	public StdListener(BufferedReader stdIn, Queue<String> shared, Semaphore semaphore, int id, int type){
		this.stdIn = stdIn;
		this.shared = shared;
		this.semaphore = semaphore;
		this.id = id;
		this.type = type;
	}
	
	@Override
	public void run() {
		String inputLine = "InitValue";
		while (true){
			//System.out.println("[DEBUG] Process "+id+" listening from stdin" + type +" "+stdIn);
			try {
				inputLine = stdIn.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			synchronized (shared) {
				shared.add(inputLine);
			}
			//System.out.println("[DEBUG] Process " + id + " sends notify");
			semaphore.release();
			
		}
	}

}
