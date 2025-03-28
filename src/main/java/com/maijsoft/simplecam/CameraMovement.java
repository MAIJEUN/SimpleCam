package com.maijsoft.simplecam;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class CameraMovement {
    private final Player player;
    private final List<Location> positions;
    private final double duration;  // 카메라 이동 시간
    private final String function;  // 보간 함수 (linear, easein, easeout 등)
    private final Location endTeleport; // 이동 후 종료 위치
    private int tick = 0;  // 이동 시간의 tick
    private int totalTicks;  // 총 이동할 tick 수

    public CameraMovement(Player player, List<Location> positions, double duration, String function, Location endTeleport) {
        this.player = player;
        this.positions = positions;
        this.duration = duration;
        this.function = function.toLowerCase();
        this.endTeleport = endTeleport;
        this.totalTicks = (int) (duration * 20); // 20 ticks = 1초 (초당 20회 이동)
    }

    // 카메라 이동 시작
    public void start() {
        if (positions.size() < 2) {
            player.sendMessage("카메라는 최소 두 개의 위치가 필요합니다.");
            return;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                if (tick >= totalTicks) {
                    // 이동이 끝나면 종료 위치로 텔레포트
                    if (endTeleport != null) {
                        player.teleport(endTeleport);
                    }
                    cancel();  // 이동 종료
                    return;
                }

                double t = (double) tick / totalTicks;  // 시간 비율 (0 ~ 1 사이)
                double progress = applyEasingFunction(t); // 보간 함수 적용

                // 현재 진행 상황에 맞는 카메라 위치 계산
                Location interpolatedLocation = interpolate(progress);
                player.teleport(interpolatedLocation);  // 계산된 위치로 이동

                tick++;
            }
        }.runTaskTimer(CameraPlugin.getInstance(), 0L, 1L); // SimpleCamPlugin 인스턴스를 통해 작업 실행
    }

    // 보간 함수 적용 (easein, easeout, 등)
    private double applyEasingFunction(double t) {
        switch (function) {
            case "easein":
                return t * t;  // 시작은 느리게
            case "easeout":
                return 1 - Math.pow(1 - t, 2);  // 끝은 느리게
            case "easeinout":
                return t < 0.5 ? 2 * t * t : 1 - Math.pow(-2 * t + 2, 2) / 2;  // 양쪽 끝은 느리게, 가운데 빠르게
            case "elasticin":
                return Math.sin(13 * Math.PI / 2 * t) * Math.pow(2, 10 * (t - 1));  // elastic 효과
            case "elasticout":
                return Math.sin(-13 * Math.PI / 2 * (t + 1)) * Math.pow(2, -10 * t) + 1;  // elastic 효과
            case "elasticinout":
                return t < 0.5
                        ? -(Math.pow(2, 10 * (2 * t - 1)) * Math.sin((2 * t - 1.1) * 5 * Math.PI)) / 2
                        : (Math.pow(2, -10 * (2 * t - 1)) * Math.sin((2 * t - 1.1) * 5 * Math.PI)) / 2 + 1;  // elastic 효과
            default:
                return t;  // linear (기본값)
        }
    }

    // 현재 진행 상황에 맞는 위치 계산
    private Location interpolate(double t) {
        // t 값이 0에서 1 사이로 변하는 동안 위치를 보간
        int index = (int) (t * (positions.size() - 1));  // 두 번째 위치 인덱스
        double localT = (t * (positions.size() - 1)) - index;  // 0~1 사이의 비율로 두 위치 사이 보간

        // 두 위치 간의 보간
        Location start = positions.get(index);
        Location end = positions.get(Math.min(index + 1, positions.size() - 1));

        double x = start.getX() + (end.getX() - start.getX()) * localT;
        double y = start.getY() + (end.getY() - start.getY()) * localT;
        double z = start.getZ() + (end.getZ() - start.getZ()) * localT;
        float yaw = start.getYaw() + (end.getYaw() - start.getYaw()) * (float) localT;
        float pitch = start.getPitch() + (end.getPitch() - start.getPitch()) * (float) localT;

        return new Location(start.getWorld(), x, y, z, yaw, pitch);
    }
}
