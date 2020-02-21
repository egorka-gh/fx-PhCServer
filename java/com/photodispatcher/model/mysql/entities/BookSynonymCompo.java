package com.photodispatcher.model.mysql.entities;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

@Table(name = "book_synonym_compo")
public class BookSynonymCompo extends AbstractEntity {
	private static final long serialVersionUID = 1L;
	
    @Id
    @Column(name="parent")
    private int parent;
    @Id
    @Column(name="child")
    private int child;

    //db drived
    @Column(name="child_alias", updatable=false, insertable=false)
    private String childAlias;

    @Transient
    private boolean deleted;

	public int getParent() {
		return parent;
	}

	public void setParent(int parent) {
		this.parent = parent;
	}

	public int getChild() {
		return child;
	}

	public void setChild(int child) {
		this.child = child;
	}

	public String getChildAlias() {
		return childAlias;
	}

	public void setChildAlias(String childAlias) {
		this.childAlias = childAlias;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}


}
