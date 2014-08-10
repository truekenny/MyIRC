package me.truekenny.MyIRC;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public class PrivateTabCompleter implements TabCompleter {

    /**
     * Экземпляр главного класса плагина
     */
    private final MyIRC myIRC;

    /**
     * Объект для логирования сообщений плагина
     */
    Logger log = Logger.getLogger("Minecraft");

    /**
     * Сохранение объекта главного класса
     *
     * @param instance Экземпляр плагина
     */
    public PrivateTabCompleter(MyIRC instance) {
        myIRC = instance;
    }

    /**
     * Автокомплит ников
     *
     * @param sender Кто
     * @param cmd ?
     * @param alias Команда
     * @param args Параметры до нажатия TAB
     * @return Список 
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if(!alias.equalsIgnoreCase("w") && !alias.equalsIgnoreCase("tell")) {

            return null;
        }

        List<String> nicks = new ArrayList<String>();

        String prefix = (args.length == 1) ? args[0].toLowerCase() : "";

        for (String nick : myIRC.userList()) {
            if (nick.toLowerCase().startsWith(prefix)) {
                nicks.add(nick);
            }
        }

        for (String nick : myIRC.ircServer.userList()) {
            if (nick.toLowerCase().startsWith(prefix)) {
                nicks.add(nick);
            }
        }

        Collections.sort(nicks);

        return nicks;
    }

}