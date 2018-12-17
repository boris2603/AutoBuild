package com.company;

public class ZNIDescriptionItem {
    String ZNI;
    String State;
    String Description;
    String ConfigurationItem;
    boolean LoadRequirement;

    ZNIDescriptionItem(String ZNI, String Description, String State, String ConfigurationItem, String LoadRequirement) {
        this.ZNI = ZNI;
        this.Description = Description;
        this.ConfigurationItem = ConfigurationItem;
        this.State = State;
        if (LoadRequirement.length()>0) {
            this.LoadRequirement = !LoadRequirement.equals("Выдержит");
        }
    }
}

