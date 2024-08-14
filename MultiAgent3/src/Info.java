
public class Info implements Message {
	
	private int assignment;
	private int id;
	private VarTuple myVarTuple;

	public Info(int id,int as) {
		this.id = id;
		this.assignment = as;
		myVarTuple = new VarTuple(id,as);
	}
	
	public VarTuple getInfo() {
		return this.myVarTuple;
	}
}
