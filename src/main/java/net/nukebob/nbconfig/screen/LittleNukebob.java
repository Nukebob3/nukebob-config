package net.nukebob.nbconfig.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;

import java.util.function.Consumer;

public class LittleNukebob {
    private static final float HITBOX_RADIUS = (float) Math.sqrt(0.15);

    private Vec2 pos = Vec2.ZERO;
    private Vec2 vel = Vec2.ZERO;

    private float rot = 0;

    public boolean inside = false;

    public final MutableComponent name;
    private final Consumer<LittleNukebob> onPress;
    public boolean enabled;

    public LittleNukebob(MutableComponent name, Consumer<LittleNukebob> onPress) {
        this.name = name;
        this.onPress = onPress;
    }

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
        float R = NukebobConfigScreen.CENTER_RADIUS;
        Vec2 posRelative = NukebobConfigScreen.CENTER_POS.add(pos.scale(-1));
        float distToBigOne = posRelative.length();
        float bigCollisionRadius = R+r;
        float holeWidth = 0.45f;
        if (distToBigOne<bigCollisionRadius) { //first check big circle
            //inside stuff
            if (inside) {
                setVel(new Vec2(0, 3f));
                setPosX(width/2f);
                return;
            }

            float distToAbsolute = (posRelative.x*(posRelative.x<0?-1:1)-posRelative.y)/Mth.sqrt(2);
            boolean outsideComplex = posRelative.y-r<-0.5f*R || //below bottom
                    Mth.abs(posRelative.x)>R/Mth.sqrt(2)-r; //outside absolute slope
            //boolean touchingBottomCorner = new Vec2(holeWidth * R, -0.5f * R).distanceToSqr(new Vec2(Mth.abs(posRelative.x), posRelative.y))<r*r;
            boolean touchingTopCorner = new Vec2(holeWidth*R, holeWidth * R).distanceToSqr(new Vec2(Mth.abs(posRelative.x),posRelative.y))<r*r;
            boolean belowAbsolute = (Mth.abs(posRelative.x)>holeWidth*R-r && posRelative.y+r<holeWidth*R)&&(touchingTopCorner||posRelative.y<holeWidth*R);//area under absolute slopes
            boolean absoluteSlope = -(Mth.abs(posRelative.x)-posRelative.y)/Mth.sqrt(2)<r && Mth.abs(posRelative.x)>holeWidth*R-r;
            boolean notTouchingAbsoluteSlope = posRelative.y-r<holeWidth*R;
            boolean inside = posRelative.y<r && Mth.abs(posRelative.x)+r<holeWidth*R;

            if (absoluteSlope&&!belowAbsolute) {
                if (outsideComplex) {
                    if (Mth.abs(posRelative.add(vel.scale(-delta)).x)<R/Mth.sqrt(2f)) bounceAbsolute(posRelative, r, distToAbsolute);
                    else bounceCircle(bigCollisionRadius);
                } else {
                    bounceAbsolute(posRelative, r, distToAbsolute);
                }
            } else if (outsideComplex&&notTouchingAbsoluteSlope) {
                bounceCircle(bigCollisionRadius);
            } else if (belowAbsolute) {
                bounceWall(posRelative, holeWidth, r);
            }
            if (inside&&!this.inside&&!outsideComplex) {
                this.inside = true;
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.GENERIC_EAT, 1.0F));
                this.onPress();
            }
        } else {
            if (this.inside) {
                this.inside = false;
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_TRUMPET_OXIDIZED.value(), 0.5f, 2f));
            }
        }
    }

    private void bounceCircle(float bigCollisionRadius) {
        Vec2 normal = pos.add(NukebobConfigScreen.CENTER_POS.scale(-1)).normalized();
        setVel(vel.add(normal.scale(-2f * (vel.dot(normal)))).scale(0.8f));
        setPos(NukebobConfigScreen.CENTER_POS.add(normal.scale(bigCollisionRadius + 1)));
    }

    private void bounceAbsolute(Vec2 posRelative, float r, float distToAbsolute) {
        boolean left = posRelative.x > 0;
        Vec2 normal = new Vec2(left ? 1f : -1f, -1).normalized();

        Vec2 incomingVel = vel.normalized();

        float dot = vel.dot(normal);
        if (dot < 0) {
            setVel(vel.add(normal.scale(-1.8f * dot)));
        }
        //move back along velocity until hit absolute
        addPos(incomingVel.scale((r - distToAbsolute) / dot));
    }

    private void bounceWall(Vec2 posRelative, float holeWidth, float r) {
        boolean left = posRelative.x > 0;
        setPosX(NukebobConfigScreen.CENTER_POS.x+(left?-1f:1f)*(holeWidth*r));
        setVel(new Vec2(vel.x*-0.8f, vel.y));
    }

    public Vec2 physicsStep(int steps, float littleNukeScale, float width, float height, Vec2 newVel) {
        Vec2 pos = this.pos;
        Vec2 vel = newVel;
        float delta = 0.5f;

        for (int i = 0; i < steps; i++) {
            //radius
            float r = getCollisionRadius(littleNukeScale);

            //gravity
            float gravity = 3f;
            vel = vel.add(new Vec2(0, gravity * delta));
            //apply velocity
            pos = pos.add(vel.scale(delta));
            //wall bounce
            float wallRestitution = 0.8f;
            if (pos.y + r > height) {
                vel = new Vec2(vel.x, vel.y * -1 * wallRestitution);
            }
            if (pos.y - r < 0) {
                vel = new Vec2(vel.x, vel.y * -1 * wallRestitution);
            }
            if (pos.x + r > width) {
                vel = new Vec2(vel.x * -1 * wallRestitution, vel.y);
            }
            if (pos.x - r < 0) {
                vel = new Vec2(vel.x * -1 * wallRestitution, vel.y);
            }
            pos = new Vec2(Mth.clamp(pos.x, r, width - r), Mth.clamp(pos.y, r, height - r));
            //floor friction
            if (pos.y + r > height - r - 3) {
                vel = new Vec2(vel.x * 0.975f, vel.y);
            }
            //big nukebob collision
            float R = NukebobConfigScreen.CENTER_RADIUS;
            Vec2 posRelative = NukebobConfigScreen.CENTER_POS.add(pos.scale(-1));
            float distToBigOne = posRelative.length();
            float bigCollisionRadius = R + r;

            float holeWidth = 0.45f;
            if (distToBigOne < bigCollisionRadius) { //first check big circle
                float distToAbsolute = (posRelative.x * (posRelative.x < 0 ? -1 : 1) - posRelative.y) / Mth.sqrt(2);
                boolean outsideComplex = posRelative.y - r < -0.5f * R || //below bottom
                        Mth.abs(posRelative.x) > R / Mth.sqrt(2) - r; //outside absolute slope
                boolean touchingTopCorner = new Vec2(holeWidth * R, holeWidth * R).distanceToSqr(new Vec2(Mth.abs(posRelative.x), posRelative.y)) < r * r;
                boolean belowAbsolute = (Mth.abs(posRelative.x) > holeWidth * R - r && posRelative.y + r < holeWidth * R) && (touchingTopCorner || posRelative.y < holeWidth * R);
                boolean absoluteSlope = -(Mth.abs(posRelative.x) - posRelative.y) / Mth.sqrt(2) < r && Mth.abs(posRelative.x) > holeWidth * R - r;
                boolean notTouchingAbsoluteSlope = posRelative.y - r < holeWidth * R;
                boolean inside = posRelative.y<r && Mth.abs(posRelative.x)+r<holeWidth*R;

                if (absoluteSlope && !belowAbsolute) {
                    if (outsideComplex) {
                        if (Mth.abs(posRelative.add(vel.scale(-delta)).x) < R / Mth.sqrt(2f)) {
                            boolean left = posRelative.x > 0;
                            Vec2 normal = new Vec2(left ? 1f : -1f, -1).normalized();
                            Vec2 incomingVel = vel.normalized();
                            float dot = vel.dot(normal);
                            if (dot < 0) {
                                vel = vel.add(normal.scale(-1.8f * dot));
                            }
                            pos = pos.add(incomingVel.scale((r - distToAbsolute) / dot));
                        } else {
                            Vec2 normal = pos.add(NukebobConfigScreen.CENTER_POS.scale(-1)).normalized();
                            vel = vel.add(normal.scale(-2f * (vel.dot(normal)))).scale(0.8f);
                            pos = NukebobConfigScreen.CENTER_POS.add(normal.scale(bigCollisionRadius + 1));
                        }
                    } else {
                        boolean left = posRelative.x > 0;
                        Vec2 normal = new Vec2(left ? 1f : -1f, -1).normalized();
                        Vec2 incomingVel = vel.normalized();
                        float dot = vel.dot(normal);
                        if (dot < 0) {
                            vel = vel.add(normal.scale(-1.8f * dot));
                        }
                        pos = pos.add(incomingVel.scale((r - distToAbsolute) / dot));
                    }
                } else if (outsideComplex && notTouchingAbsoluteSlope) {
                    Vec2 normal = pos.add(NukebobConfigScreen.CENTER_POS.scale(-1)).normalized();
                    vel = vel.add(normal.scale(-2f * (vel.dot(normal)))).scale(0.8f);
                    pos = NukebobConfigScreen.CENTER_POS.add(normal.scale(bigCollisionRadius + 1));
                } else if (belowAbsolute) {
                    boolean left = posRelative.x > 0;
                    pos = new Vec2(NukebobConfigScreen.CENTER_POS.x + (left ? -1f : 1f) * (holeWidth * R - r), pos.y);
                    vel = new Vec2(vel.x * -0.8f, vel.y);
                }
                if (inside&&!this.inside&&!outsideComplex) {
                    return new Vec2(-1,-1);
                }
            }
        }
        return pos;
    }

    public void pushOutOfBigNukebob(float littleNukeScale) {
        float r = getCollisionRadius(littleNukeScale);
        float distToBigOne = NukebobConfigScreen.CENTER_POS.add(pos.scale(-1)).length();
        float bigCollisionRadius = NukebobConfigScreen.CENTER_RADIUS + r;

        if (distToBigOne < bigCollisionRadius) {
            Vec2 normal = pos.add(NukebobConfigScreen.CENTER_POS.scale(-1)).normalized();
            setPos(NukebobConfigScreen.CENTER_POS.add(normal.scale(bigCollisionRadius + 1f)));
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

    public void onPress() {
        this.onPress.accept(this);
    }
}
