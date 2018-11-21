package com.company;

import java.util.ArrayList;
import java.util.HashMap;

public class BuildBOrderList {
    private ArrayList<String> BOArray= new ArrayList();
    private HashMap<String,BuildListItem> release;

    public BuildBOrderList(HashMap<String,BuildListItem> Release)
    {
        this.release = Release;
    };

    public String getBuildList()
    {
        String BOListReport="";

        for(BuildListItem value : release.values())
        {
          if ((value.getType()!=BuildListItem.BuildListItemType.errCicleLinks) && (value.getType()!=BuildListItem.BuildListItemType.hasError) && (value.getType()!=BuildListItem.BuildListItemType.withoutChange))
                     BOArray.addAll(AddReleaseItem(value.getItem().getZNI()));
        }

        for (String item : BOArray)
        {
            BOListReport=BOListReport + item + ",";
        }
        return BOListReport;
    }

    private ArrayList<String> AddReleaseItem(String ZNI)
    {
        ArrayList<String> retVal = new ArrayList<>();

        if (release.containsKey(ZNI)) {
            for (String depZNI : release.get(ZNI).getItem().getDependenceList()) {
                if (!BOArray.contains(depZNI))
                    if (release.get(depZNI).getType()!=BuildListItem.BuildListItemType.withoutChange)
                        retVal.addAll(AddReleaseItem(depZNI));
            }
            retVal.add(ZNI);
        }
        return retVal;
    }

}
