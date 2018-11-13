package com.company;

import java.util.HashMap;
import java.util.List;

public class ZNIDescription {
    public HashMap<String, ZNIDescriptionItem> ZNIDescriptionList;

    ZNIDescription(String ZNIDescriptionFilePath) {
        ZNIDescriptionList = new HashMap<>();
        ParceZNIDescriptionFile(FileProvider.LoadFile(ZNIDescriptionFilePath));
    }

    private void ParceZNIDescriptionFile(List<String> lines) {
        for(String line : lines) {
            String[] Items = line.split(";");

            if (Items.length>=5) {
                ZNIDescriptionItem hasItem = new ZNIDescriptionItem(Items[0], Items[2], Items[1], Items[4]);
                ZNIDescriptionList.put(Items[0], hasItem);
            }
        }
    }


}
