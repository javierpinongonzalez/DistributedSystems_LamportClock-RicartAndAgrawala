import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;


/*
 *
 * Classe que realitza les tasques de procès HeavyWeight per l'algoritme Lamport Clock
 *
 */
public class LamportHWProcess extends Thread{
	
	boolean debug;
	
	public Socket sock; 
	public PrintWriter stdOut;
	public BufferedReader stdIn;
	
	public Socket sockP1;
	public PrintWriter stdOutP1;
	
	public Socket sockP3; 
	public BufferedReader stdInP3;
	
	public LamportHWProcess(boolean debug){
		this.debug = debug;
	}
	
	/*
	 *
	 * Sobreescriu el metode Run de la classe Thread
	 *
	 */
	public void run(){
		
		initConfig();
		
		LamportNode [] lamportList = new LamportNode[3];

		
		for (int i=1 ; i<=3 ; i++){
			lamportList[i-1] = new LamportNode(i, new LamportClock());
			lamportList[i-1].debug = debug;
		}
				
		for (int i=0; i<3 ; i++){
			try {
				Thread.sleep(250);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			lamportList[i].start();
		}
		
		initSockP3();
		initSockP1();
		
		doIterations();
		
	}
	
	/*
	 *
	 * Inicialitza la configuració dels sockets
	 *
	 */
	private void initConfig(){
		try {
			ServerSocket serverSock = new ServerSocket(5900);
			if (debug) System.out.println("[DEBUG] Process LamportHW listening...");
			sock = serverSock.accept();
			if (debug) System.out.println("[DEBUG] Process LamportHW connected");

			stdOut = new PrintWriter(sock.getOutputStream(), true);
			stdIn = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			
			serverSock.close();
					
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	/*
	 *
	 * Inicializa els sockets amb el procès 1
	 *
	 */
	private void initSockP1(){
		try {
			ServerSocket serverSock = new ServerSocket(5901);
			if (debug) System.out.println("[DEBUG] Process LamportHWP1 listening...");
			sockP1 = serverSock.accept();
			if (debug) System.out.println("[DEBUG] Process LamportHWP1 connected");

			stdOutP1 = new PrintWriter(sockP1.getOutputStream(), true);
			
			serverSock.close();
					
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	/*
	 *
	 * Inicializa els sockets amb el procès 3
	 *
	 */
	private void initSockP3(){
		try {
			ServerSocket serverSock = new ServerSocket(5902);
			if (debug) System.out.println("[DEBUG] Process LamportHWP3 listening...");
			sockP3 = serverSock.accept();
			if (debug) System.out.println("[DEBUG] Process LamportHWP3 connected");

			stdInP3 = new BufferedReader(new InputStreamReader(sockP3.getInputStream()));
			
			serverSock.close();
					
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}	
		
	}

	/*
	 *
	 * Bucle infinit per a passar el token
	 *
	 */	
	private void doIterations(){
		while (true){
			
			stdOutP1.println("FREE");
			try {
				stdInP3.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
			stdOut.println("TOKEN");
			
			try {
				stdIn.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
