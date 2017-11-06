package org.ctlv.proxmox.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ctlv.proxmox.api.Constants;
import org.ctlv.proxmox.api.ProxmoxAPI;
import org.ctlv.proxmox.api.data.LXC;
import org.ctlv.proxmox.generator.GeneratorMain;

public class Monitor implements Runnable {

	Analyzer analyzer;
	ProxmoxAPI api;
	Map<String, List<LXC>> myCTsPerServer = new HashMap<String, List<LXC>>();
	
	public Monitor(ProxmoxAPI api, Analyzer analyzer) {
		this.api = api;
		this.analyzer = analyzer;
	}
	
	List<LXC> myCts = new ArrayList<LXC>();

	@Override
	public void run() {
		
		while(true) {
			
			// Lancer l'analyse
			analyzer.analyze(GeneratorMain.myCTsPerServer);

			
			// attendre une certaine p�riode
			try {
				Thread.sleep(Constants.MONITOR_PERIOD * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}
}
