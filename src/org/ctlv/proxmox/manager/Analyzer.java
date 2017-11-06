package org.ctlv.proxmox.manager;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.security.auth.login.LoginException;

import org.ctlv.proxmox.api.Constants;
import org.ctlv.proxmox.api.ProxmoxAPI;
import org.ctlv.proxmox.api.data.LXC;
import org.json.JSONException;

public class Analyzer {
	ProxmoxAPI api;
	Controller controller;
	
	public Analyzer(ProxmoxAPI api, Controller controller) {
		this.api = api;
		this.controller = controller;
	}
	
	public void analyze(Map<String, List<LXC>> myCTsPerServer)  {

		if (!myCTsPerServer.isEmpty()) {
			// Calculer la quantit� de RAM utilis�e par mes CTs sur chaque serveur
			// ...
			HashMap<String, Integer> serversMem = new HashMap<>();
			
			for (String serverName : myCTsPerServer.keySet()) {
				int mem = 0;
				for (LXC ct : myCTsPerServer.get(serverName)) {
					mem += ct.getMem();
				}
				serversMem.put(serverName, mem);
			}
			
			// M�moire autoris�e sur chaque serveur
	
			HashMap<String, Float> authorisedMemPerServer = new HashMap<>();
			for (String serveur : myCTsPerServer.keySet()) {
				try {
					authorisedMemPerServer.put(serveur, api.getNode(serveur).getMemory_total() * Constants.MAX_THRESHOLD);
				} catch (LoginException | JSONException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
							
			}

			
			// Analyze et Actions
			// ...
			if(myCTsPerServer.containsKey(Constants.SERVER2) && myCTsPerServer.containsKey(Constants.SERVER1)) {
				if((serversMem.get(Constants.SERVER1) > authorisedMemPerServer.get(Constants.SERVER1)/2) && (serversMem.get(Constants.SERVER2) < authorisedMemPerServer.get(Constants.SERVER2)/2)) {
					controller.migrateFromTo(Constants.SERVER1, Constants.SERVER2);
					System.out.println("migrate 1 to 2");
				} else if((serversMem.get(Constants.SERVER1) < authorisedMemPerServer.get(Constants.SERVER1)/2) && (serversMem.get(Constants.SERVER2) > authorisedMemPerServer.get(Constants.SERVER2)/2)) {
					controller.migrateFromTo(Constants.SERVER2, Constants.SERVER1);
					System.out.println("migrate 2 to 1");
				} else if((serversMem.get(Constants.SERVER1) > authorisedMemPerServer.get(Constants.SERVER1)*0.75)) {
					controller.offLoad(Constants.SERVER1);
					System.out.println("offLoad 1 ");
				} else if((serversMem.get(Constants.SERVER2) > authorisedMemPerServer.get(Constants.SERVER2)*0.75)) {
					controller.offLoad(Constants.SERVER2);	
					System.out.println("offLoad 2");
				}
			} else if(myCTsPerServer.containsKey(Constants.SERVER1) && (serversMem.get(Constants.SERVER1) > authorisedMemPerServer.get(Constants.SERVER1)*0.75)) {
				controller.migrateFromTo(Constants.SERVER1, Constants.SERVER2);
				System.out.println("migrate 1 to 2 car null");
			} else if(myCTsPerServer.containsKey(Constants.SERVER2) && (serversMem.get(Constants.SERVER2) > authorisedMemPerServer.get(Constants.SERVER2)*0.75)) {
				controller.migrateFromTo(Constants.SERVER2, Constants.SERVER1);
				System.out.println("migrate 2 to 1 car null");
			} 
			
		
		}
		
	}

}
