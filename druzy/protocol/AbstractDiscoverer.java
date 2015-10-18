package druzy.protocol;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractDiscoverer implements Discoverer {

	public AbstractDiscoverer() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public  List<Integer> getPorts(){
		return new ArrayList<Integer>();
	}

}
