package me.truekenny.MyIRC;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

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
     * @param instance
     */
    public PlayerListener(MyIRC instance) {
        myIRC = instance;
        log.info(myIRC.config.getString("messages.console.playerListener"));
    }

    /**
     * Обрабытывает вход пользователя
     *
     * @param event
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
     * @param event
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // log.info(event.getPlayer().getName() + " вышел из игры.");
        myIRC.ircServer.part("-1", getFullName(event));
    }

    /**
     * Обрабатывает сообщения пользователя
     *
     * @param event
     */
    @EventHandler
    public void onPlayerChat(PlayerChatEvent event) {
        // log.info(event.getPlayer().getName() + " написал «" + event.getMessage() + "»");
        myIRC.ircServer.privmsg("-1", getFullName(event), event.getMessage());
    }

    /**
     * Возвращает полное имя игрока
     *
     * @param event
     * @return
     */
    public String getFullName(PlayerEvent event) {
        return event.getPlayer().getName() + "!ingame@" + myIRC.host(event.getPlayer().getAddress().getHostName());
    }
}
