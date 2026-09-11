package visualparticle.better.client.render.nanovg;

import visualparticle.better.client.render.ui.UiCommand;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;
import org.joml.Matrix3x2fc;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.nanovg.NVGPaint;
import org.lwjgl.nanovg.NanoVG;
import org.lwjgl.nanovg.NanoVGGL3;
import org.lwjgl.system.MemoryUtil;

public final class NanoVGCanvas implements AutoCloseable {
	private static final String UI_FONT="visualparticle-ui",STRONG_FONT="visualparticle-ui-semibold",ICON_FONT="visualparticle-icons";
	private long context; private NVGColor color,secondColor; private NVGPaint paint;
	private ByteBuffer uiFontData,strongFontData,iconFontData;
	public void begin(float width,float height,float pixelRatio,Matrix3x2fc transform){ensureInitialized();NanoVG.nvgBeginFrame(context,width,height,pixelRatio);NanoVG.nvgSave(context);NanoVG.nvgTransform(context,transform.m00(),transform.m01(),transform.m10(),transform.m11(),transform.m20(),transform.m21());}
	public float textWidth(String value,float size,boolean strong){ensureInitialized();NanoVG.nvgFontFace(context,strong?STRONG_FONT:UI_FONT);NanoVG.nvgFontSize(context,size);float[] bounds=new float[4];NanoVG.nvgTextBounds(context,0,0,value==null?"":value,bounds);return Math.max(0,bounds[2]-bounds[0]);}
	public void render(List<UiCommand> commands){for(UiCommand command:commands){
		if(command instanceof UiCommand.Rect v)drawRect(v);else if(command instanceof UiCommand.GradientRect v)drawGradient(v);else if(command instanceof UiCommand.Outline v)drawOutline(v);else if(command instanceof UiCommand.Circle v)drawCircle(v);else if(command instanceof UiCommand.Line v)drawLine(v);else if(command instanceof UiCommand.Text v)drawText(v);else if(command instanceof UiCommand.Icon v)drawIcon(v);else if(command instanceof UiCommand.ColorPicker v)drawColorPicker(v);else if(command instanceof UiCommand.HueBar v)drawHueBar(v);else if(command instanceof UiCommand.AlphaBar v)drawAlphaBar(v);else if(command instanceof UiCommand.PushScissor v)pushScissor(v);else if(command instanceof UiCommand.PopScissor)NanoVG.nvgRestore(context);
	}}
	public void end(){NanoVG.nvgRestore(context);NanoVG.nvgEndFrame(context);}
	private void drawRect(UiCommand.Rect v){NanoVG.nvgBeginPath(context);NanoVG.nvgRoundedRect(context,v.x(),v.y(),v.width(),v.height(),v.radius());NanoVG.nvgFillColor(context,argb(v.color(),color));NanoVG.nvgFill(context);}
	private void drawGradient(UiCommand.GradientRect v){float ex=v.horizontal()?v.x()+v.width():v.x(),ey=v.horizontal()?v.y():v.y()+v.height();NanoVG.nvgLinearGradient(context,v.x(),v.y(),ex,ey,argb(v.startColor(),color),argb(v.endColor(),secondColor),paint);NanoVG.nvgBeginPath(context);NanoVG.nvgRoundedRect(context,v.x(),v.y(),v.width(),v.height(),v.radius());NanoVG.nvgFillPaint(context,paint);NanoVG.nvgFill(context);}
	private void drawOutline(UiCommand.Outline v){NanoVG.nvgBeginPath(context);NanoVG.nvgRoundedRect(context,v.x(),v.y(),v.width(),v.height(),v.radius());NanoVG.nvgStrokeWidth(context,v.thickness());NanoVG.nvgStrokeColor(context,argb(v.color(),color));NanoVG.nvgStroke(context);}
	private void drawCircle(UiCommand.Circle v){NanoVG.nvgBeginPath(context);NanoVG.nvgCircle(context,v.centerX(),v.centerY(),v.radius());NanoVG.nvgFillColor(context,argb(v.color(),color));NanoVG.nvgFill(context);}
	private void drawLine(UiCommand.Line v){NanoVG.nvgBeginPath(context);NanoVG.nvgMoveTo(context,v.x1(),v.y1());NanoVG.nvgLineTo(context,v.x2(),v.y2());NanoVG.nvgStrokeWidth(context,v.thickness());NanoVG.nvgStrokeColor(context,argb(v.color(),color));NanoVG.nvgStroke(context);}
	private void drawText(UiCommand.Text v){NanoVG.nvgFontFace(context,v.strong()?STRONG_FONT:UI_FONT);NanoVG.nvgFontSize(context,v.size());NanoVG.nvgTextAlign(context,horizontalAlign(v.align())|NanoVG.NVG_ALIGN_MIDDLE);NanoVG.nvgFillColor(context,argb(v.color(),color));NanoVG.nvgText(context,v.x(),v.y(),v.value());}
	private void drawColorPicker(UiCommand.ColorPicker v){
		int rgb=java.awt.Color.HSBtoRGB(v.hue(),1,1)&0xFFFFFF;
		NanoVG.nvgBeginPath(context);NanoVG.nvgRoundedRect(context,v.x(),v.y(),v.width(),v.height(),v.radius());NanoVG.nvgFillColor(context,argb(0xFF000000|rgb,color));NanoVG.nvgFill(context);
		NanoVG.nvgLinearGradient(context,v.x(),v.y(),v.x()+v.width(),v.y(),argb(0xFFFFFFFF,color),argb(0x00FFFFFF,secondColor),paint);NanoVG.nvgBeginPath(context);NanoVG.nvgRoundedRect(context,v.x(),v.y(),v.width(),v.height(),v.radius());NanoVG.nvgFillPaint(context,paint);NanoVG.nvgFill(context);
		NanoVG.nvgLinearGradient(context,v.x(),v.y(),v.x(),v.y()+v.height(),argb(0x00000000,color),argb(0xFF000000,secondColor),paint);NanoVG.nvgBeginPath(context);NanoVG.nvgRoundedRect(context,v.x(),v.y(),v.width(),v.height(),v.radius());NanoVG.nvgFillPaint(context,paint);NanoVG.nvgFill(context);
	}
	private void drawHueBar(UiCommand.HueBar v){
		int steps=96;float step=v.width()/steps;for(int i=0;i<steps;i++){int rgb=java.awt.Color.HSBtoRGB(i/(float)(steps-1),1,1);NanoVG.nvgBeginPath(context);NanoVG.nvgRect(context,v.x()+i*step,v.y(),step+0.8f,v.height());NanoVG.nvgFillColor(context,argb(0xFF000000|(rgb&0xFFFFFF),color));NanoVG.nvgFill(context);}
	}
	private void drawAlphaBar(UiCommand.AlphaBar v){
		int rgb=v.color()&0xFFFFFF;NanoVG.nvgLinearGradient(context,v.x(),v.y(),v.x()+v.width(),v.y(),argb(rgb,color),argb(0xFF000000|rgb,secondColor),paint);NanoVG.nvgBeginPath(context);NanoVG.nvgRoundedRect(context,v.x(),v.y(),v.width(),v.height(),v.radius());NanoVG.nvgFillPaint(context,paint);NanoVG.nvgFill(context);
	}	private void drawIcon(UiCommand.Icon v){
		NanoVG.nvgFontFace(context,ICON_FONT);
		NanoVG.nvgFontSize(context,v.size());
		NanoVG.nvgTextAlign(context,NanoVG.NVG_ALIGN_LEFT|NanoVG.NVG_ALIGN_BASELINE);
		NanoVG.nvgFillColor(context,argb(v.color(),color));
		String glyph=new String(Character.toChars(v.codePoint()));
		float[] bounds=new float[4];
		NanoVG.nvgTextBounds(context,0,0,glyph,bounds);
		float drawX=v.x()-(bounds[2]-bounds[0])*.5f-bounds[0];
		float drawY=v.y()-(bounds[3]-bounds[1])*.5f-bounds[1];
		NanoVG.nvgText(context,drawX,drawY,glyph);
	}	private void pushScissor(UiCommand.PushScissor v){NanoVG.nvgSave(context);NanoVG.nvgIntersectScissor(context,v.x(),v.y(),v.width(),v.height());}
	private void ensureInitialized(){if(context!=0)return;context=NanoVGGL3.nvgCreate(NanoVGGL3.NVG_ANTIALIAS|NanoVGGL3.NVG_STENCIL_STROKES);if(context==0)throw new IllegalStateException("Could not create NanoVG OpenGL context");color=NVGColor.calloc();secondColor=NVGColor.calloc();paint=NVGPaint.calloc();uiFontData=loadFont("/assets/visualparticle-better/fonts/inter-variable.ttf",UI_FONT);strongFontData=loadFont("/assets/visualparticle-better/fonts/inter-semibold.ttf",STRONG_FONT);iconFontData=loadFont("/assets/visualparticle-better/fonts/material-icons-round.otf",ICON_FONT);}
	private ByteBuffer loadFont(String path,String name){try(InputStream stream=NanoVGCanvas.class.getResourceAsStream(path)){if(stream==null)throw new IllegalStateException("Missing font resource: "+path);byte[] bytes=stream.readAllBytes();ByteBuffer buffer=MemoryUtil.memAlloc(bytes.length);buffer.put(bytes).flip();if(NanoVG.nvgCreateFontMem(context,name,buffer,false)==-1){MemoryUtil.memFree(buffer);throw new IllegalStateException("Could not load NanoVG font: "+name);}return buffer;}catch(IOException exception){throw new IllegalStateException("Could not read font resource: "+path,exception);}}
	private static int horizontalAlign(UiCommand.HorizontalAlign value){return switch(value){case LEFT->NanoVG.NVG_ALIGN_LEFT;case CENTER->NanoVG.NVG_ALIGN_CENTER;case RIGHT->NanoVG.NVG_ALIGN_RIGHT;};}
	private static NVGColor argb(int value,NVGColor target){target.r(((value>>16)&255)/255f);target.g(((value>>8)&255)/255f);target.b((value&255)/255f);target.a(((value>>>24)&255)/255f);return target;}
	@Override public void close(){if(context==0)return;NanoVGGL3.nvgDelete(context);context=0;color.free();secondColor.free();paint.free();MemoryUtil.memFree(uiFontData);MemoryUtil.memFree(strongFontData);MemoryUtil.memFree(iconFontData);color=secondColor=null;paint=null;uiFontData=strongFontData=iconFontData=null;}
}
