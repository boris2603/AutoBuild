package com.company;


import java.io.File;
import java.util.HashMap;
import java.nio.file.Paths;
import java.util.ArrayList;

import com.company.BuildListItem;

public class Main {

    public static void main(String[] args) {
	// write your code here
        HashMap<String,BuildListItem> BuildItemsList=new HashMap<>();
        BuildListItem BuildItem=new BuildListItem(BuildListItem.BuildListItemType.newZNI,"","","");
        String NewDistribPath = "";


        if (args.length<4)
        {
           System.out.println("Auto Release Bulid usage");
           System.out.println("   AutoReleaseEKS.jar <NewReleaseObjectsFile> <OldReleaseObjectsFile> <OverlapDetectorErrorFile> <URLReleaseListFile> <ZNIDescriptionFile> ");
            System.out.println("   <NewReleaseObjectsFile>  file ODListObjects.txt buld by OverlapDetector utility from current release build");
            System.out.println("   <OldReleaseObjectsFile>  file ODListObjects.txt buld by OverlapDetector utility from previous release build");
            System.out.println("   <OverlapDetectorErrorFile>  file logerr.txt buld by OverlapDetector utility from current release build");
            System.out.println("   <URLReleaseListFile>  csv file buld by CurParse utility");
            System.out.println("   <ZNIDescriptionFile>  csv file downloaded form HP PPM (ZNI,State,Description,Create Date,CI)");

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

        // Сравнение релизов

        // ЗНИ, тип, ссылка, дистрибутив
        for (ReleaseItem Item : ReleaseNew.ReleaseItems.values())
        {
            if (!ReleaseOld.ReleaseItems.containsKey(Item.getZNI()))
            {
                // Новое ЗНИ
                BuildItem=new BuildListItem( BuildListItem.BuildListItemType.newZNI, Item.getZNI(), Item.getDistributive(), Item.getULR());
                BuildItemsList.put(Item.getZNI(),BuildItem);
            }
            else
            {
                if (ReleaseOld.ReleaseItems.get(Item.getZNI()).getDistributive().equals(Item.getDistributive()))
                {
                    // Без изменений
                    BuildItem=new BuildListItem( BuildListItem.BuildListItemType.withoutChange, Item.getZNI(), Item.getDistributive(), Item.getULR());
                    BuildItemsList.put(Item.getZNI(),BuildItem);
                }
                else
                {
                    // Новая версия
                    BuildItem=new BuildListItem( BuildListItem.BuildListItemType.newVersion, Item.getZNI(), Item.getDistributive(), Item.getULR());
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

        for (BuildListItem Item : BuildItemsList.values())
        {
            String ReportString=Item.ZNI+" "+Item.URL;
            switch (Item.Type) {
                case newZNI:
                    NewZNIReport = NewZNIReport + System.lineSeparator() + ReportString;
                    break;
                case newVersion:
                    NewVersionReport = NewVersionReport + System.lineSeparator() + ReportString;
                    break;
                case withoutChange:
                    WithoutChangeReport = WithoutChangeReport + System.lineSeparator() + ReportString;
                    break;
                case hasError:
                    HasErrorReport=HasErrorReport+","+ Item.ZNI;
                    HasErrorRemoveCmd.add("DEL "+NewDistribPath+ File.separator+Item.Distributive+".*");
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
        System.out.println("BAT для удаления:");
        HasErrorRemoveCmd.forEach(System.out::println);
        FileProvider.SaveFile(Paths.get(NewDistribPath,"makeBuild.bat").toString(), HasErrorRemoveCmd);

        System.out.println();
        System.out.println("Список измененй:");
        ChangeListNotes.forEach(System.out::println);
        FileProvider.SaveFile(Paths.get(NewDistribPath,"BuildNotes.txt").toString(), ChangeListNotes);

    }
}
