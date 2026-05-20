package kr.maijsoft.simplecam.manager;

import kr.maijsoft.simplecam.SimpleCam;
import kr.maijsoft.simplecam.camera.Camera;
import kr.maijsoft.simplecam.camera.Scene;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class CameraStore {

    private final SimpleCam plugin;
    private final File file;
    private final Map<String, Camera> cameras = new LinkedHashMap<>();
    private final Map<String, Scene> scenes = new LinkedHashMap<>();

    public CameraStore(SimpleCam plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "cameras.yml");
    }

    public void load() {
        cameras.clear();
        scenes.clear();
        if (!file.exists()) return;
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);

        ConfigurationSection camSection = yaml.getConfigurationSection("cameras");
        if (camSection != null) {
            for (String key : camSection.getKeys(false)) {
                ConfigurationSection s = camSection.getConfigurationSection(key);
                if (s == null) continue;
                Camera cam = Camera.load(key, s);
                if (cam != null) cameras.put(key.toLowerCase(), cam);
            }
        }

        ConfigurationSection sceneSection = yaml.getConfigurationSection("scenes");
        if (sceneSection != null) {
            for (String key : sceneSection.getKeys(false)) {
                ConfigurationSection s = sceneSection.getConfigurationSection(key);
                if (s == null) continue;
                scenes.put(key.toLowerCase(), Scene.load(key, s));
            }
        }
    }

    public void save() {
        YamlConfiguration yaml = new YamlConfiguration();
        ConfigurationSection camSection = yaml.createSection("cameras");
        for (Camera cam : cameras.values()) {
            cam.save(camSection.createSection(cam.name()));
        }
        ConfigurationSection sceneSection = yaml.createSection("scenes");
        for (Scene scene : scenes.values()) {
            scene.save(sceneSection.createSection(scene.name()));
        }
        try {
            if (!plugin.getDataFolder().exists() && !plugin.getDataFolder().mkdirs()) {
                plugin.getLogger().warning("Failed to create plugin data folder.");
            }
            yaml.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save cameras.yml: " + e.getMessage());
        }
    }

    public Camera getCamera(String name) {
        return cameras.get(name.toLowerCase());
    }

    public Collection<Camera> cameras() {
        return Collections.unmodifiableCollection(cameras.values());
    }

    public boolean addCamera(Camera camera) {
        String key = camera.name().toLowerCase();
        if (cameras.containsKey(key)) return false;
        cameras.put(key, camera);
        return true;
    }

    public boolean removeCamera(String name) {
        return cameras.remove(name.toLowerCase()) != null;
    }

    public Scene getScene(String name) {
        return scenes.get(name.toLowerCase());
    }

    public Collection<Scene> scenes() {
        return Collections.unmodifiableCollection(scenes.values());
    }

    public boolean addScene(Scene scene) {
        String key = scene.name().toLowerCase();
        if (scenes.containsKey(key)) return false;
        scenes.put(key, scene);
        return true;
    }

    public boolean removeScene(String name) {
        return scenes.remove(name.toLowerCase()) != null;
    }
}
