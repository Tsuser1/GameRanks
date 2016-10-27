package io.voidpowered.gameranks.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import io.voidpowered.gameranks.GameRanks;

public class Updater {
	GameRanks plugin;

	public Updater(GameRanks plugin) {
		this.plugin = plugin;
	}

	private String readurl = "https://raw.githubusercontent.com/J4D3N/GameRanks/master/distribution.txt";

	public void checkUpdates() {
		if (plugin.getConfig().getBoolean("update-notify", true)) {
			try {
				plugin.getLogger().info("Checking for a new version of GameRanks...");
				URL url = new URL(readurl);
				BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
				String str;
				while ((str = br.readLine()) != null) {
					// Build types: EDG - Bleeding edge  DEV - Latest beta build  PUB - Completely stable builds
					if (str.startsWith(plugin.getConfig().getString("release-type")) && !str.substring(3).startsWith(plugin.getDescription().getVersion())) {
						String[] updatemsg = str.split(":"); // Return the text after the colon as the update message
						if(updatemsg.length > 1){
							plugin.getLogger().info(updatemsg[1]);
						} else {
							plugin.getLogger().info("Updates were found. Please upgrade!");
						}
						return;
					}
				}
				br.close();
			} catch (IOException e) {
				plugin.getLogger().severe("Unable to fetch latest version information!");
			}
			plugin.getLogger().info("Finished checking for updates.");
		}
	}
}