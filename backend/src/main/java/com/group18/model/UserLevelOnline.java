package com.group18.model;

public class UserLevelOnline {
    private String levelType;
    private Long onlineCount;
    private String updateTime;

    public String getLevelType() { return levelType; }
    public void setLevelType(String levelType) { this.levelType = levelType; }
    public Long getOnlineCount() { return onlineCount; }
    public void setOnlineCount(Long onlineCount) { this.onlineCount = onlineCount; }
    public String getUpdateTime() { return updateTime; }
    public void setUpdateTime(String updateTime) { this.updateTime = updateTime; }
}
