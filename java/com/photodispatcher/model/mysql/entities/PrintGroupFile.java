package com.photodispatcher.model.mysql.entities;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "print_group_file")
public class PrintGroupFile extends AbstractEntity {
	private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    @Column(name="id")
    private int id;
    @Column(name="print_group")
    private String print_group;
    @Column(name="file_name")
    private String file_name;
    @Column(name="prt_qty")
    private int prt_qty;
    @Column(name="book_num")
    private int book_num;
    @Column(name="page_num")
    private int page_num;
    @Column(name="caption")
    private String caption;
    @Column(name="book_part")
    private int book_part;
    @Column(name="printed")
    private boolean printed;
    /*
    @Column(name="print_forvard")
    private boolean print_forvard;
    */

	//ref props
    @Column(name="path", insertable=false, updatable=false)
    private String path;
    @Column(name="tech_point", insertable=false, updatable=false)
    private int tech_point;
    @Column(name="tech_point_name", insertable=false, updatable=false)
    private String tech_point_name;
    @Column(name="tech_state", insertable=false, updatable=false)
    private int tech_state;
    @Column(name="tech_state_name", insertable=false, updatable=false)
    private String tech_state_name;
    @Column(name="tech_date", insertable=false, updatable=false)
    private String tech_date;
    @Column(name="book_part_name", insertable=false, updatable=false)
    private String book_part_name;
    
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getPrint_group() {
		return print_group;
	}
	public void setPrint_group(String print_group) {
		this.print_group = print_group;
	}
	public String getFile_name() {
		return file_name;
	}
	public void setFile_name(String file_name) {
		this.file_name = file_name;
	}
	public int getPrt_qty() {
		return prt_qty;
	}
	public void setPrt_qty(int prt_qty) {
		this.prt_qty = prt_qty;
	}
	public int getBook_num() {
		return book_num;
	}
	public void setBook_num(int book_num) {
		this.book_num = book_num;
	}
	public int getPage_num() {
		return page_num;
	}
	public void setPage_num(int page_num) {
		this.page_num = page_num;
	}
	public String getCaption() {
		return caption;
	}
	public void setCaption(String caption) {
		this.caption = caption;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public int getTech_point() {
		return tech_point;
	}
	public void setTech_point(int tech_point) {
		this.tech_point = tech_point;
	}
	public String getTech_point_name() {
		return tech_point_name;
	}
	public void setTech_point_name(String tech_point_name) {
		this.tech_point_name = tech_point_name;
	}
	public int getTech_state() {
		return tech_state;
	}
	public void setTech_state(int tech_state) {
		this.tech_state = tech_state;
	}
	public String getTech_state_name() {
		return tech_state_name;
	}
	public void setTech_state_name(String tech_state_name) {
		this.tech_state_name = tech_state_name;
	}
	public String getTech_date() {
		return tech_date;
	}
	public void setTech_date(String tech_date) {
		this.tech_date = tech_date;
	}
	public String getBook_part_name() {
		return book_part_name;
	}
	public void setBook_part_name(String book_part_name) {
		this.book_part_name = book_part_name;
	}
	public int getBook_part() {
		return book_part;
	}
	public void setBook_part(int book_part) {
		this.book_part = book_part;
	}
	public boolean isPrinted() {
		return printed;
	}
	public void setPrinted(boolean printed) {
		this.printed = printed;
	}
    

}
