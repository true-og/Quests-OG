package net.trueog.questsOG.quests

import net.luckperms.api.node.types.PermissionNode
import net.trueog.diamondbankog.PostgreSQL
import net.trueog.questsOG.BooleanRequirement
import net.trueog.questsOG.MainThreadBlock.runOnMainThread
import net.trueog.questsOG.ProgressRequirement
import net.trueog.questsOG.QuestsOG
import net.trueog.questsOG.Requirement
import org.bukkit.Bukkit
import org.bukkit.Statistic
import org.bukkit.entity.Player

class HomesThree : Quest {
    private data class Requirements(
        val totalShards: Int,
        val ticksPlayed: Int,
        val totalCm: Int,
        val hasBeaconator: Boolean,
        val levels: Int,
        val duelsWins: Int
    )

    private val beaconatorAdvancement = Bukkit.getServer().advancementIterator().asSequence()
        .single { it.key.toString() == "minecraft:nether/create_full_beacon" }

    private suspend fun fetchRequirements(player: Player): Requirements? {
        val playerShardsResult = QuestsOG.diamondBankAPI.getPlayerShards(player.uniqueId, PostgreSQL.ShardType.ALL)
        val playerShards = playerShardsResult.getOrElse {
            return null
        }
        if (playerShards.isNeededShardTypeNull(PostgreSQL.ShardType.ALL)) {
            return null
        }
        val totalShards =
            playerShards.shardsInBank!! + playerShards.shardsInInventory!! + playerShards.shardsInEnderChest!!

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
            totalShards,
            ticksPlayed,
            totalCm,
            advancementProgress.isDone,
            player.level,
            duelsWins
        )
    }

    override suspend fun isEligible(player: Player): Boolean? {
        val requirements = fetchRequirements(player)

        if (requirements == null) {
            return null
        }

        return requirements.totalShards >= 250 * 9 &&
                requirements.ticksPlayed / 20.0 / 60.0 / 60.0 / 24.0 >= 5 &&
                requirements.totalCm / 100.0 >= 50000 && // 100k?
                requirements.hasBeaconator &&
                requirements.levels >= 100 &&
                requirements.duelsWins >= 20
    }

    override suspend fun consumeQuestItems(player: Player): Boolean {
        val withdrawResult =
            QuestsOG.diamondBankAPI.withdrawFromPlayer(player.uniqueId, 250 * 9, "Homes three quest claimed", null)
        if (withdrawResult.isFailure) {
            return false
        }

        runOnMainThread {
            player.level -= 100
        }

        return true
    }

    override fun reward(player: Player) {
        val homesTwoNode = PermissionNode.builder("essentials.sethome.multiple.homes-2").build()
        val homesThreeNode = PermissionNode.builder("essentials.sethome.multiple.homes-3").build()

        QuestsOG.luckPerms.userManager.modifyUser(player.uniqueId) { user ->
            user.data().remove(homesTwoNode)
            user.data().add(homesThreeNode)
        }
    }

    override suspend fun getRequirements(player: Player): Array<Requirement>? {
        val requirements = fetchRequirements(player)

        if (requirements == null) {
            return null
        }

        return arrayOf(
            ProgressRequirement("Total Shards", requirements.totalShards, 250 * 9),
            ProgressRequirement("Ticks Played", requirements.ticksPlayed, 8640000),
            ProgressRequirement("Total Cm Travelled", requirements.totalCm, 5000000),
            BooleanRequirement("Beaconator", requirements.hasBeaconator),
            ProgressRequirement("Levels", requirements.levels, 100),
            ProgressRequirement("Duels Wins", requirements.duelsWins, 20)
        )
    }
}