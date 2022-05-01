package de.blablubbabc.enforceVillagerTradeSelection;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class EnforceVillagerTradeSelectionPlugin extends JavaPlugin {

	public final Config config = new Config(this);

	@Override
	public void onLoad() {
		Log.setUp(getLogger());
		config.load();
	}

	@Override
	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(new EventListener(), this);
	}

	@Override
	public void onDisable() {
	}
}
