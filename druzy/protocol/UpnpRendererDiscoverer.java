package druzy.protocol;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.fourthline.cling.DefaultUpnpServiceConfiguration;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceImpl;
import org.fourthline.cling.registry.DefaultRegistryListener;
import org.fourthline.cling.registry.Registry;
import org.fourthline.cling.registry.RegistryListener;
import org.fourthline.cling.transport.impl.DatagramIOImpl;
import org.fourthline.cling.transport.Router;
import org.fourthline.cling.transport.spi.DatagramIO;
import org.fourthline.cling.transport.spi.DatagramProcessor;
import org.fourthline.cling.transport.spi.InitializationException;
import org.fourthline.cling.transport.spi.NetworkAddressFactory;
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
	private int portDatagram=18042;
	
	public static final String UPNP_NAMESPACE="schemas-upnp-org";
	public static final String MEDIA_RENDERER_TYPE="MediaRenderer";
	public static final UpnpHeader<DeviceType> MEDIA_RENDERER_HEADER=new DeviceTypeHeader(new DeviceType(UPNP_NAMESPACE,MEDIA_RENDERER_TYPE));
	
	
	public UpnpRendererDiscoverer() {
		super();
		if (upnpService==null){
			upnpService=new UpnpServiceImpl(new DefaultUpnpServiceConfiguration(port){
				@SuppressWarnings("rawtypes")
				@Override
				public DatagramIO createDatagramIO(NetworkAddressFactory networkAddressFactory){
					DatagramIOImpl d=(DatagramIOImpl) super.createDatagramIO(networkAddressFactory);
					DatagramIOImpl result=new DatagramIOImpl(d.getConfiguration()){
						@Override
						public void init(InetAddress bindAddress, Router router, DatagramProcessor datagramProcessor) throws InitializationException {
							this.router = router;
					        this.datagramProcessor = datagramProcessor;

					        try {

					            // TODO: UPNP VIOLATION: The spec does not prohibit using the 1900 port here again, however, the
					            // Netgear ReadyNAS miniDLNA implementation will no longer answer if it has to send search response
					            // back via UDP unicast to port 1900... so we use an ephemeral port
					        	Logger.getLogger(DatagramIO.class.getName()).info("Creating bound socket (for datagram input/output) on: " + bindAddress);
					            localAddress = new InetSocketAddress(bindAddress, 18042);
					            socket = new MulticastSocket(localAddress);
					            socket.setTimeToLive(configuration.getTimeToLive());
					            socket.setReceiveBufferSize(32768); // Keep a backlog of incoming datagrams if we are not fast enough

					        } catch (Exception ex) {
					            throw new InitializationException("Could not initialize " + getClass().getSimpleName() + ": " + ex);
					        }
						}
						
					};
			          
					return result;
				}
			});
	
		}
		
	}

	@Override
	public void startDiscovery(final int delay,final DiscoveryListener listener) {
		RegistryListener registryListener=new DefaultRegistryListener(){
			
			@Override
			public void remoteDeviceAdded(Registry registry, RemoteDevice device){
				listener.deviceDiscovery(new UpnpRenderer(device,upnpService));
				
			};
		};
		for (RemoteDevice device : upnpService.getRegistry().getRemoteDevices()){
			if (device.getType().equals(new DeviceType(UPNP_NAMESPACE,MEDIA_RENDERER_TYPE))){
				listener.deviceDiscovery(new UpnpRenderer(device,upnpService));
			}
		}
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
					listener.deviceDiscovery(new UpnpRenderer(device,upnpService));
				};
			};
			
			
			upnpService.getRegistry().addListener(registryListener);
			upnpService.getControlPoint().search(new UDNHeader(new UDN(identifier)),delay);
		}else{
			
			listener.deviceDiscovery(new UpnpRenderer(d,upnpService));
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
		list.add(portDatagram);
		return list;
	}
	
	
}
