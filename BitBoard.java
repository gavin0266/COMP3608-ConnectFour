import java.util.Arrays;

public class BitBoard {
	
	// bitmask corresponds to board as follows in 7x6 case:
//  .  .  .  .  .  .  .  TOP
//  5 12 19 26 33 40 47
//  4 11 18 25 32 39 46
//  3 10 17 24 31 38 45
//  2  9 16 23 30 37 44
//  1  8 15 22 29 36 43
//  0  7 14 21 28 35 42  BOTTOM
	static final int HEIGHT = 6; //ROW
	static final int WIDTH = 7; //COLUMN
	static final int NUMBER_OF_BITS = 48;
	static final int H1 = HEIGHT + 1;
	static final long BOTTOM = (1L << (H1*WIDTH)) / ((1L << H1)-1); // BOTTOM row set to 1;
	static final long COLUMN = (1L << HEIGHT)-1; // FIRST COL SET TO 1
	static final long BOARD = BOTTOM * COLUMN;
	static final long ODD_ROWS = ((0x14 * BOTTOM));
	static final long EVEN_ROWS = ((0x2a * BOTTOM));
	
	static final int[][] WINNING_LINES = 
		    {{3, 4, 5, 7, 5, 4, 3}, 
            {4, 6, 8, 10, 8, 6, 4},
            {5, 8, 11, 13, 11, 8, 5}, 
            {5, 8, 11, 13, 11, 8, 5},
            {4, 6, 8, 10, 8, 6, 4},
            {3, 4, 5, 7, 5, 4, 3}};
	
	
	public long bitboard[];
	public short[] height;
	public int col;
	public int value;
	public int[] winning_sums;
	public int countToken;
	
	public BitBoard(String state) {
		this.bitboard = new long[2];
		this.bitboard[0] = 0b000000000000000000000000000000000000000000000000L;
		this.bitboard[1] = 0b000000000000000000000000000000000000000000000000L;
		this.height = new short[] {0, 7, 14, 21, 28, 35, 42};
		this.col = -1;
		this.value = -1;
		this.winning_sums = new int[] {0, 0};
		String[] rows = state.split(",");
		for(int i = 0; i < rows.length; i++) {
			for(int j = 0; j < rows[i].length(); j++) {
				switch(rows[i].charAt(j)){
				case 'r': 
					this.play(j, 1);
					break;
				case 'y':
					this.play(j, 2);
					break;
				}
			}			
		}
		
		this.countToken = Long.bitCount(bitboard[0] | bitboard[1]);

		
	}
	
	public BitBoard() {
		// TODO Auto-generated constructor stub
	}
	
	public void updateTokenCount() {
		this.countToken = Long.bitCount(bitboard[0] | bitboard[1]);
	}

	public BitBoard copy() {
		BitBoard newBoard = new BitBoard();
		newBoard.bitboard = new long[] {bitboard[0], bitboard[1]};
		newBoard.height = new short[WIDTH];
		for(int i = 0; i < WIDTH; ++i) {
			newBoard.height[i] = this.height[i];
		}
		newBoard.winning_sums = new int[] {winning_sums[0], winning_sums[1]};
//		newBoard.countToken = this.countToken;
		
		return newBoard;
	}
	
	public int utility() {
		if(isWin(1)) {
			return 10000;
		}
		else if(isWin(2)) {
			return -10000;
		}
		return 0;
	}
	
	public int utility(int depth) {
		if(isWin(1)) {
			return 10000 - depth;
		}
		else if(isWin(2)) {
			return -10000 + depth;
		}
		return 0;
	}
	
	private boolean isWin(int player) {
		long bb = bitboard[player-1];
		if ((bb & (bb >> 6) & (bb >> 12) & (bb >> 18)) != 0) return true; // diagonal \
		if ((bb & (bb >> 8) & (bb >> 16) & (bb >> 24)) != 0) return true; // diagonal /
		if ((bb & (bb >> 7) & (bb >> 14) & (bb >> 21)) != 0) return true; // horizontal
		if ((bb & (bb >> 1) & (bb >>  2) & (bb >>  3)) != 0) return true; // vertical
		return false;
	}

	private int inARow4(int player) {
		long bb = bitboard[player-1];
		int total = 0;
		total += Long.bitCount((bb & (bb >> 6) & (bb >> 12) & (bb >> 18))); // diagonal \
		total += Long.bitCount((bb & (bb >> 8) & (bb >> 16) & (bb >> 24))); // diagonal /
		total += Long.bitCount((bb & (bb >> 7) & (bb >> 14) & (bb >> 21))); // horizontal
		total += Long.bitCount((bb & (bb >> 1) & (bb >> 2) & (bb >> 3))); // vertical
		return total;
	}
	
	private int inARow3(int player) {
		long bb = bitboard[player-1];
		int total = 0;
		total += Long.bitCount((bb & (bb >> 6) & (bb >> 12))); // diagonal \
		total += Long.bitCount((bb & (bb >> 8) & (bb >> 16))); // diagonal /
		total += Long.bitCount((bb & (bb >> 7) & (bb >> 14))); // horizontal
		total += Long.bitCount((bb & (bb >> 1) & (bb >> 2))); // vertical
		return total;
	}
	
	private int inARow2(int player) {
		long bb = bitboard[player-1];
		int total = 0;
		total += Long.bitCount((bb & (bb >> 6))); // diagonal \
		total += Long.bitCount((bb & (bb >> 8))); // diagonal /
		total += Long.bitCount((bb & (bb >> 7))); // horizontal
		total += Long.bitCount((bb & (bb >> 1))); // vertical
		return total;
	}
	
	private long topMask(int col) {
		long tempBoard = bitboard[0] | bitboard[1];
		tempBoard |= (1L << (6-col)*HEIGHT + 1);
		return tempBoard;
	}
	
	public boolean canPlay(int col) {
//		long tempBoard = bitboard[0] | bitboard[1];
//		print(tempBoard);
		return (height[col] - H1*col) < HEIGHT;
//		return (tempBoard != topMask(col));
	}
	
	public void play(int col, int player) {
//		winning_sums[player-1] = 0;
		winning_sums[player-1] += WINNING_LINES[height[col]%WIDTH][col];
		long move = 1L << height[col]++;
		bitboard[player-1] ^= move;
		
	}
	
	public void print(long board) {
		String bitBoardStr = String.format("%56s", Long.toBinaryString(board)).replace(' ', '0');
		System.out.println(bitBoardStr);
		String[] bitboardArray = bitBoardStr.split("(?<=\\G.......)");

		for(int i = 0; i < H1; i++) {
			for(int j = WIDTH; j >= 0; j--) {
				System.out.print(bitboardArray[j].charAt(i) + " ");
			}
			System.out.println();
		}

	}
	
	//DEPRECATED
	public void printBitBoard(int player)  {
		String bitBoardStr = null;
		bitBoardStr = String.format("%48s", Long.toBinaryString(bitboard[player-1])).replace(' ', '0');
		System.out.println(bitBoardStr);
		String[] bitboardArray = bitBoardStr.split("(?<=\\G.......)");

		for(int i = 0; i < HEIGHT; i++) {
			for(int j = WIDTH-1; j >= 0; j--) {
				System.out.print(bitboardArray[j].charAt(i) + " ");
			}
			System.out.println();
		}
	}
	
	public int assignment_evaluation() {
		int result = 0;
		int[] player1 = new int[] {Long.bitCount(bitboard[0]), inARow2(1), inARow3(1), inARow4(1) };
		int[] player2 = new int[] {Long.bitCount(bitboard[1]), inARow2(2), inARow3(2), inARow4(2) };
		
		player1[1] -= 2*player1[2]+3*player1[3];
		player1[2] -= 2*player1[3];
		
		player2[1] -= 2*player2[2]+3*player2[3];
		player2[2] -= 2*player2[3];
				
		result += 1000*(player1[3]);
		result += 100*(player1[2]);
		result += 10*(player1[1]);
		result += player1[0];
		result -= 1000*(player2[3]);
		result -= 100*(player2[2]);
		result -= 10*(player2[1]);
		result -= player2[0];
		
		return result;
	}
	
	public int evaluation() {
		boolean assignment = false;
		if(assignment) {
			return assignment_evaluation();
		}
		
		int result = 0;
		
		result += winning_sums[0];
		result -= winning_sums[1];
		
//		result += 100*Long.bitCount(p1Threats & ODD_ROWS);
		
		long p1Threats = findThreat(1);
		long p2Threats = findThreat(2);
		
//		result += 25*(Long.bitCount(p1Threats) - Long.bitCount(p2Threats));
		
		long p1Even = p1Threats & EVEN_ROWS;
		long p2Even = p2Threats & EVEN_ROWS;
		long p1Odd = p1Threats & ODD_ROWS;
		long p2Odd = p2Threats & ODD_ROWS;
		
		
		int p1EvenCount = Long.bitCount(p1Even);  
		int dangerousP1EvenCount = p1EvenCount - Long.bitCount((p2Odd << 1) & p1Even);
		
		int p2EvenCount = Long.bitCount(p2Even);
		int dangerousP2EvenCount = p2EvenCount - Long.bitCount((p1Odd << 1) & p2Even);
		
		int p1OddCount = Long.bitCount(p1Odd);
		int dangerousP1OddCount = p1OddCount - Long.bitCount((p2Even << 1) & p1Odd);
		
		int p2OddCount = Long.bitCount(p2Odd);
		int dangerousP2OddCount = p2OddCount - Long.bitCount((p1Even << 1) & p2Odd);
		
		result += 250*p1OddCount;
		result += 1000*dangerousP1OddCount;
		result += 50*p1EvenCount;
		result += 300*dangerousP1EvenCount;
		result -= 50*p2OddCount;
		result -= 250*p2EvenCount;
		result -= 1000*dangerousP2EvenCount;
		result -= 300*dangerousP2OddCount;

		
		
		
		return result;
	}
	
	public long findThreat(int player) {
		long bb = bitboard[player-1];
		long empty = (~(bitboard[0] | bitboard[1]) & BOARD);
		
		long result = 0L;
		
		result |= (empty & (bb >> H1) & (bb >> H1*2) & (bb >> H1*3)); //_XXX
		result |= (empty & (bb << H1) & (bb << H1*2) & (bb << H1*3)); //XXX_
		result |= (empty & (bb >> H1) & (bb >> H1*2) & (bb << H1)); //X_XX
		result |= (empty & (bb << H1) & (bb << H1*2) & (bb >> H1)); //XX_X
		
		result |= (empty & (bb >> H1+1) & (bb >> 2*(H1+1)) & (bb >> 3*(H1+1)) ); // diag / _XXX
		result |= (empty & (bb << H1+1) & (bb << 2*(H1+1)) & (bb << 3*(H1+1)) ); // diag / XXX_
		result |= (empty & (bb >> H1+1) & (bb >> 2*(H1+1)) & (bb << (H1+1)) ); //diag / X_XX
		result |= (empty & (bb << H1+1) & (bb << 2*(H1+1)) & (bb >> (H1+1)) ); //diag / XX_X
		
		result |= (empty & (bb << H1-1) & (bb << 2*(H1-1)) & (bb << 3*(H1-1)) ); // diag \ _XXX
		result |= (empty & (bb >> H1-1) & (bb >> 2*(H1-1)) & (bb >> 3*(H1-1)) ); // diag \ XXX_
		result |= (empty & (bb >> H1-1) & (bb >> 2*(H1-1)) & (bb << (H1-1)) ); // diag \ XX_X
		result |= (empty & (bb << H1-1) & (bb << 2*(H1-1)) & (bb >> (H1-1)) ); // diag \ X_XX
		
//		print(empty);
//		print(bb);
//		print(result);

		return result;
	}
	
	public static void main(String[] args) {
		long startTime = System.nanoTime();
		BitBoard bitboard = new BitBoard("r.ryr.r,..yyy..,.......,.......,.......,.......,.......");
//		bitboard.print(bitboard.bitboard[0]);

//		bitboard.findThreat(1);
		System.out.println(bitboard.evaluation());
//		bitboard.print((bitboard.findThreat(1) & odd_threats));
		for(int i = 0; i < 10000000; i++) {
			BitBoard b = bitboard.copy();
			b.evaluation();
			b.utility();

		}
//		for(int i = 0; i < 100000; i++) {
//			Board board = new Board(".ryryy,.rryyr.,.ryr...,.r.....,.......,.......");
//			board.evaluation();
//		}
		long endTime = System.nanoTime();
		long duration = (endTime - startTime);
		double seconds = (double)duration / 1000000000.0;
		System.out.println(seconds);
//		bitboard.printBitBoard(1);
//		System.out.print(bitboard.utility());
		//		bitboard.printBitBoard(2);
//		System.out.println();
	}
}
