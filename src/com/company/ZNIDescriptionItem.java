package com.company;

public class ZNIDescriptionItem {
    String ZNI;
    String State;
    String Description;
    String ConfigurationItem;

    ZNIDescriptionItem(String ZNI, String Description, String State, String ConfigurationItem) {
        this.ZNI = ZNI;
        this.Description = Description;
        this.ConfigurationItem = ConfigurationItem;
        this.State = State;
    }
}

