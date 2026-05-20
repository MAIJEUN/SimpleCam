package kr.maijsoft.simplecam.camera;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;

import java.util.Objects;
import java.util.UUID;

public final class Camera {

    private final String name;
    private UUID worldId;
    private double x, y, z;
    private float yaw, pitch;

    public Camera(String name, Location loc) {
        this.name = name;
        apply(loc);
    }

    private Camera(String name, UUID worldId, double x, double y, double z, float yaw, float pitch) {
        this.name = name;
        this.worldId = worldId;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public String name() {
        return name;
    }

    public Location toLocation() {
        World w = Bukkit.getWorld(worldId);
        if (w == null) return null;
        return new Location(w, x, y, z, yaw, pitch);
    }

    public UUID worldId() { return worldId; }
    public double x() { return x; }
    public double y() { return y; }
    public double z() { return z; }
    public float yaw() { return yaw; }
    public float pitch() { return pitch; }

    public void apply(Location loc) {
        Objects.requireNonNull(loc.getWorld(), "Camera location requires a world.");
        this.worldId = loc.getWorld().getUID();
        this.x = loc.getX();
        this.y = loc.getY();
        this.z = loc.getZ();
        this.yaw = loc.getYaw();
        this.pitch = loc.getPitch();
    }

    public void save(ConfigurationSection section) {
        section.set("world", worldId.toString());
        section.set("x", x);
        section.set("y", y);
        section.set("z", z);
        section.set("yaw", yaw);
        section.set("pitch", pitch);
    }

    public static Camera load(String name, ConfigurationSection section) {
        String worldStr = section.getString("world");
        if (worldStr == null) return null;
        UUID worldId;
        try {
            worldId = UUID.fromString(worldStr);
        } catch (IllegalArgumentException e) {
            return null;
        }
        return new Camera(
                name,
                worldId,
                section.getDouble("x"),
                section.getDouble("y"),
                section.getDouble("z"),
                (float) section.getDouble("yaw"),
                (float) section.getDouble("pitch")
        );
    }

    public ConfigurationSection serialize() {
        ConfigurationSection s = new MemoryConfiguration();
        save(s);
        return s;
    }
}
