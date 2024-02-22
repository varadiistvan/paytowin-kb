package org.koornbeurs.paytowin

import com.paytowin.grpc.Paytowin.PotionEffect
import com.paytowin.grpc.Paytowin.Tool
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import org.bukkit.potion.PotionEffectType

class EffectApplier(private val player: Player, private val server: Plugin) {

    fun tool(tool: Tool) {
        Bukkit.getScheduler().runTask(server, Runnable {
            val item = ItemStack(Material.DIAMOND_HOE)
            
            Bukkit.getScheduler().runTaskLater(server, Runnable {
                println("Removing $item")
            }, 20 * 4)
        })

    }

    fun potion(potionEffect: PotionEffect) {
        Bukkit.getScheduler().runTask(server, Runnable {
            println(player)
            println(PotionEffectType.getById(potionEffect.nameValue + 1))
            PotionEffectType.getById(potionEffect.nameValue + 1)
                ?.let {
                    org.bukkit.potion.PotionEffect(it, potionEffect.duration * 20, potionEffect.amplifier)
                }
                ?.let {
                    player.addPotionEffect(it)
                }
        })
    }


}