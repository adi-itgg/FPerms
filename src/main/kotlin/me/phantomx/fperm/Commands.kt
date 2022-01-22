package me.phantomx.fperm

import me.phantomx.fperm.Constants.E_
import me.phantomx.fperm.Constants.add
import me.phantomx.fperm.Constants.addgroup
import me.phantomx.fperm.Constants.addmember
import me.phantomx.fperm.Constants.default
import me.phantomx.fperm.Constants.group
import me.phantomx.fperm.Constants.info
import me.phantomx.fperm.Constants.listgroups
import me.phantomx.fperm.Constants.reload
import me.phantomx.fperm.Constants.remove
import me.phantomx.fperm.Constants.removegroup
import me.phantomx.fperm.Constants.removemember
import me.phantomx.fperm.Constants.user
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.OfflinePlayer
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender


class Commands(private val main: FPerms) : CommandExecutor{

    private val fpHead = "§3------------ [§bFPerms§3] ------------"

    override fun onCommand(s: CommandSender, p1: Command, p2: String, p3: Array<String>): Boolean {
        if (p3.isEmpty()) {
            usage(s, E_)
            return true
        }
        return when (p3[0].lowercase()) {
            reload -> {
                main.reloadPermsWithousSaving()
                s.sendMessage("${ChatColor.GREEN}Permission have been reloaded.")
                true
            }
            default -> defaultGroup(s, p3)
            user -> user(s, p3)
            group -> group(s, p3)
            listgroups -> listGroups(s)
            else -> {
                usage(s, E_)
                true
            }
        }
    }

    private fun usage(s: CommandSender, p: String) {
        s.sendMessage(fpHead)
        s.sendMessage(when (p) {
                E_ ->
                    """
                    §3FPerms commands: §b/fp ...
                    §3- §bdefault §3- manage default permissions for everyone
                    §3- §buser <user> §3- manage permissions per user
                    §3- §bgroup <group> §3- manage permissions per group
                    §3- §blistgroups §3- list all groups
                    §3- §breload §3- reload permissions from config
                """
                default -> """
                    §3Default subcommands: §b/lp default ...
                    §3- §binfo §3- show default permissions
                    §3- §badd §3- add default permission to everyone
                    §3- §bremove <permission> §3- remove default permission from everyone
                """
                user -> """
                    §3User subcommands: §b/lp user <user> ...
                    §3- §binfo §3- show a user's permissions and groups
                    §3- §badd <permission> §3- add permission to a user
                    §3- §bremove <permission> §3- remove permission from a user
                    §3- §baddgroup <group> §3- add user to a group
                `   §3- §bremovegroup <group> §3- remove user from a group
                """
                group -> """
                    §3Group subcommands: §b/lp group <group> ...
                    §3- §binfo §3- show a group's permissions and members
                    §3- §badd <permission> §3- add permission to a group
                    §3- §bremove <permission> §3- remove permission from a group
                    §3- §baddmember <user> §3- add user to a group
                    §3- §bremovemember <user> §3- remove user from a group
                """
                else -> E_
            }.trimIndent())
    }

    private fun listGroups(s: CommandSender): Boolean {
        s.sendMessage(fpHead)
        s.sendMessage("§3Groups:")
        for (group in main.config.getConfigurationSection("groups").getKeys(false)) {
            var users = 0
            var perms = 0
            for (user in main.config.getConfigurationSection("users").getKeys(false))
                if (main.config.getStringList("users.$user.groups").contains(group))
                    users++
            for (perm in main.config.getStringList("groups.$group.permissions"))
                perms++
            s.sendMessage("§3- §a$group §3($perms permissions, $users members)")
        }
        return true
    }
    private fun listDefaultPermissions(sender: CommandSender): Boolean {
        sender.sendMessage(fpHead)
        sender.sendMessage("§3Default permissions: ")
        for (perm in main.config.getStringList("default.permissions"))
            sender.sendMessage("§3- §a$perm")
        return true
    }

    private fun listPermissions(p: OfflinePlayer, s: CommandSender): Boolean {
        s.sendMessage(fpHead)
        s.sendMessage("§3User: §a${p.name}")
        s.sendMessage("§3Default permissions: ")
        for (perm in main.config.getStringList("default.permissions"))
            s.sendMessage("§3- §a$perm")
        s.sendMessage("§3User permissions: ")
        for (perm in main.config.getStringList("users." + p.name + ".permissions"))
            s.sendMessage("§3- §a$perm")
        s.sendMessage("§3Groups: ")
        for (group in main.config.getStringList("users." + p.name + ".groups"))
            s.sendMessage("§3- §a$group")
        return true
    }

    private fun addPermission(p: OfflinePlayer?, arg: String, s: CommandSender) : Boolean {
        s.sendMessage(fpHead)
        val perms: MutableList<String>
        if (p == null) {
            perms = main.config.getStringList("default.permissions")
            if (perms.contains(arg)) {
                s.sendMessage("§2Permission §a$arg§2 already is a default permission.")
                return true
            }
            perms.add(arg)
            main.config.set("default.permissions", perms)
            s.sendMessage("§2Added permission §a$arg§2 to §aall players§2.")
        } else {
            perms = main.config.getStringList("users.${p.name}.permissions")
            if(perms.contains(arg)) {
                s.sendMessage("§2Player §a${p.name}§2 already has permission §a$arg§2.")
                return true
            }
            perms.add(arg)
            main.config.set("users.${p.name}.permissions", perms)
            s.sendMessage("§2Added permission §a" + arg + "§2 to player §a${p.name}§2.")
        }
        main.reloadPerms()
        return true
    }

    private fun removePermission(p: OfflinePlayer?, arg: String, s: CommandSender): Boolean {
        s.sendMessage(fpHead)
        val perms: MutableList<String>
        if (p == null) {
            perms = main.config.getStringList("default.permissions")
            perms.remove(arg)
            main.config["default.permissions"] = perms
            s.sendMessage("§2Removed permission §a$arg§2 from §aall players§2.")
        } else {
            perms = main.config.getStringList("users.${p.name}.permissions")
            perms.remove(arg)
            main.config["users." + p.name + ".permissions"] = perms
            s.sendMessage("§2Removed permission §a$arg§2 from player §a${p.name}§2.")
        }
        main.reloadPerms()
        return true
    }

    private fun shift(args: Array<String>) = args.copyOfRange(1, args.size)

    private fun defaultGroup(s: CommandSender, a: Array<String>) : Boolean {
        val args = shift(a)
        if (args.isEmpty() || args.size < 2) {
            usage(s, default)
            return true
        }
        if (args[0] == "info")
            return listDefaultPermissions(s)
        return when (args[0].lowercase()) {
            add -> return addPermission(null, args[1], s)
            remove -> return removePermission(null, args[1], s)
            else -> {
                usage(s, default)
                false
            }
        }
    }

    private fun addGroup(p: OfflinePlayer, arg: String, sender: CommandSender): Boolean {
        sender.sendMessage(fpHead)
        val groups: MutableList<String> = main.config.getStringList("users.${p.name}.groups")
        if (groups.contains(arg)) {
            sender.sendMessage("§2Player §a${p.name} already is in group §a$arg§2.")
            return true
        }
        groups.add(arg)
        main.config["users.${p.name}.groups"] = groups
        sender.sendMessage("§2Added player §a${p.name}§2 to group §a$arg§2.")
        main.reloadPerms()
        return true
    }

    private fun removeGroup(p: OfflinePlayer, arg: String, sender: CommandSender): Boolean {
        sender.sendMessage(fpHead    )
        val groups: MutableList<String> = main.config.getStringList("users.${p.name}.groups")
        groups.remove(arg)
        main.config["users.${p.name}.groups"] = groups
        sender.sendMessage("§2Removed player §a${p.name}§2 from group §a$arg§2.")
        main.reloadPerms()
        return true
    }

    private fun user(sender: CommandSender, a: Array<String>): Boolean {
        val args = shift(a)
        if (args.isEmpty()) {
            usage(sender, "user")
            return true
        }
        val p = Bukkit.getOfflinePlayer(args[0])
        if (p == null) {
            sender.sendMessage("§cCould not find player §4" + args[0] + "§c.")
            return true
        }
        if (args.size > 1 && args[1].equals("info", ignoreCase = true)) {
            return listPermissions(p, sender)
        }
        if (args.size < 3) {
            usage(sender, "user")
            return true
        }
        when (args[1].lowercase()) {
            add -> return addPermission(p, args[2], sender)
            remove -> return removePermission(p, args[2], sender)
            addgroup -> return addGroup(p, args[2], sender)
            removegroup -> return removeGroup(p, args[2], sender)
        }
        usage(sender, "user")
        return true
    }

    private fun listGroupPermissions(args: Array<String>, s: CommandSender): Boolean {
        s.sendMessage(fpHead)
        s.sendMessage("§3Group: §a${args[0]}")
        s.sendMessage("§3Group permissions: ")
        for (perm in main.config.getStringList("groups.${args[0]}.permissions"))
            s.sendMessage("§3- §a$perm")
        s.sendMessage("§3Members: ")
        for (user in main.config.getConfigurationSection("users").getKeys(false))
            if (main.config.getStringList("users.$user.groups").contains(args[0]))
                s.sendMessage("§3- §a$user")
        return true
    }

    private fun addGroupPermission(group: String, arg: String, s: CommandSender): Boolean {
        s.sendMessage(fpHead)
        val perms: MutableList<String> = main.config.getStringList("groups.$group.permissions")
        if (perms.contains(arg)) {
            s.sendMessage("§2Group §a$group§2 already has permission §a$arg§2.")
            return true
        }
        perms.add(arg)
        main.config["groups.$group.permissions"] = perms
        s.sendMessage("§2Added permission §a$arg§2 to group §a$group§2.")
        main.reloadPerms()
        return true
    }

    private fun removeGroupPermission(group: String, arg: String, s: CommandSender): Boolean {
        s.sendMessage(fpHead)
        val perms: MutableList<String> = main.config.getStringList("groups.$group.permissions")
        perms.remove(arg)
        main.config["groups.$group.permissions"] = perms
        s.sendMessage("§2Removed permission §a$arg§2 from group §a$group§2.")
        main.reloadPerms()
        return true
    }

    private fun group(sender: CommandSender, a: Array<String>): Boolean {
        val args = shift(a)
        if (args.isEmpty()) {
            usage(sender, group)
            return true
        }
        if (args.size > 1 && args[1].equals(info, ignoreCase = true))
            return listGroupPermissions(args, sender)
        if (args.size < 3) {
            usage(sender, group)
            return true
        }
        when (args[1].lowercase()) {
            add -> return addGroupPermission(args[0], args[2], sender)
            remove -> return removeGroupPermission(args[0], args[2], sender)
        }
        if (args[1].equals(addmember, ignoreCase = true) || args[1].equals(removemember, ignoreCase = true)) {
           val p = Bukkit.getOfflinePlayer(args[2])
            if (p == null) {
                sender.sendMessage("§cCould not find player §4${args[0]}§c.")
                return true
            }
            when (args[1].lowercase()) {
                addmember -> return addGroup(p, args[0], sender)
                removemember -> return removeGroup(p, args[0], sender)
            }
        }
        usage(sender, group)
        return true
    }

}