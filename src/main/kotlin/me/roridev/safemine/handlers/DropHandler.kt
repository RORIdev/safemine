package me.roridev.safemine.handlers

import com.destroystokyo.paper.Title
import me.roridev.safemine.Safemine
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.GameMode
import org.bukkit.Sound
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockDropItemEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory

class DropHandler : Listener {
    @EventHandler
    fun Drop(event: BlockDropItemEvent){
        if(event.player.gameMode != GameMode.SURVIVAL) return
        if(!event.player.inventory.hasSpace(event.items.map{x -> x.itemStack}.toMutableList())) {
            cancelEvent(event)
            return
        }
        event.isCancelled = true
        for(item in event.items){
            transferItems(event,item.itemStack)
        }
    }

    private fun cancelEvent(event: BlockDropItemEvent){
        event.isCancelled = true
        val player = event.player
        val component = TextComponent(Safemine.config.getString("message.fullInv"))
        component.color = ChatColor.RED
        player.sendTitle(Title(component))
        player.playSound(player.location, Sound.ENTITY_PLAYER_LEVELUP,0.3f,1.0f)
    }

    private fun transferItems(event : BlockDropItemEvent, item: ItemStack){
        event.isCancelled = true
        val inventory = event.player.inventory
        val stack = inventory.contents.filterNotNull().firstOrNull {it.type == item.type}
        if(stack == null){
            if(!inventory.hasSpace(event.items.map{x -> x.itemStack}.toMutableList())) {
                cancelEvent(event)
                return
            }
            newItemStack(inventory,item)
            return
        }
        val index = inventory.contents.indexOfFirst { it?.type == item.type }
        if(stack.amount + item.amount <= stack.maxStackSize) {
            stack.amount += item.amount
            inventory.contents[index] = stack
        } else {
            val reminder = stack.amount + item.amount - stack.maxStackSize
            stack.amount = stack.maxStackSize
            item.amount = reminder
            inventory.contents[index] = stack
            newItemStack(inventory,item)
        }
    }

    private fun newItemStack(inv: PlayerInventory, stack: ItemStack) {
        if(!inv.hasSpace(mutableListOf(stack))) return
        inv.addItem(stack)
    }

}
fun PlayerInventory.hasSpace(items: MutableList<ItemStack>): Boolean {

    val inventorySize = 36 + if(itemInOffHand.amount == 0) 0 else 1
    if(contents.filterNotNull().size < inventorySize) { //Slot available
        return true
    }
    //From now on the check is only done if the inventory has no spaces left.
    val materials = items.map { x -> x.type }
    val intersection = contents.map{x -> x?.type}.intersect(materials)
    if(intersection.isEmpty()) {
        return false
    } else {
        val pos = materials.map{x -> (contents.indexOfFirst {it?.amount == it?.maxStackSize && it?.type == x })}
        return pos.none { it != -1 }
    }
}
