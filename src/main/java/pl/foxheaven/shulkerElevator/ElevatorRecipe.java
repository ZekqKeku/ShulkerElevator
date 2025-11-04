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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ElevatorRecipe {

    private final JavaPlugin plugin;
    private final String KEY_NAME = "shulker_elevator";

    public ElevatorRecipe(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void registerRecipe() {
        FileConfiguration config = plugin.getConfig();

        Material resultMaterial = Material.matchMaterial(config.getString("elevator-block", "PURPLE_SHULKER_BOX"));
        if (resultMaterial == null) resultMaterial = Material.PURPLE_SHULKER_BOX;

        ItemStack elevator = new ItemStack(resultMaterial);
        ItemMeta meta = elevator.getItemMeta();

        if (meta == null) {
            plugin.getLogger().severe("Nie można pobrać ItemMeta dla materiału: " + resultMaterial);
            return;
        }

        String displayName = config.getString("block-name", "&6&lElevator");
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));

        List<String> lore = config.getStringList("block-lore").stream()
                .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                .collect(Collectors.toList());
        if (!lore.isEmpty()) meta.setLore(lore);

        boolean glowRequested = config.getBoolean("glow", true);
        plugin.getLogger().info("Checking glow setting... Value read from config: " + glowRequested);

        if (glowRequested) {
            try {
                meta.addEnchant(Enchantment.UNBREAKING, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                plugin.getLogger().info("Successfully applied glow (enchant + hide flags).");
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to apply  glow method: " + e.getMessage());
            }
        }

        elevator.setItemMeta(meta);

        NamespacedKey correctKey = new NamespacedKey(plugin, KEY_NAME);

        if (Bukkit.getRecipe(correctKey) != null) {
            Bukkit.removeRecipe(correctKey);
            plugin.getLogger().info("Removed existing recipe with correct key (overwriting): " + correctKey);
        }

        ShapedRecipe recipe = new ShapedRecipe(correctKey, elevator);
        recipe.shape(config.getStringList("recipe.shape").toArray(new String[0]));

        Map<String, Object> ingredients = config.getConfigurationSection("recipe.ingredients").getValues(false);
        for (Map.Entry<String, Object> entry : ingredients.entrySet()) {
            char symbol = entry.getKey().charAt(0);
            Material mat = Material.matchMaterial(entry.getValue().toString());
            if (mat != null) recipe.setIngredient(symbol, mat);
        }

        Bukkit.addRecipe(recipe);

        plugin.getLogger().info("Elevator recipe registration finished with FINAL key: " + correctKey);
    }
}