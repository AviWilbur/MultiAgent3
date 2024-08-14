
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

public class DisBTAgent implements Runnable {

	private int id,assignment,lastAgent,domainSize,maxConstraint,agents;
	private static int BTs,CCs,Ass = 0;
	private Mailer mailer;
	private HashMap<Integer, ConsTable> constraints;
	private Message mail,CPA,BT,result,noGood;
	private Stack<Integer> currentDomain = new Stack<Integer>();
	private ArrayList<Integer> domain = new ArrayList();
	private List<VarTuple> explinations = new ArrayList();
	private VarTuple p;
	private Solution solution;
	
	/*
	 * constructor parameters -
	 * agent's id
	 * a reference to mailer
	 * private information
	 * Statistics object
	 */
	public DisBTAgent(int id, Mailer mailer, HashMap<Integer, ConsTable> constraints, Solution solution, int n, int d) {
		this.id = id;
		this.mailer = mailer;
		this.constraints = constraints;
		this.solution = solution;
		this.domainSize = d;
		this.agents = n;
		this.lastAgent = n -1;
		for(int i = 0; i<domainSize;i++) {
			domain.add(i);
			currentDomain.push(i);
		}
	}

	@Override
	public void run() {
		this.assignment = currentDomain.peek();
		if(this.id == 0) {
			VarTuple v = new VarTuple(id,assignment);
			CPA = new CPA();
			((CPA)CPA).addMe(v);
			Ass++;
			mailer.send((this.id +1),CPA);
		}
		loop1:while(true) {    
			this.mail = this.mailer.readOne(this.id);
			if(this.mail != null) {
				if(mail instanceof Result) {
					break loop1;
				}
				if(this.id == 0) {
					if(this.currentDomain.isEmpty()) {
						noGood = new Result();
						for(int a = 0; a <= lastAgent;a++) {
							mailer.send(a,noGood);
						}
						break loop1;
					}
					VarTuple v = new VarTuple(id,assignment);
					CPA = new CPA();
					((CPA)CPA).addMe(v);
					Ass++;
					mailer.send((this.id +1),CPA);
				}
				if(mail instanceof BackTrack) {
					if(this.currentDomain.size() <= 1) {
						if(this.id == 0) {
							noGood = new Result();
							for(int a = 0; a <= lastAgent;a++) {
								mailer.send(a,noGood);
							}
							break loop1;
						}
						for(int d:domain) {
							this.currentDomain.push(d);
						}
					    BT = new BackTrack(((BackTrack)mail).getBT());
					    BTs++;
						maxConstraint = this.explinations.get(0).getI();
						for(VarTuple vv:explinations) {
							if(vv.getI() < maxConstraint) {
								maxConstraint = vv.getI();
							}
						}
					    mailer.send(maxConstraint,BT);     // send Backtrack to max constraint
						assignment = currentDomain.peek();
					}else {
						this.currentDomain.pop();
						((BackTrack) mail).getBT().remove(id);
						assignment = currentDomain.peek();
						CPA = new CPA(((BackTrack)mail).getBT());
						mailer.send(this.id,this.CPA);
					}
				}
				if(mail instanceof CPA && !((CPA) this.mail).containsMe(id)) {
					explinations.clear();
					loop2:for( VarTuple v : ((CPA)mail).getCPA()) {       // iterate through CPA
						CCs++;
						if(constraints.containsKey(v.getI())) {           // if we are constraint together
								if(this.constraints.get(v.getI()).check(v.getJ(),assignment)) {   // if constraint table is true
									p = new VarTuple(id,assignment);
									CPA = new CPA(((CPA)mail).getCPA());
									((CPA)CPA).addMe(p);
									Ass++;
									if(this.id == lastAgent) {
										result = new Result();
										for(int z = 0; z < lastAgent;z++) {
											mailer.send(z,result);
										}
										break loop1;
									}
									mailer.send((this.id +1),CPA);
									break loop2;
								}else {                                    // constraint table is false
									explinations.add(v);
									if(this.currentDomain.isEmpty()) {
										for(int d:domain) {
											this.currentDomain.push(d);
										}
										BT = new BackTrack(((CPA)mail).getCPA());
										BTs++;
										maxConstraint = this.explinations.get(0).getI();
										for(VarTuple vv:explinations) {
											if(vv.getI() < maxConstraint) {
												maxConstraint = vv.getI();
											}
										}
										mailer.send(maxConstraint,BT);        // send Backtrack to max constraint
										assignment = currentDomain.peek();
										break loop2;
									}else {                                   // current domain is not empty
										assignment = currentDomain.pop();
										CPA = new CPA(((CPA)mail).getCPA());
										mailer.send(this.id, this.CPA);
										break loop2;
									}
								}
						}else {                               // we are not constraint together 
							p = new VarTuple(id,assignment);
							CPA = new CPA(((CPA)mail).getCPA());
							((CPA)CPA).addMe(p);
							Ass++;
							if(this.id == lastAgent) {
								result = new Result();
								for(int z = 0; z < lastAgent;z++) {
									mailer.send(z,result);
								}
								break loop1;
							}
							mailer.send((this.id +1),CPA);
							break loop2;
						}
					}
				}
			}
		}
		solution.set(id, assignment);
	}	

}