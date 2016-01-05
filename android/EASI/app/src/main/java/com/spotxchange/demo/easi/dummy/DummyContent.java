package com.spotxchange.demo.easi.dummy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class DummyContent {

    /**
     * An array of sample (dummy) items.
     */
    public static final List<Testcase> ITEMS = new ArrayList<Testcase>();

    private static final int COUNT = 25;

    static {
        // Add some sample items.
        for (int i = 1; i <= COUNT; i++) {
            addItem(createDummyItem(i));
        }
    }

    private static void addItem(Testcase item) {
        ITEMS.add(item);
    }

    private static Testcase createDummyItem(int position) {
        return new Testcase(
            "data-spotx_channel_id\tdata-spotx_ad_volume\tdata-spotx_loop\tdata-spotx_click_to_replay\tdata-spotx_unmute_on_mouse\tdata-spotx_collapse\n",
            "126302\t100\t1\t1\t0\t0"
            );
    }

    private static String makeDetails(int position) {
        StringBuilder builder = new StringBuilder();
        builder.append("Details about Item: ").append(position);
        for (int i = 0; i < position; i++) {
            builder.append("\nMore details information here.");
        }
        return builder.toString();
    }

    /**
     * A dummy item representing a piece of content.
     */
    public static class Testcase {
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

        @Override
        public String toString() {
            return scriptlet;
        }
    }
}
