package io.voidpowered.gameranks.api;

public final class Rank {

	private final int id;
	private final String name;
	
	private String[] permissions;
	private String[] description;
	private String prefix, suffix, group;
	private double price, refund;
	
	public Rank(int id, String name) {
		this.id = id;
		this.name = name;
		this.permissions = new String[0];
		this.description = new String[0];
		this.prefix = null;
		this.suffix = null;
		this.group = null;
		this.price = 0D;
		this.refund = 0D;
	}

	/**
	 * Returns the ID of the rank.
	 * @return Rank ID
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * Returns the permissions associated with the rank.
	 * @return Permissions
	 */
	public String[] getPermissions() {
		return permissions;
	}

	/**
	 * Manually defines the permissions for the rank. Does not apply to configuration.
	 * @param permissions New rank permissions.
	 */
	public void setPermissions(String[] permissions) {
		this.permissions = permissions;
	}
	
	/**
	 * Returns the prefix of the rank.
	 * @return Rank prefix
	 */
	public String getPrefix() {
		return prefix;
	}

	/**
	 * Manually defines the prefix of the rank. Does not apply to configuration.
	 * @param prefix New rank prefix
	 */
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	/**
	 * Returns the suffix of the rank.
	 * @return Rank suffix
	 */
	public String getSuffix() {
		return suffix;
	}

	/**
	 * Manually defines the suffix of the rank. Does not apply to configuration.
	 * @param prefix New rank suffix
	 */
	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}

	/**
	 * Returns the name of the rank.
	 * @return Rank name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the refund value of the rank.
	 * @return Refund value
	 */
	public double getRefund() {
		return refund;
	}

	/**
	 * Manually defines the refund value of the rank. Does not apply to configuration.
	 * @param refund New refund value of rank
	 */
	public void setRefund(double refund) {
		this.refund = refund;
	}

	/**
	 * Returns the price value of the rank.
	 * @return Price value
	 */
	public double getPrice() {
		return price;
	}

	/**
	 * Manually defines the price value of the rank. Does not apply to configuration.
	 * @param price New price value of rank
	 */
	public void setPrice(double price) {
		this.price = price;
	}

	/**
	 * Returns the group of the rank.
	 * @return Rank group
	 */
	public String getGroup() {
		return group;
	}

	/**
	 * Manually defines the group of the rank. Does not apply to configuration.
	 * @param group
	 */
	public void setGroup(String group) {
		this.group = group;
	}

	/**
	 * Returns description of the rank.
	 * @return Rank description
	 */
	public String[] getDescription() {
		return description;
	}

	/**
	 * Manually defines the description of the rank. Does not apply to configuration.
	 * @param description New rank description
	 */
	public void setDescription(String[] description) {
		this.description = description;
	}
	
}
