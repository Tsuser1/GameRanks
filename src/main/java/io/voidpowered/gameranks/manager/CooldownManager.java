package io.voidpowered.gameranks.manager;

import java.util.UUID;

import org.bukkit.OfflinePlayer;

import io.voidpowered.gameranks.config.GRConfiguration;
import io.voidpowered.gameranks.util.CooldownType;

public class CooldownManager {
	private GRConfiguration users;
	
	public CooldownManager(GRConfiguration users){
		this.users = users;
	}
	
	
	
	/**
	 * Set the cooldown begin time for a player.
	 * @param player Player
	 * @param type Type of cooldown
	 */
	public void setCooldown(OfflinePlayer player, CooldownType type){
		if(player == null || type == null){
			return;
		}
		setCooldown(player.getUniqueId(), type);
	}
	
	/**
	 * Set the cooldown begin time for a player.
	 * @param uuid UUID of Player
	 * @param type Type of cooldown
	 */
	public void setCooldown(UUID uuid, CooldownType type){
		if(uuid == null || type == null){
			return;
		}
		users.getConfig().set("users." + uuid.toString() + ".cooldown." + type, System.currentTimeMillis());
		users.saveConfig();
	}
	
	/**
	 * Check if a player is still in a command cooldown.
	 * @param uuid UUID of Player
	 * @param type Type of cooldown
	 * @return Is player cooled down
	 */
	public boolean isCooling(UUID uuid, CooldownType type){
		if(uuid == null || type == null){
			return true; // Just say the player is cooling down if the request is invalid.
		}
		return (System.currentTimeMillis() - users.getConfig().getLong("users." + uuid.toString() + ".cooldown." + type) > type.getDuration()*1000) ? false : true;
	}
}
