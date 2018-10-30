package com.company;

public class BuildListItem {

    public enum BuildListItemType {newZNI,newVersion,withoutChange,hasError};

    String ZNI;
    String Distributive;
    String URL;
    BuildListItemType Type;


    public BuildListItem(BuildListItemType type, String ZNI, String distributive, String URL) {
        this.ZNI = ZNI;
        Distributive = distributive;
        this.URL = URL;
        Type = type;
    }
}
