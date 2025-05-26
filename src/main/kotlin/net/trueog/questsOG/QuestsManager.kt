package net.trueog.questsOG

import org.bukkit.entity.Player

class QuestsManager {
//    enum class HomesLevel(val permission: String) {
//        TWO("essentials.sethome.multiple.homes-2"),
//        THREE("essentials.sethome.multiple.homes-3"),
//        FOUR("essentials.sethome.multiple.homes-4"),
//        FIVE("essentials.sethome.multiple.homes-5"),
//        SIX("essentials.sethome.multiple.homes-6")
//    }

    val homesLevels = hashMapOf(
        "essentials.sethome.multiple.homes-2" to 2,
        "essentials.sethome.multiple.homes-3" to 3,
        "essentials.sethome.multiple.homes-4" to 4,
        "essentials.sethome.multiple.homes-5" to 5,
        "essentials.sethome.multiple.homes-6" to 6
    )

    fun getCurrentAmountOfHomes(player: Player): Int {
        val permission = player.effectivePermissions.single { homesLevels.contains(it.permission) }
        if (permission == null) {
            return 1
        }
        val homesLevel = homesLevels[permission.permission]!!
        return homesLevel
    }

    fun isEligibleForNextHomesLevel(player: Player) {
        val currentAmountOfHomes = getCurrentAmountOfHomes(player)

    }
}