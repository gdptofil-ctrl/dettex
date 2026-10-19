/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_124
 *  net.minecraft.class_1799
 *  net.minecraft.class_2561
 *  net.minecraft.class_9323
 */
package ru.recoin.common.util.auction;

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.class_124;
import net.minecraft.class_1799;
import net.minecraft.class_2561;
import net.minecraft.class_9323;

public class AuctionPriceParser {
    private static final Pattern PRICE_PATTERN = Pattern.compile("\\$\\s*([\\d\\s.,]+)");
    private static final Pattern KEYWORD_PRICE_PATTERN = Pattern.compile("(?i)(?:\u0446\u0435\u043d\u0430|\u0441\u0442\u043e\u0438\u043c\u043e\u0441\u0442\u044c|price|cost|buy(?:out)?)[^\\d]{0,16}([\\d\\s.,]+)");
    private static final Pattern TOOLTIP_TOTAL_PRICE = Pattern.compile("(?:\u0446\u0435\u043d\u0430|\u0441\u0442\u043e\u0438\u043c\u043e\u0441\u0442\u044c|price|cost)\\s*:?\\s*[$\u20bd]?\\s*([0-9][0-9\\s.,]*)", 66);
    private static final Pattern TOOLTIP_TRAILING_CURRENCY = Pattern.compile("(?:\u0446\u0435\u043d\u0430|\u0441\u0442\u043e\u0438\u043c\u043e\u0441\u0442\u044c|price|cost)\\s*:?\\s*([0-9][0-9\\s.,]*)\\s*[$\u20bd]|([0-9][0-9\\s.,]*)\\s*[$\u20bd]", 66);
    private static final Pattern TOOLTIP_CURRENCY_PRICE = Pattern.compile("[$\u20bd]\\s*([0-9][0-9\\s.,]*)");
    private static final Pattern FUNTIME_TOTAL_PRICE = Pattern.compile("^\\s*(?:[$\u20bd]\\s*)?\u0446\u0435\u043d[\u0430a]\\s*:\\s*[$\u20bd]?\\s*([0-9][0-9\\s.,]*)\\s*$", 66);
    private static final Pattern MARKET_PRICE = Pattern.compile("(?:\u0446\u0435\u043d\u0430|\u0441\u0442\u043e\u0438\u043c\u043e\u0441\u0442\u044c|price|cost)(?:\\s+(?:\u043b\u043e\u0442\u0430|\u0442\u043e\u0432\u0430\u0440\u0430|\u043f\u043e\u043a\u0443\u043f\u043a\u0438|\u0432\u0441\u0435\u0433\u043e))?\\s*[:\uff1a-]?\\s*[$\u20bd]?\\s*([0-9]+(?:[\\s.,][0-9]+)*)(?:\\s*([k\u043am\u043c]))?", 66);
    private static final Pattern MARKET_UNIT_PRICE = Pattern.compile("(?:(?:\u0446\u0435\u043d\u0430|\u0441\u0442\u043e\u0438\u043c\u043e\u0441\u0442\u044c|price|cost)\\s*)?(?:\u0437\u0430\\s*(?:1\\s*)?\u0448\u0442(?:\u0443\u043a[\u0443\u044b])?|/\\s*\u0448\u0442|per\\s*(?:item|unit))\\s*[:\uff1a-]?\\s*[$\u20bd]?\\s*([0-9]+(?:[\\s.,][0-9]+)*)(?:\\s*([k\u043am\u043c]))?", 66);

    public int getPrice(class_1799 stack) {
        class_9323 tag = stack.method_57353();
        if (tag == null) {
            return -1;
        }
        String components = tag.toString();
        int directPrice = this.tryParse(components, PRICE_PATTERN);
        if (directPrice >= 0) {
            return directPrice;
        }
        return this.tryParse(components, KEYWORD_PRICE_PATTERN);
    }

    public static long getTooltipTotal(List<class_2561> tooltip) {
        if (tooltip == null) {
            return -1L;
        }
        long ft = AuctionPriceParser.getFunTimeTooltipTotal(tooltip);
        if (ft >= 0L) {
            return ft;
        }
        long currencyFallback = -1L;
        for (class_2561 text : tooltip) {
            String raw = text.getString();
            String line = class_124.method_539((String)raw);
            if (line == null || line.isBlank() || AuctionPriceParser.isPerItemLine(line = line.toLowerCase(Locale.ROOT).replace('\u0451', '\u0435').trim())) continue;
            long exact = AuctionPriceParser.parseLong(line, TOOLTIP_TOTAL_PRICE);
            if (exact >= 0L) {
                return exact;
            }
            exact = AuctionPriceParser.parseLong(line, TOOLTIP_TRAILING_CURRENCY);
            if (exact >= 0L) {
                return exact;
            }
            if (currencyFallback >= 0L) continue;
            currencyFallback = AuctionPriceParser.parseLong(line, TOOLTIP_CURRENCY_PRICE);
        }
        return currencyFallback;
    }

    public static long getFunTimeTooltipTotal(List<class_2561> tooltip) {
        if (tooltip == null) {
            return -1L;
        }
        for (int i = 0; i < tooltip.size(); ++i) {
            long price;
            String raw = tooltip.get(i).getString();
            String line = class_124.method_539((String)raw);
            if (line == null || AuctionPriceParser.isPerItemLine(line = line.toLowerCase(Locale.ROOT).replace('\u0451', '\u0435').trim()) || (price = AuctionPriceParser.parseLong(line, FUNTIME_TOTAL_PRICE)) < 0L) continue;
            return price;
        }
        return -1L;
    }

    public static long getMarketUnitPrice(List<class_2561> tooltip, int count) {
        if (tooltip == null) {
            return -1L;
        }
        long unitPrice = -1L;
        for (class_2561 text : tooltip) {
            String line = class_124.method_539((String)text.getString());
            if (line == null || line.isBlank()) continue;
            long price = AuctionPriceParser.parseMarketPrice(line, AuctionPriceParser.isPerItemLine(line = line.toLowerCase(Locale.ROOT).replace('\u0451', '\u0435').replace('\u00a0', ' ').replace('\u202f', ' ').trim()) ? MARKET_UNIT_PRICE : MARKET_PRICE);
            if (price <= 0L && AuctionPriceParser.isPerItemLine(line)) {
                price = AuctionPriceParser.parseMarketPrice(line, MARKET_PRICE);
            }
            if (price <= 0L) continue;
            if (AuctionPriceParser.isPerItemLine(line)) {
                if (unitPrice >= 0L) continue;
                unitPrice = price;
                continue;
            }
            return Math.max(1L, price / (long)Math.max(1, count));
        }
        long total = AuctionPriceParser.getTooltipTotal(tooltip);
        if (total > 0L) {
            return Math.max(1L, total / (long)Math.max(1, count));
        }
        return unitPrice;
    }

    private static long parseMarketPrice(String line, Pattern pattern) {
        Matcher matcher = pattern.matcher(line);
        if (!matcher.find()) {
            return -1L;
        }
        String amount = matcher.group(1);
        String suffix = matcher.group(2);
        try {
            if (suffix != null) {
                double value = Double.parseDouble(amount.replace(" ", "").replace(',', '.'));
                double multiplier = suffix.equalsIgnoreCase("m") || suffix.equalsIgnoreCase("\u043c") ? 1000000.0 : 1000.0;
                double price = value * multiplier;
                return price > 0.0 && price <= 9.223372036854776E18 ? Math.round(price) : -1L;
            }
            String digits = amount.replaceAll("[^0-9]", "");
            return digits.isEmpty() ? -1L : Long.parseLong(digits);
        }
        catch (NumberFormatException ignored) {
            return -1L;
        }
    }

    public static boolean isPerItemLine(String line) {
        return line.contains("\u0437\u0430 \u0448\u0442\u0443\u043a\u0443") || line.contains("\u0437\u0430 1 \u0448\u0442") || line.contains("/\u0448\u0442") || line.contains("\u0437\u0430 \u0448\u0442") || line.contains("per item") || line.contains("per unit") || line.contains("unit price");
    }

    private static long parseLong(String line, Pattern pattern) {
        String digits;
        String match;
        Matcher matcher = pattern.matcher(line);
        if (!matcher.find()) {
            return -1L;
        }
        String string = match = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
        if (match == null) {
            match = matcher.group();
        }
        if ((digits = match.replaceAll("[^0-9]", "")).isEmpty()) {
            return -1L;
        }
        try {
            return Long.parseLong(digits);
        }
        catch (NumberFormatException ignored) {
            return -1L;
        }
    }

    private int tryParse(String input, Pattern pattern) {
        Matcher matcher = pattern.matcher(input);
        if (!matcher.find()) {
            return -1;
        }
        String price = matcher.group(1).replaceAll("[^\\d]", "");
        if (price.isEmpty()) {
            return -1;
        }
        try {
            return Integer.parseInt(price);
        }
        catch (NumberFormatException ignored) {
            return -1;
        }
    }
}

