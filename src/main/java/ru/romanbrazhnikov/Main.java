package ru.romanbrazhnikov;

import ru.romanbrazhnikov.agilescraper.AgileScraper;
import ru.romanbrazhnikov.agilescraper.hardcodedconfigs.HardcodedConfigFactory;
import ru.romanbrazhnikov.agilescraper.hardcodedconfigs.PrimitiveConfiguration;

public class Main {
    public static void main(String[] args){
        AgileScraper scraper = new AgileScraper();
        HardcodedConfigFactory configFactory = new HardcodedConfigFactory();
        //PrimitiveConfiguration configuration = configFactory.getSpranCommSell();
        //PrimitiveConfiguration configuration = configFactory.getSpranFlatSell();
        //PrimitiveConfiguration configuration = configFactory.getProstoTomskCommSell();
        //PrimitiveConfiguration configuration = configFactory.getProstoTomskCommRent();
        PrimitiveConfiguration configuration = configFactory.getUpWorkDataScrapingJobs();
        scraper.run(configuration);
    }
}
