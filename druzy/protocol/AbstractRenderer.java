package druzy.protocol;


import java.io.File;
import java.sql.Time;

import javax.swing.ImageIcon;

import druzy.mvc.AbstractModel;

public abstract class AbstractRenderer extends AbstractModel implements Renderer {

	protected final int DEFAULT_VOLUME_MAX=100;
	
	public AbstractRenderer() {
		super();
	}

	@Override
	abstract public String getIdentifier();

	@Override
	public void setIdentifier(String identifier) {
		throw new UnsupportedOperationException("setIdentifier is not supportable");
	}

	@Override
	public String getProtocol() {
		return null;
	}

	@Override
	public void setProtocol(String protocol) {
		throw new UnsupportedOperationException("setProtocol is not supportable");
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public void setName(String name) {
		throw new UnsupportedOperationException("setName is not supportable");
	}

	@Override
	public ImageIcon getIcon() {
		return null;
	}

	@Override
	public void setIcon(ImageIcon icon) {
		throw new UnsupportedOperationException("setIcon is not supportable");
	}

	@Override
	public void play() {}
	
	@Override
	public boolean isPlay(){return false;}

	@Override
	public void pause() {}
	
	@Override
	public boolean isPause(){return false;}
	
	@Override
	public void stop() {}
	
	@Override
	public boolean isStop(){return false;}

	@Override
	public boolean send(File f) {
		return false;
	}
	
	@Override
	public String toString(){
		return "Name : "+getName()+"\nIdentifier : "+getIdentifier();
	}
	
	@Override
	public boolean equals(Object o){
		if (o instanceof Renderer){
			Renderer r=(Renderer)o;
			return this.getIdentifier().equals(r.getIdentifier());
		}else return false;
	}
	
	@Override
	public void shutdown(){}

	@Override
	public Time getDuration(){
		return null;
	}
	
	@Override
	public Time getTimePosition(){
		return null;
	}
	
	@Override
	public void setTimePosition(Time timeDuration){
		throw new UnsupportedOperationException("setTimePosition is not implementable");
	}

	@Override
	public int getVolume(){
		return 0;
	}
	
	@Override
	public void setVolume(int volume){
		throw new UnsupportedOperationException("setVolume is not implementable");
	}
	
	@Override
	public int getVolumeMax(){
		return DEFAULT_VOLUME_MAX; 
	}
	
	@Override
	public void setVolumeMax(int volumeMax){
		throw new UnsupportedOperationException("volumeMax is not implementable");
	}
	
	@Override
	public int getVolumeMin(){
		return 0; 
	}
	
	@Override
	public void setVolumeMin(int volumeMax){
		throw new UnsupportedOperationException("volumeMax is not implementable");
	}
	
	@Override
	public boolean isMute(){
		return false;
	}
	
	@Override
	public void setMute(boolean mute){
		throw new UnsupportedOperationException("setMute is not implementable");
	}
	
	@Override
	public boolean accept(File file){
		return true;
	}

}
