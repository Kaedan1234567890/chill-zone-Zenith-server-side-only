# Chill Zone Zenith - Activation Core v0.1.0-alpha

This is the first coding foundation for the Zenith progression mod.

## Branches
- Ender
- Ravager
- Guardian
- Warden
- Wither
- Zenith

Every branch begins **disabled** in a new world.

## Admin commands

```text
/zenith status

/zenith activate ender
/zenith deactivate ender

/zenith activate ravager
/zenith deactivate ravager

/zenith activate guardian
/zenith deactivate guardian

/zenith activate warden
/zenith deactivate warden

/zenith activate wither
/zenith deactivate wither

/zenith activate zenith
/zenith deactivate zenith

/zenith activateall
/zenith deactivateall
```

The category argument has autocomplete.

Commands currently require vanilla permission level 2.
A LuckPerms permission node can be added in the next pass.

## What activation will control

The single `ZenithGate` class is intentionally used as the master switch.

When a branch is OFF:
- its custom mob/boss drops should not drop;
- its custom crafting station should reject that branch's recipes;
- its sword abilities can be disabled too, so deactivation really shuts the branch down.

When a branch is ON:
- branch drops are eligible;
- branch recipes can work in that branch's custom crafting table;
- branch sword abilities can work.

The Zenith category is independent from the five boss branches.

## Persistence

Activation state is saved to the Minecraft world using `SavedData`,
so it survives server restarts.

## Next coding pass

1. Register custom items and load the approved PNG textures.
2. Register the six crafting-station blocks.
3. Add restricted custom crafting menus/recipes.
4. Add branch-gated custom drops.
5. Add sword abilities/cooldowns.
6. Add LuckPerms permission node and polished messages.


## GitHub build

This package includes `.github/workflows/build.yml`. After uploading the extracted contents to GitHub and committing them, the **Build Chill Zone Zenith** Action starts automatically. The compiled server mod is uploaded as the `chill-zone-zenith-mod` artifact.


## Fix 3
Updated command permissions and persistent SavedData code for the Minecraft 26.2 APIs.
