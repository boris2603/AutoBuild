package com.company;
import com.company.ReleaseItem;
import com.company.FileProvider;

import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;


public class ReleaseObjects {
    private HashMap<String,ReleaseItem> ReleaseItems;


    ReleaseObjects(String ReleasePath, String ReleaseURLPath)
    {
        ReleaseItems = new HashMap<>();
        ParceReleaseObjects(FileProvider.LoadFile(ReleasePath));
        ParceURL(FileProvider.LoadFile(ReleaseURLPath));
    }

    public Collection<ReleaseItem> getITems() {return ReleaseItems.values(); };

    public boolean containsZNI(String ZNI) {return ReleaseItems.containsKey(ZNI);};

    public ReleaseItem getZNI(String ZNI) {return ReleaseItems.get(ZNI);};


    // Проверяем что ЗНИ есть в релизе
    boolean ZNIExistInRelease(String ZNI)
    {
        boolean retVal = ReleaseItems.containsKey(ZNI);

        if (!retVal) {
            for (ReleaseItem value : ReleaseItems.values()) {
                if (value.CheckAlsoReleasedZNI(ZNI))
                {
                    retVal=true;
                    break;
                }
            }
        }
        return retVal;
    }


    private void ParceURL(List<String> lines)
    {
        for(String line : lines) {
            String[] Items = line.split(",");

            ReleaseItem hasItem = ReleaseItems.get(Items[2]);
            if  (hasItem!=null) {
                hasItem.setULR(Items[3]);
                hasItem.setJiraIssue(Items[1]);
            }

        }
    }

    public List<String> getZNIList()
    {
        List<String> retVal = new ArrayList<String>();
        ReleaseItems.keySet().forEach(key->retVal.add(key));

        return retVal;
    }


    private void ParceReleaseObjects(List<String> lines)
    {
        ReleaseItems.clear();

        for(String line : lines) {
            if (line.charAt(0)!='#') {
                String[] items = line.split(",");

                if (items.length > 0) {
                    int idxGlobal = 3;
                    ReleaseItem Item = new ReleaseItem(items[0], items[1], items[2]);

                    // Загрузим список электронной почты
                    for (int idx = idxGlobal; idx < items.length && !items[idx].equals("#"); idx++) {
                        Item.AddEmail(items[idx]);
                        idxGlobal = idx;
                    }
                    if ((items.length - idxGlobal) > 1 && items[idxGlobal + 1].equals("#"))
                        idxGlobal = idxGlobal + 2;
                    else idxGlobal++;

                    // Загрузим список зависимых ЗНИ
                    for (int idx = idxGlobal; idx < items.length && !items[idx].equals("%"); idx++) {
                        Item.AddDepend(items[idx]);
                        idxGlobal = idx;
                    }
                    if ((items.length - idxGlobal) > 1 && items[idxGlobal + 1].equals("%"))
                        idxGlobal = idxGlobal + 2;
                    else idxGlobal++;

                    // Загрузим список ЗНИ реализованных совместно
                    for (int idx = idxGlobal; idx < items.length; idx++) {
                        Item.AddAlsoReleased(items[idx]);
                    }
                    ReleaseItems.put(Item.getZNI(), Item);
                }
            }
        }

    }

}
