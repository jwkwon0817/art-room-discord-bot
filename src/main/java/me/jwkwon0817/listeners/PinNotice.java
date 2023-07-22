package me.jwkwon0817.listeners;

import me.jwkwon0817.secure.Bot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;

public class PinNotice extends ListenerAdapter {
	@Override
	public void onMessageReceived(MessageReceivedEvent e) {
		TextChannel channel = e.getChannel().asTextChannel();
		
		if (e.getAuthor().isBot()) return;
		
		if (channel.getParentCategory().getId().equals(Bot.PIN_NOTICE_CATEGORY_ID) || channel.getId().equals(Bot.PHOTO_CHANNEL_ID)) {
			// get message which selfuser sent
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
}
