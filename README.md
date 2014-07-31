MyIRC
=====

- IRC server inside bukkit plugin.

Version
----

- 1.0.2 – List IRC users ingame /irc
- 1.0.1 – Fix (onNickChange);
- 1.0 – message between Ingame players and IRC users, hide user host.

Roadway
----

- 1.1 – Private message.

config.yml
----

```yml
irc:
  host: irc.example.com
  gameHost: server.example.com
  port: 6667
  channel: '#minecraft'
  creator: example.com
  topic: Welcome to MyIRC channel
messages:
  console:
    onEnable: MyIRC loaded!
    onDisable: MyIRC disabled!
    playerListener: PlayerListener loaded!
  irc:
    erroneusNickname: Erroneus Nickname
    nicknameInUse: Nickname is already in use
    privateOff: Private messages under construction
    kickOnSameNick: Someone came into the game with your nickname
  game:
    list: IRC users
rules:
  hide:
    hosts: google.com:hide,yahoo.com:microsoft.com,.*example.net:clear

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