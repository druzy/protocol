package druzy.protocol;

import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Time;

import javax.swing.ImageIcon;

import druzy.server.RestrictedFileServer;
import druzy.utils.TimeUtils;

public class ChromecastRenderer extends AbstractRenderer {

	private boolean play=false;
	private String identifier=null;
	private String name=null;
	private String protocol=null;
	//private int iconPort=8008;
	
	public static final String APP_ID_VIDEO="CC1AD845";
	public static final String CHROMECAST_ICON="druzy/protocol/image/chromecast-icon.png";
	
	public ChromecastRenderer(String identifier, String name){
		super();
		this.protocol="chromecast";
		this.identifier=identifier;
		this.name=name;
	}

	@Override
	public void play() {
		new Bridge(Bridge.PLAY,identifier).exec();
	}

	@Override
	public boolean isPlay(){
		return play;
	}
	
/*	private void setPlay(boolean play){
		if (play){
			setPause(false);
			setStop(false);
		}
		
		if (this.play!=play){
			boolean old=this.play;
			this.play=play;
			firePropertyChange(new PropertyChangeEvent(this,"play",old,play));
		}
	}*/
	
	@Override
	public String getIdentifier() {
		return identifier;
	}

	@Override
	public void setIdentifier(String identifier) {
		this.identifier=identifier;
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
		return name;
	}

	@Override
	public void setName(String name) {
		this.name=name;
	}

	@Override
	public ImageIcon getIcon() {
		return new ImageIcon(ClassLoader.getSystemResource(CHROMECAST_ICON));
	}

	

	@Override
	public void pause() {
		new Bridge(Bridge.PAUSE,identifier).exec();
	}

	@Override
	public void stop() {
		new Bridge(Bridge.STOP,identifier).exec();
	}

	@Override
	public boolean send(final File file) {
		if (!RestrictedFileServer.getInstance(PORT_FILE).isStarting()) RestrictedFileServer.getInstance(PORT_FILE).start();
		RestrictedFileServer.getInstance(PORT_FILE).addAuthorizedFile(file);
		String url=RestrictedFileServer.getInstance(PORT_FILE).toString();//+file.getAbsolutePath();
		try {
			url=url+URLEncoder.encode(file.getAbsolutePath(),"utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		System.out.println(url);
		Bridge b=new Bridge(Bridge.SEND,identifier,url,"video/mp4");
		b.exec();
		return true;
	}

	@Override
	public Time getDuration(){
		String strDuration=new Bridge(Bridge.MEDIA_STATUS,identifier).exec().get(0).get("duration");
		if (strDuration.equals("None")) return Time.valueOf("00:00:00");
		else{
			float f=Float.parseFloat(strDuration);
			return TimeUtils.secondsToTime((int)f);
		}
	}

}
