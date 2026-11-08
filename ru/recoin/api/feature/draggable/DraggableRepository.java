/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.api.feature.draggable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ru.recoin.api.feature.draggable.AbstractDraggable;
import ru.recoin.implement.features.draggables.Armor;
import ru.recoin.implement.features.draggables.CoolDowns;
import ru.recoin.implement.features.draggables.HotBar;
import ru.recoin.implement.features.draggables.HotKeys;
import ru.recoin.implement.features.draggables.Inventory;
import ru.recoin.implement.features.draggables.Lyrics;
import ru.recoin.implement.features.draggables.MediaPlayer;
import ru.recoin.implement.features.draggables.Notifications;
import ru.recoin.implement.features.draggables.OpponentCooldownsHud;
import ru.recoin.implement.features.draggables.Potions;
import ru.recoin.implement.features.draggables.ServerHelperBinds;
import ru.recoin.implement.features.draggables.StaffList;
import ru.recoin.implement.features.draggables.TargetHud;
import ru.recoin.implement.features.draggables.Watermark;

public class DraggableRepository {
    private final List<AbstractDraggable> draggable = new ArrayList<AbstractDraggable>();
    private final Map<String, AbstractDraggable> nameMap = new HashMap<String, AbstractDraggable>();
    private final Map<Class<?>, AbstractDraggable> classMap = new HashMap();

    public void setup() {
        this.register(new TargetHud(), new Potions(), new HotKeys(), new Armor(), new Watermark(), new Inventory(), new CoolDowns(), new OpponentCooldownsHud(), new ServerHelperBinds(), new StaffList(), new Notifications(), new MediaPlayer(), new HotBar(), new Lyrics());
    }

    public synchronized void register(AbstractDraggable ... module) {
        for (AbstractDraggable d : module) {
            this.draggable.add(d);
            this.nameMap.put(d.getName().toLowerCase(), d);
            this.classMap.put(d.getClass(), d);
        }
    }

    public synchronized void bringToFront(AbstractDraggable target) {
        if (target != null && this.draggable.remove(target)) {
            this.draggable.add(target);
        }
    }

    public synchronized List<AbstractDraggable> draggable() {
        return this.draggable;
    }

    public synchronized <T extends AbstractDraggable> T get(String name) {
        if (name == null) {
            return null;
        }
        return (T)this.nameMap.get(name.toLowerCase());
    }

    public synchronized <T extends AbstractDraggable> T get(Class<T> clazz) {
        if (clazz == null) {
            return null;
        }
        AbstractDraggable d = this.classMap.get(clazz);
        if (d != null) {
            return (T)d;
        }
        for (AbstractDraggable item : this.draggable) {
            if (!clazz.isAssignableFrom(item.getClass())) continue;
            this.classMap.put(clazz, item);
            return (T)item;
        }
        return null;
    }
}

