/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.api.repository.macro;

import java.util.ArrayList;
import java.util.List;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.event.EventManager;
import ru.recoin.api.repository.macro.Macro;
import ru.recoin.common.QuickImports;
import ru.recoin.common.QuickLogger;
import ru.recoin.implement.events.keyboard.KeyEvent;

public class MacroRepository
implements QuickImports,
QuickLogger {
    public List<Macro> macroList = new ArrayList<Macro>();

    public MacroRepository(EventManager eventManager) {
        eventManager.register(this);
    }

    public void addMacro(String name, String message, int key) {
        this.macroList.add(new Macro(name, message, key));
    }

    public boolean hasMacro(String text) {
        return this.macroList.stream().anyMatch(macro -> macro.name().equalsIgnoreCase(text));
    }

    public void deleteMacro(String text) {
        this.macroList.removeIf(macro -> macro.name().equalsIgnoreCase(text));
    }

    public void clearList() {
        this.macroList.clear();
    }

    @EventHandler
    public void onKey(KeyEvent e) {
        if (MacroRepository.mc.field_1724 != null && e.action() == 0 && MacroRepository.mc.field_1755 == null) {
            this.macroList.stream().filter(macro -> macro.key() == e.key()).findFirst().ifPresent(macro -> MacroRepository.mc.field_1724.field_3944.method_45729(macro.message()));
        }
    }
}

