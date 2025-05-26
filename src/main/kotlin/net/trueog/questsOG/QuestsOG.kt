package net.trueog.questsOG

import me.realized.duels.api.Duels
import net.luckperms.api.LuckPerms
import net.trueog.diamondbankog.DiamondBankAPI
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin


class QuestsOG : JavaPlugin() {
    companion object {
        lateinit var plugin: QuestsOG
        lateinit var config: Config
        lateinit var redis: Redis
        lateinit var diamondBankAPI: DiamondBankAPI
        lateinit var luckPerms: LuckPerms
        lateinit var duels: Duels
    }

    override fun onEnable() {
        plugin = this

        Companion.config = Config()
        if (Companion.config.load()) {
            Bukkit.getPluginManager().disablePlugin(this)
            return
        }

        redis = Redis()

        val diamondBankAPIProvider =
            server.servicesManager.getRegistration<DiamondBankAPI>(DiamondBankAPI::class.java)
        if (diamondBankAPIProvider == null) {
            logger.severe("DiamondBank-OG API is null")
            Bukkit.getPluginManager().disablePlugin(this)
            return
        }
        diamondBankAPI = diamondBankAPIProvider.provider

        val luckPermsProvider = Bukkit.getServicesManager().getRegistration(LuckPerms::class.java)
        if (luckPermsProvider == null) {
            this.logger.severe("Luckperms API is null, quitting....")
            Bukkit.getPluginManager().disablePlugin(this)
            return
        }
        luckPerms = luckPermsProvider.provider

        val duels = Bukkit.getServer().pluginManager.getPlugin("Duels")
        if (duels == null) {
            this.logger.severe("Duels API is null, quitting....")
            Bukkit.getPluginManager().disablePlugin(this)
            return
        }
        Companion.duels = duels as Duels

        this.server.pluginManager.registerEvents(Events(), this)
        getCommand("claimquest")?.setExecutor(ClaimQuest())
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }
}
