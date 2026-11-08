package ru.recoin.implement.features.modules.misc.browser;

import net.minecraft.class_243;
import net.minecraft.class_2960;

/**
 * One screen hanging in the world: a web page or the window of a desktop program. It is placed at
 * anchor, facing back along yaw, width blocks wide; its height follows the picture's aspect.
 */
public abstract class Panel {
    private static int counter;

    public final int id = ++counter;
    public class_243 anchor;
    public float yaw;
    public double width;

    protected Panel(class_243 anchor, float yaw, double width) {
        this.anchor = anchor;
        this.yaw = yaw;
        this.width = width;
    }

    /** Short name for chat and lists. */
    public abstract String title();

    /** The picture to draw, or null while it is not there yet. */
    public abstract class_2960 texture();

    /** Size of the picture in pixels (also the coordinate space for the mouse). */
    public abstract int pixelWidth();

    public abstract int pixelHeight();

    /** Text shown on the dark placeholder while texture() is null. */
    public abstract String status();

    /** Called every frame on the render thread (uploads new frames). */
    public void renderTick() {
    }

    public abstract void mouseMove(int x, int y);

    public abstract void mousePress(int x, int y, int button);

    public abstract void mouseRelease(int x, int y, int button);

    public abstract void wheel(int x, int y, double amount);

    /** GLFW key code, scancode, modifiers. */
    public abstract void keyPress(int key, int scancode, int mods);

    public abstract void keyRelease(int key, int scancode, int mods);

    public abstract void charTyped(char c, int mods);

    public abstract void close();

    /** False when the panel should be removed (e.g. the program was closed). */
    public boolean alive() {
        return true;
    }

    public double halfWidth() {
        return this.width / 2.0;
    }

    public double halfHeight() {
        int w = Math.max(1, this.pixelWidth());
        int h = Math.max(1, this.pixelHeight());
        return this.halfWidth() * h / w;
    }

    /** Unit vector along the panel to the right (as seen by the viewer). */
    public class_243 right() {
        double r = Math.toRadians(this.yaw);
        return new class_243(-Math.cos(r), 0.0, -Math.sin(r));
    }

    public static class_243 up() {
        return new class_243(0.0, 1.0, 0.0);
    }

    /** Normal pointing back at the viewer. */
    public class_243 normal() {
        double r = Math.toRadians(this.yaw);
        return new class_243(Math.sin(r), 0.0, -Math.cos(r));
    }

    /**
     * Where the ray from origin along dir hits this panel: {distance, u, v} with u, v in 0..1 from
     * the top left corner, or null when it misses.
     */
    public double[] hit(class_243 origin, class_243 dir) {
        class_243 n = this.normal();
        double denom = dir.method_1026(n);
        if (Math.abs(denom) < 1.0E-6) return null;
        double t = this.anchor.method_1020(origin).method_1026(n) / denom;
        if (t <= 0.0) return null;
        class_243 p = origin.method_1019(dir.method_1021(t));
        class_243 d = p.method_1020(this.anchor);
        double lx = d.method_1026(this.right());
        double ly = d.method_1026(Panel.up());
        double hw = this.halfWidth();
        double hh = this.halfHeight();
        if (Math.abs(lx) > hw || Math.abs(ly) > hh) return null;
        return new double[]{t, lx / (2 * hw) + 0.5, 0.5 - ly / (2 * hh)};
    }

    /** Point on the panel at u, v (for the 3D cursor). */
    public class_243 point(double u, double v) {
        return this.anchor.method_1019(this.right().method_1021((u - 0.5) * 2 * this.halfWidth())).method_1019(Panel.up().method_1021((0.5 - v) * 2 * this.halfHeight()));
    }
}
