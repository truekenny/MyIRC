package me.truekenny.MyIRC;

import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * Главный класс плагина
 *
 * @author truekenny
 */
public class MyIRC extends JavaPlugin {
    /**
     * Экземпляр для прослушивания событий пользователя
     */
    private final PlayerListener playerListener = new PlayerListener(this);

    public Server server;

    /**
     * Объект для логирования сообщений плагина
     */
    Logger log = Logger.getLogger("Minecraft");

    /**
     * Активация плагина
     */
    @Override
    public void onEnable() {
        // Активирую прослушивание событий пользователя
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(playerListener, this);

        server = Server.Activate(this);

        log.info("MyIRC загружен!");
    }

    /**
     * Деактивация плагина
     */
    @Override
    public void onDisable() {
        server.Deactivate();

        log.info("MyIRC отключен.");
    }

    /**
     * Возвращает список пользователей игры
     */
    public ArrayList<String> userList() {
        ArrayList<String> users = new ArrayList<String>();
        for (Player player : getOnlinePlayers()) {
            //Location playerLocation = player.getLocation();
            //log.info(player.getName() + ": " + playerLocation.getBlockX() + "/" + playerLocation.getBlockZ());
            users.add(player.getName());
        }
        return users;
    }

    /**
     * @return Player[] Возвращает список игроков
     */
    public static List<Player> getOnlinePlayers() {
        List<Player> list = Lists.newArrayList();
        for (World world : Bukkit.getWorlds()) {
            list.addAll(world.getPlayers());
        }
        return Collections.unmodifiableList(list);
    }

    /**
     * Возвращает флаг уникальности ника
     * @param nick
     * @return
     */
    public boolean isUniqueNick(String nick) {
        nick = nick.toLowerCase();

        for (String ingameNick : userList()) {
            if(nick.equals(ingameNick.toLowerCase())) return false;
        }

        for (String inchatNick : server.userList()) {
            if(nick.equals(inchatNick.toLowerCase())) return false;
        }

        return true;
    }
}
