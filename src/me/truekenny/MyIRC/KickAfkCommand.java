package me.truekenny.MyIRC;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class KickAfkCommand implements CommandExecutor {
    /**
     * Экземпляр плагина
     */
    private final MyIRC myirc;

    /**
     * Конструктор
     *
     * @param myirc Экземпляр плагина
     */
    public KickAfkCommand(MyIRC myirc) {
        this.myirc = myirc;
    }

    /**
     * Обрабатывает команду /kickafk
     *
     * @param sender  Отправитель команды
     * @param command ?
     * @param label   ?
     * @param split   Параметры команды
     * @return Результат
     */
    public boolean onCommand(CommandSender sender, Command command, String label, String[] split) {
        if ((sender instanceof Player) && (!sender.isOp())) {

            return false;
        }

        int timeout = 300;
        if (split.length == 1) {
            timeout = Integer.parseInt(split[0]);
        }

        for (Player player : myirc.getOnlinePlayers()) {
            PlayerData playerData = Players.getPlayerData(player);

            long playerIdle = System.currentTimeMillis() / 1000L - playerData.timeIdle;
            if (playerIdle > timeout) {
                player.kickPlayer("AFK more than " + timeout + " seconds");
                myirc.log.info(player.getName() + " kicked by kickafk (timeIdle: " + playerIdle + ")");
            }

        }

        return true;
    }

}
