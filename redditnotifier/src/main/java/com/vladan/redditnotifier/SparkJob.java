package com.vladan.redditnotifier;

import com.vladan.redditnotifier.config.KafkaConfig;
import com.vladan.redditnotifier.domain.Keyword;
import com.vladan.redditnotifier.model.Submission;
import com.vladan.redditnotifier.service.KeywordOccurrenceService;
import com.vladan.redditnotifier.service.KeywordService;
import com.vladan.redditnotifier.service.SlackApiService;
import com.vladan.redditnotifier.util.VaderUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.spark.SparkConf;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka010.ConsumerStrategies;
import org.apache.spark.streaming.kafka010.KafkaUtils;
import org.apache.spark.streaming.kafka010.LocationStrategies;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import scala.Tuple2;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class SparkJob implements CommandLineRunner {

    private final Collection<String> topics = Collections.singletonList("unfiltered");
    private final SparkConf sparkConf;
    private final KafkaConfig kafkaConfig;

    public SparkJob(SparkConf sparkConf, KafkaConfig kafkaConfig) {
        this.sparkConf = sparkConf;
        this.kafkaConfig = kafkaConfig;
    }

    @Override
    @Async
    public void run(String... args) {
        log.info("Running Spark Service...");

        // Create context with a 20 seconds batch interval
        JavaStreamingContext jsc = new JavaStreamingContext(sparkConf, Durations.seconds(20));

        // Create direct kafka stream with brokers and topics
        JavaInputDStream<ConsumerRecord<String, Submission>> messages = KafkaUtils.createDirectStream(
                jsc, LocationStrategies.PreferConsistent(),
                ConsumerStrategies.Subscribe(topics, kafkaConfig.configs()));

        JavaDStream<Submission> results = messages.map(ConsumerRecord::value);

        JavaDStream<Submission> submissionsWithText = results
                .filter(data -> !StringUtils.isEmpty(data.getText()));

        JavaPairDStream<Submission, List<Keyword>> subKeywordsPairs = submissionsWithText
                .mapToPair(submission -> new Tuple2<>(
                        submission, KeywordService.getAllKeywordsFromText(submission.getText())))
                .filter(tuple -> !CollectionUtils.isEmpty(tuple._2));

        JavaPairDStream<Submission, Float> compounds = subKeywordsPairs.mapToPair(
                tuple -> new Tuple2<>(tuple._1, VaderUtils.generateCompound(tuple._1.getText())));

        JavaPairDStream<Submission, Tuple2<Float, List<Keyword>>> processedSubmissions = compounds.join(subKeywordsPairs);

        processedSubmissions.foreachRDD(rdd -> {
            rdd.foreach(tuple -> {
                // Send notification to Slack
                SlackApiService.sendNotification(tuple._1, tuple._2._1, tuple._2._2);
                // Insert occurrences into database
                KeywordOccurrenceService.saveKeywordsOccurrence(tuple._1, tuple._2._1, tuple._2._2);
            });
        });

        // Start the computation
        jsc.start();
        try {
            jsc.awaitTermination();
        } catch (InterruptedException e) {
            log.error("Spark Service interrupted", e);
        }
    }
}
