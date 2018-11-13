package com.company;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.MalformedInputException;
import java.nio.charset.UnmappableCharacterException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

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
}
