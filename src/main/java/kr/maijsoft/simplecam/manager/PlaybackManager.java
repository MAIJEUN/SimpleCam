package kr.maijsoft.simplecam.manager;

import kr.maijsoft.simplecam.SimpleCam;
import kr.maijsoft.simplecam.camera.Scene;
import kr.maijsoft.simplecam.playback.PlaybackSession;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class PlaybackManager {

    private final SimpleCam plugin;
    private final CameraStore store;
    private final Map<UUID, PlaybackSession> sessions = new HashMap<>();

    public PlaybackManager(SimpleCam plugin, CameraStore store) {
        this.plugin = plugin;
        this.store = store;
    }

    public boolean isPlaying(Player player) {
        return sessions.containsKey(player.getUniqueId());
    }

    public PlaybackSession start(Player player, Scene scene) {
        stop(player);
        PlaybackSession session = new PlaybackSession(plugin, store, player, scene);
        if (!session.isPlayable()) return null;
        sessions.put(player.getUniqueId(), session);
        session.onFinish(s -> sessions.remove(s.player().getUniqueId(), s));
        session.start();
        return session;
    }

    public boolean stop(Player player) {
        PlaybackSession session = sessions.remove(player.getUniqueId());
        if (session == null) return false;
        session.stop(true);
        return true;
    }

    public void stopAll() {
        for (PlaybackSession session : sessions.values()) {
            session.stop(true);
        }
        sessions.clear();
    }
}
