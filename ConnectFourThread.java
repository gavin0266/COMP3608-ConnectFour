import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

public class ConnectFourThread implements Callable<Integer>{
	
	private String state;
	private String player;
	private AtomicInteger result;
	
	public ConnectFourThread(String state, String player, AtomicInteger result) {
		this.state = state;
		this.player = player;
		this.result = result;
		
	}

	public Integer call(){	
		
		ConnectFour connectFour = new ConnectFour(state, player, 'A', 1);
		int r = connectFour.tournament();
		result.set(r);
		while(!Thread.interrupted()) {
//			if(connectFour.getDepth() == 2) return 0;
//			System.out.println("Depth: " + connectFour.getDepth());
			connectFour.increaseDepth();
			result.set(connectFour.tournament());
			
		}
		return 0;
		
	}
	
}
