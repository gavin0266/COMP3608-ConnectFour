import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;


public class ConnectFour {
	
	private String state;
	private int player;
	private boolean alphaBeta;
	private int maxDepth;
	private int nodeCount;
	private int terminalCount;
	private int[] moveOrder;
	
	static final int HEIGHT = 6; //ROW
	static final int WIDTH = 7; //COLUMN
	
	public ConnectFour(String state, String player, char algorithm, int depth) {
		this.state = state;
		this.nodeCount = 0;
		if(player.equals("red"))
			this.player = 1;
		else
			this.player = 2;
		
		switch(algorithm) {
			case 'A':
				this.alphaBeta = true;
				break;
			case 'M':
				this.alphaBeta = false;
				break;
			default:
				this.alphaBeta = true;
				break;
		}
		this.maxDepth = depth;
		this.moveOrder = new int[] {3, 4, 2, 5, 1, 6, 0};
//		this.moveOrder = new int[] {0, 1, 2, 3, 4, 5, 6};
	}
	
	public void increaseDepth() {
		maxDepth++;
	}
	
	public int getDepth() {
		return maxDepth;
	}
	
	
	private BitBoard maxValueBB(BitBoard b, int depth, int alpha, int beta) {
		nodeCount++;
		
		//Terminal Node	
		int checkWin = b.utility(depth);	
		
		if(checkWin != 0) {
			b.value = checkWin;
			return b;
		}
		
		//Max-Depth reached
		if(depth == maxDepth) {
			int val = b.evaluation();
			b.value = val;
			return b;
		}
		
		b.value = -20000;
		BitBoard max = b;
		boolean fullBoard = true;
		//Branching				
		for(int col = 0; col < WIDTH; col++) {
			BitBoard cb = b.copy();
			if(cb.canPlay(moveOrder[col])) {
				
				fullBoard = false;
				cb.play(moveOrder[col], ((player+depth-1)%2)+1);

				if(depth==0)
					cb.col = moveOrder[col];
				cb = minValueBB(cb, 1+depth, alpha, beta);
				if(depth==0) {

					System.out.println("col: " + moveOrder[col] + " val: " + cb.value);
				}
				//Pruning
				if(cb.value >= beta) {
					b.value = cb.value;
					return b;
				}
				
				if(max.value < cb.value) {
					max = cb;
					alpha = Integer.max(alpha, max.value);
				}
				
			}			
		}
		
		if(fullBoard)
			b.value = 0;
		else
			b.value = max.value;
		
		if(depth == 0) {
			b.col = max.col;			
		}
		
		return b;
	}
	
	private BitBoard minValueBB(BitBoard b, int depth, int alpha, int beta) {
		nodeCount++;
		//Terminal Node
		int checkWin = b.utility(depth);		
		if(checkWin != 0) {
			b.value = checkWin;
			return b;
		}
		
		//Max-Depth reached
		if(depth == maxDepth) {
			int val = b.evaluation();
			b.value = val;
			return b;
		}
		
		b.value = 20000;
		BitBoard min = b;
		boolean fullBoard = true;
		//Branching				
		for(int col = 0; col < WIDTH; col++) {
			BitBoard cb = b.copy();
			if(cb.canPlay(moveOrder[col])) {
				fullBoard = false;
				cb.play(moveOrder[col], ((player+depth-1)%2)+1);
				if(depth==0)
					cb.col = moveOrder[col];
				cb = maxValueBB(cb, 1+depth, alpha, beta);
				
				
				//Pruning
				if(cb.value <= alpha) {
					b.value = cb.value;
					return b;
				}
				
				if(min.value > cb.value) {			
					min = cb;
					beta = Integer.min(beta, min.value);
				}
				
			}			
		}
		
		
		if(fullBoard)
			b.value = 0;
		else
			b.value = min.value;
		
		if(depth == 0)
			b.col = min.col;
		
		return b;
	}
	
	
	private int bitboardABSearch(BitBoard root) {
		BitBoard move;

		
		if(player == 1) {
			move = maxValueBB(root, 0, -20000, 20000);
			if(move.value < -9997)
				return -1;
		} else {
			move = minValueBB(root, 0, -20000, 20000);
			if(move.value > 9997)
				return -1;
		}
		
		
		return move.col;
	}
	
	public int tournament() {
		nodeCount = 0;
		BitBoard board = new BitBoard(state);
		
		// board.updateTokenCount();
		
		if(board.countToken == 4 && board.height[3] == 25) {
			return 3;
		}
		
		BitBoard checkBoard;
		int p = (player == 1)? (2):(1);
		for(int col = 0; col < WIDTH; col++) {
			if(board.canPlay(col)) {
				checkBoard = board.copy();
				checkBoard.play(col, p); 
				if(checkBoard.utility() != 0) {
					return col;
				}
			}
		}
		
		return bitboardABSearch(board);
	}
	
	
	public static void main(String args[]) {
		long startTime = System.nanoTime();
		ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
		AtomicInteger result = new AtomicInteger(-1);
		Callable<Integer> thread = new ConnectFourThread(args[0], args[1], result);
		Future<Integer> futureResult = executor.submit(thread);
		
		try {
			futureResult.get(970, TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			futureResult.cancel(true);
			System.out.println(result);
			System.exit(1);

			
		} catch (InterruptedException e) {	
		} catch (ExecutionException e) {			
		}
		executor.shutdown();
		long endTime = System.nanoTime();
		long duration = (endTime - startTime);
		double seconds = (double)duration / 1000000000.0;
		
	}
}


