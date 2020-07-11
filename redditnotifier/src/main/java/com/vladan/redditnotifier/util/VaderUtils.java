package com.vladan.redditnotifier.util;

import com.vader.sentiment.analyzer.SentimentAnalyzer;
import com.vladan.redditnotifier.model.SentimentEnum;
import lombok.experimental.UtilityClass;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class VaderUtils {

    private static final String COMPOUND_KEY = "compound";

    public static Float generateCompound(String text) throws IOException {
        float compound = 0.0F;
        List<String> sentences = splitToSentences(text);
        if (!CollectionUtils.isEmpty(sentences)) {
            for (String sentence : sentences) {
                SentimentAnalyzer sentimentAnalyzer = new SentimentAnalyzer(sentence);
                sentimentAnalyzer.analyze();
                compound += sentimentAnalyzer.getPolarity().get(COMPOUND_KEY);
            }
            return BigDecimal.valueOf(compound / sentences.size())
                    .setScale(2, RoundingMode.HALF_UP).floatValue();
        }
        return compound;
    }

    public static SentimentEnum getSentimentType(Float compound) {
        if (compound.compareTo(-0.05F) <= 0) {
            return SentimentEnum.negative;
        } else if (compound.compareTo(0.05F) >= 0) {
            return SentimentEnum.positive;
        } else {
            return SentimentEnum.neutral;
        }
    }

    private static List<String> splitToSentences(String text) {
        List<String> sentences = new ArrayList<>();

        BreakIterator iterator = BreakIterator.getSentenceInstance();
        iterator.setText(text);
        int start = iterator.first();
        for (int end = iterator.next(); end != BreakIterator.DONE; start = end, end = iterator.next()) {
            sentences.add(text.substring(start, end));
        }

        return sentences;
    }
}
