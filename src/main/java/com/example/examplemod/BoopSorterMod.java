package com.example.examplemod;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_MIDDLE;

@Mod(BoopSorterMod.MOD_ID)
public class BoopSorterMod
{
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MOD_ID = "boop_sorter_mod";

    public BoopSorterMod() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class StaticKeyHandler
    {
        @SubscribeEvent
        public static void onGuiMouseReleasedEvent(GuiScreenEvent.MouseReleasedEvent.Pre event)
        {
            if (event.getButton() != GLFW_MOUSE_BUTTON_MIDDLE)
            {
                Log("Ignoring because not MMB clicked.");
                return;
            }

            var screen = event.getGui();

            if (canSortInventoryScreen(screen))
            {
                StaticKeyHandler.sortInventory();
                return;
            }

            if (canSortChestScreen(screen))
            {
                StaticKeyHandler.sortInventory();
                return;
            }
        }

        public static boolean canSortInventoryScreen(Screen screen)
        {
            if (!(screen instanceof InventoryScreen inventoryScreen))
            {
                Log("Ignoring because MMB not clicked in the inventory screen.");
                return false;
            }

            var slot = inventoryScreen.getSlotUnderMouse();
            if (slot == null)
            {
                Log("Ignoring because MMB not clicked on an inventory slot.");
                return false;
            }

            var slotIndex = slot.getSlotIndex();
            if (slotIndex < InventoryMenu.INV_SLOT_START || slotIndex >= InventoryMenu.INV_SLOT_END)
            {
                Log("Ignoring because MMB not clicked on a basic inventory slot.");
                return false;
            }

            Log("Can be sorted!");
            return true;
        }

        public static boolean canSortChestScreen(Screen screen)
        {
            if (!(screen instanceof ContainerScreen containerScreen))
            {
                Log("Ignoring because MMB not clicked in a container screen (ex.: Chest).");
                return false;
            }

            var slot = containerScreen.getSlotUnderMouse();
            if (slot == null)
            {
                Log("Ignoring because MMB not clicked on a slot.");
                return false;
            }

            if (!(slot.container instanceof SimpleContainer))
            {
                Log("Ignoring because MMB not clicked on a container's slot.");
                return false;
            }

            Log("Can be sorted!");
            return true;
        }

        public static void sortInventory()
        {
            // TODO: Implement me!
        }

        public static void Log(String message)
        {
            BoopSorterMod.LOGGER.info("[KeyHandler] " + message);
        }
    }
}
