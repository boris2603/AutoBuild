package com.company;


import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.HashMap;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;

import com.company.BuildListItem;

public class Main {

    public static void main(String[] args) {
        // Список ЗНИ в релизах
        HashMap<String,BuildListItem> BuildItemsList=new HashMap<>();
        BuildListItem BuildItem=new BuildListItem();

        // Путь где лежит новый дистрибутив
        String NewDistribPath = "";
        // Путь где лежит старый дистрибутив
        String OldDistribPath = "";


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


        // Получим путь где лежит новая сборка
        NewDistribPath = Paths.get(args[0]).getParent().toString();
        // Получим путь где лежит старая сборка
        OldDistribPath = Paths.get(args[1]).getParent().toString();

        // Загрузим описание релизов
        ReleaseObjects ReleaseNew=new ReleaseObjects(args[0],args[3]);
        ReleaseObjects ReleaseOld=new ReleaseObjects(args[1],args[3]);

        // Загрузим описание ошибок
        ReleaseErrors  ReleaseErr=new ReleaseErrors(args[2]);

        // Загрузим опиесание ЗНИ из ЦУПа
        ZNIDescription ZNIDescript = new ZNIDescription(args[4]);

        // Проверим флаг что нужны все объекты
        boolean fullLoaderFlag=false;
        if (args.length>5) fullLoaderFlag=args[5].equals("-f");

        //Флаг расширенной диагностики в консоли
        boolean debugFlag=false;
        if (fullLoaderFlag) {
            if (args.length > 6)
                debugFlag = args[6].equals("-d");
        }
        else {
            if (args.length == 6)
                debugFlag = args[5].equals("-d");
        }

        // Сравнение релизов

        // ЗНИ, тип, ссылка, дистрибутив
        for (ReleaseItem Item : ReleaseNew.getITems())
        {
            if (!ReleaseOld.containsZNI(Item.getZNI()))
            {
                // Новое ЗНИ
                BuildItem=new BuildListItem( BuildListItem.BuildListItemType.newZNI, Item);
                BuildItemsList.put(Item.getZNI(),BuildItem);
            }
            else
            {
                if (ReleaseOld.getZNI(Item.getZNI()).getDistributive().equals(Item.getDistributive()))
                {
                    // Без изменений
                    BuildItem=new BuildListItem( BuildListItem.BuildListItemType.withoutChange, Item);
                    BuildItemsList.put(Item.getZNI(),BuildItem);
                }
                else
                {
                    // Новая версия
                    BuildItem=new BuildListItem( BuildListItem.BuildListItemType.newVersion, Item);
                    BuildItemsList.put(Item.getZNI(),BuildItem);

                    // Проверим что изменилось

                }

            }
        }

        // Определить циклические зависимости по ЗНИ кандидатам в релизе
        for (BuildListItem Item : BuildItemsList.values())
        {
            String BaseZNI = Item.getItem().getZNI();
            for (String DepZNI : Item.getItem().getDependenceList())
            {
                BuildListItem DepItem=BuildItemsList.get(DepZNI);
                if (DepItem==null)
                {
                    Item.setType(BuildListItem.BuildListItemType.errBuildLinks, DepZNI);
                    ReleaseErr.registerError(BaseZNI," ссылается на ЗНИ "+DepZNI+" которой нет в сборке ");
                }
                else
                    if (DepItem.getItem().getDependenceList().contains(BaseZNI))
                    {
                        BuildItemsList.get(DepZNI).setType(BuildListItem.BuildListItemType.errCicleLinks, BaseZNI);
                        Item.setType(BuildListItem.BuildListItemType.errCicleLinks, DepZNI);
                        ReleaseErr.registerError(BaseZNI," сожержит цикличные ссылки в порядке установке с ЗНИ "+DepZNI);
                        break;
                    }
            }
        }

        // Отметим ЗНИ которые имеют не разрешеннные пересечения или ошибки написания install.txt
        for (String ReleaseError : ReleaseErr.getItems())
        {
            if (BuildItemsList.containsKey(ReleaseError))
            {
                BuildItemsList.get(ReleaseError).setType(BuildListItem.BuildListItemType.hasError,"");
            }
        };

        // Отметим ЗНИ по которым есть ошибки и  от них есть зависимые ЗНИ они не включаются в сборку
        for (BuildListItem Item : BuildItemsList.values())
        {
            for (String ReleaseError : ReleaseErr.getItems())
            {
                if (Item.getItem().getDependenceList().contains(ReleaseError)) {
                    Item.setType(BuildListItem.BuildListItemType.errBuildLinks, ReleaseError);
                }
            }
        }

        String NewZNIReport="";
        String NewVersionReport="";
        String WithoutChangeReport="";
        String HasErrorReport="";
        String HasLinkErrorReport="";
        String BuildListReport="";
        String ChangeOnlyInstallReport="";

        ArrayList<String> HasErrorRemoveCmd=new ArrayList();
        Date Now = new Date();
        SimpleDateFormat DateFormatter = new SimpleDateFormat("YYY-MM-dd");
        String ErrDistribStoragePath=Paths.get(NewDistribPath).getParent().toString()+File.separator+"ERR_BUILD_"+DateFormatter.format(Now).toString();

        HasErrorRemoveCmd.add("@ECHO OFF");
        HasErrorRemoveCmd.add("RD /S /Q "+ErrDistribStoragePath);
        HasErrorRemoveCmd.add("MD "+ErrDistribStoragePath);

        ArrayList<String> ChangeListNotes=new ArrayList();;
        ArrayList<String> BuildURLList=new ArrayList<>();

        BuildBOrderList BuildOrder = new BuildBOrderList(BuildItemsList);
        BuildOrder.CompareRelease(ReleaseOld,NewDistribPath,OldDistribPath);

        boolean flagBuildOrderList = false;

        if (flagBuildOrderList) {
            BuildListReport = BuildOrder.getBuildList(fullLoaderFlag);
        }

        for (BuildListItem Item : BuildItemsList.values())
        {
            ReleaseItem releaseItem = Item.getItem();
            String ReportString=releaseItem.getZNI()+" "+releaseItem.getULR();

            switch (Item.getType()) {
                case newZNI:
                    NewZNIReport = NewZNIReport + System.lineSeparator() + ReportString;
                    BuildURLList.add(releaseItem.getZNI()+","+releaseItem.getJiraIssue()+","+releaseItem.getULR());
                    break;
                case newVersion:
                    NewVersionReport = NewVersionReport + System.lineSeparator() + ReportString;
                    BuildURLList.add(releaseItem.getZNI()+","+releaseItem.getJiraIssue()+","+releaseItem.getULR());
                    break;
                case withoutChange:
                    WithoutChangeReport = WithoutChangeReport + System.lineSeparator() + ReportString;
                    if (fullLoaderFlag) {
                        BuildURLList.add(releaseItem.getZNI()+","+releaseItem.getJiraIssue()+","+releaseItem.getULR());
                    };
                    break;
                case changeOnlyInstall:
                    ChangeOnlyInstallReport = ChangeOnlyInstallReport + System.lineSeparator() + ReportString;
                    if (fullLoaderFlag) {
                        BuildURLList.add(releaseItem.getZNI()+","+releaseItem.getJiraIssue()+","+releaseItem.getULR());
                    };
                    break;
                case hasError:
                    HasErrorReport=HasErrorReport+","+ releaseItem.getZNI();
                    HasErrorRemoveCmd.add("MOVE /Y "+releaseItem.getDistributive()+".* "+ErrDistribStoragePath);
                    break;
                case errCicleLinks:
                    HasLinkErrorReport=HasLinkErrorReport + System.lineSeparator() + Item.getItem().getZNI()+" сожержит цикличные ссылки в порядке установке с ЗНИ "+Item.getBuildError();
                    HasErrorRemoveCmd.add("MOVE /Y "+releaseItem.getDistributive()+".* "+ErrDistribStoragePath);
                case errBuildLinks:
                    HasLinkErrorReport=HasLinkErrorReport + System.lineSeparator() + Item.getItem().getZNI()+" ссылается на ЗНИ "+Item.getBuildError()+" которой нет в сборке ";
                    HasErrorRemoveCmd.add("MOVE /Y "+releaseItem.getDistributive()+".* "+ErrDistribStoragePath);
                    ReleaseErr.registerError(Item.getItem().getZNI()," ссылается на ЗНИ "+Item.getBuildError()+" которой нет в сборке ");
            }

            // Формируем список ЗНИ с описанем
            if (ZNIDescript.ZNIDescriptionList.containsKey(releaseItem.getZNI())) {
                ZNIDescriptionItem ZNIItem= ZNIDescript.ZNIDescriptionList.get(releaseItem.getZNI());
                ChangeListNotes.add(ZNIItem.ZNI + " "+ ZNIItem.Description+" "+ZNIItem.ConfigurationItem);
            }
            else
            {
                System.out.print("Error ZNI description not found ");
                System.out.println(releaseItem.getZNI());
            }
            if (!releaseItem.getAlsoReleasedList().isEmpty())
            {
               for(String AlsoeleasedZNI : releaseItem.getAlsoReleasedList()) {
                   ZNIDescriptionItem ZNIItem = ZNIDescript.ZNIDescriptionList.get(AlsoeleasedZNI);
                   ChangeListNotes.add(ZNIItem.ZNI + " " + ZNIItem.Description + " " + ZNIItem.ConfigurationItem);
               }
            }

        }


        System.out.println();
        System.out.println("Новые ЗНИ:");
        System.out.println(NewZNIReport);

        System.out.println();
        System.out.println("Новыe версии ЗНИ:");
        System.out.println(NewVersionReport);

        if (!ChangeOnlyInstallReport.isEmpty()) {
            System.out.println();
            System.out.println("Новыe версии ЗНИ без изменений в хранилищах и pck:");
            System.out.println(ChangeOnlyInstallReport);
        }

        System.out.println();
        System.out.println("Без изменений:");
        System.out.println(WithoutChangeReport);

        System.out.println();
        System.out.println("ЗНИ с ошибками не включены в сборку:");
        System.out.println(HasErrorReport);

        System.out.println();
        System.out.println("ЗНИ не включены в сборку т.к. зависят от ЗНИ с ошибками:");
        System.out.println(HasLinkErrorReport);

        if (flagBuildOrderList) {
            System.out.println();
            if (fullLoaderFlag)
                System.out.println("Порядок установки ЗНИ:");
            else
                System.out.println("Порядок установки новых и измененнных ЗНИ:");
            System.out.println(BuildListReport);
        }

        if (debugFlag) {
            System.out.println();
            System.out.println("BAT для перемещения объектов:");
            HasErrorRemoveCmd.forEach(System.out::println);

            System.out.println();
            System.out.println("Список измененй:");
            ChangeListNotes.forEach(System.out::println);

            System.out.println();
            System.out.println("Список ЗНИ для загрузки:");
            BuildURLList.forEach(System.out::println);
        }
        FileProvider.SaveFile(Paths.get(NewDistribPath, "makeBuild.bat").toString(), HasErrorRemoveCmd);
        FileProvider.SaveFile(Paths.get(NewDistribPath, "BuildNotes.txt").toString(), ChangeListNotes);
        FileProvider.SaveFile(Paths.get(NewDistribPath, "BuildURLList.csv").toString(), BuildURLList);
        FileProvider.SaveFile(Paths.get(NewDistribPath, "ODMail.txt").toString(), ReleaseErr.getMailBody());

        ArrayList<String> txtAddressList=new ArrayList<>();
        for(String ErrItem : ReleaseErr.getItems())
        {
            ArrayList<String> ItemEmails=ReleaseNew.getZNI(ErrItem).getEmails();
            if (!ItemEmails.isEmpty()) ItemEmails.forEach(s->txtAddressList.add(s+";"));
        }
        FileProvider.SaveFile(Paths.get(NewDistribPath, "ODAddress.txt").toString(), txtAddressList);
    }
}
