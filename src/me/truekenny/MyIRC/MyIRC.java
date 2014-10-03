package me.truekenny.MyIRC;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.dynmap.DynmapCommonAPI;

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
    Map<String, String> rewriteHosts;

    /**
     * Скрытые игроки в чате
     */
    String[] hiddenGamers;

    private DynmapCommonAPI dynmapAPI;

    /**
     * Ping-задание
     */
    private int taskPingId;

    /**
     * Активация плагина
     */
    @Override
    public void onEnable() {
        defaultConfig();

        playerListener = new PlayerListener(this);

        // Активирую прослушивание событий пользователя
        PluginManager pm = getServer().getPluginManager();

        Plugin dynmap = pm.getPlugin("dynmap");
        dynmapAPI = (DynmapCommonAPI) dynmap;

        pm.registerEvents(playerListener, this);
        pm.registerEvents(new DynmapListener(this), this);

        ircServer = IRCServer.Activate(this);

        getCommand("irc").setExecutor(new IrcCommand(this));

        TabCompleter tabCompleter = new PrivateTabCompleter(this);

        getCommand("w").setTabCompleter(tabCompleter);
        getCommand("tell").setTabCompleter(tabCompleter);

        taskPingId = getServer().getScheduler().scheduleSyncRepeatingTask(this, new PingTask(this), 20, 20 * config.getLong("irc.time.ping"));

        log.info(config.getString("messages.console.onEnable"));
    }

    /**
     * Деактивация плагина
     */
    @Override
    public void onDisable() {
        ircServer.Deactivate();
        getServer().getScheduler().cancelTask(taskPingId);

        log.info(config.getString("messages.console.onDisable"));
    }

    /**
     * Работа с конфигурацией
     */
    public void defaultConfig() {
        config = getConfig();

        config.addDefault("irc.host", "irc.example.com");
        config.addDefault("irc.gameHost", "server.example.com");
        config.addDefault("irc.port", 6667);
        config.addDefault("irc.channel", "#minecraft");
        config.addDefault("irc.creator", "Creator");
        config.addDefault("irc.topic", "Welcome to MyIRC channel");

        config.addDefault("irc.time.ping", 45);
        config.addDefault("irc.time.timeout", 180);

        config.addDefault("messages.console.onEnable", "MyIRC loaded!");
        config.addDefault("messages.console.onDisable", "MyIRC disabled!");
        config.addDefault("messages.console.playerListener", "PlayerListener loaded!");

        config.addDefault("messages.irc.erroneusNickname", "Erroneus Nickname");
        config.addDefault("messages.irc.nicknameInUse", "Nickname is already in use");
        // config.addDefault("messages.irc.privateOff", "Private messages under construction");
        config.addDefault("messages.irc.kickOnSameNick", "Someone came into the game with your nickname");
        config.addDefault("messages.irc.noSuchNick", "No such nick");

        config.addDefault("messages.game.list", "IRC users");

        config.addDefault("messages.game.noSuchNick", "No such nick");

        config.addDefault("rules.rewrite.hosts", "google.com:hide,yahoo.com:microsoft.com");
        config.addDefault("rules.hide.gamers", "admin,OpeRaToR");

        config.addDefault("irc.operPassword", String.valueOf(Math.round(Math.random() * Integer.MAX_VALUE)));

        config.options().copyDefaults(true);
        saveConfig();

        rewriteHosts = Splitter.on(',').withKeyValueSeparator(":")
                .split(config.getString("rules.rewrite.hosts"));

        hiddenGamers = config.getString("rules.hide.gamers").split(",");
    }

    /**
     * Возвращает список пользователей игры
     */
    public ArrayList<String> userList() {
        ArrayList<String> users = new ArrayList<String>();
        for (Player player : getOnlinePlayers()) {
            //Location playerLocation = player.getLocation();
            //log.info(player.getName() + ": " + playerLocation.getBlockX() + "/" + playerLocation.getBlockZ());

            if (isHiddenGamer(player.getName())) {

                continue;
            }

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
        for (String ingameNick : userList()) {
            if (nick.equalsIgnoreCase(ingameNick) && isHiddenGamer(ingameNick) == false) return false;
        }

        for (String inchatNick : ircServer.userList()) {
            if (nick.equalsIgnoreCase(inchatNick)) return false;
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
        for (Map.Entry<String, String> entry : rewriteHosts.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            host = host.replaceAll(key, value);
        }

        return host;
    }

    /**
     * Отправляет личное сообщение
     *
     * @param message Сообщение
     * @param from    Имя игрока
     * @param to      Имя игрока
     * @return Имя игрока в правильном регистре
     */
    public String sendPrivate(String message, String from, String to) {
        for (Player destPlayer : getOnlinePlayers()) {
            if (to.equalsIgnoreCase(destPlayer.getName())) {
                // Игрок из игры отправляет сообщение игроку из игры
                destPlayer.sendMessage(ChatColor.LIGHT_PURPLE + "<" + from + "> " + message);

                return destPlayer.getName();
            }
        }

        return null;
    }

    /**
     * Скрытый игрок
     *
     * @param gamer Имя игрока
     * @return Результат
     */
    public boolean isHiddenGamer(String gamer) {
        for (String hiddenGamer : hiddenGamers) {
            if (hiddenGamer.equalsIgnoreCase(gamer)) {

                return true;
            }
        }

        return false;
    }

    /**
     * Отправить сообщение на карту
     *
     * @param nick
     * @param message
     */
    public void sendMessageToDynmap(String nick, String message) {
        if (dynmapAPI == null) {

            return;
        }
        dynmapAPI.sendBroadcastToWeb("IRC: " + nick, message);
    }
}
