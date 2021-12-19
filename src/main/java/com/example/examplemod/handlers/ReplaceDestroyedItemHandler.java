package com.example.examplemod.handlers;

import com.example.examplemod.messages.ReplaceDestroyedItemMessage;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import java.util.function.Supplier;

public class ReplaceDestroyedItemHandler {
    /** Replace the recently (broken tool/depleted stack) with an identical one from the player's inventory (if found). */
    public static void handle(ReplaceDestroyedItemMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        var context = contextSupplier.get();

        context.enqueueWork(() -> {
            var player = context.getSender();
            if (player == null) {
                return;
            }

            var destroyedItemId = message.getDestroyedItemId();
            var inventory = player.getInventory();
            for (var i = InventoryMenu.INV_SLOT_START; i < InventoryMenu.INV_SLOT_END; i++) {
                var inventoryItem = inventory.getItem(i);
                var inventoryItemId = inventoryItem.getDescriptionId();

                if (!destroyedItemId.equals(inventoryItemId)) {
                    continue;
                }

                var inventoryItemCopy = inventoryItem.copy();
                inventory.removeItem(inventoryItem);
                var interactionHand = player.getUsedItemHand();
                player.setItemInHand(interactionHand, inventoryItemCopy);
                break;
            }
        });

        context.setPacketHandled(true);
    }
}
