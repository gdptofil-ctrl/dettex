/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_124
 *  net.minecraft.class_2338
 *  net.minecraft.class_2596
 *  net.minecraft.class_2828$class_2829
 *  net.minecraft.class_2828$class_5911
 */
package ru.recoin.implement.features.commands.defaults;

import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import net.minecraft.class_124;
import net.minecraft.class_2338;
import net.minecraft.class_2596;
import net.minecraft.class_2828;
import ru.recoin.api.feature.command.Command;
import ru.recoin.api.feature.command.argument.IArgConsumer;
import ru.recoin.api.feature.command.exception.CommandException;

public final class VClipCommand
extends Command {
    public VClipCommand() {
        super("vclip");
    }

    @Override
    public void execute(String label, IArgConsumer args2) throws CommandException {
        double offset;
        args2.requireExactly(1);
        if (VClipCommand.mc.field_1724 == null || VClipCommand.mc.field_1687 == null || VClipCommand.mc.field_1724.field_3944 == null) {
            return;
        }
        String input = args2.getString();
        switch (input.toLowerCase(Locale.ROOT)) {
            case "up": {
                offset = this.findOffset(true);
                break;
            }
            case "down": {
                offset = this.findOffset(false);
                break;
            }
            default: {
                try {
                    offset = Double.parseDouble(input);
                    break;
                }
                catch (NumberFormatException error) {
                    this.logDirect(input + " \u043d\u0435 \u044f\u0432\u043b\u044f\u0435\u0442\u0441\u044f \u0447\u0438\u0441\u043b\u043e\u043c", class_124.field_1061);
                    return;
                }
            }
        }
        if (!Double.isFinite(offset) || offset == 0.0) {
            this.logDirect("\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u0432\u044b\u043f\u043e\u043b\u043d\u0438\u0442\u044c \u0442\u0435\u043b\u0435\u043f\u043e\u0440\u0442\u0430\u0446\u0438\u044e", class_124.field_1061);
            return;
        }
        double x = VClipCommand.mc.field_1724.method_23317();
        double y = VClipCommand.mc.field_1724.method_23318();
        double z = VClipCommand.mc.field_1724.method_23321();
        for (int i = 0; i < 3; ++i) {
            VClipCommand.mc.field_1724.field_3944.method_52787((class_2596)new class_2828.class_5911(VClipCommand.mc.field_1724.method_24828(), VClipCommand.mc.field_1724.field_5976));
        }
        VClipCommand.mc.field_1724.field_3944.method_52787((class_2596)new class_2828.class_2829(x, y + offset, z, false, VClipCommand.mc.field_1724.field_5976));
        VClipCommand.mc.field_1724.method_5814(x, y + offset, z);
        this.logDirect("\u0422\u0435\u043b\u0435\u043f\u043e\u0440\u0442\u0438\u0440\u043e\u0432\u0430\u043d\u043e \u043d\u0430 " + (int)offset + " \u0431\u043b\u043e\u043a\u043e\u0432 \u043f\u043e \u0432\u0435\u0440\u0442\u0438\u043a\u0430\u043b\u0438", class_124.field_1080);
    }

    private double findOffset(boolean up) {
        class_2338 origin = VClipCommand.mc.field_1724.method_24515();
        if (up) {
            for (int i = 3; i < 255; ++i) {
                class_2338 feet = origin.method_10086(i);
                if (!VClipCommand.mc.field_1687.method_8320(feet).method_26215() || !VClipCommand.mc.field_1687.method_8320(feet.method_10084()).method_26215()) continue;
                return (double)feet.method_10264() - VClipCommand.mc.field_1724.method_23318();
            }
        } else {
            for (int i = -1; i > -255; --i) {
                class_2338 solid = origin.method_10069(0, i, 0);
                if (VClipCommand.mc.field_1687.method_8320(solid).method_26215() || !VClipCommand.mc.field_1687.method_8320(solid.method_10074()).method_26215() || !VClipCommand.mc.field_1687.method_8320(solid.method_10087(2)).method_26215()) continue;
                return (double)solid.method_10087(2).method_10264() - VClipCommand.mc.field_1724.method_23318();
            }
        }
        return 0.0;
    }

    @Override
    public Stream<String> tabComplete(String label, IArgConsumer args2) {
        return args2.hasExactlyOne() ? Stream.of("up", "down") : Stream.empty();
    }

    @Override
    public String getShortDesc() {
        return "\u0422\u0435\u043b\u0435\u043f\u043e\u0440\u0442 \u043f\u043e \u0432\u0435\u0440\u0442\u0438\u043a\u0430\u043b\u0438";
    }

    @Override
    public List<String> getLongDesc() {
        return List.of("vclip <\u0440\u0430\u0441\u0441\u0442\u043e\u044f\u043d\u0438\u0435>", "vclip up", "vclip down");
    }
}

