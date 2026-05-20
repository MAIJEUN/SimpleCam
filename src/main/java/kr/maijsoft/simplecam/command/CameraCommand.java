package kr.maijsoft.simplecam.command;

import kr.maijsoft.simplecam.SimpleCam;
import kr.maijsoft.simplecam.camera.Camera;
import kr.maijsoft.simplecam.gui.MainGui;
import kr.maijsoft.simplecam.manager.CameraStore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class CameraCommand implements CommandExecutor, TabCompleter {

    private static final List<String> SUBS = List.of("create", "delete", "list", "tp", "gui");

    private final SimpleCam plugin;
    private final CameraStore store;

    public CameraCommand(SimpleCam plugin, CameraStore store) {
        this.plugin = plugin;
        this.store = store;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "create" -> handleCreate(sender, args);
            case "delete" -> handleDelete(sender, args);
            case "list" -> handleList(sender);
            case "tp" -> handleTp(sender, args);
            case "gui" -> handleGui(sender);
            default -> sendHelp(sender);
        }
        return true;
    }

    private void handleCreate(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Players only.", NamedTextColor.RED));
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage: /camera create <name>", NamedTextColor.YELLOW));
            return;
        }
        String name = args[1];
        Camera cam = new Camera(name, player.getLocation());
        if (!store.addCamera(cam)) {
            sender.sendMessage(Component.text("A camera named '" + name + "' already exists.", NamedTextColor.RED));
            return;
        }
        store.save();
        sender.sendMessage(Component.text("Camera '" + name + "' created.", NamedTextColor.GREEN));
    }

    private void handleDelete(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage: /camera delete <name>", NamedTextColor.YELLOW));
            return;
        }
        if (!store.removeCamera(args[1])) {
            sender.sendMessage(Component.text("No camera named '" + args[1] + "'.", NamedTextColor.RED));
            return;
        }
        store.save();
        sender.sendMessage(Component.text("Camera deleted.", NamedTextColor.GREEN));
    }

    private void handleList(CommandSender sender) {
        if (store.cameras().isEmpty()) {
            sender.sendMessage(Component.text("No cameras yet.", NamedTextColor.GRAY));
            return;
        }
        sender.sendMessage(Component.text("Cameras (" + store.cameras().size() + "):", NamedTextColor.AQUA));
        for (Camera cam : store.cameras()) {
            sender.sendMessage(Component.text(
                    " - " + cam.name() + " @ " + fmt(cam.x()) + ", " + fmt(cam.y()) + ", " + fmt(cam.z()),
                    NamedTextColor.GRAY));
        }
    }

    private void handleTp(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Players only.", NamedTextColor.RED));
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage: /camera tp <name>", NamedTextColor.YELLOW));
            return;
        }
        Camera cam = store.getCamera(args[1]);
        if (cam == null) {
            sender.sendMessage(Component.text("No camera named '" + args[1] + "'.", NamedTextColor.RED));
            return;
        }
        Location loc = cam.toLocation();
        if (loc == null) {
            sender.sendMessage(Component.text("Camera world is not loaded.", NamedTextColor.RED));
            return;
        }
        player.teleport(loc);
    }

    private void handleGui(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Players only.", NamedTextColor.RED));
            return;
        }
        new MainGui(plugin, store).open(player);
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(Component.text("SimpleCam - Camera commands", NamedTextColor.AQUA));
        sender.sendMessage(Component.text(" /camera create <name>", NamedTextColor.GRAY));
        sender.sendMessage(Component.text(" /camera delete <name>", NamedTextColor.GRAY));
        sender.sendMessage(Component.text(" /camera list", NamedTextColor.GRAY));
        sender.sendMessage(Component.text(" /camera tp <name>", NamedTextColor.GRAY));
        sender.sendMessage(Component.text(" /camera gui", NamedTextColor.GRAY));
    }

    private static String fmt(double v) {
        return String.format(Locale.ROOT, "%.1f", v);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return filter(SUBS, args[0]);
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("delete") || args[0].equalsIgnoreCase("tp"))) {
            List<String> names = new ArrayList<>();
            store.cameras().forEach(c -> names.add(c.name()));
            return filter(names, args[1]);
        }
        return Collections.emptyList();
    }

    private static List<String> filter(List<String> input, String prefix) {
        String lower = prefix.toLowerCase(Locale.ROOT);
        List<String> out = new ArrayList<>();
        for (String s : input) {
            if (s.toLowerCase(Locale.ROOT).startsWith(lower)) out.add(s);
        }
        return out;
    }
}
