package me.truekenny.MyIRC;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
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
    }

    /**
     * Обрабатывает выход пользователя
     *
     * @param event
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        log.info(event.getPlayer().getName() + " вышел из игры.");
    }

    /**
     * Обрабатывает сообщения пользователя
     *
     * @param event
     */
    @EventHandler
    public void onPlayerChat(PlayerChatEvent event) {
        log.info(event.getPlayer().getName() + " написал «" + event.getMessage() + "»");
    }
}
