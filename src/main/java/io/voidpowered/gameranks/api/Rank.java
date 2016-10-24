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

	public int getId() {
		return id;
	}
	
	public String[] getPermissions() {
		return permissions;
	}

	public void setPermissions(String[] permissions) {
		this.permissions = permissions;
	}
	
	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getSuffix() {
		return suffix;
	}

	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}

	public String getName() {
		return name;
	}

	public double getRefund() {
		return refund;
	}

	public void setRefund(double refund) {
		this.refund = refund;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public String[] getDescription() {
		return description;
	}

	public void setDescription(String[] description) {
		this.description = description;
	}
	
}
