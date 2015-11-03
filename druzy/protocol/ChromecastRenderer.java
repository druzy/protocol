package druzy.protocol;

import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Time;

import javax.swing.ImageIcon;

import druzy.chromecast.Bridge;
import druzy.chromecast.BridgeEvent;
import druzy.chromecast.BridgeListener;
import druzy.chromecast.WatchChromecast;
import druzy.server.RestrictedFileServer;
import druzy.utils.TimeUtils;

public class ChromecastRenderer extends AbstractRenderer {

	private String identifier=null;
	private String name=null;
	private String protocol=null;
	private boolean play=false;
	private boolean pause=false;
	private boolean stop=false;
	private Time duration=null;
	private Time timePosition=null;
	private int volume=100;
	private boolean mute=false;
	
	private Bridge bridge=null;
	private WatchChromecast watch=null;
	
	//private int iconPort=8008;
	
	public static final String APP_ID_VIDEO="CC1AD845";
	public static final String CHROMECAST_ICON="druzy/protocol/image/chromecast-icon.png";
	
	public ChromecastRenderer(String identifier, String name){
		super();
		this.protocol="chromecast";
		this.identifier=identifier;
		this.name=name;
		this.bridge=new Bridge(Bridge.BRIDGE,identifier);
		bridge.exec(new BridgeListener(){

			@Override
			public void newMessage(BridgeEvent event) {
				String ask=event.getMessage().get("ask");
				switch(ask){
				case "media_status":
					//duration
					float f=Float.parseFloat(event.getMessage().get("duration"));
					ChromecastRenderer.this.setDurationHimself(TimeUtils.secondsToTime((int)f));
					
					//current_time
					f=Float.parseFloat(event.getMessage().get("current_time"));
					ChromecastRenderer.this.setTimePositionHimself(TimeUtils.secondsToTime((int)f));
					
					//player_state
					String playerState=event.getMessage().get("player_state");
					if (playerState.equals("PLAYING")){
						ChromecastRenderer.this.setPlay(true);
					}else if (playerState.equals("PAUSED")){
						ChromecastRenderer.this.setPause(true);
					}
					
					//volume
					f=Float.parseFloat(event.getMessage().get("volume_level"))*100;
					ChromecastRenderer.this.setVolumeHimself((int)f);
					
				}
			}
			
		});
		duration=Time.valueOf("00:00:00");
		timePosition=Time.valueOf("00:00:00");
	}

	@Override
	public String getIdentifier() {
		return identifier;
	}

	@Override
	public void setIdentifier(String identifier) {
		String old=this.identifier;
		this.identifier=identifier;
		firePropertyChange(new PropertyChangeEvent(this,"identifier",old,identifier));
	}

	@Override
	public String getProtocol() {
		return protocol;
	}

	@Override
	public void setProtocol(String protocol) {
		String old=this.protocol;
		this.protocol=protocol;
		firePropertyChange(new PropertyChangeEvent(this,"protocol",old,protocol));
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		String old=this.name;
		this.name=name;
		firePropertyChange(new PropertyChangeEvent(this,"name",old,name));
	}

	@Override
	public ImageIcon getIcon() {
		return new ImageIcon(ClassLoader.getSystemResource(CHROMECAST_ICON));
	}

	@Override
	public void shutdown(){
		stop();
		bridge.sendMessage("quit_app");
		bridge.sendMessage("exit");
	}
	
	@Override
	public void play() {
		bridge.sendMessage("play");
		setPlay(true);
	}

	@Override
	public boolean isPlay(){
		return play;
	}
	
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
		bridge.sendMessage("pause");
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
		stopWatch();
		bridge.sendMessage("stop");
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
	public boolean send(final File file) {
		if (!RestrictedFileServer.getInstance(PORT_FILE).isStarting()) RestrictedFileServer.getInstance(PORT_FILE).start();
		RestrictedFileServer.getInstance(PORT_FILE).addAuthorizedFile(file);
		String url=RestrictedFileServer.getInstance(PORT_FILE).toString();
		try {
			url=url+URLEncoder.encode(file.getAbsolutePath(),"utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return false;
		}
		bridge.sendMessage("send",url,"video/mp4");
		startWatch();
		return true;
	}

	@Override
	public Time getDuration(){
		return duration;
	}
	
	private void setDurationHimself(Time duration){
		Time old=this.duration;
		this.duration=duration;
		firePropertyChange(new PropertyChangeEvent(this,"duration",old,duration));
	}
	
	public Time getTimePosition() {
		return timePosition;
	}

	@Override
	public void setTimePosition(Time timePosition) {
		bridge.sendMessage("seek",String.valueOf(TimeUtils.timeToSeconds(timePosition)));
		
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
	public int getVolume(){return volume;}
	
	@Override
	public void setVolume(int volume){
		System.out.println(String.valueOf(volume/100.0));
		bridge.sendMessage("volume",String.valueOf(volume/100.0));
		
		setVolumeHimself(volume);
	}
	
	private void setVolumeHimself(int volume){
		int old=volume;
		this.volume=volume;
		firePropertyChange(new PropertyChangeEvent(this,"volume",old,volume));
	}
	
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
	
 	private void startWatch(){
		watch=new WatchChromecast(bridge);
		watch.start();
	}
	
	private void stopWatch(){
		if (watch!=null) watch.interrupt();
	}
}