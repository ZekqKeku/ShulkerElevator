package pl.foxheaven.shulkerElevator;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.ChatColor;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ElevatorRecipe {

    private final JavaPlugin plugin;

    public ElevatorRecipe(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void registerRecipe() {
        FileConfiguration config = plugin.getConfig();

        // Elevator block type
        Material resultMaterial = Material.matchMaterial(config.getString("elevator-block", "PURPLE_SHULKER_BOX"));
        if (resultMaterial == null) resultMaterial = Material.PURPLE_SHULKER_BOX;

        // Prepare item
        ItemStack elevator = new ItemStack(resultMaterial);
        ItemMeta meta = elevator.getItemMeta();

        // Display name with colors
        String displayName = config.getString("block-name", "&6&lElevator");
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));

        // Lore with colors
        List<String> lore = config.getStringList("block-lore").stream()
                .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                .collect(Collectors.toList());
        if (!lore.isEmpty()) meta.setLore(lore);

        // Glow: try Paper/Spigot newer API first (setEnchantmentGlintOverride), fallback to hidden enchant
        boolean glowRequested = config.getBoolean("glow", false);
        if (glowRequested) {
            boolean applied = trySetGlintOverride(meta, true);
            if (!applied) {
                // fallback: add harmless enchant and hide it
                meta.addEnchant(Enchantment.EFFICIENCY, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
        }

        elevator.setItemMeta(meta);

        // Create shaped recipe
        NamespacedKey key = new NamespacedKey(plugin, "shulker_elevator");
        ShapedRecipe recipe = new ShapedRecipe(key, elevator);
        recipe.shape(config.getStringList("recipe.shape").toArray(new String[0]));

        // Ingredients
        Map<String, Object> ingredients = config.getConfigurationSection("recipe.ingredients").getValues(false);
        for (Map.Entry<String, Object> entry : ingredients.entrySet()) {
            char symbol = entry.getKey().charAt(0);
            Material mat = Material.matchMaterial(entry.getValue().toString());
            if (mat != null) recipe.setIngredient(symbol, mat);
        }

        Bukkit.addRecipe(recipe);
        plugin.getLogger().info("Elevator recipe registered from config.yml (glow=" + glowRequested + ")");
    }

    /**
     * Tries to call ItemMeta#setEnchantmentGlintOverride(Boolean) via reflection.
     * Returns true if call succeeded and glint override set, false otherwise.
     *
     * This avoids compile-time dependency issues when building against APIs
     * that don't have the method.
     */
    private boolean trySetGlintOverride(ItemMeta meta, boolean value) {
        try {
            Method m = meta.getClass().getMethod("setEnchantmentGlintOverride", Boolean.class);
            if (m != null) {
                m.invoke(meta, Boolean.valueOf(value));
                return true;
            }
        } catch (NoSuchMethodException ignored) {
            // method not present on this server API
        } catch (Throwable t) {
            plugin.getLogger().warning("Failed to set enchantment glint override via reflection: " + t.getMessage());
        }
        return false;
    }
}
