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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.*;
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

    private String fileAuthMe = "plugins/AuthMe/auths.db";

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

        if (checkAuthMe()) {
            log.info(config.getString("messages.console.authMeFound"));
        }

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

        config.addDefault("debug", false);

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
        config.addDefault("messages.console.authMeFound", "MyIRC - AuthMe support enabled (only default SHA256)!");

        config.addDefault("messages.irc.erroneusNickname", "Erroneus Nickname");
        config.addDefault("messages.irc.nicknameInUse", "Nickname is already in use");
        config.addDefault("messages.irc.kickOnSameNick", "Someone came into the game with your nickname");
        config.addDefault("messages.irc.noSuchNick", "No such nick");

        config.addDefault("messages.irc.passwordAccepted", "Password accepted");
        config.addDefault("messages.irc.passwordWrong", "Wrong password");
        config.addDefault("messages.irc.unAuthorized", "You are not logged. Use the command /PASS [password]. Message was not delivered.");
        config.addDefault("messages.irc.unAuthorized2", "Usually configured to connect to the chat there is a field for the password for automatic login.");

        config.addDefault("messages.irc.awayOn", "You have been marked as being away");
        config.addDefault("messages.irc.awayOff", "You are no longer marked as being away");

        config.addDefault("messages.game.list", "IRC users");

        config.addDefault("messages.game.noSuchNick", "No such nick");

        config.addDefault("messages.game.away", "is away:");

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

    /**
     * Проверяет наличие файла authMe
     *
     * @return
     */
    public boolean checkAuthMe() {
        File f = new File(fileAuthMe);
        return f.exists();
    }

    /**
     * Возвращет строку для указанного ника из AuthMe
     *
     * @param nick
     * @return
     */
    public String getNickLineAuthMe(String nick) {

        BufferedReader br = null;

        try {

            String sCurrentLine;

            br = new BufferedReader(new FileReader(fileAuthMe));

            while ((sCurrentLine = br.readLine()) != null) {

                if (sCurrentLine.toLowerCase().startsWith(nick.toLowerCase() + ":")) {
                    return sCurrentLine;
                }

            }

        } catch (IOException e) {
            log.info("File " + fileAuthMe + " not found");
        } finally {
            try {
                if (br != null) br.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        return null;
    }

    /**
     * Проверяет пароль пользователя
     *
     * @param nick
     * @param pass
     * @return
     */
    public boolean checkAuthMePass(String nick, String pass) {
        log.info("checkAuthMePass: " + nick + ", " + pass);

        String nickLine = getNickLineAuthMe(nick);

        if (nickLine == null) {

            // Если ник не зарегистрирован, то считать авторизованным
            return true;
        }

        StringTokenizer st = new StringTokenizer(nickLine);
        String nick_ = st.nextToken(":");
        String nothin = st.nextToken("$");
        String type = st.nextToken("$");
        String salt = st.nextToken("$");
        String hash = st.nextToken(":").substring(1);

        boolean result = hash.equalsIgnoreCase(sha256(sha256(pass) + salt));

        log.info(nick_ + "," + salt + "," + hash + ": " + result);

        return result;
    }

    /**
     * Возвращает SHA256 хэш
     *
     * @param in
     * @return
     */
    public String sha256(String in) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(in.getBytes("UTF-8"));

            return getHexString(md.digest());

        } catch (Exception e) {
        }

        return "fail";
    }

    /**
     * Преобразует массив байт в HEX
     *
     * @param b
     * @return
     * @throws Exception
     */
    public static String getHexString(byte[] b) throws Exception {
        String result = "";
        for (int i = 0; i < b.length; i++) {
            result +=
                    Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
        }
        return result;
    }

    /**
     * Возвращет полное имя по нику (игра+чат)
     * @param nick
     * @return
     */
    public String getFullNameBy(String nick) {
        Player player = getServer().getPlayer(nick);
        if (player != null) {

            return playerListener.getFullName(player);
        }

        String fullName = ircServer.getFullNameBy(nick);
        if (fullName != null) {

            return fullName;
        }

        return nick + "!offline@offline";
    }
}
