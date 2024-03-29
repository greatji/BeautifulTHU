package com.thu.domain;

import org.apache.commons.lang.StringEscapeUtils;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Created by JasonLee on 16/12/3.
 */
@Entity
public class Response {
    @Id
    //@GeneratedValue
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "responseGenerator")
    @SequenceGenerator(name = "responseGenerator", sequenceName = "responseSequence", allocationSize=1)
    private Long responseId;
    private String responseContent;
    @ManyToOne
    private TUser responder;
    private LocalDateTime respondTime = LocalDateTime.now();
    private Long likes = 0L;

    protected Response() {}

    public Response(String responseContent, TUser responder) {
        this.responseContent = responseContent;
        this.responder = responder;
    }

    public Long getResponseId() {
        return responseId;
    }

    public String getResponseContent() {
        return StringEscapeUtils.unescapeHtml(responseContent);
    }

    public void setResponseContent(String responseContent) {
        this.responseContent = responseContent;
    }

    public TUser getResponder() {
        return responder;
    }

    public void setResponder(TUser responder) {
        this.responder = responder;
    }

    public LocalDateTime getRespondTime() {
        return respondTime;
    }

    public void setRespondTime(LocalDateTime respondTime) {
        this.respondTime = respondTime;
    }

    public Long getLikes() {
        return likes;
    }

    public void setLikes(Long likes) {
        this.likes = likes;
    }

    public void incrementLikes() {
        this.likes += 1;
    }

    public void decrementLikes() {
        if (this.likes > 0) {
            this.likes -= 1;
        }
    }
}
