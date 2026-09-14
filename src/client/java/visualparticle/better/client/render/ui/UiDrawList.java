package visualparticle.better.client.render.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class UiDrawList {
	private final List<UiCommand> commands=new ArrayList<>(); private int scissorDepth; private boolean sealed;
	public UiDrawList rect(float x,float y,float width,float height,int color){return roundedRect(x,y,width,height,0,color);}
	public UiDrawList roundedRect(float x,float y,float width,float height,float radius,int color){checkMutable();commands.add(new UiCommand.Rect(x,y,positive(width),positive(height),clampRadius(radius,width,height),color));return this;}
	public UiDrawList gradientRoundedRect(float x,float y,float width,float height,float radius,int startColor,int endColor,boolean horizontal){checkMutable();commands.add(new UiCommand.GradientRect(x,y,positive(width),positive(height),clampRadius(radius,width,height),startColor,endColor,horizontal));return this;}
	public UiDrawList outlineRoundedRect(float x,float y,float width,float height,float radius,float thickness,int color){checkMutable();commands.add(new UiCommand.Outline(x,y,positive(width),positive(height),clampRadius(radius,width,height),positive(thickness),color));return this;}
	public UiDrawList circle(float x,float y,float radius,int color){checkMutable();commands.add(new UiCommand.Circle(x,y,positive(radius),color));return this;}
	public UiDrawList line(float x1,float y1,float x2,float y2,float thickness,int color){checkMutable();commands.add(new UiCommand.Line(x1,y1,x2,y2,positive(thickness),color));return this;}
	public UiDrawList text(float x,float y,float size,int color,String value){return text(x,y,size,color,value,UiCommand.HorizontalAlign.LEFT,false);}
	public UiDrawList strongText(float x,float y,float size,int color,String value){return text(x,y,size,color,value,UiCommand.HorizontalAlign.LEFT,true);}
	public UiDrawList centeredText(float x,float y,float size,int color,String value,boolean strong){return text(x,y,size,color,value,UiCommand.HorizontalAlign.CENTER,strong);}
	public UiDrawList rightText(float x,float y,float size,int color,String value,boolean strong){return text(x,y,size,color,value,UiCommand.HorizontalAlign.RIGHT,strong);}
	public UiDrawList text(float x,float y,float size,int color,String value,UiCommand.HorizontalAlign align,boolean strong){checkMutable();if(value!=null&&!value.isEmpty())commands.add(new UiCommand.Text(x,y,positive(size),color,value,align,strong));return this;}
	public UiDrawList icon(float x,float y,float size,int color,int codePoint){checkMutable();commands.add(new UiCommand.Icon(x,y,positive(size),color,codePoint));return this;}
	public UiDrawList colorPicker(float x,float y,float width,float height,float radius,float hue){checkMutable();commands.add(new UiCommand.ColorPicker(x,y,positive(width),positive(height),clampRadius(radius,width,height),Math.max(0,Math.min(1,hue))));return this;}
	public UiDrawList hueBar(float x,float y,float width,float height,float radius){checkMutable();commands.add(new UiCommand.HueBar(x,y,positive(width),positive(height),clampRadius(radius,width,height)));return this;}
	public UiDrawList alphaBar(float x,float y,float width,float height,float radius,int color){checkMutable();commands.add(new UiCommand.AlphaBar(x,y,positive(width),positive(height),clampRadius(radius,width,height),color));return this;}
	public UiDrawList pushScissor(float x,float y,float width,float height){checkMutable();commands.add(new UiCommand.PushScissor(x,y,positive(width),positive(height)));scissorDepth++;return this;}
	public UiDrawList popScissor(){checkMutable();if(scissorDepth==0)throw new IllegalStateException("No scissor to pop");commands.add(new UiCommand.PopScissor());scissorDepth--;return this;}
	public List<UiCommand> seal(){if(scissorDepth!=0)throw new IllegalStateException("Unbalanced scissor commands: "+scissorDepth);sealed=true;return Collections.unmodifiableList(commands);}
	public boolean isEmpty(){return commands.isEmpty();}
	private void checkMutable(){if(sealed)throw new IllegalStateException("Draw list was already submitted");}
	private static float positive(float value){if(!Float.isFinite(value)||value<=0)throw new IllegalArgumentException("Expected a positive finite value");return value;}
	private static float clampRadius(float radius,float width,float height){if(!Float.isFinite(radius))throw new IllegalArgumentException("Radius must be finite");return Math.max(0,Math.min(radius,Math.min(width,height)*.5f));}
}
