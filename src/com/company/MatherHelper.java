package com.company;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MatherHelper {

    // Встречается-ли паттерн в строке
    public static boolean PatternFound(String line, Pattern pPattern)
    {
        Matcher m = pPattern.matcher(line);
        return m.find();
    }

    // Получить массив подстрок в котором встечается pPattern
    public static ArrayList<String> GetMatchList(String line, Pattern pPattern)
    {
        ArrayList<String> ResultList = new ArrayList<>();

        Matcher m = pPattern.matcher(line);
        while (m.find()) {
            ResultList.add(m.group());
        }
        return ResultList;
    }

    // Получить маасив подстрок по pPattern c учетом сдвига CorrectIndex, если найден pPatternIndicator
    public static ArrayList<String> GetMatchListByIndicator(String line, Pattern pPattern, Pattern pPatternIndicator)
    {
        ArrayList<String> ResultList = new ArrayList<>();

        Matcher m = pPatternIndicator.matcher(line);
        if (m.find()) {
            ResultList = GetMatchList(line,pPattern);
        }
        return ResultList;
    }

    public static String GetMatchParam(String line, Pattern pPattern, int MatchGroupIdx) {
        String Result = "";

        Matcher m = pPattern.matcher(line);
        if (m.find())  {
            Result = m.group(MatchGroupIdx);
        }
        return Result;
    }
}
