package druzy.protocol;

import java.util.EventObject;
import java.util.HashMap;

public class BridgeEvent extends EventObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 334987377160297266L;
	private HashMap<String,String> message=null;
	
	public BridgeEvent(Object source, HashMap<String,String> message) {
		super(source);
		this.message=message;
		
	}

	public HashMap<String, String> getMessage() {
		return message;
	}
}
