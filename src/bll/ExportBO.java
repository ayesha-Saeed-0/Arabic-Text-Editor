package bll;

public class ExportBO implements IExport {
	public String convertToMarkdown(String content) {
	    StringBuilder markdownBuilder = new StringBuilder();
	    String normalizedContent = content.replace("\r\n", "\n").replace("\r", "\n");
	    String[] lines = normalizedContent.split("\n");

	    boolean inOrderedList = false;
	    boolean inUnorderedList = false;

	    for (String line : lines) {
	        line = line.trim();
	        if (line.matches("\\d+\\. .*")) {
	            // Handle ordered list
	            if (!inOrderedList) {
	                if (inUnorderedList) {
	                    markdownBuilder.append("</ul>\n");
	                    inUnorderedList = false;
	                }
	                markdownBuilder.append("<ol dir=\"rtl\">\n");
	                inOrderedList = true;
	            }
	            markdownBuilder.append("<li>").append(line.substring(line.indexOf('.') + 2)).append("</li>\n");
	        } else if (line.startsWith("- ")) {
	            // Handle unordered list
	            if (!inUnorderedList) {
	                if (inOrderedList) {
	                    markdownBuilder.append("</ol>\n");
	                    inOrderedList = false;
	                }
	                markdownBuilder.append("<ul dir=\"rtl\">\n");
	                inUnorderedList = true;
	            }
	            markdownBuilder.append("<li>").append(line.substring(2)).append("</li>\n");
	        } else if (line.contains("https://")) {
	            // Handle URL
	            markdownBuilder.append("<p dir=\"rtl\"><a href=\"").append(line).append("\">").append(line).append("</a></p>\n");
	        } else if (line.isEmpty()) {
	            // Handle empty lines
	            if (inOrderedList) {
	                markdownBuilder.append("</ol>\n");
	                inOrderedList = false;
	            }
	            if (inUnorderedList) {
	                markdownBuilder.append("</ul>\n");
	                inUnorderedList = false;
	            }
	        } else {
	            // Handle normal paragraphs
	            if (inOrderedList) {
	                markdownBuilder.append("</ol>\n");
	                inOrderedList = false;
	            }
	            if (inUnorderedList) {
	                markdownBuilder.append("</ul>\n");
	                inUnorderedList = false;
	            }
	            markdownBuilder.append("<p dir=\"rtl\">").append(line).append("</p>\n");
	        }
	    }

	    // Close any open list tags
	    if (inOrderedList) {
	        markdownBuilder.append("</ol>\n");
	    }
	    if (inUnorderedList) {
	        markdownBuilder.append("</ul>\n");
	    }

	    return markdownBuilder.toString();
	}


}
