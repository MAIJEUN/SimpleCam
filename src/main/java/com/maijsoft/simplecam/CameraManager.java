package com.maijsoft.simplecam;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class CameraManager {
    private final Map<String, List<Location>> cameraPositions = new HashMap<>();
    private final Map<String, Double> cameraTimes = new HashMap<>();
    private final Map<String, String> cameraFunctions = new HashMap<>();
    private final Map<String, Location> endTeleport = new HashMap<>();
    private final Map<String, GameMode> cameraGameModes = new HashMap<>();
    private final Map<String, GameMode> originalGameModes = new HashMap<>();

    // 카메라 위치 설정
    public void setCameraPosition(String cameraName, int index, Location location) {
        cameraPositions.computeIfAbsent(cameraName, k -> new ArrayList<>());
        List<Location> positions = cameraPositions.get(cameraName);
        while (positions.size() <= index) {
            positions.add(null);
        }
        positions.set(index, location);
    }

    // 카메라 시간 설정
    public void setCameraTime(String cameraName, double time) {
        cameraTimes.put(cameraName, time);
    }

    // 카메라 이동 함수 설정
    public void setCameraFunction(String cameraName, String function) {
        cameraFunctions.put(cameraName, function);
    }

    // 카메라 종료 위치 설정
    public void setEndTeleport(String cameraName, Location location) {
        endTeleport.put(cameraName, location);
    }

    // 카메라 게임모드 설정
    public void setCameraGameMode(String cameraName, GameMode gameMode) {
        cameraGameModes.put(cameraName, gameMode);
    }

    // 카메라 게임모드 가져오기
    public GameMode getCameraGameMode(String cameraName) {
        return cameraGameModes.getOrDefault(cameraName, GameMode.SURVIVAL);
    }

    // 카메라 시작 전 사용자의 원래 게임모드 저장
    public void setOriginalGameMode(String cameraName, GameMode originalGameMode) {
        originalGameModes.put(cameraName, originalGameMode);
    }

    // 카메라 종료 후 원래 게임모드로 복구
    public GameMode getOriginalGameMode(String cameraName) {
        return originalGameModes.getOrDefault(cameraName, GameMode.SURVIVAL);
    }

    // 카메라 초기화
    public void resetCamera(String cameraName) {
        cameraPositions.remove(cameraName);
        cameraTimes.remove(cameraName);
        cameraFunctions.remove(cameraName);
        endTeleport.remove(cameraName);
        cameraGameModes.remove(cameraName);
        originalGameModes.remove(cameraName);
    }

    // 카메라 시작
    public void startCamera(String cameraName, Player player) {
        if (!cameraPositions.containsKey(cameraName)) {
            player.sendMessage("카메라 '" + cameraName + "'을 찾을 수 없습니다.");
            return;
        }

        List<Location> positions = cameraPositions.get(cameraName);
        double time = cameraTimes.getOrDefault(cameraName, 5.0);
        String function = cameraFunctions.getOrDefault(cameraName, "linear");

        // 게임모드 변경
        GameMode originalGameMode = player.getGameMode();
        setOriginalGameMode(cameraName, originalGameMode); // 원래 게임모드 저장
        player.setGameMode(getCameraGameMode(cameraName)); // 새로운 게임모드 설정

        // 카메라 애니메이션 시작
        new CameraMovement(player, positions, time, function, endTeleport.get(cameraName)).start();

        // 카메라 끝날 때 원래 게임모드로 복구
        new BukkitRunnable() {
            @Override
            public void run() {
                player.setGameMode(getOriginalGameMode(cameraName)); // 원래 게임모드로 복구
            }
        }.runTaskLater(CameraPlugin.getPlugin(CameraPlugin.class), (long) (time * 20)); // 카메라 시간 후 원래 게임모드 복구
    }

    // 카메라 목록 가져오기
    public Map<String, List<Location>> getAllCameras() {
        return cameraPositions;
    }
}
