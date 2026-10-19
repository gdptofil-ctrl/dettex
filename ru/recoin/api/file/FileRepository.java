/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.api.file;

import java.util.ArrayList;
import java.util.List;
import ru.recoin.api.file.ClientFile;
import ru.recoin.api.file.impl.BlockESPFile;
import ru.recoin.api.file.impl.EntityESPFile;
import ru.recoin.api.file.impl.FriendFile;
import ru.recoin.api.file.impl.MacroFile;
import ru.recoin.api.file.impl.ModuleFile;
import ru.recoin.api.file.impl.PrefixFile;
import ru.recoin.api.file.impl.StaffFile;
import ru.recoin.api.file.impl.WayFile;
import ru.recoin.core.Main;

public class FileRepository {
    private final List<ClientFile> clientFiles = new ArrayList<ClientFile>();

    public void setup(Main main) {
        this.register(new ModuleFile(main.getModuleRepository(), main.getDraggableRepository()), new EntityESPFile(main.getBoxESPRepository()), new BlockESPFile(main.getBoxESPRepository()), new MacroFile(main.getMacroRepository()), new WayFile(main.getWayRepository()), new PrefixFile(), new FriendFile(), new StaffFile());
    }

    public void register(ClientFile ... clientFIle) {
        this.clientFiles.addAll(List.of(clientFIle));
    }

    public List<ClientFile> getClientFiles() {
        return this.clientFiles;
    }
}

