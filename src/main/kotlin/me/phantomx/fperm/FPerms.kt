package me.phantomx.fperm

import org.apache.commons.lang.math.NumberUtils
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.permissions.PermissionAttachment
import org.bukkit.plugin.java.JavaPlugin
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern


class FPerms : JavaPlugin(), CommandExecutor, Listener {

    private val perms : HashMap<UUID, PermissionAttachment> = HashMap()
    private var version: Int = -1

    override fun onEnable() {
        saveDefaultConfig()
        val m: Matcher = Pattern.compile("^1\\.(\\d*)\\.").matcher(Bukkit.getBukkitVersion())
        while (m.find())
            if (NumberUtils.isNumber(m.group(1))) version = m.group(1).toInt()
        server.pluginManager.registerEvents(this, this)
        getCommand("fp").tabCompleter = TabCompleter()
        getCommand("fp").executor = Commands(this)
        getCommand("ping").executor = this
        addPermOnPlayers()
    }

    override fun onDisable() {
        removePerms()
        saveConfig()
        HandlerList.unregisterAll(this as Listener)
    }

    private fun addPerm(p : Player) {
        val att = p.addAttachment(this)
        for (perm in config.getStringList("default.permissions"))
            att.setPermission(perm, true)
        for (grup in config.getStringList("users.${p.name}.groups"))
            for (perm in config.getStringList("groups.${grup}.permissions"))
                att.setPermission(perm, true)
        for (perm in config.getStringList("users.${p.name}.permissions"))
            att.setPermission(perm, true)
        perms[p.uniqueId] = att
        if (version >= 13) p.updateCommands()
    }

    @EventHandler
    fun onPlayerJoin(e: PlayerJoinEvent) {
        addPerm(e.player)
    }

    @EventHandler
    fun onPlayerQuit(e: PlayerQuitEvent) {
        e.player.removeAttachment(perms[e.player.uniqueId])
    }

    private fun addPermOnPlayers() {
        for (p in server.onlinePlayers)
            if (p != null) addPerm(p)
    }

    private fun removePerms() {
        for (p in server.onlinePlayers)
            p.removeAttachment(perms[p.uniqueId])
    }

    fun reloadPerms() {
        saveConfig()
        reloadPermsWithousSaving()
    }

    fun reloadPermsWithousSaving() {
        removePerms()
        reloadConfig()
        addPermOnPlayers()
    }

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<String>
    ): Boolean {
        if (sender is Player && args.isEmpty()) {
            sender.sendMessage("§3Ping §6${getPing(sender)}§3 ms")
            return true
        } else if (args.isNotEmpty() && args[0].length > 1)
            for (p in server.onlinePlayers)
                if (p.name.lowercase() == args[0].lowercase() || p.displayName.lowercase() == args[0].lowercase()) {
                    sender.sendMessage("§3Ping §e${p.name} §3- §6${getPing(p)}§3 ms")
                    return true
                }
        return false
    }

    private fun getPing(p1: Player): Int {
        var p = p1
        val cCPClass: Class<*>
        val v = Bukkit.getServer().javaClass.getPackage().name.replace(".", ",").split(",").toTypedArray()[3]
        val cn = "org.bukkit.craftbukkit.$v.entity.CraftPlayer"
        cCPClass = Class.forName(cn)
        if (!p.javaClass.name.equals(cn))  //compatibility with some plugins
            p = Bukkit.getPlayer(p.uniqueId) //cast to org.bukkit.entity.Player
        try {
            val cCraftPlayer = cCPClass.cast(p)
            val getHandle: Method = cCraftPlayer.javaClass.getMethod("getHandle", *arrayOfNulls(0))
            val entityPlayer: Any = getHandle.invoke(cCraftPlayer, arrayOfNulls<Any>(0))
            val ping: Field = entityPlayer.javaClass.getDeclaredField("ping")
            return ping.getInt(entityPlayer)
        } catch (e: Exception) {
            try {
                val cCraftPlayer = cCPClass.cast(p)
                return cCraftPlayer.javaClass.getDeclaredMethod("getPing")
                    .invoke(cCraftPlayer) as Int
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
        return 0
    }
}