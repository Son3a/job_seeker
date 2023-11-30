package com.nsb.job_seeker.model;

import java.util.Date;

public class ChatMessage {
    public String senderId, receiverId, message, dateTime;
    public Date dateObject;
    public String conversionId, conversionName, conversionImage, conversionCompany;

    @Override
    public String toString() {
        return "ChatMessage{" +
                "senderId='" + senderId + '\'' +
                ", receiverId='" + receiverId + '\'' +
                ", message='" + message + '\'' +
                ", dateTime='" + dateTime + '\'' +
                ", dateObject=" + dateObject +
                ", conversionId='" + conversionId + '\'' +
                ", conversionName='" + conversionName + '\'' +
                ", conversionImage='" + conversionImage + '\'' +
                '}';
    }
}
