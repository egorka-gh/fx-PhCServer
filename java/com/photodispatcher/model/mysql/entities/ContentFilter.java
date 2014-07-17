package com.photodispatcher.model.mysql.entities;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "phcconfig.content_filter")
public class ContentFilter extends AbstractEntity {

	private static final long serialVersionUID = 1L;
	
	public ContentFilter() {
		setHasAutoId(true);
	}

	//database props
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private int id;
    @Column(name="name")
    private String name;
    @Column(name="is_photo_allow")
    private boolean is_photo_allow;
    @Column(name="is_retail_allow")
    private boolean is_retail_allow;
    @Column(name="is_pro_allow")
    private boolean is_pro_allow;
    @Column(name="is_alias_filter")
    private boolean is_alias_filter;

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public boolean isIs_photo_allow() {
		return is_photo_allow;
	}
	public void setIs_photo_allow(boolean is_photo_allow) {
		this.is_photo_allow = is_photo_allow;
	}
	public boolean isIs_retail_allow() {
		return is_retail_allow;
	}
	public void setIs_retail_allow(boolean is_retail_allow) {
		this.is_retail_allow = is_retail_allow;
	}
	public boolean isIs_pro_allow() {
		return is_pro_allow;
	}
	public void setIs_pro_allow(boolean is_pro_allow) {
		this.is_pro_allow = is_pro_allow;
	}
	public boolean isIs_alias_filter() {
		return is_alias_filter;
	}
	public void setIs_alias_filter(boolean is_alias_filter) {
		this.is_alias_filter = is_alias_filter;
	}
}
