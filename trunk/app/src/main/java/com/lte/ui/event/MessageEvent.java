package com.lte.ui.event;

import com.lte.data.ImsiData;
import com.lte.data.MacData;

import java.util.List;

import me.yokeyword.fragmentation.SupportFragment;

/**
 * Created by chenxiaojun on 2017/9/22.
 */

public class MessageEvent {
    private List<MacData> macDataList;
    private List<ImsiData> imsiDataList;
    public String data;

    public boolean isUpDate;

    public MacData macData;

    public ImsiData imsiData;

    public MessageEvent(MacData macData) {
        this.macData = macData;
    }

    public MessageEvent(ImsiData imsiData) {
        this.imsiData = imsiData;
    }

    public MessageEvent(String bing) {
        data = bing;
    }

    public MessageEvent(boolean isUpDate) {
        this.isUpDate = isUpDate;
    }
    public MessageEvent(List<ImsiData> imsiDataList) {
        this.imsiDataList = imsiDataList;
    }

    @Override
    public String toString() {
        return "MessageEvent{" +
                "macDataList=" + macDataList +
                ", imsiDataList=" + imsiDataList +
                ", data='" + data + '\'' +
                ", isUpDate=" + isUpDate +
                ", macData=" + macData +
                ", imsiData=" + imsiData +
                '}';
    }
}
