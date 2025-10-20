package pl.foxheaven.shulkerElevator;

import org.bukkit.plugin.java.JavaPlugin;

public final class ShulkerElevator extends JavaPlugin {

    private ElevatorRecipe recipeManager;

    @Override
    public void onEnable() {
        getLogger().info("ShulkerElevator enabled");

        saveDefaultConfig();

        recipeManager = new ElevatorRecipe(this);
        recipeManager.registerRecipe();

        getServer().getPluginManager().registerEvents(new ElevatorListener(this), this);

        getLogger().info("ShulkerElevator fully loaded");
    }

    public ElevatorRecipe getRecipeManager() {
        return recipeManager;
    }
}
