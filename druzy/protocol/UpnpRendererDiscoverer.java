package druzy.protocol;

import java.util.ArrayList;
import java.util.List;

import org.fourthline.cling.DefaultUpnpServiceConfiguration;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceImpl;
import org.fourthline.cling.registry.DefaultRegistryListener;
import org.fourthline.cling.registry.Registry;
import org.fourthline.cling.registry.RegistryListener;
import org.fourthline.cling.model.message.header.DeviceTypeHeader;
import org.fourthline.cling.model.message.header.UDNHeader;
import org.fourthline.cling.model.message.header.UpnpHeader;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.types.DeviceType;
import org.fourthline.cling.model.types.UDN;

public class UpnpRendererDiscoverer extends AbstractDiscoverer{

	static private UpnpService upnpService=null;
	
	private int port=18040;
	private int portFile=18041;
	
	public static final String UPNP_NAMESPACE="schemas-upnp-org";
	public static final String MEDIA_RENDERER_TYPE="MediaRenderer";
	public static final UpnpHeader<DeviceType> MEDIA_RENDERER_HEADER=new DeviceTypeHeader(new DeviceType(UPNP_NAMESPACE,MEDIA_RENDERER_TYPE));
	
	
	public UpnpRendererDiscoverer() {
		super();
		if (upnpService==null){
			upnpService=new UpnpServiceImpl(new DefaultUpnpServiceConfiguration(port));
		}
		
	}

	@Override
	public void startDiscovery(final int delay,final DiscoveryListener listener) {
		RegistryListener registryListener=new DefaultRegistryListener(){
			
			@Override
			public void remoteDeviceAdded(Registry registry, RemoteDevice device){
				listener.deviceDiscovery(new UpnpRenderer(device,upnpService, portFile));
				
			};
		};
		
		upnpService.getRegistry().addListener(registryListener);
		upnpService.getControlPoint().search(MEDIA_RENDERER_HEADER,delay);
		
		
	}
	
	@Override
	public void startDiscovery(final int delay, String identifier, final DiscoveryListener listener) {

		RemoteDevice d=upnpService.getRegistry().getRemoteDevice(new UDN(identifier), true);
		if (d==null){
			
			RegistryListener registryListener=new DefaultRegistryListener(){
				
				@Override
				public void remoteDeviceAdded(Registry registry, RemoteDevice device){
					listener.deviceDiscovery(new UpnpRenderer(device,upnpService,portFile));
				};
			};
			
			
			upnpService.getRegistry().addListener(registryListener);
			upnpService.getControlPoint().search(new UDNHeader(new UDN(identifier)),delay);
		}else{
			
			listener.deviceDiscovery(new UpnpRenderer(d,upnpService,portFile));
		}
		
	}

	@Override
	public void stopDiscovery() {
		
	}

	@Override
	public void restartDiscovery() {
		
	}

	public static UpnpService getUpnpService() {
		return upnpService;
	}

	public static void setUpnpService(UpnpService upnpService) {
		UpnpRendererDiscoverer.upnpService = upnpService;
	}

	@Override
	public List<Integer> getPorts(){
		ArrayList<Integer> list=new ArrayList<Integer>();
		list.add(port);
		list.add(portFile);
		return list;
	}
	
	
}
