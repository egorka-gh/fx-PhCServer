package com.photodispatcher.model.mysql.entities;

import javax.persistence.Column;
import javax.persistence.Table;

@Table(name = "prn_queue_items")
public class PrnQueueItems extends AbstractEntity {
	private static final long serialVersionUID = 1L;
	
    @Column(name="prn_queue")
    private int prn_queue;

    @Column(name="sub_queue")
    private int sub_queue;

    @Column(name="print_group")
    private String print_group;

	public int getPrn_queue() {
		return prn_queue;
	}

	public void setPrn_queue(int prn_queue) {
		this.prn_queue = prn_queue;
	}

	public String getPrint_group() {
		return print_group;
	}

	public void setPrint_group(String print_group) {
		this.print_group = print_group;
	}

	public int getSub_queue() {
		return sub_queue;
	}

	public void setSub_queue(int sub_queue) {
		this.sub_queue = sub_queue;
	}

    
}
