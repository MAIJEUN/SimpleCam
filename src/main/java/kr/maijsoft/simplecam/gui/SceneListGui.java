package kr.maijsoft.simplecam.gui;

import kr.maijsoft.simplecam.SimpleCam;
import kr.maijsoft.simplecam.camera.Scene;
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

public final class SceneListGui {

    public static final int SLOT_BACK = 49;

    private final SimpleCam plugin;
    private final CameraStore store;

    public SceneListGui(SimpleCam plugin, CameraStore store) {
        this.plugin = plugin;
        this.store = store;
    }

    public void open(Player player) {
        GuiHolder holder = new GuiHolder(GuiHolder.Type.SCENE_LIST, null);
        Inventory inv = Bukkit.createInventory(holder, 54, Component.text("Scenes"));
        holder.bind(inv);

        int slot = 0;
        Iterator<Scene> it = store.scenes().iterator();
        while (it.hasNext() && slot < 45) {
            Scene scene = it.next();
            inv.setItem(slot++, sceneIcon(scene));
        }

        inv.setItem(SLOT_BACK, MainGui.icon(
                Material.ARROW,
                Component.text("Back", NamedTextColor.YELLOW),
                List.of(Component.text("Return to main menu", NamedTextColor.GRAY))
        ));

        player.openInventory(inv);
    }

    private ItemStack sceneIcon(Scene scene) {
        ItemStack item = new ItemStack(Material.FILLED_MAP);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(scene.name(), NamedTextColor.GOLD));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text(scene.shots().size() + " shots", NamedTextColor.GRAY));
            lore.add(Component.text("Left-click: play on you", NamedTextColor.DARK_GRAY));
            lore.add(Component.text("Right-click: view details", NamedTextColor.DARK_GRAY));
            lore.add(Component.text("Shift+Right: delete", NamedTextColor.RED));
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
}
