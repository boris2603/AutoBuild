package com.company;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
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
        else {
            try {
                lines = Files.readAllLines(Paths.get(FileName), Charset.forName("windows-1251"));
            } catch (IOException e) {
                System.out.println("IO Error reading file " + FileName);
                System.out.println(e.getMessage());
                HasErrorString = HasErrorString + "IO error reading File " + FileName + System.lineSeparator();
            }
        }
        return lines;
    }
}
