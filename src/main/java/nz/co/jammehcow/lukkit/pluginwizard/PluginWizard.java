package nz.co.jammehcow.lukkit.pluginwizard;

import nz.co.jammehcow.lukkit.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PluginWizard implements Runnable {
    public enum Step {
        ENTRY,
        NAME,
        VERSION,
        AUTHOR,
        DESC,
        EXIT, // terminate loop, create (or error) and cleanup. Not used in currentStep member
        REPEAT // Used to repeat the current action if the input was invalid. Also no used in current step member
    }

    private static final String nameRegex = "/^([0-9]|[a-z]|[A-Z])([0-9]|[a-z]|[A-Z]|-|_)+$/g";

    final Main plugin;
    private final CommandSender sender;
    private final WizardChatHandler chatHandler;
    private final PluginTemplate template = new PluginTemplate();
    private Step currentStep = Step.ENTRY;

    public PluginWizard(Main plugin, CommandSender sender) {
        this.plugin = plugin;
        this.sender = sender;
        this.chatHandler = new WizardChatHandler(this, this.sender);
    }

    @Override
    public void run() {
        this.chatHandler.setup();

        while (true) {
            Step step = executeNextStep();

            if (step == Step.EXIT) {
                // Break loop, cleanup
                break;
            } else if (step != Step.REPEAT) {
                this.currentStep = step;
            }
        }

        // TODO: plugins created, summary etc..

        this.cleanup();
    }

    @WizardStep(value = Step.ENTRY, firstRunOutput = "")
    private synchronized Step entry() {
        this.sender.sendMessage("Welcome to the Lukkit Plugin Wizard!");
        this.sender.sendMessage("You'll be asked a few questions and a plugin will be generated by the end.");
        return Step.NAME;
    }

    @WizardStep(value = Step.NAME, firstRunOutput = "First off let's get a plugin name sorted")
    private synchronized Step name() {
        this.sender.sendMessage("Naming your plugin:");
        this.sender.sendMessage("Legal names are [a-z], [A-Z], [0-9], hypens (-) and underscores (_).");
        this.sender.sendMessage("It also must start with an uppercase or lowercase letter. Makes the name tidier.");

        String tempName = this.chatHandler.getInput();

        if (!tempName.matches(nameRegex)) {
            this.sender.sendMessage("Invalid name, let's try that again...");
            return Step.REPEAT;
        }

        this.sender.sendMessage("Cool, that's one this done.");

        return Step.AUTHOR;
    }

    @WizardStep(value = Step.AUTHOR, firstRunOutput = "Now we'll move on to an author name.")
    private synchronized Step author() {
        // TODO regex test
        this.sender.sendMessage("To set the name to your own, just type \"self\" (without the quotes).");
        return Step.DESC;
    }

    @WizardStep(value = Step.DESC, firstRunOutput = "Next up is a description!")
    private synchronized Step description() {
        this.sender.sendMessage("If you want to write this over multiple lines, add a backslash (\\) at the end of the line.");
        this.sender.sendMessage("Enter a description:");

        StringBuilder description = new StringBuilder();
        do {
            description.append(this.chatHandler.getInput());
        } while (description.toString().endsWith("\\"));

        String finalDesc = description.toString().replace("\\", "\n");

        this.sender.sendMessage("Cool, description is done.");

        return Step.VERSION;
    }

    @WizardStep(value = Step.VERSION, firstRunOutput = "Your version should look something like 1.0.0, but you can do whatever really. Don't go too crazy.")
    private synchronized Step version() {
        this.sender.sendMessage("Input your version:");
        String version = this.chatHandler.getInput();

        return Step.EXIT;
    }

    private Step executeNextStep() {
        Method[] methods = this.getClass().getDeclaredMethods();
        Method finalMethod = null;

        for (Method m : methods) {
            if (m.isAnnotationPresent(WizardStep.class) && m.getAnnotation(WizardStep.class).value() == this.currentStep) {
                finalMethod = m;
                break;
            }
        }

        if (finalMethod != null) {
            try {
                // Space out the steps in chat
                this.printBreak();

                // Get a first time only message and send it if it's the first time
                String preMessage = finalMethod.getAnnotation(WizardStep.class).firstRunOutput();
                if (!preMessage.isEmpty()) this.sender.sendMessage(preMessage);

                //noinspection ConfusingArgumentToVarargsMethod
                return (Step) finalMethod.invoke(this, null);
            } catch (IllegalAccessException | InvocationTargetException e) {
                Main.instance.getLogger().severe(e.getLocalizedMessage());
            }
        }

        // TODO: null or error
        Main.instance.getLogger().severe("Could not run method for Step " + this.currentStep.toString());
        cleanup();
        return Step.EXIT;
    }

    private void printBreak() {
        this.sender.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "\n--------------------------------\n" + ChatColor.RESET);
    }

    private void finalizePlugin() {
        // stub
    }

    public void cleanup() {
        this.sender.sendMessage(ChatColor.GREEN + "Quitting Lukkit plugin wizard...");
        this.chatHandler.cleanup();
        Main.instance.removeWizard(this);
    }
}
