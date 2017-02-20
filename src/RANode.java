import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;


/*
 *
 * Classe que emula un node de l'algoritme Ricart&Agrawala
 *
 */
public class RANode extends Thread{
	public int id;
	public LamportClock ts;
	public ArrayList<LamportQueueNode> q;

	public boolean debug = false;
	
	public Socket sock; 
	public PrintWriter stdOut;
	public BufferedReader stdIn;
	
	public Socket sockHWP1;
	public BufferedReader stdInP1;
	
	public Socket sockHWP2;
	public PrintWriter stdOutP2;

	
	public RANode(int id, LamportClock ts){
		this.id = id;
		this.ts = ts;
		this.q = new ArrayList<LamportQueueNode>();
	}
	
	/*
	 *
	 * Sobreescriu el metode Run de la classe Thread
	 *
	 */
	public void run(){
		initConfig();		
		doIterations();
	}
	
	/*
	 *
	 * Inicialitza la configuració dels sockets
	 *
	 */
	public void initConfig(){
		
		try {
			sleep(1000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
		if (id == 1){
			connectSocketHWP1();
			listenSocket();
		}else{
			try {
				sleep(250);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			connectSocket();
			connectSocketHWP2();
		}
	}
	
	/*
	 *
	 * Inicialitza el socket que es conectarà amb l'altre process LightWeight 
	 *
	 */
	public void listenSocket(){
		
		try{
			ServerSocket serverSock = new ServerSocket(9600);
			if (debug) System.out.println("[DEBUG] Process " + id + " listening...");
			sock = serverSock.accept();
			if (debug) System.out.println("[DEBUG] Process " + id + " connected");

			stdOut = new PrintWriter(sock.getOutputStream(), true);
			stdIn = new BufferedReader(new InputStreamReader(sock.getInputStream()));
					
			serverSock.close(); 
					
		}catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 *
	 * Inicialitza el socket que es conectarà al process HeavyWeigh 2
	 *
	 */
	public void connectSocketHWP1(){
		try {
			if (debug) System.out.println("[DEBUG] Process " + id + " connecting to HWP1...");

			sockHWP1 = new Socket("127.0.0.1", 5903);	
			
			stdInP1 = new BufferedReader(new InputStreamReader(sockHWP1.getInputStream()));
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 *
	 * Inicialitza el socket que es conectarà al process HeavyWeigh 2
	 *
	 */
	public void connectSocketHWP2(){
		try {
			if (debug) System.out.println("[DEBUG] Process " + id + " connecting to HWP2...");
			
			sockHWP2 = new Socket("127.0.0.1", 5904);	
			
			stdOutP2 = new PrintWriter(sockHWP2.getOutputStream(), true);
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 *
	 * Inicialitza el socket que es conectarà amb l'altre process LightWeight 
	 *
	 */
	public void connectSocket(){
		try {
			if (debug) System.out.println("[DEBUG] Process " + id + " connecting...");
			
			sock = new Socket("127.0.0.1", 9600);	
			
			stdOut = new PrintWriter(sock.getOutputStream(), true);
			stdIn = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 *
	 * Bucle infinit, obté un nou missatge y el processa
	 *
	 */
	private void doIterations(){
		String message;
		requestCS();
		
		while (true){	
			message = getMessage();
			processMessage(message);
		}
	}
	
	/*
	 *
	 * Llegeix el primer missatge del socket
	 *
	 */
	private String getMessage(){
		String inputLine = "initValue";
		if (debug) System.out.println("[DEBUG] Process " + id + " waiting...");
		try {
			inputLine = stdIn.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (debug) System.out.println("[DEBUG] Process " + id + " recieved message: "+inputLine);
		
		return inputLine;
	}
	

	/*
	 *
	 * Processa el missatge rebut
	 *
	 */
	private void processMessage(String message){
		String[] parts;
		
		parts = message.split("-");
		
		if (parts[0].equals("REQUEST")){
			if (debug) System.out.println("[DEBUG] Process " + id + " ts.ticks: "+ts.ticks+" recieved ts.ticks: "+Integer.parseInt(parts[1]));

			if (ts.ticks > Integer.parseInt(parts[1]) || (ts.ticks == Integer.parseInt(parts[1]) && Integer.parseInt(parts[2]) < id)){
				stdOut.println("REPLY-"+ts.ticks+"-"+id);	
			}else{
				if (debug) System.out.println("[DEBUG] Process " + id + " queued process "+Integer.parseInt(parts[2]));
				q.add(new LamportQueueNode(Integer.parseInt(parts[1]), Integer.parseInt(parts[2])));
			}
		}
		else if (parts[0].equals("REPLY")){
			
			if (id == 1){
				try {
					stdInP1.readLine();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			for (int i = 0; i< 10 ; i++){
				System.out.println("Sóc el procés lightweight B"+id);
			}
			try {
				sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			if (id == 2){
				stdOutP2.println("FREE");
			}
			
			releaseCS();
			requestCS();

		}
	}
	
	/*
	 *
	 * Demana accès a la zona crítica
	 *
	 */
	public void requestCS(){
		ts.tick();
		broadcastMsg("REQUEST");
	}
	
	/*
	 *
	 * Allibera la zona crítica
	 *
	 */
	public void releaseCS(){
		if (!q.isEmpty()){
			q.remove(0);
			stdOut.println("REPLY-"+ts.ticks+"-"+id);
		}	
	}
	
	/*
	 *
	 * Envia un missatge a tots els nodes
	 *
	 */
	public void broadcastMsg(String type){
		stdOut.println(type+"-"+ts.ticks+"-"+id);
	}
}
