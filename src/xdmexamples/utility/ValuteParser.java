/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xdmexamples.utility;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javafx.util.StringConverter;

/**
 *
 * @author sakit
 */
public class ValuteParser {

    private static final String url = "http://currencies.apps.grandtrunk.net/getrange/";
    private static final double[][][][] values = new double[16][12][31][6];

    public String[] startParsing(LocalDate startDate, LocalDate endDate, String currencyFrom, int index) {
        List<String> result = new ArrayList<>();
        try {
            //http://currencies.apps.grandtrunk.net/getrange/<fromdate>/<todate>/<fromcode>/<tocode>
            String path = url + converter.toString(startDate) + "/" + converter.toString(endDate)
                    + "/" + currencyFrom + "/azn";
            URL oracle = new URL(path);
            try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(oracle.openStream()))) {
                String inputLine;

                while ((inputLine = in.readLine()) != null) {
                    System.out.println(inputLine);
                    result.add(inputLine);
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return result.toArray(new String[result.size()]);
    }

    private final StringConverter converter = new StringConverter<LocalDate>() {
        DateTimeFormatter dateFormatter
                = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        @Override
        public String toString(LocalDate date) {
            if (date != null) {
                return dateFormatter.format(date);
            } else {
                return "";
            }
        }

        @Override
        public LocalDate fromString(String string) {
            if (string != null && !string.isEmpty()) {
                return LocalDate.parse(string, dateFormatter);
            } else {
                return null;
            }
        }
    };

    public static double getValute(int year, int monthValue, int dayOfMonth, int index) {
        return values[year - 2000][monthValue - 1][dayOfMonth - 1][index];
    }

    public static double getValuteForYear(int year, int currency) {
        double maxVal = 0.0;

        for (int m = 0; m < 12; m++) {
            for (int d = 0; d < 31; d++) {
                if (maxVal <= values[year - 2000][m][d][currency]) {
                    maxVal = values[year - 2000][m][d][currency];
                }
            }
        }
        return maxVal;
    }

    public static double getValuteForMonth(int year, int month, int currency) {
        double maxVal = 0.0;
        for (int i = 0; i < 31; i++) {
            if (maxVal <= values[year - 2000][month - 1][i][currency]) {
                maxVal = values[year - 2000][month - 1][i][currency];
            }
        }
        return maxVal;
    }

    public static void setValue(String str, int currency) {
        String[] tmp = str.split(" ");
        int y = Integer.valueOf(tmp[0].substring(0, 4)) - 2000;
        int m = Integer.valueOf(tmp[0].substring(5, 7)) - 1;
        int d = Integer.valueOf(tmp[0].substring(8, 10)) - 1;
        double v = Double.valueOf(tmp[1]);
        values[y][m][d][currency] = v;
    }
}
