# Mod Template

A reusable starting point for Minecraft Java Fabric mods.

## What's included

- Gradle build setup (`build.gradle`, `settings.gradle`, `gradle.properties`)
- GitHub Actions workflow that builds the jar automatically (`.github/workflows/build.yml`)
- Mod Menu entry point and settings screen structure
- Mixin config and folder layout
- Icon folder for the Mod Menu icon

## How to use

1. Click **Use this template** on this repo, or copy the files.
2. Rename `modtemplate` to your mod's id in the package folders, file names, and contents.
3. Set the versions in `gradle.properties` (see below).
4. Update `fabric.mod.json` with your mod's details.
5. Add your code under `src/main/java`.
6. Commit your changes. The Build workflow will build the jar.
7. Download the `.jar` from the **Actions** tab, under **Artifacts**.

## Versions

Set these in `gradle.properties` for each mod. Check the values on fabricmc.net/develop for your target version:

- `minecraft_version`: the Minecraft version
- `yarn_mappings`: the matching Yarn build
- `loader_version`: the Fabric Loader version
- `fabric_version`: the Fabric API version for that Minecraft version

Also check these for your version:

- **Java version:** newer Minecraft versions may need a different Java.
- **Loom plugin version** in `build.gradle`, and the Gradle version in `build.yml`, must support your Minecraft version.

## Requirements

- Fabric Loader and Fabric API
- Mod Menu (optional, for the settings screen)

## License

MIT
