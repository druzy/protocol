package druzy.protocol;

import java.util.List;


public interface Discoverer {
	public List<Integer> getPorts();
	public void startDiscovery(int delay, DiscoveryListener listener);
	public void startDiscovery(int delay, String identifier, DiscoveryListener listener);
	public void stopDiscovery();
	public void restartDiscovery();
}
