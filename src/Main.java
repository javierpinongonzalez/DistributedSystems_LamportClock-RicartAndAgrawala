
public class Main {
	public static void main(String [ ] args)
	{
		boolean debug = false;
		
		RAHWProcess ra = new RAHWProcess(debug);
		LamportHWProcess lamport = new LamportHWProcess(debug);
		
		ra.start();
		lamport.start();
		
	}
}
