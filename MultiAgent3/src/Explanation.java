import java.util.ArrayList;
import java.util.List;

public class Explanation {

	private List<VarTuple> tuples = new ArrayList<VarTuple>();

	public List<VarTuple> getTuples() {
		return tuples;
	}
	
	public void add(VarTuple v) {
		if(!tuples.contains(v)) {
			tuples.add(v);
		}
	}
}
