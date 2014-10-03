package com.reporter.document.output;

import com.reporter.document.layout.XlsLayoutDirection;

public class OutputOutline {
    /**
     * Outline direction
     */
    private XlsLayoutDirection direction;
    
    /**
     * Outline from row
     */
    private int fromX;

    /**
     * Outline from column
     */
    private int fromY;

    /**
     * Outline to row
     */
    private int toX;

    /**
     * Outline to column
     */
    private int toY;

    /**
     * @param direction
     */
    public OutputOutline(XlsLayoutDirection direction) {
        super();
        this.direction = direction;
    }

    /**
     * @return the direction
     */
    public XlsLayoutDirection getDirection() {
        return direction;
    }

	public int getFromX() {
		return fromX;
	}

	public void setFromX(int fromX) {
		this.fromX = fromX;
	}

	public int getFromY() {
		return fromY;
	}

	public void setFromY(int fromY) {
		this.fromY = fromY;
	}

	public int getToX() {
		return toX;
	}

	public void setToX(int toX) {
		this.toX = toX;
	}

	public int getToY() {
		return toY;
	}

	public void setToY(int toY) {
		this.toY = toY;
	}
    

}
