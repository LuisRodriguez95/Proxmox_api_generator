package firstPart;

import java.io.IOException;

import javax.security.auth.login.LoginException;

import org.ctlv.proxmox.api.ProxmoxAPI;
import org.ctlv.proxmox.api.data.LXC;
import org.json.JSONException;

public class Main {
	public static void main(String[] args) throws LoginException, JSONException, IOException {
		ProxmoxAPI api = new ProxmoxAPI();	
		
		// % d’utilisation de la CPU
		float cpu = api.getNode("srv-px1").getCpu();
		System.out.println("%cpu = " + cpu);
		// % d'utilisation de la RAM
		float ram = api.getNode("srv-px1").getMemory_used()/api.getNode("srv-px2").getMemory_total();
		System.out.println("%Ram " + ram);
		// % d'utilisation des disques durs
		float disk = api.getNode("srv-px1").getRootfs_used()/api.getNode("srv-px2").getRootfs_total();
		System.out.println("%disk " + disk);
		
		// Creation d'un ct dans le serveur px2
		api.createCT("srv-px2", "11580", "ct-tpgei-ctlv-A15-ct01", 16);
		
		//Lancement du ct
		api.startCT("srv-px2", "11580");
		
		//Informations relatives à un conteneur
		LXC ct = api.getCT("srv-px2", "11580");
		System.out.println(ct.getName() + " Cpu :" + ct.getCpu() + " Disk:" + ct.getDisk() + " Ram:" + ct.getMem());
		/* 
		List<LXC> cts = api.getCTs("srv-px2");
		for (LXC lxc : cts) {
			System.out.println(lxc.getName() + " Cpu :" + lxc.getCpu() + " Disk:" + lxc.getDisk() + " Ram:" + lxc.getMem());
		}
		*/
		
	}
}
