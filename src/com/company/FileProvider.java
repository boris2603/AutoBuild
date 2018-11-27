package com.company;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.MalformedInputException;
import java.nio.charset.UnmappableCharacterException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class FileProvider {
    private static String HasErrorString=new String();

    public boolean HasError()
    {
        return (!HasErrorString.isEmpty());
    }

    // Вернуть список ошибок парсинга
    public String getHasErrorString() {
        return HasErrorString;
    }


    // Загрузить файл с диска с обработкой ошибок
    public static List<String> LoadFile(String FileName)
    {
        List<String> lines = new ArrayList<String>();
        if (!Files.exists(Paths.get(FileName)))
        {
            System.out.println("File not found " + FileName);
            HasErrorString = HasErrorString + "File not found " + FileName + System.lineSeparator();
        }
        else
        {
            try {
                lines = Files.readAllLines(Paths.get(FileName), Charset.forName("UTF-8"));
            } catch (UnmappableCharacterException | MalformedInputException UECEx) {
                try {
                    lines = Files.readAllLines(Paths.get(FileName),Charset.forName("windows-1251"));
                } catch (Exception e) {
                    System.out.println("IO Error reading file " + FileName);
                    System.out.println(e.getMessage());
                    HasErrorString = HasErrorString + "IO error reading File " + FileName + System.lineSeparator();
                }
            }
            catch (IOException IOEx)
            {
                System.out.println("IO Error reading file " + FileName);
                System.out.println(IOEx.getMessage());
                HasErrorString = HasErrorString + "IO error reading File " + FileName + System.lineSeparator();
            }
        }
        return lines;
    }

    // Сохранить список в файл, файл перезаписывается
    public static void SaveFile(String fileName, List<String> ReportText)
    {

        try
        {
            if (Files.exists(Paths.get(fileName)))
                Files.delete(Paths.get(fileName));
            Files.write(Paths.get(fileName), ReportText, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        }
        catch (IOException e)
        {
            System.out.println("IO Error writing Objects File "+fileName);
            System.out.println(e.getLocalizedMessage());
            System.out.println(e.getMessage());
            System.out.println(e.fillInStackTrace());
        }
    }

    // Сформировать список каталогов начиная от текущего
    public ArrayList<String> getDirList(String fromDir) {
        ArrayList<String> RetVal = new ArrayList<>();

        if(!Files.exists(Paths.get(fromDir)))
            System.out.println(fromDir + " directory not exists");

        try (Stream<Path> fileList = Files.find(Paths.get(fromDir), Integer.MAX_VALUE, (path,attrs) -> attrs.isDirectory()))
        {
            fileList.forEach(s->RetVal.add(s.toString()));
        }
        catch (IOException e) {
            System.out.println("IO Error reading file list from " + fromDir);
            System.out.println(e.getLocalizedMessage());
            System.out.println(e.getMessage());
            System.out.println(e.fillInStackTrace());
        };
        return RetVal;

    }

    // Сформировать список файлов в каталоге по маске
    public ArrayList<String> getFilesList(String dir, String ext1, String ext2)
    {
        ArrayList<String> RetVal=new ArrayList<>();

        if(!Files.exists(Paths.get(dir)))
            System.out.println(dir + " directory not exists");

        try (Stream<Path> fileList = Files.find(Paths.get(dir), Integer.MAX_VALUE, (path,attrs) -> attrs.isRegularFile() && path.toString().endsWith("."+ext1) && path.toString().endsWith("."+ext2)))
            {
                fileList.forEach(s->RetVal.add(s.toString()));
            }
        catch (IOException e) {
            System.out.println("IO Error reading file list from " + dir);
            System.out.println(e.getLocalizedMessage());
            System.out.println(e.getMessage());
            System.out.println(e.fillInStackTrace());
        };
        return RetVal;
    }

    // Сравнить файлы по времени создания и размеру
    public boolean isSameiles(String file1, String file2)
    {
        long file1size=0;
        long file2size=0;
        FileTime file1FileTime=FileTime.fromMillis(System.currentTimeMillis());
        FileTime file2FileTime=file1FileTime;

        try {
            file1size=Files.size(Paths.get(file1));
            file1FileTime=Files.getLastModifiedTime(Paths.get(file1));
        }
        catch (IOException e) {
            System.out.println("IO Error reading file list from " + file1);
            System.out.println(e.getLocalizedMessage());
            System.out.println(e.getMessage());
            System.out.println(e.fillInStackTrace());
        };
        try {
            file2size=Files.size(Paths.get(file2));
            file2FileTime=Files.getLastModifiedTime(Paths.get(file2));
        }
        catch (IOException e) {
            System.out.println("IO Error reading file list from " + file2);
            System.out.println(e.getLocalizedMessage());
            System.out.println(e.getMessage());
            System.out.println(e.fillInStackTrace());
        };

        return ((file1size==file2size) && (file1FileTime.compareTo(file2FileTime)==1));
    }


    public static boolean fileExistsInList(FileInfo checkedFile, ArrayList<FileInfo> fileList)
    {

        for(FileInfo fileItem: fileList)
        {
            if (checkedFile._name.equals(fileItem._name)) {
                if ((checkedFile._size == fileItem._size) && (checkedFile._lastModifiedTime.compareTo(fileItem._lastModifiedTime) == 0)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean fileExists(String fileName) { return Files.exists(Paths.get(fileName)); }


}
