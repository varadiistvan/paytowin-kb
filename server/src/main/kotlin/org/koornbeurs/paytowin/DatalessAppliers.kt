package org.koornbeurs.paytowin

import com.paytowin.grpc.Paytowin
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.random.Random


abstract class DatalessApplier(
    val plugin: Plugin,
    val intervalTicks: Int = 0,
    val repetitions: Int,
    val bukkitPlayer: Player
) : BukkitRunnable()

class RandomPotionApplier(plugin: Plugin, intervalTicks: Int = 40, repetitions: Int = 10, bukkitPlayer: Player) :
    DatalessApplier(plugin, intervalTicks, repetitions, bukkitPlayer) {
    private var currentRun = 0

    override fun run() {
        if (currentRun >= repetitions) {
            this.cancel() // Stop the task after 'n' repetitions
            return
        }
        PotionEffectType.getById(Paytowin.PotionNameWrapper.PotionName.entries.shuffled().first().number + 1)
            ?.let { PotionEffect(it, intervalTicks, 1) }?.let { this.bukkitPlayer.addPotionEffect(it) }
        // Your repeated code goes here

        currentRun++ // Increment the run count
    }
}


class SpinnyApplier(
    plugin: Plugin,
    repetitions: Int = 200,
    bukkitPlayer: Player
) :
    DatalessApplier(plugin, 0, repetitions, bukkitPlayer) {
    private var currentRun = 0

    class Smoother(val bukkitPlayer: Player, val deltaYaw: Double, val deltaPitch: Double) : BukkitRunnable() {
        private var currentRun = 0
        override fun run() {
            if (currentRun >= 10) {
                this.cancel()
                return
            }
            bukkitPlayer.setRotation(
                bukkitPlayer.yaw + (deltaYaw / 10).toFloat(),
                bukkitPlayer.pitch + (deltaPitch / 10).toFloat()
            )

            currentRun++
        }
    }

    override fun run() {
        if (currentRun >= repetitions) {
            this.cancel() // Stop the task after 'n' repetitions
            return
        }

        val currentYaw = bukkitPlayer.yaw.toDouble()// Example starting yaw
        val currentPitch = bukkitPlayer.pitch.toDouble() // Example starting pitch
        val alpha = 3.0 // Shape parameter for Pareto distribution
        val xm = 1.0 // Scale parameter for Pareto distribution

        val (deltaYaw, deltaPitch) = adjustCamera(alpha, xm)

        Smoother(bukkitPlayer, deltaYaw, deltaPitch).runTaskTimer(plugin, 0L, 2L)

        currentRun++ // Increment the run count
    }

    fun paretoDistribution(alpha: Double, xm: Double = 1.0): Double {
        val u = Random.nextDouble()
        return xm / (1 - u).pow(1 / alpha)
    }

    fun adjustCamera(alpha: Double, xm: Double): Pair<Double, Double> {
        // Choose a direction θ uniformly from [0, 2π)
        val theta = Random.nextDouble(0.0, 2 * PI)

        // Choose a distance d from a Pareto distribution
        val d = paretoDistribution(alpha, xm)

        // Project the distance d onto the x and y axes to simulate splitting the movement
        val dx = d * cos(theta) // Movement along the x-axis
        val dy = d * sin(theta) // Movement along the y-axis

        // Assuming dx affects yaw and dy affects pitch, calculate the change
        // Here, we scale the adjustments to ensure they are reasonable for yaw and pitch
        // The scale factor might need adjustment based on your application's sensitivity
        val deltaYaw = dx * 30 / PI // Convert radians to degrees for yaw
        val deltaPitch = dy * 30 / PI // Convert radians to degrees for pitch

        println("detlayaw $deltaPitch, delatpitch $deltaPitch")

        return Pair(deltaYaw, deltaPitch)
    }
}