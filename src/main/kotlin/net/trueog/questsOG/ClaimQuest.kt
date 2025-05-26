package net.trueog.questsOG

import net.trueog.questsOG.progression.HomesProgression
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class ClaimQuest : CommandExecutor {
    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>?
    ): Boolean {
        if (sender !is Player) {
            sender.sendMessage("You can only execute this command as a player.")
            return true
        }

        val nextQuest = HomesProgression.getNextQuest(sender)
        if (nextQuest == null) {
            sender.sendMessage("You cannot progress any further")
            return true
        }
        if (nextQuest.isEligible(sender)) {
            nextQuest.consumeQuestItems(sender)
            nextQuest.reward(sender)
            sender.sendMessage("Claimed quest!")
        } else {
            sender.sendMessage("You are not eligible to claim the quest")
            sender.sendMessage(nextQuest.unmetRequirements(sender))
        }
        return true
    }

}