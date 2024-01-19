package org.koornbeurs.paytowin

import org.bukkit.plugin.java.JavaPlugin

class PayToWin : JavaPlugin() {

    private var threadHandle: Thread? = null;
    override fun onEnable() {
        threadHandle = PayToWinServer(50051).start()
        println(1234)
        // server.blockUntilShutdown()
    }

    override fun onDisable() {
        // Plugin shutdown logic
        // server.stop()

        println("Bruh?")
        threadHandle?.interrupt()
    }
}
