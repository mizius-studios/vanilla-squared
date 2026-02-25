# Vanilla² ⚔️🛡️

Vanilla² is a Fabric combat overhaul for Minecraft 1.21.11.  
It rebalances weapons, armor, and combat interactions while staying close to the vanilla feel.

## What this mod changes 🎮

### Core combat rebalance ⚔️

- Reworks sword, axe, and trident combat stats such as attack damage, attack speed, and interaction tuning
- Expands armor scaling so high armor values keep working past vanilla limits
- Updates combat math to support higher armor and magic absorb values
- Adds a custom mace protection attribute that reduces mace smash damage

### Dual wield system 🤺

- Adds a dual wield data component and matching logic for compatible weapon pairs
- Merges enchantments from offhand into mainhand during attacks with blocked enchant safeguards
- Grants offhand powered bonus damage on strong hits
- Supports charge based critical bonus behavior triggered through offhand use
- Plays sweep and crit combat feedback for the dual wield flow

### Item behavior updates 🧰

- Adjusts tool and weapon durability values by material
- Applies armor durability updates through armor material handling
- Gives shields block attack component behavior with tuned values
- Changes potion stack sizes
- Adjusts fishing rod durability and adds offensive hook interactions with enchant integration

### Client side combat quality of life ✨

- Lets sword targeting pass through grass and flowers so nearby entities are easier to hit
- Improves offhand interaction priority for fishing rod and shield related combat cases
- Extends armor HUD rendering to display armor values above 20 with extra rows

## Tech stack and requirements 🧱

- Minecraft 1.21.11
- Fabric Loader 0.18.4 or newer
- Fabric API 0.141.2 plus 1.21.11
- Java 21
- Gradle via wrapper scripts included in the repo

## Build guide 🔨

### Quick build

```bash
./gradlew build
```

Build outputs appear in `build/libs`.

### Run in dev client

```bash
./gradlew runClient
```

If you use the helper scripts, update the destination folder inside each script to match your local launcher profile.

## Project layout 🗂️

- `src/main/java` shared gameplay logic and server side mixins
- `src/client/java` client side mixins and combat targeting quality of life logic
- `src/main/resources` Fabric metadata and main mixin config
- `src/client/resources` client mixin config

## Notes for contributors 🧪

- Java release target is 21
- Main mod id is `vanilla-squared`
- Main gameplay hooks are delivered through mixins and Fabric events

Have fun testing and tuning combat ⚡
