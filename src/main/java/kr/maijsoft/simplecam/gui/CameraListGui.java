package kr.maijsoft.simplecam.gui;

import kr.maijsoft.simplecam.SimpleCam;
import kr.maijsoft.simplecam.camera.Camera;
import kr.maijsoft.simplecam.manager.CameraStore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public final class CameraListGui {

    public static final int SLOT_BACK = 49;

    private final SimpleCam plugin;
    private final CameraStore store;

    public CameraListGui(SimpleCam plugin, CameraStore store) {
        this.plugin = plugin;
        this.store = store;
    }

    public void open(Player player) {
        GuiHolder holder = new GuiHolder(GuiHolder.Type.SCENE_LIST, "__cameras__");
        Inventory inv = Bukkit.createInventory(holder, 54, Component.text("Cameras"));
        holder.bind(inv);

        int slot = 0;
        Iterator<Camera> it = store.cameras().iterator();
        while (it.hasNext() && slot < 45) {
            Camera cam = it.next();
            inv.setItem(slot++, cameraIcon(cam));
        }

        inv.setItem(SLOT_BACK, MainGui.icon(
                Material.ARROW,
                Component.text("Back", NamedTextColor.YELLOW),
                List.of(Component.text("Return to main menu", NamedTextColor.GRAY))
        ));

        player.openInventory(inv);
    }

    private ItemStack cameraIcon(Camera cam) {
        ItemStack item = new ItemStack(Material.SPYGLASS);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(cam.name(), NamedTextColor.AQUA));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text(String.format(Locale.ROOT,
                    "x=%.1f y=%.1f z=%.1f", cam.x(), cam.y(), cam.z()), NamedTextColor.GRAY));
            lore.add(Component.text("Left-click: teleport", NamedTextColor.DARK_GRAY));
            lore.add(Component.text("Shift+Right: delete", NamedTextColor.RED));
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
}
