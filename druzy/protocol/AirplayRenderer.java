package druzy.protocol;

import java.io.File;

import javax.jmdns.ServiceInfo;
import javax.swing.ImageIcon;

public class AirplayRenderer extends AbstractRenderer {

	private ServiceInfo info=null;
	private String protocol=null;
	
	public static final String AIRPLAY_ICON="druzy/protocol/image/airplay-icon.png";
	public static final String ID_PROPERTY="deviceid";
	
	public AirplayRenderer(ServiceInfo info) {
		super();
		this.info=info;
		this.protocol="airplay";
	}

	@Override
	public String getIdentifier() {
		return info.getPropertyString(ID_PROPERTY);
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
		return info.getName();
	}

	@Override
	public ImageIcon getIcon() {
		return new ImageIcon(ClassLoader.getSystemResource(AIRPLAY_ICON));
	}

	@Override
	public void play() {
		// TODO Auto-generated method stub

	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub

	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean send(File f) {
		return false;
	}


}
