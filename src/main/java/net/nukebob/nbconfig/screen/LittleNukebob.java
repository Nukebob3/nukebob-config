package net.nukebob.nbconfig.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.Vec2;

public class LittleNukebob {
    private static final float HITBOX_RADIUS = (float) Math.sqrt(0.15);
    private static final float HITBOX_OFFSET = 0.03f;

    private Vec2 pos = Vec2.ZERO;
    private Vec2 vel = Vec2.ZERO;

    private static final float HOLE_HALF_WIDTH_FRAC = 0.8f/3.0f;
    private static final float HOLE_Y_THRESHOLD_FRAC = 2.5f / 3f;
    private static final float SHELL_THICKNESS = 0.15f;

    private boolean inside = false;

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
        float acceleration = 0.01f;
        addVel(new Vec2(0, acceleration).scale(delta));
        addPos(getVel().scale(delta));

        Vec2 hitboxCenter = getHitboxCenter(littleNukeScale, width, height);
        float rx = getCollisionRadiusX(littleNukeScale, width);
        float ry = getCollisionRadiusY(littleNukeScale, height);

        //wall collision
        if (hitboxCenter.y + ry > 1) {
            addPosY(-((hitboxCenter.y + ry) - 1));
            scaleVelocityY(-0.9f);
        }
        if (hitboxCenter.y - ry < 0) {
            addPosY(-(hitboxCenter.y - ry));
            scaleVelocityY(-0.9f);
        }
        if (hitboxCenter.x + rx > 1) {
            addPosX(-((hitboxCenter.x + rx) - 1));
            scaleVelocityX(-0.9f);
        }
        if (hitboxCenter.x - rx < 0) {
            addPosX(-(hitboxCenter.x - rx));
            scaleVelocityX(-0.9f);
        }
        //drag
        scaleVelocity(0.99f);

        //if consumed
        if (getPos().x<0.65&getPos().x>0.35&&getPos().y>0.45&&getPos().y<0.55) {
            if (!inside) {
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.GENERIC_EAT.value(), 1f, 5f));
                inside = true;
            }
            setVel(new Vec2(0,0));
        } else if (inside) {
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_TRUMPET_OXIDIZED.value(), 0f, 5f));
            inside = false;
        }
    }
    public Vec2 getHitboxCenter(float littleNukeScale, float width, float height) {
        Vec2 localOffset = new Vec2(0, HITBOX_OFFSET * littleNukeScale / height).rotate(rot);
        return pos.add(localOffset);
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

        float ny = localY - HITBOX_OFFSET;
        return localX * localX + ny * ny < 0.15f;
    }
    private boolean fallingThroughHole = false;

    public void resolveCenterSphereCollision(Vec2 centerPos, float centerRadiusPixels, float littleNukeScale, float width, float height) {
        Vec2 hitboxCenter = getHitboxCenter(littleNukeScale, width, height);
        float myRx = getCollisionRadiusX(littleNukeScale, width);
        float myRy = getCollisionRadiusY(littleNukeScale, height);
        float centerRx = centerRadiusPixels / width;
        float centerRy = centerRadiusPixels / height;

        Vec2 delta = hitboxCenter.add(centerPos.scale(-1));
        float nx = delta.x / (myRx + centerRx);
        float ny = delta.y / (myRy + centerRy);
        float distNorm = (float) Math.sqrt(nx * nx + ny * ny);

        boolean inHoleNow = isInHole(nx, ny);

        if (inHoleNow) {
            fallingThroughHole = true;
        } else if (distNorm < (1f - SHELL_THICKNESS)) {
            fallingThroughHole = false;
        } else if (distNorm > 1f) {
            fallingThroughHole = false;
        }

        if (distNorm < 1f && distNorm > (1f - SHELL_THICKNESS) && !fallingThroughHole) {
            Vec2 correctedDelta = delta.scale(1f / distNorm);
            Vec2 newHitboxCenter = centerPos.add(correctedDelta);
            addPos(newHitboxCenter.add(hitboxCenter.scale(-1)));

            Vec2 normal = new Vec2(nx / distNorm, ny / distNorm);
            float velDot = vel.x * normal.x + vel.y * normal.y;
            if (velDot < 0) {
                addVel(normal.scale(-2f * velDot * 0.5f));
            }
        }
    }

    public void pushOutOfCenterSphere(Vec2 centerPos, float centerRadiusPixels, float littleNukeScale, float width, float height) {
        Vec2 hitboxCenter = getHitboxCenter(littleNukeScale, width, height);
        float myRx = getCollisionRadiusX(littleNukeScale, width);
        float myRy = getCollisionRadiusY(littleNukeScale, height);
        float centerRx = centerRadiusPixels / width;
        float centerRy = centerRadiusPixels / height;

        Vec2 delta = hitboxCenter.add(centerPos.scale(-1));
        float nx = delta.x / (myRx + centerRx);
        float ny = delta.y / (myRy + centerRy);
        float distNorm = (float) Math.sqrt(nx * nx + ny * ny);

        if (distNorm < 1f && distNorm > 0.0001f && !isInHole(nx, ny)) {
            Vec2 correctedDelta = delta.scale(1f / distNorm);
            Vec2 newHitboxCenter = centerPos.add(correctedDelta);
            Vec2 correction = newHitboxCenter.add(hitboxCenter.scale(-1));
            addPos(correction);
        }
    }

    private boolean isInHole(float nx, float ny) {
        float margin = 0.05f;
        return Math.abs(nx) < (HOLE_HALF_WIDTH_FRAC - margin) && ny < -(HOLE_Y_THRESHOLD_FRAC + margin);
    }

    public Vec2 getLightDirectionRelative(Vec2 lightSource, float height, float littleNukeScale) {
        return pos.scale(height).add(littleNukeScale/2f).add(lightSource.scale(-1)).normalized().rotate(-rot);
    }
}
