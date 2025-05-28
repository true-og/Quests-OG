package net.trueog.questsOG.quests

import net.luckperms.api.node.types.PermissionNode
import net.trueog.questsOG.BooleanRequirement
import net.trueog.questsOG.ProgressRequirement
import net.trueog.questsOG.QuestsOG
import net.trueog.questsOG.Requirement
import org.bukkit.Bukkit
import org.bukkit.Statistic
import org.bukkit.entity.Player

class HomesFour : Quest {
    private data class Requirements(
        val playerTotalBalance: Int,
        val ticksPlayed: Int,
        val totalCm: Int,
        val hasFuriousCocktail: Boolean,
        val hasSeriousDedication: Boolean,
        val levels: Int,
        val hasDiedToFellAccidentWater: Boolean,
        val duelsWins: Int
    )

    private val furiousCocktailAdvancement = Bukkit.getServer().advancementIterator().asSequence()
        .single { it.key.toString() == "minecraft:nether/all_potions" }
    private val seriousDedicationAdvancement = Bukkit.getServer().advancementIterator().asSequence()
        .single { it.key.toString() == "minecraft:husbandry/obtain_netherite_hoe" }

    private fun fetchRequirements(player: Player): Requirements? {
        val playerTotalBalanceFuture = QuestsOG.diamondBankAPI.getPlayerTotalBalance(player.uniqueId)
        val playerTotalBalance = playerTotalBalanceFuture.get()
        if (playerTotalBalance == null) {
            return null
        }

        val ticksPlayed = player.getStatistic(Statistic.PLAY_ONE_MINUTE)

        val walkOneCm = player.getStatistic(Statistic.WALK_ONE_CM)
        val walkOnWaterOneCm = player.getStatistic(Statistic.WALK_ON_WATER_ONE_CM)
        val climbOneCm = player.getStatistic(Statistic.CLIMB_ONE_CM)
        val walkUnderWaterOneCm = player.getStatistic(Statistic.WALK_UNDER_WATER_ONE_CM)
        val minecartOneCm = player.getStatistic(Statistic.MINECART_ONE_CM)
        val boatOneCm = player.getStatistic(Statistic.BOAT_ONE_CM)
        val pigOneCm = player.getStatistic(Statistic.PIG_ONE_CM)
        val horseOneCm = player.getStatistic(Statistic.HORSE_ONE_CM)
        val sprintOneCm = player.getStatistic(Statistic.SPRINT_ONE_CM)
        val crouchOneCm = player.getStatistic(Statistic.CROUCH_ONE_CM)
        val aviateOneCm = player.getStatistic(Statistic.AVIATE_ONE_CM)
        val swimOneCm = player.getStatistic(Statistic.SWIM_ONE_CM)
        val striderOneCm = player.getStatistic(Statistic.STRIDER_ONE_CM)
        val totalCm =
            walkOneCm + walkOnWaterOneCm + climbOneCm + walkUnderWaterOneCm + minecartOneCm + boatOneCm + pigOneCm + horseOneCm + sprintOneCm + crouchOneCm + aviateOneCm + swimOneCm + striderOneCm

        val furiousCocktailAdvancementProgress = player.getAdvancementProgress(furiousCocktailAdvancement)

        val seriousDedicationAdvancementProgress = player.getAdvancementProgress(seriousDedicationAdvancement)

        val hasDiedToFellAccidentWater =
            QuestsOG.redis.getValue("questsog:${player.uniqueId}:deaths:fellAccidentWater") == "true"

        val duelsWins = QuestsOG.duels.userManager.get(player.uniqueId)?.wins ?: 0

        // TODO: Check if player has a villager head
//        val hasVillagerHead = player.inventory.any { isVillagerHead(it) }

        return Requirements(
            playerTotalBalance,
            ticksPlayed,
            totalCm,
            furiousCocktailAdvancementProgress.isDone,
            seriousDedicationAdvancementProgress.isDone,
            player.level,
            hasDiedToFellAccidentWater,
            duelsWins
        )
    }

    override fun isEligible(player: Player): Boolean? {
        val requirements = fetchRequirements(player)

        if (requirements == null) {
            return null
        }

        return requirements.playerTotalBalance >= 1000 &&
                requirements.ticksPlayed / 20.0 / 60.0 / 60.0 / 24.0 >= 10 &&
                requirements.totalCm / 100.0 >= 200000 &&
                requirements.hasFuriousCocktail &&
                requirements.hasSeriousDedication &&
                requirements.levels >= 150 &&
                requirements.hasDiedToFellAccidentWater &&
                requirements.duelsWins >= 50
    }

    override fun consumeQuestItems(player: Player): Boolean {
        val withdrawFuture = QuestsOG.diamondBankAPI.withdrawFromPlayer(player.uniqueId, 1000)
        val error = withdrawFuture.get()
        if (error == null || error) {
            return false
        }

        player.level -= 150

        return true
    }

    override fun reward(player: Player) {
        val homesThreeNode = PermissionNode.builder("essentials.sethome.multiple.homes-3").build()
        val homesFourNode = PermissionNode.builder("essentials.sethome.multiple.homes-4").build()

        QuestsOG.luckPerms.userManager.modifyUser(player.uniqueId) { user ->
            user.data().remove(homesThreeNode)
            user.data().add(homesFourNode)
        }
    }

    override fun getRequirements(player: Player): Array<Requirement>? {
        val requirements = fetchRequirements(player)

        if (requirements == null) {
            return null
        }

        return arrayOf(
            ProgressRequirement("Total Balance", requirements.playerTotalBalance, 1000),
            ProgressRequirement("Ticks Played", requirements.ticksPlayed, 17280000),
            ProgressRequirement("Total Cm Travelled", requirements.totalCm, 20000000),
            BooleanRequirement("A Furious Cocktail", requirements.hasFuriousCocktail),
            BooleanRequirement("Serious Dedication", requirements.hasSeriousDedication),
            ProgressRequirement("Levels", requirements.levels, 150),
            BooleanRequirement("Died to \"death.fell.accident.water\"", requirements.hasDiedToFellAccidentWater),
            ProgressRequirement("Duels Wins", requirements.duelsWins, 50)
        )
    }
}