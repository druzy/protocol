package druzy.protocol;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.security.GeneralSecurityException;

import javax.swing.ImageIcon;

import druzy.server.RestrictedFileServer;

import su.litvak.chromecast.api.v2.ChromeCast;
import su.litvak.chromecast.api.v2.Status;

public class ChromecastRenderer extends AbstractRenderer {

	private ChromeCast chromecast=null;
	private String identifier=null;
	private String name=null;
	private ImageIcon icon=null;
	private String protocol=null;
	private int iconPort=0;
	
	public static final int HTTP_SERVER_PORT=8965; 
	
	public static final String APP_ID_VIDEO="CC1AD845";
	
	public ChromecastRenderer(ChromeCast chromecast,String identifier, String name, URI icon){
		super();
		this.protocol="chromecast";
		this.iconPort=8008;
		this.chromecast=chromecast;
		this.identifier=identifier;
		this.name=name;
		try {
			this.icon=new ImageIcon(new URL("http://"+chromecast.getAddress()+":"+iconPort+icon.toString()));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

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
		return icon;
	}

	@Override
	public void setIcon(ImageIcon icon) {
		this.icon=icon;
	}

	@Override
	public void play() {
		try {
			System.out.println("avant play");
			if (!chromecast.isConnected()) chromecast.connect();
			if (chromecast.isAppAvailable(APP_ID_VIDEO)){
				if (!chromecast.isAppRunning(APP_ID_VIDEO)) chromecast.launchApp(APP_ID_VIDEO);
				chromecast.play();
				System.out.println("aès play");
			}
		
		} catch (IOException e) {
			e.printStackTrace();
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void pause() {
		try {
			chromecast.pause();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void stop() {
		try {
			chromecast.stopApp();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	public boolean send(File file) {
		try {
			if (!chromecast.isConnected()) chromecast.connect();
			
			Status status=chromecast.getStatus();
			if (chromecast.isAppAvailable(APP_ID_VIDEO) && !status.isAppRunning(APP_ID_VIDEO)){
				chromecast.launchApp(APP_ID_VIDEO);
			}
			System.out.println("dans send");
			RestrictedFileServer.getInstance(HTTP_SERVER_PORT).addAuthorizedFile(file);
			if (!RestrictedFileServer.getInstance(HTTP_SERVER_PORT).isStarting()) RestrictedFileServer.getInstance(HTTP_SERVER_PORT).start();
			chromecast.load(RestrictedFileServer.getInstance(HTTP_SERVER_PORT).toString()+file.getAbsolutePath());
			System.out.println("après load");
		
		} catch (IOException | GeneralSecurityException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}

}
