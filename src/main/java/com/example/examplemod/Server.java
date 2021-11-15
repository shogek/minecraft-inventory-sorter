package com.example.examplemod;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

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
            player.addItem(new ItemStack(Items.DIAMOND));
        });
        context.setPacketHandled(true);
    }
}
