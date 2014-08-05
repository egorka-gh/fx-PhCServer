package com.photodispatcher.model.mysql.entities;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "phcconfig.lab_print_code")
public class LabPrintCode extends AbstractEntity {
	private static final long serialVersionUID = 1L;

	//database props
    @Id
    @GeneratedValue
    @Column(name="id")
    private int id;
    @Column(name="src_type")
    private int src_type;
    @Column(name="src_id")
    private int src_id;
    @Column(name="prt_code")
    private String prt_code;
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
    @Column(name="is_duplex")
    private boolean is_duplex;
    @Column(name="is_pdf")
    private boolean is_pdf;
    @Column(name="roll")
    private int roll;
    
    //ref name
    @Column(name="paper_name", updatable=false, insertable=false)
    private String paper_name;
    @Column(name="frame_name", updatable=false, insertable=false)
    private String frame_name;
    @Column(name="correction_name", updatable=false, insertable=false)
    private String correction_name;
    @Column(name="cutting_name", updatable=false, insertable=false)
    private String cutting_name;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getSrc_type() {
		return src_type;
	}
	public void setSrc_type(int src_type) {
		this.src_type = src_type;
	}
	public int getSrc_id() {
		return src_id;
	}
	public void setSrc_id(int src_id) {
		this.src_id = src_id;
	}
	public String getPrt_code() {
		return prt_code;
	}
	public void setPrt_code(String prt_code) {
		this.prt_code = prt_code;
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
	public int getRoll() {
		return roll;
	}
	public void setRoll(int roll) {
		this.roll = roll;
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

    

}
