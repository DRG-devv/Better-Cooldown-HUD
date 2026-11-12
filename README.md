# Better Cooldown HUD

A Fabric mod that replaces your crosshair with one you paint yourself, and shows your attack cooldown by filling it from the centre outward.

## Features

- Paint your own crosshair on an 11 x 11 grid, in Mod Menu
- The painted crosshair replaces the default plus sign
- The attack cooldown spreads from the centre cell to every side at the same time
- Choose the crosshair colour and the cooldown colour
- Choose a crosshair size, from default up to 4 px per cell
- Settings save to a config file, so they persist between sessions
- Client-side only, so no server install is needed

## Requirements

- Minecraft Java Edition 1.21.11
- [Fabric Loader](https://fabricmc.net/)
- [Fabric API](https://modrinth.com/mod/fabric-api)
- [Mod Menu](https://modrinth.com/mod/modmenu) (optional, for the editor screen)

## Installation

1. Download the jar from the [latest release](../../releases/latest) or from Modrinth.
2. Place it in your `.minecraft/mods` folder, along with Fabric API.
3. Launch the game with Fabric 1.21.11.

## Using the editor

1. Open **Mod Menu**, find **Better Cooldown HUD**, and open its settings.
2. The grid shows your crosshair. The grey outline marks the centre cell, where the cooldown starts.
3. **Left-click** an empty cell to paint it.
4. **Right-click** a cell to erase it. You can't erase the last remaining cell.
5. Use **Crosshair** and **Cooldown** to cycle through the colours.
6. Use **Size** to change how large each painted cell is on screen.
7. Use **Reset to default** to bring back the plus sign.
8. Press **Done** to save.

## How the cooldown works

The cooldown spreads outward from the centre cell. Cells the same distance from the centre fill together, so every arm and outline recharges at the same time. Painted cells use the crosshair colour when ready and the cooldown colour while recharging.

## Config file

Settings are saved to `.minecraft/config/bettercooldownhud.properties`:

- `shape`: the painted cell numbers
- `crosshair_color` and `cooldown_color`: colours as ARGB hex, for example `FF3366FF`
- `crosshair_size`: `0` for default, up to `4`

## Known limitations

- The cooldown always spreads from the centre cell. You can't choose another start point.
- The hotbar's attack indicator is not changed by this mod.

## Building from source

This repo builds with GitHub Actions. Each push runs the Build workflow, and the jar is available under **Actions > Build > Artifacts**.

To build locally, run `./gradlew build` (on Windows, `gradlew.bat build`). The jar appears in `build/libs/`.

## License

MIT
