package net.nukebob.nbconfig.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.nukebob.nbconfig.NukebobConfig;
import net.nukebob.nbconfig.render.NukebobPipelines;
import org.jspecify.annotations.NonNull;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;

public abstract class NukebobConfigScreen extends Screen {
    private final ArrayList<LittleNukebob> littleNukebobs;

    private Vec2 clickPos = Vec2.ZERO;
    private int selected = -1;
    private int mouseButton = -1;

    private Vec2 lastDragMouse = Vec2.ZERO;
    private Vec2 dragVelocity = Vec2.ZERO;

    public static Vec2 CENTER_POS = Vec2.ZERO;
    public static float CENTER_RADIUS = 0;

    public NukebobConfigScreen() {
        super(Component.literal("Config"));
        littleNukebobs = setConfig();
    }

    public abstract ArrayList<LittleNukebob> setConfig();

    @Override
    protected void init() {
        super.init();
        for (int i = 0; i < littleNukebobs.size(); i++) {
            LittleNukebob littleNukebob = littleNukebobs.get(i);
            littleNukebob.setVel(Vec2.ZERO);
            littleNukebob.setRotVel(0);
            littleNukebob.setRot(0);
            littleNukebob.setPos(new Vec2((float) (0.5f*width + (i+0.5f-littleNukebobs.size()/2f)*height*0.8/3.1f/6f*3f), 0.9f*height));
            littleNukebob.update();
        }
    }

    @Override
    public void render(@NonNull GuiGraphics graphics, int mouseX, int mouseY, float a) {
        super.render(graphics, mouseX, mouseY, a);
        int scale = (int) (this.height*0.8);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, NukebobConfig.id("nukebobs/big"), (width-scale)/2, (height-scale)/2, scale, scale);

        //help arrow
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, NukebobConfig.id("help_arrow"), (width-scale/8)/2, (int) (height*0.05f), scale/8, scale/8, 0x99FFFFFF);
        graphics.drawCenteredString(minecraft.font, "Drop configs in here to toggle", width/2, (int) (height*0.02f), 0x99FFFFFF);

        //big one
        CENTER_POS = new Vec2(width/2f, height/2f - (float) scale / 13f);
        CENTER_RADIUS = scale / 3.1f;


/* hitbox for big one
        //big hitbox
        float quadX = (width - scale) / 2f;
        float quadY = (height - scale) / 2f;
        float xOffsetPixels = CENTER_POS.x - quadX;
        float yOffsetPixels = CENTER_POS.y - quadY;
        float normalizedXOffset = xOffsetPixels / (float) scale;
        float normalizedYOffset = yOffsetPixels / (float) scale;
        float normalizedRadius = CENTER_RADIUS / (float) scale;

        int redChannel = Mth.clamp((int)(normalizedXOffset * 255f), 0, 255);
        int greenChannel = Mth.clamp((int)(normalizedYOffset * 255f), 0, 255);
        int blueChannel = Mth.clamp((int)(normalizedRadius * 255f), 0, 255);

        int bigCol = 0xFF000000 | (redChannel << 16) | (greenChannel << 8) | blueChannel;
        graphics.blit(NukebobPipelines.BIG_NUKEBOB_HITBOX, NukebobConfig.id("textures/gui/sprites/nukebobs/big.png"), (width-scale)/2, (height-scale)/2, 0, 0, scale, scale, scale, scale, bigCol);

*/



        int littleNukeScale = scale/6;
        //force vector
        if (selected!=-1&&mouseButton==GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            Vec2 force = new Vec2(mouseX, mouseY).add(clickPos.scale(-1));
            float length = Math.min(force.length(),75);
            force = force.normalized().scale(length);
            float angle = (float) Math.atan2(force.x, -force.y);
            float arrowScale = length/32f;

            int steps = 9;
            for (int i = 0; i < steps; i++) {
                Vec2 futurePos = littleNukebobs.get(selected).physicsStep(i, littleNukeScale, width, height, force);
                int color = 0x00FFFFFF | ((int)(255 * ((steps - i) / (float) steps)) << 24);
                graphics.vLine((int) futurePos.x, (int) futurePos.y, (int) futurePos.y, color);
            }

            graphics.pose().pushMatrix();
            graphics.pose().translate(clickPos.x, clickPos.y);
            graphics.pose().rotate(angle);
            graphics.pose().scale(arrowScale, arrowScale);
            graphics.pose().translate(-16, -32);
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, NukebobConfig.id("arrow"), 0, 0, 32, 32, 0x99FFFFFF);
            graphics.pose().popMatrix();
        }
        //right-click drag
        else if (selected!=-1&&mouseButton==GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            Vec2 newMouse = new Vec2(mouseX, mouseY);
            dragVelocity = newMouse.add(lastDragMouse.scale(-1));
            lastDragMouse = newMouse;

            littleNukebobs.get(selected).setPos(
                    new Vec2(Mth.clamp(
                            (float) mouseX,
                            littleNukebobs.get(selected).getCollisionRadius(littleNukeScale),
                            width - littleNukebobs.get(selected).getCollisionRadius(littleNukeScale)),

                            Mth.clamp((float) mouseY,
                                    littleNukebobs.get(selected).getCollisionRadius(littleNukeScale),
                                    height - littleNukebobs.get(selected).getCollisionRadius(littleNukeScale))));

            littleNukebobs.get(selected).pushOutOfBigNukebob(littleNukeScale);
        }

        //render and physics
        for (int i = 0; i<littleNukebobs.size(); i++) {
            LittleNukebob littleNukebob = littleNukebobs.get(i);

            //light
            Vec2 lightSource = new Vec2((float) width /2+ (float) height *3/8, (float) -height /1.5f);
            Vec2 lightDirectionRelative = littleNukebob.getLightDirectionRelative(lightSource, littleNukeScale);
            //physics
            if (i!=selected||!(mouseButton==GLFW.GLFW_MOUSE_BUTTON_LEFT||mouseButton==GLFW.GLFW_MOUSE_BUTTON_RIGHT)) {
                float delta = (float) Math.min(minecraft.getDeltaTracker().getRealtimeDeltaTicks(),0.5);
                littleNukebob.physics(delta, (float) littleNukeScale, width, height);
            } else {
                littleNukebob.setVel(Vec2.ZERO);
                littleNukebob.setRotVel(0f);
            }
            //tooltip
            boolean hovered = littleNukebob.isInHitbox(littleNukeScale, mouseX, mouseY);
            if (hovered&&!littleNukebob.inside) graphics.setTooltipForNextFrame(minecraft.font, littleNukebob.name.copy().append(Component.literal(": ").append(littleNukebob.enabled?Component.literal("enabled").withColor(0x3dd162):Component.literal("disabled").withColor(0xe64b43))), mouseX, mouseY);

            graphics.pose().pushMatrix();
            graphics.pose().translate(littleNukebob.getPos().x, littleNukebob.getPos().y);
            graphics.pose().rotate(littleNukebob.getRot());
            graphics.pose().translate(-littleNukeScale/2f, -littleNukeScale/2f);
            int color = 0xFF000000;
            int r = (int) ((lightDirectionRelative.x * 0.5f + 0.5f) * 255f);
            int g = (int) ((lightDirectionRelative.y * 0.5f + 0.5f) * 255f);
            color = color | (r << 16) | (g << 8);
            graphics.blit(NukebobPipelines.LITTLE_NUKEBOB_GUI, NukebobConfig.id("textures/gui/sprites/nukebobs/"+(!(hovered&&!littleNukebob.inside)?"little":"little_hover")+(littleNukebob.enabled?"_on":"")+".png"), 0,0, 0, 0, littleNukeScale, littleNukeScale, littleNukeScale, littleNukeScale, color);
            graphics.pose().popMatrix();
        }
        interNukebobCollision(littleNukeScale);

        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, NukebobConfig.id("nukebobs/big_front"), (width-scale)/2, (height-scale)/2, scale, scale);
    }

    @Override
    public boolean mouseClicked(@NonNull MouseButtonEvent event, boolean doubleClick) {
        boolean clicked = false;
        for (int i = 0; i < littleNukebobs.size(); i++) {
            if (littleNukebobs.get(i).isInHitbox((float) (height*0.8/6), (float) event.x(), (float) event.y())&&!littleNukebobs.get(i).inside) {
                clicked = true;
                selected = i;
                clickPos = new Vec2((float) event.x(), (float) event.y());
                lastDragMouse = new Vec2((float) event.x(), (float) event.y());
                mouseButton = event.button();
                break;
            }
        }
        if (clicked) {
            AbstractButton.playButtonClickSound(minecraft.getSoundManager());
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseReleased(@NonNull MouseButtonEvent event) {
        if (selected!=-1) {
            if (event.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                Vec2 force = new Vec2((float) event.x(), (float) event.y()).add(clickPos.scale(-1));
                float length = Math.min(force.length(), 75);
                length = (length / 5);
                force = force.normalized().scale(length);

                littleNukebobs.get(selected).setVel(force.scale(4f));
            } else if (event.button() == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                littleNukebobs.get(selected).setVel(dragVelocity.scale(3f));
            }
        }
        selected = -1;
        mouseButton = -1;
        return super.mouseReleased(event);
    }

    public void interNukebobCollision(float littleNukeScale) {
        int size = littleNukebobs.size();
        for (int a = 0; a < 4; a++) {
            for (int i = 0; i < size; i++) {
                for (int j = i + 1; j < size; j++) {
                    LittleNukebob b1 = littleNukebobs.get(i);
                    LittleNukebob b2 = littleNukebobs.get(j);

                    Vec2 posRel = b1.getPos().add(b2.getPos().negated());
                    Vec2 velRel = b1.getVel().add(b2.getVel().negated());
                    float distSq = posRel.x * posRel.x + posRel.y * posRel.y;

                    float r1 = b1.getCollisionRadius(littleNukeScale);
                    float r2 = b2.getCollisionRadius(littleNukeScale);
                    float minDist = r1 + r2;

                    if (distSq >= minDist * minDist || distSq == 0) continue;

                    float dist = (float) Math.sqrt(distSq);
                    Vec2 normal = posRel.normalized();
                    float overlap = minDist - dist;

                    Vec2 correction = normal.scale(overlap * 0.5f);
                    b1.setPos(b1.getPos().add(correction));
                    b2.setPos(b2.getPos().add(correction.negated()));

                    Vec2 v1 = b1.getVel().add(posRel.negated().scale((velRel.dot(posRel)/posRel.lengthSquared()))).scale(0.9f);
                    Vec2 v2 = b2.getVel().add(posRel.scale((velRel.negated().dot(posRel.negated())/posRel.negated().lengthSquared()))).scale(0.9f);
                    b1.setVel(v1);
                    b2.setVel(v2);

                    Vec2 tangent = new Vec2(-normal.y, normal.x);
                    float tangentVel = velRel.dot(tangent);
                    b1.rotVel += (tangentVel / r1) * 0.025f;
                    b2.rotVel += (tangentVel / r2) * 0.025f;
                }
            }
        }
    }
}
