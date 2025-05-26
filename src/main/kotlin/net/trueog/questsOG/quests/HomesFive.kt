package net.trueog.questsOG.quests

import net.luckperms.api.node.types.PermissionNode
import net.trueog.questsOG.QuestsOG
import org.bukkit.Bukkit
import org.bukkit.Statistic
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player

class HomesFive : Quest {
    private data class Requirements(
        val playerTotalBalance: Int,
        val ticksPlayed: Int,
        val pigOneCm: Int,
        val striderOneCm: Int,
        val dolphinKills: Int,
        val zoglinKills: Int,
        val hasCutestPredator: Boolean,
        val hasTwoByTwo: Boolean,
        val hasCompleteCatalogue: Boolean,
        val hasMonstersHunted: Boolean,
        val hasDiedToFellWhileClimbing: Boolean,
        val hasDiedToMagmaBlockWhileFightingZoglin: Boolean,
        val levels: Int,
        val fishCaught: Int,
        val duelsWins: Int
    )

    private val cutestPredatorAdvancement = Bukkit.getServer().advancementIterator().asSequence()
        .single { advancement -> advancement.key.toString() == "minecraft:husbandry/axolotl_in_a_bucket" }
    private val twoByTwoAdvancement = Bukkit.getServer().advancementIterator().asSequence()
        .single { advancement -> advancement.key.toString() == "minecraft:husbandry/bred_all_animals" }
    private val completeCatalogueAdvancement = Bukkit.getServer().advancementIterator().asSequence()
        .single { advancement -> advancement.key.toString() == "minecraft:husbandry/complete_catalogue" }
    private val monstersHuntedAdvancement = Bukkit.getServer().advancementIterator().asSequence()
        .single { advancement -> advancement.key.toString() == "minecraft:adventure/kill_all_mobs" }

    private fun getRequirements(player: Player): Requirements {
        val playerTotalBalanceFuture = QuestsOG.diamondBankAPI.getPlayerTotalBalance(player.uniqueId)
        val playerTotalBalance = playerTotalBalanceFuture.get()

        val ticksPlayed = player.getStatistic(Statistic.PLAY_ONE_MINUTE)

        val pigOneCm = player.getStatistic(Statistic.PIG_ONE_CM)
        val striderOneCm = player.getStatistic(Statistic.STRIDER_ONE_CM)

        val dolphinKills = player.getStatistic(Statistic.KILL_ENTITY, EntityType.DOLPHIN)

        val zoglinKills = player.getStatistic(Statistic.KILL_ENTITY, EntityType.ZOGLIN)

        val hasCutestPredator = player.getAdvancementProgress(cutestPredatorAdvancement).isDone

        val hasTwoByTwo = player.getAdvancementProgress(twoByTwoAdvancement).isDone

        val hasCompleteCatalogue = player.getAdvancementProgress(completeCatalogueAdvancement).isDone

        val hasMonstersHunted = player.getAdvancementProgress(monstersHuntedAdvancement).isDone

        val hasDiedToFellWhileClimbing =
            QuestsOG.redis.getValue("questsog:${player.uniqueId}:deaths:fellWhileClimbing") == "true"

        val hasDiedToMagmaBlockWhileFightingZoglin =
            QuestsOG.redis.getValue("questsog:${player.uniqueId}:deaths:diedToMagmaBlockWhileFightingZoglin") == "true"

        val fishCaught = player.getStatistic(Statistic.FISH_CAUGHT)

        val duelsWins = QuestsOG.duels.userManager.get(player.uniqueId)?.wins ?: 0

        return Requirements(
            playerTotalBalance,
            ticksPlayed,
            pigOneCm,
            striderOneCm,
            dolphinKills,
            zoglinKills,
            hasCutestPredator,
            hasTwoByTwo,
            hasCompleteCatalogue,
            hasMonstersHunted,
            hasDiedToFellWhileClimbing,
            hasDiedToMagmaBlockWhileFightingZoglin,
            player.level,
            fishCaught,
            duelsWins
        )
    }

    override fun isEligible(player: Player): Boolean {
        val requirements = getRequirements(player)

        return requirements.playerTotalBalance >= 2500 &&
                requirements.ticksPlayed / 20.0 / 60.0 / 60.0 / 24.0 >= 15 &&
                requirements.pigOneCm / 100000.0 >= 5 &&
                requirements.striderOneCm / 100000.0 >= 1 &&
                requirements.dolphinKills >= 50 &&
                requirements.zoglinKills >= 50 &&
                requirements.hasCutestPredator &&
                requirements.hasTwoByTwo &&
                requirements.hasCompleteCatalogue &&
                requirements.hasMonstersHunted &&
                requirements.hasDiedToFellWhileClimbing &&
                requirements.hasDiedToMagmaBlockWhileFightingZoglin &&
                requirements.levels >= 200 &&
                requirements.fishCaught >= 2000 &&
                requirements.duelsWins >= 150
    }

    override fun consumeQuestItems(player: Player): Boolean {
        val withdrawFuture = QuestsOG.diamondBankAPI.withdrawFromPlayer(player.uniqueId, 2500)
        val error = withdrawFuture.get()
        if (error) {
            return true
        }

        player.level -= 200

        return false
    }

    override fun reward(player: Player) {
        val homesFourNode = PermissionNode.builder("essentials.sethome.multiple.homes-4").build()
        val homesFiveNode = PermissionNode.builder("essentials.sethome.multiple.homes-5").build()

        QuestsOG.luckPerms.userManager.modifyUser(player.uniqueId) { user ->
            user.data().remove(homesFourNode)
            user.data().add(homesFiveNode)
        }
    }

    override fun unmetRequirements(player: Player): String {
        val requirements = getRequirements(player)

        return "Total Balance: ${requirements.playerTotalBalance}/1000 | Ticks Played: ${requirements.ticksPlayed}/25920000 " +
                "| Cm Travelled on Pig: ${requirements.pigOneCm}/500000 | Cm Travelled on Strider: ${requirements.striderOneCm}/ 100000" +
                "| Dolphins killed: ${requirements.dolphinKills}/50 | Zoglins killed: ${requirements.zoglinKills}/50 | The Cutest Predator: ${requirements.hasCutestPredator} " +
                "| Two by Two: ${requirements.hasTwoByTwo} | Complete Catalogue: ${requirements.hasCompleteCatalogue} | Monsters Hunted: ${requirements.hasMonstersHunted} " +
                "| Died to Fell While Climbing ${requirements.hasDiedToFellWhileClimbing} | Died to Magna Block While Fighting Zoglin: ${requirements.hasDiedToMagmaBlockWhileFightingZoglin} " +
                "| Levels: ${requirements.levels}/200 | Fish Caught: ${requirements.fishCaught}/2000 | Duels Wins: ${requirements.duelsWins}"
    }
}