/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.implement.features.commands.defaults;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import ru.recoin.api.feature.command.Command;
import ru.recoin.api.feature.command.argument.IArgConsumer;
import ru.recoin.api.feature.command.exception.CommandException;
import ru.recoin.api.repository.rct.RCTRepository;
import ru.recoin.common.QuickImports;
import ru.recoin.common.util.world.ServerUtil;
import ru.recoin.core.Main;
import ru.recoin.implement.features.modules.misc.PvPSafe;

public class RCTCommand
extends Command
implements QuickImports {
    private final RCTRepository repository;

    protected RCTCommand(Main main) {
        super("rct");
        this.repository = main.getRCTRepository();
    }

    @Override
    public void execute(String label, IArgConsumer args2) throws CommandException {
        if (PvPSafe.getInstance().isState() && ServerUtil.isPvp()) {
            PvPSafe.getInstance().warnBlocked();
            return;
        }
        if (args2.hasAny()) {
            args2.requireMin(1);
            int anarchy = args2.getArgs().getFirst().getAs(Integer.class);
            this.repository.reconnect(anarchy);
        } else {
            this.repository.reconnect(ServerUtil.getAnarchy());
        }
    }

    @Override
    public Stream<String> tabComplete(String label, IArgConsumer args2) throws CommandException {
        return Stream.empty();
    }

    @Override
    public String getShortDesc() {
        return "\u0420\u045f\u0420\u00b5\u0421\u0402\u0420\u00b5\u0420\u00b7\u0420\u00b0\u0421\u2026\u0420\u0455\u0420\u0491\u0420\u0451\u0421\u201a \u0420\u0405\u0420\u00b0 \u0420\u00b0\u0420\u0405\u0420\u00b0\u0421\u0402\u0421\u2026\u0420\u0451\u0421\u040b";
    }

    @Override
    public List<String> getLongDesc() {
        return Arrays.asList("\u0420\u045f\u0420\u00b5\u0421\u0402\u0420\u00b5\u0420\u00b7\u0420\u00b0\u0421\u2026\u0420\u0455\u0420\u0491\u0420\u0451\u0421\u201a \u0420\u0405\u0420\u00b0 \u0420\u00b0\u0420\u0405\u0420\u00b0\u0421\u0402\u0421\u2026\u0420\u0451\u0421\u040b", "", "\u0420\u0098\u0421\u0403\u0420\u0457\u0420\u0455\u0420\u00bb\u0421\u040a\u0420\u00b7\u0420\u0455\u0420\u0406\u0420\u00b0\u0420\u0405\u0420\u0451\u0420\u00b5:", "> rct <anarchy> - \u0420\u2014\u0420\u00b0\u0421\u2026\u0420\u0455\u0420\u0491\u0420\u0451\u0421\u201a \u0420\u0405\u0420\u00b0 <anarchy>", "> rct - \u0420\u045f\u0420\u00b5\u0421\u0402\u0420\u00b5\u0420\u00b7\u0420\u00b0\u0421\u2026\u0420\u0455\u0420\u0491\u0420\u0451\u0421\u201a \u0420\u0405\u0420\u00b0 \u0420\u00b0\u0420\u0405\u0420\u00b0\u0421\u0402\u0421\u2026\u0420\u0451\u0421\u040b \u0420\u0456\u0420\u0491\u0420\u00b5 \u0420\u0406\u0421\u2039 \u0421\u201a\u0420\u0455\u0420\u00bb\u0421\u040a\u0420\u0454\u0420\u0455 \u0421\u2021\u0421\u201a\u0420\u0455 \u0420\u00b1\u0421\u2039\u0420\u00bb\u0420\u0451");
    }
}

