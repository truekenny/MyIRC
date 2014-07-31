package me.truekenny.MyIRC;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
    private PlayerListener playerListener;

    /**
     * Экземпляр IRC сервер
     */
    public IRCServer ircServer;

    /**
     * Экземпляр конфигурации
     */
    public FileConfiguration config;

    /**
     * Объект для логирования сообщений плагина
     */
    Logger log = Logger.getLogger("Minecraft");

    /**
     * Правила для хостов
     */
    Map<String, String> hostRules;

    /**
     * Активация плагина
     */
    @Override
    public void onEnable() {
        defaultConfig();

        playerListener = new PlayerListener(this);

        // Активирую прослушивание событий пользователя
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(playerListener, this);

        ircServer = IRCServer.Activate(this);

        getCommand("irc").setExecutor(new IrcCommand(this));

        log.info(config.getString("messages.console.onEnable"));
    }

    /**
     * Деактивация плагина
     */
    @Override
    public void onDisable() {
        ircServer.Deactivate();

        log.info(config.getString("messages.console.onDisable"));
    }

    /**
     * Работа с конфигурацией
     */
    public void defaultConfig() {
        config = getConfig();

        config.addDefault("irc.host", "irc.example.ru");
        config.addDefault("irc.gameHost", "server.example.ru");
        config.addDefault("irc.port", 6667);
        config.addDefault("irc.channel", "#minecraft");
        config.addDefault("irc.creator", "Creator");
        config.addDefault("irc.topic", "Welcome to MyIRC channel");

        config.addDefault("messages.console.onEnable", "MyIRC loaded!");
        config.addDefault("messages.console.onDisable", "MyIRC disabled!");
        config.addDefault("messages.console.playerListener", "PlayerListener loaded!");

        config.addDefault("messages.irc.erroneusNickname", "Erroneus Nickname");
        config.addDefault("messages.irc.nicknameInUse", "Nickname is already in use");
        config.addDefault("messages.irc.privateOff", "Private messages under construction");
        config.addDefault("messages.irc.kickOnSameNick", "Someone came into the game with your nickname");

        config.addDefault("messages.game.list", "IRC users");

        config.addDefault("rules.hide.hosts", "google.com:hide,yahoo.com:microsoft.com");

        config.options().copyDefaults(true);
        saveConfig();

        hostRules = Splitter.on(',').withKeyValueSeparator(":")
                .split(config.getString("rules.hide.hosts"));
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
     * @return Возвращает список игроков
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
     *
     * @param nick Имя
     * @return Результат уникальности имени
     */
    public boolean isUniqueNick(String nick) {
        nick = nick.toLowerCase();

        for (String ingameNick : userList()) {
            if (nick.equals(ingameNick.toLowerCase())) return false;
        }

        for (String inchatNick : ircServer.userList()) {
            if (nick.equals(inchatNick.toLowerCase())) return false;
        }

        return true;
    }

    /**
     * Выполняет фильтрацию и возвращает новый хост
     *
     * @param host Хост
     * @return Фильтрованный хост
     */
    public String host(String host) {
        for (Map.Entry<String, String> entry : hostRules.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            host = host.replaceAll(key, value);
        }

        return host;
    }
}
