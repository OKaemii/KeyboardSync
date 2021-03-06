package javision;

import java.awt.image.BufferedImage;

public class EdgeDetection {
  private int getGreyScale(int rgb) {
    int r = (rgb >> 16) & 0xff;
    int g = (rgb >> 8) & 0xff;
    int b = (rgb) & 0xff;

    // from https://en.wikipedia.org/wiki/Grayscale, calculating luminance
    int grey = (int) (0.2126 * r + 0.7152 * g + 0.0722 * b);

    return grey;
  }

  public BufferedImage sobel(BufferedImage base) {
    int imWidth = base.getWidth();
    int imHeight = base.getHeight();

    int maxGrad = -1;
    int[][] edgeColors = new int[imWidth][imHeight];

    for (int x = 1; x < imWidth - 1; x++) {
      for (int y = 1; y < imHeight - 1; y++) {
        int m00 = getGreyScale(base.getRGB(x - 1, y - 1));
        int m01 = getGreyScale(base.getRGB(x - 1, y));
        int m02 = getGreyScale(base.getRGB(x - 1, y + 1));

        int m10 = getGreyScale(base.getRGB(x, y - 1));
        int m11 = getGreyScale(base.getRGB(x, y));
        int m12 = getGreyScale(base.getRGB(x, y + 1));

        int m20 = getGreyScale(base.getRGB(x + 1, y - 1));
        int m21 = getGreyScale(base.getRGB(x + 1, y));
        int m22 = getGreyScale(base.getRGB(x + 1, y + 1));

        // sobel's kernel
        int s1 = ((-1 * m00) + (0 * m01)  + (1 * m02))  + ((-2 * m10) + (0 * m11) + (2 * m12)) + ((-1 * m20) + (0 * m21) + (1 * m22));
        int s2 = ((-1 * m00) + (-2 * m01) + (-1 * m02)) + ((0 * m10)  + (0 * m11) + (0 * m12)) + ((1 * m20)  + (2 * m21) + (1 * m22));

        double mag = Math.sqrt((s1 * s1) + (s2 * s2));

        if (maxGrad < (int) mag) {
          maxGrad = (int) mag;
        }

        edgeColors[x][y] = (int) mag;
      }
    }

    double scale = 255.0 / maxGrad;

    BufferedImage output = base;

    for (int x = 1; x < imWidth - 1; x++) {
      for (int y = 1; y < imHeight - 1; y++) {
        int edgeColor = edgeColors[x][y];
        edgeColor = (int) (edgeColor * scale);
        edgeColor = 0xff000000 | (edgeColor << 16) | (edgeColor << 8) | edgeColor;

        output.setRGB(x, y, edgeColor);
      }
    }

    return output;
  }
}
