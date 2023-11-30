package com.nsb.job_seeker.listener;

import com.nsb.job_seeker.model.ChatMessage;
import com.nsb.job_seeker.model.User;

import java.util.List;

public interface ConversionListener {
    void onConversionClicked(User user, String companyName);
    void onChangeListConversion(List<ChatMessage> chatMessageList);
}
