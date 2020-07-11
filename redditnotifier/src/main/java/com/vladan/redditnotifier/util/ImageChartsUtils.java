package com.vladan.redditnotifier.util;

import com.vladan.redditnotifier.domain.KeywordOccurrence;
import com.vladan.redditnotifier.model.SentimentEnum;
import lombok.experimental.UtilityClass;
import org.apache.http.client.utils.URIBuilder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

@UtilityClass
public class ImageChartsUtils {

    private static final String POSITIVE_COLOR = "2bed3e";
    private static final String NEUTRAL_COLOR = "47b9ed";
    private static final String NEGATIVE_COLOR = "f52614";
    private static final String POSITIVE = "Positive";
    private static final String NEUTRAL = "Neutral";
    private static final String NEGATIVE = "Negative";

    public static String generateUrl(String word, List<KeywordOccurrence> occurrences) {
        HashMap<LocalDate, List<KeywordOccurrence>> map = new HashMap<>();
        occurrences.forEach(keywordOccurrence -> {
            if (map.containsKey(keywordOccurrence.getDate())) {
                map.get(keywordOccurrence.getDate()).add(keywordOccurrence);
            } else {
                map.put(keywordOccurrence.getDate(), new ArrayList<>(Arrays.asList(keywordOccurrence)));
            }
        });

        TreeMap<LocalDate, List<KeywordOccurrence>> treeMap = new TreeMap<>(map);

        List<String> dates = new ArrayList<>();
        List<String> positive = new ArrayList<>();
        List<String> neutral = new ArrayList<>();
        List<String> negative = new ArrayList<>();
        treeMap.forEach((date, occurrenceList) -> {
            dates.add(date.toString());

            int posCount = (int) occurrenceList.stream()
                    .filter(occurrence -> occurrence.getSentiment().equals(SentimentEnum.positive))
                    .count();
            positive.add(String.valueOf(posCount));
            int neuCount = (int) occurrenceList.stream()
                    .filter(occurrence -> occurrence.getSentiment().equals(SentimentEnum.neutral))
                    .count();
            neutral.add(String.valueOf(neuCount));
            int negCount = (int) occurrenceList.stream()
                    .filter(occurrence -> occurrence.getSentiment().equals(SentimentEnum.negative))
                    .count();
            negative.add(String.valueOf(negCount));
        });
        String positiveCountString = String.join(",", positive);
        String neutralCountString = String.join(",", neutral);
        String negativeCountString = String.join(",", negative);

        URIBuilder builder = new URIBuilder()
                .setScheme("https")
                .setHost("image-charts.com")
                .setPath("/chart")
                .addParameter("chco", String.join(",", Arrays.asList(POSITIVE_COLOR, NEUTRAL_COLOR, NEGATIVE_COLOR)))
                .addParameter("chdl", String.join("|", Arrays.asList(POSITIVE, NEUTRAL, NEGATIVE)))
                .addParameter("chdlp", "r")
                .addParameter("chs", "700x300")
                .addParameter("cht", "bvg")
                .addParameter("chtt", "Number of occurrences for word: " + word)
                .addParameter("chxt", "x,y")
                .addParameter("chxl", "0:|" + String.join("|", dates))
                .addParameter("chd", "t:" + String.join("|",
                        Arrays.asList(positiveCountString, neutralCountString, negativeCountString)))
                .addParameter("chg", "1,1");

        return builder.toString();
    }
}
