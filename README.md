# ShulkerElevator

[![License: CC BY-NC 4.0](https://img.shields.io/badge/License-CC%20BY--NC%204.0-lightgrey.svg)](https://creativecommons.org/licenses/by-nc/4.0/)

A modern, highly customizable elevator plugin for Minecraft servers. Create elevators out of various blocks like Wool, Glass, Concrete, or Terracotta with dynamic color support and advanced configuration options.

## Features

*   **Dynamic Profiles**: Create unlimited elevator types in the `blocks/` folder.
*   **Color Support**: Automatically detects the dominant color of ingredients during crafting to produce a matching elevator block.
*   **Obstruction Checks**: Toggle whether elevators can teleport players through solid blocks (`pass-through`).
*   **Smart Placement**: Automatically sets blocks to face UP when placed (optional per profile).
*   **Glint Effect**: Modern 1.21+ enchantment glint support (on supported blocks).
*   **Customizable Messages**: Fully translatable `messages.yml` with prefix support.

## Commands

| Command | Description | Permission |
|:--- |:--- |:--- |
| `/elevator` | Shows plugin version and basic help. | None |
| `/elevator list` | Lists all successfully loaded and failed elevator profiles. | `shulkerelevator.admin` |
| `/elevator report` | Provides a link to report bugs or suggest new features. | None |
| `/elevator reload` | Reloads all profiles and message configurations. | `shulkerelevator.admin` |

## Permissions

### Administrative
*   `shulkerelevator.admin`: Grants access to `list`, `reload`, and developer debug tools.

### Elevator Usage
Permissions are defined per profile in their respective `.yml` files. The plugin automatically prepends `shulkerelevator.` to the configured strings.

*   `shulkerelevator.<permission_use>`: Allows a player to teleport using that elevator type.
*   `shulkerelevator.<permission_craft>`: Allows a player to craft that elevator type.

*Default examples:*
*   `shulkerelevator.wool.use` / `shulkerelevator.wool.craft`
*   `shulkerelevator.glass.use` / `shulkerelevator.glass.craft`
*   `shulkerelevator.concrete.use` / `shulkerelevator.concrete.craft`

## Configuration

### Elevator Profiles (`/plugins/ShulkerElevator/blocks/`)
Each file in this folder represents a unique elevator type. Key settings include:
*   `material`: The base block (supports tags like `#WOOLS`, `#GLASS`, `#CONCRETE`).
*   `max-distance`: How many blocks the elevator can "jump".
*   `pass-through`: If `false`, the path between elevators must be clear of solid blocks.
*   `auto-up`: Forces the block to face UP when placed.
*   `recipe`: Fully customizable 3x3 crafting shape and ingredients.

#### Example: Wool Elevator (`wool_elevator.yml`)
```yaml
material: "#WOOLS"
max-distance: 16
glow: true
pass-through: true
auto-up: true
permission_use: "wool.use"
permission_craft: "wool.craft"
display-name: "&f&lWoolen Elevator"
lore:
  - "&7Standard range elevator"
  - "&7Max distance: &b16"
recipe:
  shape: ["WWW", "WEW", "WWW"]
  ingredients:
    W: "#WOOLS"
    E: ENDER_PEARL
```

#### Example: Glass Elevator (`glass_elevator.yml`)
```yaml
material: "#GLASS"
max-distance: 8
glow: true
pass-through: true
auto-up: true
permission_use: "glass.use"
permission_craft: "glass.craft"
display-name: "&b&lGlass Elevator"
lore:
  - "&7Short range transparent elevator"
  - "&7Max distance: &b8"
recipe:
  shape: ["GGG", "GEG", "GGG"]
  ingredients:
    G: "#GLASS"
    E: ENDER_PEARL
```

#### Example: Concrete Elevator (`concrete_elevator.yml`)
```yaml
material: "#CONCRETE"
max-distance: 32
glow: true
pass-through: false
auto-up: true
permission_use: "concrete.use"
permission_craft: "concrete.craft"
display-name: "&8&lConcrete Elevator"
lore:
  - "&7Long range heavy elevator"
  - "&7Max distance: &b32"
recipe:
  shape: ["CCC", "CEC", "CCC"]
  ingredients:
    C: "#CONCRETE"
    E: ENDER_PEARL
```

### Messages (`/plugins/ShulkerElevator/messages.yml`)
Translate every player-facing string and customize the plugin prefix. Supports color codes and placeholders like `%direction%`.

## Bug Reports & Feature Requests
Please report any issues or suggest improvements on our GitHub repository:
[https://github.com/ZekqKeku/ShulkerElevator/issues](https://github.com/ZekqKeku/ShulkerElevator/issues)

---

## License

[![License: CC BY-NC 4.0](https://img.shields.io/badge/License-CC%20BY--NC%204.0-lightgrey.svg)](https://creativecommons.org/licenses/by-nc/4.0/)

ShulkerElevator is the intellectual property of zekq.

Under the **CC BY-NC 4.0** license, you are free to:
*   **Share** — copy and redistribute the material in any medium or format
*   **Adapt** — remix, transform, and build upon the material

Under the following terms:
*   **Attribution** — You must give appropriate credit (mention **zekq** as the original author), provide a link to the license, and indicate if changes were made. You may do so in any reasonable manner, but not in any way that suggests the licensor endorses you or your use.
*   **NonCommercial** — You may not use the material for commercial purposes. Only the original author (**zekq**) reserves the right to monetize this work.

For more details, see the [LICENSE](LICENSE) file
