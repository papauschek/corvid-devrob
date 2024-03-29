/*
 * Encog(tm) Core v3.0 - Java Version
 * http://www.heatonresearch.com/encog/
 * http://code.google.com/p/encog-java/
 
 * Copyright 2008-2011 Heaton Research, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *   
 * For more information on Heaton Research copyrights, licenses 
 * and trademarks visit:
 * http://www.heatonresearch.com/copyright
 */
package org.encog.util;

public class HTMLReport {
	private StringBuilder text = new StringBuilder();

	public void beginHTML() {
		text.append("<html>");
	}

	public void endHTML() {
		text.append("</html>");
	}

	public void title(String str) {
		text.append("<head><title>");
		text.append(str);
		text.append("</title></head>");
	}

	public void beginPara() {
		text.append("<p>");
	}

	public void endPara() {
		text.append("</p>");
	}

	public void bold(String str) {
		text.append("<b>");
		text.append(str);
		text.append("</b>");
	}

	public void para(String str) {
		text.append("<p>");
		text.append(str);
		text.append("</p>");
	}

	public void clear() {
		this.text.setLength(0);
	}

	public String toString() {
		return this.text.toString();
	}

	public void beginBody() {
		text.append("<body>");
	}

	public void endBody() {
		text.append("</body>");
	}

	public void h1(String title) {
		text.append("<h1>");
		text.append(title);
		text.append("</h1>");
	}

	public void beginTable() {
		text.append("<table border=\"1\">");
	}

	public void endTable() {
		text.append("</table>");
	}

	public void beginRow() {
		text.append("<tr>");
	}

	public void endRow() {
		text.append("</tr>");
	}

	public void header(String head) {
		text.append("<th>");
		text.append(head);
		text.append("</th>");
	}
	
	public void cell(String head) {
		cell(head,0);
	}

	public void cell(String head, int colSpan) {
		text.append("<td");
		if (colSpan > 0) {
			text.append(" colspan=\"");
			text.append(colSpan);
			text.append("\"");
		}
		text.append(">");
		text.append(head);
		text.append("</td>");
	}

	public void tablePair(String name, String value) {
		beginRow();
		cell("<b>" + name + "</b>");
		cell(value);
		endRow();
		
		
	}
	
	public void h2(String title) {
		text.append("<h2>");
		text.append(title);
		text.append("</h2>");
	}

	public void h3(String title) {
		text.append("<h3>");
		text.append(title);
		text.append("</h3>");
	}

	public void beginList() {
		text.append("<ul>");		
	}

	public void listItem(String str) {
		text.append("<li>");
		text.append(str);
		
	}

	public void endList() {
		text.append("</ul>");		
	}

	public void beginTableInCell(int colSpan) {
		text.append("<td");
		if (colSpan > 0) {
			text.append(" colspan=\"");
			text.append(colSpan);
			text.append("\"");
		}
		text.append(">");
		text.append("<table border=\"1\" width=\"100%\">");
	}

	public void endTableInCell() {
		text.append("</table></td>");
		
	}
}
