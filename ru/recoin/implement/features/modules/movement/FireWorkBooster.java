/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_243
 *  net.minecraft.class_3532
 *  net.minecraft.class_4587
 */
package ru.recoin.implement.features.modules.movement;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.BooleanSetting;
import ru.recoin.api.feature.module.setting.implement.SelectSetting;
import ru.recoin.api.feature.module.setting.implement.ValueSetting;
import ru.recoin.api.system.font.FontRenderer;
import ru.recoin.api.system.font.Fonts;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.render.MatrixStackCompat;
import ru.recoin.implement.events.player.FireworkEvent;
import ru.recoin.implement.events.render.DrawEvent;
import ru.recoin.implement.features.modules.combat.killaura.rotation.RotationController;
import ru.recoin.implement.features.modules.movement.fireworkbooster.BoosterModeSetting;
import ru.recoin.implement.features.modules.movement.fireworkbooster.BoosterSectionHeaderSetting;
import ru.recoin.implement.features.modules.movement.fireworkbooster.FireworkBoostCoefficients;

public class FireWorkBooster
extends Module {
    public final SelectSetting mode = new BoosterModeSetting("\u041c\u043e\u0434").value("ReallyWorld 1.16.5", "ReallyWorld 1.21.11", "BravoHvH", "Custom");
    public final BooleanSetting drawAngles = new BooleanSetting("\u0420\u0438\u0441\u043e\u0432\u0430\u0442\u044c \u0442\u0435\u043a\u0443\u0449\u0438\u0439 Yaw \u0438\u043b\u0438 Pitch", "").setValue(false);
    public final BoosterSectionHeaderSetting horizontalHeader = new BoosterSectionHeaderSetting("\u0411\u0443\u0441\u0442\u0435\u0440 \u043f\u043e X", () -> this.mode.isSelected("Custom"));
    public final ValueSetting horizontal0To5 = new ValueSetting("0-5 \u041f\u043e X", "").setValue(1.5f).range(1.5f, 2.5f).step(0.01f).visible(() -> this.mode.isSelected("Custom"));
    public final ValueSetting horizontal5To10 = new ValueSetting("5-10 \u041f\u043e X", "").setValue(1.5f).range(1.5f, 2.5f).step(0.01f).visible(() -> this.mode.isSelected("Custom"));
    public final ValueSetting horizontal10To15 = new ValueSetting("10-15 \u041f\u043e X", "").setValue(1.5f).range(1.5f, 2.5f).step(0.01f).visible(() -> this.mode.isSelected("Custom"));
    public final ValueSetting horizontal15To20 = new ValueSetting("15-20 \u041f\u043e X", "").setValue(1.5f).range(1.5f, 2.5f).step(0.01f).visible(() -> this.mode.isSelected("Custom"));
    public final ValueSetting horizontal20To25 = new ValueSetting("20-25 \u041f\u043e X", "").setValue(1.5f).range(1.5f, 2.5f).step(0.01f).visible(() -> this.mode.isSelected("Custom"));
    public final ValueSetting horizontal25To30 = new ValueSetting("25-30 \u041f\u043e X", "").setValue(1.5f).range(1.5f, 2.5f).step(0.01f).visible(() -> this.mode.isSelected("Custom"));
    public final ValueSetting horizontal30To35 = new ValueSetting("30-35 \u041f\u043e X", "").setValue(1.5f).range(1.5f, 2.5f).step(0.01f).visible(() -> this.mode.isSelected("Custom"));
    public final ValueSetting horizontal35To40 = new ValueSetting("35-40 \u041f\u043e X", "").setValue(1.5f).range(1.5f, 2.5f).step(0.01f).visible(() -> this.mode.isSelected("Custom"));
    public final ValueSetting horizontal40To45 = new ValueSetting("40-45 \u041f\u043e X", "").setValue(1.5f).range(1.5f, 2.5f).step(0.01f).visible(() -> this.mode.isSelected("Custom"));
    public final BoosterSectionHeaderSetting verticalHeader = new BoosterSectionHeaderSetting("\u0411\u0443\u0441\u0442\u0435\u0440 \u043f\u043e Y", () -> this.mode.isSelected("Custom"));
    public final ValueSetting vertical0To5 = new ValueSetting("0-5 \u041f\u043e Y", "").setValue(1.5f).range(1.5f, 2.5f).step(0.01f).visible(() -> this.mode.isSelected("Custom"));
    public final ValueSetting vertical5To10 = new ValueSetting("5-10 \u041f\u043e Y", "").setValue(1.5f).range(1.5f, 2.5f).step(0.01f).visible(() -> this.mode.isSelected("Custom"));
    public final ValueSetting vertical10To15 = new ValueSetting("10-15 \u041f\u043e Y", "").setValue(1.5f).range(1.5f, 2.5f).step(0.01f).visible(() -> this.mode.isSelected("Custom"));
    public final ValueSetting vertical15To20 = new ValueSetting("15-20 \u041f\u043e Y", "").setValue(1.5f).range(1.5f, 2.5f).step(0.01f).visible(() -> this.mode.isSelected("Custom"));
    public final ValueSetting vertical20To25 = new ValueSetting("20-25 \u041f\u043e Y", "").setValue(1.5f).range(1.5f, 2.5f).step(0.01f).visible(() -> this.mode.isSelected("Custom"));
    public final ValueSetting vertical25To30 = new ValueSetting("25-30 \u041f\u043e Y", "").setValue(1.5f).range(1.5f, 2.5f).step(0.01f).visible(() -> this.mode.isSelected("Custom"));
    public final ValueSetting vertical30To35 = new ValueSetting("30-35 \u041f\u043e Y", "").setValue(1.5f).range(1.5f, 2.5f).step(0.01f).visible(() -> this.mode.isSelected("Custom"));
    public final ValueSetting vertical35To40 = new ValueSetting("35-40 \u041f\u043e Y", "").setValue(1.5f).range(1.5f, 2.5f).step(0.01f).visible(() -> this.mode.isSelected("Custom"));
    public final ValueSetting vertical40To45 = new ValueSetting("40-45 \u041f\u043e Y", "").setValue(1.5f).range(1.5f, 2.5f).step(0.01f).visible(() -> this.mode.isSelected("Custom"));
    private final transient AnimatedNumber animatedYaw = new AnimatedNumber();
    private final transient AnimatedNumber animatedPitch = new AnimatedNumber();

    public FireWorkBooster() {
        super("FireWorkBooster", "FireWork Booster", ModuleCategory.MOVEMENT);
        this.setup(this.mode, this.horizontalHeader, this.horizontal0To5, this.horizontal5To10, this.horizontal10To15, this.horizontal15To20, this.horizontal20To25, this.horizontal25To30, this.horizontal30To35, this.horizontal35To40, this.horizontal40To45, this.verticalHeader, this.vertical0To5, this.vertical5To10, this.vertical10To15, this.vertical15To20, this.vertical20To25, this.vertical25To30, this.vertical30To35, this.vertical35To40, this.vertical40To45, this.drawAngles);
    }

    private float textWidth(String text) {
        return FireWorkBooster.font().getStringWidth(text);
    }

    public float getHorizontalBoost() {
        FireworkBoostCoefficients boost = new FireworkBoostCoefficients(1.5f, 1.5f, 1.5f);
        this.calculateBoost(boost);
        return boost.getXBoostMultiplier();
    }

    @EventHandler
    public void onDraw(DrawEvent event) {
        if (this.drawAngles.isValue() && FireWorkBooster.mc.field_1724 != null && FireWorkBooster.mc.field_1687 != null) {
            int yawDegrees = (int)this.foldAngle(FireWorkBooster.mc.field_1724.method_36454());
            int pitchDegrees = (int)this.foldAngle(FireWorkBooster.mc.field_1724.method_36455());
            this.animatedYaw.setNumber(yawDegrees);
            this.animatedPitch.setNumber(pitchDegrees);
            float centerX = (float)mc.method_22683().method_4486() / 2.0f;
            float centerY = (float)mc.method_22683().method_4502() / 2.0f;
            class_4587 matrix = MatrixStackCompat.from(event.getDrawContext().method_51448());
            this.update(matrix, "Yaw: ", this.animatedYaw, centerX, centerY + 40.0f);
            this.update(matrix, "Pitch: ", this.animatedPitch, centerX, centerY + 70.0f);
        }
    }

    private float foldAngle(float angle) {
        float foldedAngle = angle % 180.0f;
        if (foldedAngle > 90.0f) {
            foldedAngle -= 180.0f;
        } else if (foldedAngle < -90.0f) {
            foldedAngle += 180.0f;
        }
        return foldedAngle;
    }

    private void update(class_4587 matrix, String label, AnimatedNumber number, float centerX, float y) {
        String digitsText = number.getText();
        String fullText = label + digitsText + "\u00b0";
        float leftX = centerX - FireWorkBooster.font().getStringWidth(fullText) * 1.05f / 2.0f;
        int textColor = -1315861;
        int accentColor = ColorUtil.getClientColor();
        FireWorkBooster.font().drawString(matrix, label, leftX, y, textColor);
        float digitsX = leftX + this.textWidth(label);
        long now = System.nanoTime();
        for (int digitIndex = 0; digitIndex < number.digits.size(); ++digitIndex) {
            AnimatedDigit digit = number.digits.get(digitIndex);
            float digitX = digitsX + this.textWidth(digitsText.substring(0, digitIndex));
            float progress = digit.progress(now);
            float easedProgress = 1.0f - (float)Math.pow(1.0f - progress, 3.0);
            if (progress < 1.0f) {
                FireWorkBooster.font().drawString(matrix, digit.previous, digitX, y + easedProgress * 6.0f, FireWorkBooster.withAlpha(textColor, 1.0f - easedProgress));
                float accentPulse = (float)Math.sin((double)progress * Math.PI);
                int digitColor = FireWorkBooster.blendArgb(textColor, accentColor, accentPulse * 0.28f);
                FireWorkBooster.font().drawString(matrix, digit.current, digitX, y - 6.0f + easedProgress * 6.0f, FireWorkBooster.withAlpha(digitColor, easedProgress));
                continue;
            }
            FireWorkBooster.font().drawString(matrix, digit.current, digitX, y, textColor);
        }
        float suffixX = digitsX + this.textWidth(digitsText);
        FireWorkBooster.font().drawString(matrix, "\u00b0", suffixX, y, textColor);
    }

    public void calculateBoost(FireworkBoostCoefficients boost) {
        float foldedPitch;
        float pitchDeviation;
        if (FireWorkBooster.mc.field_1724 == null) {
            return;
        }
        float yaw = class_3532.method_15393((float)FireWorkBooster.mc.field_1724.method_36454());
        float pitch = FireWorkBooster.mc.field_1724.method_36455();
        float foldedYaw = this.foldAngle(yaw);
        float yawDeviation = Math.abs(foldedYaw);
        float horizontalMultiplier = 1.5f;
        float verticalMultiplier = 1.5f;
        if (yawDeviation > 45.0f) {
            yawDeviation = 90.0f - yawDeviation;
        }
        if ((pitchDeviation = Math.abs(foldedPitch = this.foldAngle(pitch))) > 45.0f) {
            pitchDeviation = 90.0f - pitchDeviation;
        }
        if (this.mode.isSelected("ReallyWorld 1.16.5")) {
            if (yawDeviation <= 5.0f && pitchDeviation <= 5.0f) {
                horizontalMultiplier = 1.6f;
            } else if (yawDeviation <= 10.0f && pitchDeviation <= 10.0f) {
                horizontalMultiplier = 1.63f;
            } else if (yawDeviation <= 15.0f && pitchDeviation <= 15.0f) {
                horizontalMultiplier = 1.66f;
            } else if (yawDeviation <= 20.0f && pitchDeviation <= 20.0f) {
                horizontalMultiplier = 1.71f;
            } else if (yawDeviation <= 25.0f && pitchDeviation <= 25.0f) {
                horizontalMultiplier = 1.73f;
            } else if (yawDeviation <= 30.0f && pitchDeviation <= 30.0f) {
                horizontalMultiplier = 1.81f;
            } else if (yawDeviation <= 35.0f && pitchDeviation <= 35.0f) {
                horizontalMultiplier = 1.81f;
            } else if (yawDeviation <= 40.0f && pitchDeviation <= 40.0f) {
                horizontalMultiplier = 1.81f;
            } else if (yawDeviation <= 45.0f || pitchDeviation <= 45.0f) {
                horizontalMultiplier = 1.81f;
            }
            verticalMultiplier = pitchDeviation <= 5.0f ? 1.6f : (pitchDeviation <= 10.0f ? 1.62f : (pitchDeviation <= 15.0f ? 1.6f : (pitchDeviation <= 20.0f ? 1.62f : (pitchDeviation <= 25.0f ? 1.64f : (pitchDeviation <= 30.0f ? 1.68f : (pitchDeviation <= 35.0f ? 1.75f : (pitchDeviation <= 40.0f ? 1.9f : (pitchDeviation >= 43.0f && pitchDeviation <= 45.0f ? 2.26f : 2.16f))))))));
            if (verticalMultiplier > horizontalMultiplier) {
                horizontalMultiplier = verticalMultiplier;
            }
        }
        if (this.mode.isSelected("ReallyWorld 1.21.11")) {
            horizontalMultiplier = yawDeviation <= 30.0f && pitchDeviation <= 30.0f ? 1.53f : (yawDeviation <= 40.0f && pitchDeviation <= 40.0f ? 2.04f : 2.059f);
            verticalMultiplier = pitchDeviation <= 30.0f ? 1.53f : 2.02f;
            horizontalMultiplier = Math.max(horizontalMultiplier, verticalMultiplier);
        }
        if (this.mode.isSelected("BravoHvH")) {
            if (yawDeviation <= 5.0f && pitchDeviation <= 5.0f) {
                horizontalMultiplier = 1.64f;
            } else if (yawDeviation <= 10.0f && pitchDeviation <= 10.0f) {
                horizontalMultiplier = 1.67f;
            } else if (yawDeviation <= 15.0f && pitchDeviation <= 15.0f) {
                horizontalMultiplier = 1.68f;
            } else if (yawDeviation <= 20.0f && pitchDeviation <= 20.0f) {
                horizontalMultiplier = 1.73f;
            } else if (yawDeviation <= 25.0f && pitchDeviation <= 25.0f) {
                horizontalMultiplier = 1.77f;
            } else if (yawDeviation <= 30.0f && pitchDeviation <= 30.0f) {
                horizontalMultiplier = 1.87f;
            } else if (yawDeviation <= 35.0f && pitchDeviation <= 35.0f) {
                horizontalMultiplier = 2.01f;
            } else if (yawDeviation <= 40.0f && pitchDeviation <= 40.0f) {
                horizontalMultiplier = 2.05f;
            } else if (yawDeviation <= 45.0f || pitchDeviation <= 45.0f) {
                horizontalMultiplier = 2.05f;
            }
            verticalMultiplier = pitchDeviation <= 5.0f ? 1.65f : (pitchDeviation <= 10.0f ? 1.7f : (pitchDeviation <= 15.0f ? 1.71f : (pitchDeviation <= 20.0f ? 1.74f : (pitchDeviation <= 25.0f ? 1.79f : (pitchDeviation <= 30.0f ? 1.91f : (pitchDeviation <= 35.0f ? 2.01f : (pitchDeviation <= 40.0f ? 2.02f : 2.02f)))))));
        }
        if (this.mode.isSelected("Custom")) {
            if (yawDeviation <= 5.0f && pitchDeviation <= 5.0f) {
                horizontalMultiplier = this.horizontal0To5.getValue();
            } else if (yawDeviation <= 10.0f && pitchDeviation <= 10.0f) {
                horizontalMultiplier = this.horizontal5To10.getValue();
            } else if (yawDeviation <= 15.0f && pitchDeviation <= 15.0f) {
                horizontalMultiplier = this.horizontal10To15.getValue();
            } else if (yawDeviation <= 20.0f && pitchDeviation <= 20.0f) {
                horizontalMultiplier = this.horizontal15To20.getValue();
            } else if (yawDeviation <= 25.0f && pitchDeviation <= 25.0f) {
                horizontalMultiplier = this.horizontal20To25.getValue();
            } else if (yawDeviation <= 30.0f && pitchDeviation <= 30.0f) {
                horizontalMultiplier = this.horizontal25To30.getValue();
            } else if (yawDeviation <= 35.0f && pitchDeviation <= 35.0f) {
                horizontalMultiplier = this.horizontal30To35.getValue();
            } else if (yawDeviation <= 40.0f && pitchDeviation <= 40.0f) {
                horizontalMultiplier = this.horizontal35To40.getValue();
            } else if (yawDeviation <= 45.0f || pitchDeviation <= 45.0f) {
                horizontalMultiplier = this.horizontal40To45.getValue();
            }
            verticalMultiplier = pitchDeviation <= 5.0f ? this.vertical0To5.getValue() : (pitchDeviation <= 10.0f ? this.vertical5To10.getValue() : (pitchDeviation <= 15.0f ? this.vertical10To15.getValue() : (pitchDeviation <= 20.0f ? this.vertical15To20.getValue() : (pitchDeviation <= 25.0f ? this.vertical20To25.getValue() : (pitchDeviation <= 30.0f ? this.vertical25To30.getValue() : (pitchDeviation <= 35.0f ? this.vertical30To35.getValue() : (pitchDeviation <= 40.0f ? this.vertical35To40.getValue() : this.vertical40To45.getValue())))))));
            if (verticalMultiplier > horizontalMultiplier) {
                horizontalMultiplier = verticalMultiplier;
            }
        }
        boost.setXBoostMultiplier(horizontalMultiplier);
        boost.setYBoostMultiplier(verticalMultiplier);
        boost.setZBoostMultiplier(horizontalMultiplier);
    }

    @EventHandler
    public void onFirework(FireworkEvent event) {
        if (FireWorkBooster.mc.field_1724 == null || FireWorkBooster.mc.field_1687 == null) {
            return;
        }
        FireworkBoostCoefficients boost = new FireworkBoostCoefficients(1.5f, 1.5f, 1.5f);
        this.calculateBoost(boost);
        class_243 direction = RotationController.INSTANCE.getMoveRotation().toVector();
        class_243 velocity = event.getVector();
        event.setVector(velocity.method_1031(direction.field_1352 * ((double)boost.getXBoostMultiplier() - 1.5), direction.field_1351 * ((double)boost.getYBoostMultiplier() - 1.5), direction.field_1350 * ((double)boost.getZBoostMultiplier() - 1.5)));
    }

    private static FontRenderer font() {
        return Fonts.getSize(40, Fonts.Type.INTER_MEDIUM);
    }

    private static int withAlpha(int color, float opacity) {
        int alpha = Math.min(255, Math.max(0, (int)(opacity * 255.0f)));
        return color & 0xFFFFFF | alpha << 24;
    }

    private static int blendArgb(int start, int end, float fraction) {
        int color = 0;
        for (int shift = 0; shift <= 24; shift += 8) {
            int a = start >>> shift & 0xFF;
            int b = end >>> shift & 0xFF;
            color |= (int)((float)a + (float)(b - a) * fraction) << shift;
        }
        return color;
    }

    private static final class AnimatedNumber {
        private final List<AnimatedDigit> digits = new ArrayList<AnimatedDigit>();
        private String text = "";
        private Integer pendingValue;

        private AnimatedNumber() {
        }

        private boolean isAnimating() {
            long now = System.nanoTime();
            for (AnimatedDigit digit : this.digits) {
                if (!(digit.progress(now) < 1.0f)) continue;
                return true;
            }
            return false;
        }

        private void update(String nextText) {
            if (nextText.length() != this.digits.size()) {
                this.text = nextText;
                this.initializeDigits(nextText);
            } else {
                long now = System.nanoTime();
                for (int digitIndex = 0; digitIndex < nextText.length(); ++digitIndex) {
                    this.digits.get(digitIndex).setDigit(nextText.charAt(digitIndex), now);
                }
                this.text = nextText;
            }
        }

        private String getText() {
            return this.text;
        }

        private void initializeDigits(String digitsText) {
            this.digits.clear();
            for (int digitIndex = 0; digitIndex < digitsText.length(); ++digitIndex) {
                this.digits.add(new AnimatedDigit(digitsText.charAt(digitIndex)));
            }
        }

        private void setNumber(int value) {
            String nextText = Integer.toString(value);
            if (this.text.isEmpty()) {
                this.text = nextText;
                this.initializeDigits(nextText);
            } else {
                Integer n = this.pendingValue = this.text.equals(nextText) ? null : Integer.valueOf(value);
                if (!this.isAnimating() && this.pendingValue != null) {
                    String pendingText = Integer.toString(this.pendingValue);
                    this.pendingValue = null;
                    this.update(pendingText);
                }
            }
        }
    }

    private static final class AnimatedDigit {
        private String previous;
        private String current;
        private long changedAt;
        private static final float DURATION_NANOS = 1.9E8f;

        private void setDigit(char character, long now) {
            String nextDigit = Character.toString(character);
            if (!this.current.equals(nextDigit)) {
                this.previous = this.current;
                this.current = nextDigit;
                this.changedAt = now;
            }
        }

        private float progress(long now) {
            if (this.changedAt == 0L) {
                return 1.0f;
            }
            float elapsedFraction = (float)(now - this.changedAt) / 1.9E8f;
            return Math.max(0.0f, Math.min(1.0f, elapsedFraction));
        }

        private AnimatedDigit(char character) {
            this.current = this.previous = Character.toString(character);
            this.changedAt = 0L;
        }
    }
}

