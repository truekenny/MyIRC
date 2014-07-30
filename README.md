MyIRC
=====

- IRC server inside bukkit plugin.

Version
----

- 1.0 – message between Ingame players and IRC users, hide user host.

Roadway
----

- 1.1 – Private message.

config.yml
----

```yml
irc:
  host: irc.example.ru
  gameHost: server.example.ru
  port: 6667
  channel: '#minecraft'
messages:
  console:
    onEnable: MyIRC loaded!
    onDisable: MyIRC disabled!
    playerListener: PlayerListener loaded!
  irc:
    erroneusNickname: Erroneus Nickname
    nicknameInUse: Nickname is already in use
    privateOff: Private messages under construction
rules:
  hide:
    hosts: google.com:hide,yahoo.com:hide,.*example.com:admin

```

Implemented IRC commands
----

- Join (auto-join)
- Part
- Quit
- Privmsg
- Who
- Whois

10x
----
http://dillinger.io – Markdown online editor