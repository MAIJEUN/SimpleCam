package kr.maijsoft.simplecam.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

/**
 * Sentinel holder used to identify SimpleCam-owned inventories in events.
 */
public final class GuiHolder implements InventoryHolder {

    public enum Type { MAIN, SCENE_LIST, SCENE_EDIT }

    private final Type type;
    private final String contextName;
    private Inventory inventory;

    public GuiHolder(Type type, String contextName) {
        this.type = type;
        this.contextName = contextName;
    }

    public Type type() {
        return type;
    }

    public String contextName() {
        return contextName;
    }

    public void bind(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
