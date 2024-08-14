//This project implements and compares two distributed algorithms, Asynchronous Backtracking (ABT) and Distributed Backtracking (DisBT), for
//solving constraint satisfaction problems (CSPs) with multiple agents. The Main class generates random CSP instances and solves each one using
//both algorithms, then checks whether both algorithms produce identical results in terms of solvability. Each agent in the system operates
//independently as a thread, using a Mailer to communicate and exchange information about variable assignments and constraints. The agents use
//these algorithms to explore possible solutions, handle conflicts, and backtrack if necessary, eventually arriving at a solution or determining
//that no solution exists. The Solution class stores the final results, indicating whether the problem is solvable and what the assignments are.

public class Main {

	public static void main(String[] args) throws InterruptedException {

		// extract parameters
		int n = Integer.valueOf(args[0]).intValue();
		int d = Integer.valueOf(args[1]).intValue();
		double p1 = Double.valueOf(args[2]).doubleValue();
		
		for (double p2 = 0.1; p2 <= 0.9; p2 += 0.1) {
			Generator gen = new Generator(n, d, p1, p2);
			
			for (int N = 0; N < 50; N++) {
				
				// generate and print CSP
				CSP csp = gen.generateDCSP();
				csp.print();
				
				// solve by two algorithms
				Solution solution1 = ABT.search(n, d, csp);
				Solution solution2 = DisBT.search(n, d, csp);
				
				// verify that the solutions are identical
				System.out.println(check(solution1, solution2));
				System.out.println();
			}
		}
		
	}
	
	private static boolean check(Solution s1, Solution s2) {
		return s1.isSolveable() == s2.isSolveable();
	}
}
