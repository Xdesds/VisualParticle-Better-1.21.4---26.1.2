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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.ThreadLocalRandom;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.arrow.ThrownTrident;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;

public final class ParticlesModule extends Module {
    private static final Identifier SHADER=Identifier.fromNamespaceAndPath("minecraft","core/position_tex_color");
    private static final RenderPipeline PIPELINE=RenderPipelines.register(RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET).withLocation(Identifier.fromNamespaceAndPath("visualparticle-better","pipeline/visual_particles")).withVertexShader(SHADER).withFragmentShader(SHADER).withSampler("Sampler0").withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR,VertexFormat.Mode.QUADS).withBlend(new BlendFunction(SourceFactor.SRC_ALPHA,DestFactor.ONE)).withCull(false).withDepthWrite(false).build());
    private static final Map<String,RenderType>TYPES=new HashMap<>();private static final Gson GSON=new GsonBuilder().setPrettyPrinting().create();private static final Path CONFIG=FabricLoader.getInstance().getConfigDir().resolve("visualparticle-better.json");
    private final ModeSetting uiLanguage=new ModeSetting("Interface language","ClickGUI language","RU","RU","EN");
    private final BooleanSetting master=new BooleanSetting("Master switch","Enable the particle system",true);
    private final BooleanSetting attack=new BooleanSetting("Hit particles","Spawn on hit",true),critical=new BooleanSetting("Critical particles","Extra burst on critical hit",true),damage=new BooleanSetting("Damage particles","Damage burst around target",true),death=new BooleanSetting("Death particles","Burst when target dies",true);
    private final BooleanSetting walk=new BooleanSetting("Walk particles","Spawn while walking",true),sprint=new BooleanSetting("Sprint particles","Spawn while sprinting",true),jump=new BooleanSetting("Jump particles","Spawn on jump",true),elytra=new BooleanSetting("Elytra particles","Spawn while flying",true);
    private final BooleanSetting arrowTrail=new BooleanSetting("Arrow trail","Trail behind your arrows",true),tridentTrail=new BooleanSetting("Trident trail","Trail behind your tridents",true),fireworkTrail=new BooleanSetting("Firework trail","Trail behind your fireworks",true);
    private final BooleanSetting blockBreak=new BooleanSetting("Block break","Burst when a block breaks",true),blockPlace=new BooleanSetting("Block place","Burst when a block is placed",true),blockHit=new BooleanSetting("Block hit","Particles while mining",true);
    private final BooleanSetting totem=new BooleanSetting("Totem pop","Spawn on totem pop",true),itemPickup=new BooleanSetting("Item pickup","Spawn when collecting an item",true),world=new BooleanSetting("Ambient world","Ambient particles",false);
    private final ModeSetting colorPreset=new ModeSetting("Color preset","Built-in palette","Default","Default","Blue","Red","Purple","Pink","Cyan","Green","Gold","Orange","Lime","Ice","Fire","Galaxy","White","Random");
    private final ModeSetting shape=new ModeSetting("Particle shape","Particle sprite","Star","Star","Star Alt","Snowflake","Dollar","Heart","Line","Lightning","Triangle","Rhombus","Crown","Cross","Arrow","Pearl Mark","Cube Blast","Dash Bloom");
    private final ModeSetting criticalShape=new ModeSetting("Critical shape","Critical hit sprite","Rhombus","Star","Star Alt","Heart","Lightning","Rhombus","Crown","Cross","Cube Blast"),movementShape=new ModeSetting("Movement shape","Walk and sprint sprite","Star","Star","Star Alt","Snowflake","Heart","Line","Rhombus"),jumpShape=new ModeSetting("Jump shape","Jump burst sprite","Star Alt","Star","Star Alt","Snowflake","Heart","Rhombus","Crown"),projectileShape=new ModeSetting("Projectile shape","Projectile trail sprite","Arrow","Arrow","Line","Lightning","Rhombus","Star Alt"),blockShape=new ModeSetting("Block shape","Block interaction sprite","Cube Blast","Cube Blast","Rhombus","Line","Star","Cross"),itemShape=new ModeSetting("Item shape","Item effect sprite","Crown","Crown","Star","Heart","Dollar","Rhombus");
    private final ModeSetting worldShape=new ModeSetting("World shape","Ambient particle sprite","Star","Star","Star Alt","Snowflake","Dollar","Heart","Line","Lightning","Triangle","Rhombus","Crown","Cross","Arrow","Pearl Mark","Cube Blast","Dash Bloom");
    private final ModeSetting glowMode=new ModeSetting("Glow mode","Original bloom rendering","Both","Bloom","Bloom Sample","Both");
    private final ModeSetting renderMode=new ModeSetting("Render mode","Flat or volumetric sprites","2D Sprites","2D Sprites","3D Cross"),physicsMode=new ModeSetting("Physics mode","Particle movement style","Classic","Classic","Storm","Vortex","Zero Gravity","Magnetic");
    private final ModeSetting activePreset=new ModeSetting("Active preset","Quick settings profile","Purple PvP","Custom","Purple PvP","Minimal","Cinematic","Storm","Arcane");
    private final NumberSetting spread=new NumberSetting("Spread","Spawn spread",1,.2f,3,.1f,"x"),speed=new NumberSetting("Speed","Velocity multiplier",2,.1f,4,.1f,"x"),lifeTime=new NumberSetting("Life","Particle lifetime",2.5f,.3f,8,.1f,"s"),size=new NumberSetting("Size","Particle scale",1,.2f,2,.05f,"x"),glow=new NumberSetting("Glow","Glow size",7.5f,.5f,12,.1f,"x");
    private final NumberSetting attackAmount=new NumberSetting("Hit amount","Particles per hit",22,2,80,1,""),criticalAmount=new NumberSetting("Critical amount","Particles per critical",28,2,90,1,""),deathAmount=new NumberSetting("Death amount","Particles per death",48,4,120,1,"");
    private final NumberSetting walkAmount=new NumberSetting("Walk density","Walking density",10,0,60,1,"/s"),sprintAmount=new NumberSetting("Sprint density","Sprinting density",18,0,80,1,"/s"),jumpAmount=new NumberSetting("Jump amount","Particles per jump",14,1,50,1,""),projectileDensity=new NumberSetting("Trail density","Projectile trail density",3,1,6,1,"x"),blockAmount=new NumberSetting("Block amount","Block effect amount",18,2,60,1,""),worldAmount=new NumberSetting("Ambient amount","Ambient density",80,10,300,5,"");
    private final BooleanSetting physics=new BooleanSetting("Particle physics","Gravity and block collision",true),worldPhysics=new BooleanSetting("World physics","Physics for ambient particles",false),customColors=new BooleanSetting("Custom particle colors","Use the colors below",false),animatedGradient=new BooleanSetting("Animated gradients","Animate primary and secondary colors",true);
    private final NumberSetting worldLifeTime=new NumberSetting("World life","Ambient particle lifetime",10,2,60,.5f,"s"),worldSize=new NumberSetting("World size","Ambient particle scale",1.5f,.1f,1.5f,.05f,"x"),worldGlow=new NumberSetting("World glow","Ambient glow size",3,.1f,8,.1f,"x");
    private final ColorSetting uiPrimary=new ColorSetting("GUI primary","Primary interface color",0xFF68AEFF),uiSecondary=new ColorSetting("GUI secondary","Secondary interface color",0xFFFF6DA4);
    private final ColorSetting attackColor=new ColorSetting("Attack primary","Primary attack color",0xFFFF6D78),attackSecondColor=new ColorSetting("Attack gradient","Secondary attack color",0xFFFFC371),moveColor=new ColorSetting("Move primary","Primary movement color",0xFF68AEFF),moveSecondColor=new ColorSetting("Move gradient","Secondary movement color",0xFFB46DFF),projectileColor=new ColorSetting("Projectile primary","Primary projectile color",0xFFFFD166),projectileSecondColor=new ColorSetting("Projectile gradient","Secondary projectile color",0xFFFF6DA4),elytraColor=new ColorSetting("Elytra primary","Primary elytra color",0xFF75F4FF),elytraSecondColor=new ColorSetting("Elytra gradient","Secondary elytra color",0xFF68AEFF),worldColor=new ColorSetting("World primary","Primary world color",0xFF8DFFB3),worldSecondColor=new ColorSetting("World gradient","Secondary world color",0xFF68AEFF),totemColor=new ColorSetting("Totem primary","Primary totem color",0xFF7CFC00),totemSecondColor=new ColorSetting("Totem gradient","Secondary totem color",0xFFFFD700);
    private final List<Particle>particlesList=Collections.synchronizedList(new ArrayList<>());private final Map<Integer,PendingDeath> pendingDeaths=new HashMap<>();private Vec3 lastPlayer=Vec3.ZERO;private long ticks;private boolean lastOnGround=true;private int lastHurtTime;private String appliedPreset="";
    public ParticlesModule(){super("particles","Particles","VisualParticle Better cosmetic system",0xE7B0,ModuleCategory.RENDER,true);addSettings(master,attack,critical,damage,death,shape,criticalShape,attackAmount,criticalAmount,deathAmount,walk,sprint,jump,elytra,movementShape,jumpShape,walkAmount,sprintAmount,jumpAmount,arrowTrail,tridentTrail,fireworkTrail,projectileShape,projectileDensity,blockBreak,blockPlace,blockHit,blockShape,blockAmount,totem,itemPickup,itemShape,renderMode,colorPreset,glowMode,spread,speed,lifeTime,size,glow,physics,physicsMode,world,worldPhysics,worldShape,worldAmount,worldLifeTime,worldSize,worldGlow,uiPrimary,uiSecondary,customColors,animatedGradient,attackColor,attackSecondColor,moveColor,moveSecondColor,projectileColor,projectileSecondColor,worldColor,worldSecondColor,totemColor,totemSecondColor,activePreset,uiLanguage);loadConfig();UiLanguage.use(uiLanguage.value());}    @Override protected void onEnable(){
        particlesList.clear();
        track(ClientCore.getInstance().events().subscribe(ClientTickEvent.class,this::tick));
        track(ClientCore.getInstance().events().subscribe(AttackTargetEvent.class,this::attack));
        track(ClientCore.getInstance().events().subscribe(WorldRenderEvent.class,this::render));
    }
    @Override protected void onDisable(){particlesList.clear();}

    public List<Setting<?>> settingsFor(String section){return switch(section){
        case "Combat"->List.of(attack,shape,attackAmount,critical,criticalShape,criticalAmount,damage,death,deathAmount);
        case "Movement"->List.of(walk,sprint,jump,elytra,movementShape,jumpShape,walkAmount,sprintAmount,jumpAmount);
        case "Projectiles"->List.of(arrowTrail,tridentTrail,fireworkTrail,projectileShape,projectileDensity);
        case "Blocks"->List.of(blockBreak,blockPlace,blockHit,blockShape,blockAmount);
        case "Items"->List.of(totem,itemPickup,itemShape);
        case "Rendering"->List.of(master,renderMode,colorPreset,glowMode,spread,speed,lifeTime,size,glow,physics,physicsMode,world,worldPhysics,worldShape,worldAmount,worldLifeTime,worldSize,worldGlow,uiPrimary,uiSecondary,customColors,animatedGradient,attackColor,attackSecondColor,moveColor,moveSecondColor,projectileColor,projectileSecondColor,worldColor,worldSecondColor,totemColor,totemSecondColor);
        case "Presets"->List.of(activePreset);
        default->List.of();};}

    private void attack(AttackTargetEvent event){if(!master.enabled())return;LivingEntity target=event.target();Vec3 center=target.position().add(0,target.getBbHeight()*.55,0);Minecraft mc=Minecraft.getInstance();boolean crit=mc.player!=null&&mc.player.fallDistance>0&&!mc.player.onGround()&&!mc.player.isInWater();if(critical.enabled()&&crit)spawnBurst(center,Math.round(criticalAmount.value()),.15,Role.ATTACK,criticalShape.value(),.85f);else if(attack.enabled())spawnBurst(center,Math.round(attackAmount.value()),.11,Role.ATTACK,shape.value(),1);if(death.enabled())pendingDeaths.put(target.getId(),new PendingDeath(target,ticks+12));}
    private void tick(ClientTickEvent ignored){
        Minecraft mc=Minecraft.getInstance();ticks++;if(!activePreset.value().equals(appliedPreset)){applyPreset(activePreset.value());appliedPreset=activePreset.value();}
        if(!master.enabled()||mc.player==null||mc.level==null){particlesList.clear();pendingDeaths.clear();lastPlayer=Vec3.ZERO;lastHurtTime=0;return;}
        Iterator<PendingDeath> deaths=pendingDeaths.values().iterator();while(deaths.hasNext()){PendingDeath pending=deaths.next();LivingEntity e=pending.entity();if(e.isDeadOrDying()||e.isRemoved()){spawnBurst(e.position().add(0,e.getBbHeight()*.45,0),Math.round(deathAmount.value()),.18,Role.ATTACK,"Crown",1.1f);deaths.remove();}else if(ticks>pending.expires())deaths.remove();}
        if(damage.enabled()&&mc.player.hurtTime>lastHurtTime)spawnBurst(mc.player.position().add(0,mc.player.getBbHeight()*.55,0),Math.max(5,Math.round(attackAmount.value()*.5f)),.08,Role.ATTACK,"Lightning",.6f);lastHurtTime=mc.player.hurtTime;
        Vec3 current=mc.player.position(),motion=lastPlayer==Vec3.ZERO?Vec3.ZERO:current.subtract(lastPlayer);lastPlayer=current;boolean moving=motion.horizontalDistanceSqr()>.00025;
        if(mc.player.isFallFlying()&&elytra.enabled()&&due(24))spawn(current.add(random(-.3,.3),.65,random(-.3,.3)),motion.scale(-.1).add(random(-.02,.02),0,random(-.02,.02)),.55f,lifeTime.value()*.5f,Role.ELYTRA,movementShape.value(),false);
        else if(mc.player.isSprinting()&&moving&&sprint.enabled()&&due(sprintAmount.value()))spawn(current.add(random(-.25,.25),.12,random(-.25,.25)),new Vec3(-motion.x*.1,.025,-motion.z*.1),.6f,lifeTime.value()*.45f,Role.MOVE,movementShape.value(),false);
        else if(moving&&walk.enabled()&&due(walkAmount.value()))spawn(current.add(random(-.22,.22),.1,random(-.22,.22)),new Vec3(-motion.x*.08,.025,-motion.z*.08),.5f,lifeTime.value()*.4f,Role.MOVE,movementShape.value(),false);
        boolean ground=mc.player.onGround();if(jump.enabled()&&lastOnGround&&!ground&&motion.y>.07)spawnBurst(current.add(0,.15,0),Math.round(jumpAmount.value()),.085,Role.MOVE,jumpShape.value(),.65f);lastOnGround=ground;
        if(ticks%Math.max(1,7-Math.round(projectileDensity.value()))==0)for(Entity entity:mc.level.entitiesForRendering())if(entity instanceof Projectile p&&p.getOwner()==mc.player&&!entity.onGround()){boolean show=entity instanceof ThrownTrident?tridentTrail.enabled():entity instanceof FireworkRocketEntity?fireworkTrail.enabled():entity instanceof Arrow&&arrowTrail.enabled();if(show)spawn(entity.position(),entity.getDeltaMovement().scale(-.05),.42f,lifeTime.value()*.3f,Role.PROJECTILE,projectileShape.value(),false);}
        if(world.enabled()&&ticks%Math.max(1,Math.round(220/worldAmount.value()))==0)spawn(current.add(random(-10,10),random(.2,6),random(-10,10)),new Vec3(random(-.012,.012),random(-.003,.012),random(-.012,.012)),worldSize.value(),worldLifeTime.value(),Role.WORLD,worldShape.value(),true);
        synchronized(particlesList){Iterator<Particle> it=particlesList.iterator();while(it.hasNext()){Particle p=it.next();p.update(mc,p.world?worldPhysics.enabled():physics.enabled(),physicsMode.value(),ticks);if(p.dead())it.remove();}while(particlesList.size()>620)particlesList.removeFirst();}
    }
    private boolean due(float perSecond){return perSecond>0&&ticks%Math.max(1,Math.round(20/perSecond))==0;}
    private void spawnBurst(Vec3 center,int amount,double velocity,Role role,String sprite,float scale){for(int i=0;i<amount;i++)spawn(center,new Vec3(random(-velocity,velocity),random(.025,velocity*1.35),random(-velocity,velocity)),scale,lifeTime.value(),role,sprite,false);}
    public void spawnTotem(Entity entity){if(enabled()&&master.enabled()&&totem.enabled())spawnBurst(entity.position().add(0,entity.getBbHeight()*.55,0),48,.16,Role.TOTEM,itemShape.value(),.85f);}
    public void spawnItemPickup(Entity entity){if(enabled()&&master.enabled()&&itemPickup.enabled())spawnBurst(entity.position().add(0,.15,0),10,.07,Role.TOTEM,itemShape.value(),.48f);}
    public void spawnBlockHit(BlockPos pos,Direction face){if(enabled()&&master.enabled()&&blockHit.enabled()&&ticks%2==0)spawnSurface(pos,face,Math.max(2,Math.round(blockAmount.value()*.22f)),.42f);}
    public void spawnBlockBreak(BlockPos pos){if(enabled()&&master.enabled()&&blockBreak.enabled())spawnBurst(Vec3.atCenterOf(pos),Math.round(blockAmount.value()),.105,Role.WORLD,blockShape.value(),.72f);}
    public void spawnBlockPlace(BlockPos pos,Direction face){if(enabled()&&master.enabled()&&blockPlace.enabled())spawnSurface(pos,face,Math.max(3,Math.round(blockAmount.value()*.7f)),.6f);}
    private void spawnSurface(BlockPos pos,Direction face,int amount,float scale){Vec3 n=new Vec3(face.getStepX(),face.getStepY(),face.getStepZ()),c=Vec3.atCenterOf(pos).add(n.scale(.51));for(int i=0;i<amount;i++)spawn(c.add(random(-.3,.3),random(-.3,.3),random(-.3,.3)),n.scale(.05).add(random(-.02,.02),random(-.02,.02),random(-.02,.02)),scale,lifeTime.value()*.4f,Role.WORLD,blockShape.value(),false);}
    private void spawn(Vec3 pos,Vec3 velocity,float particleScale,float seconds,Role role,String sprite,boolean ambient){particlesList.add(new Particle(pos,velocity.scale(speed.value()),particleColor(role),size.value()*particleScale,(long)(seconds*1000),texture(sprite),ambient));}
    private void render(WorldRenderEvent event){
        Particle[] snapshot;synchronized(particlesList){snapshot=particlesList.toArray(Particle[]::new);}if(!master.enabled()||snapshot.length==0)return;float partial=Mth.clamp(event.partialTick(),0,1);Vec3 camera=event.context().gameRenderer().getMainCamera().position();PoseStack stack=event.context().matrices();boolean threeD=renderMode.is("3D Cross");Set<String> textures=new HashSet<>();for(Particle p:snapshot)textures.add(p.texture);
        for(String texture:textures){VertexConsumer out=event.context().consumers().getBuffer(type(texture));for(Particle p:snapshot)if(p.texture.equals(texture)){Vec3 at=p.interpolated(partial).subtract(camera);float alpha=p.alpha(),particleSize=p.scale*.5f;if(threeD)worldBillboard(stack,out,at,particleSize,p.rotation,withAlpha(p.color,(int)(alpha*255)));else billboard(stack,out,at,particleSize,p.rotation,withAlpha(p.color,(int)(alpha*255)));}}
        boolean bloom=glowMode.is("Bloom")||glowMode.is("Both"),sample=glowMode.is("Bloom Sample")||glowMode.is("Both");if(bloom){VertexConsumer out=event.context().consumers().getBuffer(type("dashbloom"));for(Particle p:snapshot){float a=p.alpha(),g=p.world?worldGlow.value():glow.value();billboard(stack,out,p.interpolated(partial).subtract(camera),p.scale*.25f*g,0,withAlpha(p.color,(int)(80*a)));}}if(sample){VertexConsumer out=event.context().consumers().getBuffer(type("dashbloomsample"));for(Particle p:snapshot){float a=p.alpha(),g=p.world?worldGlow.value():glow.value();billboard(stack,out,p.interpolated(partial).subtract(camera),p.scale*.1f*g,0,withAlpha(p.color,(int)(140*a)));}}
    }    private String texture(String value){return switch(value){case"Dollar"->"dollar";case"Heart"->"heart";case"Snowflake"->"snowflake";case"Star"->"star";case"Star Alt"->"star1";case"Line"->"line";case"Lightning"->"lightning";case"Triangle"->"triangle";case"Rhombus"->"rhombus";case"Crown"->"crown";case"Cross"->"cross";case"Arrow"->"arrow";case"Pearl Mark"->"pearl-mark";case"Cube Blast"->"cubeblast1";case"Dash Bloom"->"dashbloom";default->"star";};}
    private int particleColor(Role role){
        if(customColors.enabled()){int a=primary(role),b=secondary(role);float p=animatedGradient.enabled()?(float)(.5+.5*Math.sin(System.currentTimeMillis()/420.0)):ThreadLocalRandom.current().nextFloat();return blend(a,b,p);}
        return switch(colorPreset.value()){case"Blue"->0xFF4A90E2;case"Red"->0xFFC54A39;case"Purple"->0xFF9B6DFF;case"Pink"->0xFFFF6DA4;case"Cyan"->0xFF75F4FF;case"Green"->0xFF68E39A;case"Gold"->0xFFFFD166;case"Orange"->0xFFFF8C42;case"Lime"->0xFF7CFC00;case"Ice"->0xFFBDEBFF;case"Fire"->blend(0xFFFF3D3D,0xFFFFC14D,ThreadLocalRandom.current().nextFloat());case"Galaxy"->blend(0xFF6D5BFF,0xFFFF5BD6,ThreadLocalRandom.current().nextFloat());case"White"->0xFFFFFFFF;case"Random"->0xFF000000|ThreadLocalRandom.current().nextInt(0x1000000);default->0xFF896148;};
    }
    private int primary(Role role){return switch(role){case ATTACK->attackColor.argb();case MOVE->moveColor.argb();case PROJECTILE->projectileColor.argb();case ELYTRA->elytraColor.argb();case WORLD->worldColor.argb();case TOTEM->totemColor.argb();};}
    private int secondary(Role role){return switch(role){case ATTACK->attackSecondColor.argb();case MOVE->moveSecondColor.argb();case PROJECTILE->projectileSecondColor.argb();case ELYTRA->elytraSecondColor.argb();case WORLD->worldSecondColor.argb();case TOTEM->totemSecondColor.argb();};}
    private static int blend(int a,int b,float p){int r=Math.round(Mth.lerp(p,a>>16&255,b>>16&255)),g=Math.round(Mth.lerp(p,a>>8&255,b>>8&255)),bl=Math.round(Mth.lerp(p,a&255,b&255));return 0xFF000000|r<<16|g<<8|bl;}
    public void applyPresetByName(String preset){if(activePreset.modes().contains(preset))activePreset.set(preset);applyPreset(preset);appliedPreset=preset;saveConfig();}
    public String activePresetName(){return activePreset.value();}
    private void applyPreset(String preset){
        if("Purple PvP".equals(preset)){colorPreset.set("Purple");customColors.set(true);attackColor.set(0xFF9B6DFF);attackSecondColor.set(0xFFFF6DA4);shape.set("Heart");criticalShape.set("Rhombus");movementShape.set("Star");glow.set(.7f);lifeTime.set(.4f);size.set(.8f);physics.set(true);physicsMode.set("Classic");renderMode.set("2D Sprites");world.set(false);}
        else if("Minimal".equals(preset)){colorPreset.set("White");customColors.set(false);shape.set("Line");critical.set(false);damage.set(false);death.set(false);walk.set(false);sprint.set(false);world.set(false);glowMode.set("Bloom");glow.set(1.5f);lifeTime.set(.45f);size.set(.45f);physicsMode.set("Classic");renderMode.set("2D Sprites");}
        else if("Cinematic".equals(preset)){colorPreset.set("Galaxy");customColors.set(false);critical.set(true);damage.set(true);death.set(true);walk.set(true);sprint.set(true);jump.set(true);elytra.set(true);world.set(true);worldShape.set("Snowflake");glowMode.set("Both");glow.set(6f);lifeTime.set(2.4f);size.set(1.05f);physicsMode.set("Zero Gravity");renderMode.set("3D Cross");}
        else if("Storm".equals(preset)){colorPreset.set("Ice");customColors.set(false);walk.set(true);sprint.set(true);jump.set(true);elytra.set(true);world.set(true);movementShape.set("Snowflake");glowMode.set("Bloom Sample");glow.set(3f);lifeTime.set(1.5f);size.set(.75f);physics.set(true);physicsMode.set("Storm");renderMode.set("2D Sprites");}
        else if("Arcane".equals(preset)){colorPreset.set("Purple");customColors.set(true);attackColor.set(0xFF7B61FF);attackSecondColor.set(0xFFFF4FD8);moveColor.set(0xFF55DFFF);moveSecondColor.set(0xFF9B6DFF);world.set(true);worldShape.set("Rhombus");glowMode.set("Both");glow.set(4.5f);lifeTime.set(1.8f);size.set(.9f);physics.set(true);physicsMode.set("Vortex");renderMode.set("3D Cross");}
    }    public String languageCode(){return uiLanguage.value();}
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
    private static void worldBillboard(PoseStack s,VertexConsumer o,Vec3 p,float size,float rotation,int color){s.pushPose();s.translate(p.x,p.y,p.z);s.mulPose(Minecraft.getInstance().gameRenderer.getMainCamera().rotation());s.mulPose(Axis.YP.rotationDegrees(32));s.mulPose(Axis.ZP.rotationDegrees(rotation));float h=size*.5f;PoseStack.Pose pose=s.last();o.addVertex(pose,-h,-h,0).setUv(0,0).setColor(color);o.addVertex(pose,h,-h,0).setUv(1,0).setColor(color);o.addVertex(pose,h,h,0).setUv(1,1).setColor(color);o.addVertex(pose,-h,h,0).setUv(0,1).setColor(color);s.popPose();}
    private static double random(double a,double b){return ThreadLocalRandom.current().nextDouble(a,b);}private static int withAlpha(int c,int a){return c&0xFFFFFF|Mth.clamp(a,0,255)<<24;}
    private record PendingDeath(LivingEntity entity,long expires){}
    private static final class Particle{
        Vec3 previousPosition,position,velocity;final Vec3 origin;final int color;final float scale,phase;float rotation;final long born,life;long fadeOutStart=-1;final String texture;final boolean world;
        Particle(Vec3 p,Vec3 v,int c,float s,long l,String t,boolean w){previousPosition=position=origin=p;velocity=v;color=c;scale=s;life=l;texture=t;world=w;born=System.currentTimeMillis();rotation=ThreadLocalRandom.current().nextFloat()*360;phase=ThreadLocalRandom.current().nextFloat()*6.283f;}
        void update(Minecraft mc,boolean enabled,String mode,long tick){previousPosition=position;if(enabled){double time=tick*.075+phase;Vec3 force=switch(mode){case"Storm"->new Vec3(.007+Math.sin(time*1.7)*.004,Math.sin(time*2.3)*.0015,Math.cos(time)*.006);case"Vortex"->{Vec3 d=position.subtract(origin);double len=Math.max(.2,Math.sqrt(d.x*d.x+d.z*d.z));yield new Vec3(-d.z/len*.004,.0006+Math.sin(time)*.0005,d.x/len*.004);}case"Zero Gravity"->new Vec3(Math.sin(time)*.0008,Math.cos(time*1.4)*.0007,Math.sin(time*.8)*.0008);case"Magnetic"->{if(mc.player==null)yield Vec3.ZERO;Vec3 d=mc.player.getEyePosition().subtract(position);yield d.lengthSqr()<.01?Vec3.ZERO:d.normalize().scale(.0045);}default->new Vec3(0,-.0004,0);};velocity=velocity.add(force);}Vec3 next=position.add(velocity);if(enabled&&mc.level!=null&&!mc.level.getBlockState(BlockPos.containing(next)).isAir())velocity=new Vec3(velocity.x*.72,Math.abs(velocity.y)*.58,velocity.z*.72);else position=next;double drag=switch(mode){case"Storm"->.994;case"Vortex","Zero Gravity"->.996;case"Magnetic"->.992;default->.99;};velocity=velocity.scale(drag);rotation+=mode.equals("Storm")?5:mode.equals("Vortex")?3:2;long now=System.currentTimeMillis();if(fadeOutStart<0&&now-born>life)fadeOutStart=now;}
        Vec3 interpolated(float partial){return previousPosition.lerp(position,partial);}boolean dead(){return fadeOutStart>=0&&alpha()<=.001f;}float alpha(){long now=System.currentTimeMillis();float in=ease(Mth.clamp((now-born)/120f,0,1));if(fadeOutStart<0)return in;return Math.min(in,1-ease(Mth.clamp((now-fadeOutStart)/240f,0,1)));}private static float ease(float v){return v<.5f?2*v*v:1-(float)Math.pow(-2*v+2,2)/2;}
    }
}