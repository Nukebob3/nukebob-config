package net.nukebob.nbconfig.screen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
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

public class NukebobConfigScreen extends Screen {
    private final Screen parent;
    private final LittleNukebob[] littleNukebobs;
    private final LittleNukebob showLivesInNametag;

    private Vec2 clickPos = Vec2.ZERO;
    private int selected = -1;
    private int mouseButton = -1;

    private Vec2 lastDragMouse = Vec2.ZERO;
    private Vec2 dragVelocity = Vec2.ZERO;

    public NukebobConfigScreen(Screen screen) {
        parent = screen;
        super(Component.literal("Config"));
        showLivesInNametag = new LittleNukebob();
        showLivesInNametag.setPos(new Vec2(0.5f, 0.9f));
        littleNukebobs = new LittleNukebob[]{showLivesInNametag};
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractRenderState(graphics, mouseX, mouseY, a);
        int scale = (int) (this.height*0.8);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, NukebobConfig.id("nukebobs/big"), (width-scale)/2, (height-scale)/2, scale, scale);

        //big one
        Vec2 centerPos = new Vec2(0.5f - (float) scale / width / 80f, 0.5f - (float) scale / height / 15f);
        float centerRadius = scale / 3.1f;

        int littleNukeScale = scale/6;
        //force vector
        if (selected!=-1&&mouseButton==GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            Vec2 force = new Vec2(mouseX, mouseY).add(clickPos.scale(-1));
            float length = Math.min(force.length(),75);
            force = force.normalized().scale(length);
            float angle = (float) Math.atan2(force.x, -force.y);
            float arrowScale = length/32f;

            graphics.pose().pushMatrix();
            graphics.pose().translate(clickPos.x, clickPos.y);
            graphics.pose().rotate(angle);
            graphics.pose().scale(arrowScale, arrowScale);
            graphics.pose().translate(-16, -32);
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, NukebobConfig.id("arrow"), 0, 0, 32, 32, 0x99FFFFFF);
            graphics.pose().popMatrix();
        } else if (selected!=-1&&mouseButton==GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            Vec2 newMouse = new Vec2(mouseX / (float) width, mouseY / (float) height);
            dragVelocity = newMouse.add(lastDragMouse.scale(-1));
            lastDragMouse = newMouse;

            littleNukebobs[selected].setPos(
                    new Vec2(Mth.clamp(
                            (float) mouseX / width,
                            littleNukebobs[selected].getCollisionRadiusX(littleNukeScale, width),
                            1 - littleNukebobs[selected].getCollisionRadiusX(littleNukeScale, width)),

                            Mth.clamp((float) mouseY / height,
                                    0 + littleNukebobs[selected].getCollisionRadiusY(littleNukeScale, height) - 0.003f,
                                    1 - littleNukebobs[selected].getCollisionRadiusY(littleNukeScale, height)) - 0.003f));

            littleNukebobs[selected].pushOutOfCenterSphere(centerPos, centerRadius, littleNukeScale, width, height);
        }

        for (int i = 0; i<littleNukebobs.length; i++) {
            LittleNukebob littleNukebob = littleNukebobs[i];

            //light
            Vec2 lightSource = new Vec2((float) width /2+ (float) height *3/8, (float) -height /1.5f);
            Vec2 lightDirectionRelative = littleNukebob.getLightDirectionRelative(lightSource, height, littleNukeScale);
            //physics
            if (i!=selected&&!(mouseButton==GLFW.GLFW_MOUSE_BUTTON_LEFT||mouseButton==GLFW.GLFW_MOUSE_BUTTON_RIGHT)) {
                littleNukebob.physics((float) Math.min(minecraft.getDeltaTracker().getRealtimeDeltaTicks(),0.5), (float) littleNukeScale, width, height);
                littleNukebob.resolveCenterSphereCollision(centerPos, centerRadius, littleNukeScale, width, height);
            } else {
                littleNukebob.setVel(Vec2.ZERO);
            }
            //tooltip
            boolean hovered = littleNukebob.isInHitbox(littleNukeScale, width, height, mouseX, mouseY) && !(littleNukebob.getPos().x<0.55&littleNukebob.getPos().x>0.45&&littleNukebob.getPos().y>0.25&&littleNukebob.getPos().y<0.75);
            if (hovered) graphics.setTooltipForNextFrame(minecraft.font, Component.literal("ballz"), mouseX, mouseY);

            graphics.pose().pushMatrix();
            graphics.pose().translate(littleNukebob.getPos().x*width, littleNukebob.getPos().y*height);
            graphics.pose().rotate(littleNukebob.getRot());
            graphics.pose().translate(-littleNukeScale/2f, -littleNukeScale/2f);
            int color = 0xFF000000;
            int r = (int) ((lightDirectionRelative.x * 0.5f + 0.5f) * 255f);
            int g = (int) ((lightDirectionRelative.y * 0.5f + 0.5f) * 255f);
            color = color | (r << 16) | (g << 8);
            graphics.blit(NukebobPipelines.LITTLE_NUKEBOB_GUI, NukebobConfig.id("textures/gui/sprites/nukebobs/"+(!hovered?"little":"little_hover")+".png"), 0,0, 0, 0, littleNukeScale, littleNukeScale, littleNukeScale, littleNukeScale, color);
            graphics.pose().popMatrix();
        }

        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, NukebobConfig.id("nukebobs/big_front"), (width-scale)/2, (height-scale)/2, scale, scale);
    }

    @Override
    public boolean mouseClicked(@NonNull MouseButtonEvent event, boolean doubleClick) {
        boolean clicked = false;
        for (int i = 0; i < littleNukebobs.length; i++) {
            if (littleNukebobs[i].isInHitbox((float) (height*0.8/6), (float) width, (float) height, (float) event.x(), (float) event.y())) {
                clicked = true;
                selected = i;
                clickPos = new Vec2((float) event.x(), (float) event.y());
                lastDragMouse = new Vec2((float) (event.x() / (float) width), (float) (event.y() / (float) height));
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
                length = (length / 5) * (length / 5);
                force = force.normalized().scale(length);

                littleNukebobs[selected].setVel(force.scale(0.001f));
            } else if (event.button() == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                littleNukebobs[selected].setVel(dragVelocity.scale(3f));
            }
        }
        selected = -1;
        mouseButton = -1;
        return super.mouseReleased(event);
    }

    @Override
    public void onClose() {
        minecraft.setScreenAndShow(parent);
    }
}
