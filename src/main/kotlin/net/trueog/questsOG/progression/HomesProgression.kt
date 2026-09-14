package net.trueog.questsOG.progression

import net.trueog.questsOG.quests.*
import org.bukkit.entity.Player

object HomesProgression : Progression {
    private const val HOMES_PERMISSION_PREFIX = "essentials.sethome.multiple.homes-"

    override val quests = arrayOf(HomesTwo(), HomesThree(), HomesFour(), HomesFive(), HomesSix())

    /**
     * Highest home tier the player currently holds via essentials.sethome.multiple.homes-N, or 1 if none. Takes the max
     * rather than assuming a single node so a leftover lower node (failed removal, group inheritance) never resets the
     * ladder.
     */
    fun getCurrentHomeCount(player: Player): Int =
        player.effectivePermissions
            .filter { it.value && it.permission.startsWith(HOMES_PERMISSION_PREFIX) }
            .mapNotNull { it.permission.removePrefix(HOMES_PERMISSION_PREFIX).toIntOrNull() }
            .maxOrNull() ?: 1

    override fun getNextQuest(player: Player): Quest? {
        // Quest at index i grants i + 2 homes, so the next quest lives at index current - 1.
        val nextQuestIndex = getCurrentHomeCount(player) - 1

        return quests.getOrNull(nextQuestIndex)
    }

    fun getHomeCount(quest: Quest): Int = quests.indexOf(quest) + 2
}
