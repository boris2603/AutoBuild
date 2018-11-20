package com.company;

public class BuildListItem {

    public enum BuildListItemType {newZNI,newVersion,withoutChange,hasError};

    String ZNI;
    String Distributive;
    String URL;
    String JiraIssue;
    BuildListItemType Type;


    public BuildListItem(BuildListItemType type, String ZNI, String distributive, String URL, String JiraIssue) {
        this.ZNI = ZNI;
        Distributive = distributive;
        this.URL = URL;
        Type = type;
        this.JiraIssue=JiraIssue;
    }
}
