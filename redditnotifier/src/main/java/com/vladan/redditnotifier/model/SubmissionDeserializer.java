package com.vladan.redditnotifier.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Deserializer;

@Slf4j
public class SubmissionDeserializer implements Deserializer<Submission> {

    @Override
    public Submission deserialize(String s, byte[] bytes) {
        ObjectMapper mapper = new ObjectMapper();
        Submission submission = new Submission();

        try {
            submission = mapper.readValue(bytes, Submission.class);
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return submission;
    }
}
