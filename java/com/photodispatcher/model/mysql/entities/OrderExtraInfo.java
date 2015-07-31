package com.photodispatcher.model.mysql.entities;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

@Table(name = "order_extra_info")
public class OrderExtraInfo extends AbstractEntity {
	private static final long serialVersionUID = 1L;

    @Id
    @Column(name="id")
    private String id;
    @Id
    @Column(name="sub_id")
    private String sub_id;
    
    @Column(name="endpaper")
    private String endpaper;
    @Column(name="interlayer")
    private String interlayer;
    @Column(name="calc_type")
    private String calc_type;
    @Column(name="cover")
    private String cover;
    @Column(name="format")
    private String format;
    @Column(name="corner_type")
    private String corner_type;
    @Column(name="kaptal")
    private String kaptal;
    @Column(name="cover_material")
    private String coverMaterial;
    @Column(name="books")
    private int books;
    @Column(name="sheets")
    private int sheets;
    @Column(name="date_in")
    private Date dateIn;
    @Column(name="date_out")
    private Date dateOut;
    @Column(name="book_thickness")
    private float bookThickness;
    @Column(name="remark")
    private String remark;
    @Column(name="paper")
    private String paper;
    @Column(name="calc_alias")
    private String calcAlias;
    @Column(name="calc_title")
    private String calcTitle;
    @Column(name="weight")
    private int weight;
    
    //ref
    @Column(name="book_type", updatable=false, insertable=false)
    private int book_type;
    @Column(name="book_part", updatable=false, insertable=false)
    private int book_part;
   
    //childs
    @Transient
    private List<OrderExtraMessage> messagesLog;

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getEndpaper() {
		return endpaper;
	}
	public void setEndpaper(String endpaper) {
		this.endpaper = endpaper;
	}
	public String getInterlayer() {
		return interlayer;
	}
	public void setInterlayer(String interlayer) {
		this.interlayer = interlayer;
	}
	public String getCalc_type() {
		return calc_type;
	}
	public void setCalc_type(String calc_type) {
		this.calc_type = calc_type;
	}
	public String getCover() {
		return cover;
	}
	public void setCover(String cover) {
		this.cover = cover;
	}
	public String getFormat() {
		return format;
	}
	public void setFormat(String format) {
		this.format = format;
	}
	public String getCorner_type() {
		return corner_type;
	}
	public void setCorner_type(String corner_type) {
		this.corner_type = corner_type;
	}
	public String getKaptal() {
		return kaptal;
	}
	public void setKaptal(String kaptal) {
		this.kaptal = kaptal;
	}
	public String getSub_id() {
		return sub_id;
	}
	public void setSub_id(String sub_id) {
		this.sub_id = sub_id;
	}
	public int getBook_type() {
		return book_type;
	}
	public void setBook_type(int book_type) {
		this.book_type = book_type;
	}
	public String getCoverMaterial() {
		return coverMaterial;
	}
	public void setCoverMaterial(String coverMaterial) {
		this.coverMaterial = coverMaterial;
	}
	public int getBooks() {
		return books;
	}
	public void setBooks(int books) {
		this.books = books;
	}
	public int getSheets() {
		return sheets;
	}
	public void setSheets(int sheets) {
		this.sheets = sheets;
	}
	public Date getDateIn() {
		return dateIn;
	}
	public void setDateIn(Date dateIn) {
		this.dateIn = dateIn;
	}
	public Date getDateOut() {
		return dateOut;
	}
	public void setDateOut(Date dateOut) {
		this.dateOut = dateOut;
	}
	public float getBookThickness() {
		return bookThickness;
	}
	public void setBookThickness(float bookThickness) {
		this.bookThickness = bookThickness;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public String getPaper() {
		return paper;
	}
	public void setPaper(String paper) {
		this.paper = paper;
	}
	public String getCalcAlias() {
		return calcAlias;
	}
	public void setCalcAlias(String calcAlias) {
		this.calcAlias = calcAlias;
	}
	public String getCalcTitle() {
		return calcTitle;
	}
	public void setCalcTitle(String calcTitle) {
		this.calcTitle = calcTitle;
	}
	public int getWeight() {
		return weight;
	}
	public void setWeight(int weight) {
		this.weight = weight;
	}
	public List<OrderExtraMessage> getMessagesLog() {
		return messagesLog;
	}
	public void setMessagesLog(List<OrderExtraMessage> messagesLog) {
		this.messagesLog = messagesLog;
	}
	public int getBook_part() {
		return book_part;
	}
	public void setBook_part(int book_part) {
		this.book_part = book_part;
	}
    
}
