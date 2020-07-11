package com.vladan.redditnotifier.controller;

import com.google.gson.Gson;
import com.slack.api.app_backend.slash_commands.response.SlashCommandResponse;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.util.json.GsonFactory;
import com.vladan.redditnotifier.service.KeywordOccurrenceService;
import com.vladan.redditnotifier.service.KeywordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SlackController {

    private static final Gson gson = GsonFactory.createSnakeCase();
    private final String IN_CHANNEL = "in_channel";
    private final String EPHEMERAL = "ephemeral";

    private KeywordService keywordService;
    private KeywordOccurrenceService keywordOccurrenceService;

    @Autowired
    public SlackController(KeywordService keywordService,
                           KeywordOccurrenceService keywordOccurrenceService) {
        this.keywordService = keywordService;
        this.keywordOccurrenceService = keywordOccurrenceService;
    }

    @RequestMapping(value = "/track", method = RequestMethod.POST)
    public ResponseEntity<String> addKeyWord(@RequestParam("text") String text,
                                             @RequestParam("channel_id") String channelId) {
        String word = text.trim().split(" ")[0].toLowerCase();
        keywordService.saveKeyword(word, channelId);

        return ResponseEntity.ok(gson.toJson(SlashCommandResponse.builder()
                .responseType(IN_CHANNEL)
                .text("Added keyword: " + word).build()));
    }

    @RequestMapping(value = "/untrack", method = RequestMethod.POST)
    public ResponseEntity<String> removeKeyWord(@RequestParam("text") String text,
                                                @RequestParam("channel_id") String channelId) {
        String word = text.trim().split(" ")[0].toLowerCase();
        keywordService.deleteKeyword(word, channelId);

        return ResponseEntity.ok(gson.toJson(SlashCommandResponse.builder()
                .responseType(IN_CHANNEL)
                .text("Deleted keyword: " + word).build()));
    }

    @RequestMapping(value = "/keywords", method = RequestMethod.POST)
    public ResponseEntity<String> getAllKeywords(@RequestParam("channel_id") String channelId) {
        List<String> wordList = keywordService.getAllWordsForChannel(channelId);
        if (!CollectionUtils.isEmpty(wordList)) {
            String words = String.join(", ", wordList);

            return ResponseEntity.ok(gson.toJson(SlashCommandResponse.builder()
                    .responseType(IN_CHANNEL)
                    .text("You track following keywords: " + words).build()));
        } else {

            return ResponseEntity.ok(gson.toJson(SlashCommandResponse.builder()
                    .responseType(EPHEMERAL)
                    .text("You don't track any keywords on this channel.").build()));
        }
    }

    @RequestMapping(value = "/statistics", method = RequestMethod.POST)
    public ResponseEntity<String> getStatisticsForm(@RequestParam("channel_id") String channelId,
                                                    @RequestParam("text") String text) {
        String word = text.trim().split(" ")[0].toLowerCase();
        if (keywordService.exists(word, channelId)) {
            List<LayoutBlock> blocks = keywordOccurrenceService.composeStatisticsMessage(word);

            return ResponseEntity.ok(gson.toJson(SlashCommandResponse.builder()
                    .responseType(IN_CHANNEL)
                    .text("Test")
                    .blocks(blocks).build()));
        } else {
            return ResponseEntity.ok(gson.toJson(SlashCommandResponse.builder()
                    .responseType(EPHEMERAL)
                    .text("You don't track this word! Type /track " + text + " to start tracking it.").build()));
        }
    }

}
