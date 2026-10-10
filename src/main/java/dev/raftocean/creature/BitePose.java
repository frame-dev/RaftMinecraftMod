package dev.raftocean.creature;

/** Cosmetic keyframes shared with regression checks; never applies damage or moves a collider. */
public final class BitePose {
    public static final int WINDUP_TICKS=40;
    public static final int RECOVERY_TICKS=12;
    public record Sample(float jaw,float thrust,float pitch,float tailBoost) { }
    private BitePose() { }
    private static float ease(double t){t=Math.clamp(t,0,1);return (float)(t*t*(3-2*t));}
    public static Sample sample(float windup,double impactAge) {
        float anticipation=ease(windup/30.0);
        float jaw=42*anticipation;
        float thrust=-.035F*anticipation,pitch=-3*anticipation,tail=1+anticipation*.3F;
        if(impactAge>=0 && impactAge<RECOVERY_TICKS){
            jaw=impactAge<3?42*(1-ease(impactAge/3)):0;
            if(impactAge>=6){jaw=(float)Math.sin((impactAge-6)/6*Math.PI)*3;}
            float snap=(float)Math.sin(Math.clamp(impactAge/4,0,1)*Math.PI);
            float recoil=(float)Math.sin(Math.clamp((impactAge-4)/8,0,1)*Math.PI);
            thrust=.18F*snap-.08F*recoil;
            pitch=6*snap-4*recoil;
            tail=1+.6F*snap;
        }
        return new Sample(jaw,thrust,pitch,tail);
    }
}
