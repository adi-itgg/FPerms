package me.phantomx.fperm

import me.phantomx.fperm.Constants.add
import me.phantomx.fperm.Constants.addgroup
import me.phantomx.fperm.Constants.addmember
import me.phantomx.fperm.Constants.default
import me.phantomx.fperm.Constants.group
import me.phantomx.fperm.Constants.info
import me.phantomx.fperm.Constants.listgroups
import me.phantomx.fperm.Constants.listmembers
import me.phantomx.fperm.Constants.reload
import me.phantomx.fperm.Constants.remove
import me.phantomx.fperm.Constants.removegroup
import me.phantomx.fperm.Constants.removemember
import me.phantomx.fperm.Constants.user
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

class TabCompleter : TabCompleter {

    private val commandsL: List<String> = listOf(user, group, listgroups, reload)
    private val userL: List<String> = listOf(add, remove, addgroup, removegroup, info)
    private val groupL: List<String> = listOf(add, remove, info, listmembers, addmember, removemember)
    private val defaultGroupL: List<String> = listOf(add, remove, info)

    override fun onTabComplete(
        p0: CommandSender,
        p1: Command,
        p2: String,
        p3: Array<out String>
    ): List<String>? {
        if (p3.size <= 1)
            return commandsL
        return when (p3[0].lowercase()) {
            user -> if (p3.size == 2) null else if (p3.size == 3) userL else null
            group -> if (p3.size == 2) null else if (p3.size == 3) groupL else null
            listgroups -> null
            default -> if (p3.size == 2) defaultGroupL else null
            else -> null
        }
    }

}