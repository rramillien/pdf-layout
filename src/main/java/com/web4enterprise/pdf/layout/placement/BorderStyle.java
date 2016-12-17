package com.web4enterprise.pdf.layout.placement;

public class BorderStyle {
	public static final BorderStyle NONE = new BorderStyle();
	static {
		NONE.width = 0;
		NONE.lineStyle = LineStyle.NONE;
	}
	public static final BorderStyle THIN_SOLID = new BorderStyle();
	
	public float width = 1.0f;
	public LineStyle lineStyle = LineStyle.SOLID;
	
	public BorderStyle() {
	}
	
	public BorderStyle(float width) {
		this.width = width;
	}

	public BorderStyle(float width, LineStyle lineStyle) {
		this.width = width;
		this.lineStyle = lineStyle;
	}
}
