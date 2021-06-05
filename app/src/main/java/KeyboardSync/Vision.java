package KeyboardSync;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.stream.IntStream;
import java.awt.Color;

public class Vision {
  private double[] doubleArange(int start, int end, int step) {
    return IntStream.rangeClosed(0, (int) ((end - start) / step)).mapToDouble(x -> x * step + start).toArray();
  }

  private double[] doubleDeg2Rad(double[] ll) {
    for (int i = 0; i < ll.length; i++) {
      ll[i] = ll[i] * Math.PI / 180.f;
    }
    return ll;
  }

  private double[] doubleCos(double[] ll) {
    for (int i = 0; i < ll.length; i++) {
      ll[i] = Math.cos(ll[i]);
    }
    return ll;
  }

  private double[] doubleSin(double[] ll) {
    for (int i = 0; i < ll.length; i++) {
      ll[i] = Math.sin(ll[i]);
    }
    return ll;
  }

  private int getGreyScale(int rgb) {
    int r = (rgb >> 16) & 0xff;
    int g = (rgb >> 8) & 0xff;
    int b = (rgb) & 0xff;

    // from https://en.wikipedia.org/wiki/Grayscale, calculating luminance
    int grey = (int) (0.2126 * r + 0.7152 * g + 0.0722 * b);

    return grey;
  }

  private double[][] getNormalisedHoughSpace(BufferedImage image, double angleResolution, int thresholdPixels) {
    // https://en.wikipedia.org/wiki/Hough_transform

    int imWidth = image.getWidth();
    int imHeight = image.getHeight();
    System.out.println(imWidth + " " + imHeight);

    double[] thetas = doubleDeg2Rad(doubleArange(-90, 90, (int) angleResolution));
    System.out.println(thetas.length + ":: " + thetas[0]);

    int maxDist = (int) Math.ceil(Math.sqrt(imWidth * imWidth + imHeight * imHeight));
    int nTheta = thetas.length;

    double houghSpace[][] = new double[2 * maxDist][nTheta];

    int nMaxBin = 0;

    double[] cosThetas = doubleCos(thetas);
    double[] sinThetas = doubleSin(thetas);

    for (int x = 0; x < imWidth; x++) {
      for (int y = 0; y < imHeight; y++) {
        if (getGreyScale(image.getRGB(x, y)) > 240) {
          for (int iTheta = 0; iTheta < nTheta; iTheta++) {
            int pho = (int) Math.ceil(x * cosThetas[iTheta] + y * sinThetas[iTheta]);
            houghSpace[pho][iTheta] += 1;

            if (houghSpace[pho][iTheta] > nMaxBin) {
              nMaxBin = (int) houghSpace[pho][iTheta];
            }

          }
        }
      }
    }
    System.out.println(nMaxBin);

    // normalise output
    for (int pho = 0; pho < houghSpace.length; pho++) {
      for (int theta = 0; theta < houghSpace[0].length; theta++) {
        houghSpace[pho][theta] *= 255.0;
        houghSpace[pho][theta] /= nMaxBin;
      }
    }
    return houghSpace;
  }

  private BufferedImage drawLines(BufferedImage image, double[][] houghSpace, double angleResolution) {
    Graphics2D canvas = image.createGraphics();

    Color myColour = new Color(240, 248, 255); // Color white

    canvas.setColor(myColour);
    angleResolution = 1;

    // a magic number between 0 and 255
    int threshold = 179;

    int imWidth = image.getWidth();
    int imHeight = image.getHeight();

    double[] thetas = doubleDeg2Rad(doubleArange(-90, 90, (int) angleResolution));
    int maxDist = (int) Math.ceil(Math.sqrt(imWidth * imWidth + imHeight * imHeight));

    for (int iPho = 0; iPho < houghSpace.length; iPho++) {
      for (int iTheta = 0; iTheta < houghSpace[0].length; iTheta++) {
        if (houghSpace[iPho][iTheta] >= threshold) {
          double theta = thetas[iTheta];
          double pho = iPho - maxDist;

          double a = Math.cos(theta);
          double b = Math.sin(theta);

          double x0 = a * pho;
          double y0 = b * pho;

          int x1 = (int) (x0 + 100 * (-b));
          int y1 = (int) (y0 + 100 * (a));

          int x2 = (int) (x0 - 100 * (-b));
          int y2 = (int) (y0 - 100 * (a));

          canvas.drawLine(x1, y1, x2, y2);
          System.out.println("(" + x1 + "," + y1 + ")");
        }
      }
    }

    return image;
  }

  public BufferedImage houghLines(BufferedImage image, double angleResolution, boolean writeHough) {
    double[][] houghSpace = getNormalisedHoughSpace(image, angleResolution, 0);
    BufferedImage im = null;
    if (writeHough) {
      im = new BufferedImage(houghSpace.length, houghSpace[0].length, BufferedImage.TYPE_INT_RGB);
    } else {
      im = drawLines(image, houghSpace, angleResolution);
    }

    return im;
  }
}
