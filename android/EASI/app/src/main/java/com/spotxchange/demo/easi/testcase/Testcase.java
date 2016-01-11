package com.spotxchange.demo.easi.testcase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Model representing a single set of test conditions.
 */
public class Testcase implements Parcelable {
    public static final int NOT_RUN = -1;
    public static final int PASSED = 108;
    public static final int FAILED = 109;

    public final String scriptlet;
    public int state = NOT_RUN;

    public Testcase(String columns, String input) {
        this.scriptlet = parseScriptlet(columns, input);
    }

    public Testcase(Parcel parcel) {
        this.scriptlet = parcel.readString();
        this.state = parcel.readInt();
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


    /* Parcelable methods */
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(scriptlet);
        parcel.writeInt(state);
    }

    public static final Creator<Testcase> CREATOR = new Creator<Testcase>() {
        @Override
        public Testcase createFromParcel(Parcel parcel) {
            return new Testcase(parcel);
        }

        @Override
        public Testcase[] newArray(int size) {
            return new Testcase[size];
        }
    };
}
