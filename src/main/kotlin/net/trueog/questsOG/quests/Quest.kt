package net.trueog.questsOG.quests

import org.bukkit.entity.Player

interface Quest {
    fun isEligible(player: Player): Boolean?

    /**
     * @return True if successful
     */
    fun consumeQuestItems(player: Player): Boolean

    fun reward(player: Player)

    fun unmetRequirements(player: Player): String?
}