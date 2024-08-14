
public class Nogood implements Message {

	private int badAssignment,id;
	private Explanation explanation;
	
	Nogood(int id,int badAssignment, Explanation explanation) {
		this.badAssignment = badAssignment;
		this.explanation = explanation;
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public int getBadAssignment() {
		return badAssignment;
	}

	public Explanation getExplanation() {
		return explanation;
	}
}
 