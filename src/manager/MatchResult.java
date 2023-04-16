package manager;

import config.ConfigEntry;

public class MatchResult {
	private final String action;
	private final ConfigEntry rule;

	MatchResult(String action, ConfigEntry rule) {
		this.action = action;
		this.rule = rule;
	}

	public String getAction() {
		return action;
	}

	public ConfigEntry getRule() {
		return rule;
	}
	
	@Override
	public String toString() {
		return "action:"+action+" rule:"+rule;
	}
}