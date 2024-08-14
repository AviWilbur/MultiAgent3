
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

/*
 * used for communication among agents
 */
public class Mailer {

	private int count = 0;
	private int stop = 0;
	private boolean flag;
	private static int NCCC,NCAP = 0;
	
	public Mailer() {
		
	}
	
	// maps between agents and their mailboxes
	private HashMap<Integer, List<Message>> map = new HashMap<>();
	private HashMap<Integer, Integer> finished = new HashMap<>();
	
	public int getNCCC() {
		return NCCC;
	}
	
	public int getNCAP() {
		return NCAP;
	}
	
	// send message @m to agent @receiver
	public void send(int receiver, Message m) {
	
//		System.out.println("MESSAGE");
		
		count = getCount() + 1;

		List<Message> l = map.get(receiver);
		
		synchronized (l) {
			l.add(m);
			finished.put(receiver,1);
		}
	}

	// agent @receiver reads the first message from its mail box
	public Message readOne(int receiver) {
		List<Message> l = map.get(receiver);
		synchronized (l) {
			if (l.isEmpty()) {
				finished.put(receiver,0);
				flag = true;
				for (Entry<Integer,Integer> e: finished.entrySet()) {
					if(e.getValue() == 1) {
						stop = 0;
						flag = false;
						return null;
					}
				}
				if(flag) {
					stop++;
					if(stop > 10000) {
						Message end = new End();
						return end;
					}else {
						return null;
					}
				}
			}
			if(l.isEmpty()) {
				return null;
			}
			Message m = l.get(0);
			l.remove(0);
			return m;
		}
	}
	
	// only used for initialization
	public void put(int i) {
		List<Message> l= new ArrayList<Message>();
		this.map.put(i, l);
		this.finished.put(i,1);
	}

	public int getCount() {
		return count;
	}
	
	public int getStop() {
		return stop;
	}
}
