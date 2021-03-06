package me.joehosten.illusivestafftracker;

import games.negative.framework.BasePlugin;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import me.joehosten.illusivestafftracker.commands.CommandCheck;
import me.joehosten.illusivestafftracker.commands.discord.DiscordCommandCheckLegacy;
import me.joehosten.illusivestafftracker.commands.discord.DiscordCommandHelp;
import me.joehosten.illusivestafftracker.listeners.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.awt.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public final class IllusiveStaffTracker extends BasePlugin {

    @Getter
    @Setter
    private static IllusiveStaffTracker instance;
    @Getter
    public HashMap<UUID, Long> map = new HashMap<>();
    @Getter
    @Setter
    private TextChannel textChannel;

    @SneakyThrows
    @Override
    public void onEnable() {
        setInstance(this);

        getConfig().options().copyDefaults();
        saveConfig();

        registerListeners(new PlayerLogListener(this));
        registerCommands(new CommandCheck());

        new DateCheckRunnable(this).runTaskTimerAsynchronously(this, 0L, 3600L * 20L);
        new PlayerSaveTask().runTaskTimerAsynchronously(this, 0L, 120L * 20L);
        new DailySummaryTask(this).runTaskTimerAsynchronously(this, 0L, 86400L * 20L);

        // Discord
        String botToken = "[redacted]";
        String textChannelId = "965844461772996628";
//        new SlashCommandListener();
        JDA jda = JDABuilder.createDefault(botToken).build().awaitReady();

        textChannel = jda.getTextChannelById(textChannelId);
        jda.addEventListener(new DiscordCommandCheckLegacy(), new DiscordCommandHelp(), new DiscordBotPingEvent());
//        registerDevCommands();

        sendEmbed(Bukkit.getOfflinePlayer("hypews"), "Bot online", true, Color.RED);
    }

    @Override
    public void onDisable() {
        new PlayerLogListener(this).saveAllPlayers();
    }

    public void sendEmbed(OfflinePlayer player, String content, boolean contentAuthorLine, Color color) {
        if (textChannel == null) return;

        EmbedBuilder builder = new EmbedBuilder()
                .setAuthor(contentAuthorLine ? content : player.getName(),
                        null,
                        "https://crafatar.com/avatars/" + player.getUniqueId() + "?overlay=1");
        builder.setColor(color);
        if (!contentAuthorLine) {
            builder.setDescription(content);
        }
        LocalDate ld = LocalDate.now();
        builder.setFooter(ld.getDayOfMonth() + "/" + ld.getMonthValue() + "/" + ld.getYear() + " (day/month/year)");

        textChannel.sendMessageEmbeds(builder.build()).queue();
    }

    public String convertTime(Long ms) {
        int seconds = (int) (ms / 1000) % 60;
        int minutes = (int) ((ms / (1000 * 60)) % 60);
        int hours = (int) ((ms / (1000 * 60 * 60)) % 24);

        return hours + " hours, " + minutes + " minutes and "
                + seconds + " seconds";

    }

    public Role checkMemberRoles(Member member, String name) {
        List<Role> roles = member.getRoles();
        return roles.stream()
                .filter(role -> role.getName().equals(name)) // filter by role name
                .findFirst() // take first result
                .orElse(null); // else return null
    }

}
