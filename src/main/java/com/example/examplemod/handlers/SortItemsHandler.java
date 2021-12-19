package com.example.examplemod.handlers;

import com.example.examplemod.BoopSorterMod;
import com.example.examplemod.messages.SortItemsMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;

public class SortItemsHandler {
    public static void handle(SortItemsMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        var context = contextSupplier.get();
        var sortTarget = message.getSortTarget();
        var shouldSortByCategory = message.shouldSortByCategory();

        context.enqueueWork(() -> {
            var player = context.getSender();
            if (player == null) {
                return;
            }

            switch (sortTarget) {
                case INVENTORY -> mergeAndSortInventoryItemStacks(player, shouldSortByCategory);
                case CONTAINER -> mergeAndSortContainerItemStacks(player, shouldSortByCategory);
                default -> log("Unknown value of enum 'SortTarget'!");
            }
        });

        context.setPacketHandled(true);
    }

    private static void log(String message) {
        BoopSorterMod.LOGGER.info("[Server] " + message);
    }

    private static void mergeAndSortInventoryItemStacks(ServerPlayer player, boolean shouldSortByCategory) {
        var inventory = player.getInventory();
        mergeInventoryItemStacks(inventory);
        sortInventoryItemStacks(inventory, shouldSortByCategory);
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

    private static void sortInventoryItemStacks(Inventory inventory, boolean shouldSortByCategory) {
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

        if (shouldSortByCategory) {
            inventoryItems.sort(SortItemsHandler::compareItemStacksByReversedNamed);
        } else {
            inventoryItems.sort(SortItemsHandler::compareItemStacksByName);
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

    private static void mergeAndSortContainerItemStacks(ServerPlayer player, boolean shouldSortByCategory) {
        var containerMenu = player.containerMenu;
        if (containerMenu == null) {
            log("Attempted to sort the container but it was `null`!");
            return;
        }

        mergeContainerItemStacks(containerMenu);
        sortContainerItemStacks(containerMenu, shouldSortByCategory);
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

    private static void sortContainerItemStacks(AbstractContainerMenu containerMenu, boolean shouldSortByCategory) {
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

        if (shouldSortByCategory) {
            itemCopies.sort(SortItemsHandler::compareItemStacksByReversedNamed);
        } else {
            itemCopies.sort(SortItemsHandler::compareItemStacksByName);
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

    private static int compareItemStacksByName(ItemStack itemStack1, ItemStack itemStack2) {
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

        var comparison = item1Name.compareTo(item2Name);
        if (comparison == 0) {
            // We're comparing item stacks of the same item
            return compareItemStacksByQuantity(itemStack1, itemStack2);
        }

        return comparison;
    }

    /** For example, all ores will be grouped together, because their item names ("iron_ore") end with "ore". */
    private static int compareItemStacksByReversedNamed(ItemStack itemStack1, ItemStack itemStack2) {
        /*
         * id1 = "block.minecraft.iron_ore"
         * id2 = "block.minecraft.gold_ore"
         */
        var id1 = itemStack1.getDescriptionId();
        var id2 = itemStack2.getDescriptionId();
        var id1Reversed = new StringBuilder(id1).reverse().toString();
        var id2Reversed = new StringBuilder(id2).reverse().toString();

        var comparison = id1Reversed.compareTo(id2Reversed);
        if (comparison == 0) {
            // We're comparing item stacks of the same item
            return compareItemStacksByQuantity(itemStack1, itemStack2);
        }

        return comparison;
    }

    /** For example, "64 dirt" will appear before "60 dirt" */
    private static int compareItemStacksByQuantity(ItemStack itemStack1, ItemStack itemStack2) {
        var itemCount1 = itemStack1.getCount();
        var itemCount2 = itemStack2.getCount();
        return (itemCount1 - itemCount2) * -1;
    }
}
