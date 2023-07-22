package me.jwkwon0817.listeners;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.*;

public class CallManager extends ListenerAdapter {
	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent e) {
		User bot = e.getJDA().getSelfUser();
		
		if (e.getName().equals("관리자호출")) {
			Button callButton = Button.primary("call-manager", "관리자 호출");
			
			MessageEmbed callEmbed = new EmbedBuilder()
				.setAuthor(bot.getName(), null, bot.getAvatarUrl())
				.setTitle("**관리자 호출**")
				.setDescription("인증이 제대로 되지 않거나, 문제가 생겼을 경우에 아래 버튼을 눌러 관리자를 호출해 주세요!")
				.setColor(Color.CYAN)
				.setFooter(e.getGuild().getName(), e.getGuild().getIconUrl())
				.build();
			
			e.replyEmbeds(callEmbed).setActionRow(callButton).queue();
		}
	}
	
	@Override
	public void onButtonInteraction(ButtonInteractionEvent e) {
		if (e.getComponentId().equals("call-manager")) {
			e.reply("관리자를 호출했습니다!").setEphemeral(true).queue();
		}
	}
}
