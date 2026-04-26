package pl.zekq.shulkerElevator;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.ChatColor;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ElevatorRecipe {

    private final ShulkerElevator plugin;

    public ElevatorRecipe(ShulkerElevator plugin) {
        this.plugin = plugin;
    }

    public void registerRecipes() {
        for (ElevatorType type : plugin.getElevatorTypes()) {
            registerTypeRecipe(type);
        }
    }

    private void registerTypeRecipe(ElevatorType type) {
        ItemStack itemTemplate = createElevatorItem(type, Material.WHITE_WOOL);
        NamespacedKey key = new NamespacedKey(plugin, "elevator_" + type.getId());

        if (Bukkit.getRecipe(key) != null) {
            Bukkit.removeRecipe(key);
        }

        ShapedRecipe recipe = new ShapedRecipe(key, itemTemplate);
        recipe.shape(type.getRecipeShape().toArray(new String[0]));

        for (Map.Entry<String, Object> entry : type.getRecipeIngredients().entrySet()) {
            char symbol = entry.getKey().charAt(0);
            String value = entry.getValue().toString().toUpperCase();

            if (value.startsWith("#")) {
                List<Material> materials = getMaterialsFromTag(value.substring(1));
                if (!materials.isEmpty()) {
                    recipe.setIngredient(symbol, new org.bukkit.inventory.RecipeChoice.MaterialChoice(materials));
                }
            } else {
                Material mat = Material.matchMaterial(value);
                if (mat != null) recipe.setIngredient(symbol, mat);
            }
        }

        Bukkit.addRecipe(recipe);
    }

    public List<Material> getMaterialsFromTag(String tagName) {
        List<Material> materials = new ArrayList<>();
        for (Material mat : Material.values()) {
            String name = mat.name();
            if (name.contains("LEGACY")) continue;
            boolean match = false;
            switch (tagName) {
                case "WOOLS" -> match = name.endsWith("_WOOL");
                case "GLASS" -> match = name.endsWith("_STAINED_GLASS") || name.equals("GLASS") || name.endsWith("_STAINED_GLASS_PANE") || name.equals("GLASS_PANE");
                case "CONCRETE" -> match = name.endsWith("_CONCRETE") || name.endsWith("_CONCRETE_POWDER");
                case "TERRACOTTA" -> match = name.endsWith("_TERRACOTTA") || name.equals("TERRACOTTA");
            }
            if (match) materials.add(mat);
        }
        return materials;
    }

    public ItemStack createElevatorItem(ElevatorType type, Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', type.getDisplayName()));
        List<String> lore = type.getLore().stream()
                .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                .collect(Collectors.toList());
        if (!lore.isEmpty()) meta.setLore(lore);

        if (type.isGlow()) {
            meta.addEnchant(Enchantment.MENDING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
            meta.setEnchantmentGlintOverride(true);
        }

        NamespacedKey elevatorKey = new NamespacedKey(plugin, "is_elevator");
        meta.getPersistentDataContainer().set(elevatorKey, org.bukkit.persistence.PersistentDataType.STRING, type.getId());

        item.setItemMeta(meta);
        return item;
    }
}
