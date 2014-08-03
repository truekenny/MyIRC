package me.truekenny.MyIRC;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;

import java.util.StringTokenizer;
import java.util.logging.Logger;

/**
 * Класс-слушатель событий происходящих с пользователем
 *
 * @author truekenny
 */
@SuppressWarnings("deprecation")
public class PlayerListener implements Listener {
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
    public PlayerListener(MyIRC instance) {
        myIRC = instance;
        log.info(myIRC.config.getString("messages.console.playerListener"));
    }

    /**
     * Обрабытывает вход пользователя
     *
     * @param event Событие
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // log.info(event.getPlayer().getName() + " вошел в игру.");
        myIRC.ircServer.kick(event.getPlayer().getName());

        myIRC.ircServer.join("-1", getFullName(event));
        myIRC.ircServer.mode("-1", event.getPlayer().getName());
    }

    /**
     * Обрабатывает выход пользователя
     *
     * @param event Событие
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // log.info(event.getPlayer().getName() + " вышел из игры.");
        myIRC.ircServer.part("-1", getFullName(event), "Gamer quit");

        myIRC.ircServer.backNick(event.getPlayer().getName());
    }

    /**
     * Обрабатывает сообщения пользователя
     *
     * @param event Событие
     */
    @EventHandler
    public void onPlayerChat(PlayerChatEvent event) {
        // log.info(event.getPlayer().getName() + " написал «" + event.getMessage() + "»");
        myIRC.ircServer.privmsg("-1", getFullName(event), event.getMessage());
    }

    /**
     * Возвращает полное имя игрока
     *
     * @param event Событие
     * @return Полное имя игрока
     */
    public String getFullName(PlayerEvent event) {
        return event.getPlayer().getName() + "!ingame@" + myIRC.host(event.getPlayer().getAddress().getHostName());
    }

    /**
     * Обрабытывает отправку личных сообщений игроков
     *
     * @param event
     */
    @EventHandler
    public void onPreCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();

        StringTokenizer st = new StringTokenizer(event.getMessage());
        if (!st.hasMoreTokens()) return;
        String command = st.nextToken().trim();
        if (!st.hasMoreTokens()) return;
        String dest = st.nextToken().trim();
        if (!st.hasMoreTokens()) return;
        String message = st.nextToken(IRCClient.CRLF).trim();


        if (command.equalsIgnoreCase("/tell") || command.equalsIgnoreCase("/w")) {
            event.setCancelled(true);

            String destCase;

            if ((destCase = myIRC.sendPrivate(message, player.getName(), dest)) != null) {
                player.sendMessage(ChatColor.BLUE + "<" + destCase + "> " + message);

                return;
            }

            if ((destCase = myIRC.ircServer.sendPrivate(message, player.getName(), dest)) != null) {
                player.sendMessage(ChatColor.BLUE + "<" + destCase + "> " + message);

                return;
            }

            player.sendMessage(ChatColor.RED + "<" + dest + "> " + myIRC.config.getString("messages.game.noSuchNick"));
        }
    }
}
