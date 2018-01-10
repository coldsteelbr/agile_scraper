package ru.romanbrazhnikov;

import ru.romanbrazhnikov.agilescraper.AgileScraper;
import ru.romanbrazhnikov.agilescraper.configuration.PrimitiveConfigBuilder;
import ru.romanbrazhnikov.agilescraper.hardcodedconfigs.HardcodedConfigFactory;
import ru.romanbrazhnikov.agilescraper.hardcodedconfigs.PrimitiveConfiguration;



public class Main {
    private static final String SPRAN_FLATS = "conf_spran_flat_sale.prs";
    private static final String SPRAN_COMM_SALE = "conf_spran_comm_sale.prs";
    private static final String SPRAN_FLATS_TEST = "conf_spran_flat_sale_test.prs";
    private static final String NOMER_JANNA = "conf_nomer_janna.prs";

    public static void main(String[] args){
        String configFileName = args[0];

        AgileScraper scraper = new AgileScraper();
        //HardcodedConfigFactory configFactory = new HardcodedConfigFactory();
        //PrimitiveConfiguration configuration = configFactory.getSpranCommSell();
        //PrimitiveConfiguration configuration = configFactory.getSpranFlatSell();
        //PrimitiveConfiguration configuration = configFactory.getProstoTomskCommSell();
        //PrimitiveConfiguration configuration = configFactory.getProstoTomskCommRent();
        //PrimitiveConfiguration configuration = configFactory.getUpWorkDataScrapingJobs();

        PrimitiveConfigBuilder builder = new PrimitiveConfigBuilder();
        String configToDownload = configFileName;
        builder.readFromXmlFile(configToDownload);
        PrimitiveConfiguration configuration = builder.init();
        //System.out.println(configuration.getDebugInfo());

        scraper.run(configuration);
    }
}
