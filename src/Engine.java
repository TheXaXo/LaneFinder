import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.*;
import java.util.List;

public class Engine implements Runnable {

    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final double MAX_SLOPE = -0.4;
    private static final double MIN_SLOPE = 0.4;
    private static final int RHO = 1;
    private static final double THETA = Math.PI / 180;
    private static final int THRESHOLD = 50;
    private static final int MIN_LINE_LENGTH = 100;
    private static final int MAX_LINE_GAP = 10;
    private static final int CANNY_THRESHOLD_1 = 100;
    private static final int CANNY_THRESHOLD_2 = 200;
    private static final int CAPTURE_AREA_X = 20;
    private static final int CAPTURE_AREA_Y = 200;
    private static final int BLUR_SIZE = 5;
    private static final int LINE_THICKNESS = 5;

    private MatOfPoint points;

    public Engine() {
        //Make a triangle
        Point[] points = new Point[]{
                new Point(0, HEIGHT),
                new Point(WIDTH, HEIGHT),
                new Point(WIDTH / 2, HEIGHT / 3)
        };
        this.points = new MatOfPoint();
        this.points.fromArray(points);
    }

    @Override
    public void run() {
        Robot robot = null;

        try {
            robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }

        Rectangle captureArea = new Rectangle(CAPTURE_AREA_X, CAPTURE_AREA_Y, WIDTH, HEIGHT);
        CustomJFrame frame = new CustomJFrame(WIDTH, HEIGHT);

        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_3BYTE_BGR);
        BufferedImage processedImage = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_BYTE_GRAY);

        while (true) {
            image.setData(robot.createScreenCapture(captureArea).getData());
            this.processImage(image, processedImage);
            frame.addImage(processedImage);
        }
    }

    private void processImage(BufferedImage image, BufferedImage processedImage) {
        byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();

        Mat mat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC3);
        mat.put(0, 0, pixels);

        Mat gray = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC1);


        //Here we set the image to greyscale, convert it to binary representation and blur it.
        Imgproc.cvtColor(mat, gray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.GaussianBlur(gray, gray, new Size(BLUR_SIZE, BLUR_SIZE), 0);

        Imgproc.Canny(gray, gray, CANNY_THRESHOLD_1, CANNY_THRESHOLD_2);

        //We cut the part of the image that is not needed
        Mat mask = new Mat(gray.size(), gray.type());
        mask.setTo(new Scalar(0));
        Imgproc.fillPoly(mask, new ArrayList<>(Arrays.asList(this.points)), new Scalar(255));
        Core.bitwise_and(gray, mask, gray);

        //We extract the lines found by the edge detection
        Mat lines = new Mat();
        Imgproc.HoughLinesP(gray, lines, RHO, THETA, THRESHOLD, MIN_LINE_LENGTH, MAX_LINE_GAP);

        java.util.List<Line> leftLines = new ArrayList<>();
        java.util.List<Line> rightLines = new ArrayList<>();

        for (int i = 0; i < lines.rows(); i++) {
            double[] coordinates = lines.get(i, 0);
            double x1 = coordinates[0];
            double y1 = coordinates[1];
            double x2 = coordinates[2];
            double y2 = coordinates[3];

            Point start = new Point((int) x1, (int) y1);
            Point end = new Point((int) x2, (int) y2);

            double lineSlope = (y2 - y1) / (x2 - x1);

            if (lineSlope > MAX_SLOPE && lineSlope < MIN_SLOPE) {
                continue;
            }

            if (lineSlope >= 0) {
                rightLines.add(new Line(start, end));
            } else {
                leftLines.add(new Line(start, end));
            }
//            Imgproc.line(gray, start, end, new Scalar(255, 255, 255), LINE_THICKNESS);
        }

        double averageX1 = 0;
        double averageY1 = 0;
        double averageX2 = 0;
        double averageY2 = 0;

        for (Line line : leftLines) {
            averageX1 += line.getStartX();
            averageY1 += line.getStartY();
            averageX2 += line.getEndX();
            averageY2 += line.getEndY();
        }

        Point startLeft = new Point(averageX1 / leftLines.size(), averageY1 / leftLines.size());
        Point endLeft = new Point(averageX2 / leftLines.size(), averageY2 / leftLines.size());

        averageX1 = 0;
        averageY1 = 0;
        averageX2 = 0;
        averageY2 = 0;

        for (Line line : rightLines) {
            averageX1 += line.getStartX();
            averageY1 += line.getStartY();
            averageX2 += line.getEndX();
            averageY2 += line.getEndY();
        }

        Point startRight = new Point(averageX1 / rightLines.size(), averageY1 / rightLines.size());
        Point endRight = new Point(averageX2 / rightLines.size(), averageY2 / rightLines.size());

        Imgproc.line(gray, startLeft, endLeft, new Scalar(255, 255, 255), LINE_THICKNESS);
        Imgproc.line(gray, startRight, endRight, new Scalar(255, 255, 255), LINE_THICKNESS);

        gray.get(0, 0, pixels);

        processedImage.getRaster().setDataElements(0, 0, mat.cols(), mat.rows(), pixels);
    }
}