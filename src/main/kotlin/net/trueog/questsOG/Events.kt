package net.trueog.questsOG

import kotlinx.coroutines.DelicateCoroutinesApi
import net.kyori.adventure.text.TranslatableComponent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent

class Events : Listener {
    @OptIn(DelicateCoroutinesApi::class)
    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        val translatableDeathMessage = event.deathMessage() as TranslatableComponent

        if (translatableDeathMessage.key() == "death.fell.accident.water") {
            QuestsOG.redis.setValue("questsog:${event.player.uniqueId}:deaths:fellAccidentWater", "true")
            return
        }

        if (translatableDeathMessage.key() == "death.fell.accident.other_climbable") {
            QuestsOG.redis.setValue("questsog:${event.player.uniqueId}:deaths:fellWhileClimbing", "true")
            return
        }

        val translatableDeathMessageArgs = translatableDeathMessage.args()

        if (translatableDeathMessage.key() == "death.attack.hotFloor.player" && translatableDeathMessageArgs.size >= 2 && (translatableDeathMessageArgs[1] as TranslatableComponent).key() == "entity.minecraft.zoglin") {
            QuestsOG.redis.setValue(
                "questsog:${event.player.uniqueId}:deaths:diedToMagmaBlockWhileFightingZoglin",
                "true"
            )
            return
        }
    }
}