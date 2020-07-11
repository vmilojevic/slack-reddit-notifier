package com.vladan.redditnotifier.repository;

import com.vladan.redditnotifier.domain.KeywordOccurrence;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface KeywordOccurrenceRepository extends JpaRepository<KeywordOccurrence, Long> {

    List<KeywordOccurrence> findAllByWordAndDateBetweenOrderByDateAsc(String word, LocalDate startDate, LocalDate endDate);

    List<KeywordOccurrence> findAllByWord(String word);

    @Query(value = "SELECT AVG(a.rcount) FROM " +
            "(SELECT COUNT(*) AS rcount FROM slack_bot.keyword_occurrence WHERE word = :word " +
            "GROUP BY date) a",
            nativeQuery = true)
    Float averageOccurrencesPerDay(@Param("word") String word);

    @Query(value = "SELECT subreddit FROM slack_bot.keyword_occurrence " +
            "WHERE word = :word GROUP BY subreddit ORDER BY COUNT(*) DESC",
            nativeQuery = true)
    List<String> topSubredditsByWord(@Param("word") String word, Pageable pageable);

    @Query(value = "SELECT (SELECT COUNT(*) FROM slack_bot.keyword_occurrence WHERE word = :word AND sentiment = :sentiment) " +
            "* 100 " +
            "/ (SELECT COUNT(*) FROM slack_bot.keyword_occurrence WHERE word = :word) ",
            nativeQuery = true)
    Float sentimentPercentagesByWord(@Param("word") String word, @Param("sentiment") String sentiment);

}
