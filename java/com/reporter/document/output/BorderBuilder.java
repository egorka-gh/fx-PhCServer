package com.reporter.document.output;

import java.util.List;

import com.reporter.document.layout.Layout;
import com.reporter.document.layout.XLSCommands;
import com.reporter.document.layout.XlsLayoutDirection;
import com.reporter.document.layout.XlsReference;

public class BorderBuilder {
    private OutputElement arrChilds[][];
    private SectionElementOffsets elementOffsets;
    private SectionOffset sectionOffset;
    private int fromRow;
    private int fromCol;
    private List<OutputElement> elementChilds;
    private OutputWorkbook outputWorkbook; 
    
    /**
     * constructor
     */
    public BorderBuilder(List<OutputElement> elementChilds, SectionElementOffsets elementOffsets, SectionOffset sectionOffset, OutputWorkbook outputWorkbook){
        this.elementOffsets = elementOffsets;
        this.sectionOffset = sectionOffset;
        this.elementChilds = elementChilds;
        this.outputWorkbook = outputWorkbook;
        Layout layout= sectionOffset.getLayuot();
        if (layout.getCommand() != XLSCommands.XLSCommandSheet){
            fromRow =  layout.getReferences().getRowIndex();
            fromCol = layout.getReferences().getColIndex();
        }
        fromRow = fromRow + sectionOffset.getStartingYOffset() + sectionOffset.getYOffset();
        fromCol = fromCol + sectionOffset.getStartingXOffset() + sectionOffset.getXOffset();
        arrChilds = new OutputElement[elementOffsets.getHeight()][elementOffsets.getWidth()];
        for (OutputElement oe : elementChilds){
            if (oe.getOutputReferences().getColIndex() < fromCol ){
                int r =0;
                r++;
            }
            if ((oe.getOutputReferences().getRowIndex()-fromRow) < elementOffsets.getHeight()){
                if ((oe.getOutputReferences().getColIndex()-fromCol) < elementOffsets.getWidth()){
                	arrChilds[oe.getOutputReferences().getRowIndex()-fromRow][oe.getOutputReferences().getColIndex()-fromCol]= oe;
                } else{
                    //may be cross column out of array
                    int t=0;
                    t++;
                }
            } else {
                //may be cross row out of array
                int t=0;
                t++;
            }
            
        }
    }
    
    /**
     * build borders
     * 
     */
    public void build(){
        //TODO frame command can break algorithm  
        //for each not inserted row 
        //shift columns borders to element width (max column shift)
        int elemWidth = elementOffsets.getWidth();
        int tmplWidth = sectionOffset.getLayuot().getReferences().getColNumFromRegionReference();
        for (int i = 0; i < elementOffsets.getHeight(); i++){
            //get x offset at row
        	int i2tmpl = elementOffsets.resultToTemplateY(innerToTepmlateY(i));
            int rwOffs = elementOffsets.getXOffsetAtRow(i2tmpl);
            int startCol = tmplWidth -1 + rwOffs;
            if (!elementOffsets.isRowInserted(innerToTepmlateY(i))){
                //process column borders
                //used last cell to clone style & build offset 
                OutputElement oe = arrChilds[i][startCol];
                if (oe != null){
                    short style = oe.getStyleIndex();
                	for (int cl = elemWidth-1; cl > startCol; cl--){
                		short newSt =-1;
                        if (!(cl == elemWidth-1)){
                            //clear left & right border
                        	newSt = outputWorkbook.getNewStyle(style, false, false, true, true);
                        } else {
                            //last col clear left border
                        	newSt = outputWorkbook.getNewStyle(style, false, true, true, true);
                        }
                        if (newSt !=-1){
                        	XlsReference ref = oe.getOutputReferences();
                        	OutputElement el = new OutputElement();
                        	el.setOutputReferences(ref.getCloneWithOffset(cl-startCol, 0));
                        	el.setStyleIndex(newSt);
                        	elementChilds.add(el);
                        	if (arrChilds[i][cl] != null){
                        		//TODO wrong column offset
                        		int r=0;
                        		r++;
                        	}
                        	arrChilds[i][cl] = el;
                        }
                    }
                	if (elemWidth-1 > startCol){
                		outputWorkbook.checkTemplateStyle(oe, XlsLayoutDirection.ColumnDirection);
                	}
                }
            } else {
            	//inserted row clear right border in last cell
                OutputElement oe = arrChilds[i][startCol];
                if ((oe != null) && (elemWidth-1 > startCol)){
            		outputWorkbook.checkTemplateStyle(oe, XlsLayoutDirection.ColumnDirection);
                }
            }
        }
        //for each inserted row build row borders
        for (int i = 0; i < elementOffsets.getHeight(); i++){
            if (elementOffsets.isRowInserted(innerToTepmlateY(i))){
                //finde next not inserted row
                int nextRw = elementOffsets.getNextNotInsertedRow(innerToTepmlateY(i));
                boolean hasNext = true;
                if (nextRw == -1){
                    hasNext = false;
                } else {
                    nextRw = tepmlateToInnerY(nextRw);
                }
            	boolean lastRow = (i == elementOffsets.getHeight()-1);
                for (int cl = 0; cl < elementOffsets.getWidth(); cl++){
                    //for each column if there is no element
                    if (arrChilds[i][cl] == null){
                    	OutputElement toe = null;
                    	int referenceRowOffset = 1;//used previous row
                    	if (!hasNext){
                            //last subsection in layout & no row after it
                    		//make only left & right borders around section element and fully process last row to  make bottom border
                    		if (lastRow){
                                //last row - clone all styles from previous not inserted row 
                                int prvRw = elementOffsets.getPreviousNotInsertedRow(innerToTepmlateY(i));
                                prvRw = tepmlateToInnerY(prvRw);
                                prvRw = (prvRw == i) ? i-1 : prvRw; 
                                toe = arrChilds[prvRw][cl];
                                referenceRowOffset = i - prvRw;//used previous not inserted row
                    		} else {
                                //clone only first (left border)and last (right border) from previous row 
                    			if ((cl == 0) || (cl == elementOffsets.getWidth()-1)){
                    				//first or last column
                    				toe = arrChilds[i-1][cl];
                    			}
                    		}
                    	} else {
                            //simple uses previous row as template and connect vertical borders ws next not inserted row
                            toe = arrChilds[i-1][cl];
                    	}
                    	
                        if (toe != null){
                        	//template output elemend found
                            short styleTmpl = toe.getStyleIndex();
                        	//try to make new style
                            short style = -1;
                        	if (hasNext){
                        		//get style form next not inserted row
                                OutputElement noe = arrChilds[nextRw][cl];
                                if (noe != null){
                                    short styleNext = noe.getStyleIndex();
                                    //clear top & bottom border
                                    style = outputWorkbook.getNewStyleVCombine(styleTmpl, styleNext);
                                }
                        	} else {
                        		if (lastRow){
                        			if (cl==0){
                        				//get left & bottom borders
                        				style = outputWorkbook.getNewStyle(styleTmpl, true, false, false, true);
                        			} else if ((cl == elementOffsets.getWidth()-1)){
                        				//get right & bottom border
                        				style = outputWorkbook.getNewStyle(styleTmpl, false, true, false, true);
                        			} else {
                        				//get bottom border
                        				style = outputWorkbook.getNewStyle(styleTmpl, false, false, false, true);
                        			}
                            		outputWorkbook.checkTemplateStyle(toe, XlsLayoutDirection.RowDirection);
                        		} else{
                        			if (cl==0){
                        				//get left border
                        				style = outputWorkbook.getNewStyle(styleTmpl, true, false, false, false);
                        			} else if ((cl == elementOffsets.getWidth()-1)){
                        				//get right border
                        				style = outputWorkbook.getNewStyle(styleTmpl, false, true, false, false);
                        			}
                        		}
                        	}
                            if (style != -1){
                            	//create output element
                                XlsReference ref = toe.getOutputReferences();
                                OutputElement el = new OutputElement();
                                el.setOutputReferences(ref.getCloneWithOffset(0, referenceRowOffset));
                                el.setStyleIndex(style);
                                el.setAfterCrosstab(toe.isAfterCrosstab());
                                el.setUderCrosstab(toe.isUderCrosstab());
                                elementChilds.add(el);
                                arrChilds[i][cl] = el;
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * converts x coord form relative (zerro based) to absolute
     * 
     * @param column
     * @return
     */
    private int innerToTepmlateX(int column){
        return (column + sectionOffset.getLayuot().getReferences().getColIndex());
    }
    /**
     * converts Y coord form relative (zerro based) to absolute
     * @param row
     * @return
     */
    private int innerToTepmlateY(int row){
        return (row + sectionOffset.getLayuot().getReferences().getRowIndex());
    }
    /**
     * converts x coord form absolute to relative  
     * 
     * @param column
     * @return
     */
    private int tepmlateToInnerX(int column){
        return (column - sectionOffset.getLayuot().getReferences().getColIndex());
    }
    /**
     * converts y coord form absolute to relative
     *   
     * @param row
     * @return
     */
    private int tepmlateToInnerY(int row){
        return (row - sectionOffset.getLayuot().getReferences().getRowIndex());
    }
}
