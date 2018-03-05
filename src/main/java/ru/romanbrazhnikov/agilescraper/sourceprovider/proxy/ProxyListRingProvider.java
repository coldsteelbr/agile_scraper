package ru.romanbrazhnikov.agilescraper.sourceprovider.proxy;

import ru.romanbrazhnikov.agilescraper.utils.FileUtils;

import java.io.IOException;

public class ProxyListRingProvider {

    private static ProxyListRing mInstance;

    private ProxyListRingProvider(){}

    public static ProxyListRing getInstance(String path){

        if(mInstance == null){

            // try to read the file

            String string;
            try {
                string = FileUtils.readFromFileToString(path);
                mInstance = new ProxyListRing();
                mInstance.selfPopulateFromProxyList(string);
            } catch (IOException e) {
                System.out.printf("ProxyList file: \"%s\" not found\n", path);
                return null;
            }

        }


        return mInstance;
    }
}
