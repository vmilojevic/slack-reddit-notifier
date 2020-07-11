package com.vladan.redditnotifier.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Submission implements Serializable {

    private String id;
    private String title;
    private String text;
    private String link;
    private String subreddit;
    private String type;

}
