package net.nukebob.nbconfig.screen;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;

public class LittleNukebob {
    private static final float HITBOX_RADIUS = (float) Math.sqrt(0.15);

    private Vec2 pos = Vec2.ZERO; //0 to 1
    private Vec2 vel = Vec2.ZERO;

    private float rot = 0;

    public void setPos(Vec2 pos) {
        this.pos = pos;
    }
    public void setPosX(float x) {
        this.pos = new Vec2(x, pos.y);
    }
    public void setPosY(float y) {
        this.pos = new Vec2(pos.x, y);
    }
    public void addPos(Vec2 add) {
        this.pos = this.pos.add(add);
    }
    public void addPosX(float x) {
        pos = new Vec2(pos.x+x, pos.y);
    }
    public void addPosY(float y) {
        pos = new Vec2(pos.x, pos.y+y);
    }
    public Vec2 getPos() {
        return pos;
    }

    public void setVel(Vec2 vel) {
        this.vel = vel;
    }
    public void scaleVelocity(float scale) {
        this.vel = new Vec2(vel.x * scale, vel.y * scale);
    }
    public void scaleVelocityX(float scale) {
        this.vel = new Vec2(vel.x * scale, vel.y);
    }
    public void scaleVelocityY(float scale) {
        this.vel = new Vec2(vel.x, vel.y * scale);
    }
    public void addVel(Vec2 add) {
        this.vel = this.vel.add(add);
    }
    public Vec2 getVel() {
        return vel;
    }

    public void setRot(float rot) {
        this.rot = rot;
    }
    public float getRot() {
        return rot;
    }

    public void physics(float delta, float littleNukeScale, float width, float height) {
        //radius
        float ry = getCollisionRadiusY(littleNukeScale, height);
        float rx = getCollisionRadiusX(littleNukeScale, width);

        //gravity
        float gravity = 0.01f;
        addVel(new Vec2(0, gravity*delta));
        //apply velocity
        addPos(vel.scale(delta));
        //wall bounce
        float wallRestitution = 0.8f;
        if (pos.y+ry>1) {
            scaleVelocityY(-1*wallRestitution);
        }
        if (pos.y-ry<0) {
            scaleVelocityY(-1*wallRestitution);
        }
        if (pos.x+rx>1) {
            scaleVelocityX(-1*wallRestitution);
        }
        if (pos.x-rx<0) {
            scaleVelocityX(-1*wallRestitution);
        }
        setPosY(Mth.clamp(pos.y, ry, 1-ry));
        setPosX(Mth.clamp(pos.x, rx, 1-rx));
        //floor friction
        if (pos.y+ry>0.999f) {
            scaleVelocityX(0.975f);
        }
    }

    public float getCollisionRadiusX(float littleNukeScale, float width) {
        return HITBOX_RADIUS * littleNukeScale / width;
    }

    public float getCollisionRadiusY(float littleNukeScale, float height) {
        return HITBOX_RADIUS * littleNukeScale / height;
    }

    public boolean isInHitbox(float littleNukeScale, float width, float height, float mouseX, float mouseY) {
        float pivotX = pos.x * width;
        float pivotY = pos.y * height;

        float dx = mouseX - pivotX;
        float dy = mouseY - pivotY;

        float cos = (float) Math.cos(-rot);
        float sin = (float) Math.sin(-rot);
        float localX = dx * cos - dy * sin;
        float localY = dx * sin + dy * cos;

        localX /= littleNukeScale;
        localY /= littleNukeScale;

        float ny = localY;
        return localX * localX + ny * ny < 0.15f;
    }

    public Vec2 getLightDirectionRelative(Vec2 lightSource, float height, float littleNukeScale) {
        return pos.scale(height).add(littleNukeScale/2f).add(lightSource.scale(-1)).normalized().rotate(-rot);
    }
}
