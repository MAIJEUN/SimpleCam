package com.maijsoft.simplecam;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class CameraCommand implements CommandExecutor, TabCompleter {
    private final CameraManager cameraManager;

    public CameraCommand(CameraManager cameraManager) {
        this.cameraManager = cameraManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("콘솔에서는 사용할 수 없습니다.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 2) {
            player.sendMessage("사용법: /cm <subcommand> <arguments>");
            return false;
        }

        String subcommand = args[0].toLowerCase();
        String cameraName = args[1];

        switch (subcommand) {
            case "pos":
                if (args.length < 3) {
                    player.sendMessage("사용법: /cm pos <카메라 이름> <인덱스>");
                    return false;
                }
                int index = Integer.parseInt(args[2]);
                cameraManager.setCameraPosition(cameraName, index, player.getLocation());
                player.sendMessage("카메라 '" + cameraName + "'의 " + index + "번째 위치 설정됨.");
                break;

            case "postime":
                if (args.length < 3) {
                    player.sendMessage("사용법: /cm postime <카메라 이름> <초>");
                    return false;
                }
                double time = Double.parseDouble(args[2]);
                cameraManager.setCameraTime(cameraName, time);
                player.sendMessage("카메라 '" + cameraName + "'의 이동 시간을 " + time + "초로 설정.");
                break;

            case "function":
                if (args.length < 3) {
                    player.sendMessage("사용법: /cm function <카메라 이름> <linear/easein/easeout>");
                    return false;
                }
                cameraManager.setCameraFunction(cameraName, args[2]);
                player.sendMessage("카메라 '" + cameraName + "'의 이동 함수를 '" + args[2] + "'으로 설정.");
                break;

            case "endtp":
                cameraManager.setEndTeleport(cameraName, player.getLocation());
                player.sendMessage("카메라 '" + cameraName + "'의 종료 이동 좌표 설정됨.");
                break;

            case "reset":
                cameraManager.resetCamera(cameraName);
                player.sendMessage("카메라 '" + cameraName + "'의 설정 초기화됨.");
                break;

            case "start":
                cameraManager.startCamera(cameraName, player);
                player.sendMessage("카메라 '" + cameraName + "' 시작됨.");
                break;

            case "gamemode":
                if (args.length < 3) {
                    player.sendMessage("사용법: /cm gamemode <카메라 이름> <게임모드>");
                    return false;
                }
                GameMode gameMode;
                try {
                    gameMode = GameMode.valueOf(args[2].toUpperCase());
                } catch (IllegalArgumentException e) {
                    player.sendMessage("잘못된 게임모드입니다.");
                    return false;
                }
                cameraManager.setCameraGameMode(cameraName, gameMode);
                player.sendMessage("카메라 '" + cameraName + "'의 게임모드를 '" + gameMode.name() + "'으로 설정.");
                break;

            default:
                player.sendMessage("알 수 없는 명령어입니다.");
                break;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return null;
        }

        List<String> completions = new ArrayList<>();
        Map<String, List<Location>> cameras = cameraManager.getAllCameras();
        List<String> cameraNames = new ArrayList<>(cameras.keySet());

        if (args.length == 1) {
            // 첫 번째 인자: 서브 명령어 목록
            completions.add("pos");
            completions.add("postime");
            completions.add("function");
            completions.add("endtp");
            completions.add("reset");
            completions.add("start");
            completions.add("gamemode");
        } else if (args.length == 2) {
            // 두 번째 인자: 카메라 이름 자동 완성
            return cameraNames.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 3) {
            switch (args[0].toLowerCase()) {
                case "pos":
                    // 세 번째 인자: 카메라 위치 인덱스
                    int size = cameras.containsKey(args[1]) ? cameras.get(args[1]).size() : 0;
                    completions.add(String.valueOf(size)); // 현재 마지막 인덱스 제안
                    completions.add(String.valueOf(size + 1)); // 새로 추가할 위치 인덱스 제안
                    break;

                case "postime":
                    // 세 번째 인자: 이동 시간 자동 완성
                    completions.add("1.0");
                    completions.add("2.5");
                    completions.add("5.0");
                    completions.add("10.0");
                    break;

                case "function":
                    // 세 번째 인자: 함수 자동 완성
                    completions.add("linear");
                    completions.add("easein");
                    completions.add("easeout");
                    completions.add("easeinout");
                    completions.add("elasticin");
                    completions.add("elasticout");
                    completions.add("elasticinout");
                    break;

                case "gamemode":
                    // 세 번째 인자: 게임모드 자동 완성
                    completions.add("SURVIVAL");
                    completions.add("CREATIVE");
                    completions.add("ADVENTURE");
                    completions.add("SPECTATOR");
                    break;
            }
        }

        // 자동 완성 필터링
        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
    }
}