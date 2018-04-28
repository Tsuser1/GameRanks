package io.voidpowered.gameranks.util;

/**
 * This provides the various types of cooldowns that are available for use in GameRanks and the ability to retrieve and set them.
 * @author Tsuser1
 */
public enum CooldownType {
	RANKUP(3L),
	RANKDOWN(3L),
	LIST(3L);
	
	private Long cooldown;
	
	CooldownType(Long duration){
		this.cooldown = duration;
	}
	
	/**
	 * Get the duration of a cooldown.
	 * @return Time in seconds
	 */
	public Long getDuration(){
		return cooldown;
	}
	
	/**
	 * Set the duration of a cooldown.
	 * @param duration Time in seconds
	 */
	public void setDuration(Long duration){
		this.cooldown = duration;
	}
}
