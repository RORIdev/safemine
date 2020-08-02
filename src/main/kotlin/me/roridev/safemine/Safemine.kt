package me.roridev.safemine

import me.roridev.safemine.handlers.BreakHandler
import me.roridev.safemine.handlers.DropHandler
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

class Safemine : JavaPlugin(){
    override fun onEnable() {
        saveDefaultConfig()
        reloadConfig()
        server.pluginManager.registerEvents(BreakHandler(), this)
        server.pluginManager.registerEvents(DropHandler(), this)
    }

    override fun onDisable() {
    }
    companion object{
        val logger get() = Bukkit.getPluginManager().getPlugin("safemine")!!.logger
        val config get() = Bukkit.getPluginManager().getPlugin("safemine")!!.config
    }
}