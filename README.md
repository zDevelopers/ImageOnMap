ImageOnMap
==========

Repo for ImageOnMap, a bukkit plugin.


## Features

ImageOnMap allows you to load a picture from the Internet to a Minecraft map.

- Loads an image from a URL onto a map. PNG, JPEG and GIF are supported.
- These images will be saved on your server and reloaded at restart.
- Big pictures will be cut automatically into several parts! As example a 1024x1024 picture will be cut in 16 maps.
- Your image will be centered.
- You can put your map in an item frame.

This plugin is a free software licenced under the GNU General Public License (version 3 or above). You can also get unstable development builds here.


## Quick guide

- Ensure that you have a free slot in your inventory, as ImageOnMap will give you a map.
- Type `/tomap URL`, where URL is a link to the picture you want to render (see the section below).
- Enjoy your picture! You can place it in an item frame to make a nice poster if you want.


## Commands and Permissions

### `/tomap <url>`

Renders an image and gives a map to the player with it.

- This command can only be used by a player.
- The link must be complete, do not forget that the chat limit is 240 characters.
- You can use an URL shortener like tinyURL or bitly.
- If you want a picture in one map, type resize after the link.
- Permission: `imageonmap.new` (or `imageonmap.userender`—legacy, but will be kept in the plugin).


### `/maps`

Opens a GUI to see, retrieve and manage the user's maps.

- This command can only be used by a player.
- Opens a GUI listing all the maps in a pagnated view.
- A book is displayed too to see some usage statistics (maps created, quotas).
- An user can retrieve a map by left-clicking it, or manage it by right-clicking.
- Maps can be renamed (for organization), deleted (but they won't render in game anymore!), or partially retrieved (for posters maps containing more than one map).
- Permission: `imageonmap.list`, plus `imageonmap.get`, `imageonmap.rename` and `imageonmap.delete` for actions into the GUI.


### `/maptool <new|list|get|delete|explore|migrate>`

Main command to manage the maps. The less used in everyday usage, too.

- The commands names are pretty obvious.
- `/maptool new` is an alias of `/tomap`.
- `/maptool explore` is an alias of `/maps`.
- `/maptool migrate` migrates the old maps when you upgrade from IoM <= 2.7 to IoM 3.0. You HAVE TO execute this command to retrieve all maps when you do such a migration.
- Permissions:
  - `imageonmap.new` for `/maptool new`;
  - `imageonmap.list` for both `/maptool list` and `/maptool explore`;
  - `imageonmap.get` for `/maptool get`;
  - `imageonmap.delete` for `/maptool delete`;
  - `imageonmap.administrative` for `/maptool migrate`.

### About the permissions

All permissions are by default granted to everyone, with the exception of `imageonmap.administrative`. We believe that in most cases, servers administrators want to give the availability to create images on maps to every player.  
Negate a permission using a plugin manager to remove it, if you want to restrict this possibility to a set of users.

You can grant `imageonmap.*` to users, as this permission is a shortcut for all _user_ permissions (excluding `imageonmap.administrative`).


## Configuration

```yaml
# Plugin language. Empty: system language.
# Available: en_US (default, fallback) and fr_FR.
lang:

# Allows collection of anonymous statistics on plugin environment and usage
# The statistics are publicly visible here: http://mcstats.org/plugin/ImageOnMap
collect-data: true

# Images rendered on maps consume Minecraft maps ID, and there are only 32 767 of them.
# You can limit the maximum number of maps a player, or the whole server, can use with ImageOnMap.
# 0 means unlimited.
map-global-limit: 0
map-player-limit: 0
```

## Changelog

### 3.0

The 3.0 release is a complete rewrite of the original ImageOnMap plugin, now based on zLib, which adds many feature and fixes many bugs.

This new version is not compatible with the older ones, so your older maps will not be loaded. Run the `/maptool migrate` command (as op or in console) in order to get them back in this new version.

You will find amongst the new features:

- New Splatter maps, making it easy to deploy and remove big posters in one click !
- No more item tags when maps are put in item frames !
- Internationalization support (only french and english are supported for now, contributions are welcome)
- Map Quotas (for players and the whole server)
- A new map Manager (based on an inventory interface), to list, rename, get and delete your maps
- Improvements on the commands system (integrated help and autocompletion)
- Asynchronous maps rendering (your server won't freeze anymore when rendering big maps, and you can queue multiple map renderings !)
- UUID management (which requires to run `/maptool migrate`)

### 3.1

- Fixed permissions support by adding a full set of permissions for every action of the plugin.



## Data collection

We use metrics to collect basic information about the usage of this plugin. This can be disabled by setting `collect-data` to false in `config.yml`.
