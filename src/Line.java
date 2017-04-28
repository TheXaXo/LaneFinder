import org.opencv.core.Point;

public class Line {

    private Point start;
    private Point end;

    public Line(Point start, Point end) {
        this.start = start;
        this.end = end;
    }

    public double getStartX() {
        return this.start.x;
    }

    public double getStartY() {
        return this.start.y;
    }

    public double getEndX() {
        return this.end.x;
    }

    public double getEndY() {
        return this.end.y;
    }
}