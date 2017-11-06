package org.ctlv.proxmox.manager;

import java.io.IOException;
import java.util.List;

import javax.security.auth.login.LoginException;

import org.ctlv.proxmox.api.Constants;
import org.ctlv.proxmox.api.ProxmoxAPI;
import org.json.JSONException;

public class Controller {

	ProxmoxAPI api;
	public Controller(ProxmoxAPI api){
		this.api = api;
	}
	
	// migrer un conteneur du serveur "srcServer" vers le serveur "dstServer"
	public void migrateFromTo(String srcServer, String dstServer)  {
		try {
			List<String> ctList = api.getCTList(srcServer);
			api.migrateCT(srcServer, ctList.get(0), dstServer);
		} catch (LoginException | JSONException | IOException e) {
			// TOsDO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// arr�ter le plus vieux conteneur sur le serveur "server"
	public void offLoad(String server) {
		try {
			String oldest = "";
			long oldestTime = 0;
			List<String> ctList = api.getCTList(server);
			for (int i = 0; i < ctList.size(); i++) {
		
				long time = api.getCT(server, ctList.get(i)).getUptime();
				if (time > oldestTime){
					oldest = ctList.get(i);
				}
			}
			api.stopCT(server, oldest);
		} catch (LoginException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
