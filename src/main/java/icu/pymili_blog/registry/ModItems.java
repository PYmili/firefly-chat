package icu.pymili_blog.registry;

import icu.pymili_blog.FireflyChat;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;


public class ModItems {
    public static final Item ICE_ETHER = registerItems(
            "shenli_linghua", new Item(new FabricItemSettings())
    );
    private static Item registerItems(String name, Item item) {
        return Registry.register(
                Registries.ITEM,
                new Identifier(FireflyChat.MOD_ID, name),
                item
        );
    }
    public static void addItemsToItemGroup(FabricItemGroupEntries fabricItemGroupEntries) {
        fabricItemGroupEntries.add(ICE_ETHER);
    }
    public static void registerModItems() {
        FireflyChat.LOGGER.debug("Registering mod items for" + FireflyChat.MOD_ID);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS)
                .register(ModItems::addItemsToItemGroup);
    }
}
