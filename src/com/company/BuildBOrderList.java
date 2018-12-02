package com.company;

import java.nio.file.Paths;
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

    // Получить список ЗНИ в порядке наката
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
            if (BOListReport.length()==0)
                BOListReport=item;
            else
                BOListReport=BOListReport +","+item;
        }
        return BOListReport;
    }

    // Добавить ЗНИ в сборку
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

    // Сравнивает 2 релиза
    public void CompareRelease(ReleaseObjects ReleaseOld, String ReleasePathNew, String ReleasePathOld)
    {
        for(BuildListItem Item : release.values())
        {
            if (Item.getType()==BuildListItemType.newVersion) {

                // Проверим что не изменились PCK
                ArrayList<FileInfo> oldReleasePck = ReleaseOld.getZNI(Item.getItem().getZNI()).GetPCKList(ReleasePathOld);
                for (FileInfo pckFile : Item.getItem().GetPCKList(ReleasePathNew)) {
                        if (FileProvider.fileExistsInList(pckFile,oldReleasePck)) {
                                Item.setType(BuildListItemType.changeOnlyInstall, "");
                    }
                }

                // Проверим что не изменились
                ArrayList<FileInfo> oldReleaseMdb = ReleaseOld.getZNI(Item.getItem().getZNI()).GetMDBList(ReleasePathOld);
                for (FileInfo mdbFile : Item.getItem().GetPCKList(ReleasePathNew)) {
                    if (FileProvider.fileExistsInList(mdbFile,oldReleaseMdb)) {
                        Item.setType(BuildListItemType.changeOnlyInstall, "");
                    }
                }
            }
        }

    }

}
