/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_10042
 *  net.minecraft.class_10691
 *  net.minecraft.class_1297
 *  net.minecraft.class_1309
 *  net.minecraft.class_1542
 *  net.minecraft.class_1665
 *  net.minecraft.class_1667
 *  net.minecraft.class_1676
 *  net.minecraft.class_1680
 *  net.minecraft.class_1681
 *  net.minecraft.class_1683
 *  net.minecraft.class_1684
 *  net.minecraft.class_1685
 *  net.minecraft.class_1686
 *  net.minecraft.class_1753
 *  net.minecraft.class_1764
 *  net.minecraft.class_1771
 *  net.minecraft.class_1776
 *  net.minecraft.class_1779
 *  net.minecraft.class_1792
 *  net.minecraft.class_1799
 *  net.minecraft.class_1802
 *  net.minecraft.class_1823
 *  net.minecraft.class_1828
 *  net.minecraft.class_1835
 *  net.minecraft.class_1937
 *  net.minecraft.class_2246
 *  net.minecraft.class_2338
 *  net.minecraft.class_2350
 *  net.minecraft.class_2374
 *  net.minecraft.class_2382
 *  net.minecraft.class_239
 *  net.minecraft.class_239$class_240
 *  net.minecraft.class_243
 *  net.minecraft.class_2960
 *  net.minecraft.class_332
 *  net.minecraft.class_3486
 *  net.minecraft.class_3532
 *  net.minecraft.class_3857
 *  net.minecraft.class_3959$class_3960
 *  net.minecraft.class_3965
 *  net.minecraft.class_3966
 *  net.minecraft.class_4587
 *  net.minecraft.class_4587$class_4665
 *  net.minecraft.class_897
 *  net.minecraft.class_922
 *  net.minecraft.class_9278
 *  net.minecraft.class_9334
 */
package ru.recoin.implement.features.modules.render;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import net.minecraft.class_10042;
import net.minecraft.class_10691;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1542;
import net.minecraft.class_1665;
import net.minecraft.class_1667;
import net.minecraft.class_1676;
import net.minecraft.class_1680;
import net.minecraft.class_1681;
import net.minecraft.class_1683;
import net.minecraft.class_1684;
import net.minecraft.class_1685;
import net.minecraft.class_1686;
import net.minecraft.class_1753;
import net.minecraft.class_1764;
import net.minecraft.class_1771;
import net.minecraft.class_1776;
import net.minecraft.class_1779;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1823;
import net.minecraft.class_1828;
import net.minecraft.class_1835;
import net.minecraft.class_1937;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2374;
import net.minecraft.class_2382;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_2960;
import net.minecraft.class_332;
import net.minecraft.class_3486;
import net.minecraft.class_3532;
import net.minecraft.class_3857;
import net.minecraft.class_3959;
import net.minecraft.class_3965;
import net.minecraft.class_3966;
import net.minecraft.class_4587;
import net.minecraft.class_897;
import net.minecraft.class_922;
import net.minecraft.class_9278;
import net.minecraft.class_9334;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.system.font.FontRenderer;
import ru.recoin.api.system.font.Fonts;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.entity.EntityCompat;
import ru.recoin.common.util.entity.PlayerIntersectionUtil;
import ru.recoin.common.util.item.PotionTrackerUtil;
import ru.recoin.common.util.math.MathUtil;
import ru.recoin.common.util.math.ProjectionUtil;
import ru.recoin.common.util.other.Instance;
import ru.recoin.common.util.render.MatrixStackCompat;
import ru.recoin.common.util.render.Render2DUtil;
import ru.recoin.common.util.render.Render3DUtil;
import ru.recoin.implement.events.render.DrawEvent;
import ru.recoin.implement.events.render.WorldRenderEvent;
import ru.recoin.implement.features.modules.combat.killaura.rotation.Angle;
import ru.recoin.implement.features.modules.combat.killaura.rotation.RaytracingUtil;
import ru.recoin.implement.features.modules.combat.killaura.rotation.RotationController;
import ru.recoin.implement.features.modules.render.EntityESP;

public class ProjectilePrediction
extends Module {
    public static final double SPLASH_RADIUS = 4.0;
    private final List<Point> points = new ArrayList<Point>();
    private final List<SplashRing> splashRings = new ArrayList<SplashRing>();
    private final List<List<class_243>> smoothedHandPaths = new ArrayList<List<class_243>>();
    private class_1792 predictedHandItem;
    private int handPathIndex;
    private long lastHandFrameNanos;
    private double handSmoothing;
    private boolean currentHandCrossbow;
    private boolean lastPredictionWasCrossbow;

    public static ProjectilePrediction getInstance() {
        return Instance.get(ProjectilePrediction.class);
    }

    public ProjectilePrediction() {
        super("ProjectilePrediction", "Projectile Prediction", ModuleCategory.RENDER);
    }

    @EventHandler
    public void onDraw(DrawEvent e) {
        class_332 context = e.getDrawContext();
        for (Point point : this.points) {
            class_243 vec3d = ProjectionUtil.worldSpaceToScreenSpace(point.pos);
            int ticks = point.ticks;
            if (!ProjectionUtil.canSee(point.pos)) continue;
            FontRenderer font = Fonts.getSize(13, Fonts.Type.INTER_BOLD);
            FontRenderer ownerFont = Fonts.getSize(11, Fonts.Type.INTER_BOLD);
            double time = (double)(ticks * 50) / 1000.0;
            String text = String.format("%.1f", time) + " \u0441\u0435\u043a";
            String owner = point.owner == null ? "" : point.owner.method_5477().getString();
            float padding = 3.0f;
            float mainWidth = 8.0f + padding + font.getStringWidth(text) + padding * 2.0f;
            float ownerWidth = owner.isEmpty() ? 0.0f : 8.0f + padding + ownerFont.getStringWidth(owner) + padding * 2.0f;
            float posX = (float)vec3d.method_10216();
            float posY = (float)(vec3d.method_10214() + 4.0);
            float mainX = posX - mainWidth / 2.0f;
            EntityESP.getInstance().drawTagBackground(MatrixStackCompat.from(context.method_51448()), mainX, posY - padding, mainWidth, 10.0f);
            font.drawString(MatrixStackCompat.from(context.method_51448()), text, mainX + 8.0f + padding * 2.0f, posY + 0.5f, -1);
            Render2DUtil.defaultDrawStack(context, point.stack, mainX + 2.0f, posY - padding, true, false, 0.5f);
            if (owner.isEmpty()) continue;
            float ownerX = posX - ownerWidth / 2.0f;
            float ownerY = posY + 8.0f;
            float ownerTextY = ownerY + 4.0f;
            EntityESP.getInstance().drawTagBackground(MatrixStackCompat.from(context.method_51448()), ownerX, ownerY, ownerWidth, 10.0f);
            this.drawOwnerHead(context, point.owner, ownerX + 2.0f, ownerY + 1.0f);
            ownerFont.drawString(MatrixStackCompat.from(context.method_51448()), owner, ownerX + 8.0f + padding * 2.0f, ownerTextY, -1);
        }
    }

    private void drawOwnerHead(class_332 context, class_1297 owner, float x, float y) {
        if (!(owner instanceof class_1309)) {
            return;
        }
        class_1309 living = (class_1309)owner;
        class_897 baseRenderer = mc.method_1561().method_3953((class_1297)living);
        if (!(baseRenderer instanceof class_922)) {
            return;
        }
        class_922 renderer = (class_922)baseRenderer;
        class_10042 state = (class_10042)renderer.method_62425((class_1297)living, tickCounter.getTickProgress(false));
        class_2960 texture = renderer.method_3885(state);
        Render2DUtil.drawRoundedTexture(context, texture, x, y, 8.0f, 1.5f, 8.0f, 8.0f, 8, 8, 64, 64, -1);
    }

    @EventHandler
    public void onWorldRender(WorldRenderEvent e) {
        if (ProjectilePrediction.mc.field_1724 == null || ProjectilePrediction.mc.field_1687 == null) {
            return;
        }
        this.points.clear();
        this.splashRings.clear();
        this.handPathIndex = 0;
        long frameNanos = System.nanoTime();
        this.handSmoothing = this.lastHandFrameNanos == 0L ? 1.0 : 1.0 - Math.exp((double)(-Math.min(frameNanos - this.lastHandFrameNanos, 250000000L)) / 5.5E7);
        this.lastHandFrameNanos = frameNanos;
        RotationController rotations = RotationController.INSTANCE;
        Angle angle = rotations.getRotation();
        if (rotations.hasActiveRotation()) {
            Angle previous = rotations.getPreviousRotation();
            float delta = e.getPartialTicks();
            angle = new Angle(class_3532.method_17821((float)delta, (float)previous.getYaw(), (float)angle.getYaw()), class_3532.method_16439((float)delta, (float)previous.getPitch(), (float)angle.getPitch()));
        } else {
            float delta = e.getPartialTicks();
            angle = new Angle(class_3532.method_17821((float)delta, (float)ProjectilePrediction.mc.field_1724.field_5982, (float)ProjectilePrediction.mc.field_1724.method_36454()), class_3532.method_16439((float)delta, (float)ProjectilePrediction.mc.field_1724.field_6004, (float)ProjectilePrediction.mc.field_1724.method_36455()));
        }
        this.drawPredictionInHand(e.getStack(), EntityCompat.getHandItems((class_1309)ProjectilePrediction.mc.field_1724), angle);
        this.getProjectiles().forEach(entity -> {
            class_243 motion = entity.method_18798();
            class_243 pos = MathUtil.interpolate(entity);
            for (int i = 0; i < 300; ++i) {
                class_243 prevPos = pos;
                pos = pos.method_1019(motion);
                motion = this.calculateMotion((class_1297)entity, prevPos, motion);
                class_3965 result = RaytracingUtil.raycast(prevPos, pos, class_3959.class_3960.field_17558, entity);
                if (!result.method_17783().equals((Object)class_239.class_240.field_1333)) {
                    pos = result.method_17784();
                }
                Render3DUtil.drawLine(prevPos, pos, ColorUtil.multAlpha(ColorUtil.fade(i), class_3532.method_15363((float)((float)i / 25.0f), (float)0.0f, (float)1.0f)), 2.0f, false);
                if (result.method_17783().equals((Object)class_239.class_240.field_1333) && !(pos.field_1351 < -128.0)) continue;
                if (entity instanceof class_1686) {
                    this.splashRings.add(new SplashRing(pos, this.ticksToSeconds(i)));
                }
                this.BreakingBad((class_1297)entity, pos, i);
                break;
            }
        });
        this.splashRings.forEach(this::renderSplashRadius);
    }

    public void drawPredictionInHand(class_4587 matrix, Iterable<class_1799> stacks, Angle angle) {
        class_1792 activeItem = ProjectilePrediction.mc.field_1724.method_6030().method_7909();
        for (class_1799 stack : stacks) {
            List<class_239> results;
            List<class_239> list;
            boolean crossbowPrediction;
            boolean predicts;
            class_1792 item;
            Object item2 = stack.method_7909();
            class_1792 class_17922 = stack.method_7909();
            Objects.requireNonNull(stack.method_7909());
            if (class_17922 instanceof class_1835) {
                item2 = (class_1835)class_17922;
                predicts = item2.equals(activeItem) && ProjectilePrediction.mc.field_1724.method_6048() >= 10;
            } else if (class_17922 instanceof class_1753) {
                class_1753 item3 = (class_1753)class_17922;
                predicts = item3.equals(activeItem) && ProjectilePrediction.mc.field_1724.method_6115();
            } else if (class_17922 instanceof class_1764) {
                item = (class_1764)class_17922;
                predicts = class_1764.method_7781((class_1799)stack);
            } else if (class_17922 instanceof class_1779) {
                class_1779 ignored = (class_1779)class_17922;
                predicts = true;
            } else if (class_17922 instanceof class_1828) {
                class_1828 ignored = (class_1828)class_17922;
                predicts = true;
            } else if (class_17922 instanceof class_1823) {
                class_1823 ignored = (class_1823)class_17922;
                predicts = true;
            } else if (class_17922 instanceof class_1771) {
                class_1771 ignored = (class_1771)class_17922;
                predicts = true;
            } else if (class_17922 instanceof class_1776) {
                class_1776 ignored = (class_1776)class_17922;
                predicts = true;
            } else {
                predicts = PotionTrackerUtil.isSplashLikePotion(stack);
            }
            if (!predicts) continue;
            if (this.predictedHandItem != stack.method_7909()) {
                this.predictedHandItem = stack.method_7909();
                this.smoothedHandPaths.clear();
            }
            this.currentHandCrossbow = crossbowPrediction = stack.method_7909() instanceof class_1764;
            Objects.requireNonNull(stack.method_7909());
            if (item2 instanceof class_1779) {
                item = (class_1779)item2;
                list = this.renderAndCheckTrajectory((class_1676)new class_1683((class_1937)ProjectilePrediction.mc.field_1687, (class_1309)ProjectilePrediction.mc.field_1724, stack), 0.8, angle);
            } else if (item2 instanceof class_1828) {
                class_1828 item4 = (class_1828)item2;
                list = this.renderAndCheckTrajectory((class_1676)new class_10691((class_1937)ProjectilePrediction.mc.field_1687, (class_1309)ProjectilePrediction.mc.field_1724, stack), 0.55, angle);
            } else if (item2 instanceof class_1835 item5 && item5.equals(activeItem) && ProjectilePrediction.mc.field_1724.method_6048() >= 10) {
                list = this.renderAndCheckTrajectory((class_1676)new class_1685((class_1937)ProjectilePrediction.mc.field_1687, (class_1309)ProjectilePrediction.mc.field_1724, stack), 2.5, angle);
            } else if (item2 instanceof class_1823) {
                class_1823 item6 = (class_1823)item2;
                list = this.renderAndCheckTrajectory((class_1676)new class_1680((class_1937)ProjectilePrediction.mc.field_1687, (class_1309)ProjectilePrediction.mc.field_1724, stack), 1.5, angle);
            } else if (item2 instanceof class_1771) {
                class_1771 item7 = (class_1771)item2;
                list = this.renderAndCheckTrajectory((class_1676)new class_1681((class_1937)ProjectilePrediction.mc.field_1687, (class_1309)ProjectilePrediction.mc.field_1724, stack), 1.5, angle);
            } else if (item2 instanceof class_1776) {
                class_1776 item8 = (class_1776)item2;
                list = this.renderAndCheckTrajectory((class_1676)new class_1684((class_1937)ProjectilePrediction.mc.field_1687, (class_1309)ProjectilePrediction.mc.field_1724, stack), 1.5, angle);
            } else if (item2 instanceof class_1753 item9 && item9.equals(activeItem) && ProjectilePrediction.mc.field_1724.method_6115()) {
                list = this.renderAndCheckTrajectory((class_1676)new class_1667((class_1937)ProjectilePrediction.mc.field_1687, (class_1309)ProjectilePrediction.mc.field_1724, stack, stack), 3.0f * class_3532.method_15363((float)(((float)ProjectilePrediction.mc.field_1724.method_6048() + tickCounter.getTickProgress(false)) / 20.0f), (float)0.0f, (float)1.0f), angle);
            } else if (item2 instanceof class_1764 && class_1764.method_7781((class_1799)stack)) {
                class_1764 item10 = (class_1764)item2;
                class_9278 component = (class_9278)stack.method_58694(class_9334.field_49649);
                ArrayList<class_239> list2 = new ArrayList<class_239>();
                if (component != null && !component.method_57442()) {
                    float velocity = ((class_1799)component.method_57437().get(0)).method_31574(class_1802.field_8639) ? 1.6f : 3.15f;
                    list2.add(this.renderAndCheckTrajectory(angle.toVector(), (class_1676)new class_1667((class_1937)ProjectilePrediction.mc.field_1687, (class_1309)ProjectilePrediction.mc.field_1724, stack, stack), velocity, false));
                    if (component.method_57437().size() > 1) {
                        list2.add(this.renderAndCheckTrajectory(angle.addYaw(-10.0f).toVector(), (class_1676)new class_1667((class_1937)ProjectilePrediction.mc.field_1687, (class_1309)ProjectilePrediction.mc.field_1724, stack, stack), velocity, false));
                        list2.add(this.renderAndCheckTrajectory(angle.addYaw(10.0f).toVector(), (class_1676)new class_1667((class_1937)ProjectilePrediction.mc.field_1687, (class_1309)ProjectilePrediction.mc.field_1724, stack, stack), velocity, false));
                    }
                }
                list = list2;
            } else if (PotionTrackerUtil.isSplashLikePotion(stack)) {
                list = this.renderAndCheckTrajectory((class_1676)new class_10691((class_1937)ProjectilePrediction.mc.field_1687, (class_1309)ProjectilePrediction.mc.field_1724, stack), 0.55, angle);
            } else {
                list = null;
            }
            if ((results = list) == null || (results = results.stream().filter(Objects::nonNull).toList()).isEmpty()) continue;
            this.renderProjectileResults(matrix, results, crossbowPrediction);
            if (PotionTrackerUtil.isSplashLikePotion(stack)) {
                for (int i = 0; i < results.size(); ++i) {
                    class_243 landing = i < this.smoothedHandPaths.size() && !this.smoothedHandPaths.get(i).isEmpty() ? this.smoothedHandPaths.get(i).getLast() : results.get(i).method_17784();
                    this.splashRings.add(new SplashRing(landing, 0.0));
                }
            }
            return;
        }
        this.smoothedHandPaths.clear();
        this.predictedHandItem = null;
    }

    private List<class_239> renderAndCheckTrajectory(class_1676 entity, double velocity, Angle angle) {
        return new ArrayList<class_239>(Collections.singleton(this.renderAndCheckTrajectory(angle.toVector(), entity, velocity)));
    }

    private class_239 renderAndCheckTrajectory(class_243 lookVec, class_1676 entity, double velocity) {
        return this.renderAndCheckTrajectory(lookVec, entity, velocity, true);
    }

    private class_239 renderAndCheckTrajectory(class_243 lookVec, class_1676 entity, double velocity, boolean inheritPlayerMotion) {
        float sqrt = class_3532.method_15355((float)lookVec.method_46409().lengthSquared());
        class_243 carryMotion = inheritPlayerMotion ? ProjectilePrediction.mc.field_1724.method_73189().method_1023(ProjectilePrediction.mc.field_1724.field_6014, ProjectilePrediction.mc.field_1724.field_6036, ProjectilePrediction.mc.field_1724.field_5969) : class_243.field_1353;
        class_243 pos = ProjectilePrediction.mc.field_1724.method_33571().method_1019(MathUtil.interpolate((class_1297)ProjectilePrediction.mc.field_1724).method_1020(ProjectilePrediction.mc.field_1724.method_73189()));
        class_243 motion = lookVec.method_1021(velocity / (double)sqrt).method_1019(carryMotion);
        ArrayList<class_243> path = new ArrayList<class_243>(128);
        path.add(pos);
        for (int i = 0; i < 300; ++i) {
            class_3965 blockResult;
            class_243 prevPos = pos;
            pos = pos.method_1019(motion);
            motion = this.calculateMotion((class_1297)entity, prevPos, motion);
            class_239 entityResult = this.raycastLivingEntities(prevPos, pos, (class_1297)entity);
            class_239 result = this.closestHit(prevPos, entityResult, (class_239)(blockResult = RaytracingUtil.raycast(prevPos, pos, class_3959.class_3960.field_17558, (class_1297)entity)));
            path.add(result.method_17783().equals((Object)class_239.class_240.field_1333) ? pos : result.method_17784());
            if (!result.method_17783().equals((Object)class_239.class_240.field_1333)) {
                this.renderSmoothedHandPath(path);
                return result;
            }
            if (pos.field_1351 < -128.0) break;
        }
        class_243 landingPos = pos;
        this.renderSmoothedHandPath(path);
        return new class_239(landingPos){

            public class_239.class_240 method_17783() {
                return class_239.class_240.field_1332;
            }
        };
    }

    private void renderSmoothedHandPath(List<class_243> path) {
        int index;
        List previous = (index = this.handPathIndex++) < this.smoothedHandPaths.size() ? this.smoothedHandPaths.get(index) : List.of();
        ArrayList<class_243> smoothed = new ArrayList<class_243>(path.size());
        for (int i = 0; i < path.size(); ++i) {
            class_243 point = path.get(i);
            if (!this.currentHandCrossbow && !previous.isEmpty()) {
                point = ((class_243)previous.get(Math.min(i, previous.size() - 1))).method_35590(point, this.handSmoothing);
            }
            smoothed.add(point);
            if (i <= 0) continue;
            int color = ColorUtil.multAlpha(ColorUtil.fade(i - 1), class_3532.method_15363((float)((float)(i - 1) / 25.0f), (float)0.0f, (float)1.0f));
            Render3DUtil.drawLine((class_243)smoothed.get(i - 1), point, color, 2.0f, false);
        }
        if (index < this.smoothedHandPaths.size()) {
            this.smoothedHandPaths.set(index, smoothed);
        } else {
            this.smoothedHandPaths.add(smoothed);
        }
    }

    public void renderProjectileResults(class_4587 matrix, List<class_239> results, boolean crossbowPrediction) {
        this.lastPredictionWasCrossbow = crossbowPrediction;
        for (int index = 0; index < results.size(); ++index) {
            class_239 result = results.get(index);
            int color = result.method_17783().equals((Object)class_239.class_240.field_1331) ? ColorUtil.RED : ColorUtil.GREEN;
            double width = 0.3;
            class_2350 markerDirection = this.getDirection(result);
            class_243 endpoint = index < this.smoothedHandPaths.size() && !this.smoothedHandPaths.get(index).isEmpty() ? this.smoothedHandPaths.get(index).getLast() : result.method_17784();
            class_243 markerPos = endpoint.method_1019(class_243.method_24954((class_2382)markerDirection.method_62675()).method_1021(0.012));
            matrix.method_22903();
            class_243 cameraPos = ProjectilePrediction.mc.field_1773.method_19418().method_71156();
            class_243 renderPos = markerPos.method_1020(cameraPos);
            matrix.method_61958(renderPos);
            class_4587.class_4665 entry = matrix.method_23760().method_56822();
            int size = 90;
            for (int i = 0; i <= size; ++i) {
                Render3DUtil.drawLine(entry, this.orientMarkerPoint(MathUtil.cosSin(i, size, width), markerDirection), this.orientMarkerPoint(MathUtil.cosSin(i + 1, size, width), markerDirection), color, color, 1.0f, false);
            }
            Render3DUtil.drawLine(entry, this.orientMarkerPoint(new class_243(0.0, 0.0, -width), markerDirection), this.orientMarkerPoint(new class_243(0.0, 0.0, width), markerDirection), color, color, 1.0f, false);
            Render3DUtil.drawLine(entry, this.orientMarkerPoint(new class_243(-width, 0.0, 0.0), markerDirection), this.orientMarkerPoint(new class_243(width, 0.0, 0.0), markerDirection), color, color, 1.0f, false);
            matrix.method_22909();
        }
    }

    public List<class_1297> getProjectiles() {
        return PlayerIntersectionUtil.streamEntities().filter(e -> (e instanceof class_1665 || e instanceof class_3857 || e instanceof class_1542) && !this.visible((class_1297)e)).toList();
    }

    public List<class_239> checkTrajectory(class_1676 entity, double velocity, Angle angle) {
        return new ArrayList<class_239>(Collections.singleton(this.checkTrajectory(angle.toVector(), entity, velocity)));
    }

    public class_239 checkTrajectory(class_243 lookVec, class_1676 entity, double velocity) {
        class_243 class_2432;
        float sqrt = class_3532.method_15355((float)lookVec.method_46409().lengthSquared());
        class_1676 class_16762 = entity;
        Objects.requireNonNull(class_16762);
        class_1676 class_16763 = class_16762;
        if (class_16763 instanceof class_1667 arrow && arrow.method_54759().method_7909().equals(class_1802.field_8399)) {
            class_2432 = class_243.field_1353;
        } else {
            class_2432 = ProjectilePrediction.mc.field_1724.method_73189().method_1023(ProjectilePrediction.mc.field_1724.field_6014, ProjectilePrediction.mc.field_1724.field_6036, ProjectilePrediction.mc.field_1724.field_5969);
        }
        class_243 motion = class_2432;
        return this.traceTrajectory(ProjectilePrediction.mc.field_1724.method_33571().method_1019(MathUtil.interpolate((class_1297)ProjectilePrediction.mc.field_1724).method_1020(ProjectilePrediction.mc.field_1724.method_73189())), lookVec.method_1021(velocity / (double)sqrt).method_1019(motion), entity);
    }

    public class_239 calcTrajectory(class_1676 e) {
        return this.traceTrajectory(e.method_73189(), e.method_18798(), e);
    }

    public class_239 traceTrajectory(class_243 pos, class_243 motion, class_1676 entity) {
        for (int i = 0; i < 300; ++i) {
            class_243 prevPos = pos;
            pos = pos.method_1019(motion);
            motion = this.calculateMotion((class_1297)entity, prevPos, motion);
            class_3965 result = RaytracingUtil.raycast(prevPos, pos, class_3959.class_3960.field_17558, (class_1297)entity);
            if (!result.method_17783().equals((Object)class_239.class_240.field_1333)) {
                return result;
            }
            class_239 entityResult = this.raycastLivingEntities(prevPos, pos, (class_1297)entity);
            if (!entityResult.method_17783().equals((Object)class_239.class_240.field_1333)) {
                return entityResult;
            }
            if (pos.field_1351 < -128.0) break;
        }
        return null;
    }

    public class_243 calculateMotion(class_1297 entity, class_243 prevPos, class_243 motion) {
        double d;
        boolean isInWater = ProjectilePrediction.mc.field_1687.method_8316(class_2338.method_49638((class_2374)prevPos)).method_15767(class_3486.field_15517);
        class_1297 class_12972 = entity;
        Objects.requireNonNull(class_12972);
        class_1297 class_12973 = class_12972;
        if (class_12973 instanceof class_1685) {
            class_1685 trident = (class_1685)class_12973;
            d = 0.99;
        } else if (class_12973 instanceof class_1665 && isInWater) {
            class_1665 persistent = (class_1665)class_12973;
            d = 0.6;
        } else if (isInWater) {
            d = 0.8;
        } else {
            d = 0.99;
        }
        double multiply = d;
        return motion.method_1021(multiply).method_1031(0.0, -entity.method_56989(), 0.0);
    }

    private void BreakingBad(class_1297 entity, class_243 pos, int ticks) {
        class_1297 class_12972 = entity;
        Objects.requireNonNull(class_12972);
        class_1297 class_12973 = class_12972;
        if (class_12973 instanceof class_1542 item) {
            this.points.add(new Point(item.method_6983(), pos, ticks, null));
        } else if (class_12973 instanceof class_3857 thrown) {
            this.points.add(new Point(thrown.method_7495(), pos, ticks, this.ownerEntity((class_1297)thrown)));
        } else if (class_12973 instanceof class_1665 persistent) {
            this.points.add(new Point(persistent.method_54759(), pos, ticks, this.ownerEntity((class_1297)persistent)));
        }
    }

    private class_1297 ownerEntity(class_1297 entity) {
        class_1676 projectile;
        return entity instanceof class_1676 && (projectile = (class_1676)entity).method_24921() != null ? projectile.method_24921() : null;
    }

    private class_2350 getDirection(class_239 result) {
        if (result instanceof class_3965) {
            class_3965 blockHitResult = (class_3965)result;
            return blockHitResult.method_17780();
        }
        if (result instanceof class_3966) {
            class_243 toPlayer = ProjectilePrediction.mc.field_1724.method_33571().method_1020(result.method_17784()).method_1029();
            return class_2350.method_58251((class_243)toPlayer);
        }
        return class_2350.method_58251((class_243)result.method_17784().method_1020(ProjectilePrediction.mc.field_1724.method_33571()).method_1029());
    }

    private class_243 orientMarkerPoint(class_243 point, class_2350 direction) {
        return switch (direction) {
            case class_2350.field_11033 -> new class_243(point.field_1352, 0.0, -point.field_1350);
            case class_2350.field_11043 -> new class_243(point.field_1352, point.field_1350, 0.0);
            case class_2350.field_11035 -> new class_243(point.field_1352, -point.field_1350, 0.0);
            case class_2350.field_11034 -> new class_243(0.0, -point.field_1350, point.field_1352);
            case class_2350.field_11039 -> new class_243(0.0, -point.field_1350, -point.field_1352);
            default -> point;
        };
    }

    private boolean visible(class_1297 entity) {
        boolean posChange = entity.method_23317() == entity.field_6014 && entity.method_23318() == entity.field_6036 && entity.method_23321() == entity.field_5969;
        boolean itemEntityCheck = entity instanceof class_1542 && (entity.method_24828() || PlayerIntersectionUtil.isBoxInBlock(entity.method_5829().method_1014(2.0), class_2246.field_10382));
        return posChange || itemEntityCheck;
    }

    private void renderSplashRadius(SplashRing ring) {
        int baseColor = ColorUtil.multAlpha(ColorUtil.getClientColor(), ring.alpha() <= 0.0 ? 0.75f : 0.45f);
        int shadowColor = ColorUtil.multAlpha(baseColor, 0.45f);
        class_243 center = ring.pos.method_1031(0.0, 0.08, 0.0);
        int segments = 64;
        for (int i = 0; i < segments; ++i) {
            double startAngle = Math.toRadians(360.0 / (double)segments * (double)i);
            double endAngle = Math.toRadians(360.0 / (double)segments * (double)(i + 1));
            class_243 start = center.method_1031(Math.cos(startAngle) * 4.0, 0.0, Math.sin(startAngle) * 4.0);
            class_243 end = center.method_1031(Math.cos(endAngle) * 4.0, 0.0, Math.sin(endAngle) * 4.0);
            Render3DUtil.drawLine(start, end, baseColor, 2.0f, false);
            Render3DUtil.drawLine(start.method_1031(0.0, 0.01, 0.0), end.method_1031(0.0, 0.01, 0.0), shadowColor, 4.0f, false);
        }
    }

    private double ticksToSeconds(int ticks) {
        return (double)(ticks * 50) / 1000.0;
    }

    private class_239 raycastLivingEntities(class_243 from, class_243 to, class_1297 projectile) {
        class_1297 closestEntity = null;
        class_243 closestPos = null;
        double closestDistance = Double.MAX_VALUE;
        for (class_1297 entity : PlayerIntersectionUtil.streamEntities().toList()) {
            double distance;
            class_243 hitPos;
            class_1676 projectileEntity;
            if (!(entity instanceof class_1309)) continue;
            class_1309 living = (class_1309)entity;
            if (projectile instanceof class_1676 && living == (projectileEntity = (class_1676)projectile).method_24921() || !living.method_5805() || (hitPos = (class_243)entity.method_5829().method_1014(0.3).method_992(from, to).orElse(null)) == null || !((distance = from.method_1025(hitPos)) < closestDistance)) continue;
            closestDistance = distance;
            closestEntity = entity;
            closestPos = hitPos;
        }
        if (closestEntity != null) {
            return new class_3966(closestEntity, closestPos);
        }
        return new class_239(to){

            public class_239.class_240 method_17783() {
                return class_239.class_240.field_1333;
            }
        };
    }

    private class_239 closestHit(class_243 from, class_239 first, class_239 second) {
        if (first.method_17783().equals((Object)class_239.class_240.field_1333)) {
            return second;
        }
        if (second.method_17783().equals((Object)class_239.class_240.field_1333)) {
            return first;
        }
        return from.method_1025(first.method_17784()) <= from.method_1025(second.method_17784()) ? first : second;
    }

    private record Point(class_1799 stack, class_243 pos, int ticks, class_1297 owner) {
    }

    private record SplashRing(class_243 pos, double alpha) {
    }
}

