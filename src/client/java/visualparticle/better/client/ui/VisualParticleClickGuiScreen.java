package visualparticle.better.client.ui;

import visualparticle.better.client.ClientCore;
import visualparticle.better.client.render.ui.AnimatedFloat;
import visualparticle.better.client.module.impl.render.ParticlesModule;
import visualparticle.better.client.render.ui.UiDrawList;
import visualparticle.better.client.ui.ClickGuiModel.Category;
import visualparticle.better.client.ui.ClickGuiModel.Choice;
import visualparticle.better.client.ui.ClickGuiModel.ColorValue;
import visualparticle.better.client.ui.ClickGuiModel.Group;
import visualparticle.better.client.ui.ClickGuiModel.Key;
import visualparticle.better.client.ui.ClickGuiModel.Module;
import visualparticle.better.client.ui.ClickGuiModel.Setting;
import visualparticle.better.client.ui.ClickGuiModel.Slider;
import visualparticle.better.client.ui.ClickGuiModel.Toggle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public final class VisualParticleClickGuiScreen extends Screen {
	private static final int WHITE=0xFFF1F1F1, TEXT=0xFFC9C9CB, MUTED=0xFF73757B;
	private static int ACCENT=0xFFECEDEF;
	private static final int PANEL=0xFA090A0C, SIDE=0xFC0D0E10, CARD=0xFF101113, BORDER=0xFF191A1E;
	private static final Identifier FLAG_RU=Identifier.fromNamespaceAndPath("visualparticle-better","textures/gui/flag_ru.png"), FLAG_GB=Identifier.fromNamespaceAndPath("visualparticle-better","textures/gui/flag_gb.png");
	private final List<Category> categories=ClickGuiModel.create();
	private final AnimatedFloat nav=new AnimatedFloat(0);
	private int categoryIndex;
	private Module expanded;
	private Slider dragging;
	private Choice openChoice;
	private Dropdown dropdown;
	private Group openGroup;
	private GroupDropdown groupDropdown;
	private ColorValue openColor;
	private ColorDrag colorDrag=ColorDrag.NONE;
	private boolean previousDown;
	private boolean previousRightDown;
	private boolean searchFocused;
	private boolean twoColumns=false;
	private String search="";
	private float scrollOffset,scrollTarget,maxScroll;
	private long lastFrame=System.nanoTime();

	public VisualParticleClickGuiScreen(){super(Component.literal("Visual Particle Settings"));if(!categories.isEmpty()&&!categories.getFirst().modules().isEmpty())expanded=categories.getFirst().modules().getFirst();if(Boolean.getBoolean("visualparticle-better.previewColorPicker")&&categories.size()>3){categoryIndex=3;expanded=categories.get(3).modules().getFirst();openColor=expanded.settings.stream().filter(ColorValue.class::isInstance).map(ColorValue.class::cast).findFirst().orElse(null);}}

	@Override public void removed(){ClientCore.getInstance().modules().find("particles").filter(ParticlesModule.class::isInstance).map(ParticlesModule.class::cast).ifPresent(ParticlesModule::saveConfig);super.removed();}

	@Override public void render(GuiGraphics graphics,int mouseX,int mouseY,float tickDelta){
		ACCENT=particles().uiPrimaryColor();
		long now=System.nanoTime();
		float dt=Math.min(.05f,(now-lastFrame)/1_000_000_000f);
		lastFrame=now;
		boolean down=GLFW.glfwGetMouseButton(Minecraft.getInstance().getWindow().handle(),
			GLFW.GLFW_MOUSE_BUTTON_LEFT)==GLFW.GLFW_PRESS;
		boolean pressed=down&&!previousDown;
		boolean rightDown=GLFW.glfwGetMouseButton(Minecraft.getInstance().getWindow().handle(),
			GLFW.GLFW_MOUSE_BUTTON_RIGHT)==GLFW.GLFW_PRESS;
		boolean rightPressed=rightDown&&!previousRightDown;
		Layout l=Layout.create(width,height);
		if(pressed&&openChoice!=null&&dropdown!=null&&!dropdown.contains(mouseX,mouseY))openChoice=null;
		if(pressed&&openGroup!=null&&groupDropdown!=null&&!groupDropdown.contains(mouseX,mouseY))openGroup=null;
		dropdown=null;
		groupDropdown=null;
		UiDrawList ui=new UiDrawList();
		backdrop(ui,l);
		sidebar(ui,l,mouseX,mouseY,pressed,dt);
		Category category=categories.get(categoryIndex);
		header(ui,l,mouseX,mouseY,pressed);
		moduleGrid(ui,l,category,mouseX,mouseY,down,pressed,rightPressed,dt);
		drawDropdown(ui,l,mouseX,mouseY,pressed);
		ClientCore.getInstance().renderer().submit(graphics,ui);
		drawLanguageFlag(graphics,l);
		if(!down){dragging=null;colorDrag=ColorDrag.NONE;}
		previousDown=down;
		previousRightDown=rightDown;
	}

	private ParticlesModule particles(){return ClientCore.getInstance().modules().find("particles").filter(ParticlesModule.class::isInstance).map(ParticlesModule.class::cast).orElseThrow();}
	private void drawLanguageFlag(GuiGraphics graphics,Layout l){
		float y=l.y+23*l.s,sx=l.mainX+l.mainW-116*l.s,lx=sx-39*l.s;
		int x0=Math.round(lx+4*l.s),y0=Math.round(y-4*l.s),w=Math.max(10,Math.round(13*l.s)),h=Math.max(7,Math.round(8*l.s));
		graphics.blit("RU".equals(particles().languageCode())?FLAG_RU:FLAG_GB,x0,y0,x0+w,y0+h,0,1,0,1);
	}
	private void backdrop(UiDrawList ui,Layout l){
		ui.rect(0,0,width,height,0xC9000000);
		for(int i=0;i<90;i++){
			float px=(i*83%Math.max(1,width));
			float py=(i*47%Math.max(1,height));
			ui.circle(px,py,.45f,0x16FFFFFF);
		}
		for(int i=5;i>0;i--){
			float p=i*3.5f*l.s;
			ui.roundedRect(l.x-p,l.y-p+4*l.s,l.w+p*2,l.h+p*2,14*l.s,0x08000000);
		}
		ui.roundedRect(l.x,l.y,l.w,l.h,8*l.s,PANEL)
			.outlineRoundedRect(l.x,l.y,l.w,l.h,8*l.s,Math.max(.55f,l.s*.7f),0xFF202126)
			.roundedRect(l.x,l.y,l.side,l.h,8*l.s,SIDE)
			.rect(l.x+l.side-l.s,l.y+10*l.s,l.s,l.h-20*l.s,BORDER);
	}

	private void sidebar(UiDrawList ui,Layout l,int mx,int my,boolean pressed,float dt){
		float bx=l.x+15*l.s, brandY=l.y+24*l.s;
		ui.strongText(bx,brandY,9.5f*l.s,WHITE,"VisualParticle Better");
		float h=27*l.s,g=2*l.s,x=l.x+9*l.s,w=l.side-18*l.s;
		for(int i=0;i<categories.size();i++){
			float iy=categoryY(l,i,h,g);
			if(pressed&&hit(mx,my,x,iy,w,h)){
				categoryIndex=i;expanded=categories.get(i).modules().isEmpty()?null:categories.get(i).modules().getFirst();dragging=null;openChoice=null;openGroup=null;openColor=null;colorDrag=ColorDrag.NONE;scrollOffset=scrollTarget=0;nav.setTarget(i);
			}
		}
		nav.setTarget(categoryIndex);
		float animated=nav.update(dt,18);
		float indicatorY=categoryY(l,animated,h,g);
		ui.roundedRect(x,indicatorY,w,h,6*l.s,0xFF171920)
			.roundedRect(x,indicatorY+7*l.s,2*l.s,h-14*l.s,l.s,ACCENT);
		for(int i=0;i<categories.size();i++){
			Category c=categories.get(i);
			float iy=categoryY(l,i,h,g);
			int color=i==categoryIndex?WHITE:MUTED;
			ui.icon(x+14*l.s,iy+h/2,11*l.s,color,c.icon())
				.text(x+28*l.s,iy+h/2,8.5f*l.s,color,UiLanguage.text(c.name()));
		}

	}

	private void header(UiDrawList ui,Layout l,int mx,int my,boolean pressed){
		float y=l.y+23*l.s;
		float sx=l.mainX+l.mainW-116*l.s,sw=88*l.s;
		float lx=sx-39*l.s,lw=33*l.s;
		if(pressed&&hit(mx,my,lx,y-10*l.s,lw,20*l.s)){particles().toggleLanguage();openChoice=null;openGroup=null;}
		ui.roundedRect(lx,y-10*l.s,lw,20*l.s,5*l.s,0xFF111214).outlineRoundedRect(lx,y-10*l.s,lw,20*l.s,5*l.s,Math.max(.5f,l.s*.6f),BORDER).strongText(lx+21*l.s,y,6.4f*l.s,WHITE,UiLanguage.code());
		boolean searchOver=hit(mx,my,sx,y-10*l.s,sw,20*l.s);
		if(pressed)searchFocused=searchOver;
		ui.roundedRect(sx,y-10*l.s,sw,20*l.s,5*l.s,searchFocused?0xFF181A1D:0xFF111214)
			.outlineRoundedRect(sx,y-10*l.s,sw,20*l.s,5*l.s,Math.max(.5f,l.s*.6f),
				searchFocused?0xFF3A3B40:BORDER)
			.icon(sx+sw-10*l.s,y,9*l.s,MUTED,0xE8B6);
		String shown=search.isEmpty()?UiLanguage.text("Search settings..."):search;
		if(searchFocused&&(System.currentTimeMillis()/500)%2==0)shown+="_";
		ui.text(sx+7*l.s,y,7*l.s,search.isEmpty()?MUTED:TEXT,shown);
		float gx=l.mainX+l.mainW-18*l.s;
		if(pressed&&hit(mx,my,gx-8*l.s,y-9*l.s,17*l.s,18*l.s))twoColumns=!twoColumns;
		ui.icon(gx,y,10*l.s,twoColumns?WHITE:MUTED,twoColumns?0xE8F0:0xE8EF)
			.line(l.mainX,l.y+42*l.s,l.mainX+l.mainW,l.y+42*l.s,Math.max(.5f,l.s*.6f),BORDER);
	}
	private void moduleGrid(UiDrawList ui,Layout l,Category category,int mx,int my,boolean down,boolean pressed,boolean rightPressed,float dt){
		List<Module> visible=new ArrayList<>();
		String query=search.toLowerCase(Locale.ROOT).trim();
		if(query.isEmpty())visible.addAll(category.modules());
		else for(Category source:categories)for(Module m:source.modules())
			if(m.name.toLowerCase(Locale.ROOT).contains(query)||UiLanguage.text(m.name).toLowerCase(Locale.ROOT).contains(query)||m.description.toLowerCase(Locale.ROOT).contains(query)||m.settings.stream().anyMatch(setting->setting.name().toLowerCase(Locale.ROOT).contains(query)||setting.hint().toLowerCase(Locale.ROOT).contains(query)))visible.add(m);
		float gap=13*l.s;
		int columns=twoColumns?2:1;
		float columnW=(l.mainW-gap*(columns-1))/columns;
		scrollOffset+=(scrollTarget-scrollOffset)*Math.min(1,dt*18);
		float viewportTop=l.y+44*l.s,viewportBottom=l.y+l.h-10*l.s;
		float[] ys=new float[columns];
		for(int i=0;i<columns;i++)ys[i]=l.y+49*l.s-scrollOffset;
		ui.pushScissor(l.mainX,l.y+44*l.s,l.mainW,l.h-54*l.s);
		for(int i=0;i<visible.size();i++){
			Module m=visible.get(i);
			int column=columns==1?0:i%2;
			float x=l.mainX+column*(columnW+gap),y=ys[column];
			float baseH=31*l.s;
			float settingsH=expanded==m?settingsHeight(m,l.s):0;
			boolean pointerInView=hit(mx,my,l.mainX,viewportTop,l.mainW,viewportBottom-viewportTop);
			boolean over=pointerInView&&hit(mx,my,x,y,columnW,baseH);
			float toggleX=x+columnW-25*l.s;
			
			expanded=m;
			ui.roundedRect(x,y,columnW,baseH+settingsH,5*l.s,
				expanded==m?0xFF12141A:over?0xFF111319:CARD)
				.strongText(x+9*l.s,y+15*l.s,8.2f*l.s,expanded==m?WHITE:TEXT,UiLanguage.text(m.name));

			
			if(expanded==m)drawSettings(ui,l,m,x+8*l.s,y+35*l.s,columnW-16*l.s,pointerInView?mx:-100000,pointerInView?my:-100000,down&&pointerInView,pressed&&pointerInView,dt);
			ys[column]+=baseH+settingsH+5*l.s;
		}
		float contentBottom=ys[0];for(int i=1;i<columns;i++)contentBottom=Math.max(contentBottom,ys[i]);
		maxScroll=Math.max(0,contentBottom+scrollOffset-viewportBottom);scrollTarget=Math.max(0,Math.min(maxScroll,scrollTarget));
		if(maxScroll>1){float trackH=viewportBottom-viewportTop;float thumbH=Math.max(18*l.s,trackH*trackH/(trackH+maxScroll));float thumbY=viewportTop+(trackH-thumbH)*(scrollOffset/maxScroll);ui.roundedRect(l.mainX+l.mainW-1.5f*l.s,thumbY,1.5f*l.s,thumbH,.75f*l.s,0x6673757B);}
		if(visible.isEmpty())ui.centeredText(l.mainX+l.mainW/2,l.y+l.h/2,9*l.s,MUTED,UiLanguage.text("No settings found"),false);
		ui.popScissor();
	}

	private void drawSettings(UiDrawList ui,Layout l,Module module,float x,float y,float w,
			int mx,int my,boolean down,boolean pressed,float dt){
		for(Setting setting:module.settings){
			if(!setting.visible())continue;
			if(setting instanceof Toggle t){
				ui.strongText(x,y+7*l.s,7.2f*l.s,TEXT,t.name())
					.text(x,y+17*l.s,6*l.s,MUTED,t.hint());
				if(pressed&&hit(mx,my,x,y,w,23*l.s))t.toggle();
				float p=t.animation.update(dt,20);
				ui.roundedRect(x+w-11*l.s,y+4*l.s,8*l.s,8*l.s,2*l.s,p>.5f?ACCENT:0xFF24262E);
				if(p>.5f)ui.icon(x+w-7*l.s,y+8*l.s,7*l.s,PANEL,0xE5CA);
				y+=25*l.s;
			}else if(setting instanceof Slider s){
				ui.strongText(x,y+6*l.s,7.2f*l.s,TEXT,s.name())
					.rightText(x+w,y+6*l.s,6.8f*l.s,TEXT,s.value(),true)
					.text(x,y+15*l.s,6*l.s,MUTED,s.hint());
				float tx=x+w*.48f,tw=w*.37f,ty=y+11*l.s;
				if(pressed&&hit(mx,my,tx,ty-5*l.s,tw,12*l.s))dragging=s;
				if(down&&dragging==s){s.model.setFromMouse(mx,tx,tw);s.sync();}
				float p=s.model.update(dt);
				ui.roundedRect(tx,ty,tw,2*l.s,l.s,0xFF292C35)
					.roundedRect(tx,ty,Math.max(l.s,tw*p),2*l.s,l.s,ACCENT)
					.circle(tx+tw*p,ty+l.s,2.6f*l.s,WHITE);
				y+=27*l.s;
			}else if(setting instanceof Choice c){
				ui.strongText(x,y+9*l.s,7.2f*l.s,TEXT,UiLanguage.text(c.name()));
				float sw=Math.min(73*l.s,w*.42f),sx=x+w-sw;
				if(pressed&&hit(mx,my,sx,y,sw,20*l.s)){openChoice=openChoice==c?null:c;openGroup=null;openColor=null;}
				ui.roundedRect(sx,y,sw,20*l.s,4*l.s,0xFF191B22)
					.text(sx+6*l.s,y+10*l.s,6.5f*l.s,TEXT,c.value())
					.icon(sx+sw-7*l.s,y+10*l.s,8*l.s,openChoice==c?ACCENT:MUTED,openChoice==c?0xE5CE:0xE5CF);
				if(openChoice==c)dropdown=new Dropdown(c,sx,y,sw,20*l.s,17*l.s);
				y+=24*l.s;
			}else if(setting instanceof Group g){
				ui.strongText(x,y+9*l.s,7.2f*l.s,TEXT,g.name());
				float sw=Math.min(73*l.s,w*.42f),sx=x+w-sw;
				if(pressed&&hit(mx,my,sx,y,sw,20*l.s)){openGroup=openGroup==g?null:g;openChoice=null;openColor=null;}
				ui.roundedRect(sx,y,sw,20*l.s,4*l.s,0xFF191B22)
					.text(sx+6*l.s,y+10*l.s,6.3f*l.s,TEXT,g.summary())
					.icon(sx+sw-7*l.s,y+10*l.s,8*l.s,openGroup==g?ACCENT:MUTED,openGroup==g?0xE5CE:0xE5CF);
				if(openGroup==g)groupDropdown=new GroupDropdown(g,sx,y,Math.max(sw,105*l.s),20*l.s,18*l.s);
				y+=24*l.s;
			}else if(setting instanceof ColorValue c){
				boolean opened=openColor==c;
				ui.strongText(x,y+8*l.s,7.2f*l.s,TEXT,c.name()).text(x,y+17*l.s,5.8f*l.s,MUTED,c.hint());
				float sw=36*l.s,sx=x+w-sw;
				if(pressed&&hit(mx,my,x,y,w,21*l.s)){openColor=opened?null:c;colorDrag=ColorDrag.NONE;openChoice=null;openGroup=null;opened=!opened;}
				checker(ui,sx,y+3*l.s,sw,13*l.s,3*l.s);
				ui.roundedRect(sx,y+3*l.s,sw,13*l.s,3*l.s,c.argb()).outlineRoundedRect(sx,y+3*l.s,sw,13*l.s,3*l.s,Math.max(.55f,l.s*.7f),opened?WHITE:0x66FFFFFF)
					.text(sx-50*l.s,y+9*l.s,5.8f*l.s,opened?TEXT:MUTED,c.value());
				y+=24*l.s;
				if(openColor==c){drawColorPicker(ui,c,x,y,w,l.s,mx,my,down,pressed);y+=148*l.s;}
			}else if(setting instanceof Key k){
				ui.strongText(x,y+9*l.s,7.2f*l.s,TEXT,k.name());
				float kw=38*l.s;
				ui.roundedRect(x+w-kw,y,kw,19*l.s,4*l.s,0xFF1C1E26)
					.centeredText(x+w-kw/2,y+9.5f*l.s,6*l.s,TEXT,k.key(),true);
				y+=23*l.s;
			}
		}
	}

	private void drawDropdown(UiDrawList ui,Layout l,int mx,int my,boolean pressed){
		Dropdown d=dropdown;
		if(d==null||openChoice!=d.choice){drawGroupDropdown(ui,l,mx,my,pressed);return;}
		float listY=d.y+d.headerH+2*l.s;
		float listH=d.choice.values.size()*d.rowH+4*l.s;
		if(pressed&&hit(mx,my,d.x,listY,d.w,listH)){
			int index=(int)((my-listY-2*l.s)/d.rowH);
			if(index>=0&&index<d.choice.values.size()){
				d.choice.select(index);
				openChoice=null;
				return;
			}
		}
		for(int i=4;i>0;i--)ui.roundedRect(d.x-i*l.s,d.y-i*l.s+3*l.s,d.w+i*2*l.s,d.headerH+listH+i*2*l.s,5*l.s,0x09000000);
		ui.roundedRect(d.x-.5f*l.s,d.y-.5f*l.s,d.w+l.s,d.headerH+listH+3*l.s,4.5f*l.s,0xFF2A2D38)
			.roundedRect(d.x,d.y,d.w,d.headerH+listH+2*l.s,4*l.s,0xFF101116)
			.roundedRect(d.x,d.y,d.w,d.headerH,4*l.s,0xFF191B22)
			.text(d.x+6*l.s,d.y+d.headerH/2,6.5f*l.s,WHITE,d.choice.value())
			.icon(d.x+d.w-7*l.s,d.y+d.headerH/2,8*l.s,ACCENT,0xE5CE);
		for(int i=0;i<d.choice.values.size();i++){
			float ry=listY+2*l.s+i*d.rowH;
			boolean selected=i==d.choice.selected,hover=hit(mx,my,d.x+2*l.s,ry,d.w-4*l.s,d.rowH);
			if(selected||hover)ui.roundedRect(d.x+2*l.s,ry,d.w-4*l.s,d.rowH,3*l.s,selected?0xFF1C2133:0xFF17191F);
			ui.text(d.x+7*l.s,ry+d.rowH/2,6.5f*l.s,selected?WHITE:TEXT,d.choice.display(i));
			if(selected)ui.icon(d.x+d.w-8*l.s,ry+d.rowH/2,7*l.s,ACCENT,0xE5CA);
		}
		drawGroupDropdown(ui,l,mx,my,pressed);
	}
	private void drawGroupDropdown(UiDrawList ui,Layout l,int mx,int my,boolean pressed){
		GroupDropdown d=groupDropdown;
		if(d==null||openGroup!=d.group)return;
		float listY=d.y+d.headerH+2*l.s,listH=d.group.values.size()*d.rowH+4*l.s;
		if(pressed&&hit(mx,my,d.x,listY,d.w,listH)){
			int index=(int)((my-listY-2*l.s)/d.rowH);
			if(index>=0&&index<d.group.values.size())d.group.values.get(index).toggle();
		}
		for(int i=4;i>0;i--)ui.roundedRect(d.x-i*l.s,d.y-i*l.s+3*l.s,d.w+i*2*l.s,d.headerH+listH+i*2*l.s,5*l.s,0x09000000);
		ui.roundedRect(d.x-.5f*l.s,d.y-.5f*l.s,d.w+l.s,d.headerH+listH+3*l.s,4.5f*l.s,0xFF2A2D38)
			.roundedRect(d.x,d.y,d.w,d.headerH+listH+2*l.s,4*l.s,0xFF101116)
			.roundedRect(d.x,d.y,d.w,d.headerH,4*l.s,0xFF191B22)
			.text(d.x+6*l.s,d.y+d.headerH/2,6.5f*l.s,WHITE,d.group.name())
			.rightText(d.x+d.w-7*l.s,d.y+d.headerH/2,5.8f*l.s,MUTED,d.group.summary(),true);
		for(int i=0;i<d.group.values.size();i++){
			Toggle option=d.group.values.get(i);
			float ry=listY+2*l.s+i*d.rowH;
			boolean hover=hit(mx,my,d.x+2*l.s,ry,d.w-4*l.s,d.rowH);
			if(hover)ui.roundedRect(d.x+2*l.s,ry,d.w-4*l.s,d.rowH,3*l.s,0xFF17191F);
			ui.text(d.x+7*l.s,ry+d.rowH/2,6.4f*l.s,option.value?WHITE:TEXT,option.name())
				.roundedRect(d.x+d.w-14*l.s,ry+5*l.s,8*l.s,8*l.s,2*l.s,option.value?ACCENT:0xFF24262E);
			if(option.value)ui.icon(d.x+d.w-10*l.s,ry+9*l.s,7*l.s,WHITE,0xE5CA);
		}
	}
	private static float categoryY(Layout l,float index,float height,float gap){
		return l.y+48*l.s+index*(height+gap);
	}
	private float settingsHeight(Module m,float s){
		float h=9*s;
		for(Setting setting:m.settings)if(setting.visible()){
			h+=(setting instanceof Slider?27:setting instanceof Toggle?25:24)*s;
			if(setting instanceof ColorValue color&&openColor==color)h+=148*s;
		}
		return h;
	}
	private void drawColorPicker(UiDrawList ui,ColorValue color,float x,float y,float w,float s,int mx,int my,boolean down,boolean pressed){
		float panelW=Math.min(w,300*s),panelX=x,panelH=141*s,pad=8*s;
		float svX=panelX+pad,svY=y+pad,svW=panelW-pad*2,svH=76*s;
		float hueY=svY+svH+8*s,hueH=9*s,alphaY=hueY+hueH+8*s,alphaH=9*s;
		for(int i=5;i>0;i--)ui.roundedRect(panelX-i*s,y-i*s+4*s,panelW+i*2*s,panelH+i*2*s,9*s,0x09000000);
		ui.roundedRect(panelX,y,panelW,panelH,8*s,0xFF0A0C11)
			.outlineRoundedRect(panelX,y,panelW,panelH,8*s,Math.max(.65f,s*.75f),0xFF323745)
			.colorPicker(svX,svY,svW,svH,5*s,color.hue())
			.outlineRoundedRect(svX,svY,svW,svH,5*s,Math.max(.65f,s*.7f),0x88FFFFFF)
			.hueBar(svX,hueY,svW,hueH,3*s)
			.outlineRoundedRect(svX,hueY,svW,hueH,3*s,Math.max(.55f,s*.6f),0x77FFFFFF);
		checker(ui,svX,alphaY,svW,alphaH,3*s);
		ui.alphaBar(svX,alphaY,svW,alphaH,3*s,color.argb())
			.outlineRoundedRect(svX,alphaY,svW,alphaH,3*s,Math.max(.55f,s*.6f),0x77FFFFFF);
		if(pressed){if(hit(mx,my,svX,svY,svW,svH))colorDrag=ColorDrag.SV;else if(hit(mx,my,svX,hueY,svW,hueH))colorDrag=ColorDrag.HUE;else if(hit(mx,my,svX,alphaY,svW,alphaH))colorDrag=ColorDrag.ALPHA;}
		if(down&&openColor==color){if(colorDrag==ColorDrag.SV)color.setHsb(color.hue(),clamp01((mx-svX)/svW),1-clamp01((my-svY)/svH));else if(colorDrag==ColorDrag.HUE)color.setHsb(clamp01((mx-svX)/svW),color.saturation(),color.brightness());else if(colorDrag==ColorDrag.ALPHA)color.setAlpha(clamp01((mx-svX)/svW));}
		float pickX=svX+color.saturation()*svW,pickY=svY+(1-color.brightness())*svH;
		ui.circle(pickX,pickY,5*s,0xCC000000).circle(pickX,pickY,3.2f*s,WHITE);
		float hueX=svX+color.hue()*svW;ui.roundedRect(hueX-2*s,hueY-2*s,4*s,hueH+4*s,2*s,0xEE08090C).outlineRoundedRect(hueX-2*s,hueY-2*s,4*s,hueH+4*s,2*s,Math.max(.6f,s*.65f),WHITE);
		float alphaX=svX+color.alpha()*svW;ui.roundedRect(alphaX-2*s,alphaY-2*s,4*s,alphaH+4*s,2*s,0xEE08090C).outlineRoundedRect(alphaX-2*s,alphaY-2*s,4*s,alphaH+4*s,2*s,Math.max(.6f,s*.65f),WHITE);
		float infoY=alphaY+18*s;checker(ui,svX,infoY,18*s,12*s,4*s);ui.roundedRect(svX,infoY,18*s,12*s,4*s,color.argb()).outlineRoundedRect(svX,infoY,18*s,12*s,4*s,Math.max(.55f,s*.6f),0x88FFFFFF)
			.strongText(svX+25*s,infoY+6*s,7.2f*s,WHITE,color.value())
			.rightText(svX+svW,infoY+6*s,6.2f*s,MUTED,String.format(Locale.ROOT,"H %d°  S %d%%  B %d%%  A %d%%",Math.round(color.hue()*360),Math.round(color.saturation()*100),Math.round(color.brightness()*100),Math.round(color.alpha()*100)),false);
	}
	private static void checker(UiDrawList ui,float x,float y,float w,float h,float radius){
		ui.roundedRect(x,y,w,h,radius,0xFF252832);float cell=Math.max(2,h/2);int columns=Math.max(1,(int)Math.ceil(w/cell));for(int i=0;i<columns;i++)for(int row=0;row<2;row++)if(((i+row)&1)==0)ui.rect(x+i*cell,y+row*cell,Math.min(cell,x+w-(x+i*cell)),Math.min(cell,y+h-(y+row*cell)),0xFF3A3E49);
	}
	private static float clamp01(float value){return Math.max(0,Math.min(1,value));}
	private enum ColorDrag{NONE,SV,HUE,ALPHA}
	private static void toggle(UiDrawList ui,float x,float y,float w,float h,float p,float s){
		ui.roundedRect(x,y,w,h,h/2,mix(0xFF292C34,ACCENT,p))
			.circle(x+h/2+(w-h)*p,y+h/2,h/2-2*s,mix(WHITE,0xFF111216,p));
	}
	private static int mix(int a,int b,float t){
		t=Math.max(0,Math.min(1,t));
		int aa=Math.round((a>>>24)+((b>>>24)-(a>>>24))*t);
		int r=Math.round(((a>>16)&255)+(((b>>16)&255)-((a>>16)&255))*t);
		int g=Math.round(((a>>8)&255)+(((b>>8)&255)-((a>>8)&255))*t);
		int bl=Math.round((a&255)+((b&255)-(a&255))*t);
		return aa<<24|r<<16|g<<8|bl;
	}
	private static boolean hit(float mx,float my,float x,float y,float w,float h){
		return mx>=x&&mx<=x+w&&my>=y&&my<=y+h;
	}

	private record Dropdown(Choice choice,float x,float y,float w,float headerH,float rowH){
		boolean contains(float mx,float my){
			float listH=choice.values.size()*rowH+6;
			return hit(mx,my,x,y,w,headerH+listH);
		}
	}
	private record GroupDropdown(Group group,float x,float y,float w,float headerH,float rowH){
		boolean contains(float mx,float my){return hit(mx,my,x,y,w,headerH+group.values.size()*rowH+6);}
	}

	@Override public boolean charTyped(CharacterEvent event){
		if(searchFocused&&event.isAllowedChatCharacter()&&search.length()<28){
			search+=event.codepointAsString();scrollOffset=scrollTarget=0;
			return true;
		}
		return super.charTyped(event);
	}
	@Override public boolean keyPressed(KeyEvent event){
		if(searchFocused){
			if(event.key()==GLFW.GLFW_KEY_BACKSPACE){
				if(!search.isEmpty())search=search.substring(0,search.offsetByCodePoints(search.length(),-1));scrollOffset=scrollTarget=0;
				return true;
			}
			if(event.key()==GLFW.GLFW_KEY_ENTER){searchFocused=false;return true;}
			if(event.key()==GLFW.GLFW_KEY_ESCAPE){searchFocused=false;return true;}
		}
		return super.keyPressed(event);
	}
	@Override public boolean mouseScrolled(double mouseX,double mouseY,double horizontalAmount,double verticalAmount){
		Layout l=Layout.create(width,height);
		if(hit((float)mouseX,(float)mouseY,l.mainX,l.y+44*l.s,l.mainW,l.h-54*l.s)){
			scrollTarget=Math.max(0,Math.min(maxScroll,scrollTarget-(float)verticalAmount*36*l.s));
			openChoice=null;openGroup=null;
			return true;
		}
		return super.mouseScrolled(mouseX,mouseY,horizontalAmount,verticalAmount);
	}
	@Override public boolean isPauseScreen(){return false;}

	private record Layout(float x,float y,float w,float h,float s,float side,float mainX,float mainW){
		static Layout create(int sw,int sh){
			float designW=600,designH=340,margin=18;
			float fitScale=Math.min((sw-margin*2)/designW,(sh-margin*2)/designH);
			float s=Math.max(.55f,Math.min(.78f,fitScale));
			float w=designW*s,h=designH*s;
			float x=(sw-w)/2,y=(sh-h)/2,side=130*s,mainX=x+side+14*s;
			return new Layout(x,y,w,h,s,side,mainX,x+w-mainX-14*s);
		}
	}
}
