package net.trueog.questsOG

import java.io.File
import org.bukkit.configuration.file.YamlConfiguration

class Config {
    lateinit var redisUrl: String

    /** @return True if successful */
    fun load(): Boolean {
        val file = File(QuestsOG.plugin.dataFolder, "config.yml")
        if (!file.exists()) {
            QuestsOG.plugin.saveDefaultConfig()
        }
        val config = YamlConfiguration.loadConfiguration(file)

        try {
            redisUrl = config.get("redisUrl") as String
        } catch (_: Exception) {
            QuestsOG.plugin.logger.severe("Failed to parse config option \"redisUrl\" as a string")
            return false
        }

        return true
    }
}
