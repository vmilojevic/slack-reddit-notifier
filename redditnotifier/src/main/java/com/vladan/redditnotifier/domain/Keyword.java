package com.vladan.redditnotifier.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import java.io.Serializable;

@Entity
@IdClass(KeywordId.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Keyword implements Serializable {

    @Id
    @Column(name = "word")
    private String word;

    @Id
    @Column(name = "channel_id")
    private String channelId;
}
