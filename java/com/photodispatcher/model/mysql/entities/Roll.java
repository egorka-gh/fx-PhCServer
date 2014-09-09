package com.photodispatcher.model.mysql.entities;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "roll")
public class Roll extends AbstractEntity {
	private static final long serialVersionUID = 1L;
	
    @Id
    @Column(name="width")
    private int width;
    @Column(name="pixels")
    private int pixels;
	public int getWidth() {
		return width;
	}
	public void setWidth(int width) {
		this.width = width;
	}
	public int getPixels() {
		return pixels;
	}
	public void setPixels(int pixels) {
		this.pixels = pixels;
	}

    
}
