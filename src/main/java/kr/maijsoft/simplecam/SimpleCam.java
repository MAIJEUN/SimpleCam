package kr.maijsoft.simplecam;

import kr.maijsoft.simplecam.command.CameraCommand;
import kr.maijsoft.simplecam.command.SceneCommand;
import kr.maijsoft.simplecam.gui.GuiListener;
import kr.maijsoft.simplecam.manager.CameraStore;
import kr.maijsoft.simplecam.manager.PlaybackManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class SimpleCam extends JavaPlugin {

    private CameraStore store;
    private PlaybackManager playback;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.store = new CameraStore(this);
        this.store.load();

        this.playback = new PlaybackManager(this, store);

        CameraCommand cameraCommand = new CameraCommand(this, store);
        Objects.requireNonNull(getCommand("camera")).setExecutor(cameraCommand);
        Objects.requireNonNull(getCommand("camera")).setTabCompleter(cameraCommand);

        SceneCommand sceneCommand = new SceneCommand(this, store, playback);
        Objects.requireNonNull(getCommand("scene")).setExecutor(sceneCommand);
        Objects.requireNonNull(getCommand("scene")).setTabCompleter(sceneCommand);

        getServer().getPluginManager().registerEvents(new GuiListener(this, store, playback), this);

        getLogger().info("SimpleCam " + getPluginMeta().getVersion() + " enabled.");
    }

    @Override
    public void onDisable() {
        if (playback != null) playback.stopAll();
        if (store != null) store.save();
    }

    public CameraStore store() {
        return store;
    }

    public PlaybackManager playback() {
        return playback;
    }
}
