package kr.maijsoft.simplecam.gui;

import kr.maijsoft.simplecam.SimpleCam;
import kr.maijsoft.simplecam.manager.CameraStore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public final class MainGui {

    public static final int SLOT_CAMERAS = 11;
    public static final int SLOT_SCENES = 15;

    private final SimpleCam plugin;
    private final CameraStore store;

    public MainGui(SimpleCam plugin, CameraStore store) {
        this.plugin = plugin;
        this.store = store;
    }

    public void open(Player player) {
        GuiHolder holder = new GuiHolder(GuiHolder.Type.MAIN, null);
        Inventory inv = Bukkit.createInventory(holder, 27, Component.text("SimpleCam"));
        holder.bind(inv);

        inv.setItem(SLOT_CAMERAS, icon(
                Material.SPYGLASS,
                Component.text("Cameras", NamedTextColor.AQUA),
                List.of(
                        Component.text(store.cameras().size() + " saved", NamedTextColor.GRAY),
                        Component.text("Click to view list", NamedTextColor.DARK_GRAY)
                )
        ));
        inv.setItem(SLOT_SCENES, icon(
                Material.FILLED_MAP,
                Component.text("Scenes", NamedTextColor.GOLD),
                List.of(
                        Component.text(store.scenes().size() + " saved", NamedTextColor.GRAY),
                        Component.text("Click to manage", NamedTextColor.DARK_GRAY)
                )
        ));

        player.openInventory(inv);
    }

    static ItemStack icon(Material material, Component name, List<Component> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(name);
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
}
