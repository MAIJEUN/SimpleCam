package kr.maijsoft.simplecam.gui;

import kr.maijsoft.simplecam.SimpleCam;
import kr.maijsoft.simplecam.camera.Camera;
import kr.maijsoft.simplecam.camera.Scene;
import kr.maijsoft.simplecam.manager.CameraStore;
import kr.maijsoft.simplecam.manager.PlaybackManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class GuiListener implements Listener {

    private final SimpleCam plugin;
    private final CameraStore store;
    private final PlaybackManager playback;

    public GuiListener(SimpleCam plugin, CameraStore store, PlaybackManager playback) {
        this.plugin = plugin;
        this.store = store;
        this.playback = playback;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (!(holder instanceof GuiHolder gui)) return;

        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) return;

        switch (gui.type()) {
            case MAIN -> handleMain(player, event);
            case SCENE_LIST -> handleList(player, gui, event);
            case SCENE_EDIT -> {}
        }
    }

    private void handleMain(Player player, InventoryClickEvent event) {
        int slot = event.getRawSlot();
        if (slot == MainGui.SLOT_CAMERAS) {
            new CameraListGui(plugin, store).open(player);
        } else if (slot == MainGui.SLOT_SCENES) {
            new SceneListGui(plugin, store).open(player);
        }
    }

    private void handleList(Player player, GuiHolder gui, InventoryClickEvent event) {
        int slot = event.getRawSlot();
        if (slot == SceneListGui.SLOT_BACK) {
            new MainGui(plugin, store).open(player);
            return;
        }
        ItemStack item = event.getCurrentItem();
        if (item == null || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        Component name = meta.displayName();
        if (name == null) return;
        String label = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(name);

        boolean isCameras = "__cameras__".equals(gui.contextName());
        ClickType click = event.getClick();

        if (isCameras) {
            Camera cam = store.getCamera(label);
            if (cam == null) return;
            if (click == ClickType.SHIFT_RIGHT) {
                store.removeCamera(cam.name());
                store.save();
                new CameraListGui(plugin, store).open(player);
                player.sendMessage(Component.text("Camera deleted: " + cam.name(), NamedTextColor.GREEN));
            } else if (click.isLeftClick()) {
                Location loc = cam.toLocation();
                if (loc != null) player.teleport(loc);
            }
            return;
        }

        Scene scene = store.getScene(label);
        if (scene == null) return;
        if (click == ClickType.SHIFT_RIGHT) {
            store.removeScene(scene.name());
            store.save();
            new SceneListGui(plugin, store).open(player);
            player.sendMessage(Component.text("Scene deleted: " + scene.name(), NamedTextColor.GREEN));
        } else if (click.isLeftClick()) {
            player.closeInventory();
            if (playback.start(player, scene) == null) {
                player.sendMessage(Component.text(
                        "Scene needs at least 2 valid cameras to play.", NamedTextColor.RED));
            } else {
                player.sendMessage(Component.text(
                        "Playing scene: " + scene.name(), NamedTextColor.GREEN));
            }
        } else if (click == ClickType.RIGHT) {
            player.sendMessage(Component.text("Scene: " + scene.name() + " (" + scene.shots().size() + " shots)", NamedTextColor.AQUA));
            int i = 0;
            for (Scene.Shot shot : scene.shots()) {
                player.sendMessage(Component.text(
                        " " + i + ". " + shot.cameraName() + " (" + shot.seconds() + "s)",
                        NamedTextColor.GRAY));
                i++;
            }
        }
    }
}
