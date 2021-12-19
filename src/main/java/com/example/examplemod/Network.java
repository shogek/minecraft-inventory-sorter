package com.example.examplemod;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fmllegacy.network.NetworkRegistry;
import net.minecraftforge.fmllegacy.network.simple.SimpleChannel;

import java.util.Objects;

public class Network {
    /** Update the value after adding a new packet to update the server. */
    public static final String NETWORK_VERSION = "0.1";

    public static final SimpleChannel CHANNEL;

    static {
        CHANNEL = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation(BoopSorterMod.MOD_ID, "network"))
                .clientAcceptedVersions(version -> Objects.equals(version, NETWORK_VERSION))
                .serverAcceptedVersions(version -> Objects.equals(version, NETWORK_VERSION))
                .networkProtocolVersion(() -> NETWORK_VERSION)
                .simpleChannel();

        CHANNEL.messageBuilder(Message.class, 1)
                .decoder(Message::decode)
                .encoder(Message::encode)
                .consumer(Server::onMessage)
                .add();

        CHANNEL.messageBuilder(NewMessage.class, 1)
                .decoder(NewMessage::decode)
                .encoder(NewMessage::encode)
                .consumer(Server::onNewMessage)
                .add();
    }

    public static void init() {
        CHANNEL.registerMessage(0, Message.class, Message::encode, Message::decode, Server::onMessage);
        CHANNEL.registerMessage(1, NewMessage.class, NewMessage::encode, NewMessage::decode, Server::onNewMessage);
    }
}
