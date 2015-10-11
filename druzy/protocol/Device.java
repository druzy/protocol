package druzy.protocol;

import javax.swing.ImageIcon;

public interface Device{
	
	public String getIdentifier();
	public void setIdentifier(String identifier);
	public String getProtocol();
	public void setProtocol(String protocol);
	public String getName();
	public void setName(String name);
	public ImageIcon getIcon();
	public void setIcon(ImageIcon icon);
	public void shutdown();
}
