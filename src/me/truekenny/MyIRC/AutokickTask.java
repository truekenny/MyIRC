package me.truekenny.MyIRC;

import org.bukkit.entity.Player;

public class AutokickTask implements Runnable {

    public static MyIRC plugin;

    public AutokickTask(MyIRC instance) {
        plugin = instance;
    }

    public void run() {
        // plugin.log.info("Autokick…");

        for (Player player : plugin.getOnlinePlayers()) {
            PlayerData playerData = Players.getPlayerData(player);

            long playerIdle = System.currentTimeMillis() / 1000L - playerData.timeIdle;
            if (playerIdle > plugin.config.getInt("game.autokick.idle")) {
                player.kickPlayer("AFK more than " + plugin.config.getInt("game.autokick.idle") + " seconds");
                plugin.log.info(player.getName() + " kicked by autokick (timeIdle: " + playerIdle + ")");
            }

        }

        // plugin.log.info("Autokick complate");
    }

}