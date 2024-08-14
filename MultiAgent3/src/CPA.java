import java.util.ArrayList;
import java.util.List;

public class CPA implements Message {
	
	private List<VarTuple> CPA = new ArrayList();

	public CPA() {
		
	   }
	   
	public List<VarTuple> getCPA() {
		return CPA;
	}

	public CPA(List<VarTuple> cpa) {
		this.CPA.clear();
		for(VarTuple v: cpa) {
			this.CPA.add(v);
		}
	}
	
   public void addMe(VarTuple v) {
	    CPA.add(v);
   }
   
   public boolean containsMe(int i) {
	   for(VarTuple v: CPA) {
		   if(v.getI() == i) {
			   return true;
		   }
	   }
	   return false;
   }
}