package io.github.pitchblacknights.endritual;

import com.destroystokyo.paper.ParticleBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import java.lang.Math;

public class EndRitualCommand implements CommandExecutor {
    private final static JavaPlugin plugin = JavaPlugin.getPlugin(EndRitual.class);
    private Boolean run = true;

    private static void particleCircleEnd(final Entity particleCircle, final Entity flyingDisc, final World world) {
        //TODO: Fix the particle spread
        new ParticleBuilder(Particle.CLOUD).location(particleCircle.getLocation().add(0, 2.3, 0)).offset(1.0, 1.0, 1.0).count(1000).spawn();
        world.playSound(particleCircle.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, SoundCategory.AMBIENT, 100, 1);

        try {
            flyingDisc.remove();
        } catch (Exception e) {
            EndRitual.log.warning("The 'flyingDisc' was not present at deletion!\nSome thing might have gone wrong!");
        }
        particleCircle.remove();

    }

    private static void showBaseHelp(CommandSender sender) {
        sender.sendMessage(Component.text("Usage: /endritual <start|stop>").color(NamedTextColor.DARK_RED));
    }

    private static boolean teleport(final Entity entity, final Location destination) {
        if (!(entity instanceof Player)) {
            // Teleport passenger
            if (!entity.getPassengers().isEmpty()) {
                final Entity passenger = entity.getPassengers().get(0);
                entity.removePassenger(passenger);
                if (teleport(passenger, destination)) {
                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> entity.addPassenger(passenger));
                }
            }

            // Teleport entity
            try {
                entity.teleport(destination);
            } catch (Exception e) {
                return false;
            }
            return true;
        } else {
            return entity.teleport(destination);
        }
    }

    @Override
    public boolean onCommand(@NotNull final CommandSender sender, @NotNull final Command command, @NotNull final String label, @NotNull final String[] args) {
        Player player;
        World world;
        try {
            player = (Player) sender;
            world = player.getWorld();
        } catch (ClassCastException e) {
            sender.sendMessage(Component.text("This command can only be used by a Player").color(NamedTextColor.DARK_RED));
            return false;
        }

        if (args.length != 1 || args[0].equalsIgnoreCase("help")) {
            showBaseHelp(sender);
            return false;
        } else if (args[0].equalsIgnoreCase("start")) {
            // Runs when command is '/endritual start'
            Item flyingDisc = world.spawn(player.getLocation(), Item.class);
            flyingDisc.setCanMobPickup(false);
            flyingDisc.setCanPlayerPickup(false);
            flyingDisc.setGravity(false);
            flyingDisc.setItemStack(new ItemStack(Material.MUSIC_DISC_13, 1));

            ArmorStand particleCircle = world.spawn(player.getLocation().add(0, -1.7, 0), ArmorStand.class);
            particleCircle.setInvisible(true);
            particleCircle.setBasePlate(false);
            particleCircle.setMarker(false);
            particleCircle.setGravity(false);
            particleCircle.addPassenger(flyingDisc);
            particleCircle.setRotation(0, 0);

            particleCircleLoop(particleCircle, flyingDisc, world);
            return true;

        } else if (args[0].equalsIgnoreCase("stop")) {
            run = false;
            return true;
        } else {
            showBaseHelp(sender);
            return false;
        }
    }

    private void particleCircleLoop(final Entity particleCircle, final Entity flyingDisc, final World world) {
        double endY = particleCircle.getY() + 5.7;
        run = true;
        new BukkitRunnable() {
            @Override
            public void run() {
                Location location = particleCircle.getLocation();
                Location destination = particleCircle.getLocation();
                destination.add(0, 0.05, 0);
                destination.setPitch(location.getPitch());
                destination.setYaw(location.getYaw() + 6);
                teleport(particleCircle, destination);

                double particleX1 = particleCircle.getX()+0.6*Math.cos(particleCircle.getYaw());
                double particleZ1 = particleCircle.getZ()+0.6*Math.sin(particleCircle.getYaw());
                double particleX2 = particleCircle.getX()+0.6*-Math.cos(particleCircle.getYaw());
                double particleZ2 = particleCircle.getZ()+0.6*-Math.sin(particleCircle.getYaw());
                new ParticleBuilder(Particle.REDSTONE).color(51, 51, 51).location(world, particleX1, particleCircle.getY()+2.3, particleZ1).count(1).spawn();
                new ParticleBuilder(Particle.REDSTONE).color(51, 51, 51).location(world, particleX2, particleCircle.getY()+2.3, particleZ2).count(1).spawn();
                if (particleCircle.getY() >= endY || !run) {
                    particleCircleEnd(particleCircle, flyingDisc, world);
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}
