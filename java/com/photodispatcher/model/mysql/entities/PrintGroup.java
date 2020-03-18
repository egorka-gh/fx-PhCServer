package com.photodispatcher.model.mysql.entities;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

@Table(name = "print_group")
public class PrintGroup extends AbstractEntity {
	private static final long serialVersionUID = 1L;

    @Id
    @Column(name="id")
    private String id;
    @Column(name="order_id")
    private String order_id;
    @Column(name="sub_id")
    private String sub_id;
    @Column(name="state")
    private int state;
    @Column(name="state_date")
    private Date state_date;
    @Column(name="path")
    private String path;
    @Column(name="width")
    private int width;
    @Column(name="height")
    private int height;
    @Column(name="paper")
    private int paper;
    @Column(name="frame")
    private int frame;
    @Column(name="correction")
    private int correction;
    @Column(name="cutting")
    private int cutting;
    @Column(name="file_num")
    private int file_num;
    @Column(name="destination")
    private int destination;
    @Column(name="book_type")
    private int book_type;
    @Column(name="book_part")
    private int book_part;
    @Column(name="book_num")
    private int book_num;
    @Column(name="sheet_num")
    private int sheet_num;
    @Column(name="is_duplex")
    private boolean is_duplex;
    @Column(name="is_pdf")
    private boolean is_pdf;
    @Column(name="is_reprint")
    private boolean is_reprint;
    @Column(name="prints")
    private int prints;
    @Column(name="reprint_id")
    private String reprint_id;
    @Column(name="butt")
    private int butt;
    @Column(name="laminat")
    private int laminat;
    
    @Column(name="prints_done")
    private int prints_done;

    @Column(name="prn_queue")
    private int prn_queue;

    @Column(name="alias")
    private String alias;

    @Column(name="sheets_per_file")
    private int sheets_per_file;
    @Column(name="is_revers")
    private boolean is_revers;
    @Column(name="compo_type")
    private int compo_type;

    
    /*
    @Transient
    private BookPgTemplate bookTemplate;
    */
	
	//ref
    @Column(name="source_id", insertable=false, updatable=false)
    private int source_id;
    @Column(name="order_folder", insertable=false, updatable=false)
    private String order_folder;
    @Column(name="source_name", insertable=false, updatable=false)
    private String source_name;
    @Column(name="state_name", insertable=false, updatable=false)
    private String state_name;
    @Column(name="paper_name", insertable=false, updatable=false)
    private String paper_name;
    @Column(name="frame_name", insertable=false, updatable=false)
    private String frame_name;
    @Column(name="correction_name", insertable=false, updatable=false)
    private String correction_name;
    @Column(name="cutting_name", insertable=false, updatable=false)
    private String cutting_name;
    @Column(name="lab_name", insertable=false, updatable=false)
    private String lab_name;
    @Column(name="book_type_name", insertable=false, updatable=false)
    private String book_type_name;
    @Column(name="book_part_name", insertable=false, updatable=false)
    private String book_part_name;
    @Column(name="min_sheet", insertable=false, updatable=false)
    private int min_sheet;
    @Column(name="max_sheet", insertable=false, updatable=false)
    private int max_sheet;
    @Column(name="laminat_name", updatable=false, insertable=false)
    private String laminat_name;
    @Column(name="compo_type_name", updatable=false, insertable=false)
    private String compo_type_name;

    //childs
    @Transient
    private List<PrintGroupFile> files;
    @Transient
    private List<PrintGroupReject> rejects;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getOrder_id() {
		return order_id;
	}

	public void setOrder_id(String order_id) {
		this.order_id = order_id;
	}

	public String getSub_id() {
		return sub_id;
	}

	public void setSub_id(String sub_id) {
		this.sub_id = sub_id;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public Date getState_date() {
		return state_date;
	}

	public void setState_date(Date state_date) {
		this.state_date = state_date;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
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

	public int getFile_num() {
		return file_num;
	}

	public void setFile_num(int file_num) {
		this.file_num = file_num;
	}

	public int getDestination() {
		return destination;
	}

	public void setDestination(int destination) {
		this.destination = destination;
	}

	public int getBook_type() {
		return book_type;
	}

	public void setBook_type(int book_type) {
		this.book_type = book_type;
	}

	public int getBook_part() {
		return book_part;
	}

	public void setBook_part(int book_part) {
		this.book_part = book_part;
	}

	public int getBook_num() {
		return book_num;
	}

	public void setBook_num(int book_num) {
		this.book_num = book_num;
	}

	public int getSheet_num() {
		return sheet_num;
	}

	public void setSheet_num(int sheet_num) {
		this.sheet_num = sheet_num;
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

	public boolean isIs_reprint() {
		return is_reprint;
	}

	public void setIs_reprint(boolean is_reprint) {
		this.is_reprint = is_reprint;
	}

	public int getPrints() {
		return prints;
	}

	public void setPrints(int prints) {
		this.prints = prints;
	}

	public int getPrints_done() {
		return prints_done;
	}

	public void setPrints_done(int prints_done) {
		this.prints_done = prints_done;
	}

	public int getSource_id() {
		return source_id;
	}

	public void setSource_id(int source_id) {
		this.source_id = source_id;
	}

	public String getOrder_folder() {
		return order_folder;
	}

	public void setOrder_folder(String order_folder) {
		this.order_folder = order_folder;
	}

	public String getSource_name() {
		return source_name;
	}

	public void setSource_name(String source_name) {
		this.source_name = source_name;
	}

	public String getState_name() {
		return state_name;
	}

	public void setState_name(String state_name) {
		this.state_name = state_name;
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

	public String getLab_name() {
		return lab_name;
	}

	public void setLab_name(String lab_name) {
		this.lab_name = lab_name;
	}

	public String getBook_type_name() {
		return book_type_name;
	}

	public void setBook_type_name(String book_type_name) {
		this.book_type_name = book_type_name;
	}

	public String getBook_part_name() {
		return book_part_name;
	}

	public void setBook_part_name(String book_part_name) {
		this.book_part_name = book_part_name;
	}

	public List<PrintGroupFile> getFiles() {
		return files;
	}

	public void setFiles(List<PrintGroupFile> files) {
		this.files = files;
	}

	public String getReprint_id() {
		return reprint_id;
	}

	public void setReprint_id(String reprint_id) {
		this.reprint_id = reprint_id;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public int getButt() {
		return butt;
	}

	public void setButt(int butt) {
		this.butt = butt;
	}

	public int getPrn_queue() {
		return prn_queue;
	}

	public void setPrn_queue(int prn_queue) {
		this.prn_queue = prn_queue;
	}

	public int getSheets_per_file() {
		return sheets_per_file;
	}

	public void setSheets_per_file(int sheets_per_file) {
		this.sheets_per_file = sheets_per_file;
	}

	public boolean isIs_revers() {
		return is_revers;
	}

	public void setIs_revers(boolean is_revers) {
		this.is_revers = is_revers;
	}

	public int getMin_sheet() {
		return min_sheet;
	}

	public void setMin_sheet(int min_sheet) {
		this.min_sheet = min_sheet;
	}

	public int getMax_sheet() {
		return max_sheet;
	}

	public void setMax_sheet(int max_sheet) {
		this.max_sheet = max_sheet;
	}

	public List<PrintGroupReject> getRejects() {
		return rejects;
	}

	public void setRejects(List<PrintGroupReject> rejects) {
		this.rejects = rejects;
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

	public String getCompo_type_name() {
		return compo_type_name;
	}

	public void setCompo_type_name(String compo_type_name) {
		this.compo_type_name = compo_type_name;
	}
    
}
