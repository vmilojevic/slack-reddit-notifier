package com.vladan.redditnotifier.repository;

import com.vladan.redditnotifier.domain.Keyword;
import com.vladan.redditnotifier.domain.KeywordId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface KeywordRepository extends JpaRepository<Keyword, KeywordId> {

    List<Keyword> findAllByWord(String word);

}
