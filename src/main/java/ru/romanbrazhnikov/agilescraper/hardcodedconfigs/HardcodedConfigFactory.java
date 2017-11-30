package ru.romanbrazhnikov.agilescraper.hardcodedconfigs;

import ru.romanbrazhnikov.agilescraper.requestarguments.Argument;
import ru.romanbrazhnikov.agilescraper.requestarguments.Values;
import ru.romanbrazhnikov.agilescraper.sourceprovider.HttpMethods;

import java.util.ArrayList;
import java.util.HashMap;

public class HardcodedConfigFactory {
    public static final String PARAM_PAGE = "{[PAGE]}";
    public static final String paramDistrict = "{[DISTRICT]}";
    public static final String fieldDistrict = "district";

    public static final String paramString = "areas%5B0%5D=" + paramDistrict + "&currency=1&costMode=1&cities%5B0%5D=21&page=" + PARAM_PAGE;

    public PrimitiveConfiguration getSpranCommSell() {
        PrimitiveConfiguration configuration = new PrimitiveConfiguration();

        configuration.baseUrl = "http://spran.ru/sell/comm.html";
        configuration.requestParams = paramString;
        configuration.mFirstLevelPattern =
                "<tr[^>]*>\\s*\n" +
                        "<td[^>]*>\\s*(?<TYPE>.*?)\\s*</td>\\s*\n" +
                        "<td[^>]*>\\s*(?:.*?)</td>\\s*\n" +
                        "<td[^>]*>\\s*<a(?:.*?)href\\s*=\\s*\"(?<SECONDLEVEL>.*?)\"[^>]*>\\s*(?<ADDRESS>.*?)\\s*</a>\\s*</td>\\s*\n" +
                        "<td[^>]*>\\s*<span[^>]*>\\s*<span[^>]*>\\s*(?<SQUARE>.*?)\\s*</span>\\s*</span>\\s*</td>\\s*\n" +
                        "<td[^>]*>\\s*<span[^>]*>\\s*(?<TOTALPRICE>.*?)\\s*</span>\\s*(?:.*?)</td>\\s*\n" +
                        "<td[^>]*>\\s*(?<CONTACT>.*?)\\s*</td>\\s*";
        configuration.mSecondLevelPattern =
                "комментарий\\s*продавца</h[1-6]+>\\s*\n" +
                        "<p[^>]*>\\s*(?<NOTES>.*?)\\s*</p>";

        configuration.mFirstLevelBindings = new HashMap<>();
        configuration.mFirstLevelBindings.put("TYPE", "type");
        configuration.mFirstLevelBindings.put("ADDRESS", "address");
        configuration.mFirstLevelBindings.put("SQUARE", "square");
        configuration.mFirstLevelBindings.put("TOTALPRICE", "price");
        configuration.mFirstLevelBindings.put("CONTACT", "contact_info");
        configuration.mFirstLevelBindings.put(configuration.secondLevelName, configuration.secondLevelName);


        configuration.mSecondLevelBindings = new HashMap<>();
        configuration.mSecondLevelBindings.put("NOTES", "notes");


        configuration.secondLevelBaseUrl = "http://spran.ru";

        // Request arguments
        configuration.mRequestArguments.requestArguments = new ArrayList<>();
        Argument argumentDistrict = new Argument();
        argumentDistrict.name = paramDistrict;
        argumentDistrict.field = fieldDistrict;
        argumentDistrict.mValues = new ArrayList<>();
        argumentDistrict.mValues.add(new Values("22", "Дзержинский"));
        argumentDistrict.mValues.add(new Values("23", "Железнодорожный"));
        argumentDistrict.mValues.add(new Values("24", "Заельцовский"));
        argumentDistrict.mValues.add(new Values("25", "Килининский"));
        argumentDistrict.mValues.add(new Values("26", "Кировский"));
        argumentDistrict.mValues.add(new Values("27", "Ленинский"));
        argumentDistrict.mValues.add(new Values("29", "Октябрьский"));
        argumentDistrict.mValues.add(new Values("30", "Первомайский"));
        argumentDistrict.mValues.add(new Values("31", "Советский"));
        argumentDistrict.mValues.add(new Values("32", "Центральный"));
        configuration.mRequestArguments.requestArguments.add(argumentDistrict);
        configuration.mRequestArguments.initProvider(configuration.requestParams);

        // Markers
        configuration.markers.put("city", "Новосибирск");
        configuration.markers.put("market", "ком. продажа");

        return configuration;
    }

    public PrimitiveConfiguration getProstoTomskCommSell() {
        PrimitiveConfiguration configuration = new PrimitiveConfiguration();
        configuration.method = HttpMethods.GET;
        configuration.baseUrl = "http://prosto.tomsk.ru";
        configuration.requestParams = "rm=prosto_offers_list&l_page=" + PARAM_PAGE;
        configuration.firstPageNum = 1; // default
        configuration.pageStep = 1; // default
        configuration.maxPagePattern = "page\\s*=\\s*[0-9]+\"><span[^>]*>(?<PAGENUM>.*?)\\s*</span>\\s*</a>";
        // TODO: add custom COOKIES here
        configuration.mDestinationName = "prosto_tomsk";
        configuration.mFirstLevelPattern = "<tr\\s*id\\s*=\\s*\"offer[^>]*>\\s*\n" +
                "<td[^>]*>\\s*(?:.*?)</td>\\s*\n" +
                "<td[^>]*>\\s*<div[^>]*>\\s*(?<TYPE>.*?)\\s*</div>\\s*</td>\\s*\n" +
                "<td[^>]*>\\s*<div[^>]*>\\s*(?<ADDRESS>.*?)\\s*</div>\\s*\n" +
                "(?:\n" +
                "   (?:.*?)\n" +
                "   (?:<div[^>]*>\\s*(?<DISTRICT>.*?)\\s*</div>)\n" +
                ")?\n" +
                "\\s*</td>\\s*\n" +
                "<td[^>]*>\\s*(?<TOTALSQUARE>.*?)\\s*</td>\\s*\n" +
                "<td[^>]*>\\s*<div[^>]*>\\s*(?<TOTALPRICE>.*?)\\s*</div>\\s*\n" +
                "<div[^>]*>\\s*(?:.*?)</div>\\s*</td>\\s*";
        configuration.mFirstLevelBindings = new HashMap<>();
        configuration.mFirstLevelBindings.put("TYPE", "type");
        configuration.mFirstLevelBindings.put("ADDRESS", "address");
        configuration.mFirstLevelBindings.put("DISTRICT", "district");
        configuration.mFirstLevelBindings.put("TOTALSQUARE", "total_square");
        configuration.mFirstLevelBindings.put("TOTALPRICE", "total_price");


        return configuration;
    }
}
