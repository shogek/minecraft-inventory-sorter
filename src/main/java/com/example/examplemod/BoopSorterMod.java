package com.example.examplemod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_CONTROL;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_MIDDLE;

@Mod(BoopSorterMod.MOD_ID)
public class BoopSorterMod
{
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MOD_ID = "boop_sorter_mod";

    public BoopSorterMod() {
        MinecraftForge.EVENT_BUS.register(this);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
    }

    public void commonSetup(final FMLCommonSetupEvent event) {
        Network.init();
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class StaticKeyHandler
    {
        /** `true` if the keyboard's CTRL key is being held down. */
        private static boolean _isCtrlHeldDown = false;

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
        public static void onGuiMouseReleasedEvent(GuiScreenEvent.MouseReleasedEvent.Pre event)
        {
            if (event.getButton() != GLFW_MOUSE_BUTTON_MIDDLE) {
                log("Ignoring because not MMB clicked.");
                return;
            }

            var screen = event.getGui();

            if (didUserClickOnInventorySlot(screen)) {
                var sortInventoryMessage = new Message(SortTargets.INVENTORY);
                Network.CHANNEL.sendToServer(sortInventoryMessage);
                return;
            }

            if (didUserClickOnContainerSlot(screen)) {
                var sortContainerMessage = new Message(SortTargets.CONTAINER);
                Network.CHANNEL.sendToServer(sortContainerMessage);
                return;
            }

            if (didUserClickOnIronChestModContainerSlot(screen)) {
                var sortContainerMessage = new Message(SortTargets.CONTAINER);
                Network.CHANNEL.sendToServer(sortContainerMessage);
                return;
            }

            if (StaticKeyHandler._isCtrlHeldDown) {
                StaticKeyHandler._isCtrlHeldDown = false;
                logInfoToChatAboutContainerScreen(screen);
                return;
            }
        }

        private static boolean didUserClickOnInventorySlot(Screen screen) {
            if (!(screen instanceof InventoryScreen inventoryScreen))
            {
                log("Ignoring because MMB not clicked in the inventory screen.");
                return false;
            }

            var slot = inventoryScreen.getSlotUnderMouse();
            if (slot == null)
            {
                log("Ignoring because MMB not clicked on an inventory slot.");
                return false;
            }

            var slotIndex = slot.getSlotIndex();
            if (slotIndex < InventoryMenu.INV_SLOT_START || slotIndex >= InventoryMenu.INV_SLOT_END)
            {
                log("Ignoring because MMB not clicked on a basic inventory slot.");
                return false;
            }

            return true;
        }

        private static boolean didUserClickOnContainerSlot(Screen screen) {
            if (!(screen instanceof ContainerScreen containerScreen))
            {
                log("Ignoring because MMB not clicked in a container screen (ex.: Chest, Shulker box).");
                return false;
            }

            var slot = containerScreen.getSlotUnderMouse();
            if (slot == null)
            {
                log("Ignoring because MMB not clicked on a slot.");
                return false;
            }

            if (!(slot.container instanceof SimpleContainer))
            {
                log("Ignoring because MMB not clicked on a container's slot.");
                return false;
            }

            return true;
        }

        private static boolean didUserClickOnIronChestModContainerSlot(Screen screen) {
            // This is the best that I could do without adding a hard dependency to the mod itself.
            return screen.getClass().getSimpleName().equals("IronChestScreen");
        }

        private static void log(String message) {
            BoopSorterMod.LOGGER.info("[KeyHandler] " + message);
        }

        private static void logInfoToChatAboutContainerScreen(Screen screen) {
            var screenClass = screen.getClass();

            var screenSimpleName = screenClass.getSimpleName();
            var screenName = screenClass.getName();
            var screenCanonicalName = screenClass.getCanonicalName();
            var screenPackageName = screenClass.getPackageName();
            var screenTypeName = screenClass.getTypeName();

            var message = "\n" +
                    "\u00A75" + "TypeName:\n" + "\u00A7d" + screenTypeName + "\n\n" +
                    "\u00A74" + "Name:\n" + "\u00A7c" + screenName + "\n\n" +
                    "\u00A76" + "SimpleName:\n" + "\u00A7e" + screenSimpleName + "\n\n" +
                    "\u00A72" + "CanonicalName:\n" + "\u00A7a" + screenCanonicalName + "\n\n" +
                    "\u00A73" + "PackageName:\n" + "\u00A7b" + screenPackageName;

            Minecraft.getInstance().gui.getChat().addMessage(new TextComponent(message));
        }
    }
}
