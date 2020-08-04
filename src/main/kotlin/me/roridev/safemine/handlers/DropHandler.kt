package me.roridev.safemine.handlers

import me.roridev.safemine.Safemine
import me.roridev.safemine.helpers.getInventoryIfShulker
import me.roridev.safemine.helpers.toShulkerItemStack
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.GameMode
import org.bukkit.Sound
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockDropItemEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory
import org.bukkit.inventory.meta.BlockStateMeta

class DropHandler : Listener {
    @EventHandler
    fun Drop(event: BlockDropItemEvent){
        if(event.player.gameMode != GameMode.SURVIVAL) return
        if(!event.player.inventory.hasSpace(event.items.map{x -> x.itemStack}.toMutableList())) {
            dropItem(event)
            return
        }
        event.isCancelled = true
        for(item in event.items){
            transferItems(event,item.itemStack)
        }
    }

    private fun dropItem(event: BlockDropItemEvent){
        if(Safemine.config.getBoolean("blockEvent")) event.isCancelled = true
        val player = event.player
        val component = TextComponent(Safemine.config.getString("message.fullInv"))
        component.color = ChatColor.RED
        player.sendActionBar(component)
        if(!Safemine.config.getBoolean("silent")) player.playSound(player.location, Sound.ENTITY_PLAYER_LEVELUP,0.3f,1.0f)
    }

    private fun transferItems(event : BlockDropItemEvent, item: ItemStack){
        val inventory = event.player.inventory
        val boxes = inventory.contents.filterNotNull()
                .filter{it.itemMeta is BlockStateMeta}
        val stack = inventory.contents.filterNotNull().firstOrNull {it.type == item.type}
        if(stack == null){
            if(!transferToShulkerBox(inventory, boxes, item)) {
                if(!inventory.hasSpace(event.items.map{x -> x.itemStack}.toMutableList())) {
                    dropItem(event)
                    return
                }
                if(!newItemStack(inventory,item)) dropItem(event)
                return
            }
            return
        }
        val index = inventory.contents.indexOfFirst { it?.type == item.type }
        if(stack.amount + item.amount <= stack.maxStackSize) {
            stack.amount += item.amount
            inventory.contents[index] = stack
        } else {
            if(!transferToShulkerBox(inventory, boxes, item)) {
                val reminder = stack.amount + item.amount - stack.maxStackSize
                stack.amount = stack.maxStackSize
                item.amount = reminder
                inventory.contents[index] = stack
                if(!newItemStack(inventory,item)) dropItem(event)
            }
        }
    }

    private fun transferToShulkerBox(inventory: PlayerInventory,
                                     boxes : List<ItemStack>,
                                     item : ItemStack) : Boolean {

        if(!Safemine.config.getBoolean("backpacks")) return false
        if (boxes.isNotEmpty()){
            val shulkers  = boxes.map { Pair(it, it.getInventoryIfShulker())}.toMap()
            for (entry in shulkers){
                val stack = entry.value?.contents?.firstOrNull { it?.type == item.type && it.amount != item.maxStackSize }
                if(stack != null) {
                    if(stack.amount + item.amount > item.maxStackSize) return false
                    val i = inventory.contents.indexOfFirst { it == entry.key }
                    entry.value?.addItem(item)
                    inventory.contents[i] = entry.value?.toShulkerItemStack(entry.key)
                    return true
                }
            }
            return false

        }
        return false
    }
    private fun newItemStack(inv: PlayerInventory, stack: ItemStack) : Boolean{
        if(!inv.hasSpace(mutableListOf(stack))) return false
        inv.addItem(stack)
        return true
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
