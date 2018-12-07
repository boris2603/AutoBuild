package com.company;


import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.HashMap;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;

import com.company.BuildListItem;

import static com.company.BuildListItem.BuildListItemType.*;

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
        System.out.println("New release path - "+NewDistribPath);
        System.out.println("Old reliase path "+OldDistribPath);

        System.out.print("Load release objects from "+args[0]+"...");
        // Загрузим описание релизов
        ReleaseObjects ReleaseNew=new ReleaseObjects(args[0],args[3]);
        System.out.println(" Done");
        System.out.print("Load release objects from "+args[1]+"...");
        ReleaseObjects ReleaseOld=new ReleaseObjects(args[1],args[3]);
        System.out.println(" Done");

        System.out.print("Load error list from "+args[2]+"...");
        // Загрузим описание ошибок
        ReleaseErrors  ReleaseErr=new ReleaseErrors(args[2],ReleaseNew);
        System.out.println(" Done");

        System.out.print("Load descriptions from "+args[4]+"...");
        // Загрузим опиесание ЗНИ из ЦУПа
        ZNIDescription ZNIDescript = new ZNIDescription(args[4]);
        System.out.println(" Done");

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

        System.out.print("Compare releases ...");
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
                }

            }
        }
        System.out.println("Done");

        System.out.print("Check cyclic links ...");
        // Определить циклические зависимости по ЗНИ кандидатам в релизе
        for (BuildListItem Item : BuildItemsList.values())
        {
            String BaseZNI = Item.getItem().getZNI();
            for (String DepZNI : Item.getItem().getDependenceList())
            {
                BuildListItem DepItem=BuildItemsList.get(DepZNI);
                if (DepItem==null)
                {
                    Item.setType(errBuildLinks, DepZNI);
                    ReleaseErr.registerError(BaseZNI," ссылается на ЗНИ "+DepZNI+" которой нет в сборке ");
                }
                else
                    if (DepItem.getItem().getDependenceList().contains(BaseZNI))
                    {
                        BuildItemsList.get(DepZNI).setType(errCicleLinks, BaseZNI);
                        Item.setType(errCicleLinks, DepZNI);
                        ReleaseErr.registerError(BaseZNI," сожержит цикличные ссылки в порядке установке с ЗНИ "+DepZNI);
                        break;
                    }
            }
        }
        System.out.println("Done");

        System.out.print("Check release objects having errors ...");
        // Отметим ЗНИ которые имеют не разрешеннные пересечения или ошибки написания install.txt
        for (String ReleaseError : ReleaseErr.getItems())
        {
            if (BuildItemsList.containsKey(ReleaseError))
            {
                BuildItemsList.get(ReleaseError).setType(hasError,"");
            }
        };
        System.out.println("Done");


        System.out.print("Check release objects for which there are errors and here are dependent objects from them...");
        // Отметим ЗНИ по которым есть ошибки и  от них есть зависимые ЗНИ они не включаются в сборку
        for (BuildListItem Item : BuildItemsList.values())
        {
            for (String ReleaseError : ReleaseErr.getItems())
            {
                if (Item.getItem().getDependenceList().contains(ReleaseError)) {
                    Item.setType(errBuildLinks, ReleaseError);
                }
            }
        }
        System.out.println("Done");

        // Переменные для текстовых отчетов
        String NewZNIReport="";
        String NewVersionReport="";
        String WithoutChangeReport="";
        String HasErrorReport="";
        String HasLinkErrorReport="";
        String BuildListReport="";
        String ChangeOnlyInstallReport="";
        String RemovedReport="";
        String JiraIssueReport="";
        String issueMismatchReport="";
        String needLoadRequimentRequestReport="";
        String needLoadRequimentCancelReport="";

        // Переменные для генерации файлов (один элемент- одна строка)
        ArrayList<String> ChangeListNotes=new ArrayList();;
        ArrayList<String> BuildURLList=new ArrayList<>();
        ArrayList<String> BuildMail=new ArrayList<>();

        System.out.print("Check removed release objects...");
        for (String oldReleaseItem : ReleaseOld.getZNIList())
        {
            if (ReleaseNew.getZNI(oldReleaseItem)==null)
            {
                if (RemovedReport.length()==0)
                    RemovedReport=oldReleaseItem;
                else
                    RemovedReport=RemovedReport+","+oldReleaseItem;

                for (String oldAlsoRelItem : ReleaseOld.getZNI(oldReleaseItem).getAlsoReleasedList()) {
                    if (ReleaseNew.getZNI(oldAlsoRelItem) == null) {
                            RemovedReport = RemovedReport + "," + oldAlsoRelItem;
                        }
                }
            }
        }
        System.out.println("Done");

        System.out.print("Release distribution comparison ...");

        // Подготовить имена каталогов для батн релиза
        ArrayList<String> HasErrorRemoveCmd=new ArrayList();
        Date Now = new Date();
        SimpleDateFormat DateFormatter = new SimpleDateFormat("YYY-MM-dd");
        String ErrorDistributiveParrebtPath=Paths.get(NewDistribPath).getParent().toString();
        String ErrDistribStoragePath=ErrorDistributiveParrebtPath+File.separator+"ERR_BUILD_"+DateFormatter.format(Now).toString();


        HasErrorRemoveCmd.add("RD /S /Q "+ErrDistribStoragePath);
        HasErrorRemoveCmd.add("MD "+ErrDistribStoragePath);


        // Проверим что изменилось в дистрибуивах
        BuildBOrderList BuildOrder = new BuildBOrderList(BuildItemsList);
        BuildOrder.CompareRelease(ReleaseOld,NewDistribPath,OldDistribPath);
        System.out.println("Done");

        boolean flagBuildOrderList = true;
        if (flagBuildOrderList) {
            System.out.println();
            System.out.print("Generate order list ...");
            BuildListReport = BuildOrder.getBuildList(fullLoaderFlag);
            System.out.println("Done");
        }


        System.out.println();
        System.out.println("Generate reports ...");

        for (BuildListItem Item : BuildItemsList.values())
        {
            ReleaseItem releaseItem = Item.getItem();
            String releaseItemList="";
            String ReportString="";

            releaseItemList=MakeComaSeparatedList(releaseItemList,releaseItem.getZNI()); // Список ЗНИ реализованных в дистрибутиве

            if (!releaseItem.getAlsoReleasedList().isEmpty())
                releaseItemList=MakeComaSeparatedList(releaseItemList,releaseItem.getAlsoReleasedListString());

            if (releaseItem.getULR()==null)
            {
                Item.setType(issueMismatch,releaseItemList);
            }
            ReportString=releaseItemList+" "+releaseItem.getULR();


            switch (Item.getType()) {
                case newZNI:
                    NewZNIReport = NewZNIReport + System.lineSeparator() + ReportString;
                    BuildURLList.add(releaseItem.getZNI()+","+releaseItem.getJiraIssue()+","+releaseItem.getULR());
                    JiraIssueReport=MakeComaSeparatedList(JiraIssueReport,releaseItem.getJiraIssue());
                    break;
                case newVersion:
                    NewVersionReport = NewVersionReport + System.lineSeparator() + ReportString;
                    BuildURLList.add(releaseItem.getZNI()+","+releaseItem.getJiraIssue()+","+releaseItem.getULR());
                    JiraIssueReport=MakeComaSeparatedList(JiraIssueReport,releaseItem.getJiraIssue());
                    break;
                case withoutChange:
                    WithoutChangeReport = WithoutChangeReport + System.lineSeparator() + ReportString;
                    JiraIssueReport=MakeComaSeparatedList(JiraIssueReport,releaseItem.getJiraIssue());
                    if (fullLoaderFlag) {
                        BuildURLList.add(releaseItem.getZNI()+","+releaseItem.getJiraIssue()+","+releaseItem.getULR());
                    };
                    break;
                case changeOnlyInstall:
                    ChangeOnlyInstallReport = ChangeOnlyInstallReport + System.lineSeparator() + ReportString;
                    JiraIssueReport=MakeComaSeparatedList(JiraIssueReport,releaseItem.getJiraIssue());
                    if (fullLoaderFlag) {
                        BuildURLList.add(releaseItem.getZNI()+","+releaseItem.getJiraIssue()+","+releaseItem.getULR());
                    };
                    break;
                case hasError:
                    if (releaseItem!=null && !releaseItem.getAlsoReleasedList().isEmpty())
                        HasErrorReport=MakeComaSeparatedList(HasErrorReport,releaseItem.getAlsoReleasedListString());
                    HasErrorReport=MakeComaSeparatedList(HasErrorReport,releaseItem.getZNI());
                    HasErrorRemoveCmd.add("MOVE /Y "+ErrorDistributiveParrebtPath+File.separator+releaseItem.getDistributive()+".* "+ErrDistribStoragePath);
                    break;
                case errCicleLinks:
                    HasLinkErrorReport=HasLinkErrorReport + System.lineSeparator() + releaseItemList +" сожержит цикличные ссылки в порядке установке с ЗНИ "+Item.getBuildError();
                    HasErrorRemoveCmd.add("MOVE /Y "+ErrorDistributiveParrebtPath+File.separator+releaseItem.getDistributive()+".* "+ErrDistribStoragePath);
                case errBuildLinks:
                    HasLinkErrorReport=HasLinkErrorReport + System.lineSeparator() + releaseItemList +" ссылается на ЗНИ "+Item.getBuildError()+" которой нет в сборке ";
                    HasErrorRemoveCmd.add("MOVE /Y "+ErrorDistributiveParrebtPath+File.separator+releaseItem.getDistributive()+".* "+ErrDistribStoragePath);
                case issueMismatch:
                    issueMismatchReport=MakeComaSeparatedList(issueMismatchReport,releaseItemList);
                    HasErrorRemoveCmd.add("MOVE /Y "+ErrorDistributiveParrebtPath+File.separator+releaseItem.getDistributive()+".* "+ErrDistribStoragePath);
            }

            // Проверим на требование к НТ и сформируем описание ЗНИ из ЦУПа
            if (ZNIDescript.ZNIDescriptionList.containsKey(releaseItem.getZNI())) {
                ZNIDescriptionItem ZNIItem= ZNIDescript.ZNIDescriptionList.get(releaseItem.getZNI());
                ChangeListNotes.add(ZNIItem.ZNI + " "+ ZNIItem.Description+" "+ZNIItem.ConfigurationItem);
                if (ZNIItem.LoadRequirement)
                    switch (Item.getType()) {
                        case newZNI:
                            needLoadRequimentCancelReport=MakeComaSeparatedList(needLoadRequimentCancelReport,releaseItem.getZNI());
                            break;
                        case newVersion:
                            needLoadRequimentRequestReport=MakeComaSeparatedList(needLoadRequimentRequestReport,releaseItem.getZNI());
                            break;
                    }
                }
            else
                System.out.print(System.lineSeparator()+"*** Error ZNI description not found "+releaseItem.getZNI());

            if (!releaseItem.getAlsoReleasedList().isEmpty())
            {
               for(String AlsoeleasedZNI : releaseItem.getAlsoReleasedList()) {
                   if (ZNIDescript.ZNIDescriptionList.containsKey(AlsoeleasedZNI)) {
                       ZNIDescriptionItem ZNIItem = ZNIDescript.ZNIDescriptionList.get(AlsoeleasedZNI);
                       ChangeListNotes.add(ZNIItem.ZNI + " " + ZNIItem.Description + " " + ZNIItem.ConfigurationItem);
                       if (ZNIItem.LoadRequirement)
                       switch (Item.getType()) {
                           case newZNI:
                               needLoadRequimentCancelReport=MakeComaSeparatedList(needLoadRequimentCancelReport,AlsoeleasedZNI);
                               break;
                           case newVersion:
                               needLoadRequimentRequestReport=MakeComaSeparatedList(needLoadRequimentRequestReport,AlsoeleasedZNI);
                               break;
                       }
                   }
                   else
                       System.out.print(System.lineSeparator()+"*** Error ZNI description not found "+AlsoeleasedZNI);
               }
            }
        }
        HasErrorRemoveCmd.add("EXIT /B");

        // Напечатаем ошибки внедрениия
        PrintReport("ЗНИ с ошибками пересечений не включены в сборку:",HasErrorReport, BuildMail);
        PrintReport("ЗНИ не включены в сборку тк номер ЗНИ install.txt не соответствует Jira:",issueMismatchReport,BuildMail);;
        PrintReport("ЗНИ не включены в сборку т.к. зависят от ЗНИ с ошибками:",HasLinkErrorReport,BuildMail);
        PrintReport("Не попали в этот билд, но присутствовали в прошлом",RemovedReport,BuildMail);

        // Подготовим письмо о ошибках
        ArrayList<String> ErrMail=new ArrayList<String>();

        PrintReport("ЗНИ не включены в сборку тк номер ЗНИ install.txt не соответствует Jira:",issueMismatchReport,ErrMail);;
        PrintReport("ЗНИ не включены в сборку т.к. зависят от ЗНИ с ошибками:",HasLinkErrorReport,ErrMail);

        ErrMail.addAll(ReleaseErr.getMailBody());

        // Выведем порядок установки
        if (flagBuildOrderList) {
            if (fullLoaderFlag)
                PrintReport("Рекомендуемый порядок установки ЗНИ:",BuildListReport,BuildMail );
            else
                PrintReport("Рекомендуемый порядок установки новых и измененных ЗНИ:", BuildListReport, BuildMail);
        }

        // Запросы по НТ
        PrintReport("Данные ЗНИ будут допущены к внедрению при снятии признака НТ:",needLoadRequimentCancelReport,BuildMail);
        PrintReport("В связи с обновлением версии по данным ЗНИ владельцам продуктов  необходимо получить от ЦСПС акцепт на зачет результатов НТ по версиям, переданным на НТ ранее",needLoadRequimentRequestReport,BuildMail);

        PrintReport("Новые ЗНИ:",NewZNIReport,BuildMail);
        PrintReport("Новыe версии ЗНИ:",NewVersionReport,BuildMail);

        if (!ChangeOnlyInstallReport.isEmpty())
            PrintReport("Новыe версии ЗНИ без изменений в хранилищах и pck:",ChangeOnlyInstallReport,BuildMail);


        PrintReport("Без изменений:",WithoutChangeReport,BuildMail);



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

            System.out.println();
            System.out.println("Письмо о ошибках:");
            ErrMail.forEach(System.out::println);
        }

        System.out.println();
        System.out.println("Generate files ...");


        WriteFile(NewDistribPath, "makeBuild.bat", HasErrorRemoveCmd);
        WriteFile(NewDistribPath, "BuildNotes.txt", ChangeListNotes);
        WriteFile(NewDistribPath, "BuildURLList.csv", BuildURLList);

        WriteFile(NewDistribPath, "ODMail.txt", ErrMail);
        WriteFile(NewDistribPath, "BuildMail.txt", BuildMail);

        ArrayList<String> JiraIssues = new ArrayList<>();
        JiraIssues.add(JiraIssueReport);

        WriteFile(NewDistribPath, "BuildJiraList.txt", JiraIssues);

        System.out.print("Generate Emails List ...");
        ArrayList<String> txtAddressList=new ArrayList<>();

        // Добавим адреса с релизом
        for(String ErrItem : ReleaseErr.getItems()) {

            if (ErrItem.length() != 0) {
                ReleaseItem RelItem = ReleaseNew.getZNI(ErrItem);
                if (RelItem != null) {
                    RelItem.getEmails().stream().filter(s -> (s != null)).forEach(s -> txtAddressList.add(s + ";"));
                } else System.out.println(System.lineSeparator()+"*** "+ErrItem + " not found email address");
            }
        }
        // Добавим адреса с ошибками построения релиза
        for(BuildListItem Item : BuildItemsList.values())
        {
            if (Item.getType()==errBuildLinks || Item.getType()==errCicleLinks || Item.getType()==issueMismatch)
            {
                ReleaseItem RelItem = Item.getItem();
                RelItem.getEmails().stream().filter(s -> (s != null)).forEach(s -> txtAddressList.add(s + ";"));
            }
        }
        System.out.println("Done");

        if (debugFlag) System.out.println(txtAddressList.toString());

        WriteFile(NewDistribPath, "ODAddress.txt", txtAddressList);

    }

    // Подготовить отчет по списку файлов
    public static void MakeMailBody(String reportHeader, String reportStirng, ArrayList<String> mailBody)
    {
        if (reportStirng.length()>0)
            mailBody.add(System.lineSeparator()+reportHeader+System.lineSeparator()+reportStirng);
    }

    // Показать отчет на консоли
    public static void PrintReport(String reportHeader, String reportStirng, ArrayList<String> reportBody)
    {
        if (reportStirng.length()>0)
        {
            System.out.println();
            System.out.println(reportHeader);
            System.out.println(reportStirng);
            MakeMailBody(reportHeader,reportStirng,reportBody);
        }
    }

    // Записать в фвйл строки с логированием действий
    public static void WriteFile(String filePath, String fileName, ArrayList<String> fileBody)
    {
        System.out.print("Write file "+fileName+" ... ");
        FileProvider.SaveFile(Paths.get(filePath,fileName).toString(), fileBody);
        System.out.println("Done");
    }


    // Добавить в список разделенный запятыми строку
    public static String MakeComaSeparatedList(String list, String item)
    {
        String retval;

        if (item==null) return list;

        if (list.length()==0) {
            retval=item;
        }
        else {
            retval=list+","+item;
        }
        return retval;
    }
}
