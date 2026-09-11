package visualparticle.better.client.render.ui;

public sealed interface UiCommand permits UiCommand.Rect, UiCommand.GradientRect, UiCommand.Outline,
		UiCommand.Circle, UiCommand.Line, UiCommand.PushScissor, UiCommand.PopScissor,
		UiCommand.Text, UiCommand.Icon, UiCommand.ColorPicker, UiCommand.HueBar, UiCommand.AlphaBar {
	enum HorizontalAlign { LEFT, CENTER, RIGHT }
	record Rect(float x,float y,float width,float height,float radius,int color) implements UiCommand {}
	record GradientRect(float x,float y,float width,float height,float radius,int startColor,int endColor,boolean horizontal) implements UiCommand {}
	record Outline(float x,float y,float width,float height,float radius,float thickness,int color) implements UiCommand {}
	record Circle(float centerX,float centerY,float radius,int color) implements UiCommand {}
	record Line(float x1,float y1,float x2,float y2,float thickness,int color) implements UiCommand {}
	record PushScissor(float x,float y,float width,float height) implements UiCommand {}
	record PopScissor() implements UiCommand {}
	record Text(float x,float y,float size,int color,String value,HorizontalAlign align,boolean strong) implements UiCommand {}
	record Icon(float x,float y,float size,int color,int codePoint) implements UiCommand {}
	record ColorPicker(float x,float y,float width,float height,float radius,float hue) implements UiCommand {}
	record HueBar(float x,float y,float width,float height,float radius) implements UiCommand {}
	record AlphaBar(float x,float y,float width,float height,float radius,int color) implements UiCommand {}
}
