package pl.zekq.shulkerElevator;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.util.Vector;
import org.bukkit.block.BlockFace;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ElevatorListener implements Listener {

    private final ShulkerElevator plugin;

    public ElevatorListener(ShulkerElevator plugin) {
        this.plugin = plugin;
    }

    private boolean isColorable(Material mat) {
        if (mat == null) return false;
        String name = mat.name();
        return name.endsWith("_WOOL") || 
               name.endsWith("_CONCRETE") || 
               name.endsWith("_CONCRETE_POWDER") || 
               name.endsWith("_TERRACOTTA") || 
               name.endsWith("_STAINED_GLASS") || 
               name.endsWith("_STAINED_GLASS_PANE") ||
               name.equals("GLASS") ||
               name.equals("GLASS_PANE") ||
               name.equals("TERRACOTTA");
    }

    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        Recipe recipe = event.getRecipe();
        if (recipe instanceof ShapedRecipe shaped && shaped.getKey().getKey().startsWith("elevator_")) {
            String typeId = shaped.getKey().getKey().replace("elevator_", "");
            ElevatorType type = plugin.getElevatorTypes().stream()
                    .filter(t -> t.getId().equals(typeId))
                    .findFirst().orElse(null);
            
            if (type == null) return;

            if (!type.getPermissionCraft().isEmpty()) {
                if (event.getViewers().get(0) instanceof Player player) {
                    if (!player.hasPermission(type.getPermissionCraft())) {
                        event.getInventory().setResult(null);
                        return;
                    }
                }
            }

            CraftingInventory inv = event.getInventory();
            ItemStack[] matrix = inv.getMatrix();
            Map<Material, Integer> counts = new HashMap<>();
            List<Material> order = new ArrayList<>();

            for (ItemStack item : matrix) {
                if (item != null && isColorable(item.getType())) {
                    Material mat = item.getType();
                    counts.put(mat, counts.getOrDefault(mat, 0) + 1);
                    if (!order.contains(mat)) order.add(mat);
                }
            }

            if (counts.isEmpty()) return;

            Material winner = null;
            int max = -1;
            for (Material mat : order) {
                int count = counts.get(mat);
                if (count > max) {
                    max = count;
                    winner = mat;
                }
            }

            if (winner != null) {
                inv.setResult(plugin.getRecipeManager().createElevatorItem(type, winner));
            }
        }
    }

    private ElevatorType getElevatorType(Block block) {
        if (block == null) return null;
        for (ElevatorType type : plugin.getElevatorTypes()) {
            boolean materialMatch = false;
            String configMat = type.getMaterialInput();
            if (configMat.startsWith("#")) {
                List<Material> allowed = plugin.getRecipeManager().getMaterialsFromTag(configMat.substring(1));
                if (allowed.contains(block.getType())) materialMatch = true;
            } else {
                if (block.getType() == Material.matchMaterial(configMat)) materialMatch = true;
            }
            if (materialMatch) return type;
        }
        return null;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() != null && getElevatorType(event.getClickedBlock()) != null) {
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Block placed = event.getBlockPlaced();
        ElevatorType type = getElevatorType(placed);
        if (type == null || !type.isAutoUp()) return;

        if (placed.getBlockData() instanceof Directional directional) {
            if (directional.getFacing() != BlockFace.UP) {
                directional.setFacing(BlockFace.UP);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        placed.setBlockData(directional, false);
                    }
                }.runTaskLater(plugin, 1L);
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (event.getTo() == null || event.getFrom().getY() >= event.getTo().getY()) return;

        double yVelocity = event.getTo().getY() - event.getFrom().getY();
        Block blockBelow = player.getLocation().getBlock().getRelative(0, -1, 0);

        ElevatorType type = getElevatorType(blockBelow);
        if (type != null) {
            if (yVelocity > 0.08) {
                if (!type.getPermissionUse().isEmpty() && !player.hasPermission(type.getPermissionUse())) {
                    player.sendMessage(plugin.getMessage("no-permission"));
                    return;
                }
                teleportToNextElevator(player, type, true);
            }
        }
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        if (!event.isSneaking()) return;

        Block blockBelow = player.getLocation().getBlock().getRelative(0, -1, 0);
        ElevatorType type = getElevatorType(blockBelow);
        if (type != null) {
            if (!type.getPermissionUse().isEmpty() && !player.hasPermission(type.getPermissionUse())) {
                player.sendMessage(plugin.getMessage("no-permission"));
                return;
            }
            teleportToNextElevator(player, type, false);
        }
    }

    private void teleportToNextElevator(Player player, ElevatorType type, boolean up) {
        Block base = player.getLocation().getBlock().getRelative(0, -1, 0);
        int step = up ? 1 : -1;
        String dir = up ? "up" : "down";

        for (int i = 1; i <= type.getMaxDistance(); i++) {
            Block check = base.getRelative(0, i * step, 0);
            
            if (!type.canPassThrough() && i > 1) {
                Block previous = base.getRelative(0, (i - 1) * step, 0);
                if (!previous.getType().isAir()) {
                    player.sendMessage(plugin.getMessage("obstruction-detected"));
                    return;
                }
            }

            ElevatorType checkType = getElevatorType(check);
            if (checkType != null) {
                Location loc = check.getLocation().add(0.5, 1, 0.5);
                loc.setYaw(player.getLocation().getYaw());
                loc.setPitch(player.getLocation().getPitch());

                player.teleport(loc);
                player.setVelocity(new Vector(0, 0, 0));
                player.sendMessage(plugin.getMessage("teleported").replace("%direction%", dir));
                return;
            }
        }
        player.sendMessage(plugin.getMessage("no-elevator-found").replace("%direction%", dir));
    }
}
