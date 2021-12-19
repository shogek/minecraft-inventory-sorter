# minecraft-inventory-sorter
[Built for Forge 1.17.1]  
[No external dependencies]  

## Features
### Inventory and chest sorting
1. Open the inventory (`E`) or any chest
2. Hover over a slot
3. Click the middle mouse button (`MMB`)

### Broken item and depleted stack replacement
Broke your tool or depleted a stack?  
The mod will search your inventory and place an identical tool or item stack in to the active hand.

## Supported commands
### `boops debug enable`
Enables logging.  
Hold `CTRL` and click the middle mouse button (`MMB`) on...  

1. an item in your inventory  
2. an unknown container's UI  

...to print information about in to the chat.
### `boops debug disable`
You guessed it.  

### `boops sorting alphabetically`
Sorts the items by their names.

### `boops sorting categorically`
Sorts the items by their reversed names.  
Normal: `"block.minecraft.iron_ore"`  
Reversed: `"ero_nori.tfarcenim.kcolb"`  
This groups the items by their categories, in this case, by ores.

## How to build the mod and where to put it
1. Open source folder
1. Open `cmd`
1. Run `gradlew build`
1. Navigate to `project_dir\build\libs`
1. Copy `boop_sorter_mod.jar`
1. Paste the mod in `C:\Users\YOUR_USER\AppData\Roaming\.minecraft\mods`
1. Enjoy!
