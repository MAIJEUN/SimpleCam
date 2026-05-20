package kr.maijsoft.simplecam.playback;

import kr.maijsoft.simplecam.SimpleCam;
import kr.maijsoft.simplecam.camera.Camera;
import kr.maijsoft.simplecam.camera.Scene;
import kr.maijsoft.simplecam.manager.CameraStore;
import kr.maijsoft.simplecam.util.Interpolation;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class PlaybackSession extends BukkitRunnable {

    private final SimpleCam plugin;
    private final Player player;
    private final Scene scene;
    private final List<Camera> path;
    private final List<Integer> segmentTicks;
    private final int totalTicks;
    private final GameMode originalMode;
    private final Location originalLocation;
    private final boolean useSpectator;
    private final boolean restoreOnFinish;

    private Consumer<PlaybackSession> onFinish;
    private int tick = 0;

    public PlaybackSession(SimpleCam plugin, CameraStore store, Player player, Scene scene) {
        this.plugin = plugin;
        this.player = player;
        this.scene = scene;
        this.originalMode = player.getGameMode();
        this.originalLocation = player.getLocation().clone();

        this.useSpectator = plugin.getConfig().getBoolean("playback.use-spectator", true);
        this.restoreOnFinish = plugin.getConfig().getBoolean("playback.restore-on-finish", true);
        int tickRate = plugin.getConfig().getInt("playback.tick-rate", 20);
        if (tickRate <= 0) tickRate = 20;

        this.path = new ArrayList<>();
        this.segmentTicks = new ArrayList<>();
        int total = 0;
        for (Scene.Shot shot : scene.shots()) {
            Camera cam = store.getCamera(shot.cameraName());
            if (cam == null) continue;
            path.add(cam);
            int ticks = Math.max(1, (int) Math.round(shot.seconds() * tickRate));
            segmentTicks.add(ticks);
            total += ticks;
        }
        this.totalTicks = total;
    }

    public Player player() {
        return player;
    }

    public Scene scene() {
        return scene;
    }

    public boolean isPlayable() {
        return path.size() >= 2 && totalTicks > 0;
    }

    public void onFinish(Consumer<PlaybackSession> callback) {
        this.onFinish = callback;
    }

    public void start() {
        if (useSpectator) {
            player.setGameMode(GameMode.SPECTATOR);
        }
        runTaskTimer(plugin, 0L, 1L);
    }

    public void stop(boolean restore) {
        try {
            cancel();
        } catch (IllegalStateException ignored) {
        }
        if (restore && restoreOnFinish && player.isOnline()) {
            if (useSpectator) player.setGameMode(originalMode);
            player.teleport(originalLocation);
        }
        if (onFinish != null) {
            Consumer<PlaybackSession> cb = onFinish;
            onFinish = null;
            cb.accept(this);
        }
    }

    @Override
    public void run() {
        if (!player.isOnline()) {
            stop(false);
            return;
        }
        if (tick >= totalTicks) {
            Camera last = path.get(path.size() - 1);
            Location loc = last.toLocation();
            if (loc != null) player.teleport(loc);
            stop(true);
            return;
        }

        int remaining = tick;
        int segIndex = 0;
        int segTicks = segmentTicks.get(0);
        while (segIndex < segmentTicks.size() - 1 && remaining >= segTicks) {
            remaining -= segTicks;
            segIndex++;
            segTicks = segmentTicks.get(segIndex);
        }
        double localT = (double) remaining / (double) segTicks;
        double eased = Interpolation.smoothstep(localT);

        Camera p0 = path.get(Math.max(0, segIndex - 1));
        Camera p1 = path.get(segIndex);
        Camera p2 = path.get(Math.min(path.size() - 1, segIndex + 1));
        Camera p3 = path.get(Math.min(path.size() - 1, segIndex + 2));

        World world = p1.toLocation() != null ? p1.toLocation().getWorld() : null;
        if (world == null) {
            stop(true);
            return;
        }

        double x = Interpolation.catmullRom(p0.x(), p1.x(), p2.x(), p3.x(), eased);
        double y = Interpolation.catmullRom(p0.y(), p1.y(), p2.y(), p3.y(), eased);
        double z = Interpolation.catmullRom(p0.z(), p1.z(), p2.z(), p3.z(), eased);
        float yaw = Interpolation.lerpAngle(p1.yaw(), p2.yaw(), eased);
        float pitch = Interpolation.lerpAngle(p1.pitch(), p2.pitch(), eased);

        Location target = new Location(world, x, y, z, yaw, pitch);
        player.teleport(target);
        tick++;
    }
}
