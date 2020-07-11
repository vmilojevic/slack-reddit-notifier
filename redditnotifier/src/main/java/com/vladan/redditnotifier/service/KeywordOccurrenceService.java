package com.vladan.redditnotifier.service;

import com.slack.api.model.block.ImageBlock;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.block.SectionBlock;
import com.slack.api.model.block.composition.MarkdownTextObject;
import com.slack.api.model.block.composition.PlainTextObject;
import com.slack.api.model.block.composition.TextObject;
import com.vladan.redditnotifier.domain.Keyword;
import com.vladan.redditnotifier.domain.KeywordOccurrence;
import com.vladan.redditnotifier.model.SentimentEnum;
import com.vladan.redditnotifier.model.Submission;
import com.vladan.redditnotifier.repository.KeywordOccurrenceRepository;
import com.vladan.redditnotifier.util.ImageChartsUtils;
import com.vladan.redditnotifier.util.VaderUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Service
public class KeywordOccurrenceService {

    private static KeywordOccurrenceRepository repository;

    @Autowired
    public KeywordOccurrenceService(KeywordOccurrenceRepository repository) {
        KeywordOccurrenceService.repository = repository;
    }

    public static void saveKeywordsOccurrence(Submission submission, Float compound, List<Keyword> keywords) {
        HashSet<String> words = new HashSet<>();
        keywords.forEach(keyword -> words.add(keyword.getWord()));

        words.forEach(word -> {
            KeywordOccurrence keywordOccurrence = new KeywordOccurrence();
            keywordOccurrence.setWord(word);
            keywordOccurrence.setDate(LocalDate.now());
            keywordOccurrence.setSubreddit(submission.getSubreddit());
            keywordOccurrence.setSentiment(VaderUtils.getSentimentType(compound));

            repository.save(keywordOccurrence);
        });
    }

    public List<LayoutBlock> composeStatisticsMessage(String word) {
        List<LayoutBlock> blocks = new ArrayList<>();

        // Create section block with fields
        List<TextObject> fields = new ArrayList<>();

        List<String> subreddits = repository.topSubredditsByWord(word, PageRequest.of(0, 3));

        if (!CollectionUtils.isEmpty(subreddits)) {
            StringBuilder subredditsBuilder = new StringBuilder("*Top " + subreddits.size() + " subreddits:*");
            subreddits.forEach(str -> subredditsBuilder.append("\n- ").append(str));
            fields.add(composeMarkdownTextObject(subredditsBuilder.toString()));
        }

        BigDecimal positivePerc = getSentimentPercentageForWord(word, SentimentEnum.positive);
        BigDecimal neutralPerc = getSentimentPercentageForWord(word, SentimentEnum.neutral);
        BigDecimal negativePerc = getSentimentPercentageForWord(word, SentimentEnum.negative);

        String sentimentPercString = "*Sentiment percentages:*\n"
                + "- Positive: " + positivePerc.toString() + " %\n"
                + "- Neutral: " + neutralPerc.toString() + " %\n"
                + "- Negative: " + negativePerc.toString() + " %\n";
        fields.add(composeMarkdownTextObject(sentimentPercString));

        SectionBlock statsBlock = SectionBlock.builder()
                .text(composeMarkdownTextObject("Occurrence statistics for word: *" + word + "*"))
                .fields(fields)
                .build();

        blocks.add(statsBlock);

        //Create section block without fields for average occurrences per day
        BigDecimal avg = BigDecimal.valueOf(repository.averageOccurrencesPerDay(word))
                .setScale(2, RoundingMode.HALF_UP);
        SectionBlock averageBlock = composeSectionBlock("*Avg occurrences per day:*\n " + avg.toString(), null);
        blocks.add(averageBlock);

        // Create image block with chart
        List<KeywordOccurrence> lastWeekOccurrences = repository.
                findAllByWordAndDateBetweenOrderByDateAsc(word, LocalDate.now().minusDays(6), LocalDate.now());
        String chartUrl = ImageChartsUtils.generateUrl(word, lastWeekOccurrences);
        ImageBlock imageBlock = composeImageBlock(chartUrl, "Last 7 days chart", "chart.png");
        blocks.add(imageBlock);

        return blocks;
    }

    private ImageBlock composeImageBlock(String imageUrl, String text, String altText) {
        return ImageBlock.builder()
                .title(PlainTextObject.builder().text(text).build())
                .imageUrl(imageUrl)
                .altText(altText)
                .build();
    }

    private MarkdownTextObject composeMarkdownTextObject(String text) {
        return MarkdownTextObject.builder()
                .text(text)
                .build();
    }

    private SectionBlock composeSectionBlock(String text, List<TextObject> fields) {
        return SectionBlock.builder()
                .text(composeMarkdownTextObject(text))
                .fields(fields)
                .build();
    }

    private BigDecimal getSentimentPercentageForWord(String word, SentimentEnum sentiment) {
        return BigDecimal.valueOf(repository.sentimentPercentagesByWord(word, sentiment.name()))
                .setScale(2, BigDecimal.ROUND_FLOOR);
    }
}
