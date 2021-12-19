package com.example.examplemod;

import com.example.examplemod.handlers.ReplaceDestroyedItemHandler;
import com.example.examplemod.handlers.SortItemsHandler;
import com.example.examplemod.messages.SortItemsMessage;
import com.example.examplemod.messages.ReplaceDestroyedItemMessage;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fmllegacy.network.NetworkRegistry;
import net.minecraftforge.fmllegacy.network.simple.SimpleChannel;

import java.util.Objects;

public class Network {
    /** Update the value after adding a new packet to update the server. */
    public static final String NETWORK_VERSION = "0.2";

    public static final SimpleChannel CHANNEL;

    static {
        CHANNEL = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation(BoopSorterMod.MOD_ID, "network"))
                .clientAcceptedVersions(version -> Objects.equals(version, NETWORK_VERSION))
                .serverAcceptedVersions(version -> Objects.equals(version, NETWORK_VERSION))
                .networkProtocolVersion(() -> NETWORK_VERSION)
                .simpleChannel();

        CHANNEL.messageBuilder(SortItemsMessage.class, 1)
                .decoder(SortItemsMessage::decode)
                .encoder(SortItemsMessage::encode)
                .consumer(SortItemsHandler::handle)
                .add();

        CHANNEL.messageBuilder(ReplaceDestroyedItemMessage.class, 2)
                .decoder(ReplaceDestroyedItemMessage::decode)
                .encoder(ReplaceDestroyedItemMessage::encode)
                .consumer(ReplaceDestroyedItemHandler::handle)
                .add();
    }

    public static void init() {
        CHANNEL.registerMessage(
            0,
            SortItemsMessage.class,
            SortItemsMessage::encode,
            SortItemsMessage::decode,
            SortItemsHandler::handle
        );

        CHANNEL.registerMessage(
            1,
            ReplaceDestroyedItemMessage.class,
            ReplaceDestroyedItemMessage::encode,
            ReplaceDestroyedItemMessage::decode,
            ReplaceDestroyedItemHandler::handle
        );
    }
}
