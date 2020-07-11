package com.vladan.redditnotifier.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@Slf4j
public class SlackControllerInterceptor extends HandlerInterceptorAdapter {

    @Value("${slack.verification.token}")
    private String slackVerificationToken;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (request.getParameterValues("token").length > 0) {
            String token = request.getParameterValues("token")[0];
            return slackVerificationToken.equals(token);
        } else {
            log.error("Request contains invalid Slack verification token");
            return false;
        }
    }
}
