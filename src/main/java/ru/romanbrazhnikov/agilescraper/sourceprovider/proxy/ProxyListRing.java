package ru.romanbrazhnikov.agilescraper.sourceprovider.proxy;

import java.util.LinkedList;
import java.util.Queue;

public class ProxyListRing {
    private Queue<IpPort> mIpPorts = new LinkedList<>();

    public void selfPopulateFromProxyList(String proxyListString){
        String[] splitByEOL = proxyListString.split("\\r?\\n|\\r");
        for(String currentIpPortString : splitByEOL){
            mIpPorts.offer(new IpPort(currentIpPortString));
        }
    }

    public boolean add(IpPort ipPort){
        return mIpPorts.offer(ipPort);
    }

    public IpPort get(){
        IpPort mCurrentBuffer;

        mCurrentBuffer = mIpPorts.poll();
        mIpPorts.offer(mCurrentBuffer);

        return mCurrentBuffer;
    }
}
