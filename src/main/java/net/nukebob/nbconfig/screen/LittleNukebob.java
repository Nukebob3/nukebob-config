package net.nukebob.nbconfig.screen;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;

public class LittleNukebob {
    private static final float HITBOX_RADIUS = (float) Math.sqrt(0.15);

    private Vec2 pos = Vec2.ZERO;
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
        float r = getCollisionRadius(littleNukeScale);

        //gravity
        float gravity = 3f;
        addVel(new Vec2(0, gravity*delta));
        //apply velocity
        addPos(vel.scale(delta));
        //wall bounce
        float wallRestitution = 0.8f;
        if (pos.y+r>height) {
            scaleVelocityY(-1*wallRestitution);
        }
        if (pos.y-r<0) {
            scaleVelocityY(-1*wallRestitution);
        }
        if (pos.x+r>width) {
            scaleVelocityX(-1*wallRestitution);
        }
        if (pos.x-r<0) {
            scaleVelocityX(-1*wallRestitution);
        }
        setPosY(Mth.clamp(pos.y, r, height-r));
        setPosX(Mth.clamp(pos.x, r, width-r));
        //floor friction
        if (pos.y+r>height-r-3) {
            scaleVelocityX(0.975f);
        }
        //big nukebob collision
        float distToBigOne = NukebobConfigScreen.CENTER_POS.add(pos.scale(-1)).length();
        float bigCollisionRadius = NukebobConfigScreen.CENTER_RADIUS+HITBOX_RADIUS*littleNukeScale;
        if (distToBigOne<bigCollisionRadius) {
            Vec2 normal = pos.add(NukebobConfigScreen.CENTER_POS.scale(-1)).normalized();
            setVel(vel.add(normal.scale(-2f*(vel.dot(normal)))));
            setPos(NukebobConfigScreen.CENTER_POS.add(normal.scale(bigCollisionRadius)));
        }
    }

    public float getCollisionRadius(float littleNukeScale) {
        return HITBOX_RADIUS * littleNukeScale;
    }

    public boolean isInHitbox(float littleNukeScale, float mouseX, float mouseY) {
        float pivotX = pos.x;
        float pivotY = pos.y;

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

    public Vec2 getLightDirectionRelative(Vec2 lightSource, float littleNukeScale) {
        return pos.add(littleNukeScale/2f).add(lightSource.scale(-1)).normalized().rotate(-rot);
    }
}
