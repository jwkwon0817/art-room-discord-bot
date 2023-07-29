package me.jwkwon0817.listeners;

import ch.qos.logback.classic.Logger;
import me.jwkwon0817.secure.Bot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class PinNotice extends ListenerAdapter {
	
	Logger logger = (Logger)LoggerFactory.getLogger(PinNotice.class);
	
	@Override
	public void onReady(ReadyEvent e) {
		
		List<TextChannel> channels = new ArrayList<>(e.getJDA().getCategoryById(Bot.PIN_NOTICE_CATEGORY_ID).getTextChannels());
		channels.add(e.getJDA().getTextChannelById(Bot.PHOTO_CHANNEL_ID));
		
		channels.forEach((channel) -> {
			if (!channel.getHistory().retrievePast(1).complete().get(0).getAuthor().getId().equals(e.getJDA().getSelfUser().getId())) {
				sendNotice(e, channel);
				logger.info("Sent pin notice to " + channel.getName());
			}
		});
	}
	
	@Override
	public void onMessageReceived(MessageReceivedEvent e) {
		TextChannel channel = e.getChannel().asTextChannel();
		
		if (e.getAuthor().isBot()) return;
		
		if (channel.getParentCategory().getId().equals(Bot.PIN_NOTICE_CATEGORY_ID) || channel.getId().equals(Bot.PHOTO_CHANNEL_ID)) {
			// get message which selfuser sent
			sendNotice(e, channel);
			
			logger.info("Deleted message from " + e.getAuthor().getAsTag() + " in " + channel.getName());
		}
	}
	
	private static void sendNotice(Event e, TextChannel channel) {
		channel.getHistory().retrievePast(100).queue(messages -> {
			for (Message message : messages) {
				if (message.getAuthor().equals(e.getJDA().getSelfUser())) {
					message.delete().queue();
				}
			}
			
			MessageEmbed embed = new EmbedBuilder()
				.setTitle("**의견이나 반응은 스레드를 이용해 주시기 바랍니다.**")
				.setColor(Color.CYAN)
				.build();
			
			channel.sendMessageEmbeds(embed).queue();
		});
	}
}
