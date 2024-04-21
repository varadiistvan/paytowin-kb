package org.koornbeurs.paytowin

import com.paytowin.grpc.Paytowin
import com.paytowin.grpc.Paytowin.DiamondTool
import com.paytowin.grpc.Paytowin.PotionEffect
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import org.bukkit.potion.PotionEffectType

class EffectApplier(private val player: Player, private val server: Plugin) {

    fun tool(tool: DiamondTool) {
        Bukkit.getScheduler().runTask(server, Runnable {
            val item = when (tool) {
                DiamondTool.Axe -> ItemStack(Material.DIAMOND_AXE, 1)
                DiamondTool.Hoe -> ItemStack(Material.DIAMOND_HOE, 1)
                DiamondTool.Pickaxe -> ItemStack(Material.DIAMOND_PICKAXE, 1)
                DiamondTool.Shovel -> ItemStack(Material.DIAMOND_SHOVEL, 1)
                DiamondTool.Sword -> ItemStack(Material.DIAMOND_SWORD, 1)
                DiamondTool.Helmet -> ItemStack(Material.DIAMOND_HELMET, 1)
                DiamondTool.Chestplate -> ItemStack(Material.DIAMOND_CHESTPLATE, 1)
                DiamondTool.Leggings -> ItemStack(Material.DIAMOND_LEGGINGS, 1)
                DiamondTool.Boots -> ItemStack(Material.DIAMOND_BOOTS, 1)
                DiamondTool.UNRECOGNIZED -> return@Runnable
            }

            player.inventory.addItem(item)

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

    fun item(itemName: Paytowin.MinecraftMaterialWrapper.MinecraftMaterial, amount: Int) {
        Bukkit.getScheduler().runTask(server, Runnable {
            val item = Material.getMaterial(itemName.toString())?.let { ItemStack(it, amount) }
            if (item != null) {
                player.inventory.addItem(item)
            }
        })
    }


}