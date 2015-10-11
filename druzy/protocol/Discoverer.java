package druzy.protocol;


public interface Discoverer {
	public void startDiscovery(int delay, DiscoveryListener listener);
	public void startDiscovery(int delay, String identifier, DiscoveryListener listener);
	public void stopDiscovery();
	public void restartDiscovery();
}
