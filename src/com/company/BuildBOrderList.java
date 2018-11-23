package com.company;

import java.util.ArrayList;
import java.util.HashMap;
import com.company.BuildListItem.BuildListItemType;

public class BuildBOrderList {
    private ArrayList<String> BOArray= new ArrayList();
    private HashMap<String,BuildListItem> release;

    public BuildBOrderList(HashMap<String,BuildListItem> Release)
    {
        this.release = Release;
    };

    public String getBuildList(boolean fullLoaderFlag)
    {
        String BOListReport="";



        for(BuildListItem value : release.values())
        {
          if ((value.getType()!=BuildListItemType.errCicleLinks) &&
                  (value.getType()!=BuildListItemType.hasError) &&
                  (value.getType()!=BuildListItemType.errBuildLinks))
                    if (fullLoaderFlag)
                            BOArray.addAll(AddReleaseItem(value.getItem().getZNI(),fullLoaderFlag));
                    else {
                        if (value.getType()!=BuildListItemType.withoutChange)
                            BOArray.addAll(AddReleaseItem(value.getItem().getZNI(),fullLoaderFlag)); }
        }

        for (String item : BOArray)
        {
            BOListReport=BOListReport + item + ",";
        }
        return BOListReport;
    }

    private ArrayList<String> AddReleaseItem(String ZNI,boolean fullLoaderFlag)
    {
        ArrayList<String> retVal = new ArrayList<>();

        if (release.containsKey(ZNI)) {
            for (String depZNI : release.get(ZNI).getItem().getDependenceList()) {
                if (!BOArray.contains(depZNI))
                    if (fullLoaderFlag)
                        retVal.addAll(AddReleaseItem(depZNI, fullLoaderFlag));
                    else {
                        if (release.get(depZNI).getType() != BuildListItemType.withoutChange)
                            retVal.addAll(AddReleaseItem(depZNI, fullLoaderFlag));
                    }
            }
            retVal.add(ZNI);
        }
        return retVal;
    }

}
