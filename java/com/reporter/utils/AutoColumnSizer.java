package com.reporter.utils;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFFont;

/**
 * Calculates the width of a column, based on the values within it.
 * <p>
 * For each new value added to the column, call {@link
#isNotificationRequired}.
 * If the result is true, call {@link #notifyCellValue}.
 */
public class AutoColumnSizer
{
    private static final short WIDTH_MIN = 40;
    private static final short WIDTH_MAX = 250;

    private static final short WIDTH_PADDING = 5;

    private static final int[] ROW_BAND = {2, 100, 1000, 10000, 65536};
    private static final int[] ROW_BAND_SAMPLE_FREQUENCY = {1, 10, 100,
1000};

    /** Graphics context used for obtaining FontMetrics objects */
    private Graphics2D graphics = null;
    /** Maps a Short (HSSF font index) to a FontMetrics object */
    private Map fontMetrics = new HashMap();

    private short currentWidth = WIDTH_MIN;

   
    public AutoColumnSizer()
    {
        // Nothing to do
    }

   
    private FontMetrics getFontMetrics(HSSFFont hf)
    {
        FontMetrics fm;
        Short pFont = new Short(hf.getIndex());

        fm = (FontMetrics) fontMetrics.get(pFont);
        if(fm == null) {
            // Lazy initialization of FontMetrics
            int style;
            if((hf.getBoldweight() == HSSFFont.BOLDWEIGHT_BOLD) || hf.getItalic()) {
                style = 0;
                if(hf.getBoldweight() == HSSFFont.BOLDWEIGHT_BOLD)
                    style ^= Font.BOLD;
                if(hf.getItalic())
                    style ^= Font.ITALIC;
            }
            else {
                style = Font.PLAIN;
            }
            // TODO - HSSFFonts can also be Subscript or Superscript
            Font f = new java.awt.Font(hf.getFontName(), style,
                    hf.getFontHeightInPoints());


            if(graphics == null) {
                // Lazy initialization of Graphics2D
                // Graphics & FontMetrics is not specified anywhere as threadsafe,
                // so each AutoColumnSizer creates its own
                // It would be faster to use one per thread. But overall performance
                // is already totally acceptable so I haven't bothered.
                // (It takes awhile the first time you run this code ina given VM,
                // but further runs are quick).
                BufferedImage i = new BufferedImage(1, 1, BufferedImage.TYPE_BYTE_GRAY);
                graphics = i.createGraphics();
            }

            fm = graphics.getFontMetrics(f); 
            fontMetrics.put(pFont, fm);
        }

        return fm;
    }

    /**
     * When you add a new value to a column, call this method to ask whether
     * the AutoColumnSizer is interested in it.
     */
    public boolean isNotificationRequired(int row)
    {
        if(row < 0) throw new IllegalArgumentException("illegal row: " + row);

        /* To improve performance, we calculate column widths based on
         * a SAMPLE of all rows. */

        // Find which band the row falls into...
        int rowBand = -1;       
        for (int band=0; band < ROW_BAND.length; band++) {
            if(row < ROW_BAND[band]) {
                rowBand = band - 1;
                break;
            }
        }

        if(rowBand == -1) {
            // Row doesn't fall into any band
            return false;
        }       
        else if((row % ROW_BAND_SAMPLE_FREQUENCY[rowBand]) != 0) {
            // Row isn't selected for our sample
            return false;
        }
        else {
            return true;
        }
    }

   
    public void notifyCellValue(String val, HSSFFont font)
    {
        if(val == null || val.length() == 0) return;
        if(font == null) throw new IllegalArgumentException("font is null");

        short width;
        {
            FontMetrics fm = getFontMetrics(font);
            int w = fm.stringWidth(val);
            width = (w > Short.MAX_VALUE) ? Short.MAX_VALUE : (short) w;
            // TODO - this gives an underestimate with large font-sizes.
            // TODO - What we *should* be considering is the 'displaywidth'.
            // This means we'd have to take into account cell type & format.
        }

        if(width > currentWidth) {
            currentWidth = width;
        }
    }

    public short getWidth()
    {
        //Multiply the result by a magic number (I use 48) and pass that to HSSFSheet.setColumnWidth.
        if((currentWidth + WIDTH_PADDING) <= WIDTH_MAX)
            return (short)(currentWidth + WIDTH_PADDING);
        else
            return WIDTH_MAX;
    }

    public void dispose()
    {
        if(graphics != null) {
            graphics.finalize();
            graphics = null;
        }
        fontMetrics = null;
    }


} 
/*
 * 
 */
/*
 * Rober,
To calculate column width HSSFSheet.autoSizeColumn uses Java2D classes
that throw exception if graphical environment is not available.
However, some tasks such as computing text layout can be
done in a headless mode, that is in the absence of graphical
environment.

To use headless mode you must set the following system property:
java.awt.headless=true
either via -Djava.awt.headless=true startup parameter or via
System.setProperty("java.awt.headless", "true").

As Dave mentioned, there can be slight differences in the
result:
 - Native graphical toolkits (WinXP or Unix with X11) are always
more precise than the headless toolkit. The way the font
metrics are calculated may be different in headless and headful modes.
Moreover, it may different on different platforms, i.e. there can be
minor differences between text metrics calculated under Solaris and
under WinXP. That is why projects that require high-precision,
platform-independent graphic calculations use their own mechanism to measure text (for
example, Apache FOP). I don't think it's an issue in our case.
Auto-sizing in Excel is a rough operation - user needs to
APPROXIMATELY set column width to fit the text.
 - If some of the fonts are not available, JDK uses the default font.
So if any fonts are missing in production you need to install them and
 copy font files to $JAVA_HOME/jre/lib/fonts/.

 P.S. It's good you raised this question. I'm going to update the docs
 to reflect it.

Regards,
Yegor

DF> Rober,

DF> As Andy says ask on the list.

DF> In your Tomcat startup on the unix server you need to be sure to set  
DF> the following property -

DF>   -Djava.awt.headless=true

DF> Do that and all should be good.

DF> If you notice slight differences in widths between windows and unix  
DF> that may be due to the fact that autoSizeColumn uses fonts to do its  
DF> magic. (Which is why it needs the "graphic environment") So, then you  
DF> might want to install fonts into /usr/java/jre/lib/fonts/ on the unix  
DF> box. If you get stuck with that then you can ask again - Yegor can  
DF> explain the exact rules.

DF> Regards,
DF> Dave

DF> On May 3, 2007, at 6:57 PM, bugzilla <at> apache.org wrote:

>> DO NOT REPLY TO THIS EMAIL, BUT PLEASE POST YOUR BUG·
>> RELATED COMMENTS THROUGH THE WEB INTERFACE AVAILABLE AT
>> <http://issues.apache.org/bugzilla/show_bug.cgi?id=42331>.
>> ANY REPLY MADE TO THIS MESSAGE WILL NOT BE COLLECTED AND·
>> INSERTED IN THE BUG DATABASE.
>>
>> http://issues.apache.org/bugzilla/show_bug.cgi?id=42331
>>
>>            Summary: autoSizeColumn. Exception in non graphical  
>> environment
>>            Product: POI
>>            Version: 3.0-dev
>>           Platform: Other
>>         OS/Version: other
>>             Status: NEW
>>           Severity: normal
>>           Priority: P2
>>          Component: HSSF
>>         AssignedTo: poi-dev <at> jakarta.apache.org
>>         ReportedBy: roberjruiz <at> yahoo.es
>>
>>
>> I developed a webapp that generates excel files. Development  
>> environment is
>> Tomcat server under Windows XP. Production server is a Unix machine  
>> with no
>> graphical environment.
>>
>> autoSizeColumn method, from HSSFSheet class, works ok in windows,  
>> but throws an
>> exception in unix environment. The lack of graphical environment  
>> seems to be the
>> reason.
>>

TA> Any other guess while we search for definite answer?

I would stick to "a". Looks most natural choice to me.
Even if it is not "a", the algorithm for width calculation will be
pretty close to Excel.

So the implementation plan gets more concrete:
 - get the number of characters for the default column width (default
 is 8)
 - get the default font (Arial)
 - compute width of the surrogate string "aaaaaaaa" using Java2D classes
 (java.awt.font.TextLayout).
 - derive a coefficient to translate 1/256th units to pixels.

 autosizing:
 - compute max(width) of the column cells using Java2D, translate it to
 1/256th units and resize the column.

Yegor

TA> -----Original Message-----
TA> From: Yegor Kozlov [mailto:yegor <at> dinom.ru] 
TA> Sent: Thursday, January 25, 2007 4:34 PM
TA> To: POI Users List
TA> Subject: Re[4]: Autosizing question:

TA>> I would love if you can elaborate a bit on "units of 1/256th of a
TA> character
TA>> width of the default font". Is the default font is system specific or
TA> same
TA>> across excel versions/platforms?

TA> The default font is workbook-specific.  You can get is as follows:

TA> HSSFFont font = workbook.getFontAt((short)0);
TA> There is always one with index 0. On Windows the default is Arial.

TA>> Is a specific character is used for width
TA>> calculation or we assume fixed width fonts?

TA> I don't know. Arial is default and it is a proportional font and the glyphs
TA> have different
TA> width. The phrase "1/256th of a character  width of the default font"
TA> is taken from the xls format spec. It says nothing about the
TA> used characters.

TA> Yegor

TA>> Wassalam
TA>> Tahir

TA>> -----Original Message-----
TA>> From: Yegor Kozlov [mailto:yegor <at> dinom.ru] 
TA>> Sent: Thursday, January 25, 2007 3:19 PM
TA>> To: POI Users List
TA>> Subject: Re[2]: Autosizing question:

TA>> Aha! Thank you for the link.

TA>> I think user's DPI is not an issue for autosizing.

TA>> Column width in Excel is expressed in units of 1/256th of a character
TA> width
TA>> of the default font.
TA>> In theory, if you calculate width of text and translate it to the XLS
TA>> units then the column width should not depend on client's DPI.

TA>>  A simple experiment to do: create an xls file, autosize a column and
TA>> try to view it on a system with a different DPI.

TA>> Regards,
TA>> Yegor

TA>>> I guess the issue is not in the font sizes but the screen dpi
TA>> calculation.
TA>>> Net result is if you render some text in "Ariel, 12" it will appear
TA>> smaller
TA>>> on screen as compared to same text & font rendered by windows natively.
TA>> See
TA>>> this bug report (and related bug 4016591) for details:
TA>>> http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4168757 . 

TA>>> Wassalam
TA>>> Tahir

TA>>> -----Original Message-----
TA>>> From: Yegor Kozlov [mailto:yegor <at> dinom.ru] 
TA>>> Sent: Thursday, January 25, 2007 2:28 PM
TA>>> To: POI Users List
TA>>> Subject: Re[2]: Autosizing question:

TA>>> Are you sure the Java's font sizes are incompatible with Windows fonts?
TA>>> Could you point me to the source of this information? I plan to add
TA>>> drawing capabilities to HSLF and this stuff is important to me.

TA>>> Regards,
TA>>> Yegor

ACO>>>> Its actually more compounded than that.  You can basically do this
TA>> with
ACO>>>> Java's AWT Font/FontMetrics classes but the sizes are incompatible
TA>> with
ACO>>>> Windows fonts (let alone Mac et al).  Someone has to record the data
TA>>> for
ACO>>>> Windows fonts and or an API that reads the font files and calculates
TA>>> them.

ACO>>>> -Andy

ACO>>>> Avik Sengupta wrote:
>>>>>> Since VBA have this function to autosize
>>>>>>     
>>>>>>> columns, I wonder if POI have
>>>>>>>       
>>>>>
>>>>> POI is not, and was never meant to be, a replacement for VBA. Its a
TA> file

>>>>> format reader/writer. To do autosizing, one needs a fontmetrics 
>>>>> implementation. We're yet find one that is useful and easy enuf to use.

>>>>>
>>>>> However, as a frequently requested feature, its a ripe oppurtunity for 
>>>>> contribution :)
>>>>>
>>>>> Regards
>>>>> -
>>>>> Avik
>>>>>
>>>>> On Wednesday 24 January 2007 17:36, Adelbert Groebbens wrote:
>>>>>   
>>>>>>> Autosizing question:
>>>>>>>
>>>>>>> Hello, is there a way to autosize the specified column
>>>>>>> in POI? Since VBA have this function to autosize
>>>>>>> columns, I wonder if POI have, if not, I have to
>>>>>>> setsize for each column, I think it would waste a lot
>>>>>>> of resource right? Thanks
>>>>>>>       
>>>>>> I'm facing the same problem.
>>>>>>
>>>>>> Did you find a solution?
>>>>>>
>>>>>> Bret Hart <im2heat <at> yahoo.com> writes:
>>>>>>
>>>>>>


--------------------------------------------------------
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFrame;



public class MainClass {

  public static void main(String[] a) throws Exception {
    JFrame jf = new JFrame("Demo");
    Container cp = jf.getContentPane();
    TextFormat tl = new TextFormat();
    cp.add(tl);
    jf.setSize(300, 200);
    jf.setVisible(true);
  }
}
class TextFormat extends JComponent {
   List layouts;
   Font font = new Font("SansSerif", Font.BOLD, 42);
   String text = "The quick brown fox jumped over the lazy cow";

  public void paint(Graphics g) {
    if (layouts == null)
      getLayouts(g);

    Point pen = new Point(0, 0);
    Graphics2D g2d = (Graphics2D)g;
    g2d.setColor(java.awt.Color.black); // or a property
    g2d.setFont(font);

    Iterator it = layouts.iterator();
    while (it.hasNext()) {
      TextLayout layout = (TextLayout) it.next();
      pen.y += (layout.getAscent());
      g2d.setFont(font);
      layout.draw(g2d, pen.x, pen.y);
      pen.y += layout.getDescent();
    }
  }

  private void getLayouts(Graphics g) {
    layouts = new ArrayList();

    Graphics2D g2d = (Graphics2D) g;
    FontRenderContext frc = g2d.getFontRenderContext();

    AttributedString attrStr = new AttributedString(text);
    attrStr.addAttribute(TextAttribute.FONT, font, 0, text.length());   
    LineBreakMeasurer measurer = new LineBreakMeasurer(
      attrStr.getIterator(), frc);
    float wrappingWidth;

    wrappingWidth = getSize().width - 15;

    while (measurer.getPosition() < text.length()) {
      TextLayout layout = measurer.nextLayout(wrappingWidth);
      layouts.add(layout);
    }
  }

  
}

*/