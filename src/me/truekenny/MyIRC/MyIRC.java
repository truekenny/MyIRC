package me.truekenny.MyIRC;

import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

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

    private Server server;

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
    public String userList() {
        String users = "";
        for (Player player : getOnlinePlayers()) {
            //Location playerLocation = player.getLocation();
            //log.info(player.getName() + ": " + playerLocation.getBlockX() + "/" + playerLocation.getBlockZ());
            users += player.getName() + " ";
        }
        return users.trim();
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
}
