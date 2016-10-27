package io.voidpowered.gameranks.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import org.bukkit.ChatColor;

import io.voidpowered.gameranks.GameRanks;

/* [ Build types reference for *GLOBAL* distribution ]
 * TESTING LEVEL - SHORT - DESC.
 * ZERO testing  : ALL   : Any published build (Untested)
 * SOME testing  : EDG   : Bleeding edge
 * MOSTLY tested : DEV   : Latest beta build
 * FULLY tested  : PUB   : Completely stable builds
*/

/**
 * Update notifications for GameRanks. Uses the GameRanks global distribution standards (ALL, EDG, DEV, and PUB). See source for full information and documentation of the class and standard.
 * @author Tsuser1
 */
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
					if (str.startsWith(plugin.getConfig().getString("release-type")) && !str.substring(3).startsWith(plugin.getDescription().getVersion())) {
						String[] updatemsg = str.split(":"); // Return the text after the colon as the update message
						if(updatemsg.length > 1){
							plugin.getServer().getConsoleSender().sendMessage(ChatColor.YELLOW + "NOTICE: " + ChatColor.DARK_RED + updatemsg[1]);
						} else {
							plugin.getServer().getConsoleSender().sendMessage(ChatColor.YELLOW + "NOTICE: " + ChatColor.DARK_RED + "Updates were found. Please upgrade!");
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