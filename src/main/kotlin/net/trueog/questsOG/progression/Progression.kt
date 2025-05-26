package net.trueog.questsOG.progression

import net.trueog.questsOG.quests.Quest
import org.bukkit.entity.Player

interface Progression {
    val quests: Array<Quest>

    fun getNextQuest(player: Player): Quest?
}