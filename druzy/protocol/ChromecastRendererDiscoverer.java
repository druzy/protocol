package druzy.protocol;

import druzy.chromecast.Bridge;
import druzy.chromecast.BridgeEvent;
import druzy.chromecast.BridgeListener;

public class ChromecastRendererDiscoverer extends AbstractDiscoverer {

	public ChromecastRendererDiscoverer() {}

	@Override
	public void startDiscovery(int delay, final DiscoveryListener listener){
		new Thread(){
			public void run(){
				Bridge b=new Bridge(Bridge.DISCOVERY);
				b.exec(new BridgeListener(){
					public void newMessage(BridgeEvent event){
						
						listener.deviceDiscovery(new ChromecastRenderer(event.getMessage().get("ip"),event.getMessage().get("name")));
					}
				});
			}
		}.start();
	}

	@Override
	public void startDiscovery(int delay, String identifier, DiscoveryListener listener) {
	}
 	
	@Override
	public void stopDiscovery() {
	}

	@Override
	public void restartDiscovery() {

	}

	public static void main(String[] args){
		new ChromecastRendererDiscoverer().startDiscovery(1000, new DiscoveryListener(){

			@Override
			public void deviceDiscovery(Device d) {
				
			}
			
		});
	}
	
}
