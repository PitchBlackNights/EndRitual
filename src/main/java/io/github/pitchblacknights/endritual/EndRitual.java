package io.github.pitchblacknights.endritual;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.logging.Logger;

@SuppressWarnings("unused")
public final class EndRitual extends JavaPlugin {
    public final static Logger log = Bukkit.getLogger();

    @Override
    public void onEnable() {
        // Plugin startup logic
        log.info("Enabled");
        Objects.requireNonNull(getCommand("endritual")).setExecutor(new EndRitualCommand());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        log.info("Disabled");
    }
}
