package me.truekenny.MyIRC;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
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
        PlayerData playerData = Players.getPlayerData(event.getPlayer());
        playerData.updateIdle();
        playerData.updateConnect();

        if (myIRC.isHiddenGamer(event.getPlayer().getName())) {
            log.info(event.getPlayer().getName() + " hidden gamer join");

            return;
        }

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
        if (myIRC.isHiddenGamer(event.getPlayer().getName())) {
            log.info(event.getPlayer().getName() + " hidden gamer quit");

            return;
        }

        // log.info(event.getPlayer().getName() + " вышел из игры.");
        myIRC.ircServer.part("-1", getFullName(event), "Gamer quit");

        myIRC.ircServer.backNick(event.getPlayer().getName());

        Players.remove(event.getPlayer().getName());
    }

    /**
     * Обрабатывает сообщения пользователя
     *
     * @param event Событие
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChat(PlayerChatEvent event) {
        // log.info(event.getPlayer().getName() + " написал «" + event.getMessage() + "»");
        if (event.isCancelled()) {

            return;
        }

        if (myIRC.config.getBoolean("block.duplicate.message")) {
            PlayerData playerData = Players.getPlayerData(event.getPlayer());
            if (event.getMessage().equalsIgnoreCase(playerData.getLastMessage())) {
                Player player = event.getPlayer();
                String message = "<" + player.getDisplayName() + "> " + event.getMessage();

                myIRC.log.info("Duplicate ingame message is cancelled (" + message + ").");
                event.setCancelled(true);

                player.sendMessage(message);

                return;
            }
            playerData.setLastMessage(event.getMessage());
        }

        myIRC.ircServer.privmsg("-1", getFullName(event), event.getMessage());
    }

    /**
     * Возвращает полное имя игрока
     *
     * @param event Событие
     * @return Полное имя игрока
     */
    public String getFullName(PlayerEvent event) {
        return getFullName(event.getPlayer());
    }

    public String getFullName(Player player) {
        return player.getName() + "!ingame@" + myIRC.host(Players.getPlayerData(player).host, player.getName());
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
                player.sendMessage(ChatColor.GOLD + "<" + destCase + "> " + message);

                return;
            }

            IRCClient client = myIRC.ircServer.sendPrivate(message, getFullName(player), dest);
            if (client != null) {
                player.sendMessage(ChatColor.GOLD + "<" + client.getNick() + "> " + message);

                if (client.away != null) {
                    player.sendMessage(ChatColor.GRAY + "[" + client.getNick() + "] "
                            + myIRC.config.getString("messages.game.away") + " " + client.away);
                }

                return;
            }

            player.sendMessage(ChatColor.RED + "<" + dest + "> " + myIRC.config.getString("messages.game.noSuchNick"));
        }
    }

    /**
     * Движение игрока
     *
     * @param evt
     */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent evt) {
        Players.updateIdle(evt.getPlayer());
    }
}
