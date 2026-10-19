/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1268
 *  net.minecraft.class_1306
 *  net.minecraft.class_3532
 *  net.minecraft.class_4587
 *  net.minecraft.class_7833
 *  org.joml.Quaternionfc
 */
package ru.recoin.implement.features.modules.render;

import net.minecraft.class_1268;
import net.minecraft.class_1306;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import net.minecraft.class_7833;
import org.joml.Quaternionfc;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.SelectSetting;
import ru.recoin.api.feature.module.setting.implement.ValueSetting;
import ru.recoin.common.util.other.Instance;
import ru.recoin.implement.events.item.HandAnimationEvent;
import ru.recoin.implement.events.item.SwingDurationEvent;

public class SwingAnimations
extends Module {
    private final SelectSetting swingType = new SelectSetting("Swing Type", "Select the type of swing").value("Swipe", "Down", "Smooth", "Power", "Feast");
    private final ValueSetting swingSpeed = new ValueSetting("Swing Duration", "Duration of the hit animation").setValue(1.0f).range(0.5f, 2.0f);

    public SwingAnimations() {
        super("SwingAnimations", "Swing Animations", ModuleCategory.RENDER);
        this.setup(this.swingType, this.swingSpeed);
    }

    public static SwingAnimations getInstance() {
        return Instance.get(SwingAnimations.class);
    }

    @EventHandler
    public void onSwingDuration(SwingDurationEvent event) {
        if (!this.isState()) {
            return;
        }
        event.setAnimation(this.swingSpeed.getValue());
        event.cancel();
    }

    @EventHandler
    public void onHandAnimation(HandAnimationEvent event) {
        if (!this.isState() || event.getHand() != class_1268.field_5808 || SwingAnimations.mc.field_1724 == null) {
            return;
        }
        event.getMatrices().method_46416((float)(-event.getArmSign()) * 0.56f, 0.52f + event.getEquipProgress() * 0.6f, 0.72f);
        this.apply(event.getMatrices(), event.getSwingProgress());
        event.cancel();
    }

    private void apply(class_4587 matrix, float progress) {
        int arm = SwingAnimations.mc.field_1724.method_6068() == class_1306.field_6183 ? 1 : -1;
        float sin1 = class_3532.method_15374((double)(progress * progress * (float)Math.PI));
        float sin2 = class_3532.method_15374((double)(class_3532.method_15355((float)progress) * (float)Math.PI));
        float smooth = (float)Math.sin((double)progress * Math.PI) * 0.5f;
        switch (this.swingType.getSelected()) {
            case "Swipe": {
                matrix.method_46416(0.56f * (float)arm, -0.32f, -0.72f);
                matrix.method_22907((Quaternionfc)class_7833.field_40716.rotationDegrees((float)(60 * arm)));
                matrix.method_22907((Quaternionfc)class_7833.field_40718.rotationDegrees((float)(-60 * arm)));
                matrix.method_22907((Quaternionfc)class_7833.field_40716.rotationDegrees(sin2 * sin1 * -5.0f));
                matrix.method_22907((Quaternionfc)class_7833.field_40714.rotationDegrees(sin2 * sin1 * -120.0f));
                matrix.method_22907((Quaternionfc)class_7833.field_40714.rotationDegrees(-60.0f));
                break;
            }
            case "Down": {
                matrix.method_46416((float)arm * 0.56f, -0.32f, -0.72f);
                matrix.method_22907((Quaternionfc)class_7833.field_40716.rotationDegrees((float)(76 * arm)));
                matrix.method_22907((Quaternionfc)class_7833.field_40716.rotationDegrees(sin2 * -5.0f));
                matrix.method_22907((Quaternionfc)class_7833.field_40713.rotationDegrees(sin2 * -100.0f));
                matrix.method_22907((Quaternionfc)class_7833.field_40714.rotationDegrees(sin2 * -155.0f));
                matrix.method_22907((Quaternionfc)class_7833.field_40714.rotationDegrees(-100.0f));
                break;
            }
            case "Smooth": {
                matrix.method_46416((float)arm * 0.56f, -0.42f, -0.72f);
                matrix.method_22907((Quaternionfc)class_7833.field_40716.rotationDegrees((float)arm * (45.0f + sin1 * -20.0f)));
                matrix.method_22907((Quaternionfc)class_7833.field_40718.rotationDegrees((float)arm * sin2 * -20.0f));
                matrix.method_22907((Quaternionfc)class_7833.field_40714.rotationDegrees(sin2 * -80.0f));
                matrix.method_22907((Quaternionfc)class_7833.field_40716.rotationDegrees((float)arm * -45.0f));
                matrix.method_22904(0.0, -0.1, 0.0);
                break;
            }
            case "Power": {
                matrix.method_46416((float)arm * 0.56f, -0.32f, -0.72f);
                matrix.method_46416(-smooth * smooth * sin1 * (float)arm, 0.0f, 0.0f);
                matrix.method_22907((Quaternionfc)class_7833.field_40716.rotationDegrees((float)(61 * arm)));
                matrix.method_22907((Quaternionfc)class_7833.field_40718.rotationDegrees(sin2));
                matrix.method_22907((Quaternionfc)class_7833.field_40716.rotationDegrees(sin2 * sin1 * -5.0f));
                matrix.method_22907((Quaternionfc)class_7833.field_40714.rotationDegrees(sin2 * sin1 * -30.0f));
                matrix.method_22907((Quaternionfc)class_7833.field_40714.rotationDegrees(-60.0f));
                matrix.method_22907((Quaternionfc)class_7833.field_40714.rotationDegrees(smooth * -60.0f));
                break;
            }
            case "Feast": {
                matrix.method_46416((float)arm * 0.56f, -0.32f, -0.72f);
                matrix.method_22907((Quaternionfc)class_7833.field_40716.rotationDegrees((float)(30 * arm)));
                matrix.method_22907((Quaternionfc)class_7833.field_40716.rotationDegrees(sin2 * 75.0f * (float)arm));
                matrix.method_22907((Quaternionfc)class_7833.field_40714.rotationDegrees(sin2 * -45.0f));
                matrix.method_22907((Quaternionfc)class_7833.field_40716.rotationDegrees((float)(30 * arm)));
                matrix.method_22907((Quaternionfc)class_7833.field_40714.rotationDegrees(-80.0f));
                matrix.method_22907((Quaternionfc)class_7833.field_40716.rotationDegrees((float)(35 * arm)));
            }
        }
    }
}

