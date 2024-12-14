package com.maijsoft.simplecam;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class Main extends JavaPlugin implements TabCompleter {
    private final Map<Integer, Location> startPositions = new HashMap<>();
    private final Map<Integer, Location> endPositions = new HashMap<>();
    private final Map<Integer, Double> durations = new HashMap<>();
    private final Map<Integer, Location> endTeleportPositions = new HashMap<>();
    private final Map<Integer, List<Location>> midPoints = new HashMap<>();
    private final Map<Player, GameMode> originalGameModes = new HashMap<>();

    @Override
    public void onEnable() {
        this.getCommand("cm").setExecutor((sender, command, label, args) -> {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Only players can use this command.");
                return true;
            }
            if (args.length < 2) {
                player.sendMessage("Invalid usage. Try /cm <command> <number> [options]");
                return true;
            }

            String subCommand = args[0].toLowerCase();
            int actionNumber;
            try {
                actionNumber = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                player.sendMessage("Action number must be an integer.");
                return true;
            }

            switch (subCommand) {
                case "startpos" -> {
                    startPositions.put(actionNumber, player.getLocation());
                    player.sendMessage("Start position for action " + actionNumber + " set.");
                }
                case "endpos" -> {
                    endPositions.put(actionNumber, player.getLocation());
                    player.sendMessage("End position for action " + actionNumber + " set.");
                }
                case "endtp" -> {
                    endTeleportPositions.put(actionNumber, player.getLocation());
                    player.sendMessage("End teleport position for action " + actionNumber + " set.");
                }
                case "midpos" -> {
                    midPoints.computeIfAbsent(actionNumber, k -> new ArrayList<>()).add(player.getLocation());
                    player.sendMessage("Midpoint added for action " + actionNumber + ".");
                }
                case "resetmidpos" -> {
                    midPoints.remove(actionNumber);
                    player.sendMessage("All midpoints for action " + actionNumber + " have been reset.");
                }
                case "postime" -> {
                    if (args.length < 3) {
                        player.sendMessage("Please specify the duration in seconds.");
                        return true;
                    }
                    try {
                        double duration = Double.parseDouble(args[2]);
                        durations.put(actionNumber, duration);
                        player.sendMessage("Duration for action " + actionNumber + " set to " + duration + " seconds.");
                    } catch (NumberFormatException e) {
                        player.sendMessage("Duration must be a valid number.");
                    }
                }
                case "start" -> {
                    if (args.length < 3) {
                        player.sendMessage("Please specify a movement function.");
                        return true;
                    }
                    String movementFunction = args[2].toLowerCase();
                    if (!List.of("linear", "easein", "easeout", "easeinout").contains(movementFunction)) {
                        player.sendMessage("Invalid movement function. Choose from: linear, easein, easeout, easeinout.");
                        return true;
                    }
                    if (!startPositions.containsKey(actionNumber) || !endPositions.containsKey(actionNumber) || !durations.containsKey(actionNumber)) {
                        player.sendMessage("Action " + actionNumber + " is not fully configured.");
                        return true;
                    }
                    startCamera(player, actionNumber, movementFunction);
                }
                case "showpos" -> {
                    showParticles(player, actionNumber);
                }
                default -> player.sendMessage("Unknown subcommand: " + subCommand);
            }

            return true;
        });
        this.getCommand("cm").setTabCompleter(this);
    }

    private void showParticles(Player player, int actionNumber) {
        spawnParticles(actionNumber);
        player.sendMessage("Particles are now visible for action " + actionNumber);
    }

    private void spawnParticles(int actionNumber) {
        List<Location> positions = getPositions(actionNumber);
        for (Location loc : positions) {
            loc.getWorld().spawnParticle(Particle.GLOW, loc, 10, 0, 0, 0, 0);
        }
    }

    private List<Location> getPositions(int actionNumber) {
        List<Location> positions = new ArrayList<>();
        positions.add(startPositions.get(actionNumber));
        positions.addAll(midPoints.getOrDefault(actionNumber, Collections.emptyList()));
        positions.add(endPositions.get(actionNumber));
        return positions;
    }

    private void startCamera(Player player, int actionNumber, String movementFunction) {
        List<Location> path = new ArrayList<>();
        path.add(startPositions.get(actionNumber));
        path.addAll(midPoints.getOrDefault(actionNumber, Collections.emptyList()));
        path.add(endPositions.get(actionNumber));

        double duration = durations.get(actionNumber);
        Location endTeleport = endTeleportPositions.getOrDefault(actionNumber, path.get(path.size() - 1));
        originalGameModes.put(player, player.getGameMode());
        player.setGameMode(GameMode.SPECTATOR);
        player.teleport(path.get(0));

        new BukkitRunnable() {
            double t = 0.0;
            final double step = 1.0 / (duration * 20.0);
            int segment = 0;

            @Override
            public void run() {
                if (segment >= path.size() - 1) {
                    player.teleport(endTeleport);
                    player.sendMessage("Camera movement completed.");
                    GameMode originalGameMode = originalGameModes.remove(player);
                    if (originalGameMode != null) {
                        player.setGameMode(originalGameMode);
                    }
                    this.cancel();
                    return;
                }

                t += step;
                if (t >= 1.0) {
                    t = 0.0;
                    segment++;
                }

                if (segment < path.size() - 1) {
                    Location start = path.get(segment);
                    Location end = path.get(segment + 1);
                    double adjustedT = switch (movementFunction) {
                        case "easein" -> easeIn(t);
                        case "easeout" -> easeOut(t);
                        case "easeinout" -> easeInOutCubic(t);
                        default -> t;
                    };

                    double x = interpolate(start.getX(), end.getX(), adjustedT);
                    double y = interpolate(start.getY(), end.getY(), adjustedT);
                    double z = interpolate(start.getZ(), end.getZ(), adjustedT);
                    float yaw = (float) interpolate(start.getYaw(), end.getYaw(), adjustedT);
                    float pitch = (float) interpolate(start.getPitch(), end.getPitch(), adjustedT);

                    player.teleport(new Location(start.getWorld(), x, y, z, yaw, pitch));
                }
            }
        }.runTaskTimer(this, 0L, 1L);
    }

    private double interpolate(double start, double end, double t) {
        return start + (end - start) * t;
    }

    private double easeIn(double t) {
        return t * t;
    }

    private double easeOut(double t) {
        return 1.0 - Math.pow(1.0 - t, 2.0);
    }

    private double easeInOutCubic(double t) {
        return t < 0.5 ? 4.0 * t * t * t : 1.0 - Math.pow(-2.0 * t + 2.0, 3.0) / 2.0;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("cm")) {
            if (args.length == 1) {
                return filterResults(args[0], List.of("startpos", "endpos", "midpos", "resetmidpos", "postime", "start", "showpos"));
            } else if (args.length == 2) {
                if (args[0].equalsIgnoreCase("start")) {
                    // 행동 번호를 탭 완성으로 반환
                    return filterResults(args[1], List.of("1", "2", "3", "4", "5")); // 예시로 행동 번호 제시
                }
                return filterResults(args[1], List.of("1", "2", "3", "4", "5")); // 기본 행동 번호 제시
            } else if (args.length == 3 && args[0].equalsIgnoreCase("start")) {
                // start 명령어의 세 번째 인자로 동작 방식 제안
                return filterResults(args[2], List.of("linear", "easein", "easeout", "easeinout"));
            }
        }
        return Collections.emptyList();
    }


    private List<String> filterResults(String input, List<String> options) {
        List<String> results = new ArrayList<>();
        for (String option : options) {
            if (option.startsWith(input.toLowerCase())) {
                results.add(option);
            }
        }
        return results;
    }
}
