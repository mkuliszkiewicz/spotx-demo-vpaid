package com.spotxchange.demo.easi.testcases;

import android.os.Looper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import android.os.Handler;

/**
 * Model representing a single set of test conditions.
 */
public class Testcase {
    public final String scriptlet;

    public Testcase(String columns, String input) {
        this.scriptlet = parseScriptlet(columns, input);
    }

    public static String parseScriptlet(String columns, String input) {
        String scriptlet = "";
        Iterator<String> i = Arrays.asList(columns.split("\\s+")).iterator();
        Iterator<String> j = Arrays.asList(input.split("\\s+")).iterator();

        while (i.hasNext() && j.hasNext())
        {
            scriptlet += String.format("%1$s=\"%2$s\" ", i.next(), j.next());
        }

        if (i.hasNext() || j.hasNext()) {
            throw new IllegalArgumentException("Test case input argument length does not match number of case conditions.");
        }

        return scriptlet;
    }

    public static List<Testcase> parseTestcasesFromPictOutput(String pictOutput)
    {
        ArrayList<String> rows = new ArrayList<>(Arrays.asList(pictOutput.split("\\n")));
        List<Testcase> testcases = new ArrayList<Testcase>();

        String columnNames = rows.remove(0);

        for (String row : rows)
        {
            testcases.add(new Testcase(columnNames, row));
        }

        return testcases;
    }

    @Override
    public String toString() {
        return scriptlet;
    }
}
