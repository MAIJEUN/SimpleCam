package kr.maijsoft.simplecam.camera;

import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Scene {

    private final String name;
    private final List<Shot> shots = new ArrayList<>();

    public Scene(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    public List<Shot> shots() {
        return Collections.unmodifiableList(shots);
    }

    public void addShot(String cameraName, double seconds) {
        shots.add(new Shot(cameraName, seconds));
    }

    public boolean removeShot(int index) {
        if (index < 0 || index >= shots.size()) return false;
        shots.remove(index);
        return true;
    }

    public void save(ConfigurationSection section) {
        List<Object> raw = new ArrayList<>();
        for (Shot shot : shots) {
            ConfigurationSection s = section.createSection("__tmp");
            s.set("camera", shot.cameraName());
            s.set("seconds", shot.seconds());
            raw.add(s.getValues(false));
        }
        section.set("__tmp", null);
        section.set("shots", raw);
    }

    @SuppressWarnings("unchecked")
    public static Scene load(String name, ConfigurationSection section) {
        Scene scene = new Scene(name);
        List<?> raw = section.getList("shots");
        if (raw == null) return scene;
        for (Object o : raw) {
            if (!(o instanceof java.util.Map<?, ?> map)) continue;
            Object cam = map.get("camera");
            Object sec = map.get("seconds");
            if (cam == null) continue;
            double seconds = sec instanceof Number n ? n.doubleValue() : 4.0;
            scene.addShot(cam.toString(), seconds);
        }
        return scene;
    }

    public record Shot(String cameraName, double seconds) { }
}
