package com.photodispatcher.model.mysql.entities;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

@Table(name = "book_pg_template")
public class BookPgTemplate extends AbstractEntity {
	private static final long serialVersionUID = 1L;
	
	//database props
    @Id
    @GeneratedValue
    @Column(name="id")
    private int id;
    @Column(name="book", updatable=false)
    private int book;
    @Column(name="book_part")
    private int book_part;
    @Column(name="width")
    private int width;
    @Column(name="height")
    private int height;
    @Column(name="height_add")
    private int height_add;
    @Column(name="paper")
    private int paper=0;
    @Column(name="frame")
    private int frame=0;
    @Column(name="correction")
    private int correction=0;
    @Column(name="cutting")
    private int cutting=0;
    @Column(name="is_duplex")
    private boolean is_duplex=false;
    @Column(name="is_pdf")
    private boolean is_pdf=false;
    @Column(name="is_sheet_ready")
    private boolean is_sheet_ready=false;
    @Column(name="sheet_width")
    private int sheet_width=0;
    @Column(name="sheet_len")
    private int sheet_len=0;
    @Column(name="page_width")
    private int page_width=0;
    @Column(name="page_len")
    private int page_len=0;
    @Column(name="page_hoffset")
    private int page_hoffset=0;
    @Column(name="font_size")
    private int font_size=0;
    @Column(name="font_offset")
    private String font_offset="+500+0";
    @Column(name="fontv_size")
    private int fontv_size=0;
    @Column(name="fontv_offset")
    private String fontv_offset="+0+500";
    @Column(name="notching")
    private int notching=0;
    @Column(name="stroke")
    private int stroke=0;
    @Column(name="bar_size")
    private int bar_size=0;
    @Column(name="bar_offset")
    private String bar_offset="+0+0";
    @Column(name="tech_bar")
    private int tech_bar=0;
    @Column(name="tech_bar_step")
    private float tech_bar_step=4;
    @Column(name="tech_bar_color")
    private String tech_bar_color="200000";
    @Column(name="tech_add")
    private int tech_add=4;
    @Column(name="is_tech_center")
    private boolean is_tech_center=true;
    @Column(name="tech_bar_offset")
    private String tech_bar_offset="+0-200";
    @Column(name="is_tech_top")
    private boolean is_tech_top=false;
    @Column(name="tech_bar_toffset")
    private String tech_bar_toffset="+0+0";
    @Column(name="is_tech_bot")
    private boolean is_tech_bot=false;
    @Column(name="tech_bar_boffset")
    private String tech_bar_boffset="+0+0";
    @Column(name="tech_stair_add")
    private int tech_stair_add;
    @Column(name="tech_stair_step")
    private int tech_stair_step;
    @Column(name="is_tech_stair_top")
    private boolean is_tech_stair_top=false;
    @Column(name="is_tech_stair_bot")
    private boolean is_tech_stair_bot=false;
    @Column(name="lab_type")
    private int lab_type;
    @Column(name="revers")
    private boolean revers=false;
    @Column(name="mark_size")
    private int mark_size=0;
    @Column(name="mark_offset")
    private String mark_offset="+0+0";
    @Column(name="reprint_size")
    private int reprint_size=0;
    @Column(name="reprint_offset")
    private String reprint_offset="+0+0";
    @Column(name="queue_size")
    private int queue_size=0;
    @Column(name="queue_offset")
    private String queue_offset="+0+0";
    @Column(name="laminat")
    private int laminat=0;

    //ref name
    @Column(name="book_part_name", updatable=false, insertable=false)
    private String book_part_name;
    @Column(name="paper_name", updatable=false, insertable=false)
    private String paper_name;
    @Column(name="frame_name", updatable=false, insertable=false)
    private String frame_name;
    @Column(name="correction_name", updatable=false, insertable=false)
    private String correction_name;
    @Column(name="cutting_name", updatable=false, insertable=false)
    private String cutting_name;
    @Column(name="lab_type_name", updatable=false, insertable=false)
    private String lab_type_name;
    @Column(name="laminat_name", updatable=false, insertable=false)
    private String laminat_name;
    
    
    @Transient
    private List<BookPgAltPaper> altPaper;

    @Transient
    private int compo_type;

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getBook() {
		return book;
	}
	public void setBook(int book) {
		this.book = book;
	}
	public int getBook_part() {
		return book_part;
	}
	public void setBook_part(int book_part) {
		this.book_part = book_part;
	}
	public int getWidth() {
		return width;
	}
	public void setWidth(int width) {
		this.width = width;
	}
	public int getHeight() {
		return height;
	}
	public void setHeight(int height) {
		this.height = height;
	}
	public int getHeight_add() {
		return height_add;
	}
	public void setHeight_add(int height_add) {
		this.height_add = height_add;
	}
	public int getPaper() {
		return paper;
	}
	public void setPaper(int paper) {
		this.paper = paper;
	}
	public int getFrame() {
		return frame;
	}
	public void setFrame(int frame) {
		this.frame = frame;
	}
	public int getCorrection() {
		return correction;
	}
	public void setCorrection(int correction) {
		this.correction = correction;
	}
	public int getCutting() {
		return cutting;
	}
	public void setCutting(int cutting) {
		this.cutting = cutting;
	}
	public boolean isIs_duplex() {
		return is_duplex;
	}
	public void setIs_duplex(boolean is_duplex) {
		this.is_duplex = is_duplex;
	}
	public boolean isIs_pdf() {
		return is_pdf;
	}
	public void setIs_pdf(boolean is_pdf) {
		this.is_pdf = is_pdf;
	}
	public boolean isIs_sheet_ready() {
		return is_sheet_ready;
	}
	public void setIs_sheet_ready(boolean is_sheet_ready) {
		this.is_sheet_ready = is_sheet_ready;
	}
	public int getSheet_width() {
		return sheet_width;
	}
	public void setSheet_width(int sheet_width) {
		this.sheet_width = sheet_width;
	}
	public int getSheet_len() {
		return sheet_len;
	}
	public void setSheet_len(int sheet_len) {
		this.sheet_len = sheet_len;
	}
	public int getPage_width() {
		return page_width;
	}
	public void setPage_width(int page_width) {
		this.page_width = page_width;
	}
	public int getPage_len() {
		return page_len;
	}
	public void setPage_len(int page_len) {
		this.page_len = page_len;
	}
	public int getPage_hoffset() {
		return page_hoffset;
	}
	public void setPage_hoffset(int page_hoffset) {
		this.page_hoffset = page_hoffset;
	}
	public int getFont_size() {
		return font_size;
	}
	public void setFont_size(int font_size) {
		this.font_size = font_size;
	}
	public String getFont_offset() {
		return font_offset;
	}
	public void setFont_offset(String font_offset) {
		this.font_offset = font_offset;
	}
	public int getFontv_size() {
		return fontv_size;
	}
	public void setFontv_size(int fontv_size) {
		this.fontv_size = fontv_size;
	}
	public String getFontv_offset() {
		return fontv_offset;
	}
	public void setFontv_offset(String fontv_offset) {
		this.fontv_offset = fontv_offset;
	}
	public int getNotching() {
		return notching;
	}
	public void setNotching(int notching) {
		this.notching = notching;
	}
	public int getStroke() {
		return stroke;
	}
	public void setStroke(int stroke) {
		this.stroke = stroke;
	}
	public int getBar_size() {
		return bar_size;
	}
	public void setBar_size(int bar_size) {
		this.bar_size = bar_size;
	}
	public String getBar_offset() {
		return bar_offset;
	}
	public void setBar_offset(String bar_offset) {
		this.bar_offset = bar_offset;
	}
	public int getTech_bar() {
		return tech_bar;
	}
	public void setTech_bar(int tech_bar) {
		this.tech_bar = tech_bar;
	}
	public float getTech_bar_step() {
		return tech_bar_step;
	}
	public void setTech_bar_step(float tech_bar_step) {
		this.tech_bar_step = tech_bar_step;
	}
	public String getTech_bar_color() {
		return tech_bar_color;
	}
	public void setTech_bar_color(String tech_bar_color) {
		this.tech_bar_color = tech_bar_color;
	}
	public int getTech_add() {
		return tech_add;
	}
	public void setTech_add(int tech_add) {
		this.tech_add = tech_add;
	}
	public boolean isIs_tech_center() {
		return is_tech_center;
	}
	public void setIs_tech_center(boolean is_tech_center) {
		this.is_tech_center = is_tech_center;
	}
	public String getTech_bar_offset() {
		return tech_bar_offset;
	}
	public void setTech_bar_offset(String tech_bar_offset) {
		this.tech_bar_offset = tech_bar_offset;
	}
	public boolean isIs_tech_top() {
		return is_tech_top;
	}
	public void setIs_tech_top(boolean is_tech_top) {
		this.is_tech_top = is_tech_top;
	}
	public String getTech_bar_toffset() {
		return tech_bar_toffset;
	}
	public void setTech_bar_toffset(String tech_bar_toffset) {
		this.tech_bar_toffset = tech_bar_toffset;
	}
	public boolean isIs_tech_bot() {
		return is_tech_bot;
	}
	public void setIs_tech_bot(boolean is_tech_bot) {
		this.is_tech_bot = is_tech_bot;
	}
	public String getTech_bar_boffset() {
		return tech_bar_boffset;
	}
	public void setTech_bar_boffset(String tech_bar_boffset) {
		this.tech_bar_boffset = tech_bar_boffset;
	}
	public int getTech_stair_add() {
		return tech_stair_add;
	}
	public void setTech_stair_add(int tech_stair_add) {
		this.tech_stair_add = tech_stair_add;
	}
	public int getTech_stair_step() {
		return tech_stair_step;
	}
	public void setTech_stair_step(int tech_stair_step) {
		this.tech_stair_step = tech_stair_step;
	}
	public boolean isIs_tech_stair_top() {
		return is_tech_stair_top;
	}
	public void setIs_tech_stair_top(boolean is_tech_stair_top) {
		this.is_tech_stair_top = is_tech_stair_top;
	}
	public boolean isIs_tech_stair_bot() {
		return is_tech_stair_bot;
	}
	public void setIs_tech_stair_bot(boolean is_tech_stair_bot) {
		this.is_tech_stair_bot = is_tech_stair_bot;
	}
	public String getBook_part_name() {
		return book_part_name;
	}
	public void setBook_part_name(String book_part_name) {
		this.book_part_name = book_part_name;
	}
	public String getPaper_name() {
		return paper_name;
	}
	public void setPaper_name(String paper_name) {
		this.paper_name = paper_name;
	}
	public String getFrame_name() {
		return frame_name;
	}
	public void setFrame_name(String frame_name) {
		this.frame_name = frame_name;
	}
	public String getCorrection_name() {
		return correction_name;
	}
	public void setCorrection_name(String correction_name) {
		this.correction_name = correction_name;
	}
	public String getCutting_name() {
		return cutting_name;
	}
	public void setCutting_name(String cutting_name) {
		this.cutting_name = cutting_name;
	}
	public int getLab_type() {
		return lab_type;
	}
	public void setLab_type(int lab_type) {
		this.lab_type = lab_type;
	}
	public String getLab_type_name() {
		return lab_type_name;
	}
	public void setLab_type_name(String lab_type_name) {
		this.lab_type_name = lab_type_name;
	}
	public List<BookPgAltPaper> getAltPaper() {
		return altPaper;
	}
	public void setAltPaper(List<BookPgAltPaper> altPaper) {
		this.altPaper = altPaper;
	}
	public boolean isRevers() {
		return revers;
	}
	public void setRevers(boolean revers) {
		this.revers = revers;
	}
	public int getMark_size() {
		return mark_size;
	}
	public void setMark_size(int mark_size) {
		this.mark_size = mark_size;
	}
	public String getMark_offset() {
		return mark_offset;
	}
	public void setMark_offset(String mark_offset) {
		this.mark_offset = mark_offset;
	}
	public int getReprint_size() {
		return reprint_size;
	}
	public void setReprint_size(int reprint_size) {
		this.reprint_size = reprint_size;
	}
	public String getReprint_offset() {
		return reprint_offset;
	}
	public void setReprint_offset(String reprint_offset) {
		this.reprint_offset = reprint_offset;
	}
	public int getQueue_size() {
		return queue_size;
	}
	public void setQueue_size(int queue_size) {
		this.queue_size = queue_size;
	}
	public String getQueue_offset() {
		return queue_offset;
	}
	public void setQueue_offset(String queue_offset) {
		this.queue_offset = queue_offset;
	}
	public int getLaminat() {
		return laminat;
	}
	public void setLaminat(int laminat) {
		this.laminat = laminat;
	}
	public String getLaminat_name() {
		return laminat_name;
	}
	public void setLaminat_name(String laminat_name) {
		this.laminat_name = laminat_name;
	}
	public int getCompo_type() {
		return compo_type;
	}
	public void setCompo_type(int compo_type) {
		this.compo_type = compo_type;
	}

}
