package net.trueog.questsOG

import kotlinx.coroutines.launch
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

        QuestsOG.scope.launch {
            val isEligible = nextQuest.isEligible(sender)
            if (isEligible == null) {
                sender.sendMessage("Something wrong while trying to check if you are eligible")
                return@launch
            }

            if (isEligible) {
                val successful = nextQuest.consumeQuestItems(sender)
                if (!successful) {
                    sender.sendMessage("Something wrong while trying to consume the quest items")
                    return@launch
                }
                nextQuest.reward(sender)
                sender.sendMessage("Claimed quest!")
            } else {
                sender.sendMessage("You are not eligible to claim the quest")
                val requirements = nextQuest.getRequirements(sender)
                if (requirements == null) {
                    sender.sendMessage("Something wrong while trying to get the unmet requirements")
                    return@launch
                }
                var requirementsMessage = ""
                for (requirement in requirements) {
                    if (requirement is BooleanRequirement) {
                        requirementsMessage += "${if (requirement.met) "<green>" else "<red>"}${requirement.name}: ${requirement.met}<reset> |"
                    }
                    if (requirement is ProgressRequirement) {
                        requirementsMessage += "${if (requirement.current >= requirement.target) "<green>" else "<red>"}${requirement.name}: ${requirement.current}/${requirement.target}<reset> | "
                    }
                }
                sender.sendMessage(QuestsOG.mm.deserialize(requirementsMessage))
            }
        }
        return true
    }

}