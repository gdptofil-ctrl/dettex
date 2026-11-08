package ru.recoin.implement.features.modules.misc.browser;

import net.minecraft.class_11905;
import net.minecraft.class_11908;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_437;

/**
 * Keyboard into a screen in the world: everything you type goes to the page / program you were
 * looking at. Nothing is drawn over the world except a small hint. Esc returns to the game.
 */
public final class TypeScreen extends class_437 {
    private final Panel panel;
    private final boolean auto;   // opened by clicking a text field: closes when the field is left

    public TypeScreen(Panel panel, boolean auto) {
        super(class_2561.method_43470("Typing"));
        this.panel = panel;
        this.auto = auto;
    }

    @Override
    public void method_25393() {
        super.method_25393();
        // the page left the text field (Enter in a search, a click elsewhere on the page...): back to the game
        if (this.auto && this.panel instanceof BrowserPanel b && !b.editing() && b.editingStableFor() > 400L) {
            this.method_25419();
        }
    }

    @Override
    public void method_25394(class_332 c, int mouseX, int mouseY, float delta) {
        String hint = "⌨ Клавиатура в «" + this.panel.title() + "». Esc - отменить.";
        int w = this.field_22793.method_1727(hint) + 12;
        int x = (this.field_22789 - w) / 2;
        int y = this.field_22790 - 48;
        c.method_25294(x, y, x + w, y + 16, 0xB0101018);
        c.method_51433(this.field_22793, hint, x + 6, y + 4, 0xFFB0E0FF, true);
    }

    @Override
    public void method_25420(class_332 c, int mouseX, int mouseY, float delta) {
        // no blur / dark background: you keep seeing the screen you type into
    }

    @Override
    public boolean method_25404(class_11908 key) {
        if (key.comp_4795() == 256) {
            if (this.panel instanceof BrowserPanel b) b.blur();   // leave the field so it does not grab the keyboard again
            this.method_25419();
            return true;
        }
        this.panel.keyPress(key.comp_4795(), key.comp_4796(), key.comp_4797());
        return true;
    }

    @Override
    public boolean method_16803(class_11908 key) {
        this.panel.keyRelease(key.comp_4795(), key.comp_4796(), key.comp_4797());
        return true;
    }

    @Override
    public boolean method_25400(class_11905 ch) {
        int cp = ch.comp_4793();
        if (cp != 0) {
            for (char c : Character.toChars(cp)) this.panel.charTyped(c, ch.comp_4794());
        }
        return true;
    }

    @Override
    public boolean method_25421() {
        return false;
    }
}
