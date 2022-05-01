package de.blablubbabc.enforceVillagerTradeSelection;

import org.bukkit.configuration.Configuration;
import org.bukkit.plugin.Plugin;

import com.google.common.base.Preconditions;

public class Config {

	private final Plugin plugin;

	public boolean debug;

	Config(Plugin plugin) {
		Preconditions.checkNotNull(plugin);
		this.plugin = plugin;
	}

	public void load() {
		plugin.saveDefaultConfig();
		Configuration config = plugin.getConfig();

		debug = config.getBoolean("debug");
		Log.setDebugging(debug);
	}
}
