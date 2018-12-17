package com.company;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;


public class ReleaseItem {

    private String ZNI;
    private String Distributive;
    private String ULR;
    private ArrayList<String> DependenceList;
    private ArrayList<String> AlsoReleasedList;
    private String Developer;
    private ArrayList<String> eMailList;
    private String JiraIssue;

    public String getZNI() {
        return ZNI;
    }

    public String getDistributive() {
        return Distributive;
    }

    public String getDeveloper() {
        return Developer;
    }

    public String getULR() {
        return ULR;
    }

    public void setULR(String ULR) {
        this.ULR = ULR;
    }

    public String getJiraIssue() { return JiraIssue; }

    public void setJiraIssue(String jiraIssue) { JiraIssue = jiraIssue; }

    public ArrayList<String> getDependenceList() {return DependenceList; };

    public ArrayList<String> getEmails() { return eMailList; }

    ReleaseItem(String sZNI, String sDeveloper, String sDistributive) {
        ZNI = sZNI;
        DependenceList = new ArrayList<String>();
        AlsoReleasedList = new ArrayList<String>();
        eMailList = new ArrayList<String>();
        Developer = sDeveloper;
        Distributive = sDistributive;
    }


    String getEmail(){

        String retval="";

        this.eMailList.forEach(s->retval.concat(s).concat(";"));
        return retval;
    }

    public ArrayList<String> getAlsoReleasedList() {return AlsoReleasedList;}

    public String getAlsoReleasedListString() {
        String retVal="";
        for(String alsoReleased : AlsoReleasedList)
        {
            if (retVal.isEmpty())
                retVal=alsoReleased;
             else
                retVal=retVal+"," + alsoReleased;
        }
     return retVal;
    }


    // Добавление в список с контролем добаквки
    private void AddToList(String addItem, ArrayList<String> targetList)
    {
        boolean flagAlreadyExist=false;

        for(String Item : targetList){
            if (Item.equals(addItem)) {
                    flagAlreadyExist=true;
                    break;
                }

        }
        if (!flagAlreadyExist)
                targetList.add(addItem);
    }

    // Добавим зависимость по ЗНИ
    void AddDepend(String ZNI)
    {
        if (!ZNI.equals(this.ZNI))
            AddToList(ZNI,DependenceList);
    }

    // Добавим реализованные в одном дистрибутиве
    void AddAlsoReleased(String ZNI)
    {
        if (!ZNI.equals(this.ZNI))
            AddToList(ZNI,AlsoReleasedList);
    }

    void AddEmail(String email)
    {
        AddToList(email,eMailList);
    }

    // Проверим что ЗНИ есть в реализованных в одном дистрибутиве
    boolean CheckAlsoReleasedZNI(String TestZNI)
    {
        boolean retval=false;

        // Проверим что ЗНИ есть в списке реализованных в одном дистрибутиве
        if (!AlsoReleasedList.isEmpty())
            for (String item : AlsoReleasedList)
                if (item.equals(TestZNI))
                {
                    retval=true;
                    break;
                }

        return retval;
    }

    // Проверим что ЗНИ есть в списках зависимых или реализованных в одном дистрибутиве
    boolean CheckZNI(String TestZNI)
    {
        boolean retval=false;

        // Проверим что ЗНИ есть в списке зависимых ЗНИ
        if (!DependenceList.isEmpty())
            for (String DepItem : DependenceList)
                if (DepItem.equals(TestZNI)) {
                    retval = true;
                    break;
                }

        // Проверим что ЗНИ есть в списке реализованных в одном дистрибутиве
        if (!retval  && !AlsoReleasedList.isEmpty())
            for (String item : AlsoReleasedList)
                if (item.equals(TestZNI))
                {
                    retval=true;
                    break;
                }

        return retval;
    }

    public ArrayList<FileInfo> GetPCKList(String DistribPath)
    {
        ArrayList<FileInfo> RetVal=new ArrayList<FileInfo>();

        if (FileProvider.fileExists(Paths.get(DistribPath,Distributive, "install.txt").toString())) {
            List<String> lines = FileProvider.LoadFile(Paths.get(DistribPath, Distributive, "install.txt").toString());

            // PCK файлы для установки
            Pattern pPCKListIndicator = Pattern.compile("(Установить|Устанавливаем)", Pattern.CASE_INSENSITIVE);
            Pattern pPCK = Pattern.compile("([\\w-]+\\\\)*((ЗНО|C0|CI|SD|IM))*[_A-Za-z0-9-]*\\.pck", Pattern.CASE_INSENSITIVE);

            for (String line : lines) {
                ArrayList<String> fileList = new ArrayList<String>();

                fileList.addAll(MatherHelper.GetMatchListByIndicator(line, pPCK, pPCKListIndicator));
                fileList.forEach(s -> {
                    FileInfo fileItem = new FileInfo(Paths.get(DistribPath, Distributive, s).toString());
                    RetVal.add(fileItem);
                });
            }
        }
        return RetVal;
    }

    public ArrayList<FileInfo> GetMDBList(String DistribPath)
    {
        ArrayList<FileInfo> RetVal=new ArrayList<FileInfo>();

        if (FileProvider.fileExists(Paths.get(DistribPath,Distributive, "install.txt").toString())) {
            // Индикатор что есть Mdb
            Pattern pMDB = Pattern.compile("([\\w-]+\\\\)*[_A-Za-z0-9-]*\\.mdb", Pattern.CASE_INSENSITIVE);
            List<String> lines=FileProvider.LoadFile(Paths.get(DistribPath,Distributive,"install.txt").toString());

            for (String line : lines)
            {
                ArrayList<String> fileList=new ArrayList<String>();

                fileList.addAll(MatherHelper.GetMatchList(line, pMDB));
                fileList.forEach(s->{ FileInfo fileItem=new FileInfo(Paths.get(DistribPath,Distributive,s).toString());  RetVal.add(fileItem); });
            }
        }
        return RetVal;
    }

}
