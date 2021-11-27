package com.example.examplemod;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import java.util.*;
import java.util.function.Supplier;

public class Server {
    public static void onMessage(Message message, Supplier<NetworkEvent.Context> contextSupplier) {
        var context = contextSupplier.get();

        var source = message.getSource();
        BoopSorterMod.LOGGER.info("[Server] " + source);

        context.enqueueWork(() -> {
            var player = context.getSender();
            if (player == null) {
                return;
            }

            mergeStacksInContainer(player.containerMenu);
//            var containerSlotsIndexStart = 0;
//            var containerSlotsIndexEnd = containerSlots.size() - Inventory.INVENTORY_SIZE;
//            for (var i = containerSlotsIndexStart; i < containerSlotsIndexEnd; i++) {
//                var slot
//            }
//
//            var chestIndexStart = 0;
//            var chestIndexStartEnd = player.containerMenu.slots.size() - Inventory.INVENTORY_SIZE;
//            log("lastIndex: " + chestIndexStartEnd);

//            var slots = container.slots;
//            slots.forEach(slot -> {
//                var item = slot.getItem();
//                if (item.getDescriptionId().equals(Items.AIR.getDescriptionId())) {
//                    return;
//                }
//                log(item.toString());
//            });

//            log("----------------------");
//
//            var containerItems = container.getItems();
//            containerItems.forEach(itemStack -> {
//                if (itemStack.getDescriptionId().equals(Items.AIR.getDescriptionId())) {
//                    return;
//                }
//
//                log(itemStack.toString());
//            });

//            var inventory = player.getInventory();
//            var airItemId = Items.AIR.getDescriptionId();
//            var itemsInInventory = new HashMap<String, List<ItemStack>>();
//
//            for (var i = InventoryMenu.INV_SLOT_START; i < InventoryMenu.INV_SLOT_END; i++) {
//                var itemStack = inventory.getItem(i);
//                var itemId = itemStack.getDescriptionId();
//
//                if (itemId.equals(airItemId)) {
//                    // Ignore empty slots
//                    continue;
//                }
//
//                if (!itemStack.isStackable()) {
//                    continue;
//                }
//
//                if (itemStack.getCount() == itemStack.getMaxStackSize()) {
//                    // Ignore maxed out stacks
//                    continue;
//                }
//
//                if (itemsInInventory.containsKey(itemId)) {
//                    var itemStacks = itemsInInventory.get(itemId);
//                    itemStacks.add(itemStack);
//                    itemsInInventory.put(itemId, itemStacks);
//                    continue;
//                }
//
//                var itemStacks = new ArrayList<ItemStack>();
//                itemStacks.add(itemStack);
//                itemsInInventory.put(itemId, itemStacks);
//            }
//
//            // Ignore single stacks
//            itemsInInventory.entrySet().removeIf((entry) -> entry.getValue().size() <= 1);
//
//            itemsInInventory.forEach((itemId, itemStacks) -> {
//                int totalItemCount = itemStacks.stream().map(ItemStack::getCount).reduce(Integer::sum).orElse(0);
//
//                for (ItemStack itemStack: itemStacks) {
//                    var maxSize = itemStack.getMaxStackSize();
//                    if (totalItemCount >= maxSize) {
//                        itemStack.setCount(maxSize);
//                        totalItemCount -= maxSize;
//                        continue;
//                    }
//
//                    if (totalItemCount > 0) {
//                        itemStack.setCount(totalItemCount);
//                        totalItemCount = 0;
//                        continue;
//                    }
//
//                    inventory.removeItem(itemStack);
//                }
//            });
//
//            var inventoryItems = new ArrayList<ItemStack>();
//
//            for (var i = InventoryMenu.INV_SLOT_START; i < InventoryMenu.INV_SLOT_END; i++) {
//                var itemStack = inventory.getItem(i);
//                var itemStackId = itemStack.getDescriptionId();
//                if (itemStackId.equals(airItemId)) {
//                    continue;
//                }
//
//                inventoryItems.add(itemStack);
//            }
//
//            inventoryItems.sort((item1, item2) -> {
//                var item1Id = item1.getDescriptionId();
//                var item2Id = item2.getDescriptionId();
//                var item1IdReversed = new StringBuilder(item1Id).reverse().toString();
//                var item2IdReversed = new StringBuilder(item2Id).reverse().toString();
//                return item1IdReversed.compareTo(item2IdReversed);
//            });
//
//            var slotIndex = InventoryMenu.INV_SLOT_START;
//            for (ItemStack inventoryItem : inventoryItems) {
//                var inventoryItemCopy = inventoryItem.copy();
//                inventory.setItem(slotIndex, inventoryItemCopy);
//                inventory.removeItem(inventoryItem);
//                slotIndex++;
//            }
        });
        context.setPacketHandled(true);
    }

    private static void log(String message) {
        BoopSorterMod.LOGGER.info(message);
    }

    private static void mergeStacksInContainer(AbstractContainerMenu containerMenu) {
        // TODO: 1 - merge into stacks
        // TODO: 2 - sort
        var containerSlots = containerMenu.slots;
        var containerIndexStart = 0;
        var containerIndexEnd = containerSlots.size() - Inventory.INVENTORY_SIZE;

        var itemsInContainer = new HashMap<String, List<ItemStack>>();
        var airItemId = Items.AIR.getDescriptionId();

        var slot1 = containerSlots.get(containerIndexEnd - 2);
        var slot2 = containerSlots.get(containerIndexEnd - 1);

        var itemStackInSlot1 = slot1.getItem();
        var itemStackInSlot2 = slot2.getItem();

        itemStackInSlot1.setCount(itemStackInSlot1.getCount() + itemStackInSlot2.getCount());
        itemStackInSlot2.setCount(0);
        log("Done!");

//        for (var i = containerIndexStart; i < containerIndexEnd; i++) {
//            var slot = containerSlots.get(i);
//            var itemStack = slot.getItem();
//            var itemId = itemStack.getDescriptionId();
//
//            if (itemId.equals(airItemId)) {
//                // Ignore empty slots
//                continue;
//            }
//
//            if (!itemStack.isStackable()) {
//                continue;
//            }
//
//            if (itemStack.getCount() == itemStack.getMaxStackSize()) {
//                // Ignore maxed out stacks
//                continue;
//            }
//
//            if (itemsInContainer.containsKey(itemId)) {
//                var itemStacks = itemsInContainer.get(itemId);
//                itemStacks.add(itemStack);
//                itemsInContainer.put(itemId, itemStacks);
//                continue;
//            }
//
//            var itemStacks = new ArrayList<ItemStack>();
//            itemStacks.add(itemStack);
//            itemsInContainer.put(itemId, itemStacks);
//        }
//
//        // Ignore single stacks
//        itemsInContainer.entrySet().removeIf((entry) -> entry.getValue().size() <= 1);
//
//        itemsInContainer.forEach((itemId, itemStacks) -> {
//            int totalItemCount = itemStacks.stream().map(ItemStack::getCount).reduce(Integer::sum).orElse(0);
//
//            for (ItemStack itemStack: itemStacks) {
//                var maxSize = itemStack.getMaxStackSize();
//                if (totalItemCount >= maxSize) {
//                    itemStack.setCount(maxSize);
//                    totalItemCount -= maxSize;
//                    continue;
//                }
//
//                if (totalItemCount > 0) {
//                    itemStack.setCount(totalItemCount);
//                    totalItemCount = 0;
//                    continue;
//                }
//
//                inventory.removeItem(itemStack);
//            }
//        });
    }
}
