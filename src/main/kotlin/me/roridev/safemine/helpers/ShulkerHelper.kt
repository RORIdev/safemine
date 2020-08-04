package me.roridev.safemine.helpers

import org.bukkit.Bukkit
import org.bukkit.block.ShulkerBox
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BlockStateMeta

fun ItemStack.getInventoryIfShulker() : Inventory? {
    if(itemMeta is BlockStateMeta){
        val bsm = itemMeta as BlockStateMeta
        if(bsm.blockState is ShulkerBox){
            val sb =  bsm.blockState as ShulkerBox
            val inv = Bukkit.createInventory(null,InventoryType.SHULKER_BOX, bsm.displayName)
            inv.contents = sb.inventory.contents
            return inv
        }
        return null
    }
    return null
}

fun Inventory.toShulkerItemStack(original : ItemStack) : ItemStack? {
    if(original.itemMeta is BlockStateMeta){
        val bsm = original.itemMeta as BlockStateMeta
        if(bsm.blockState is ShulkerBox){
            val shulk = bsm.blockState as ShulkerBox
            shulk.inventory.contents = contents
            bsm.blockState = shulk
            original.itemMeta = bsm
            return original
        }
        return null
    }
    return null
}
