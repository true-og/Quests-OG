package net.trueog.questsOG

import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

class Config {
    lateinit var redisUrl: String

    fun load(): Boolean {
        val file = File(QuestsOG.plugin.dataFolder, "config.yml")
        if (!file.exists()) {
            QuestsOG.plugin.saveDefaultConfig()
        }
        val config = YamlConfiguration.loadConfiguration(file)
        config.save(file)

        try {
            redisUrl = config.get("redisUrl") as String
        } catch (_: Exception) {
            QuestsOG.plugin.logger.severe("Failed to parse config option \"redisUrl\" as a string")
            return true
        }

        return false
    }
}