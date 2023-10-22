package com.nsb.job_seeker.listener;

import com.nsb.job_seeker.room.KeyWord;

public interface KeywordListener {
    void onClickRemove(KeyWord keyWord, int position);
    void onClickItem(KeyWord keyWord);
}
