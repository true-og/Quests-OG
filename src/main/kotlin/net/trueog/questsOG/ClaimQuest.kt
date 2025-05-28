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

        val isEligible = nextQuest.isEligible(sender)
        if (isEligible == null) {
            sender.sendMessage("Something wrong while trying to check if you are eligible")
            return true
        }

        if (isEligible) {
            val successful = nextQuest.consumeQuestItems(sender)
            if (!successful) {
                sender.sendMessage("Something wrong while trying to consume the quest items")
                return true
            }
            nextQuest.reward(sender)
            sender.sendMessage("Claimed quest!")
        } else {
            sender.sendMessage("You are not eligible to claim the quest")
            val requirements = nextQuest.getRequirements(sender)
            if (requirements == null) {
                sender.sendMessage("Something wrong while trying to get the unmet requirements")
                return true
            }
            var requirementsMessage = ""
            for (requirement in requirements) {
                if (requirement is BooleanRequirement) {
                    requirementsMessage += "${requirement.name}: ${requirement.met} | "
                }
                if (requirement is ProgressRequirement) {
                    requirementsMessage += "${requirement.name}: ${requirement.current}/${requirement.target} | "
                }
            }
            sender.sendMessage(requirementsMessage)
        }
        return true
    }

}