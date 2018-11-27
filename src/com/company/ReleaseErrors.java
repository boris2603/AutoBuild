package com.company;
import com.company.FileProvider;
import sun.util.locale.provider.TimeZoneNameProviderImpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class ReleaseErrors {
    HashMap<String,String> ReleaseErrorsItems;

    private static String LN=System.lineSeparator();

    ReleaseErrors(String ReleaseErrorsPath)
    {
        ReleaseErrorsItems=new HashMap<>();
        parceErrors(FileProvider.LoadFile(ReleaseErrorsPath));
    };

    public void registerError(String ZNI,String Error)
    {
      ReleaseErrorsItems.put(ZNI,Error);
    };

    public ArrayList<String> getMailBody() {
        ArrayList<String> retVal=new ArrayList<String>(ReleaseErrorsItems.values());
        return retVal;
    }


    public Collection<String> getItems() {return ReleaseErrorsItems.keySet();}

    // Проверим что по ЗНИ есть ошибки
    boolean hasErrors(String ZNI)
    {
        return !ReleaseErrorsItems.get(ZNI).isEmpty();
    }


    // Загрузим объекты с ошибками и сформируем письмо по ошибкам
    void parceErrors(List<String> lines)
    {

        boolean flagLookNextString=false;
        String sMainZNI=new String();
        String sOverlapZNI=new String();
        ReleaseErrorsItems.clear();

        for(String line : lines) {
            String[] items = line.split(",");
            String sReportString=new String();


            if ((items.length<2) & !flagLookNextString)
                continue;

            if (!flagLookNextString) {
                if (!items[1].equals(sMainZNI) & (items[0].equals("1") | items[0].equals("2") | items[0].equals("4"))) {
                    sReportString = LN+"ЗНИ " + items[1] + " разработчик " + items[2];
                    sMainZNI = items[1];
                    sOverlapZNI="";
                    ReleaseErrorsItems.put(items[1],sReportString);
                }
            }

            switch (items[0]) {
                case "1":
                    sReportString="   "+items[3]+" не вошла в сборку , но указана зависимость";
                    break;
                case "2":
                    if (!items[3].equals(sOverlapZNI)) {
                        sReportString =  "  Имеет пересечения не отмеченные в порядке наката с " + items[3] + " " + items[4]+LN;
                        sOverlapZNI=items[3];
                    };
                    sReportString=sReportString+"      "+items[5];
                    break;
                case "3":
                    sReportString=LN+LN+" Ошибка при разборе Install.txt";
                    if (items[1].length()>0)
                        sReportString=sReportString+" по ЗНИ "+items[1];
                    else
                        sReportString=sReportString+" по файлу "+items[2];
                    flagLookNextString=true;
                    sMainZNI="";
                    sOverlapZNI="";
                    break;
                case "4":
                    sReportString=sReportString+"    Сообщите о изменении кода разработчкам ЗНИ "+items[3];
                    break;
            }
            if (flagLookNextString & items.length==1)
            {
                sReportString="   "+items[0]+LN;
                flagLookNextString=false;

            }
            else ReleaseErrorsItems.put(items[1],sReportString);
        }
    }


}
