package com.maijsoft.simplecam;

import org.bukkit.plugin.java.JavaPlugin;

public class CameraPlugin extends JavaPlugin {
    private static CameraPlugin instance;

    @Override
    public void onEnable() {
        instance = this;
        getCommand("cm").setExecutor(new CameraCommand(new CameraManager()));
    }

    @Override
    public void onDisable() {
        // 플러그인 종료 시 처리할 작업들
    }

    public static CameraPlugin getInstance() {
        return instance;
    }
}
