package com.vladan.redditnotifier.service;

import com.vladan.redditnotifier.domain.Keyword;
import com.vladan.redditnotifier.domain.KeywordId;
import com.vladan.redditnotifier.repository.KeywordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class KeywordService {

    private static KeywordRepository repository;

    @Autowired
    public KeywordService(KeywordRepository repository) {
        KeywordService.repository = repository;
    }

    public void saveKeyword(String word, String channelId) {
        KeywordId keywordId = new KeywordId(word, channelId);

        if (!repository.existsById(keywordId)) {
            Keyword keyword = new Keyword(word, channelId);
            repository.save(keyword);
        }
    }

    public void deleteKeyword(String word, String channelId) {
        KeywordId keywordId = new KeywordId(word, channelId);

        if (repository.existsById(keywordId)) {
            repository.deleteById(keywordId);
        }
    }

    public List<String> getAllWordsForChannel(String channelId) {
        Keyword keyword = new Keyword();
        keyword.setChannelId(channelId);
        Example<Keyword> example = Example.of(keyword);

        List<Keyword> keywords = repository.findAll(example);

        if (!CollectionUtils.isEmpty(keywords)) {
            return keywords.stream()
                    .map(Keyword::getWord)
                    .collect(Collectors.toList());
        } else {
            return new ArrayList<>();
        }
    }

    public static List<Keyword> getAllKeywordsFromText(String text) {
        String[] split = text.toLowerCase().split("\\s+");
        String[] words = new HashSet<>(Arrays.asList(split)).toArray(new String[0]);

        List<Keyword> keywordsForText = new ArrayList<>();
        for (String word : words) {
            List<Keyword> keywords = repository.findAllByWord(word);
            if (!CollectionUtils.isEmpty(keywords)) {
                keywordsForText.addAll(keywords);
            }
        }

        return keywordsForText;
    }

    public boolean exists(String word, String channelId) {
        KeywordId id = new KeywordId();
        id.setWord(word);
        id.setChannelId(channelId);
        return repository.existsById(id);
    }
}
