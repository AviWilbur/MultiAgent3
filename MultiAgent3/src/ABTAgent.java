
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import java.util.Map.Entry;

import com.sun.source.tree.AssignmentTree;

public class ABTAgent implements Runnable {

	private int id;
	private int domainSize, agents;
	private int assignment;
	private Mailer mailer;
	private Solution solution;
	private Message mail;
	private Stack<Integer> currentDomain = new Stack<Integer>();
	private ArrayList<Integer> domain = new ArrayList();
	private ArrayList<VarTuple> myAgentView = new ArrayList();
	private boolean value;
	private int NCAP,NCCC =0;
	
	private HashMap<Integer, ConsTable> constraints;
	// map assignments to explanations -
	private Map<Integer,Explanation> explanations = new HashMap<Integer,Explanation>();
	
	/*
	 * constructor parameters -
	 * agent's id
	 * a reference to mailer
	 * private information
	 * Statistics object
	 */
	public ABTAgent(int id, Mailer mailer, HashMap<Integer, ConsTable> constraints, Solution solution, int n, int d) {
		this.id = id;
		this.mailer = mailer;
		this.constraints = constraints;
		this.solution = solution;
		this.domainSize = d;
		this.agents = n;
		for(int i = 0; i<domainSize;i++) {
			domain.add(i);
			currentDomain.push(i);
		}
	}

	@Override
	public void run() {
		boolean end = true;
		this.assignment = currentDomain.peek();
		NCAP++;
		Info myInfo = new Info(id,assignment);
//		for (Entry<Integer,ConsTable> e: constraints.entrySet()) {
		for(int i =0;i<agents;i++) {
			if(i > id) {
				mailer.send(i, myInfo);
			}
		}
		checkAgentView();
		while(end) {
			this.mail = this.mailer.readOne(this.id);
			if(mail instanceof Info) {
				update(((Info) this.mail).getInfo());
				checkAgentView();
			}
			if(mail instanceof Nogood) {
				backTrack();
			}
			if(mail instanceof End) {
				end = false;
			}
		}
		System.out.println("ID " + id + "  assignment " + assignment);
		if(assignment >= 0) {
			solution.set(id, assignment);
		}
	}
	
	// go through agentVeiw if false choose value
	// if value true send to all future contraints
	// if value false back track
	public void checkAgentView() {

		boolean  flag = true;
		boolean flag2 = false;
		loop:for(VarTuple v: myAgentView) {
			if(constraints.containsKey(v.getI())) {
				if(!constraints.get(v.getI()).check(v.getJ(),assignment)){
					NCCC++;
					flag = false;
					value = chooseValue();
					if(value) {
						NCAP++;
						Info myInfo = new Info(id,assignment);
		//				for (Entry<Integer,ConsTable> e: constraints.entrySet()) {
						for(int i =0;i<agents;i++) {
							if(i > id) {
								mailer.send(i, myInfo);
							}
						}
						break loop;
					}else {
						flag2 = true;
						break loop;
					}				
				}
			}
		}
		if(explanations.containsKey(assignment)) {
			flag = false;
			value = chooseValue();
			if(value) {
				NCAP++;
				Info myInfo = new Info(id,assignment);
	//			for (Entry<Integer,ConsTable> e: constraints.entrySet()) {
				for(int j=0;j<agents;j++) {
					if(j > id) {
						mailer.send(j, myInfo);
					}
				}
			}else {
				flag2 = true;
			}
		}
		if(flag) {
			Info myInfo = new Info(id,assignment);
//			for (Entry<Integer,ConsTable> e: constraints.entrySet()) {
			for(int j=0;j<agents;j++) {
				if(j > id) {
					mailer.send(j, myInfo);
				}
			}
		}
		if(flag2) {
			resolveNogoods();
			VarTuple rhs = findRhs();
			myAgentView.remove(rhs);
			assignment = rhs.getJ();
		}
	}
	
	// if i have a value return true else return false
	public boolean chooseValue() {
		boolean flag = true;
 		currentDomain.clear();
		for(int i = 0; i<domainSize;i++) {
			currentDomain.push(i);
		}
		while(!currentDomain.isEmpty()) {
			assignment = currentDomain.pop();
			if(!explanations.containsKey(assignment)) {
				Explanation expl = new Explanation();
				for(VarTuple v: myAgentView) {
					if(constraints.containsKey(v.getI())) {
						NCCC++;
						if(!this.constraints.get(v.getI()).check(v.getJ(),assignment)){
							flag = false;
							expl.add(v);
							explanations.put(assignment,expl);
						}
					}
				}
				if(flag) {
					return true;
				}
			}
		}
		return false;
	}
	
	
	public void backTrack() {
		boolean flag = true;
		if(((Nogood) this.mail).getBadAssignment() != assignment) {
			Info myInfo = new Info(id,assignment);
		//	for (Entry<Integer,ConsTable> e: constraints.entrySet()) {
			for(int j=0;j<agents;j++) {
				if(j > id) {
					mailer.send(j, myInfo);
				}
			}
			flag = false;
		}else {
			for (Entry<Integer,Explanation> e: explanations.entrySet()) {
				for(VarTuple a:e.getValue().getTuples()) {
					for(VarTuple b : ((Nogood) this.mail).getExplanation().getTuples()) {
						if(a.getI() == b.getI() && a.getJ() != b.getJ()) {
							flag = false;
							Info myInfo = new Info(id,assignment);
					//		for (Entry<Integer,ConsTable> n: constraints.entrySet()) {
							for(int j=0;j<agents;j++) {
								if(j > id) {
									mailer.send(j, myInfo);
								}
							}
						}
					}
				}
			}
		}
		if(flag) {
			Explanation expl = new Explanation();
			for(VarTuple c : ((Nogood) this.mail).getExplanation().getTuples()) {
				update(c);
				if(explanations.containsKey(c.getJ())) {
					explanations.get(c.getJ()).add(c);
				}
				expl.add(c);
				explanations.put(assignment, expl);
			}
			if(currentDomain.isEmpty()) {
				if(id == 0) {
					assignment = -1;
					Message finish = new End();
					for(int i =0;i<agents;i++) {
						mailer.send(i, finish);
					}
				}else {
					resolveNogoods();
					VarTuple r = findRhs();
					assignment = r.getJ();
					NCAP++;
					myAgentView.remove(r);
				}
			}else {
				assignment = currentDomain.pop();
				NCAP++;
			}
		}		
		checkAgentView();
	}
	
	
	public void update(VarTuple a) {
		ArrayList<Integer> t = new ArrayList();
		int counter = -1;
		int index = -1;
		for(VarTuple b: myAgentView) {
			counter++;
			if(a.getI() == b.getI()) {
				index = counter;
			}
		}
		if(index >- 1) {
			myAgentView.remove(index);
		}
		myAgentView.add(a);
		for (Entry<Integer,Explanation> e: explanations.entrySet()) {
			Explanation expl = new Explanation();
			for(VarTuple d:e.getValue().getTuples()) {
				if(!(d.getI() == a.getI() && d.getJ() != a.getJ())) {
					expl.add(d);
				}
			}
			if(expl.getTuples().size() == 0) {
				t.add(e.getKey());
			}else {
				explanations.put(e.getKey(), expl);
			}
		}
		for (int w:t) {
			explanations.remove(w);
		}	
	}
	
	public Stack<Integer> resetDomain() {
		Stack<Integer> s = new Stack<Integer>();
		for(int i = 0; i<domainSize;i++) {
			s.push(i);
		}
		return s;
	}
	
	private VarTuple findRhs() {

		VarTuple rhs = null;
		for (Entry<Integer, Explanation> e: explanations.entrySet()) {
			for (VarTuple v: e.getValue().getTuples()) {
				if (rhs == null || v.getI() > rhs.getI()) {
					rhs = v;
				}
			}
		}
		return rhs;
	}
	
	private Explanation findLhs(int rhsAgent) {

		Explanation explanation = new Explanation();
		Set<Integer> added = new TreeSet<Integer>();
		
		for (Entry<Integer, Explanation> e: explanations.entrySet()) {
			for (VarTuple v: e.getValue().getTuples()) {
					if (v.getI() != rhsAgent && !added.contains(v.getI())) {
						explanation.add(v);
						added.add(v.getI());
					}
				}
			}
		return explanation;
	}
	
	private void resolveNogoods() {
		
		VarTuple rhs = findRhs();
		if (rhs == null) {
			//algorithm terminates
			assignment = -1;
			Message finish = new End();
			for(int i =0;i<agents;i++) {
				mailer.send(i, finish);
			}			
		} else {
			Explanation explanation = findLhs(rhs.getI());
			
			int sendTo = rhs.getI();
			int badAssignment = rhs.getJ();
			Nogood nogood = new Nogood(id,badAssignment, explanation);
			mailer.send(sendTo, nogood);
		}
	
	}

}
