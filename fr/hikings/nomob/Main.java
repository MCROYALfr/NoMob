package fr.hikings.nomob;

import java.util.HashSet;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldguard.bukkit.RegionContainer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class Main extends JavaPlugin implements CommandExecutor, Listener {
	private final HashSet<String> regions = new HashSet<>();
	private final HashSet<String> bypass = new HashSet<>();

	@Override
	public void onEnable() {

		saveDefaultConfig();

		getConfig().getStringList("regions").stream().forEach(s -> regions.add(s));
		getConfig().getStringList("bypass").stream().forEach(s -> bypass.add(s));

		getCommand("nomob").setExecutor(this);

		getServer().getPluginManager().registerEvents(this, this);
	}

	@Override
	public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
		if (sender.isOp() && args.length == 1 && "reload".equalsIgnoreCase(args[0])) {

			reloadConfig();

			regions.clear();
			bypass.clear();

			getConfig().getStringList("regions").stream().forEach(s -> regions.add(s));
			getConfig().getStringList("bypass").stream().forEach(s -> bypass.add(s));

			sender.sendMessage("Configuration rechargée");
		}
		return true;
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onCreatureSpawn(final CreatureSpawnEvent event) {
		if (event.getSpawnReason() != SpawnReason.NATURAL) {
			return;
		}
		final RegionContainer regionContainer = WorldGuardPlugin.inst().getRegionContainer();

		if (regionContainer == null) {
			return;
		}
		final ApplicableRegionSet applicableRegions = regionContainer.createQuery().getApplicableRegions(event.getLocation());

		boolean cancelled = false;

		for (final ProtectedRegion protectedRegion : applicableRegions.getRegions()) {

			if (regions.contains(protectedRegion.getId())) {
				cancelled = true;
			}

			if (bypass.contains(protectedRegion.getId())) {
				return;
			}
		}

		if (cancelled) {
			event.setCancelled(true);
		}
	}
}
