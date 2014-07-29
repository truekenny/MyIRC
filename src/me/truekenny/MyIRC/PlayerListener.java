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
    private final MyIRC plugin;

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
        plugin = instance;
        log.info("PlayerListener загружен.");
    }

    /**
     * Обрабытывает вход пользователя
     *
     * @param event
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        log.info(event.getPlayer().getName() + " вошел в игру.");
        plugin.server.join("-1", getFullName(event));
        plugin.server.mode("-1", event.getPlayer().getName());
    }

    /**
     * Обрабатывает выход пользователя
     *
     * @param event
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        log.info(event.getPlayer().getName() + " вышел из игры.");
        plugin.server.part("-1", getFullName(event));
    }

    /**
     * Обрабатывает сообщения пользователя
     *
     * @param event
     */
    @EventHandler
    public void onPlayerChat(PlayerChatEvent event) {
        log.info(event.getPlayer().getName() + " написал «" + event.getMessage() + "»");
        plugin.server.privmsg("-1", getFullName(event), event.getMessage());
    }

    /**
     * Возвращает полное имя игрока
     *
     * @param event
     * @return
     */
    public String getFullName(PlayerEvent event) {
        return event.getPlayer().getName() + "!ingame@" + event.getPlayer().getAddress().getHostName();
    }
}
