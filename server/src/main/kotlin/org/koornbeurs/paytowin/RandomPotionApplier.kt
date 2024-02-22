package org.koornbeurs.paytowin

import com.paytowin.grpc.Paytowin
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable

class RandomPotionApplier(
    private val plugin: Plugin,
    private val intervalTicks: Int,
    private val repetitions: Int,
    private val bukkitPlayer: Player
) :
    BukkitRunnable() {
    private var currentRun = 0

    override fun run() {
        if (currentRun >= repetitions) {
            this.cancel() // Stop the task after 'n' repetitions
            return
        }
        PotionEffectType.getById(Paytowin.PotionName.entries.shuffled().first().number + 1)
            ?.let { PotionEffect(it, intervalTicks, 1) }?.let { this.bukkitPlayer.addPotionEffect(it) }
        // Your repeated code goes here

        currentRun++ // Increment the run count
    }
}