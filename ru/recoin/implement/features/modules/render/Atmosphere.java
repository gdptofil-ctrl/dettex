/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_243
 *  net.minecraft.class_4184
 *  net.minecraft.class_638
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
 */
package ru.recoin.implement.features.modules.render;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.class_243;
import net.minecraft.class_4184;
import net.minecraft.class_638;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.ColorSetting;
import ru.recoin.api.feature.module.setting.implement.SelectSetting;
import ru.recoin.api.feature.module.setting.implement.ValueSetting;
import ru.recoin.common.util.other.Instance;
import ru.recoin.common.util.render.pipeline.AtmospherePipeline;
import ru.recoin.implement.events.player.TickEvent;
import ru.recoin.implement.events.render.WorldRenderEvent;

public final class Atmosphere
extends Module {
    public final SelectSetting mode = new SelectSetting("\u0420\u0435\u0436\u0438\u043c", "\u0412\u044b\u0431\u0438\u0440\u0430\u0435\u0442 \u043e\u0441\u0432\u0435\u0449\u0435\u043d\u0438\u0435 \u0438 \u0447\u0430\u0441\u0442\u0438\u0446\u044b \u0430\u0442\u043c\u043e\u0441\u0444\u0435\u0440\u044b").value("Dawn", "Day", "Dusk", "Night", "Dust", "Snow", "Ash").selected("Snow");
    public final ValueSetting density = Atmosphere.number("\u041f\u043b\u043e\u0442\u043d\u043e\u0441\u0442\u044c", "\u041f\u043b\u043e\u0442\u043d\u043e\u0441\u0442\u044c \u0447\u0430\u0441\u0442\u0438\u0446 \u0438 \u0430\u0442\u043c\u043e\u0441\u0444\u0435\u0440\u044b", 1.0f, 3.0f);
    public final ValueSetting scatterHeight = Atmosphere.number("\u0412\u044b\u0441\u043e\u0442\u0430 \u0440\u0430\u0441\u0441\u0435\u0438\u0432\u0430\u043d\u0438\u044f", "\u0412\u044b\u0441\u043e\u0442\u0430 \u0440\u0430\u0441\u0441\u0435\u0438\u0432\u0430\u043d\u0438\u044f \u0441\u0432\u0435\u0442\u0430 \u0432 \u0430\u0442\u043c\u043e\u0441\u0444\u0435\u0440\u0435", 64.0f, 320.0f);
    public final ValueSetting godRays = Atmosphere.number("\u0421\u043e\u043b\u043d\u0435\u0447\u043d\u044b\u0435 \u043b\u0443\u0447\u0438", "\u0418\u043d\u0442\u0435\u043d\u0441\u0438\u0432\u043d\u043e\u0441\u0442\u044c \u0441\u043e\u043b\u043d\u0435\u0447\u043d\u044b\u0445 \u043b\u0443\u0447\u0435\u0439", 1.0f, 3.0f);
    public final ValueSetting softness = Atmosphere.number("\u041c\u044f\u0433\u043a\u043e\u0441\u0442\u044c", "\u041c\u044f\u0433\u043a\u043e\u0441\u0442\u044c \u0434\u044b\u043c\u043a\u0438 \u0438 \u0441\u0432\u0435\u0442\u0430", 1.0f, 3.0f);
    public final ColorSetting lightColor = new ColorSetting("\u0426\u0432\u0435\u0442 \u0441\u0432\u0435\u0442\u0430", "\u041e\u0442\u0442\u0435\u043d\u043e\u043a \u0441\u0435\u0437\u043e\u043d\u043d\u043e\u0433\u043e \u043e\u0441\u0432\u0435\u0449\u0435\u043d\u0438\u044f \u0438 \u0447\u0430\u0441\u0442\u0438\u0446").setColor(-1);
    public final ValueSetting amount = Atmosphere.number("\u0421\u0438\u043b\u0430 \u044d\u0444\u0444\u0435\u043a\u0442\u0430", "\u0418\u043d\u0442\u0435\u043d\u0441\u0438\u0432\u043d\u043e\u0441\u0442\u044c \u0441\u0435\u0437\u043e\u043d\u043d\u044b\u0445 \u044d\u0444\u0444\u0435\u043a\u0442\u043e\u0432", 1.0f, 3.0f);
    public final ValueSetting speed = Atmosphere.number("\u0421\u043a\u043e\u0440\u043e\u0441\u0442\u044c", "\u0421\u043a\u043e\u0440\u043e\u0441\u0442\u044c \u0434\u0432\u0438\u0436\u0435\u043d\u0438\u044f \u0447\u0430\u0441\u0442\u0438\u0446 \u0438 \u0430\u043d\u0438\u043c\u0430\u0446\u0438\u0438 \u0430\u0442\u043c\u043e\u0441\u0444\u0435\u0440\u044b", 1.0f, 5.0f);
    public final ValueSetting haze = Atmosphere.number("\u0414\u044b\u043c\u043a\u0430", "\u0418\u043d\u0442\u0435\u043d\u0441\u0438\u0432\u043d\u043e\u0441\u0442\u044c \u0434\u044b\u043c\u043a\u0438 \u0432\u0434\u0430\u043b\u0438", 1.0f, 3.0f);
    public final ValueSetting grade = Atmosphere.number("\u0426\u0432\u0435\u0442\u043e\u043a\u043e\u0440\u0440\u0435\u043a\u0446\u0438\u044f", "\u0418\u043d\u0442\u0435\u043d\u0441\u0438\u0432\u043d\u043e\u0441\u0442\u044c \u0441\u0435\u0437\u043e\u043d\u043d\u043e\u0439 \u0446\u0432\u0435\u0442\u043e\u043a\u043e\u0440\u0440\u0435\u043a\u0446\u0438\u0438", 1.0f, 3.0f);
    public final ValueSetting vignette = Atmosphere.number("\u0412\u0438\u043d\u044c\u0435\u0442\u043a\u0430", "\u0417\u0430\u0442\u0435\u043c\u043d\u0435\u043d\u0438\u0435 \u043a\u0440\u0430\u0451\u0432 \u044d\u043a\u0440\u0430\u043d\u0430", 1.0f, 3.0f);
    private static final AtmospherePipeline PIPELINE = new AtmospherePipeline();
    private final List<Particle> particles = new ArrayList<Particle>();
    private final ArrayDeque<Particle> recycledParticles = new ArrayDeque();
    private final Matrix4f projectionMatrix = new Matrix4f();
    private final Matrix4f inverseProjection = new Matrix4f();
    private final Matrix4f cameraRotation = new Matrix4f();
    private final Matrix4f particleTransform = new Matrix4f();
    private final Quaternionf particleRotation = new Quaternionf();
    private final Vector3f cameraPosition = new Vector3f();
    private boolean projectionValid;
    private final Random random = new Random();
    private class_638 particleWorld;
    private String particleMode;
    private long lastFrame;
    private float elapsed;

    public Atmosphere() {
        super("Atmosphere", "Atmosphere", ModuleCategory.RENDER);
        this.setup(this.mode, this.density, this.scatterHeight, this.godRays, this.softness, this.lightColor, this.amount, this.speed, this.haze, this.grade, this.vignette);
    }

    private static ValueSetting number(String name, String description, float value, float maximum) {
        return new ValueSetting(name, description).setValue(value).range(0.0f, maximum).step(0.05f);
    }

    public static Atmosphere getInstance() {
        return Instance.get(Atmosphere.class);
    }

    public int preset() {
        return switch (this.mode.getSelected()) {
            case "Dawn" -> 0;
            case "Day" -> 1;
            case "Dusk" -> 2;
            case "Night" -> 3;
            case "Dust" -> 6;
            case "Ash" -> 8;
            default -> 5;
        };
    }

    public long timeOfDay() {
        return switch (this.mode.getSelected()) {
            case "Dawn" -> 23000L;
            case "Dusk" -> 12000L;
            case "Night" -> 18000L;
            default -> 6000L;
        };
    }

    private int atmosphereColor() {
        int color = switch (this.preset()) {
            case 0 -> -12315;
            case 2 -> -1531300;
            case 3 -> -6508837;
            case 6 -> -2702684;
            case 8 -> -4604218;
            default -> -2759952;
        };
        int tint = this.lightColor.getColor();
        int red = (color >>> 16 & 0xFF) * (tint >>> 16 & 0xFF) / 255;
        int green = (color >>> 8 & 0xFF) * (tint >>> 8 & 0xFF) / 255;
        int blue = (color & 0xFF) * (tint & 0xFF) / 255;
        return 0xFF000000 | red << 16 | green << 8 | blue;
    }

    @Override
    public void activate() {
        this.particles.clear();
        this.recycledParticles.clear();
        this.projectionValid = false;
        this.lastFrame = 0L;
        this.elapsed = 0.0f;
    }

    @Override
    public void deactivate() {
        this.particles.clear();
        this.recycledParticles.clear();
        this.projectionValid = false;
        this.particleWorld = null;
        this.lastFrame = 0L;
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (Atmosphere.mc.field_1687 != this.particleWorld || !this.mode.getSelected().equals(this.particleMode)) {
            this.recycledParticles.addAll(this.particles);
            this.particles.clear();
            this.particleWorld = Atmosphere.mc.field_1687;
            this.particleMode = this.mode.getSelected();
            this.lastFrame = 0L;
        }
    }

    public static void render(Matrix4f projection) {
        Atmosphere module = Atmosphere.getInstance();
        if (module == null || !module.isState() || projection == null || Atmosphere.mc.field_1687 == null || Atmosphere.mc.field_1773.method_35765()) {
            return;
        }
        class_4184 camera = Atmosphere.mc.field_1773.method_19418();
        if (camera == null) {
            return;
        }
        if (!module.projectionValid || !module.projectionMatrix.equals((Object)projection)) {
            module.projectionMatrix.set((Matrix4fc)projection);
            module.inverseProjection.set((Matrix4fc)projection).invert();
            module.projectionValid = true;
        }
        class_243 position = camera.method_71156();
        module.cameraRotation.rotation((Quaternionfc)camera.method_23767());
        module.cameraPosition.set((float)position.field_1352, (float)position.field_1351, (float)position.field_1350);
        PIPELINE.draw(projection, module.inverseProjection, module.cameraRotation, module.cameraPosition, module.elapsed, module.preset(), module.density.getValue(), module.scatterHeight.getValue(), module.godRays.getValue(), module.softness.getValue(), module.amount.getValue(), module.speed.getValue(), module.haze.getValue(), module.grade.getValue(), module.vignette.getValue(), module.atmosphereColor());
    }

    /*
     * Exception decompiling
     */
    @EventHandler
    public void onWorldRender(WorldRenderEvent event) {
        /*
         * This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
         * 
         * java.lang.ClassCastException: class org.benf.cfr.reader.bytecode.analysis.opgraph.Op03SimpleStatement cannot be cast to class org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement (org.benf.cfr.reader.bytecode.analysis.opgraph.Op03SimpleStatement and org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement are in unnamed module of loader 'app')
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.SwitchExpressionRewriter$LValueSingleUsageCheckingRewriter.rewriteExpression(SwitchExpressionRewriter.java:96)
         *     at org.benf.cfr.reader.bytecode.analysis.parse.expression.LValueExpression.applyExpressionRewriter(LValueExpression.java:84)
         *     at org.benf.cfr.reader.bytecode.analysis.parse.rewriters.AbstractExpressionRewriter.rewriteExpression(AbstractExpressionRewriter.java:14)
         *     at org.benf.cfr.reader.bytecode.analysis.parse.expression.AbstractMemberFunctionInvokation.applyExpressionRewriter(AbstractMemberFunctionInvokation.java:87)
         *     at org.benf.cfr.reader.bytecode.analysis.parse.rewriters.AbstractExpressionRewriter.rewriteExpression(AbstractExpressionRewriter.java:14)
         *     at org.benf.cfr.reader.bytecode.analysis.parse.expression.ArithmeticOperation.applyExpressionRewriter(ArithmeticOperation.java:172)
         *     at org.benf.cfr.reader.bytecode.analysis.parse.rewriters.AbstractExpressionRewriter.rewriteExpression(AbstractExpressionRewriter.java:14)
         *     at org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriterHelper.applyForwards(ExpressionRewriterHelper.java:12)
         *     at org.benf.cfr.reader.bytecode.analysis.parse.expression.StaticFunctionInvokation.applyExpressionRewriterToArgs(StaticFunctionInvokation.java:103)
         *     at org.benf.cfr.reader.bytecode.analysis.parse.expression.StaticFunctionInvokation.applyExpressionRewriter(StaticFunctionInvokation.java:90)
         *     at org.benf.cfr.reader.bytecode.analysis.parse.rewriters.AbstractExpressionRewriter.rewriteExpression(AbstractExpressionRewriter.java:14)
         *     at org.benf.cfr.reader.bytecode.analysis.parse.statement.AssignmentSimple.rewriteExpressions(AssignmentSimple.java:167)
         *     at org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredFor.rewriteExpressions(StructuredFor.java:194)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.transformers.ExpressionRewriterTransformer.transform(ExpressionRewriterTransformer.java:24)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.transform(Op04StructuredStatement.java:680)
         *     at org.benf.cfr.reader.bytecode.analysis.structured.statement.Block.transformStructuredChildren(Block.java:421)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.transformers.ExpressionRewriterTransformer.transform(ExpressionRewriterTransformer.java:25)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.transform(Op04StructuredStatement.java:680)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.SwitchExpressionRewriter.rewriteBlockSwitches(SwitchExpressionRewriter.java:140)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.SwitchExpressionRewriter.transform(SwitchExpressionRewriter.java:71)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.switchExpression(Op04StructuredStatement.java:101)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:909)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisOrWrapFail(CodeAnalyser.java:278)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysis(CodeAnalyser.java:201)
         *     at org.benf.cfr.reader.entities.attributes.AttributeCode.analyse(AttributeCode.java:94)
         *     at org.benf.cfr.reader.entities.Method.analyse(Method.java:531)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1055)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseTop(ClassFile.java:942)
         *     at org.benf.cfr.reader.Driver.doJarVersionTypes(Driver.java:257)
         *     at org.benf.cfr.reader.Driver.doJar(Driver.java:139)
         *     at org.benf.cfr.reader.CfrDriverImpl.analyse(CfrDriverImpl.java:76)
         *     at org.benf.cfr.reader.Main.main(Main.java:54)
         */
        throw new IllegalStateException("Decompilation failed");
    }

    private static final class Particle {
        double x;
        double y;
        double z;
        float phase;
        float size;

        private Particle() {
        }
    }
}

