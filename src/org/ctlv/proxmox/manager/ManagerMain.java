package org.ctlv.proxmox.manager;

import java.io.IOException;

import javax.security.auth.login.LoginException;

import org.ctlv.proxmox.api.ProxmoxAPI;
import org.ctlv.proxmox.generator.GeneratorMain;
import org.json.JSONException;

public class ManagerMain {

	public static void main(String[] args) throws Exception {

		ProxmoxAPI api = new ProxmoxAPI();
		new Thread() {
			public void run() {
				try {
					GeneratorMain.generate(api);
					//GeneratorMain.main(null);
				} catch (LoginException | InterruptedException | JSONException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}.start();
		
		
		Analyzer analyzer = new Analyzer(api, new Controller(api));
		
		Thread th = new Thread(new Monitor(api, analyzer));
		
		th.start();
		
		
	}

}