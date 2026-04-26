package pl.zekq.shulkerElevator;

import org.bukkit.configuration.file.FileConfiguration;
import java.util.List;
import java.util.Map;

public class ElevatorType {
    private final String id;
    private final String materialInput;
    private final int maxDistance;
    private final boolean glow;
    private final boolean passThrough;
    private final boolean autoUp;
    private final String permissionUse;
    private final String permissionCraft;
    private final String displayName;
    private final List<String> lore;
    private final List<String> recipeShape;
    private final Map<String, Object> recipeIngredients;

    public ElevatorType(String id, FileConfiguration config) {
        this.id = id;
        this.materialInput = config.getString("material", "WHITE_WOOL").toUpperCase();
        this.maxDistance = config.getInt("max-distance", 32);
        this.glow = config.getBoolean("glow", true);
        this.passThrough = config.getBoolean("pass-through", true);
        this.autoUp = config.getBoolean("auto-up", true);
        
        String pUse = config.getString("permission_use", "");
        this.permissionUse = pUse.isEmpty() ? "" : "shulkerelevator." + pUse;
        
        String pCraft = config.getString("permission_craft", "");
        this.permissionCraft = pCraft.isEmpty() ? "" : "shulkerelevator." + pCraft;
        
        this.displayName = config.getString("display-name", "&6&lElevator");
        this.lore = config.getStringList("lore");
        this.recipeShape = config.getStringList("recipe.shape");
        this.recipeIngredients = config.getConfigurationSection("recipe.ingredients").getValues(false);
    }

    public String getId() { return id; }
    public String getMaterialInput() { return materialInput; }
    public int getMaxDistance() { return maxDistance; }
    public boolean isGlow() { return glow; }
    public boolean canPassThrough() { return passThrough; }
    public boolean isAutoUp() { return autoUp; }
    public String getPermissionUse() { return permissionUse; }
    public String getPermissionCraft() { return permissionCraft; }
    public String getDisplayName() { return displayName; }
    public List<String> getLore() { return lore; }
    public List<String> getRecipeShape() { return recipeShape; }
    public Map<String, Object> getRecipeIngredients() { return recipeIngredients; }
}
