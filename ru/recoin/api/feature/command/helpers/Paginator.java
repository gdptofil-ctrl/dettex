/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_124
 *  net.minecraft.class_2558
 *  net.minecraft.class_2558$class_10609
 *  net.minecraft.class_2561
 *  net.minecraft.class_2568
 *  net.minecraft.class_2568$class_10613
 *  net.minecraft.class_5250
 */
package ru.recoin.api.feature.command.helpers;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import net.minecraft.class_124;
import net.minecraft.class_2558;
import net.minecraft.class_2561;
import net.minecraft.class_2568;
import net.minecraft.class_5250;
import ru.recoin.api.feature.command.argument.IArgConsumer;
import ru.recoin.api.feature.command.exception.CommandException;
import ru.recoin.api.feature.command.exception.CommandInvalidTypeException;
import ru.recoin.common.QuickLogger;

public class Paginator<E>
implements QuickLogger {
    public final List<E> entries;
    public int pageSize = 8;
    public int page = 1;

    public Paginator(List<E> entries) {
        this.entries = entries;
    }

    @SafeVarargs
    public Paginator(E ... entries) {
        this.entries = Arrays.asList(entries);
    }

    public Paginator<E> setPageSize(int pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    public int getMaxPage() {
        return (this.entries.size() - 1) / this.pageSize + 1;
    }

    public boolean validPage(int page) {
        return page > 0 && page <= this.getMaxPage();
    }

    public void skipPages(int pages) {
        this.page += pages;
    }

    public void display(Function<E, class_2561> transform, String commandPrefix) {
        int offset;
        for (int i = offset = (this.page - 1) * this.pageSize; i < offset + this.pageSize; ++i) {
            if (i >= this.entries.size()) continue;
            this.logDirect(transform.apply(this.entries.get(i)));
        }
        boolean hasPrevPage = commandPrefix != null && this.validPage(this.page - 1);
        boolean hasNextPage = commandPrefix != null && this.validPage(this.page + 1);
        class_5250 prevPageComponent = class_2561.method_43470((String)"<<");
        if (hasPrevPage) {
            prevPageComponent.method_10862(prevPageComponent.method_10866().method_10958((class_2558)new class_2558.class_10609(String.format("%s %d", commandPrefix, this.page - 1))).method_10949((class_2568)new class_2568.class_10613((class_2561)class_2561.method_43470((String)"Click to view previous page"))));
        } else {
            prevPageComponent.method_10862(prevPageComponent.method_10866().method_10977(class_124.field_1063));
        }
        class_5250 nextPageComponent = class_2561.method_43470((String)">>");
        if (hasNextPage) {
            nextPageComponent.method_10862(nextPageComponent.method_10866().method_10958((class_2558)new class_2558.class_10609(String.format("%s %d", commandPrefix, this.page + 1))).method_10949((class_2568)new class_2568.class_10613((class_2561)class_2561.method_43470((String)"Click to view next page"))));
        } else {
            nextPageComponent.method_10862(nextPageComponent.method_10866().method_10977(class_124.field_1063));
        }
        class_5250 pagerComponent = class_2561.method_43470((String)"");
        pagerComponent.method_10862(pagerComponent.method_10866().method_10977(class_124.field_1080));
        pagerComponent.method_10852((class_2561)prevPageComponent);
        pagerComponent.method_27693(" | ");
        pagerComponent.method_10852((class_2561)nextPageComponent);
        pagerComponent.method_27693(String.format(" %d/%d", this.page, this.getMaxPage()));
        this.logDirect(new class_2561[]{pagerComponent});
    }

    public void display(Function<E, class_2561> transform) {
        this.display(transform, null);
    }

    public static <T> void paginate(IArgConsumer consumer, Paginator<T> pagi, Runnable pre, Function<T, class_2561> transform, String commandPrefix) throws CommandException {
        int page = 1;
        consumer.requireMax(1);
        if (consumer.hasAny() && !pagi.validPage(page = consumer.getAs(Integer.class).intValue())) {
            throw new CommandInvalidTypeException(consumer.consumed(), String.format("a valid page (1-%d)", pagi.getMaxPage()), consumer.consumed().getValue());
        }
        pagi.skipPages(page - pagi.page);
        if (pre != null) {
            pre.run();
        }
        pagi.display(transform, commandPrefix);
    }

    public static <T> void paginate(IArgConsumer consumer, List<T> elems, Runnable pre, Function<T, class_2561> transform, String commandPrefix) throws CommandException {
        Paginator.paginate(consumer, new Paginator<T>(elems), pre, transform, commandPrefix);
    }

    public static <T> void paginate(IArgConsumer consumer, T[] elems, Runnable pre, Function<T, class_2561> transform, String commandPrefix) throws CommandException {
        Paginator.paginate(consumer, Arrays.asList(elems), pre, transform, commandPrefix);
    }

    public static <T> void paginate(IArgConsumer consumer, Paginator<T> pagi, Function<T, class_2561> transform, String commandPrefix) throws CommandException {
        Paginator.paginate(consumer, pagi, null, transform, commandPrefix);
    }

    public static <T> void paginate(IArgConsumer consumer, List<T> elems, Function<T, class_2561> transform, String commandPrefix) throws CommandException {
        Paginator.paginate(consumer, new Paginator<T>(elems), null, transform, commandPrefix);
    }

    public static <T> void paginate(IArgConsumer consumer, T[] elems, Function<T, class_2561> transform, String commandPrefix) throws CommandException {
        Paginator.paginate(consumer, Arrays.asList(elems), null, transform, commandPrefix);
    }

    public static <T> void paginate(IArgConsumer consumer, Paginator<T> pagi, Runnable pre, Function<T, class_2561> transform) throws CommandException {
        Paginator.paginate(consumer, pagi, pre, transform, null);
    }

    public static <T> void paginate(IArgConsumer consumer, List<T> elems, Runnable pre, Function<T, class_2561> transform) throws CommandException {
        Paginator.paginate(consumer, new Paginator<T>(elems), pre, transform, null);
    }

    public static <T> void paginate(IArgConsumer consumer, T[] elems, Runnable pre, Function<T, class_2561> transform) throws CommandException {
        Paginator.paginate(consumer, Arrays.asList(elems), pre, transform, null);
    }

    public static <T> void paginate(IArgConsumer consumer, Paginator<T> pagi, Function<T, class_2561> transform) throws CommandException {
        Paginator.paginate(consumer, pagi, null, transform, null);
    }

    public static <T> void paginate(IArgConsumer consumer, List<T> elems, Function<T, class_2561> transform) throws CommandException {
        Paginator.paginate(consumer, new Paginator<T>(elems), null, transform, null);
    }

    public static <T> void paginate(IArgConsumer consumer, T[] elems, Function<T, class_2561> transform) throws CommandException {
        Paginator.paginate(consumer, Arrays.asList(elems), null, transform, null);
    }
}

