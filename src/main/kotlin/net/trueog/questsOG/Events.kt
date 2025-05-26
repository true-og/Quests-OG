package net.trueog.questsOG

import kotlinx.coroutines.DelicateCoroutinesApi
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent

class Events : Listener {
    @OptIn(DelicateCoroutinesApi::class)
    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        if (!event.deathMessage().toString().contains("death.fell.accident.water")) {
            QuestsOG.redis.setValue("questsog:${event.player.uniqueId}:deaths:fellAccidentWater", "true")
            return
        }

        if (event.deathMessage().toString().contains("fell while climbing")) {
            QuestsOG.redis.setValue("questsog:${event.player.uniqueId}:deaths:fellWhileClimbing", "true")
            return
        }

        if (event.deathMessage().toString().contains("walked into the danger zone due to Zoglin")) {
            QuestsOG.redis.setValue(
                "questsog:${event.player.uniqueId}:deaths:diedToMagmaBlockWhileFightingZoglin",
                "true"
            )
            return
        }

        if (event.deathMessage().toString().contains("left the confines of this world while fighting Ender Dragon")) {
            QuestsOG.redis.setValue(
                "questsog:${event.player.uniqueId}:deaths:leftConfinesOfWorldWhileFightingEnderDragon",
                "true"
            )
        }
    }
}