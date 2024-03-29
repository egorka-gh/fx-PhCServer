package com.photodispatcher.model.mysql.entities;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

@Table(name = "book_synonym")
public class BookSynonym extends AbstractEntity {
	private static final long serialVersionUID = 1L;

	//database props
    @Id
    @GeneratedValue
    @Column(name="id")
    private int id;
    @Column(name="src_type")
    private int src_type;
    @Column(name="synonym")
    private String synonym;
    @Column(name="book_type")
    private int book_type;
    @Column(name="is_horizontal")
    private boolean is_horizontal;
    @Column(name="synonym_type")
    private int synonym_type;
    @Column(name="has_backprint")
    private boolean has_backprint;
    @Column(name="order_program")
    private int order_program;
    @Column(name="compo_type")
    private int compo_type;
    

    //ref name
    @Column(name="src_type_name", updatable=false, insertable=false)
    private String src_type_name;
    @Column(name="book_type_name", updatable=false, insertable=false)
    private String book_type_name;
    @Column(name="synonym_type_name", updatable=false, insertable=false)
    private String synonym_type_name;
    @Column(name="order_program_name", updatable=false, insertable=false)
    private String order_program_name;
    @Column(name="compo_type_name", updatable=false, insertable=false)
    private String compo_type_name;
	//content filter mark
    @Column(name="is_allow", updatable=false, insertable=false)
    private boolean is_allow;
    
    @Transient
    private List<BookPgTemplate> templates;

    @Transient
    private List<BookSynonymCompo> compos;

    @Transient
    private List<BookSynonymGlue> glueCommands;

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

	public String getSynonym() {
		return synonym;
	}

	public void setSynonym(String synonym) {
		this.synonym = synonym;
	}

	public int getBook_type() {
		return book_type;
	}

	public void setBook_type(int book_type) {
		this.book_type = book_type;
	}

	public boolean isIs_horizontal() {
		return is_horizontal;
	}

	public void setIs_horizontal(boolean is_horizontal) {
		this.is_horizontal = is_horizontal;
	}

	public int getSynonym_type() {
		return synonym_type;
	}

	public void setSynonym_type(int synonym_type) {
		this.synonym_type = synonym_type;
	}

	public String getSrc_type_name() {
		return src_type_name;
	}

	public void setSrc_type_name(String src_type_name) {
		this.src_type_name = src_type_name;
	}

	public String getBook_type_name() {
		return book_type_name;
	}

	public void setBook_type_name(String book_type_name) {
		this.book_type_name = book_type_name;
	}

	public List<BookPgTemplate> getTemplates() {
		return templates;
	}

	public void setTemplates(List<BookPgTemplate> templates) {
		this.templates = templates;
	}

	public boolean isIs_allow() {
		return is_allow;
	}

	public void setIs_allow(boolean is_allow) {
		this.is_allow = is_allow;
	}

	public String getSynonym_type_name() {
		return synonym_type_name;
	}

	public void setSynonym_type_name(String synonym_type_name) {
		this.synonym_type_name = synonym_type_name;
	}

	public boolean isHas_backprint() {
		return has_backprint;
	}

	public void setHas_backprint(boolean has_backprint) {
		this.has_backprint = has_backprint;
	}

	public List<BookSynonymGlue> getGlueCommands() {
		return glueCommands;
	}

	public void setGlueCommands(List<BookSynonymGlue> glueCommands) {
		this.glueCommands = glueCommands;
	}

	public int getOrder_program() {
		return order_program;
	}

	public void setOrder_program(int order_program) {
		this.order_program = order_program;
	}

	public String getOrder_program_name() {
		return order_program_name;
	}

	public void setOrder_program_name(String order_program_name) {
		this.order_program_name = order_program_name;
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

	public List<BookSynonymCompo> getCompos() {
		return compos;
	}

	public void setCompos(List<BookSynonymCompo> compos) {
		this.compos = compos;
	}

	
}
