package com.photodispatcher.model.mysql.entities;

import javax.persistence.Column;
import javax.persistence.Table;

@Table(name = "phcconfig.content_filter_alias")
public class ContentFilterAlias extends AbstractEntity {
	private static final long serialVersionUID = 1L;

    @Column(name="filter")
    private int filter;
    @Column(name="filter")
    private int alias;

    //ref column
    //@Transient
    @Column(name="alias_name", updatable=false, insertable=false)
    private String alias_name;
    @Column(name="allow", updatable=false, insertable=false)
    private boolean allow;

    
	public int getFilter() {
		return filter;
	}
	public void setFilter(int filter) {
		this.filter = filter;
	}

	public int getAlias() {
		return alias;
	}
	public void setAlias(int alias) {
		this.alias = alias;
	}

	public String getAlias_name() {
		return alias_name;
	}
	public void setAlias_name(String alias_name) {
		this.alias_name = alias_name;
	}
	
	public boolean isAllow() {
		return allow;
	}
	public void setAllow(boolean allow) {
		this.allow = allow;
	}
	
	
}
