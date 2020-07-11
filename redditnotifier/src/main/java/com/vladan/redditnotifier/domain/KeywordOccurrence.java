package com.vladan.redditnotifier.domain;

import com.vladan.redditnotifier.model.SentimentEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KeywordOccurrence implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "word")
    private String word;

    @Column(name = "subreddit")
    private String subreddit;

    @Column(name = "sentiment")
    @Enumerated(EnumType.STRING)
    private SentimentEnum sentiment;

    @Column(name = "date")
    private LocalDate date;
}
