package com.photodispatcher.model.mysql.entities;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

@Table(name = "layerset")
public class Layerset extends AbstractEntity {
	private static final long serialVersionUID = 1L;
	
    @Id
    @GeneratedValue
    @Column(name="id")
    private int id;
    @Column(name="layerset_group")
    private int layerset_group;
    @Column(name="subset_type")
    private int subset_type;
    @Column(name="name")
    private String name;
    @Column(name="book_type")
    private int book_type;
    @Column(name="pdf")
    private boolean is_pdf;
    @Column(name="passover")
    private boolean is_passover;
    @Column(name="book_check_off")
    private boolean is_book_check_off;
    @Column(name="epaper_check_off")
    private boolean is_epaper_check_off;

	//db drived
    @Column(name="book_type_name", updatable=false, insertable=false)
    private String book_type_name;

	//db childs
    //layerAllocation:Array; only on client side
    @Transient
    private List<LayerSequence> sequenceStart;
    @Transient
    private List<LayerSequence> sequenceMiddle;
    @Transient
    private List<LayerSequence> sequenceEnd;
    @Transient
    private List<String> synonyms;
    @Transient
    private boolean usesEndPaper;
    
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getLayerset_group() {
		return layerset_group;
	}
	public void setLayerset_group(int layerset_group) {
		this.layerset_group = layerset_group;
	}
	public int getSubset_type() {
		return subset_type;
	}
	public void setSubset_type(int subset_type) {
		this.subset_type = subset_type;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getBook_type() {
		return book_type;
	}
	public void setBook_type(int book_type) {
		this.book_type = book_type;
	}
	public boolean isIs_pdf() {
		return is_pdf;
	}
	public void setIs_pdf(boolean is_pdf) {
		this.is_pdf = is_pdf;
	}
	public boolean isIs_passover() {
		return is_passover;
	}
	public void setIs_passover(boolean is_passover) {
		this.is_passover = is_passover;
	}
	public boolean isIs_book_check_off() {
		return is_book_check_off;
	}
	public void setIs_book_check_off(boolean is_book_check_off) {
		this.is_book_check_off = is_book_check_off;
	}
	public boolean isIs_epaper_check_off() {
		return is_epaper_check_off;
	}
	public void setIs_epaper_check_off(boolean is_epaper_check_off) {
		this.is_epaper_check_off = is_epaper_check_off;
	}
	public String getBook_type_name() {
		return book_type_name;
	}
	public void setBook_type_name(String book_type_name) {
		this.book_type_name = book_type_name;
	}
	public List<LayerSequence> getSequenceStart() {
		return sequenceStart;
	}
	public void setSequenceStart(List<LayerSequence> sequenceStart) {
		this.sequenceStart = sequenceStart;
	}
	public List<LayerSequence> getSequenceMiddle() {
		return sequenceMiddle;
	}
	public void setSequenceMiddle(List<LayerSequence> sequenceMiddle) {
		this.sequenceMiddle = sequenceMiddle;
	}
	public List<LayerSequence> getSequenceEnd() {
		return sequenceEnd;
	}
	public void setSequenceEnd(List<LayerSequence> sequenceEnd) {
		this.sequenceEnd = sequenceEnd;
	}
	public boolean isUsesEndPaper() {
		return usesEndPaper;
	}
	public void setUsesEndPaper(boolean usesEndPaper) {
		this.usesEndPaper = usesEndPaper;
	}
	public List<String> getSynonyms() {
		return synonyms;
	}
	public void setSynonyms(List<String> synonyms) {
		this.synonyms = synonyms;
	}

}
