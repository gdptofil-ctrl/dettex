package ru.recoin.implement.features.modules.render.esp;

import net.minecraft.class_1309;
import net.minecraft.class_243;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.math.MathUtil;
import ru.recoin.common.util.render.Render3DUtil;

/**
 * New animated Target ESP styles, drawn with glowing (additive) lines:
 * Helix — two strands spiralling up and down around the target;
 * Atom — three tilted rings spinning around the target like an atom;
 * Pulse — rings that rise from the feet and fade, faster on every hit.
 */
public final class AnimatedTargetESP {
    private AnimatedTargetESP() {
    }

    private static double time() {
        return (System.currentTimeMillis() % 1_000_000L) / 1000.0;
    }

    private static int mix(int a, int b, float t) {
        t = Math.max(0.0f, Math.min(1.0f, t));
        int aa = (int)((a >>> 24) + ((b >>> 24) - (a >>> 24)) * t);
        int r = (int)((a >> 16 & 0xFF) + ((b >> 16 & 0xFF) - (a >> 16 & 0xFF)) * t);
        int g = (int)((a >> 8 & 0xFF) + ((b >> 8 & 0xFF) - (a >> 8 & 0xFF)) * t);
        int bl = (int)((a & 0xFF) + ((b & 0xFF) - (a & 0xFF)) * t);
        return aa << 24 | r << 16 | g << 8 | bl;
    }

    private static int alpha(int color, float a) {
        return ColorUtil.multAlpha(color, Math.max(0.0f, Math.min(1.0f, a)));
    }

    /** Two strands spiralling around the target; hits flash them red. */
    public static void helix(class_1309 target, float anim, float hit, float speed) {
        class_243 base = MathUtil.interpolate(target);
        double h = target.method_17682();
        double r = target.method_17681() * 0.85 + 0.15;
        double t = time() * speed;
        int color = mix(ColorUtil.getClientColor(), 0xFFFF3040, hit);
        int segments = 48;
        for (int strand = 0; strand < 2; ++strand) {
            double phase = strand * Math.PI;
            class_243 prev = null;
            for (int i = 0; i <= segments; ++i) {
                double f = i / (double)segments;
                // the strand "breathes": its height scrolls up and down over time
                double y = ((f + t * 0.35) % 1.0) * h;
                double a = f * Math.PI * 4.0 + t * 2.4 + phase;
                double rr = r * (0.9 + 0.1 * Math.sin(t * 3.0 + f * 6.0));
                class_243 p = base.method_1031(Math.cos(a) * rr, y, Math.sin(a) * rr);
                if (prev != null && Math.abs(p.field_1351 - prev.field_1351) < h * 0.5) {
                    float fade = (float)Math.sin(f * Math.PI);
                    Render3DUtil.drawAdditiveLineGradient(prev, p, alpha(color, anim * fade), alpha(color, anim * fade), 2.4f, false);
                }
                prev = p;
            }
        }
    }

    /** Three tilted rings spinning like an atom; a bright "electron" runs along each ring. */
    public static void atom(class_1309 target, float anim, float hit, float speed) {
        class_243 center = MathUtil.interpolate(target).method_1031(0.0, target.method_17682() * 0.55, 0.0);
        double r = Math.max(target.method_17681(), target.method_17682() * 0.45) * 0.95 + 0.2;
        double t = time() * speed;
        int base = mix(ColorUtil.getClientColor(), 0xFFFF3040, hit);
        int segments = 56;
        for (int ring = 0; ring < 3; ++ring) {
            double tilt = Math.toRadians(55.0) ;
            double spin = ring * (Math.PI * 2.0 / 3.0) + t * 0.6;
            double electron = (t * 2.2 + ring * 2.1) % (Math.PI * 2.0);
            class_243 prev = null;
            for (int i = 0; i <= segments; ++i) {
                double a = i / (double)segments * Math.PI * 2.0;
                // circle in the XZ plane, tilted around X, then rotated around Y
                double x = Math.cos(a) * r;
                double z = Math.sin(a) * r;
                double y = z * Math.sin(tilt);
                z = z * Math.cos(tilt);
                double rx = x * Math.cos(spin) - z * Math.sin(spin);
                double rz = x * Math.sin(spin) + z * Math.cos(spin);
                class_243 p = center.method_1031(rx, y, rz);
                if (prev != null) {
                    double d = Math.abs(((a - electron) + Math.PI * 3.0) % (Math.PI * 2.0) - Math.PI);
                    float glow = (float)Math.exp(-d * 3.0);
                    int c = alpha(mix(base, 0xFFFFFFFF, glow * 0.7f), anim * (0.35f + 0.65f * glow));
                    Render3DUtil.drawAdditiveLineGradient(prev, p, c, c, 1.6f + glow * 2.5f, false);
                }
                prev = p;
            }
        }
    }

    /** Rings rising from the feet and fading out; every hit makes the pulse faster and redder. */
    public static void pulse(class_1309 target, float anim, float hit, float speed) {
        class_243 base = MathUtil.interpolate(target);
        double h = target.method_17682();
        double r0 = target.method_17681() * 0.8 + 0.1;
        double t = time() * speed * (1.0 + hit * 1.5);
        int color = mix(ColorUtil.getClientColor(), 0xFFFF3040, hit);
        int rings = 4;
        int segments = 44;
        for (int k = 0; k < rings; ++k) {
            double f = (t * 0.45 + k / (double)rings) % 1.0;
            double y = f * h * 1.05;
            double rr = r0 * (1.0 + 0.35 * Math.sin(f * Math.PI));
            float a = anim * (float)Math.sin(f * Math.PI);
            int c = alpha(color, a);
            class_243 prev = null;
            for (int i = 0; i <= segments; ++i) {
                double ang = i / (double)segments * Math.PI * 2.0 + t * 0.8;
                class_243 p = base.method_1031(Math.cos(ang) * rr, y, Math.sin(ang) * rr);
                if (prev != null) {
                    Render3DUtil.drawAdditiveLineGradient(prev, p, c, c, 2.2f, false);
                }
                prev = p;
            }
        }
    }
}
