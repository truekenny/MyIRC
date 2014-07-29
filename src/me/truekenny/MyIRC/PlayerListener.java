package me.truekenny.MyIRC;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

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
     * Сохранение объекта главного класса
     *
     * @param instance
     */
    public PlayerListener(MyIRC instance) {
        plugin = instance;
        plugin.getLogger().info("PlayerListener загружен.");
    }

    /**
     * Обрабытывает вход пользователя
     *
     * @param event
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        plugin.getLogger().info(event.getPlayer().getName() + " вошел в игру.");
    }

    /**
     * Обрабатывает выход пользователя
     *
     * @param event
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getLogger().info(event.getPlayer().getName() + " вышел из игры.");
    }

    /**
     * Обрабатывает сообщения пользователя
     *
     * @param event
     */
    @EventHandler
    public void onPlayerChat(PlayerChatEvent event) {
        plugin.getLogger().info(event.getPlayer().getName() + " написал «" + event.getMessage() + "»");
    }
}
