package net.trueog.questsOG.quests

import net.luckperms.api.node.types.PermissionNode
import net.trueog.questsOG.QuestsOG
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

    private val cutestPredatorAdvancement = Bukkit.getServer().advancementIterator().asSequence()
        .single { it.key.toString() == "minecraft:husbandry/axolotl_in_a_bucket" }
    private val twoByTwoAdvancement = Bukkit.getServer().advancementIterator().asSequence()
        .single { it.key.toString() == "minecraft:husbandry/bred_all_animals" }
    private val completeCatalogueAdvancement = Bukkit.getServer().advancementIterator().asSequence()
        .single { it.key.toString() == "minecraft:husbandry/complete_catalogue" }
    private val monstersHuntedAdvancement = Bukkit.getServer().advancementIterator().asSequence()
        .single { it.key.toString() == "minecraft:adventure/kill_all_mobs" }

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

    private fun getRequirements(player: Player): Requirements {
        val playerTotalBalanceFuture = QuestsOG.diamondBankAPI.getPlayerTotalBalance(player.uniqueId)
        val playerTotalBalance = playerTotalBalanceFuture.get()

        val ticksPlayed = player.getStatistic(Statistic.PLAY_ONE_MINUTE)

        val walkOnWaterOneCm = player.getStatistic(Statistic.WALK_ON_WATER_ONE_CM)

        val walkUnderWaterOneCm = player.getStatistic(Statistic.WALK_UNDER_WATER_ONE_CM)

        val stoneMined = player.getStatistic(Statistic.MINE_BLOCK, Material.STONE)
        val cobblestoneMined = player.getStatistic(Statistic.MINE_BLOCK, Material.COBBLESTONE)
        val stoneLikeMined = stoneMined + cobblestoneMined

        val discs = player.inventory
            .filterNotNull()
            .filter { it.type in neededDiscs }

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

    override fun isEligible(player: Player): Boolean {
        val requirements = getRequirements(player)

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
        if (error) {
            return true
        }

        player.level -= 250

        val removed = mutableSetOf<Material>()

        for (itemStack in player.inventory.filterNotNull()) {
            if (itemStack.type in neededDiscs && itemStack.type !in removed) {
                itemStack.amount -= 1
                removed += itemStack.type
            }
        }

        player.inventory.removeItem(ItemStack(Material.DRAGON_EGG, 5))

        return false
    }

    override fun reward(player: Player) {
        val homesFiveNode = PermissionNode.builder("essentials.sethome.multiple.homes-5").build()
        val homesSixNode = PermissionNode.builder("essentials.sethome.multiple.homes-6").build()

        QuestsOG.luckPerms.userManager.modifyUser(player.uniqueId) { user ->
            user.data().remove(homesFiveNode)
            user.data().add(homesSixNode)
        }
    }

    override fun unmetRequirements(player: Player): String {
        val requirements = getRequirements(player)

        return "Total Balance: ${requirements.playerTotalBalance}/1000 | Ticks Played: ${requirements.ticksPlayed}/51840000 " +
                "| Cm Walked on Water: ${requirements.walkOnWaterOneCm}/1000000 | Cm Walked under Water: ${requirements.walkUnderWaterOneCm}/1000000 " +
                "| Music Discs: ${requirements.discs}/13 " +
//                "| Left Confines of World While Fighting Ender Dragon: ${requirements.hasLeftConfinesOfWorldWhileFightingEnderDragon}/200 " +
                "| Finished Advancements: ${requirements.finishedAdvancements}/1179 | Obsidian Mined: ${requirements.obsidianMined}/1500 | Dragon Eggs: ${requirements.dragonEggs}/5 " +
                "| Levels: ${requirements.levels}/250 | Duels Wins: ${requirements.duelsWins}/300"
    }
}