package com.fariza.mpezexpenses.Model;

public class ModelGroupList {

    String groupId, groupName, groupIcon, timestamp, createdBy;

    public ModelGroupList (){

    }

    public ModelGroupList(String groupId, String groupName, String groupIcon, String timestamp, String createdBy) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.groupIcon = groupIcon;
        this.timestamp = timestamp;
        this.createdBy = createdBy;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupIcon() {
        return groupIcon;
    }

    public void setGroupIcon(String groupIcon) {
        this.groupIcon = groupIcon;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}
