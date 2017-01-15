package com.web4enterprise.pdf.layout.paragraph.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.web4enterprise.pdf.layout.document.impl.PdfPager;
import com.web4enterprise.pdf.layout.paragraph.ParagraphStyle;
import com.web4enterprise.pdf.layout.paragraph.impl.PdfParagraphEmbeddable.SplitInformation;
import com.web4enterprise.pdf.layout.placement.Stop;
import com.web4enterprise.pdf.layout.utils.CompositeList;

/*
 * An ElementLine is a line of different Elements without any line return.
 * A line of element can be composed of one or more Elements. Elements can have different styles and associated to different stops.
 * All elements for a given stop are gathered into a same list, itself gathered into a "meta" {@see CompositeList}.
 */
public class ParagraphEmbeddableLine extends CompositeList<PdfParagraphEmbeddable> {	
	/**
	 * Convert a list of elements to a list of ElementLine.
	 * List of elements is parsed to search for new line in each Element. 
	 * Elements are added to ElementLine elements until a new line is found.
	 * If a new line is found in an element, a new ElementLine is created and following elements are added until new line.
	 * @param elements The elements to parse.
	 * @return The list of ElementLine.
	 */
	public static List<ParagraphEmbeddableLine> getEmbeddableLines(CompositeList<PdfParagraphEmbeddable> elements) {
		//Initialize the array of lines of elements.
		List<ParagraphEmbeddableLine> paragraphEmbeddableLines = new ArrayList<>();
		//Create a new element line and add it to array. This will contain the elements of the first line, within a different list for each stop.
		ParagraphEmbeddableLine paragraphEmbeddableLine = new ParagraphEmbeddableLine();
		ArrayList<PdfParagraphEmbeddable> stopElementLine = new ArrayList<PdfParagraphEmbeddable>();
		paragraphEmbeddableLine.addList(stopElementLine);
		paragraphEmbeddableLines.add(paragraphEmbeddableLine);
		
		//Iterate over each paragraphEmbeddable to search for new lines and create a new List on a new Stop.
		CompositeListIterator paragraphIterator = elements.iterator();
		while(paragraphIterator.hasNext()) {
			PdfParagraphEmbeddable element = paragraphIterator.next();
			//If list has changed, it means that a stop has been inserted. So, do the same for line, insert a new List to simulate Stop.
			if(paragraphIterator.hasListChanged()) {
				stopElementLine = new ArrayList<PdfParagraphEmbeddable>();
				paragraphEmbeddableLine.addList(stopElementLine);
			}
			List<PdfParagraphEmbeddable> currentElementLines = element.getLines();
			Iterator<PdfParagraphEmbeddable> currentElementLinesIterator = currentElementLines.iterator();
			while(currentElementLinesIterator.hasNext()) {
				PdfParagraphEmbeddable currentElement = currentElementLinesIterator.next();
				stopElementLine.add(currentElement);
				if(currentElementLinesIterator.hasNext()) {
					paragraphEmbeddableLine = new ParagraphEmbeddableLine();
					stopElementLine = new ArrayList<PdfParagraphEmbeddable>();
					paragraphEmbeddableLine.addList(stopElementLine);
					paragraphEmbeddableLines.add(paragraphEmbeddableLine);
				}				
			}
		}
		
		return paragraphEmbeddableLines;
	}
	
	/***
	 * Do not permit external instantiation.
	 */
	private ParagraphEmbeddableLine() {
		//Empty constructor.
	}
	
	public List<ParagraphEmbeddableLine> splitToMaxWidth(PdfPager pdfPager, ParagraphStyle defaultStyle, float defaultFontSize, float firstLineMaxWidth, Float maxWidth, List<Stop> stops) {
		List<ParagraphEmbeddableLine> paragraphEmbeddableLines = new ArrayList<>();
		
		float currentX = 0.0f;
		ParagraphEmbeddableLine currentElementLine = new ParagraphEmbeddableLine();
		ArrayList<PdfParagraphEmbeddable> stopElementLine = new ArrayList<PdfParagraphEmbeddable>();
		currentElementLine.addList(stopElementLine);
		paragraphEmbeddableLines.add(currentElementLine);
		
		//Defines if the line being processed is the first or not.
		boolean isFirstLine = true;

		//The index of current stop.
		int currentStopIndex = 0;
		//Iterate over each paragraphEmbeddable to search for new lines and create a new List on a new Stop.
		CompositeListIterator paragraphIterator = iterator();
		while(paragraphIterator.hasNext()) {
			PdfParagraphEmbeddable element = paragraphIterator.next();

			//If list has changed, it means that a stop has been inserted. So, do the same for line, insert a new List to simulate Stop.
			if(paragraphIterator.hasListChanged()) {
				//Get next stop position and increment its index accordingly.
				Stop currentStop = stops.get(currentStopIndex);
				currentStopIndex++;

				//If we already passed stop, do not increment position. Continue to write where we are.
				float stopX = currentStop.getPosition();
				if(stopX > currentX) {
					currentX = stopX;
				}
				
				//Add a list to element line to simulate this new stop.
				stopElementLine = new ArrayList<PdfParagraphEmbeddable>();
				currentElementLine.addList(stopElementLine);
			}
			
			SplitInformation splitInformation = element.split(pdfPager, defaultStyle, defaultFontSize, currentX, firstLineMaxWidth, maxWidth);
			
			//Insert end of line on current stop line.
			Iterator<PdfParagraphEmbeddable> iterator = splitInformation.splitEmbeddables.iterator();
			stopElementLine.add(iterator.next());
			
			//Create new line for each new line.
			while(iterator.hasNext()) {
				if(isFirstLine) {
					isFirstLine = false;
				}
				//Create a new Element line with a new list inside for first stop.
				currentElementLine = new ParagraphEmbeddableLine();
				stopElementLine = new ArrayList<PdfParagraphEmbeddable>();
				currentElementLine.addList(stopElementLine);
				
				//Add current Element line to resulting lines.
				paragraphEmbeddableLines.add(currentElementLine);
				
				//Reinitialize current position and stop index.
				currentX = 0.0f;
				currentStopIndex = 0;
				
				//Add PdfParagraphEmbeddable to current stop Element line.
				PdfParagraphEmbeddable splitElement = iterator.next();
				stopElementLine.add(splitElement);
			}
			
			currentX += splitInformation.positionX;
		}
		
		return paragraphEmbeddableLines;
	}

	public int getWidth(ParagraphStyle defaultStyle, float defaultTextSize) {
		int width = 0;
		
		for(PdfParagraphEmbeddable element : this) {
			width += element.getWidth(defaultStyle, defaultTextSize);
		}
		
		return width;
	}
	
	public float getHeight(ParagraphStyle paragraphStyle) {
		float lineSpacing = 0;
		for(PdfParagraphEmbeddable element : this) {
			//Keep greatest line spacing to not overlap elements of other lines.
			float currentLineSpacing = element.getLineSpacing(paragraphStyle);
			if(currentLineSpacing > lineSpacing) {
				lineSpacing = currentLineSpacing;
			}
		}
		return lineSpacing;
	}
}
