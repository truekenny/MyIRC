package me.truekenny.MyIRC;

import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.ServerOperator;

import java.util.Set;

public class MyCommandSender implements CommandSender {
    private Server server;

    private IRCClient ircClient;

    public MyCommandSender(Server server, IRCClient ircClient) {
        this.server = server;
        this.ircClient = ircClient;
    }

    public void sendMessage(java.lang.String s) {
        String message = s.replaceAll("\n", " ").replaceAll("\r", " ");

        server.getConsoleSender().sendMessage(message);

        ircClient.ircServer.sendPrivate(ircClient.getNick(), message.trim());
    }

    public void sendMessage(java.lang.String[] strings) {
        server.getConsoleSender().sendMessage(strings);

        for (String s: strings) {
            ircClient.ircServer.sendPrivate(ircClient.getNick(), s.trim());
        }
    }

    public org.bukkit.Server getServer() {
        return server;
    }

    public java.lang.String getName() {
        return "Server";
    }

    public boolean isPermissionSet(java.lang.String s) {
        return false;
    }

    public boolean isPermissionSet(org.bukkit.permissions.Permission permission) {
        return false;
    }

    public boolean hasPermission(java.lang.String s) {
        server.getConsoleSender().sendMessage(s);

        return true;
    }

    public boolean hasPermission(org.bukkit.permissions.Permission permission) {
        server.getConsoleSender().sendMessage(permission.toString());

        return true;
    }

    public org.bukkit.permissions.PermissionAttachment addAttachment(org.bukkit.plugin.Plugin plugin, java.lang.String s, boolean b) {
        return (PermissionAttachment)(new Object());
    }

    public org.bukkit.permissions.PermissionAttachment addAttachment(org.bukkit.plugin.Plugin plugin) {
        return (PermissionAttachment)(new Object());
    }

    public org.bukkit.permissions.PermissionAttachment addAttachment(org.bukkit.plugin.Plugin plugin, java.lang.String s, boolean b, int i) {
        return (PermissionAttachment)(new Object());
    }

    public org.bukkit.permissions.PermissionAttachment addAttachment(org.bukkit.plugin.Plugin plugin, int i) {
        return (PermissionAttachment)(new Object());
    }

    public void removeAttachment(org.bukkit.permissions.PermissionAttachment permissionAttachment) {

    }

    public void recalculatePermissions() {

    }

    public java.util.Set<org.bukkit.permissions.PermissionAttachmentInfo> getEffectivePermissions() {
        return (Set)(new Object());
    }

    public boolean isOp() {
        server.getConsoleSender().sendMessage("isOp");

        return true;
    }

    public void setOp(boolean b) {

    }

}
