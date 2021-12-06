package com.example.examplemod;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import java.util.*;
import java.util.function.Supplier;

public class Server {
    public static void onMessage(Message message, Supplier<NetworkEvent.Context> contextSupplier) {
        var context = contextSupplier.get();
        var sortTarget = message.getSortTarget();
        log("User requested to sort: " + sortTarget);

        context.enqueueWork(() -> {
            var player = context.getSender();
            if (player == null) {
                return;
            }

            switch (sortTarget) {
                case INVENTORY -> mergeAndSortInventoryItemStacks(player);
                case CONTAINER -> mergeAndSortContainerItemStacks(player);
                default -> log("Unknown value of enum 'SortTarget'!");
            }
        });
        context.setPacketHandled(true);
    }

    private static void log(String message) {
        BoopSorterMod.LOGGER.info("[Server] " + message);
    }

    private static boolean isBoops(ServerPlayer player) {
        var playerName = player.getName().getContents().toLowerCase();
        return playerName.startsWith("xonism");
    }

    private static void mergeAndSortInventoryItemStacks(ServerPlayer player) {
        var inventory = player.getInventory();
        mergeInventoryItemStacks(inventory);

        var shouldSortByItemName = isBoops(player);
        sortInventoryItemStacks(inventory, shouldSortByItemName);
    }

    private static void mergeInventoryItemStacks(Inventory inventory) {
        var airItemId = Items.AIR.getDescriptionId();
        var itemsInInventory = new HashMap<String, List<ItemStack>>();

        for (var i = InventoryMenu.INV_SLOT_START; i < InventoryMenu.INV_SLOT_END; i++) {
            var itemStack = inventory.getItem(i);
            var itemId = itemStack.getDescriptionId();

            // Ignore empty slots
            if (itemId.equals(airItemId)) {
                continue;
            }

            if (!itemStack.isStackable()) {
                continue;
            }

            if (itemStack.getCount() == itemStack.getMaxStackSize()) {
                continue;
            }

            if (itemsInInventory.containsKey(itemId)) {
                var itemStacks = itemsInInventory.get(itemId);
                itemStacks.add(itemStack);
                itemsInInventory.put(itemId, itemStacks);
                continue;
            }

            var itemStacks = new ArrayList<ItemStack>();
            itemStacks.add(itemStack);
            itemsInInventory.put(itemId, itemStacks);
        }

        // Ignore single stacks because nothing to merge with
        itemsInInventory.entrySet().removeIf((entry) -> entry.getValue().size() <= 1);

        itemsInInventory.forEach((itemId, itemStacks) -> {
            int totalItemCount = itemStacks.stream().map(ItemStack::getCount).reduce(Integer::sum).orElse(0);

            for (ItemStack itemStack: itemStacks) {
                var maxSize = itemStack.getMaxStackSize();
                if (totalItemCount >= maxSize) {
                    itemStack.setCount(maxSize);
                    totalItemCount -= maxSize;
                    continue;
                }

                if (totalItemCount > 0) {
                    itemStack.setCount(totalItemCount);
                    totalItemCount = 0;
                    continue;
                }

                inventory.removeItem(itemStack);
            }
        });
    }

    private static void sortInventoryItemStacks(Inventory inventory, boolean sortByName) {
        var airItemId = Items.AIR.getDescriptionId();
        var inventoryItems = new ArrayList<ItemStack>();

        // Collect items from slots that are not empty
        for (var i = InventoryMenu.INV_SLOT_START; i < InventoryMenu.INV_SLOT_END; i++) {
            var itemStack = inventory.getItem(i);
            var itemStackId = itemStack.getDescriptionId();
            if (itemStackId.equals(airItemId)) {
                continue;
            }

            inventoryItems.add(itemStack);
        }

        if (sortByName) {
            inventoryItems.sort(Server::sortItemStacksByName);
        } else {
            inventoryItems.sort(Server::sortItemStacksByGroup);
        }

        // Start from the inventory's beginning and move over items from their respective slots
        var slotIndex = InventoryMenu.INV_SLOT_START;
        for (ItemStack inventoryItem : inventoryItems) {
            var inventoryItemCopy = inventoryItem.copy();
            inventory.setItem(slotIndex, inventoryItemCopy);
            inventory.removeItem(inventoryItem);
            slotIndex++;
        }
    }

    private static void mergeAndSortContainerItemStacks(ServerPlayer player) {
        var containerMenu = player.containerMenu;
        if (containerMenu == null) {
            log("Attempted to sort the container but it was `null`!");
            return;
        }

        mergeContainerItemStacks(containerMenu);

        var shouldSortByItemName = isBoops(player);
        sortContainerItemStacks(containerMenu, shouldSortByItemName);
    }

    private static void mergeContainerItemStacks(AbstractContainerMenu containerMenu) {
        var containerSlots = containerMenu.slots;
        var containerIndexStart = 0;
        var containerIndexEnd = containerSlots.size() - Inventory.INVENTORY_SIZE;

        // [KEY] - Item ID  [VAL] - All slots which have that item
        var slotsInContainer = new HashMap<String, List<Slot>>();
        var airItemId = Items.AIR.getDescriptionId();

        for (var i = containerIndexStart; i < containerIndexEnd; i++) {
            var slot = containerSlots.get(i);
            var itemStack = slot.getItem();
            var itemId = itemStack.getDescriptionId();

            // Ignore empty slots
            if (itemId.equals(airItemId)) {
                continue;
            }

            if (!itemStack.isStackable()) {
                continue;
            }

            if (itemStack.getCount() == itemStack.getMaxStackSize()) {
                continue;
            }

            if (slotsInContainer.containsKey(itemId)) {
                var slotsWithItem = slotsInContainer.get(itemId);
                slotsWithItem.add(slot);
                slotsInContainer.put(itemId, slotsWithItem);
                continue;
            }

            var slotsWithItem = new ArrayList<Slot>();
            slotsWithItem.add(slot);
            slotsInContainer.put(itemId, slotsWithItem);
        }

        // Ignore single stacks because nothing to merge with
        slotsInContainer.entrySet().removeIf((entry) -> entry.getValue().size() <= 1);

        slotsInContainer.forEach((itemId, slotsWithItem) -> {
            int totalItemCount = slotsWithItem.stream().map(slot -> slot.getItem().getCount()).reduce(Integer::sum).orElse(0);

            for (Slot slot: slotsWithItem) {
                var itemStack = slot.getItem();
                var maxSize = itemStack.getMaxStackSize();
                if (totalItemCount >= maxSize) {
                    itemStack.setCount(maxSize);
                    totalItemCount -= maxSize;
                    continue;
                }

                if (totalItemCount > 0) {
                    itemStack.setCount(totalItemCount);
                    totalItemCount = 0;
                    continue;
                }

                itemStack.setCount(0);
            }
        });
    }

    private static void sortContainerItemStacks(AbstractContainerMenu containerMenu, boolean sortByName) {
        var containerSlots = containerMenu.slots;
        var containerIndexStart = 0;
        var containerIndexEnd = containerSlots.size() - Inventory.INVENTORY_SIZE;

        var airItemId = Items.AIR.getDescriptionId();
        var itemCopies = new ArrayList<ItemStack>();

        // Collect all items from not empty container slots
        for (var i = containerIndexStart; i < containerIndexEnd; i++) {
            var slot = containerSlots.get(i);
            var itemStack = slot.getItem();
            var itemId = itemStack.getDescriptionId();
            if (itemId.equals(airItemId)) {
                continue;
            }

            itemCopies.add(itemStack.copy());
        }

        // Container is empty - nothing to sort
        if (itemCopies.size() < 1) {
            return;
        }

        var containerItemsCount = itemCopies.size();

        if (sortByName) {
            itemCopies.sort(Server::sortItemStacksByName);
        } else {
            itemCopies.sort(Server::sortItemStacksByGroup);
        }

        var index = containerIndexStart;
        for (ItemStack itemCopy: itemCopies) {
            var slot = containerSlots.get(index);
            // Delete item in the slot
            slot.getItem().setCount(0);
            // Place another item in its place
            slot.set(itemCopy);
            index++;
        }

        // Clear the rest of the container since all the items have been moved to its start
        for (var i = containerIndexStart + containerItemsCount; i < containerIndexEnd; i++) {
            var slot = containerSlots.get(i);
            slot.getItem().setCount(0);
        }
    }

    private static int sortItemStacksByName(ItemStack itemStack1, ItemStack itemStack2) {
        /*
         * getDescriptionId() = "block.minecraft.iron_ore"
         * getRegistryName.getPath() = "iron_ore"
        */
        var item1 = itemStack1.getItem();
        var item1Name = item1.getRegistryName() == null
            ? item1.getDescriptionId()
            : item1.getRegistryName().getPath();

        var item2 = itemStack2.getItem();
        var item2Name = item2.getRegistryName() == null
            ? item2.getDescriptionId()
            : item2.getRegistryName().getPath();

        return item1Name.compareTo(item2Name);
    }

    /** For example, all ores will be grouped together, because their item names ("iron_ore") end with "ore". */
    private static int sortItemStacksByGroup(ItemStack itemStack1, ItemStack itemStack2) {
        /*
         * id1 = "block.minecraft.iron_ore"
         * id2 = "block.minecraft.gold_ore"
         */
        var id1 = itemStack1.getDescriptionId();
        var id2 = itemStack2.getDescriptionId();
        var id1Reversed = new StringBuilder(id1).reverse().toString();
        var id2Reversed = new StringBuilder(id2).reverse().toString();
        return id1Reversed.compareTo(id2Reversed);
    }
}
