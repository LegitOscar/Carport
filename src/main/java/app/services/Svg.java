package app.services;

public class Svg {
    private static final String SVG_TEMPLATE = "<svg version=\"1.1\" \n" +
            "     xmlns=\"http://www.w3.org/2000/svg\" \n" +
            "     viewBox=\"%s\" width=\"%s\" height=\"%s\" \n" +
            "     preserveAspectRatio=\"xMinYMin\">";

    private static final String SVG_ARROW_DEFS = "<defs>\n" +
            "        <marker id=\"beginArrow\" markerWidth=\"12\" markerHeight=\"12\" refX=\"0\" refY=\"6\" orient=\"auto\">\n" +
            "            <path d=\"M0,6 L12,0 L12,12 L0,6\" style=\"fill: #000000;\" />\n" +
            "        </marker>\n" +
            "        <marker id=\"endArrow\" markerWidth=\"12\" markerHeight=\"12\" refX=\"12\" refY=\"6\" orient=\"auto\">\n" +
            "            <path d=\"M0,0 L12,6 L0,12 L0,0 \" style=\"fill: #000000;\" />\n" +
            "        </marker>\n" +
            "    </defs>";

    private static final String SVG_RECT_TEMPLATE = "<rect x=\"%.2f\" y=\"%.2f\" height=\"%f\" width=\"%f\" style=\"%s\" />";

    private StringBuilder svg = new StringBuilder();

    /**
     * Constructor for Svg.
     *
     * @param viewBox The SVG viewBox attribute (e.g., "0 0 600 400")
     * @param width The width of the SVG (e.g., "100%" or "600px")
     * @param height The height of the SVG (e.g., "400px")
     */
    public Svg(String viewBox, String width, String height) {
        svg.append(String.format(SVG_TEMPLATE, viewBox, width, height));
        svg.append(SVG_ARROW_DEFS);
    }

    /**
     * Adds a rectangle to the SVG.
     *
     * @param x X position of the rectangle
     * @param y Y position of the rectangle
     * @param height Height of the rectangle
     * @param width Width of the rectangle
     * @param style CSS style string (e.g., "stroke:#000; fill:#fff;")
     */
    public void addRectangle(double x, double y, double height, double width, String style) {
        svg.append(String.format(SVG_RECT_TEMPLATE, x, y, height, width, style));
    }

    public void addLine(int x1, int y1, int x2, int y2, String style) {
        // Implement if needed
    }

    public void addArrow(int x1, int y1, int x2, int y2, String style) {
        // Implement if needed
    }

    public void addText(int x, int y, int rotation, String text) {
        // Implement if needed
    }

    public void addSvg(Svg innerSvg) {
        svg.append(innerSvg.toString());
    }

    @Override
    public String toString() {
        return svg.append("</svg>").toString();
    }
}
