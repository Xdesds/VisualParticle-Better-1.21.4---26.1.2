package visualparticle.better.client.setting;

public final class ColorSetting extends Setting<Integer> {
    public ColorSetting(String name,String hint,int argb){super(name,hint,argb);}
    public int argb(){return value();}
    public void nextHue(){int value=value();float[] hsb=java.awt.Color.RGBtoHSB(value>>16&255,value>>8&255,value&255,null);int rgb=java.awt.Color.HSBtoRGB((hsb[0]+1f/12f)%1f,Math.max(.45f,hsb[1]),Math.max(.72f,hsb[2]));set(value&0xFF000000|rgb&0xFFFFFF);}
    public String hex(){return String.format(java.util.Locale.ROOT,"#%06X",argb()&0xFFFFFF);}
}