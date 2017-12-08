package ru.romanbrazhnikov.agilescraper.utils;

import java.io.*;

public class FileUtils {
    public static void saveStringToFile(String p_stringToSave, String p_fileName)
    {
        try(  PrintWriter out = new PrintWriter(p_fileName)  ){
            out.println(p_stringToSave);
            out.close();
        }
        catch (FileNotFoundException e)
        {
            System.out.println(e.getClass() +": " + e.getMessage());
        }
    }

    public static String readFromFileToString(String p_fileName) throws IOException
    {
        BufferedReader br;
        String everything;

        ////////////////
        br = new BufferedReader(new FileReader(p_fileName));

        StringBuilder sb = new StringBuilder();
        String line = br.readLine();

        while (line != null) {
            sb.append(line);
            sb.append(System.lineSeparator());
            line = br.readLine();
        }
        everything = sb.toString();
        ///////////////

        return everything;
    }

}
