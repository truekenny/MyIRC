package me.truekenny.MyIRC;

public class PingTask implements Runnable {

    public static MyIRC plugin;

    public PingTask(MyIRC instance) {
        plugin = instance;
    }

    public void run() {
        plugin.ircServer.broadcast("-1", "PING " + (System.currentTimeMillis() / 1000L));

        plugin.ircServer.killTimeOut();
    }

}