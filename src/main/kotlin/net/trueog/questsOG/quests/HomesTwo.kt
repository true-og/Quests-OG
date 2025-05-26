package net.trueog.questsOG.quests

import net.luckperms.api.node.types.PermissionNode
import net.trueog.questsOG.QuestsOG
import org.bukkit.Statistic
import org.bukkit.entity.Player

class HomesTwo : Quest {
    private data class Requirements(
        val playerTotalBalance: Int,
        val ticksPlayed: Int,
        val totalCm: Int,
        val levels: Int,
        val duelsWins: Int
    )

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

        val duelsWins = QuestsOG.duels.userManager.get(player.uniqueId)?.wins ?: 0

        return Requirements(playerTotalBalance, ticksPlayed, totalCm, player.level, duelsWins)
    }

    override fun isEligible(player: Player): Boolean {
        val requirements = getRequirements(player)

        return requirements.playerTotalBalance >= 100 &&
                requirements.ticksPlayed / 20.0 / 60.0 / 60.0 >= 24 &&
                requirements.totalCm / 100.0 >= 10000 &&
                requirements.levels >= 50 &&
                requirements.duelsWins >= 10
    }

    override fun consumeQuestItems(player: Player): Boolean {
        val withdrawFuture = QuestsOG.diamondBankAPI.withdrawFromPlayer(player.uniqueId, 100)
        val error = withdrawFuture.get()

        player.level -= 100

        return error
    }

    override fun reward(player: Player) {
        val homesTwoNode = PermissionNode.builder("essentials.sethome.multiple.homes-2").build()

        QuestsOG.luckPerms.userManager.modifyUser(player.uniqueId) { user ->
            user.data().add(homesTwoNode)
        }
    }

    override fun unmetRequirements(player: Player): String {
        val requirements = getRequirements(player)

        return "Total Balance: ${requirements.playerTotalBalance}/100 | Ticks Played: ${requirements.ticksPlayed}/1728000 | Total Cm Travelled: ${requirements.totalCm}/1000000"
    }
}