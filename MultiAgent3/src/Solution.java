
public class Solution {

	private boolean solveable = false;
	private int[] solution;
	
	public Solution(int n) {
		solution = new int[n];
	}

	public boolean isSolveable() {
		return solveable;
	}

	public int[] getSolution() {
		return solution;
	}

	public void set(int id, int assignment) {
		solveable = true;
		solution[id] = assignment;
	}
	
}
