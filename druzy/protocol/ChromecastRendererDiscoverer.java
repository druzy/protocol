package druzy.protocol;

import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.xbill.DNS.Message;
import org.xbill.DNS.Record;
import org.xbill.DNS.ResolverListener;
import org.xbill.DNS.TXTRecord;
import org.xbill.mDNS.Browse;
import su.litvak.chromecast.api.v2.ChromeCast;

public class ChromecastRendererDiscoverer implements Discoverer {

	public static final String CHROMECAST_TYPE="_googlecast._tcp.local.";
	public static final String ID="id";
	public static final String URI_ICON="ic";
	public static final String NAME="fn";
	
	
	public ChromecastRendererDiscoverer() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void startDiscovery(int delay, final DiscoveryListener listener){
		new Thread(){
			public void run(){
				final Set<String> castDiscovered=new HashSet<String>();
				
				Browse browse;
				try {
					browse = new Browse("_googlecast._tcp.local.");
					browse.start(new ResolverListener(){

						@Override
						public void handleException(Object o, Exception e) {
							e.printStackTrace();
						}

						@Override
						public void receiveMessage(Object o, Message message) {
							synchronized(castDiscovered){
								String id=null;
								URI uriIcon=null;
								String name=null;
								//TXT
								if (message.getSectionArray(3).length>0){
									Record record=message.getSectionArray(3)[0];
									if (record instanceof TXTRecord){
										TXTRecord txt=(TXTRecord)message.getSectionArray(3)[0];
										List<String> listTxt=txt.getStrings();
										
										
										
										//System.out.println(listTxt);
										for (String str:listTxt){
											String key=str.substring(0,str.indexOf("="));
											String value=str.substring(str.indexOf("=")+1);
											
											switch(key){
											case ID :
												id=value;
												break;
												
											case URI_ICON:
												uriIcon=URI.create(value);
												break;
												
											case NAME:
												name=value;
												break;
											
											}
										}
									}	
									
									if (!castDiscovered.contains(id)){
										castDiscovered.add(id);
										//IP
										String ip=message.getSectionArray(3)[2].rdataToString();
									
										final String id2=id;
										final String name2=name;
										final URI uriIcon2=uriIcon;
										
										final ChromeCast cast=new ChromeCast(ip);
										new Thread(){
											public void run(){
												listener.deviceDiscovery(new ChromecastRenderer(cast,id2,name2,uriIcon2));
											}
										}.start();
									}
								}
							}
						}
						 
					 
					 });
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
			}
		}.start();

	}

	@Override
	public void startDiscovery(int delay, String identifier,
			DiscoveryListener listener) {
		// TODO Auto-generated method stub
		
	}
 	
	@Override
	public void stopDiscovery() {
		// TODO Auto-generated method stub

	}

	@Override
	public void restartDiscovery() {
		// TODO Auto-generated method stub

	}

	

}
