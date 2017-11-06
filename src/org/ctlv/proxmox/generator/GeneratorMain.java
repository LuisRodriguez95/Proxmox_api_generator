package org.ctlv.proxmox.generator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.security.auth.login.LoginException;

import org.ctlv.proxmox.api.Constants;
import org.ctlv.proxmox.api.ProxmoxAPI;
import org.ctlv.proxmox.api.data.LXC;
import org.json.JSONException;

public class GeneratorMain {

	public static Map<String, List<LXC>> myCTsPerServer = new HashMap<String, List<LXC>>();
	
	private static ProxmoxAPI api;
	

	static Random rndTime = new Random(new Date().getTime());
	public static int getNextEventPeriodic(int period) {
		return period;
	}
	public static int getNextEventUniform(int max) {
		return rndTime.nextInt(max);
	}
	public static int getNextEventExponential(int inv_lambda) {
		float next = (float) (- Math.log(rndTime.nextFloat()) * inv_lambda);
		return (int)next;
	}
	
	public static void generate(ProxmoxAPI api) throws LoginException, InterruptedException, JSONException, IOException {
		GeneratorMain.api = api;
		GeneratorMain.main(null);
	}
	
	public static void main(String[] args) throws InterruptedException, LoginException, JSONException, IOException {
		
		long baseID = Constants.CT_BASE_ID;
		int lambda = 30;
		
		

		//ProxmoxAPI api = new ProxmoxAPI();
		Random rndServer = new Random(new Date().getTime());
		Random rndRAM = new Random(new Date().getTime()); 
		
		long memAllowedOnServer1 = (long) (api.getNode(Constants.SERVER1).getMemory_total() * Constants.MAX_THRESHOLD);
		long memAllowedOnServer2 = (long) (api.getNode(Constants.SERVER2).getMemory_total() * Constants.MAX_THRESHOLD);
		
		
		int ctNum = 1;
		
		while (true) {
			
			
			// 1. Calculer la quantit� de RAM utilis�e par mes CTs sur chaque serveur
			long memOnServer1 = 0;
			List<LXC> ctsServer1 = myCTsPerServer.get("srv-px1");
			if(ctsServer1 != null) {
				for(LXC ct : ctsServer1) {
					memOnServer1 += ct.getMem();
				}
			}
			
			long memOnServer2 = 0;
			List<LXC> ctsServer2 = myCTsPerServer.get("srv-px2");
			if(ctsServer2 != null) {
				for(LXC ct : ctsServer2) {
					memOnServer2 += ct.getMem();
				}
			}
			
			System.out.println("GeneratorMain >> MemOnServer1 = " + memOnServer1);
			System.out.println("GeneratorMain >> MemOnServer2 = " + memOnServer2);
			
			// M�moire autoris�e sur chaque serveur
			float memRatioOnServer1 = (float) 0.16 * api.getNode("srv-px1").getMemory_total();
			float memRatioOnServer2 = (float) 0.16 * api.getNode("srv-px2").getMemory_total(); 
			
			// Exemple de condition de l'arr�t de la g�n�ration de CTs
			if (memOnServer1 < memRatioOnServer1 && memOnServer2 < memRatioOnServer2) {  
				
				// choisir un serveur al�atoirement avec les ratios sp�cifi�s 66% vs 33%
				String serverName;
				if (rndServer.nextFloat() < Constants.CT_CREATION_RATIO_ON_SERVER1)
					serverName = Constants.SERVER1;
				else
					serverName = Constants.SERVER2;
				System.out.println("Creating on " + serverName);
				
				// cr�er un contenaire sur ce serveur
				api.createCT(serverName, String.valueOf(baseID+ctNum), Constants.CT_BASE_NAME+ctNum, 16);

								
				// planifier la prochaine cr�ation
				int timeToWait = getNextEventExponential(lambda); // par exemple une loi expo d'une moyenne de 30sec
				System.out.println("timeToWait " + timeToWait);
				// attendre jusqu'au prochain �v�nement
				Thread.sleep(1000 * timeToWait);

				//Lancement du contenaire
				try {
					api.startCT(serverName, String.valueOf(baseID+ctNum));
					
				} catch (Exception e) {
					Thread.sleep(15000);
					api.startCT(serverName, String.valueOf(baseID+ctNum));
					System.out.println("Timeout error, retrying start");
					
				}
				
				if(myCTsPerServer.get(serverName) == null) {
					List<LXC> tmp = new ArrayList<LXC>();
					tmp.add(api.getCT(serverName, String.valueOf(baseID+ctNum)));
					myCTsPerServer.put(serverName,tmp);
				} else {
					List<LXC> tmp = myCTsPerServer.get(serverName);
					tmp.add(api.getCT(serverName, String.valueOf(baseID+ctNum)));
					myCTsPerServer.put(serverName,tmp);
				}
				
				ctNum++;
			}
			else {
				System.out.println("Servers are loaded, waiting ...");
				Thread.sleep(Constants.GENERATION_WAIT_TIME* 1000);
			}
		}
		
	}

}
