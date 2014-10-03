package com.reporter.utils;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.Font;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.font.FontRenderContext;
import java.text.AttributedString;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;


public class XlsFontUtils {

    /** Graphics context */
    static private Graphics2D graphics = null;
    
    /** Maps a Short (HSSF font index) to a Font object */
    static private Map<Short, Font> fonts = new HashMap<Short, Font>();
    
    private HSSFWorkbook workbook = null;
    
    private double unitHFactor;
    private double unitVFactor;

    /**
     * 
     */
    public XlsFontUtils(HSSFWorkbook workbook) {
        this.workbook = workbook;
        //System.setProperty("java.awt.headless", "true");
        BufferedImage i = new BufferedImage(1, 1, BufferedImage.TYPE_BYTE_GRAY);
        graphics = i.createGraphics();
        //detect coefficients
        FontRenderContext frc = graphics.getFontRenderContext();
        //use standart font as Arial/PLAIN/10
        //use sie*2 to make it more precision
        //Font fnt = new java.awt.Font("Tahoma", Font.PLAIN, 9*2);
        Font fnt = new java.awt.Font("Arial", Font.PLAIN, 10*2);
        //Font fnt = new java.awt.Font("Arial", Font.PLAIN, 10);
        TextLayout tl = new TextLayout("88888888", fnt, frc);
        tl.draw(graphics, 100, 100);
        Double wd = tl.getBounds().getWidth();
        unitHFactor = 2304*2/(wd);
        double ht = tl.getBounds().getHeight();
        unitVFactor = 255*2/(ht);
    }

    private Font getFont(short fontIndex){
        Font result = fonts.get(fontIndex);
        if (result == null){
            HSSFFont hFnt = workbook.getFontAt(fontIndex);
            int style;
            if((hFnt.getBoldweight() == HSSFFont.BOLDWEIGHT_BOLD) || hFnt.getItalic()) {
                style = 0;
                if(hFnt.getBoldweight() == HSSFFont.BOLDWEIGHT_BOLD){
                    style ^= Font.BOLD;
                }
                if(hFnt.getItalic()){
                    style ^= Font.ITALIC;
                }
            } else {
                style = Font.PLAIN;
            }
            // TODO - HSSFFonts can also be Subscript or Superscript
            //result = new java.awt.Font(hFnt.getFontName(), style, hFnt.getFontHeightInPoints()*2);
            result = new java.awt.Font(hFnt.getFontName(), style, hFnt.getFontHeightInPoints());
            fonts.put(fontIndex, result);
        }
        return result;
    }

    public short getWidth(String val, short fontIndex){
        if(val == null || val.length() == 0) return -1;
        short width;
        {
            FontRenderContext frc = graphics.getFontRenderContext();
            TextLayout tl = new TextLayout(val, getFont(fontIndex), frc);
            tl.draw(graphics, 100, 100);
            Double wd = tl.getBounds().getWidth();
            wd = wd*unitHFactor;
            int w = wd.intValue();
            width = (w > Short.MAX_VALUE) ? Short.MAX_VALUE : (short) w;
            
            // TODO - this gives an underestimate with large font-sizes.
            // TODO - What we *should* be considering is the 'displaywidth'.
            // This means we'd have to take into account cell type & format.
        }
        return width;
    }

    public short getWrappedHeight(String text, short fontIndex, short columnWidth){
        short height = -1;
        //string attributed ws font 
        AttributedString attrStr = new AttributedString(text);
        attrStr.addAttribute(TextAttribute.FONT, getFont(fontIndex), 0, text.length());
        FontRenderContext frc = graphics.getFontRenderContext();
        //try to wrapp text
        LineBreakMeasurer measurer = new LineBreakMeasurer(attrStr.getIterator(), frc);
        float wrappingWidth = (float) columnWidth;
        wrappingWidth = wrappingWidth /(float)unitHFactor;
        Double ht = (double) 0;
        while (measurer.getPosition() < text.length()) {
            TextLayout tl = measurer.nextLayout(wrappingWidth);
            ht += tl.getBounds().getHeight();
        }
        ht= ht*unitVFactor;
        int h= ht.intValue();
        height = (h > Short.MAX_VALUE) ? Short.MAX_VALUE : (short) h;
        return height;
    }

    protected void dispose() {
        if(graphics != null) {
            graphics.finalize();
            graphics = null;
        }
        fonts = null;
    }

}
