package ru.romanbrazhnikov;

import ru.romanbrazhnikov.agilescraper.AgileScraper;
import ru.romanbrazhnikov.agilescraper.hardcodedconfigs.HardcodedConfigFactory;

public class Main {
    public static void main(String[] args){
        AgileScraper scraper = new AgileScraper();
        HardcodedConfigFactory configFactory = new HardcodedConfigFactory();
        //PrimitiveConfiguration configuration = configFactory.getSpranCommSell();
        //PrimitiveConfiguration configuration = configFactory.getProstoTomskCommSell();
        scraper.run(configFactory.getProstoTomskCommRent());
    }
}
