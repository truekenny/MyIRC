package me.truekenny.MyIRC;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WhoisCommand implements CommandExecutor {
    /**
     * Экземпляр плагина
     */
    private final MyIRC myirc;

    /**
     * Конструктор
     *
     * @param myirc Экземпляр плагина
     */
    public WhoisCommand(MyIRC myirc) {
        this.myirc = myirc;
    }

    /**
     * Обрабатывает команду /whois
     *
     * @param sender  Отправитель команды
     * @param command ?
     * @param label   ?
     * @param split   Параметры команды
     * @return Результат
     */
    public boolean onCommand(CommandSender sender, Command command, String label, String[] split) {
        if (!(sender instanceof Player)) {
            return false;
        }
        Player player = (Player) sender;

        if (split.length != 1) {

            return false;
        }

        myirc.ircServer.whois(player, split[0]);

        return true;
    }

}
