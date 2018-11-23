package com.company;
import com.company.FileProvider;
import sun.util.locale.provider.TimeZoneNameProviderImpl;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class ReleaseErrors {
    HashMap<String,String> ReleaseErrorsItems;

    ReleaseErrors(String ReleaseErrorsPath)
    {
        ReleaseErrorsItems=new HashMap<>();
        ParceErrors(FileProvider.LoadFile(ReleaseErrorsPath));
    };


    public Collection<String> getItems() {return ReleaseErrorsItems.keySet();}

    // Проверим что по ЗНИ есть ошибки
    boolean ZNIHasError(String ZNI)
    {
        return !ReleaseErrorsItems.get(ZNI).isEmpty();
    }


    // Загрузим объекты с ошибками
    void ParceErrors(List<String> lines)
    {
        ReleaseErrorsItems.clear();

        for(String line : lines) {
            String[] items = line.split(",");

            if (items.length>1)
                if (!ReleaseErrorsItems.containsKey(items[1]))
                        ReleaseErrorsItems.put(items[1],items[0]);
        }
    }

    // Сделать генератор отчета по релизу и списка адресов

}
