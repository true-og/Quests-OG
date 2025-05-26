package net.trueog.questsOG.quests

import net.luckperms.api.node.types.PermissionNode
import net.trueog.questsOG.QuestsOG
import org.bukkit.Bukkit
import org.bukkit.Statistic
import org.bukkit.entity.Player

class HomesThree : Quest {
    private data class Requirements(
        val playerTotalBalance: Int,
        val ticksPlayed: Int,
        val totalCm: Int,
        val hasBeaconator: Boolean,
        val levels: Int,
        val duelsWins: Int
    )

    private val beaconatorAdvancement = Bukkit.getServer().advancementIterator().asSequence()
        .single { it.key.toString() == "minecraft:nether/create_full_beacon" }

    private fun getRequirements(player: Player): Requirements {
        val playerTotalBalanceFuture = QuestsOG.diamondBankAPI.getPlayerTotalBalance(player.uniqueId)
        val playerTotalBalance = playerTotalBalanceFuture.get()

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

        val advancementProgress = player.getAdvancementProgress(beaconatorAdvancement)

        val duelsWins = QuestsOG.duels.userManager.get(player.uniqueId)?.wins ?: 0

        return Requirements(
            playerTotalBalance,
            ticksPlayed,
            totalCm,
            advancementProgress.isDone,
            player.level,
            duelsWins
        )
    }

    override fun isEligible(player: Player): Boolean {
        val requirements = getRequirements(player)

        return requirements.playerTotalBalance >= 250 &&
                requirements.ticksPlayed / 20.0 / 60.0 / 60.0 / 24.0 >= 5 &&
                requirements.totalCm / 100.0 >= 50000 && // 100k?
                requirements.hasBeaconator &&
                requirements.levels >= 100 &&
                requirements.duelsWins >= 20
    }

    override fun consumeQuestItems(player: Player): Boolean {
        val withdrawFuture = QuestsOG.diamondBankAPI.withdrawFromPlayer(player.uniqueId, 250)
        val error = withdrawFuture.get()
        if (error) {
            return true
        }

        player.level -= 100

        return false
    }

    override fun reward(player: Player) {
        val homesTwoNode = PermissionNode.builder("essentials.sethome.multiple.homes-2").build()
        val homesThreeNode = PermissionNode.builder("essentials.sethome.multiple.homes-3").build()

        QuestsOG.luckPerms.userManager.modifyUser(player.uniqueId) { user ->
            user.data().remove(homesTwoNode)
            user.data().add(homesThreeNode)
        }
    }

    override fun unmetRequirements(player: Player): String {
        val requirements = getRequirements(player)

        return "Total Balance: ${requirements.playerTotalBalance}/250 | Ticks Played: ${requirements.ticksPlayed}/8640000 | Total Cm Travelled: ${requirements.totalCm}/5000000 | Beaconator: ${requirements.hasBeaconator} | Levels: ${requirements.levels}/100"
    }
}