package visualparticle.better.client.module.impl.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DestFactor;
import com.mojang.blaze3d.platform.SourceFactor;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.math.Axis;
import visualparticle.better.client.ClientCore;
import visualparticle.better.client.ClientTickEvent;
import visualparticle.better.client.event.AttackTargetEvent;
import visualparticle.better.client.event.WorldRenderEvent;
import visualparticle.better.client.module.Module;
import visualparticle.better.client.module.ModuleCategory;
import visualparticle.better.client.setting.BooleanSetting;
import visualparticle.better.client.setting.ColorSetting;
import visualparticle.better.client.setting.Setting;
import visualparticle.better.client.ui.UiLanguage;
import visualparticle.better.client.setting.ModeSetting;
import visualparticle.better.client.setting.MultiBooleanSetting;
import visualparticle.better.client.setting.NumberSetting;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;

public final class ParticlesModule extends Module {
    private static final Identifier SHADER=Identifier.fromNamespaceAndPath("minecraft","core/position_tex_color");
    private static final RenderPipeline PIPELINE=RenderPipelines.register(RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET).withLocation(Identifier.fromNamespaceAndPath("visualparticle-better","pipeline/visual_particles")).withVertexShader(SHADER).withFragmentShader(SHADER).withSampler("Sampler0").withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR,VertexFormat.Mode.QUADS).withBlend(new BlendFunction(SourceFactor.SRC_ALPHA,DestFactor.ONE)).withCull(false).withDepthWrite(false).build());
    private static final Map<String,RenderType>TYPES=new HashMap<>();private static final Gson GSON=new GsonBuilder().setPrettyPrinting().create();private static final Path CONFIG=FabricLoader.getInstance().getConfigDir().resolve("visualparticle-better.json");
    private final ModeSetting uiLanguage=new ModeSetting("Interface language","ClickGUI language","RU","RU","EN");
    private final BooleanSetting master=new BooleanSetting("Master switch","Enable the particle system",true);
    private final BooleanSetting attack=new BooleanSetting("Attack particles","Spawn on hit",true),totem=new BooleanSetting("Totem particles","Spawn on totem pop",true),walk=new BooleanSetting("Walk particles","Spawn while moving",true),elytra=new BooleanSetting("Elytra particles","Spawn while flying",true),projectiles=new BooleanSetting("Projectile particles","Spawn on owned projectiles",true),world=new BooleanSetting("World particles","Ambient particles",true);
    private final MultiBooleanSetting triggers=new MultiBooleanSetting("Triggers","Particle sources",attack,totem,walk,elytra,projectiles);
    private final ModeSetting colorPreset=new ModeSetting("Color preset","Built-in palette","Default","Default","Blue","Red","Purple","Pink","Cyan","Green","Gold","Orange","Lime","Ice","Fire","Galaxy","White","Random");
    private final ModeSetting shape=new ModeSetting("Particle shape","Particle sprite","Star","Star","Star Alt","Snowflake","Dollar","Heart","Line","Lightning","Triangle","Rhombus","Crown","Cross","Arrow","Pearl Mark","Cube Blast","Dash Bloom");
    private final ModeSetting worldShape=new ModeSetting("World shape","Ambient particle sprite","Star","Star","Star Alt","Snowflake","Dollar","Heart","Line","Lightning","Triangle","Rhombus","Crown","Cross","Arrow","Pearl Mark","Cube Blast","Dash Bloom");
    private final ModeSetting glowMode=new ModeSetting("Glow mode","Original bloom rendering","Both","Bloom","Bloom Sample","Both");
    private final ModeSetting activePreset=new ModeSetting("Active preset","Quick settings profile","Custom","Custom","PvP","Minimal","Cinematic");
    private final NumberSetting spread=new NumberSetting("Spread","Spawn spread",1,.2f,3,.1f,"x"),speed=new NumberSetting("Speed","Velocity multiplier",2,.1f,4,.1f,"x"),lifeTime=new NumberSetting("Life","Particle lifetime",2.5f,.3f,8,.1f,"s"),size=new NumberSetting("Size","Particle scale",1,.2f,2,.05f,"x"),glow=new NumberSetting("Glow","Glow size",7.5f,.5f,12,.1f,"x");
    private final NumberSetting attackAmount=new NumberSetting("Attack amount","Particles per hit",40,10,80,1,""),walkAmount=new NumberSetting("Walk amount","Movement density",30,0,60,1,""),worldAmount=new NumberSetting("World amount","Ambient density",100,10,500,5,"");
    private final BooleanSetting physics=new BooleanSetting("Particle physics","Gravity and block collision",true),worldPhysics=new BooleanSetting("World physics","Physics for ambient particles",false),customColors=new BooleanSetting("Custom particle colors","Use the colors below",false),animatedGradient=new BooleanSetting("Animated gradients","Animate primary and secondary colors",true);
    private final NumberSetting worldLifeTime=new NumberSetting("World life","Ambient particle lifetime",10,2,60,.5f,"s"),worldSize=new NumberSetting("World size","Ambient particle scale",1.5f,.1f,1.5f,.05f,"x"),worldGlow=new NumberSetting("World glow","Ambient glow size",3,.1f,8,.1f,"x");
    private final ColorSetting uiPrimary=new ColorSetting("GUI primary","Primary interface color",0xFF68AEFF),uiSecondary=new ColorSetting("GUI secondary","Secondary interface color",0xFFFF6DA4);
    private final ColorSetting attackColor=new ColorSetting("Attack primary","Primary attack color",0xFFFF6D78),attackSecondColor=new ColorSetting("Attack gradient","Secondary attack color",0xFFFFC371),moveColor=new ColorSetting("Move primary","Primary movement color",0xFF68AEFF),moveSecondColor=new ColorSetting("Move gradient","Secondary movement color",0xFFB46DFF),projectileColor=new ColorSetting("Projectile primary","Primary projectile color",0xFFFFD166),projectileSecondColor=new ColorSetting("Projectile gradient","Secondary projectile color",0xFFFF6DA4),elytraColor=new ColorSetting("Elytra primary","Primary elytra color",0xFF75F4FF),elytraSecondColor=new ColorSetting("Elytra gradient","Secondary elytra color",0xFF68AEFF),worldColor=new ColorSetting("World primary","Primary world color",0xFF8DFFB3),worldSecondColor=new ColorSetting("World gradient","Secondary world color",0xFF68AEFF),totemColor=new ColorSetting("Totem primary","Primary totem color",0xFF7CFC00),totemSecondColor=new ColorSetting("Totem gradient","Secondary totem color",0xFFFFD700);
    private final List<Particle>particlesList=new ArrayList<>();private Vec3 lastPlayer=Vec3.ZERO;private long ticks;private String appliedPreset="Custom";
    public ParticlesModule(){super("particles","Particles","Visual Particle settings",0xE7B0,ModuleCategory.RENDER,true);addSettings(master,triggers,colorPreset,shape,glowMode,spread,speed,lifeTime,size,glow,attackAmount,walkAmount,world,worldPhysics,worldShape,worldAmount,worldLifeTime,worldSize,worldGlow,uiPrimary,uiSecondary,customColors,animatedGradient,attackColor,attackSecondColor,moveColor,moveSecondColor,projectileColor,projectileSecondColor,elytraColor,elytraSecondColor,worldColor,worldSecondColor,totemColor,totemSecondColor,activePreset,uiLanguage);loadConfig();UiLanguage.use(uiLanguage.value());}
    @Override protected void onEnable(){
        particlesList.clear();
        track(ClientCore.getInstance().events().subscribe(ClientTickEvent.class,this::tick));
        track(ClientCore.getInstance().events().subscribe(AttackTargetEvent.class,e->{if(master.enabled()&&attack.enabled())spawnBurst(e.target().position().add(0,e.target().getBbHeight()*.5,0),Math.round(attackAmount.value()),.12,Role.ATTACK); }));
        track(ClientCore.getInstance().events().subscribe(WorldRenderEvent.class,this::render));
    }
    @Override protected void onDisable(){particlesList.clear();}

    public List<Setting<?>> settingsFor(String section){
        return switch(section){
            case "General"->List.of(master,colorPreset,shape,glowMode,spread,speed,lifeTime,size,glow,physics);
            case "Events"->List.of(triggers,attackAmount,walkAmount);
            case "World"->List.of(world,worldPhysics,worldShape,worldAmount,worldLifeTime,worldSize,worldGlow);
            case "Colors"->List.of(uiPrimary,uiSecondary,customColors,animatedGradient,attackColor,attackSecondColor,moveColor,moveSecondColor,projectileColor,projectileSecondColor,elytraColor,elytraSecondColor,worldColor,worldSecondColor,totemColor,totemSecondColor);
            case "Presets"->List.of(activePreset);
            default->List.of();
        };
    }

    private void tick(ClientTickEvent ignored){
        Minecraft mc=Minecraft.getInstance();ticks++;
        if(!activePreset.value().equals(appliedPreset)){applyPreset(activePreset.value());appliedPreset=activePreset.value();}
        if(!master.enabled()||mc.player==null||mc.level==null){particlesList.clear();return;}
        Vec3 current=mc.player.position(),motion=current.subtract(lastPlayer);lastPlayer=current;
        if(walk.enabled()&&walkAmount.value()>0&&motion.horizontalDistanceSqr()>.0006&&ticks%Math.max(1,Math.round(180/walkAmount.value()))==0)
            spawn(current.add(random(-.25,.25),.1,random(-.25,.25)),new Vec3(-motion.x*.12+random(-.025,.025),.035,-motion.z*.12+random(-.025,.025)),.6f,Role.MOVE,false);
        if(elytra.enabled()&&mc.player.isFallFlying()&&ticks%2==0)
            spawn(current.add(random(-.35,.35),.7+random(-.15,.15),random(-.35,.35)),motion.scale(-.12).add(random(-.02,.02),random(-.01,.02),random(-.02,.02)),.7f,Role.ELYTRA,false);
        if(projectiles.enabled()&&ticks%2==0)for(Entity entity:mc.level.entitiesForRendering())if(entity instanceof Projectile p&&p.getOwner()==mc.player&&!entity.onGround())
            spawn(entity.position(),entity.getDeltaMovement().scale(-.08).add(random(-.015,.015),random(-.01,.01),random(-.015,.015)),.48f,Role.PROJECTILE,false);
        int worldDelay=Math.max(1,Math.round(300/worldAmount.value()));
        if(world.enabled()&&ticks%worldDelay==0)
            spawn(current.add(random(-12*spread.value(),12*spread.value()),random(.5,7),random(-12*spread.value(),12*spread.value())),new Vec3(random(-.015,.015),random(-.004,.018),random(-.015,.015)),worldSize.value(),Role.WORLD,true);
        Iterator<Particle>it=particlesList.iterator();while(it.hasNext()){Particle p=it.next();p.update(mc,p.world?worldPhysics.enabled():physics.enabled());if(p.dead())it.remove();}
        int cap=Math.max(240,Math.round(worldAmount.value()*1.5f));while(particlesList.size()>cap)particlesList.removeFirst();
    }
    private void spawnBurst(Vec3 center,int amount,double velocity,Role role){for(int i=0;i<amount;i++)spawn(center,new Vec3(random(-velocity,velocity),random(.035,velocity*1.4),random(-velocity,velocity)),1,role,false);}
    public void spawnTotem(Entity entity){if(enabled()&&master.enabled()&&totem.enabled())spawnBurst(entity.position().add(0,entity.getBbHeight()*.55,0),48,.16,Role.TOTEM);}
    private void spawn(Vec3 pos,Vec3 velocity,float scale,Role role,boolean ambient){
        float actualSize=ambient?worldSize.value():size.value()*scale;long actualLife=(long)((ambient?worldLifeTime.value():lifeTime.value())*1000);
        particlesList.add(new Particle(pos,velocity.scale(speed.value()),particleColor(role),actualSize,actualLife,texture(ambient?worldShape.value():shape.value()),ambient));
    }
    private void render(WorldRenderEvent event){
        if(!master.enabled()||particlesList.isEmpty())return;
        float partialTick=Mth.clamp(event.partialTick(),0,1);
        Vec3 camera=event.context().gameRenderer().getMainCamera().position();PoseStack stack=event.context().matrices();
        boolean bloom=glowMode.is("Bloom")||glowMode.is("Both"),bloomSample=glowMode.is("Bloom Sample")||glowMode.is("Both");
        for(Particle p:particlesList){
            Vec3 rendered=p.interpolated(partialTick);float alpha=p.alpha();float textureSize=p.scale*.5f;
            billboard(stack,event.context().consumers().getBuffer(type(p.texture)),rendered.subtract(camera),textureSize,p.rotation,withAlpha(p.color,(int)(alpha*255)));
        }
        if(bloom){
            VertexConsumer out=event.context().consumers().getBuffer(type("dashbloom"));
            for(Particle p:particlesList){float alpha=p.alpha(),textureSize=p.scale*.5f,glowScale=p.world?worldGlow.value():glow.value();billboard(stack,out,p.interpolated(partialTick).subtract(camera),textureSize*glowScale*.5f,0,withAlpha(p.color,(int)(80*alpha)));}
        }
        if(bloomSample){
            VertexConsumer out=event.context().consumers().getBuffer(type("dashbloomsample"));
            for(Particle p:particlesList){float alpha=p.alpha(),textureSize=p.scale*.5f,glowScale=p.world?worldGlow.value():glow.value();billboard(stack,out,p.interpolated(partialTick).subtract(camera),textureSize*glowScale*.2f,0,withAlpha(p.color,(int)(140*alpha)));}
        }
    }    private String texture(String value){return switch(value){case"Dollar"->"dollar";case"Heart"->"heart";case"Snowflake"->"snowflake";case"Star"->"star";case"Star Alt"->"star1";case"Line"->"line";case"Lightning"->"lightning";case"Triangle"->"triangle";case"Rhombus"->"rhombus";case"Crown"->"crown";case"Cross"->"cross";case"Arrow"->"arrow";case"Pearl Mark"->"pearl-mark";case"Cube Blast"->"cubeblast1";case"Dash Bloom"->"dashbloom";default->"glow";};}
    private int particleColor(Role role){
        if(customColors.enabled()){int a=primary(role),b=secondary(role);float p=animatedGradient.enabled()?(float)(.5+.5*Math.sin(System.currentTimeMillis()/420.0)):ThreadLocalRandom.current().nextFloat();return blend(a,b,p);}
        return switch(colorPreset.value()){case"Blue"->0xFF4A90E2;case"Red"->0xFFC54A39;case"Purple"->0xFF9B6DFF;case"Pink"->0xFFFF6DA4;case"Cyan"->0xFF75F4FF;case"Green"->0xFF68E39A;case"Gold"->0xFFFFD166;case"Orange"->0xFFFF8C42;case"Lime"->0xFF7CFC00;case"Ice"->0xFFBDEBFF;case"Fire"->blend(0xFFFF3D3D,0xFFFFC14D,ThreadLocalRandom.current().nextFloat());case"Galaxy"->blend(0xFF6D5BFF,0xFFFF5BD6,ThreadLocalRandom.current().nextFloat());case"White"->0xFFFFFFFF;case"Random"->0xFF000000|ThreadLocalRandom.current().nextInt(0x1000000);default->0xFF896148;};
    }
    private int primary(Role role){return switch(role){case ATTACK->attackColor.argb();case MOVE->moveColor.argb();case PROJECTILE->projectileColor.argb();case ELYTRA->elytraColor.argb();case WORLD->worldColor.argb();case TOTEM->totemColor.argb();};}
    private int secondary(Role role){return switch(role){case ATTACK->attackSecondColor.argb();case MOVE->moveSecondColor.argb();case PROJECTILE->projectileSecondColor.argb();case ELYTRA->elytraSecondColor.argb();case WORLD->worldSecondColor.argb();case TOTEM->totemSecondColor.argb();};}
    private static int blend(int a,int b,float p){int r=Math.round(Mth.lerp(p,a>>16&255,b>>16&255)),g=Math.round(Mth.lerp(p,a>>8&255,b>>8&255)),bl=Math.round(Mth.lerp(p,a&255,b&255));return 0xFF000000|r<<16|g<<8|bl;}
    private void applyPreset(String preset){
        if("PvP".equals(preset)){colorPreset.set("Red");customColors.set(true);attack.set(true);totem.set(true);walk.set(false);elytra.set(true);projectiles.set(true);world.set(false);shape.set("Lightning");glowMode.set("Both");attackAmount.set(48f);spread.set(.9f);speed.set(2.6f);lifeTime.set(1.3f);size.set(.85f);glow.set(8.5f);attackColor.set(0xFFFF4D5E);attackSecondColor.set(0xFFFFB65C);}
        else if("Minimal".equals(preset)){colorPreset.set("Default");customColors.set(true);attack.set(true);totem.set(true);walk.set(false);elytra.set(false);projectiles.set(false);world.set(false);shape.set("Line");glowMode.set("Bloom");attackAmount.set(16f);spread.set(.45f);speed.set(1.2f);lifeTime.set(.8f);size.set(.45f);glow.set(3f);}
        else if("Cinematic".equals(preset)){colorPreset.set("Blue");customColors.set(true);attack.set(true);totem.set(true);walk.set(true);elytra.set(true);projectiles.set(true);world.set(true);shape.set("Star Alt");worldShape.set("Snowflake");glowMode.set("Both");attackAmount.set(58f);walkAmount.set(28f);worldAmount.set(180f);spread.set(1.35f);speed.set(1.75f);lifeTime.set(3.8f);size.set(1.15f);glow.set(10f);worldLifeTime.set(16f);worldSize.set(1.35f);worldGlow.set(4.6f);}
    }
    public String languageCode(){return uiLanguage.value();}
    public void toggleLanguage(){uiLanguage.set(uiLanguage.is("RU")?"EN":"RU");UiLanguage.use(uiLanguage.value());saveConfig();}
    public int uiPrimaryColor(){return uiPrimary.argb();}
    public int uiSecondaryColor(){return uiSecondary.argb();}
    public void saveConfig(){
        JsonObject root=new JsonObject();
        for(Setting<?> setting:settings()){
            if(setting instanceof BooleanSetting value)root.addProperty(setting.name(),value.enabled());
            else if(setting instanceof NumberSetting value)root.addProperty(setting.name(),value.value());
            else if(setting instanceof ModeSetting value)root.addProperty(setting.name(),value.value());
            else if(setting instanceof ColorSetting value)root.addProperty(setting.name(),value.argb());
            else if(setting instanceof MultiBooleanSetting value){JsonObject group=new JsonObject();for(BooleanSetting child:value.values())group.addProperty(child.name(),child.enabled());root.add(setting.name(),group);}
        }
        try{Files.createDirectories(CONFIG.getParent());Files.writeString(CONFIG,GSON.toJson(root),StandardCharsets.UTF_8);}catch(Exception e){ClientCore.LOGGER.warn("Could not save particle settings",e);}
    }
    private void loadConfig(){
        if(!Files.isRegularFile(CONFIG))return;
        try{JsonObject root=GSON.fromJson(Files.readString(CONFIG,StandardCharsets.UTF_8),JsonObject.class);if(root==null)return;for(Setting<?> setting:settings()){JsonElement raw=root.get(setting.name());if(raw==null)continue;if(setting instanceof BooleanSetting value)value.set(raw.getAsBoolean());else if(setting instanceof NumberSetting value)value.set(raw.getAsFloat());else if(setting instanceof ModeSetting value){String mode=raw.getAsString();if(value.modes().contains(mode))value.set(mode);}else if(setting instanceof ColorSetting value)value.set(raw.getAsInt());else if(setting instanceof MultiBooleanSetting value&&raw.isJsonObject()){JsonObject group=raw.getAsJsonObject();for(BooleanSetting child:value.values())if(group.has(child.name()))child.set(group.get(child.name()).getAsBoolean());}}}catch(Exception e){ClientCore.LOGGER.warn("Could not load particle settings",e);}
    }
    private enum Role{ATTACK,MOVE,PROJECTILE,ELYTRA,WORLD,TOTEM}    private static RenderType type(String texture){return TYPES.computeIfAbsent(texture,key->RenderType.create("visualparticlebetter_particle_"+key,RenderSetup.builder(PIPELINE).withTexture("Sampler0",Identifier.fromNamespaceAndPath("visualparticle-better","textures/particles/"+key+".png")).sortOnUpload().createRenderSetup()));}
    private static void billboard(PoseStack s,VertexConsumer o,Vec3 p,float size,float rotation,int color){s.pushPose();s.translate(p.x,p.y,p.z);s.mulPose(Minecraft.getInstance().gameRenderer.getMainCamera().rotation());s.mulPose(Axis.ZP.rotationDegrees(rotation));float h=size*.5f;PoseStack.Pose pose=s.last();o.addVertex(pose,-h,-h,0).setUv(0,0).setColor(color);o.addVertex(pose,h,-h,0).setUv(1,0).setColor(color);o.addVertex(pose,h,h,0).setUv(1,1).setColor(color);o.addVertex(pose,-h,h,0).setUv(0,1).setColor(color);s.popPose();}
    private static double random(double a,double b){return ThreadLocalRandom.current().nextDouble(a,b);}private static int withAlpha(int c,int a){return c&0xFFFFFF|Mth.clamp(a,0,255)<<24;}
    private static final class Particle{
        Vec3 previousPosition,position,velocity;final int color;final float scale;float rotation;final long born,life;long fadeOutStart=-1;final String texture;final boolean world;
        Particle(Vec3 p,Vec3 v,int c,float s,long l,String t,boolean w){previousPosition=position=p;velocity=v;color=c;scale=s;life=l;texture=t;world=w;born=System.currentTimeMillis();rotation=ThreadLocalRandom.current().nextFloat()*360;}
        void update(Minecraft mc,boolean physics){
            previousPosition=position;
            if(physics){velocity=velocity.add(0,-.0004,0);Vec3 next=position.add(velocity);if(mc.level!=null&&!mc.level.getBlockState(net.minecraft.core.BlockPos.containing(next)).isAir())velocity=new Vec3(velocity.x*.72,Math.abs(velocity.y)*.55,velocity.z*.72);else position=next;}else position=position.add(velocity);
            velocity=velocity.scale(.99);rotation+=2;
            long now=System.currentTimeMillis();if(fadeOutStart<0&&now-born>life)fadeOutStart=now;
        }
        Vec3 interpolated(float partialTick){return previousPosition.lerp(position,partialTick);}
        boolean dead(){return fadeOutStart>=0&&alpha()<=.001f;}
        float alpha(){long now=System.currentTimeMillis();float in=ease(Mth.clamp((now-born)/150f,0,1));if(fadeOutStart<0)return in;return Math.min(in,1-ease(Mth.clamp((now-fadeOutStart)/250f,0,1)));}
        private static float ease(float v){return v<.5f?2*v*v:1-(float)Math.pow(-2*v+2,2)/2;}
    }
}