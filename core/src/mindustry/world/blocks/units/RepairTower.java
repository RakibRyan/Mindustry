package mindustry.world.blocks.units;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.struct.*;
import arc.util.*;
import mindustry.entities.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.logic.*;
import mindustry.world.*;

import static mindustry.Vars.*;

public class RepairTower extends Block{
    static final float refreshInterval = 6f;

    public float range = 80f;
    public Color circleColor = Pal.heal;
    public float circleSpeed = 120f, circleStroke = 3f;
    public float healAmount = 1f;

    public RepairTower(String name){
        super(name);
        update = true;
        solid = true;
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid){
        super.drawPlace(x, y, rotation, valid);

        Drawf.dashCircle(x * tilesize + offset, y * tilesize + offset, range, Pal.placing);
    }

    public class RepairTowerBuild extends Building implements Ranged{
        public float refresh = Mathf.random(refreshInterval);
        public float warmup = 0f;
        public float totalProgress = 0f;
        public Seq<Unit> targets = new Seq<>();

        @Override
        public void updateTile(){

            if(potentialEfficiency > 0 && (refresh += Time.delta) >= refreshInterval){
                targets.clear();
                refresh = 0f;
                Units.nearby(team, x, y, range, u -> {
                    if(u.damaged()){
                        targets.add(u);
                    }
                });
            }

            boolean any = false;
            if(efficiency > 0){
                for(var target : targets){
                    if(target.damaged()){
                        target.heal(healAmount * efficiency);
                        any = true;
                    }
                }
            }

            warmup = Mathf.lerpDelta(warmup, any ? efficiency : 0f, 0.1f);
            totalProgress += Time.delta / circleSpeed;
        }

        @Override
        public boolean shouldConsume(){
            return targets.size > 0;
        }

        @Override
        public void draw(){
            super.draw();

            Draw.z(Layer.effect);
            float mod = totalProgress % 1f;
            Draw.color(circleColor);
            Lines.stroke(circleStroke * (1f - mod));
            Lines.circle(x, y, range * mod);
            Draw.reset();
        }

        @Override
        public float range(){
            return range;
        }

        @Override
        public float warmup(){
            return warmup;
        }
    }
}
