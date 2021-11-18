package com.example.examplemod;

import net.minecraft.world.inventory.InventoryMenu;
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

            var inventory = player.getInventory();
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
        });
        context.setPacketHandled(true);
    }
}
