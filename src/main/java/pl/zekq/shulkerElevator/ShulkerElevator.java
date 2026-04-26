package pl.zekq.shulkerElevator;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.ChatColor;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.stream.Collectors;

public final class ShulkerElevator extends JavaPlugin implements TabCompleter {

    private ElevatorRecipe recipeManager;
    private final List<ElevatorType> elevatorTypes = new ArrayList<>();
    private final List<String> failedLoads = new ArrayList<>();
    private FileConfiguration messages;

    @Override
    public void onEnable() {
        saveResource("messages.yml", false);
        reloadMessages();
        loadElevatorTypes();

        recipeManager = new ElevatorRecipe(this);
        recipeManager.registerRecipes();

        getServer().getPluginManager().registerEvents(new ElevatorListener(this), this);
        getCommand("elevator").setTabCompleter(this);
    }

    public void reloadMessages() {
        File file = new File(getDataFolder(), "messages.yml");
        messages = YamlConfiguration.loadConfiguration(file);
    }

    public String getMessage(String path) {
        String msg = messages.getString(path, path);
        String prefix = messages.getString("prefix", "");
        return ChatColor.translateAlternateColorCodes('&', prefix + msg);
    }

    private void loadElevatorTypes() {
        elevatorTypes.clear();
        failedLoads.clear();
        File blocksFolder = new File(getDataFolder(), "blocks");
        
        if (!blocksFolder.exists()) {
            blocksFolder.mkdirs();
            saveResource("blocks/wool_elevator.yml", false);
            saveResource("blocks/glass_elevator.yml", false);
            saveResource("blocks/concrete_elevator.yml", false);
        }

        File[] files = blocksFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files != null) {
            for (File file : files) {
                try {
                    FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                    String id = file.getName().replace(".yml", "");
                    elevatorTypes.add(new ElevatorType(id, config));
                } catch (Exception e) {
                    failedLoads.add(file.getName());
                    getLogger().severe("Failed to load elevator block: " + file.getName());
                }
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "ShulkerElevator " + ChatColor.GRAY + "v" + getDescription().getVersion());
            sender.sendMessage(ChatColor.YELLOW + "/elevator list " + ChatColor.GRAY + "- All profiles");
            sender.sendMessage(ChatColor.YELLOW + "/elevator report " + ChatColor.GRAY + "- GitHub Issues");
            sender.sendMessage(ChatColor.YELLOW + "/elevator reload " + ChatColor.GRAY + "- Reload configs");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "report":
                sender.sendMessage(getMessage("report-note"));
                sender.sendMessage(ChatColor.AQUA + "" + ChatColor.UNDERLINE + "https://github.com/ZekqKeku/ShulkerElevator/issues");
                return true;

            case "reload":
                if (!sender.hasPermission("shulkerelevator.admin")) {
                    sender.sendMessage(getMessage("no-permission"));
                    return true;
                }
                reloadConfig();
                reloadMessages();
                loadElevatorTypes();
                recipeManager.registerRecipes();
                sender.sendMessage(getMessage("plugin-reloaded"));
                return true;

            case "list":
                if (!sender.hasPermission("shulkerelevator.admin")) {
                    sender.sendMessage(getMessage("no-permission"));
                    return true;
                }
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("list-header")));
                for (ElevatorType type : elevatorTypes) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("list-format-success")
                        .replace("%id%", type.getId())
                        .replace("%mat%", type.getMaterialInput())
                        .replace("%dist%", String.valueOf(type.getMaxDistance()))));
                }
                for (String failed : failedLoads) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("list-format-error").replace("%id%", failed)));
                }
                return true;

            case "debug":
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("Console cannot use debug.");
                    return true;
                }
                if (!player.hasPermission("shulkerelevator.admin")) {
                    player.sendMessage(getMessage("no-permission"));
                    return true;
                }
                ItemStack item = player.getInventory().getItemInMainHand();
                if (item.getType().isAir()) {
                    player.sendMessage(ChatColor.RED + "Hold an item!");
                    return true;
                }
                ItemMeta meta = item.getItemMeta();
                player.sendMessage(ChatColor.GOLD + "--- DEBUG DUMP (Check Console) ---");
                getLogger().info("=== ITEM DEBUG DUMP ===");
                getLogger().info("Material: " + item.getType());
                if (meta != null) {
                    getLogger().info("Display Name: " + meta.getDisplayName());
                    getLogger().info("Full Meta String: " + meta.toString());
                }
                return true;
        }

        sender.sendMessage(getMessage("command-not-found"));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            completions.add("list");
            completions.add("report");
            if (sender.hasPermission("shulkerelevator.admin")) {
                completions.add("reload");
            }
            return completions.stream()
                .filter(s -> s.startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    public List<ElevatorType> getElevatorTypes() { return elevatorTypes; }
    public ElevatorRecipe getRecipeManager() { return recipeManager; }
}
