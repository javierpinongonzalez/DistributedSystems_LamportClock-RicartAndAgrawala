import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;


public class LamportNode extends Thread{
	public int id;
	public LamportClock ts;
	public ArrayList<LamportQueueNode> q;

	public boolean debug = false;
	public Queue shared = new LinkedList<>();
	Semaphore getMessageSemaphore = new Semaphore(0);
	
	public Socket sock1; //forward/listen socket
	public Socket sock2; //backward/talk socket
	public PrintWriter stdOut1;
	public PrintWriter stdOut2;
	public BufferedReader stdIn1;
	public BufferedReader stdIn2;
	
	public Socket sockHWP1;
	public BufferedReader stdInHWP1;

	public Socket sockHWP3;
	public PrintWriter stdOutHWP3;

	public int replyCounter=0;
	
	
	public LamportNode (int id, LamportClock ts){
		this.id = id;
		this.ts = ts;
		this.q = new ArrayList<LamportQueueNode>();
	}
	
	public void run(){
		initConfig();
		
		initStdInListeners();
		
		doIterations();
		
		//closeConfig();
		//System.out.println("Bye from thread num: "+id);
	}
	
	private void initConfig(){
		if (debug) System.out.println("[DEBUG] Process " + id + " starting initConfig()");
		
		try {
			sleep(1000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		switch (id) {
		case 1:
			configSock1();
			try {
				sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			configSock2();
			
			configSockHWP1();
			
			break;
			
		case 2:
			configSock2();
			configSock1();
			break;
			
		case 3:
			configSock2();
			configSock1();
			configSockHWP3();
			break;
			
		default:
			System.out.println("[ERROR] Unknown id process " + id);
			break;
		}
	}
	
	private void configSockHWP1(){
		try {
			sleep(100);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			if (debug) System.out.println("[DEBUG] Process " + id + " connecting to HWP1...");
			
			sockHWP1 = new Socket("127.0.0.1", 5901);
			
			stdInHWP1 = new BufferedReader(new InputStreamReader(sockHWP1.getInputStream()));
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void configSockHWP3(){
		try {
			if (debug) System.out.println("[DEBUG] Process " + id + " connecting to HWP3...");
			
			sockHWP3 = new Socket("127.0.0.1", 5902);
			
			stdOutHWP3 = new PrintWriter(sockHWP3.getOutputStream(), true);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void configSock1(){
		//Listen
		
		try{
			ServerSocket serverSock = new ServerSocket(6900+id);
			if (debug) System.out.println("[DEBUG] Process " + id + " listening...");
			sock1 = serverSock.accept();
			if (debug) System.out.println("[DEBUG] Process " + id + " connected");

			stdOut1 = new PrintWriter(sock1.getOutputStream(), true);
			stdIn1 = new BufferedReader(new InputStreamReader(sock1.getInputStream()));
			
			serverSock.close(); //????
			
		}catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private void configSock2(){
		//Connect
		
		try {
			if (debug) System.out.println("[DEBUG] Process " + id + " connecting...");
			
			if (id == 1)
			{
				sock2 = new Socket("127.0.0.1", 6903);
			}else {
				sock2 = new Socket("127.0.0.1", 6900+id-1);
			}
			
			stdOut2 = new PrintWriter(sock2.getOutputStream(), true);
			stdIn2 = new BufferedReader(new InputStreamReader(sock2.getInputStream()));
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void initStdInListeners(){
		try {
			sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//if (debug) System.out.println("[DEBUG] Process " + id + " stdin1 " +stdIn1);
		//if (debug) System.out.println("[DEBUG] Process " + id + " stdin2 " +stdIn2);

		
		//if (debug) System.out.println("[DEBUG] Process " + id + " initializing stdListener");
		
		
		StdListener stdListener1 = new StdListener(stdIn1, shared, getMessageSemaphore, id, 1);
		stdListener1.start();
		try {
			sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		StdListener stdListener2 = new StdListener(stdIn2, shared, getMessageSemaphore,id, 2);
		stdListener2.start();
	}
	
	private void doIterations(){
		String message;
		
		stdOut1.println("REQUEST-"+ts.ticks+"-"+id);
		stdOut2.println("REQUEST-"+ts.ticks+"-"+id);
		q.add(new LamportQueueNode(ts.ticks, id));
		
		//stdOut1.println("RELEASE");
		//stdOut2.println("RELEASE");
		while (true){	
			//getMessage
			message = getMessage();
			//processMessage
			processMessage(message);
				
		}
		
		
	}
	
	private String getMessage(){
		String recievedMessage = "initialValue";
			
		if (debug) System.out.println("[DEBUG] Process " + id + " waiting...");
		
		try {
			getMessageSemaphore.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		synchronized (shared) {
			recievedMessage = (String) shared.poll();
		}
		
		if (debug) System.out.println("[DEBUG] Process " + id + " recieved message: "+recievedMessage);

		return recievedMessage;
	}
	
	private void processMessage(String message){
		String[] parts;
		
		parts = message.split("-");
		
		ts.receiveAction(Integer.parseInt(parts[1]));
		
		if (parts[0].equals("REQUEST")){
			//DO REQUEST
			q.add(new LamportQueueNode(Integer.parseInt(parts[1]), Integer.parseInt(parts[2])));			
			Collections.sort(q);

			
			//send REPLY
			sendReply(Integer.parseInt(parts[2]));
		}
		else if (parts[0].equals("REPLY")){
			//DO REPLY
			replyCounter ++;
			
			if (replyCounter>=2 && q.get(0).id == id){
				manageCS();				
			}

		}
		else if (parts[0].equals("RELEASE")){
			//DO RELEASE
			q.remove(0);
			if (q.get(0).id == id){
				manageCS();
			}
		}
		//if (debug) System.out.println("[DEBUG] Process " + id + " Parts[0] "+parts[0]);

		
	}
	
	private void sendReply(int id){
		//ts.sendAction();

		switch (this.id){
			case 1:
				if (id == 2){
					stdOut2.println("REPLY-"+ts.ticks+"-"+this.id);
				}else{ //id == 3
					stdOut1.println("REPLY-"+ts.ticks+"-"+this.id);
				}
				break;
			case 2:
				if (id == 1){
					stdOut1.println("REPLY-"+ts.ticks+"-"+this.id);
				}else{ //id == 3
					stdOut2.println("REPLY-"+ts.ticks+"-"+this.id);
				}
				break;
			case 3:
				if (id == 1){
					stdOut2.println("REPLY-"+ts.ticks+"-"+this.id);
				}else{ //id == 2
					stdOut1.println("REPLY-"+ts.ticks+"-"+this.id);
				}
				break;
		}
	}
	
	private void manageCS(){
		if (debug) System.out.println("[DEBUG] ***************************************************************");
		if (debug) System.out.println("[DEBUG] Process " + id + " has win the CS, here will be the printed msg");
		if (debug) System.out.println("[DEBUG] ***************************************************************");
		
		if (id == 1){
			waitHWToken();
		}
		
		for (int i = 0; i< 10 ; i++){
			System.out.println("Sóc el procés lightweight A"+id);
		}
		
		try {
			sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (id == 3){
			releaseHWToken();
		}
		
		q.remove(0);
		
		//send RELEASE
		
		//ts.sendAction();

		stdOut1.println("RELEASE-"+ts.ticks+"-"+this.id);
		stdOut2.println("RELEASE-"+ts.ticks+"-"+this.id);
		
		ts.sendAction();

		//send REQUEST
		stdOut1.println("REQUEST-"+ts.ticks+"-"+id);
		
		stdOut2.println("REQUEST-"+ts.ticks+"-"+id);
		
		q.add(new LamportQueueNode(ts.ticks, id));
	}
	
	private void waitHWToken(){
		try {
			stdInHWP1.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void releaseHWToken(){
		stdOutHWP3.println("FREE");
	}
	
	//DEBUG function
	private void printQ(){
		for (int i = 0; i < q.size() ; i++){
			System.out.println("[DEBUG] Process " + id + " q["+i+"].id: "+q.get(i).id +" & q["+i+"].ts: "+q.get(i).ts);
		}
	}

}
