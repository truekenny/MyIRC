package me.truekenny.MyIRC;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.dynmap.DynmapWebChatEvent;

public class DynmapListener implements Listener {

    private final MyIRC plugin;

    /**
     * @param plugin
     */
    public DynmapListener(MyIRC plugin) {
        this.plugin = plugin;
    }

    /**
     * @param event
     */
    @EventHandler
    public void onDynmapWebChatEvent(DynmapWebChatEvent event) {
        String message = event.getMessage();
        String name = event.getName().replaceAll("\\.", "_");
        String source = event.getSource();

        if(event.isCancelled()) {
            System.out.println("onDynmapWebChatEvent: CANCEL, source:" + source + ", name:" + name + ", message:" + message);

            return;
        }

        System.out.println("onDynmapWebChatEvent: source:" + source + ", name:" + name + ", message:" + message);

        if (name.equals("")) {
            name = "WebChatSystem";
        }

        // Отправить только в IRC
        plugin.ircServer.privmsg("-1", name + "!WebChat@" + plugin.config.getString("irc.gameHost"), ((char) 3) + "03" + message, false);
    }
}