package kr.maijsoft.simplecam.command;

import kr.maijsoft.simplecam.SimpleCam;
import kr.maijsoft.simplecam.camera.Camera;
import kr.maijsoft.simplecam.camera.Scene;
import kr.maijsoft.simplecam.manager.CameraStore;
import kr.maijsoft.simplecam.manager.PlaybackManager;
import kr.maijsoft.simplecam.playback.PlaybackSession;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
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

public final class SceneCommand implements CommandExecutor, TabCompleter {

    private static final List<String> SUBS = List.of(
            "create", "delete", "list", "addcam", "removecam", "play", "stop", "info"
    );

    private final SimpleCam plugin;
    private final CameraStore store;
    private final PlaybackManager playback;

    public SceneCommand(SimpleCam plugin, CameraStore store, PlaybackManager playback) {
        this.plugin = plugin;
        this.store = store;
        this.playback = playback;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "create" -> handleCreate(sender, args);
            case "delete" -> handleDelete(sender, args);
            case "list" -> handleList(sender);
            case "addcam" -> handleAddCam(sender, args);
            case "removecam" -> handleRemoveCam(sender, args);
            case "play" -> handlePlay(sender, args);
            case "stop" -> handleStop(sender, args);
            case "info" -> handleInfo(sender, args);
            default -> sendHelp(sender);
        }
        return true;
    }

    private void handleCreate(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage: /scene create <name>", NamedTextColor.YELLOW));
            return;
        }
        if (!store.addScene(new Scene(args[1]))) {
            sender.sendMessage(Component.text("A scene named '" + args[1] + "' already exists.", NamedTextColor.RED));
            return;
        }
        store.save();
        sender.sendMessage(Component.text("Scene '" + args[1] + "' created.", NamedTextColor.GREEN));
    }

    private void handleDelete(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage: /scene delete <name>", NamedTextColor.YELLOW));
            return;
        }
        if (!store.removeScene(args[1])) {
            sender.sendMessage(Component.text("No scene named '" + args[1] + "'.", NamedTextColor.RED));
            return;
        }
        store.save();
        sender.sendMessage(Component.text("Scene deleted.", NamedTextColor.GREEN));
    }

    private void handleList(CommandSender sender) {
        if (store.scenes().isEmpty()) {
            sender.sendMessage(Component.text("No scenes yet.", NamedTextColor.GRAY));
            return;
        }
        sender.sendMessage(Component.text("Scenes (" + store.scenes().size() + "):", NamedTextColor.AQUA));
        for (Scene scene : store.scenes()) {
            sender.sendMessage(Component.text(
                    " - " + scene.name() + " (" + scene.shots().size() + " shots)",
                    NamedTextColor.GRAY));
        }
    }

    private void handleAddCam(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(Component.text("Usage: /scene addcam <scene> <camera> [seconds]", NamedTextColor.YELLOW));
            return;
        }
        Scene scene = store.getScene(args[1]);
        if (scene == null) {
            sender.sendMessage(Component.text("No scene named '" + args[1] + "'.", NamedTextColor.RED));
            return;
        }
        Camera cam = store.getCamera(args[2]);
        if (cam == null) {
            sender.sendMessage(Component.text("No camera named '" + args[2] + "'.", NamedTextColor.RED));
            return;
        }
        double seconds = plugin.getConfig().getDouble("playback.default-segment-seconds", 4.0);
        if (args.length >= 4) {
            try {
                seconds = Double.parseDouble(args[3]);
            } catch (NumberFormatException e) {
                sender.sendMessage(Component.text("Invalid seconds value.", NamedTextColor.RED));
                return;
            }
        }
        scene.addShot(cam.name(), seconds);
        store.save();
        sender.sendMessage(Component.text(
                "Added '" + cam.name() + "' to scene '" + scene.name() + "' (" + seconds + "s).",
                NamedTextColor.GREEN));
    }

    private void handleRemoveCam(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(Component.text("Usage: /scene removecam <scene> <index>", NamedTextColor.YELLOW));
            return;
        }
        Scene scene = store.getScene(args[1]);
        if (scene == null) {
            sender.sendMessage(Component.text("No scene named '" + args[1] + "'.", NamedTextColor.RED));
            return;
        }
        int idx;
        try {
            idx = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(Component.text("Invalid index.", NamedTextColor.RED));
            return;
        }
        if (!scene.removeShot(idx)) {
            sender.sendMessage(Component.text("Index out of range.", NamedTextColor.RED));
            return;
        }
        store.save();
        sender.sendMessage(Component.text("Shot removed.", NamedTextColor.GREEN));
    }

    private void handlePlay(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage: /scene play <scene> [player]", NamedTextColor.YELLOW));
            return;
        }
        Scene scene = store.getScene(args[1]);
        if (scene == null) {
            sender.sendMessage(Component.text("No scene named '" + args[1] + "'.", NamedTextColor.RED));
            return;
        }
        Player target = resolveTarget(sender, args, 2);
        if (target == null) return;

        PlaybackSession session = playback.start(target, scene);
        if (session == null) {
            sender.sendMessage(Component.text(
                    "Scene needs at least 2 valid cameras to play.", NamedTextColor.RED));
            return;
        }
        sender.sendMessage(Component.text(
                "Playing '" + scene.name() + "' for " + target.getName() + ".", NamedTextColor.GREEN));
    }

    private void handleStop(CommandSender sender, String[] args) {
        Player target = resolveTarget(sender, args, 1);
        if (target == null) return;
        if (playback.stop(target)) {
            sender.sendMessage(Component.text(
                    "Stopped playback for " + target.getName() + ".", NamedTextColor.GREEN));
        } else {
            sender.sendMessage(Component.text(
                    target.getName() + " has no active playback.", NamedTextColor.GRAY));
        }
    }

    private void handleInfo(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage: /scene info <name>", NamedTextColor.YELLOW));
            return;
        }
        Scene scene = store.getScene(args[1]);
        if (scene == null) {
            sender.sendMessage(Component.text("No scene named '" + args[1] + "'.", NamedTextColor.RED));
            return;
        }
        sender.sendMessage(Component.text("Scene: " + scene.name(), NamedTextColor.AQUA));
        int i = 0;
        for (Scene.Shot shot : scene.shots()) {
            sender.sendMessage(Component.text(
                    " " + i + ". " + shot.cameraName() + " (" + shot.seconds() + "s)",
                    NamedTextColor.GRAY));
            i++;
        }
    }

    private Player resolveTarget(CommandSender sender, String[] args, int index) {
        if (args.length > index) {
            Player target = Bukkit.getPlayerExact(args[index]);
            if (target == null) {
                sender.sendMessage(Component.text("Player not found: " + args[index], NamedTextColor.RED));
                return null;
            }
            if (!sender.equals(target) && !sender.hasPermission("simplecam.admin")) {
                sender.sendMessage(Component.text("You may only target yourself.", NamedTextColor.RED));
                return null;
            }
            return target;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Console must specify a target player.", NamedTextColor.RED));
            return null;
        }
        return player;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(Component.text("SimpleCam - Scene commands", NamedTextColor.AQUA));
        sender.sendMessage(Component.text(" /scene create <name>", NamedTextColor.GRAY));
        sender.sendMessage(Component.text(" /scene delete <name>", NamedTextColor.GRAY));
        sender.sendMessage(Component.text(" /scene list", NamedTextColor.GRAY));
        sender.sendMessage(Component.text(" /scene info <name>", NamedTextColor.GRAY));
        sender.sendMessage(Component.text(" /scene addcam <scene> <camera> [seconds]", NamedTextColor.GRAY));
        sender.sendMessage(Component.text(" /scene removecam <scene> <index>", NamedTextColor.GRAY));
        sender.sendMessage(Component.text(" /scene play <scene> [player]", NamedTextColor.GRAY));
        sender.sendMessage(Component.text(" /scene stop [player]", NamedTextColor.GRAY));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) return filter(SUBS, args[0]);

        String sub = args[0].toLowerCase(Locale.ROOT);
        if (args.length == 2 && List.of("delete", "addcam", "removecam", "play", "info").contains(sub)) {
            return filter(sceneNames(), args[1]);
        }
        if (args.length == 3 && sub.equals("addcam")) {
            return filter(cameraNames(), args[2]);
        }
        if (args.length == 3 && sub.equals("play")) {
            return filter(onlinePlayers(), args[2]);
        }
        if (args.length == 2 && sub.equals("stop")) {
            return filter(onlinePlayers(), args[1]);
        }
        return Collections.emptyList();
    }

    private List<String> sceneNames() {
        List<String> out = new ArrayList<>();
        store.scenes().forEach(s -> out.add(s.name()));
        return out;
    }

    private List<String> cameraNames() {
        List<String> out = new ArrayList<>();
        store.cameras().forEach(c -> out.add(c.name()));
        return out;
    }

    private List<String> onlinePlayers() {
        List<String> out = new ArrayList<>();
        Bukkit.getOnlinePlayers().forEach(p -> out.add(p.getName()));
        return out;
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
