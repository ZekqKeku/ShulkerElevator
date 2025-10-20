package pl.foxheaven.shulkerElevator;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Directional;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import org.bukkit.block.BlockFace;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class ElevatorListener implements Listener {

    private final JavaPlugin plugin;
    private final Material elevatorBlock;
    private final int maxDistance;
    private final boolean autoUp;

    public ElevatorListener(JavaPlugin plugin) {
        this.plugin = plugin;
        FileConfiguration config = plugin.getConfig();

        Material mat = Material.matchMaterial(config.getString("elevator-block", "PURPLE_SHULKER_BOX"));
        if (mat == null) mat = Material.PURPLE_SHULKER_BOX;

        this.elevatorBlock = mat;
        this.maxDistance = config.getInt("max-distance", 32);
        this.autoUp = config.getBoolean("auto-up", true);
    }

    // Prevent opening the elevator block (right-click)
    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() != null && event.getClickedBlock().getType() == elevatorBlock) {
            event.setCancelled(true);
        }
    }

    // Force block facing upward when placed — but only if it's needed, and only once (next tick)
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!autoUp) return;
        Block placed = event.getBlockPlaced();
        if (placed.getType() != elevatorBlock) return;

        // if the block data supports Directional and is not already UP -> schedule one-time change next tick
        if (placed.getBlockData() instanceof Directional directional) {
            if (directional.getFacing() != BlockFace.UP) {
                // schedule next tick to avoid recursion or other immediate event interactions
                Bukkit.getScheduler().runTask(plugin, () -> {
                    Block b = placed.getWorld().getBlockAt(placed.getLocation());
                    if (b.getType() != elevatorBlock) return; // sanity
                    if (b.getBlockData() instanceof Directional d2) {
                        if (d2.getFacing() != BlockFace.UP) {
                            d2.setFacing(BlockFace.UP);
                            b.setBlockData(d2, false); // don't apply physics to reduce side effects
                        }
                    }
                });
            }
        }
    }

    // Detect jump → move up
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (event.getTo() == null || event.getFrom().getY() >= event.getTo().getY()) return;

        Block blockBelow = player.getLocation().getBlock().getRelative(0, -1, 0);
        if (blockBelow.getType() == elevatorBlock) {
            if (player.getVelocity().getY() > 0) {
                teleportToNextElevator(player, true);
            }
        }
    }

    // Detect sneak → move down
    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        if (!event.isSneaking()) return;

        Block blockBelow = player.getLocation().getBlock().getRelative(0, -1, 0);
        if (blockBelow.getType() == elevatorBlock) {
            teleportToNextElevator(player, false);
        }
    }

    private void teleportToNextElevator(Player player, boolean up) {
        Block base = player.getLocation().getBlock().getRelative(0, -1, 0);
        int step = up ? 1 : -1;

        for (int i = 1; i <= maxDistance; i++) {
            Block check = base.getRelative(0, i * step, 0);
            if (check.getType() == elevatorBlock) {
                // Keep yaw/pitch (player's looking angle)
                Location loc = check.getLocation().add(0.5, 1, 0.5);
                loc.setYaw(player.getLocation().getYaw());
                loc.setPitch(player.getLocation().getPitch());

                player.teleport(loc);
                player.setVelocity(new Vector(0, 0, 0));
                player.sendMessage("§aTeleported " + (up ? "up!" : "down!"));
                return;
            }
        }
        player.sendMessage("§cNo elevator " + (up ? "above you!" : "below you!"));
    }
}
