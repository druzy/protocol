package druzy.protocol;

import java.io.File;
import java.io.FileFilter;
import java.sql.Time;

import druzy.mvc.Model;

public interface Renderer extends Device,Model,FileFilter {

	public static final int PORT_FILE=18041;
	
	public void play();
	public boolean isPlay();
	public void pause();
	public boolean isPause();
	public void stop();
	public boolean isStop();
	public boolean send(File f);
	public Time getDuration();
	public Time getTimePosition();
	public void setTimePosition(Time timePosition);
	public int getVolume();
	public void setVolume(int volume);
	public int getVolumeMax();
	public void setVolumeMax(int volumeMax);
	public int getVolumeMin();
	public void setVolumeMin(int volumeMin);
	public boolean isMute();
	public void setMute(boolean mute);
}

