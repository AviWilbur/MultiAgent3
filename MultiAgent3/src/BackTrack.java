import java.util.ArrayList;
import java.util.List;

public class BackTrack implements Message  {
	
	private List<VarTuple> BT = new ArrayList();
	
	public BackTrack(List<VarTuple> bt){
		BT.clear();
		BT.addAll(bt);
	}
	
	public List<VarTuple> getBT(){
		return BT;
	}	

}
