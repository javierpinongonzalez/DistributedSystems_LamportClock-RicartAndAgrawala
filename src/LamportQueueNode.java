
public class LamportQueueNode implements Comparable<LamportQueueNode>{
	public int ts;
	public int id;
	
	public LamportQueueNode(int ts, int id) {
		this.ts = ts;
		this.id = id;
	}
	
	public int compareTo(LamportQueueNode lqn)
	{
		if (ts != lqn.ts){
			return(ts - lqn.ts);
		}else{
			return(id - lqn.id);
		}
	}
}
