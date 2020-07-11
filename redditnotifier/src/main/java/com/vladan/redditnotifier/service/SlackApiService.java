package com.vladan.redditnotifier.service;

import com.vladan.redditnotifier.domain.Keyword;
import com.vladan.redditnotifier.model.NotificationModel;
import com.vladan.redditnotifier.model.Submission;
import com.vladan.redditnotifier.util.VaderUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class SlackApiService {

    private static String SLACK_POST_MESSAGE_URL;
    private static String SLACK_BOT_TOKEN;
    private static RestTemplate restTemplate;
    private static HttpHeaders headers;

    @Value("${slack.bot.token}")
    public void setSlackBotToken(String token) {
        SLACK_BOT_TOKEN = token;
    }

    @Value("${slack.post.message.url}")
    public void setSlackPostMessageUrl(String url) {
        SLACK_POST_MESSAGE_URL = url;
    }

    @PostConstruct
    public void init() {
        restTemplate = new RestTemplate();
        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(SLACK_BOT_TOKEN);
    }

    public static void sendNotification(Submission submission, Float compound, List<Keyword> keywords) {
        Map<String, List<String>> channelWordsMap = new HashMap<>();
        keywords.forEach(kw -> {
            if (channelWordsMap.containsKey(kw.getChannelId())) {
                channelWordsMap.get(kw.getChannelId()).add(kw.getWord());
            } else {
                channelWordsMap.put(kw.getChannelId(), new ArrayList<>(Arrays.asList(kw.getWord())));
            }
        });

        channelWordsMap.forEach((channel, words) -> {
            String message = composeMessage(submission, compound, words);
            NotificationModel notificationModel = new NotificationModel();
            notificationModel.setChannel(channel);
            notificationModel.setText(message);

            send(notificationModel);
        });
    }

    private static void send(NotificationModel notificationModel) {
        HttpEntity<NotificationModel> entity = new HttpEntity<>(notificationModel, headers);
        ResponseEntity<Object> response = restTemplate.exchange(SLACK_POST_MESSAGE_URL, HttpMethod.POST, entity, Object.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            log.error("Error while sending message to channel: " + notificationModel.getChannel());
        }
    }

    private static String composeMessage(Submission submission, Float compound, List<String> words) {
        return "Found a " + VaderUtils.getSentimentType(compound) + " " +
                submission.getType() + " for keywords: " +
                String.join(", ", words) +
                "\n" + submission.getLink();
    }
}
