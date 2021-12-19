package com.example.examplemod;

import com.example.examplemod.commands.DebugDisableCommand;
import com.example.examplemod.commands.DebugEnableCommand;
import com.example.examplemod.commands.SortAlphabeticallyCommand;
import com.example.examplemod.commands.SortCategoricallyCommand;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityTeleportEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_CONTROL;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_MIDDLE;

@Mod(BoopSorterMod.MOD_ID)
public class BoopSorterMod {
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MOD_ID = "boop_sorter_mod";

    private static final State _state = new State();
    private static NewMessage _newMessage = null;

    public BoopSorterMod() {
        MinecraftForge.EVENT_BUS.register(this);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
    }

    public static void enableLogging() {
        _state.isLoggingEnabled = true;
    }

    public static void disableLogging() {
        _state.isLoggingEnabled = false;
    }

    public static void setSortingByCategory() {
        _state.shouldSortByCategory = true;
    }

    public static void setSortingByAlphabet() {
        _state.shouldSortByCategory = false;
    }

    public void commonSetup(final FMLCommonSetupEvent event) {
        Network.init();
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class StaticKeyHandler {
        /** `true` if the keyboard's CTRL key is being held down. */
        private static boolean _isCtrlHeldDown = false;

        @SubscribeEvent
        public static void onPlayerRightClickedBlock(PlayerInteractEvent.RightClickBlock event) {
            // TODO: What is this?
            var useBlock = event.getUseBlock();
            // TODO: What is this?
            var useItem = event.getUseItem();

            var test = "a";
        }

        @SubscribeEvent
        public static void RenameMeToo(LivingEquipmentChangeEvent event) {
            // TODO: What is this?
            var fromItem = event.getFrom();
            // TODO: What is this?
            var toItem = event.getFrom();
        }

        @SubscribeEvent
        public static void RenameMe(BlockEvent.BreakEvent event) {
            // TODO: Is this the block that 'broke'?
            var block = event.getState().getBlock();
            // TODO: What is this?
            var interactionHand = event.getPlayer().getUsedItemHand();
            // TODO: What is this?
            var itemInHand = event.getPlayer().getItemInHand(interactionHand);
            var what = "a";
        }

        @SubscribeEvent
        public static void onTick(TickEvent.WorldTickEvent event) {
            if (event.phase != TickEvent.Phase.END) {
                return;
            }

            // TODO: Make this more readable
            if (_newMessage != null) {
                Network.CHANNEL.sendToServer(_newMessage);
                _newMessage = null;
            }
        }

        @SubscribeEvent
        public static void onBlockPlaceEvent(BlockEvent.EntityPlaceEvent event) {
            var entity = event.getEntity();
            if (!(entity instanceof Player player)) {
                return;
            }

            var mainHandItem = player.getMainHandItem();
            if (mainHandItem.getCount() > 1) {
                return;
            }

            var usedItemId = mainHandItem.getDescriptionId();
            // TODO: Make this more readable
            _newMessage = new NewMessage(usedItemId);
        }

        /** TODO: Try replacing the item that will break with a new one if found in inventory */
//        TODO: Can I lower the priority to actually catch it post factum?
        @SubscribeEvent
        public static void onPlayerDestroyedItemEvent(PlayerDestroyItemEvent event) {
            /*
                SOURCE: https://forums.minecraftforge.net/topic/44451-solved-item-break-handler/
             */
            // TODO: Is this the hand that is using the item?
            var interactionHand = event.getHand();
            // TODO: Is this the item that will be lost?
            var getItemInHand = event.getOriginal();

            // TODO: Try this?
            //getItemInHand.onDestroyed();

            var test = "a";
        }





        @SubscribeEvent
        public static void onCommandsRegister(RegisterCommandsEvent event) {
            var dispatcher = event.getDispatcher();
            new DebugEnableCommand(dispatcher);
            new DebugDisableCommand(dispatcher);
            new SortAlphabeticallyCommand(dispatcher);
            new SortCategoricallyCommand(dispatcher);
        }

        @SubscribeEvent
        public static void onKeyboardKeyPressedPostEvent(GuiScreenEvent.KeyboardKeyPressedEvent.Post event) {
            if (event.getKeyCode() == GLFW_KEY_LEFT_CONTROL) {
                StaticKeyHandler._isCtrlHeldDown = true;
            }
        }

        @SubscribeEvent
        public static void onKeyboardKeyReleasedPostEvent(GuiScreenEvent.KeyboardKeyReleasedEvent.Post event) {
            if (event.getKeyCode() == GLFW_KEY_LEFT_CONTROL) {
                StaticKeyHandler._isCtrlHeldDown = false;
            }
        }

        @SubscribeEvent
        public static void onGuiMouseReleasedPreEvent(GuiScreenEvent.MouseReleasedEvent.Pre event) {
            if (event.getButton() != GLFW_MOUSE_BUTTON_MIDDLE) {
                log("Ignoring because not MMB clicked.");
                return;
            }

            var screen = event.getGui();

            if (StaticKeyHandler._isCtrlHeldDown) {
                logInfoToChat(screen);
            }

            if (didUserClickOnInventorySlot(screen)) {
                var sortInventoryMessage = new Message(SortTargets.INVENTORY, _state.shouldSortByCategory);
                Network.CHANNEL.sendToServer(sortInventoryMessage);
                return;
            }

            if (didUserClickOnContainerSlot(screen)) {
                var sortContainerMessage = new Message(SortTargets.CONTAINER, _state.shouldSortByCategory);
                Network.CHANNEL.sendToServer(sortContainerMessage);
                return;
            }

            if (didUserClickOnSupportedModContainerSlot(screen)) {
                var sortContainerMessage = new Message(SortTargets.CONTAINER, _state.shouldSortByCategory);
                Network.CHANNEL.sendToServer(sortContainerMessage);
                return;
            }
        }

        private static boolean didUserClickOnInventorySlot(Screen screen) {
            if (!(screen instanceof InventoryScreen inventoryScreen)) {
                log("Ignoring because MMB not clicked in the inventory screen.");
                return false;
            }

            var slot = inventoryScreen.getSlotUnderMouse();
            if (slot == null) {
                log("Ignoring because MMB not clicked on an inventory slot.");
                return false;
            }

            var slotIndex = slot.getSlotIndex();
            if (slotIndex < InventoryMenu.INV_SLOT_START || slotIndex >= InventoryMenu.INV_SLOT_END) {
                log("Ignoring because MMB not clicked on a basic inventory slot.");
                return false;
            }

            return true;
        }

        private static boolean didUserClickOnContainerSlot(Screen screen) {
            if (!(screen instanceof ContainerScreen containerScreen)) {
                log("Ignoring because MMB not clicked in a container screen (ex.: Chest, Shulker box).");
                return false;
            }

            var slot = containerScreen.getSlotUnderMouse();
            if (slot == null) {
                log("Ignoring because MMB not clicked on a slot.");
                return false;
            }

            if (!(slot.container instanceof SimpleContainer)) {
                log("Ignoring because MMB not clicked on a container's slot.");
                return false;
            }

            return true;
        }

        private static boolean didUserClickOnSupportedModContainerSlot(Screen screen) {
            var screenName = screen.getClass().getSimpleName();

            // This is the best that I could do without adding a hard dependency to the mods themselves.
            return screenName.equals("IronChestScreen") || screenName.equals("CrateScreen");
        }

        private static void log(String message) {
            BoopSorterMod.LOGGER.info("[KeyHandler] " + message);
        }

        private static void logInfo(String message) {
            Minecraft.getInstance().gui.getChat().addMessage(new TextComponent(message));
        }

        private static void logSuccess(String message) {
            // TODO: Move to an enum
            final String colorDarkGreen = "\u00A72";
            final String colorLightGreen = "\u00A7a";

            var text = colorDarkGreen + "Success!" + "\n" + colorLightGreen + message;
            Minecraft.getInstance().gui.getChat().addMessage(new TextComponent(text));
        }

        private static void logError(String message) {
            // TODO: Move to an enum
            final String colorDarkRed = "\u00A74";
            final String colorLightRed = "\u00A7c";

            var text = colorDarkRed + "Error!" + "\n" + colorLightRed + message;
            Minecraft.getInstance().gui.getChat().addMessage(new TextComponent(text));
        }

        private static void logInfoToChat(Screen screen) {
            Slot slotUnderMouse;

            if (screen instanceof InventoryScreen inventoryScreen) {
                slotUnderMouse = inventoryScreen.getSlotUnderMouse();
            } else if (screen instanceof ContainerScreen containerScreen) {
                slotUnderMouse = containerScreen.getSlotUnderMouse();
            } else {
                logInfoToChatAboutScreen(screen);
                return;
            }

            if (slotUnderMouse == null) {
                logInfoToChatAboutScreen(screen);
                return;
            }

            if (!slotUnderMouse.hasItem()) {
                logInfoToChatAboutScreen(screen);
                return;
            }

            logInfoToChatAboutItem(slotUnderMouse.getItem());
        }

        private static void logInfoToChatAboutItem(ItemStack itemStack) {
            // TODO: Move to an enum
            final String colorGold = "\u00A76";
            final String colorYellow = "\u00A7e";
            final String colorGrayDark = "\u00A78";

            var descriptionId = itemStack.getDescriptionId();
            var displayName = itemStack.getDisplayName().getContents();
            var hoverName = itemStack.getHoverName().getContents();
            var enchantmentTags = itemStack.getEnchantmentTags().getAsString();
            var simpleClassName = itemStack.getItem().getClass().getSimpleName();

            var registryPathName = "";
            var registryName = itemStack.getItem().getRegistryName();
            if (registryName != null) {
                registryPathName = registryName.getPath();
            }

            var message =
                colorGrayDark + "-----" + "\n" +
                colorGold + "DESCRIPTION ID"        + "\n" + colorYellow + descriptionId    + "\n\n" +
                colorGold + "SIMPLE CLASS NAME"     + "\n" + colorYellow + simpleClassName  + "\n\n" +
                colorGold + "DISPLAY NAME"          + "\n" + colorYellow + displayName      + "\n\n" +
                colorGold + "HOVER NAME"            + "\n" + colorYellow + hoverName        + "\n\n" +
                colorGold + "REGISTRY PATH NAME"    + "\n" + colorYellow + registryPathName + "\n\n" +
                colorGold + "ENCHANTMENT TAGS"      + "\n" + colorYellow + enchantmentTags  + "\n" +
                colorGrayDark + "-----";

            logInfo(message);
        }

        private static void logInfoToChatAboutScreen(Screen screen) {
            var screenClass = screen.getClass();

            var screenSimpleName = screenClass.getSimpleName();
            var screenName = screenClass.getName();
            var screenCanonicalName = screenClass.getCanonicalName();
            var screenPackageName = screenClass.getPackageName();
            var screenTypeName = screenClass.getTypeName();

            // TODO: Move to an enum
            final String colorAquaDark = "\u00A73";
            final String colorAquaLight = "\u00A7b";
            final String colorGrayDark = "\u00A78";

            var message =
                colorGrayDark + "-----" + "\n" +
                colorAquaDark + "SIMPLE NAME"       + "\n" + colorAquaLight + screenSimpleName      + "\n\n" +
                colorAquaDark + "NAME"              + "\n" + colorAquaLight + screenName            + "\n\n" +
                colorAquaDark + "TYPE NAME"         + "\n" + colorAquaLight + screenTypeName        + "\n\n" +
                colorAquaDark + "CANONICAL NAME"    + "\n" + colorAquaLight + screenCanonicalName   + "\n\n" +
                colorAquaDark + "PACKAGE NAME"      + "\n" + colorAquaLight + screenPackageName     + "\n" +
                colorGrayDark + "-----";

            logInfo(message);
        }
    }
}
