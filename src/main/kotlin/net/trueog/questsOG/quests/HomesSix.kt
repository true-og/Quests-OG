package net.trueog.questsOG.quests

import net.luckperms.api.node.types.PermissionNode
import net.trueog.questsOG.ProgressRequirement
import net.trueog.questsOG.QuestsOG
import net.trueog.questsOG.Requirement
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Statistic
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class HomesSix : Quest {
    private data class Requirements(
        val playerTotalBalance: Int,
        val ticksPlayed: Int,
        val walkOnWaterOneCm: Int,
        val walkUnderWaterOneCm: Int,
        val stoneLikePickedUp: Int,
        val discs: Int,
//        val hasLeftConfinesOfWorldWhileFightingEnderDragon: Boolean,
        val finishedAdvancements: Int,
        val obsidianMined: Int,
        val dragonEggs: Int,
        val levels: Int,
        val duelsWins: Int
    )

    private val neededDiscs = setOf(
        Material.MUSIC_DISC_13,
        Material.MUSIC_DISC_CAT,
        Material.MUSIC_DISC_BLOCKS,
        Material.MUSIC_DISC_CHIRP,
        Material.MUSIC_DISC_FAR,
        Material.MUSIC_DISC_MALL,
        Material.MUSIC_DISC_MELLOHI,
        Material.MUSIC_DISC_STAL,
        Material.MUSIC_DISC_STRAD,
        Material.MUSIC_DISC_WARD,
        Material.MUSIC_DISC_11,
        Material.MUSIC_DISC_WAIT,
        Material.MUSIC_DISC_5
    )

    private fun fetchRequirements(player: Player): Requirements? {
        val playerTotalBalanceFuture = QuestsOG.diamondBankAPI.getPlayerTotalBalance(player.uniqueId)
        val playerTotalBalance = playerTotalBalanceFuture.get()
        if (playerTotalBalance == null) {
            return null
        }

        val ticksPlayed = player.getStatistic(Statistic.PLAY_ONE_MINUTE)

        val walkOnWaterOneCm = player.getStatistic(Statistic.WALK_ON_WATER_ONE_CM)

        val walkUnderWaterOneCm = player.getStatistic(Statistic.WALK_UNDER_WATER_ONE_CM)

        val stoneMined = player.getStatistic(Statistic.MINE_BLOCK, Material.STONE)
        val cobblestoneMined = player.getStatistic(Statistic.MINE_BLOCK, Material.COBBLESTONE)
        val stoneLikeMined = stoneMined + cobblestoneMined

        val discs = player.inventory
            .filterNotNull()
            .filter { it.type in neededDiscs }.distinctBy { it.type }

//        val hasLeftConfinesOfWorldWhileFightingEnderDragon =
//            QuestsOG.redis.getValue("questsog:${player.uniqueId}:deaths:leftConfinesOfWorldWhileFightingEnderDragon") == "true"

        val finishedAdvancements = Bukkit.getServer().advancementIterator().asSequence().filterNotNull()
            .filter { player.getAdvancementProgress(it).isDone }

        val obsidianMined = player.getStatistic(Statistic.MINE_BLOCK, Material.OBSIDIAN)

        val dragonEggs = player.inventory.filterNotNull().count { it.type == Material.DRAGON_EGG }

        val duelsWins = QuestsOG.duels.userManager.get(player.uniqueId)?.wins ?: 0

        return Requirements(
            playerTotalBalance,
            ticksPlayed,
            walkOnWaterOneCm,
            walkUnderWaterOneCm,
            stoneLikeMined,
            discs.size,
//            hasLeftConfinesOfWorldWhileFightingEnderDragon,
            finishedAdvancements.count(),
            obsidianMined,
            dragonEggs,
            player.level,
            duelsWins
        )
    }

    override fun isEligible(player: Player): Boolean? {
        val requirements = fetchRequirements(player)

        if (requirements == null) {
            return null
        }

        return requirements.playerTotalBalance >= 5000 &&
                requirements.ticksPlayed / 20.0 / 60.0 / 60.0 / 24.0 >= 30 &&
                requirements.walkOnWaterOneCm / 100000.0 >= 10 &&
                requirements.walkUnderWaterOneCm / 100000.0 >= 10 &&
                requirements.discs >= 13 &&
//                requirements.hasLeftConfinesOfWorldWhileFightingEnderDragon &&
                requirements.finishedAdvancements >= 1179 &&
                requirements.obsidianMined >= 1500 &&
                requirements.dragonEggs >= 5 &&
                requirements.levels >= 250 &&
                requirements.duelsWins >= 300
    }

    override fun consumeQuestItems(player: Player): Boolean {
        val withdrawFuture = QuestsOG.diamondBankAPI.withdrawFromPlayer(player.uniqueId, 5000)
        val error = withdrawFuture.get()
        if (error == null || error) {
            return false
        }

        player.level -= 250

        val removed = mutableSetOf<Material>()

        for (itemStack in player.inventory.filterNotNull()) {
            if (itemStack.type in neededDiscs && itemStack.type !in removed) {
                itemStack.amount -= 1
                removed += itemStack.type
            }
        }

        val notRemoved = player.inventory.removeItem(ItemStack(Material.DRAGON_EGG, 5))

        return notRemoved.isEmpty()
    }

    override fun reward(player: Player) {
        val homesFiveNode = PermissionNode.builder("essentials.sethome.multiple.homes-5").build()
        val homesSixNode = PermissionNode.builder("essentials.sethome.multiple.homes-6").build()

        QuestsOG.luckPerms.userManager.modifyUser(player.uniqueId) { user ->
            user.data().remove(homesFiveNode)
            user.data().add(homesSixNode)
        }
    }

    override fun getRequirements(player: Player): Array<Requirement>? {
        val requirements = fetchRequirements(player)

        if (requirements == null) {
            return null
        }

        return arrayOf(
            ProgressRequirement("Total Balance", requirements.playerTotalBalance, 5000),
            ProgressRequirement("Ticks Played", requirements.ticksPlayed, 51840000),
            ProgressRequirement("Cm Walked on Water", requirements.walkOnWaterOneCm, 1000000),
            ProgressRequirement("Cm Walked under Water", requirements.walkUnderWaterOneCm, 1000000),
            ProgressRequirement("Music Discs", requirements.discs, 13),
            ProgressRequirement("Finished Advancements", requirements.finishedAdvancements, 1179),
            ProgressRequirement("Obsidian Mined", requirements.obsidianMined, 1500),
            ProgressRequirement("Dragon Eggs", requirements.dragonEggs, 5),
            ProgressRequirement("Levels", requirements.levels, 250),
            ProgressRequirement("Duels Wins", requirements.levels, 300)
        )
    }
}