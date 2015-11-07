package druzy.protocol;

import java.io.IOException;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceListener;

public class AirplayRendererDiscoverer extends AbstractDiscoverer {

	public static final String AIRPLAY_TYPE="_airplay._tcp.local.";
	
	public AirplayRendererDiscoverer() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void startDiscovery(int delay, final DiscoveryListener listener) {
		new Thread(){
			public void run(){
				try {
					final JmDNS dns=JmDNS.create();
					dns.addServiceListener(AIRPLAY_TYPE, new ServiceListener(){

						@Override
						public void serviceAdded(final ServiceEvent event) {
							System.out.println("add");
							new Thread(){
								public void run(){
									dns.requestServiceInfo(event.getType(), event.getInfo().getName(),true);
								}
							}.start();
						}

						@Override
						public void serviceRemoved(ServiceEvent event) {
							System.out.println("removed : "+event);
						}

						@Override
						public void serviceResolved(final ServiceEvent event) {
							System.out.println("resolved");
							new Thread(){
								public void run(){
									listener.deviceDiscovery(new AirplayRenderer(event.getInfo()));
								}
							}.start();
						}
						
					});
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

	@Override
	public void startDiscovery(int delay, String identifier,DiscoveryListener listener) {
		
	}
	
	@Override
	public void stopDiscovery() {
		// TODO Auto-generated method stub

	}

	@Override
	public void restartDiscovery() {
		// TODO Auto-generated method stub

	}

	

}
