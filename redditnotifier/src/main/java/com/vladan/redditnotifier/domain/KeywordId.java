package com.vladan.redditnotifier.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KeywordId implements Serializable {

    private String word;
    private String channelId;

}
