package com.company;


import java.io.File;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;

import com.company.BuildListItem;

public class Main {

    public static void main(String[] args) {
	// write your code here
        HashMap<String,BuildListItem> BuildItemsList=new HashMap<>();
        BuildListItem BuildItem=new BuildListItem(BuildListItem.BuildListItemType.newZNI,"","","","");
        String NewDistribPath = "";


        if (args.length<4)
        {
           System.out.println("Auto Release Bulid usage");
           System.out.println("   AutoReleaseEKS.jar <NewReleaseObjectsFile> <OldReleaseObjectsFile> <OverlapDetectorErrorFile> <URLReleaseListFile> <ZNIDescriptionFile> -f");
            System.out.println("   <NewReleaseObjectsFile>  file ODListObjects.txt buld by OverlapDetector utility from current release build");
            System.out.println("   <OldReleaseObjectsFile>  file ODListObjects.txt buld by OverlapDetector utility from previous release build");
            System.out.println("   <OverlapDetectorErrorFile>  file logerr.txt buld by OverlapDetector utility from current release build");
            System.out.println("   <URLReleaseListFile>  csv file buld by CurParse utility");
            System.out.println("   <ZNIDescriptionFile>  csv file downloaded form HP PPM (ZNI,State,Description,Create Date,CI)");
            System.out.println("   -f if present, all release ZNI are added to the download list (BuildURLList.csv), otherwise only new and changed");

        };

        // Загрузим описание релизов
        ReleaseObjects ReleaseNew=new ReleaseObjects(args[0],args[3]);
        ReleaseObjects ReleaseOld=new ReleaseObjects(args[1],args[3]);

        // Получим путь где лежит новая сборка
        NewDistribPath = Paths.get(args[0]).getParent().toString();

        // Загрузим описание ошибок
        ReleaseErrors  ReleaseErr=new ReleaseErrors(args[2]);
        // Загрузим опиесание ЗНИ из ЦУПа
        ZNIDescription ZNIDescript = new ZNIDescription(args[4]);

        // Проверим флаг что нужны все объекты
        boolean fullLoaderFlag=false;
        if (args.length>5) fullLoaderFlag=args[5].equals("-f");

        // Сравнение релизов

        // ЗНИ, тип, ссылка, дистрибутив
        for (ReleaseItem Item : ReleaseNew.ReleaseItems.values())
        {
            if (!ReleaseOld.ReleaseItems.containsKey(Item.getZNI()))
            {
                // Новое ЗНИ
                BuildItem=new BuildListItem( BuildListItem.BuildListItemType.newZNI, Item.getZNI(), Item.getDistributive(), Item.getULR(), Item.getJiraIssue());
                BuildItemsList.put(Item.getZNI(),BuildItem);
            }
            else
            {
                if (ReleaseOld.ReleaseItems.get(Item.getZNI()).getDistributive().equals(Item.getDistributive()))
                {
                    // Без изменений
                    BuildItem=new BuildListItem( BuildListItem.BuildListItemType.withoutChange, Item.getZNI(), Item.getDistributive(), Item.getULR(), Item.getJiraIssue());
                    BuildItemsList.put(Item.getZNI(),BuildItem);
                }
                else
                {
                    // Новая версия
                    BuildItem=new BuildListItem( BuildListItem.BuildListItemType.newVersion, Item.getZNI(), Item.getDistributive(), Item.getULR(), Item.getJiraIssue());
                    BuildItemsList.put(Item.getZNI(),BuildItem);
                }

            }
        }

        for (String ReleaseError : ReleaseErr.ReleaseErrorsItems.keySet())
        {
            if (BuildItemsList.containsKey(ReleaseError))
            {
                BuildItemsList.get(ReleaseError).Type=BuildListItem.BuildListItemType.hasError;
            }
        };


        String NewZNIReport="";
        String NewVersionReport="";
        String WithoutChangeReport="";
        String HasErrorReport="";

        ArrayList<String> HasErrorRemoveCmd=new ArrayList();
        ArrayList<String> ChangeListNotes=new ArrayList();;
        ArrayList<String> BuildURLList=new ArrayList<>();

        Date Now = new Date();
        SimpleDateFormat DateFormatter = new SimpleDateFormat("YYY-MM-dd");
        String ErrDistribStoragePath=Paths.get(NewDistribPath).getParent().toString()+File.separator+"ERR_BUILD_"+DateFormatter.format(Now).toString();


        for (BuildListItem Item : BuildItemsList.values())
        {
            String ReportString=Item.ZNI+" "+Item.URL;
            switch (Item.Type) {
                case newZNI:
                    NewZNIReport = NewZNIReport + System.lineSeparator() + ReportString;
                    BuildURLList.add(Item.ZNI+","+Item.JiraIssue+","+Item.URL);
                    break;
                case newVersion:
                    NewVersionReport = NewVersionReport + System.lineSeparator() + ReportString;
                    BuildURLList.add(Item.ZNI+","+Item.JiraIssue+","+Item.URL);
                    break;
                case withoutChange:
                    WithoutChangeReport = WithoutChangeReport + System.lineSeparator() + ReportString;
                    if (fullLoaderFlag) {
                        BuildURLList.add(Item.ZNI + "," + Item.JiraIssue + "," + Item.URL);
                    };
                    break;
                case hasError:
                    HasErrorReport=HasErrorReport+","+ Item.ZNI;
                    if (HasErrorRemoveCmd.isEmpty())
                    {
                        HasErrorRemoveCmd.add("RD /S /Q "+ErrDistribStoragePath);
                        HasErrorRemoveCmd.add("MD "+ErrDistribStoragePath);
                    }
                    HasErrorRemoveCmd.add("MOVE /Y "+NewDistribPath+ File.separator+Item.Distributive+".* "+ErrDistribStoragePath);
                    break;
            }
            if (ZNIDescript.ZNIDescriptionList.containsKey(Item.ZNI)) {
                ZNIDescriptionItem ZNIItem= ZNIDescript.ZNIDescriptionList.get(Item.ZNI);
                ChangeListNotes.add(ZNIItem.ZNI + " "+ ZNIItem.Description+" "+ZNIItem.ConfigurationItem);
            }
            else
            {
                System.out.print("Error ZNI description not found ");
                System.out.println(Item.ZNI);
            }

        }



        System.out.println();
        System.out.println("Новые ЗНИ:");
        System.out.println(NewZNIReport);

        System.out.println();
        System.out.println("Новыe версии ЗНИ:");
        System.out.println(NewVersionReport);

        System.out.println();
        System.out.println("Без изменений:");
        System.out.println(WithoutChangeReport);

        System.out.println();
        System.out.println("ЗНИ с ошибками:");
        System.out.println(HasErrorReport);

        System.out.println();
        System.out.println("BAT для перемещения объектов:");
        HasErrorRemoveCmd.forEach(System.out::println);
        FileProvider.SaveFile(Paths.get(NewDistribPath,"makeBuild.bat").toString(), HasErrorRemoveCmd);

        System.out.println();
        System.out.println("Список измененй:");
        ChangeListNotes.forEach(System.out::println);
        FileProvider.SaveFile(Paths.get(NewDistribPath,"BuildNotes.txt").toString(), ChangeListNotes);

        System.out.println();
        System.out.println("Список ЗНИ для загрузки:");
        BuildURLList.forEach(System.out::println);
        FileProvider.SaveFile(Paths.get(NewDistribPath,"BuildURLList.csv").toString(), BuildURLList);

    }
}
