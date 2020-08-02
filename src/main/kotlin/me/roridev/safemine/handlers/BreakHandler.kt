package me.roridev.safemine.handlers

import com.destroystokyo.paper.Title
import me.roridev.safemine.Safemine
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.GameMode
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent

class BreakHandler : Listener {
    @EventHandler
    fun Break(event : BlockBreakEvent){
        if(event.player.gameMode != GameMode.SURVIVAL) return
        if(!event.isDropItems) return
        if(!event.player.inventory.hasSpace(event.block.drops.toMutableList())) {
            cancelEvent(event)
            return
        }
        if(event.player.inventory.itemInMainHand.containsEnchantment(Enchantment.MENDING)
                || event.player.inventory.itemInOffHand.containsEnchantment(Enchantment.MENDING)
                || event.player.inventory.armorContents.any{it != null && it.containsEnchantment(Enchantment.MENDING)}) {
            event.player.giveExp(event.expToDrop, true)
        } else {
            event.player.giveExp(event.expToDrop, false)
        }
        event.expToDrop = 0
    }
    private fun cancelEvent(event: BlockBreakEvent){
        if(Safemine.config.getBoolean("blockEvent")) event.isCancelled = true
        val player = event.player
        val component = TextComponent(Safemine.config.getString("message.fullInv"))
        component.color = ChatColor.RED
        player.sendActionBar(component)
        if(!Safemine.config.getBoolean("silent")) player.playSound(player.location, Sound.ENTITY_PLAYER_LEVELUP,0.3f,1.0f)
    }
}

