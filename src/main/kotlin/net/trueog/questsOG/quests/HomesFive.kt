package net.trueog.questsOG.quests

import net.luckperms.api.node.types.PermissionNode
import net.trueog.diamondbankog.PostgreSQL
import net.trueog.questsOG.BooleanRequirement
import net.trueog.questsOG.MainThreadBlock.runOnMainThread
import net.trueog.questsOG.ProgressRequirement
import net.trueog.questsOG.QuestsOG
import net.trueog.questsOG.Requirement
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.Statistic
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType

class HomesFive : Quest {
    private data class Requirements(
        val totalShards: Int,
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
        val hasVillagerHead: Boolean,
        val duelsWins: Int
    )

    val customMobHeadKey = NamespacedKey(QuestsOG.mobHeads, "customMobHead")

    private val cutestPredatorAdvancement = Bukkit.getServer().advancementIterator().asSequence()
        .single { advancement -> advancement.key.toString() == "minecraft:husbandry/axolotl_in_a_bucket" }
    private val twoByTwoAdvancement = Bukkit.getServer().advancementIterator().asSequence()
        .single { advancement -> advancement.key.toString() == "minecraft:husbandry/bred_all_animals" }
    private val completeCatalogueAdvancement = Bukkit.getServer().advancementIterator().asSequence()
        .single { advancement -> advancement.key.toString() == "minecraft:husbandry/complete_catalogue" }
    private val monstersHuntedAdvancement = Bukkit.getServer().advancementIterator().asSequence()
        .single { advancement -> advancement.key.toString() == "minecraft:adventure/kill_all_mobs" }

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

        val hasVillagerHead = player.inventory.filterNotNull().any {
            val data = it.itemMeta.persistentDataContainer.get(customMobHeadKey, PersistentDataType.STRING)
            data?.startsWith("VILLAGER") == true
        }

        val duelsWins = QuestsOG.duels.userManager.get(player.uniqueId)?.wins ?: 0

        return Requirements(
            totalShards,
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
            hasVillagerHead,
            duelsWins
        )
    }

    override suspend fun isEligible(player: Player): Boolean? {
        val requirements = fetchRequirements(player)

        if (requirements == null) {
            return null
        }

        return requirements.totalShards >= 2500 * 9 &&
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
                requirements.hasVillagerHead &&
                requirements.duelsWins >= 150
    }

    override suspend fun consumeQuestItems(player: Player): Boolean {
        val withdrawResult =
            QuestsOG.diamondBankAPI.withdrawFromPlayer(player.uniqueId, 2500 * 9, "Homes five quest claimed", null)
        if (withdrawResult.isFailure) {
            return false
        }

        runOnMainThread {
            player.level -= 200
        }

        return true
    }

    override fun reward(player: Player) {
        val homesFourNode = PermissionNode.builder("essentials.sethome.multiple.homes-4").build()
        val homesFiveNode = PermissionNode.builder("essentials.sethome.multiple.homes-5").build()

        QuestsOG.luckPerms.userManager.modifyUser(player.uniqueId) { user ->
            user.data().remove(homesFourNode)
            user.data().add(homesFiveNode)
        }
    }

    override suspend fun getRequirements(player: Player): Array<Requirement>? {
        val requirements = fetchRequirements(player)

        if (requirements == null) {
            return null
        }

        return arrayOf(
            ProgressRequirement("Total Balance", requirements.totalShards, 2500 * 9),
            ProgressRequirement("Ticks Played", requirements.ticksPlayed, 25920000),
            ProgressRequirement("Cm Travelled on Pig", requirements.pigOneCm, 500000),
            ProgressRequirement("Cm Travelled on Strider", requirements.striderOneCm, 100000),
            ProgressRequirement("Dolphins Killed", requirements.dolphinKills, 50),
            ProgressRequirement("Zoglins Killed", requirements.zoglinKills, 50),
            BooleanRequirement("The Cutest Predator", requirements.hasCutestPredator),
            BooleanRequirement("Two by Two", requirements.hasTwoByTwo),
            BooleanRequirement("A Complete Catalogue", requirements.hasCompleteCatalogue),
            BooleanRequirement("Monsters Hunted", requirements.hasMonstersHunted),
            BooleanRequirement("Died to \"fell while climbing\"", requirements.hasDiedToFellWhileClimbing),
            BooleanRequirement(
                "Died to \"walked into the danger zone due to Zoglin\"",
                requirements.hasDiedToMagmaBlockWhileFightingZoglin
            ),
            ProgressRequirement("Levels", requirements.levels, 200),
            ProgressRequirement("Fish Caught", requirements.fishCaught, 2000),
            BooleanRequirement("Has Villager Head", requirements.hasVillagerHead),
            ProgressRequirement("Duels Wins", requirements.duelsWins, 150)
        )
    }
}