package druzy.protocol;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.reflections.Reflections;

public class DiscovererFactory {

	private DiscovererFactory() {	}

	public static List<Discoverer> getDiscoverers(){
		ArrayList<Discoverer> ret=new ArrayList<Discoverer>();
		
		Reflections reflect=new Reflections("druzy.protocol");
		Set<Class<? extends Discoverer>> set=reflect.getSubTypesOf(Discoverer.class);
		
		for (Class<? extends Discoverer> disco:set){
			if (!disco.equals(AbstractDiscoverer.class)){
				try {
					ret.add(disco.newInstance());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		return ret;
	}
}
