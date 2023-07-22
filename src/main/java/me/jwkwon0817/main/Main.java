package me.jwkwon0817.main;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import me.jwkwon0817.listeners.CallManager;
import me.jwkwon0817.listeners.PinNotice;
import me.jwkwon0817.listeners.ServerLink;
import me.jwkwon0817.secure.Bot;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.slf4j.LoggerFactory;

public class Main extends ListenerAdapter {
	public static void main(String[] args) throws Exception {
		Logger jdaLogger = (Logger) LoggerFactory.getLogger("net.dv8tion.jda");
		Logger logger = (Logger) LoggerFactory.getLogger(Main.class);
		jdaLogger.setLevel(Level.OFF);
		
		JDA jda = JDABuilder.createDefault(Bot.TOKEN)
				.disableCache(CacheFlag.MEMBER_OVERRIDES, CacheFlag.VOICE_STATE)
				.setBulkDeleteSplittingEnabled(false)
				.setStatus(OnlineStatus.DO_NOT_DISTURB)
				.setMemberCachePolicy(MemberCachePolicy.VOICE.or(MemberCachePolicy.OWNER))
				.setChunkingFilter(ChunkingFilter.NONE)
				.disableIntents(GatewayIntent.GUILD_PRESENCES, GatewayIntent.GUILD_MESSAGE_TYPING)
				.setLargeThreshold(50)
				.setAutoReconnect(true)
				
				.addEventListeners(
					new ServerLink(),
					new CallManager(),
					new PinNotice()
				)
				
				.setStatus(OnlineStatus.ONLINE)
				.enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_BANS, GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.GUILD_EMOJIS_AND_STICKERS, GatewayIntent.GUILD_MESSAGE_REACTIONS, GatewayIntent.GUILD_MESSAGE_TYPING)
				.build();
		
		jda.awaitReady();
		
		jda.updateCommands().addCommands(
				new CommandData[]{
					Commands.slash("관리자호출", "관리자를 호출하는 메시지를 보냅니다.")
						.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
				}).queue();
	}
}