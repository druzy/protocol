package druzy.protocol;

import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.sql.Time;
import javax.swing.ImageIcon;

import org.fourthline.cling.UpnpService;
import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.meta.RemoteService;
import org.fourthline.cling.model.meta.StateVariable;
import org.fourthline.cling.model.meta.StateVariableAllowedValueRange;
import org.fourthline.cling.model.types.ServiceType;
import org.fourthline.cling.support.contentdirectory.DIDLParser;
import org.fourthline.cling.support.model.DIDLContent;
import org.fourthline.cling.support.model.ProtocolInfo;
import org.fourthline.cling.support.model.Res;
import org.fourthline.cling.support.model.item.Item;
import org.fourthline.cling.support.model.item.VideoItem;

import druzy.server.RestrictedFileServer;
import druzy.upnp.WatchService;
import druzy.upnp.WatchServiceEvent;
import druzy.upnp.WatchServiceListener;

public class UpnpRenderer extends AbstractRenderer {

	public static final ServiceType CONNECTION_MANAGER_TYPE=new ServiceType(UpnpRendererDiscoverer.UPNP_NAMESPACE,"ConnectionManager");
	public static final ServiceType AV_TRANSPORT_TYPE=new ServiceType(UpnpRendererDiscoverer.UPNP_NAMESPACE,"AVTransport");
	public static final ServiceType RENDERING_CONTROL_TYPE=new ServiceType(UpnpRendererDiscoverer.UPNP_NAMESPACE,"RenderingControl");
	public static final int HTTP_SERVER_PORT=15322;
	
	private RemoteDevice remoteDevice=null;
	private String protocol=null;
	private RemoteService connectionManager=null;
	private RemoteService avTransport=null;
	private RemoteService renderingControl=null;
	private UpnpService upnpService=null;
	private int avTransportId=-1;
	private int connectionId=0;
	private int rcsId=-1;
	private WatchService watchPositionInfo=null;
	private WatchService watchTransportInfo=null;
	private WatchService watchVolume=null;
	private WatchService watchMute=null;
	private boolean play=false;
	private boolean pause=false;
	private boolean stop=false;
	private Time duration=null;
	private Time timePosition=null;
	private int volume=0;
	private boolean mute=false;
	
	private String sink=null;
	
	public UpnpRenderer(RemoteDevice remoteDevice, UpnpService upnpService) {
		super();
		this.remoteDevice=remoteDevice;
		this.protocol="upnp";
		this.connectionManager=remoteDevice.findService(CONNECTION_MANAGER_TYPE);
		this.avTransport=remoteDevice.findService(AV_TRANSPORT_TYPE);
		this.renderingControl=remoteDevice.findService(RENDERING_CONTROL_TYPE);
		this.upnpService=upnpService;
		this.duration=Time.valueOf("00:00:00");
		this.timePosition=Time.valueOf("00:00:00");
	}

	@Override
	public String getIdentifier() {
		return remoteDevice.getIdentity().getUdn().getIdentifierString();
	}

	@Override
	public String getProtocol() {
		return protocol;
	}

	@Override
	public void setProtocol(String protocol) {
		this.protocol=protocol;
	}

	@Override
	public String getName() {
		return remoteDevice.getDetails().getFriendlyName();
	}

	@Override
	public ImageIcon getIcon() {
		org.fourthline.cling.model.meta.Icon[] icons=remoteDevice.getIcons();
		ImageIcon ret=null;
		if (icons!=null && icons.length>0){
			URL baseUrl=remoteDevice.getDetails().getBaseURL();
			URI uri=icons[0].getUri();
			try {
				URL urlIcon=new URL(baseUrl.getProtocol(),baseUrl.getHost(),baseUrl.getPort(),uri.getPath());
				ret=new ImageIcon(urlIcon);
			} catch (MalformedURLException e) {
				ret=null;
				e.printStackTrace();
			}
		}
		return ret;
	}

	@Override
	public void play() {
		ActionInvocation<RemoteService> invocPlay=new ActionInvocation<RemoteService>(avTransport.getAction("Play"));
		invocPlay.setInput("InstanceID",String.valueOf(avTransportId));
		invocPlay.setInput("Speed", "1");
		new ActionCallback.Default(invocPlay,upnpService.getControlPoint()).run();
		setPlay(true);
	}
	
	@Override
	public boolean isPlay(){return play;}
	
	private void setPlay(boolean play){
		if (play){
			setPause(false);
			setStop(false);
		}
		
		if (this.play!=play){
			boolean old=this.play;
			this.play=play;
			firePropertyChange(new PropertyChangeEvent(this,"play",old,play));
		}
	}

	@Override
	public void pause() {
		ActionInvocation<RemoteService> invocPause=new ActionInvocation<RemoteService>(avTransport.getAction("Pause"));
		invocPause.setInput("InstanceID",String.valueOf(avTransportId));
		new ActionCallback.Default(invocPause,upnpService.getControlPoint()).run();
		setPause(true);
	}
	
	@Override
	public boolean isPause(){return pause;}
	
	private void setPause(boolean pause){
		if (pause){
			setPlay(false);
		}
		
		if (this.pause!=pause){
			boolean old=this.pause;
			this.pause=pause;
			firePropertyChange(new PropertyChangeEvent(this,"pause",old,pause));
		}
	}
	
	

	@Override
	public void stop() {
		stopWatchin();
		ActionInvocation<RemoteService> invocStop=new ActionInvocation<RemoteService>(avTransport.getAction("Stop"));
		invocStop.setInput("InstanceID",String.valueOf(avTransportId));
		new ActionCallback.Default(invocStop,upnpService.getControlPoint()).run();
		setStop(true);
	}
	
	@Override
	public boolean isStop(){return stop;}
	
	private void setStop(boolean stop){
		if (stop){
			setPlay(false);
			setPause(false);
		}
		
		if (this.stop!=stop){
			boolean old=this.stop;
			this.stop=stop;
			firePropertyChange(new PropertyChangeEvent(this,"stop",old,stop));
		}
	}

	@Override
	public boolean send(File file) {
		//arret de la surveillance de l'ancien fichier
		stopWatchin();
		String askProtocol=null;
		try {
			askProtocol="http-get:*:"+Files.probeContentType(file.toPath())+":*";
		} catch (IOException e){
			e.printStackTrace();
			return false;
		}
		//si le protocol est trouvé, on continue
		if (accept(file) && askProtocol!=null){
			//si prepareforconnectionexiste
			ActionInvocation<RemoteService> invocPrepare=new ActionInvocation<RemoteService>(connectionManager.getAction("PrepareForConnection"));
			invocPrepare.setInput("RemoteProtocolInfo",askProtocol);
			invocPrepare.setInput("PeerConnectionManager","/");
			invocPrepare.setInput("PeerConnectionID","-1");
			invocPrepare.setInput("Direction","Output");
			
			ActionCallback callback=new ActionCallback.Default(invocPrepare,upnpService.getControlPoint());
			callback.run();
			
			if (invocPrepare.getFailure()==null){ //pas de probleme de connection
				//initialisation des différents ids
				connectionId=(Integer)invocPrepare.getOutput("ConnectionID").getValue();
				avTransportId=(Integer)invocPrepare.getOutput("AVTransportID").getValue();
				rcsId=(Integer)invocPrepare.getOutput("RcsID").getValue();

			}else return false;
			
			
			//création du metadata du fichier
			DIDLContent content=new DIDLContent();
			Item item;
			item = new VideoItem(file.getAbsolutePath(),file.getParent(),file.getName(),"JMita",new Res(new ProtocolInfo(askProtocol),file.length(),RestrictedFileServer.getInstance(HTTP_SERVER_PORT).toString()+file.getAbsolutePath()));
			content.addItem(item);
			
			//envoie de l'uri et démarrage du serveur http pour envoyer la vidéo 
			RestrictedFileServer.getInstance(HTTP_SERVER_PORT).addAuthorizedFile(file);
			if (!RestrictedFileServer.getInstance(HTTP_SERVER_PORT).isStarting()) RestrictedFileServer.getInstance(HTTP_SERVER_PORT).start();
			
			ActionInvocation<RemoteService> invocSetUri=new ActionInvocation<RemoteService>(avTransport.getAction("SetAVTransportURI"));
			invocSetUri.setInput("InstanceID",String.valueOf(avTransportId));
			invocSetUri.setInput("CurrentURI", RestrictedFileServer.getInstance(HTTP_SERVER_PORT).toString()+file.getAbsolutePath());
			try {
				invocSetUri.setInput("CurrentURIMetaData",new DIDLParser().generate(content));
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			new ActionCallback.Default(invocSetUri, upnpService.getControlPoint()).run();
			
			//démarage de la surveillance
			
			startWatchin();
			
			return true;
			
		}else return false;

	}
	
	@Override
	public void shutdown(){
		stop();
	}
	
	@Override
	public Time getDuration() {
		return duration;
	}

	private void setDuration(Time duration) {
		if (!this.duration.equals(duration)){
			Time old=this.duration;
			this.duration = duration;
			firePropertyChange(new PropertyChangeEvent(this,"duration",old,duration));
		}
	}

	@Override
	public Time getTimePosition() {
		return timePosition;
	}

	@Override
	public void setTimePosition(Time timePosition) {
		ActionInvocation<RemoteService> invocSeek=new ActionInvocation<RemoteService>(avTransport.getAction("Seek"));
		invocSeek.setInput("InstanceID", String.valueOf(avTransportId));
		invocSeek.setInput("Unit","ABS_TIME");
		invocSeek.setInput("Target", timePosition.toString());
		new ActionCallback.Default(invocSeek, upnpService.getControlPoint()).run();
		
		setTimePositionHimself(timePosition);
	}
	
	private void setTimePositionHimself(Time timePosition){
		if (!this.timePosition.equals(timePosition)){
			Time old=this.timePosition;
			this.timePosition = timePosition;
			firePropertyChange(new PropertyChangeEvent(this,"timePosition",old,timePosition));
		}
	}
	
	@Override
	public int getVolume(){
		return volume;
	}
	
	@Override
	public void setVolume(int volume){
		System.out.println(volume);
		ActionInvocation<RemoteService> invocWatchVolume=new ActionInvocation<RemoteService>(renderingControl.getAction("SetVolume"));
		invocWatchVolume.setInput("InstanceID",String.valueOf(rcsId));
		invocWatchVolume.setInput("Channel", "Master");
		invocWatchVolume.setInput("DesiredVolume", String.valueOf(volume));
		new ActionCallback.Default(invocWatchVolume, upnpService.getControlPoint()).run();
		
		setVolumeHimself(volume);
	}
	
	private void setVolumeHimself(int volume){
		
		int old=this.volume;
		this.volume=volume;
		firePropertyChange(new PropertyChangeEvent(this,"volume",old,volume));
	}
	
	@Override
	public int getVolumeMax(){
		StateVariable<RemoteService> var=renderingControl.getStateVariable("Volume");
		StateVariableAllowedValueRange range=var.getTypeDetails().getAllowedValueRange();
		if (range!=null){
			return (int)range.getMaximum();
		}else return DEFAULT_VOLUME_MAX;
	}

	@Override
	public boolean isMute(){
		return mute;
	}
	
	@Override
	public void setMute(boolean mute){
		setMuteHimself(mute);
	}
	
	private void setMuteHimself(boolean mute){
		boolean old=this.mute;
		this.mute=mute;
		firePropertyChange(new PropertyChangeEvent(this,"mute",old,mute));
	}
	
	@Override
	public boolean accept(File file){
		String askProtocol=null;
		try {
			askProtocol="http-get:*:"+Files.probeContentType(file.toPath())+":*";
		} catch (IOException e){
			e.printStackTrace();
			return false;
		}
		
		if (sink==null){
			ActionCallback callback=new ActionCallback.Default(new ActionInvocation<RemoteService>(connectionManager.getAction("GetProtocolInfo")),upnpService.getControlPoint());
			callback.run();
			sink=callback.getActionInvocation().getOutput("Sink").getValue().toString();
		}
		
		if (sink!=null){
			if (sink.indexOf(askProtocol)>=0){
				return true;
			}else return false;
		}else return false;
	}
	
	private void startWatchin(){
		ActionInvocation<RemoteService> invocWatch=new ActionInvocation<RemoteService>(avTransport.getAction("GetPositionInfo"));
		invocWatch.setInput("InstanceID",String.valueOf(avTransportId));
		watchPositionInfo=new WatchService(upnpService.getControlPoint(),invocWatch,new String[]{"TrackDuration","AbsTime"},new WatchServiceListener(){

			@Override
			public void update(WatchServiceEvent wse) {
				for (String key:wse.getValues().keySet()){
					switch (key){
					case "TrackDuration":
						setDuration(Time.valueOf(wse.getValues().get(key).toString()));
						break;
						
					case "AbsTime":
						setTimePositionHimself(Time.valueOf(wse.getValues().get(key).toString()));
						break;
					}
				}
			}
			
		});
		
		ActionInvocation<RemoteService> invocWatchTransport=new ActionInvocation<RemoteService>(avTransport.getAction("GetTransportInfo"));
		invocWatchTransport.setInput("InstanceID", String.valueOf(avTransportId));
		watchTransportInfo=new WatchService(upnpService.getControlPoint(),invocWatchTransport,"CurrentTransportState",new WatchServiceListener(){

			@Override
			public void update(WatchServiceEvent wse) {
				for (String key:wse.getValues().keySet()){
					switch (key){
					case "CurrentTransportState":
						String value=wse.getValues().get(key).toString();
						switch (value){
						case "STOPPED":
							setStop(true);
							break;
						
						case "NO_MEDIA_PRESENT":
							setStop(true);
							break;
							
						case "TRANSITIONING":
							setPause(true);
							break;
							
						case "PAUSED_PLAYBACK":
							setPause(true);
							break;
							
						case "PLAYING":
							setPlay(true);
							break;
						}
						
						break;
					}
				}
			}
			
		});
	
		ActionInvocation<RemoteService> invocWatchVolume=new ActionInvocation<RemoteService>(renderingControl.getAction("GetVolume"));
		invocWatchVolume.setInput("InstanceID",String.valueOf(rcsId));
		invocWatchVolume.setInput("Channel","Master");
		watchVolume=new WatchService(upnpService.getControlPoint(),invocWatchVolume,"CurrentVolume",new WatchServiceListener(){

			@Override
			public void update(WatchServiceEvent wse) {
				for (String key:wse.getValues().keySet()){
					switch (key){
					case "CurrentVolume":
						setVolumeHimself(Integer.parseInt(wse.getValues().get(key).toString()));
						break;
					}
				}
			}
		});
		
		ActionInvocation<RemoteService> invocWatchMute=new ActionInvocation<RemoteService>(renderingControl.getAction("GetMute"));
		invocWatchMute.setInput("InstanceID", String.valueOf(rcsId));
		invocWatchVolume.setInput("Channel","Master");
		watchMute=new WatchService(upnpService.getControlPoint(),invocWatchMute,"CurrentMute",new WatchServiceListener(){

			@Override
			public void update(WatchServiceEvent wse) {
				System.out.println(wse.getValues());
				for (String key:wse.getValues().keySet()){
					switch (key){
					case "CurrentMute":
						System.out.println(wse.getValues().get(key));
						setMuteHimself(Boolean.parseBoolean(wse.getValues().get(key).toString()));
					}
				}
			}
			
		});
		
		
		
		watchPositionInfo.start();
		watchVolume.start();
		watchTransportInfo.start();
		//watchMute.start();
	}
	
	private void stopWatchin(){
		stopWatchin(watchPositionInfo,watchVolume,watchTransportInfo,watchMute);
	}
	
	private void stopWatchin(WatchService ... watchs){
		for (WatchService watch : watchs){
			if (watch!=null){
				watch.interrupt();
				try {
					watch.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
}
