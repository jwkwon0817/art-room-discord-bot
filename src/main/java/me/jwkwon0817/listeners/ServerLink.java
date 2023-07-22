package me.jwkwon0817.listeners;

import ch.qos.logback.classic.Logger;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.utils.FileUpload;
import org.apache.commons.io.IOUtils;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ServerLink extends ListenerAdapter {
	final String guildA = "1073102522769735730"; // C<>DE
	final String guildB = "988101399407648838"; // 건전 예술방
	final String channelA = "1085227532661567558"; // C<>DE
	final String channelB = "1085171809860730890"; // 건전 예술방
	
	Logger logger = (Logger) LoggerFactory.getLogger(ServerLink.class);
	
	List<String> blackList = new ArrayList<>(List.of(new String[]{
	
	}));
	
	@Override
	public void onMessageReceived(MessageReceivedEvent e) {
		User user = e.getAuthor();
		
		if (!(e.getChannel().getId().equals(channelA) || e.getChannel().getId().equals(channelB))) return;
		
		// Check user is bot
		if (user.isBot()) return;
		
		if (blackList.contains(user.getId())) {
			e.getMessage().delete().queue();
			return;
		}
		
		// Answer embeds
		EmbedBuilder eb = new EmbedBuilder();
		eb.setAuthor(user.getName(), null, user.getAvatarUrl());
		eb.setThumbnail(e.getGuild().getIconUrl());
		eb.setDescription(e.getMessage().getContentRaw());
		eb.setTitle("**" + e.getMember().getUser().getId() + " 님의 질문**");
		eb.setFooter(e.getGuild().getName(), e.getGuild().getIconUrl());
		eb.setColor(Color.CYAN);
		eb.setTimestamp(e.getMessage().getTimeCreated());
		
		// Buttons
		Button answerButton = Button.primary("answer", "답변하기");
		Button solvedButton = Button.success("solved", "해결 표시하기");
		Button deleteButton = Button.danger("delete", "삭제하기");
		
		try {
			e.getMessage().delete().queue();
			Guild oppositeGuild = e.getJDA().getGuildById(e.getGuild().getId().equals(guildA) ? guildB : guildA);
			assert oppositeGuild != null;
			TextChannel currentTextChannel = e.getChannel().asTextChannel();
			TextChannel oppositeTextChannel = oppositeGuild.getTextChannelById(currentTextChannel.getId().equals(channelA) ? channelB : channelA);
			assert oppositeTextChannel != null;
			if (e.getMessage().getAttachments().isEmpty()) {
				oppositeTextChannel.sendMessageEmbeds(eb.build()).setActionRow(answerButton).queue(message -> {
					eb.addField("ID", message.getId(), false);
					currentTextChannel.sendMessageEmbeds(eb.build()).setActionRow(answerButton, solvedButton, deleteButton).queue(msg -> {
						eb.clearFields();
						eb.addField("ID", msg.getId(), false);
						message.editMessageEmbeds(eb.build()).queue();
					});
				});
				
			} else {
				List<Message.Attachment> attachments = e.getMessage().getAttachments();
				List<FileUpload> files = new ArrayList<>();
				
				for (Message.Attachment attachment : attachments) {
					InputStream attachmentStream = attachment.retrieveInputStream().join();
					try {
						FileUpload upload = FileUpload.fromData(IOUtils.toByteArray(attachmentStream), attachment.getFileName());
						files.add(upload);
					} catch (IOException ex) {
						throw new RuntimeException(ex);
					}
				}
				
				MessageCreateAction messageAction = oppositeTextChannel.sendMessageEmbeds(eb.build()).addFiles(files).setActionRow(answerButton);
				MessageCreateAction finalMessageAction = currentTextChannel.sendMessageEmbeds(eb.build()).addFiles(files).setActionRow(answerButton, solvedButton, deleteButton);
				
				messageAction.queue(message -> {
					finalMessageAction.queue(msg -> {
						eb.addField("ID", message.getId(), false);
						msg.editMessageEmbeds(eb.build()).queue();
						eb.clearFields();
						eb.addField("ID", msg.getId(), false);
						message.editMessageEmbeds(eb.build()).queue();
					});
				});
				
			}
		} catch (Exception ex) {
			e.getChannel().sendMessage("오류가 발생했습니다.").queue();
			ex.printStackTrace();
		}
	}
	
	
	
	@Override
	public void onButtonInteraction(ButtonInteractionEvent e) {
		if (!(e.getChannel().getId().equals(channelA) || e.getChannel().getId().equals(channelB))) return;
		
		Guild oppositeGuild = e.getJDA().getGuildById(e.getGuild().getId().equals(guildA) ? guildB : guildA);
		assert oppositeGuild != null;
		TextChannel currentTextChannel = e.getChannel().asTextChannel();
		TextChannel oppositeTextChannel = oppositeGuild.getTextChannelById(currentTextChannel.getId().equals(channelA) ? channelB : channelA);
		assert oppositeTextChannel != null;
		
		if (blackList.contains(e.getUser().getId())) {
			EmbedBuilder eb = new EmbedBuilder();
			eb.setTitle("**당신은 블랙리스트입니다.**");
			eb.setDescription("당신은 이 봇을 사용할 수 없습니다.");
			eb.setColor(Color.RED);
			
			e.replyEmbeds(eb.build()).setEphemeral(true).queue();
			return;
		}
		
		switch (e.getComponentId()) {
			case "answer" -> {
				TextInput answerContent = TextInput.create("answer-content", "답변을 입력해 주세요.", TextInputStyle.PARAGRAPH)
					.setMinLength(1)
					.setRequired(true)
					.build();
				Modal answerModal = Modal.create("answer-modal", "답변하기")
					.addActionRows(ActionRow.of(answerContent))
					.build();
				e.replyModal(answerModal).queue();
			}
			
			case "solved" -> {
				String userId = e.getMessage().getEmbeds().get(0).getTitle().split(" ")[0].replace("**", "");
				if (!userId.equals(e.getUser().getId())) {
					EmbedBuilder error = new EmbedBuilder();
					error.setAuthor(e.getUser().getName(), null, e.getUser().getAvatarUrl());
					error.setTitle("**오류가 발생했습니다.**");
					error.setColor(Color.RED);
					error.setTimestamp(e.getMessage().getTimeCreated());
					error.setDescription("당신은 이 메시지의 작성자가 아닙니다.");
					e.replyEmbeds(error.build()).setEphemeral(true).queue();
					return;
				}
				
				Button solvedButton = Button.primary("solved", "해결 완료");
				Button answerButton = Button.primary("answer", "답변하기");
				
				e.getMessage().editMessageEmbeds().setEmbeds(e.getMessage().getEmbeds()).setActionRow(solvedButton.asDisabled()).queue();
				String thereId = e.getMessage().getEmbeds().get(0).getFields().get(0).getValue();
				
				assert thereId != null;
				oppositeTextChannel.retrieveMessageById(thereId).queue(message -> {
					message.editMessageEmbeds().setEmbeds(message.getEmbeds()).setActionRow(answerButton.asDisabled()).queue();
				});
				
				EmbedBuilder eb = new EmbedBuilder();
				eb.setAuthor(e.getUser().getName(), null, e.getUser().getAvatarUrl());
				eb.setTitle("**해결 표시 완료**");
				eb.setColor(Color.GREEN);
				eb.setTimestamp(e.getMessage().getTimeCreated());
				eb.setDescription("성공적으로 해결 표시를 했습니다.");
				e.replyEmbeds(eb.build()).setEphemeral(true).queue();
			}
			
			case "delete" -> {
				String userId = e.getMessage().getEmbeds().get(0).getTitle().split(" ")[0].replace("**", "");
				if (userId.equals(e.getUser().getId())) {
					String id = e.getMessage().getEmbeds().get(0).getFields().get(0).getValue();
					assert id != null;
					
					oppositeTextChannel.retrieveMessageById(id).queue(message -> {
						message.delete().queue();
					});
					
					deleteMessage(e);
				} else {
					EmbedBuilder error = new EmbedBuilder();
					error.setAuthor(e.getUser().getName(), null, e.getUser().getAvatarUrl());
					error.setTitle("**오류가 발생했습니다.**");
					error.setColor(Color.RED);
					error.setTimestamp(e.getMessage().getTimeCreated());
					error.setDescription("당신은 이 메시지의 작성자가 아닙니다.");
					e.replyEmbeds(error.build()).setEphemeral(true).queue();
				}
			}
		}
	}
	
	static void deleteMessage(ButtonInteractionEvent e) {
		e.getMessage().delete().queue();
		
		EmbedBuilder eb = new EmbedBuilder();
		eb.setAuthor(e.getUser().getName(), null, e.getUser().getAvatarUrl());
		eb.setTitle("**삭제 완료**");
		eb.setColor(Color.GREEN);
		eb.setTimestamp(e.getMessage().getTimeCreated());
		eb.setDescription("**성공적으로 메시지를 삭제했습니다.**");
		e.replyEmbeds(eb.build()).setEphemeral(true).queue();
	}
	
	@Override
	public void onModalInteraction(ModalInteractionEvent e) {
		if (!(e.getChannel().getId().equals(channelA) || e.getChannel().getId().equals(channelB))) return;
		
		Guild oppositeGuild = e.getGuild().getId().equals(guildA) ? e.getJDA().getGuildById(guildB) : e.getJDA().getGuildById(guildA);
		TextChannel currentTextChannel = e.getChannel().asTextChannel();
		TextChannel oppositeTextChannel = oppositeGuild.getTextChannelById(currentTextChannel.getId().equals(channelA) ? channelB : channelA);
		
		EmbedBuilder eb = new EmbedBuilder();
		eb.setAuthor(e.getUser().getName(), null, e.getUser().getAvatarUrl());
		eb.setTitle("**답변**");
		eb.setThumbnail(e.getGuild().getIconUrl());
		eb.setFooter(e.getGuild().getName(), e.getGuild().getIconUrl());
		eb.setColor(Color.CYAN);
		eb.setTimestamp(e.getMessage().getTimeCreated());
		
		EmbedBuilder result = new EmbedBuilder();
		result.setAuthor(e.getUser().getName(), null, e.getUser().getAvatarUrl());
		
		try {
			if (e.getModalId().equals("answer-modal")) {
				eb.setDescription(e.getValue("answer-content").getAsString());
				String messageId = e.getMessage().getEmbeds().get(0).getFields().get(0).getValue();
				assert messageId != null;
				Message message = oppositeTextChannel.retrieveMessageById(messageId).submit().join();
				embedIdToUserMention(e, eb, message);
			}
			result.setTitle("**답변 완료**");
			result.setColor(Color.GREEN);
			result.setTimestamp(e.getMessage().getTimeCreated());
			result.setDescription("답변이 성공적으로 전달되었습니다.");
		} catch (NullPointerException exception) {
			result.setTitle("**답변 실패**");
			result.setColor(Color.RED);
			result.setTimestamp(e.getMessage().getTimeCreated());
			result.setDescription("오류가 발생했습니다. 개발자에게 문의하세요.");
			
			exception.printStackTrace();
		} catch (Exception exception) {
		}
		e.replyEmbeds(result.build()).setEphemeral(true).queue();
	}
	
	static void embedIdToUserMention(ModalInteractionEvent e, EmbedBuilder eb, Message message) {
		String userMention = ("<@" + message.getEmbeds().get(0).getTitle().split(" ")[0] + ">").replace("**", "");
		message.reply(userMention).addEmbeds(eb.build()).queue();
		e.getMessage().reply(userMention).addEmbeds(eb.build()).queue();
	}
}