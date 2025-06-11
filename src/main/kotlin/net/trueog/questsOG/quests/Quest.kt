package net.trueog.questsOG.quests

import net.trueog.questsOG.Requirement
import org.bukkit.entity.Player

interface Quest {
    suspend fun isEligible(player: Player): Boolean?

    /**
     * @return True if successful
     */
    suspend fun consumeQuestItems(player: Player): Boolean

    fun reward(player: Player)

    suspend fun getRequirements(player: Player): Array<Requirement>?
}