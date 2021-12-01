package com.example.examplemod;

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
                case INVENTORY -> mergeAndSortInventoryItemStacks(player.getInventory());
                case CONTAINER -> mergeAndSortContainerItemStacks(player.containerMenu);
                default -> log("Unknown value of enum 'SortTarget'!");
            }
        });
        context.setPacketHandled(true);
    }

    private static void log(String message) {
        BoopSorterMod.LOGGER.info("[Server] " + message);
    }

    // TODO: Explain me
    private static void mergeInventoryItemStacks(Inventory inventory) {
        var airItemId = Items.AIR.getDescriptionId();
        var itemsInInventory = new HashMap<String, List<ItemStack>>();

        for (var i = InventoryMenu.INV_SLOT_START; i < InventoryMenu.INV_SLOT_END; i++) {
            var itemStack = inventory.getItem(i);
            var itemId = itemStack.getDescriptionId();

            if (itemId.equals(airItemId)) {
                // Ignore empty slots
                continue;
            }

            if (!itemStack.isStackable()) {
                continue;
            }

            if (itemStack.getCount() == itemStack.getMaxStackSize()) {
                // Ignore maxed out stacks
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

        // Ignore single stacks
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

    // TODO: Explain me
    private static void sortInventoryItemStacks(Inventory inventory) {
        var airItemId = Items.AIR.getDescriptionId();
        var inventoryItems = new ArrayList<ItemStack>();

        // TODO: What does this loop do?
        for (var i = InventoryMenu.INV_SLOT_START; i < InventoryMenu.INV_SLOT_END; i++) {
            var itemStack = inventory.getItem(i);
            var itemStackId = itemStack.getDescriptionId();
            if (itemStackId.equals(airItemId)) {
                continue;
            }

            inventoryItems.add(itemStack);
        }

        // TODO: Add an example
        inventoryItems.sort((itemStack1, itemStack2) -> {
            // TODO: Simplify
            var item1Name = "";
            var item1 = itemStack1.getItem();
            if (item1.getRegistryName() == null) {
                item1Name = item1.getDescriptionId();
            } else {
                item1Name = item1.getRegistryName().getPath();
            }

            var item2Name = "";
            var item2 = itemStack2.getItem();
            if (item2.getRegistryName() == null) {
                item2Name = item2.getDescriptionId();
            } else {
                item2Name = item2.getRegistryName().getPath();
            }

            return item1Name.compareTo(item2Name);
        });

        // TODO: What does this loop do?
        var slotIndex = InventoryMenu.INV_SLOT_START;
        for (ItemStack inventoryItem : inventoryItems) {
            var inventoryItemCopy = inventoryItem.copy();
            inventory.setItem(slotIndex, inventoryItemCopy);
            inventory.removeItem(inventoryItem);
            slotIndex++;
        }
    }

    private static void mergeAndSortInventoryItemStacks(Inventory inventory) {
        if (inventory == null) {
            log("Attempted to sort the inventory but it was `null`!");
            return;
        }

        mergeInventoryItemStacks(inventory);
        sortInventoryItemStacks(inventory);
    }

    private static void mergeAndSortContainerItemStacks(AbstractContainerMenu containerMenu) {
        if (containerMenu == null) {
            log("Attempted to sort the container but it was `null`!");
            return;
        }

        mergeContainerItemStacks(containerMenu);
        sortContainerItemStacks(containerMenu);
    }

    private static void mergeContainerItemStacks(AbstractContainerMenu containerMenu) {
        // TODO: Duplicated - move to parent function?
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

            if (itemId.equals(airItemId)) {
                // Ignore empty slots
                continue;
            }

            if (!itemStack.isStackable()) {
                continue;
            }

            if (itemStack.getCount() == itemStack.getMaxStackSize()) {
                // Ignore maxed out stacks
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

        // Ignore single stacks
        // TODO: Will this work? Or should I change to `.getSlot().getCount()`?
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

                // TODO: Is this ItemStack removal method not leaving any dangling pointers?
                // TODO: Check if inventory is really empty
                // TODO: Does `hasItem()` return `true`?
                itemStack.setCount(0);
            }
        });
    }

    private static void sortContainerItemStacks(AbstractContainerMenu containerMenu) {
        // TODO: Duplicated - move to parent function?
        var containerSlots = containerMenu.slots;
        var containerIndexStart = 0;
        var containerIndexEnd = containerSlots.size() - Inventory.INVENTORY_SIZE;

        var airItemId = Items.AIR.getDescriptionId();
        var itemCopies = new ArrayList<ItemStack>();

        for (var i = containerIndexStart; i < containerIndexEnd; i++) {
            var slot = containerSlots.get(i);
            var itemStack = slot.getItem();
            var itemId = itemStack.getDescriptionId();
            if (itemId.equals(airItemId)) {
                continue;
            }

            itemCopies.add(itemStack.copy());
        }

        if (itemCopies.size() < 1) {
            return;
        }

        // TODO: Add an example
        itemCopies.sort((itemStack1, itemStack2) -> {
            // TODO: Simplify
            var item1Name = "";
            var item1 = itemStack1.getItem();
            if (item1.getRegistryName() == null) {
                item1Name = item1.getDescriptionId();
            } else {
                item1Name = item1.getRegistryName().getPath();
            }

            var item2Name = "";
            var item2 = itemStack2.getItem();
            if (item2.getRegistryName() == null) {
                item2Name = item2.getDescriptionId();
            } else {
                item2Name = item2.getRegistryName().getPath();
            }

            return item1Name.compareTo(item2Name);
        });

        var index = containerIndexStart;
        for (ItemStack itemCopy: itemCopies) {
            var slot = containerSlots.get(index);
            slot.getItem().setCount(0);
            slot.set(itemCopy);
            index++;
        }

        for (var i = containerIndexStart + itemCopies.size(); i < containerIndexEnd; i++) {
            var slot = containerSlots.get(i);
            slot.getItem().setCount(0);
        }
    }
}
