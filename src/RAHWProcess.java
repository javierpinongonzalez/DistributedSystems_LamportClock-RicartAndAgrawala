import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;



public class RAHWProcess extends Thread{
	boolean debug;
	
	public Socket sock; 
	public PrintWriter stdOut;
	public BufferedReader stdIn;
	
	
	public Socket sockP1;
	public PrintWriter stdOutP1;
	
	public Socket sockP2; 
	public BufferedReader stdInP2;
	
	
	public RAHWProcess(boolean debug){
		this.debug = debug;
	}
	
	public void run(){
		
		initConfig();
		
		RANode [] RAList = new RANode[2];
		//System.out.println("Hello World");
		
		for (int i=1 ; i<=2 ; i++){
			RAList[i-1] = new RANode(i, new LamportClock());
			RAList[i-1].debug = debug;
		}
				
		for (int i=0; i<2 ; i++){
			try {
				Thread.sleep(250);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			RAList[i].start();
		}
		
		initSockP1();
		initSockP2();
		
		doIterations();
	}
	
	private void initConfig(){
		try {
			sleep(250);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			if (debug) System.out.println("[DEBUG] Process RAHW connecting...");
			
			sock = new Socket("127.0.0.1", 5900);
			stdOut = new PrintWriter(sock.getOutputStream(), true);
			stdIn = new BufferedReader(new InputStreamReader(sock.getInputStream()));
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	private void initSockP1(){
		try {
			ServerSocket serverSock = new ServerSocket(5903);
			if (debug) System.out.println("[DEBUG] Process RAHWP1 listening...");
			sockP1 = serverSock.accept();
			if (debug) System.out.println("[DEBUG] Process RAHWP1 connected");

			stdOutP1 = new PrintWriter(sockP1.getOutputStream(), true);
			
			serverSock.close();
					
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	private void initSockP2(){
		try {
			ServerSocket serverSock = new ServerSocket(5904);
			if (debug) System.out.println("[DEBUG] Process RAHWP2 listening...");
			sockP2 = serverSock.accept();
			if (debug) System.out.println("[DEBUG] Process RAHWP2 connected");

			stdInP2 = new BufferedReader(new InputStreamReader(sockP2.getInputStream()));
			
			serverSock.close();
					
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
	}
	
	private void doIterations(){
		while (true){
			try {
				stdIn.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			stdOutP1.println("FREE");
			try {
				stdInP2.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			stdOut.println("TOKEN");
		}
	}
}
