package bebee.beans;

import bebee.pojo.HashedImage;

import java.awt.Graphics2D;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.imageio.ImageIO;


public class HashedImageBean {
    private static int ORIGINAL_WIDTH = 0;
    private static int ORIGINAL_HEIGHT = 0;

    private int size = 32;
    private int smallerSize = 8;
    private HashedImage hashedImage;


    public HashedImage getHashedImage() {
        return hashedImage;
    }

    public void setHashedImage(HashedImage hashedImage) {
        this.hashedImage = hashedImage;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setSmallerSize(int smallerSize) {
        this.smallerSize = smallerSize;
    }

    public int getSmallerSize() {
        return smallerSize;
    }

    public HashedImageBean(HashedImage hashedImage, int size, int smallerSize) {
        this.hashedImage = hashedImage;
        this.size = size;
        this.smallerSize = smallerSize;

        initCoefficients();
    }

    public String getHash() throws IOException {
        BufferedImage img = new BufferedImage(420, 420,
        BufferedImage.TYPE_INT_ARGB);

        InputStream is = hashedImage.getInputStream();
        img = ImageIO.read(is);
        this.ORIGINAL_HEIGHT = img.getHeight();
        this.ORIGINAL_WIDTH = img.getWidth();

        img = grayscale(img);
        img = borderRemoval(img);
        img = resize(img, size, size);
        img = rotate(img);
        

        double[][] vals = new double[size][size];
        for (int x = 0; x < img.getWidth(); x++) {
            for (int y = 0; y < img.getHeight(); y++) {
                vals[x][y] = getBlue(img, x, y);
            }
        }

        long start = System.currentTimeMillis();
        double[][] dctVals = applyDCT(vals);
        double total = 0;

        for (int x = 0; x < getSmallerSize(); x++) {
            for (int y = 0; y < getSmallerSize(); y++) {
                total += dctVals[x][y];
            }
        }
        total -= dctVals[0][0];

        double avg = total / (double) ((getSmallerSize() * getSmallerSize()) - 1);
        String hash = "";

        for (int x = 0; x < getSmallerSize(); x++) {
            for (int y = 0; y < getSmallerSize(); y++) {
                if (x != 0 || y != 0) {
                    hash += (dctVals[x][y] > avg ? "1" : "0");
                }
            }
        }

        byte[] b = hash.getBytes();
        for (int i = 0; i < b.length; i++) {
            if (hash.charAt(i) == '0') {
                b[i] = 0;
            } else {
                b[i] = (byte) 255;
            }
        }

        return hash;
    }
    

    private BufferedImage rotate(BufferedImage img) {
        List<Double> meanArray = new ArrayList<>();
        int sides = 2;
        if (this.ORIGINAL_HEIGHT == this.ORIGINAL_WIDTH) {
            sides = 4;
        } else if (this.ORIGINAL_HEIGHT > this.ORIGINAL_WIDTH) {
            try {
                img = rotate(img, 90.0);
            } catch (Exception ex) {
            }
        }
        for (int i = 0; i < sides; i++) {
            meanArray.add(i, 0.0);
        }

        try {
            for (int x = 0; x < img.getWidth(); x++) {
                for (int y = 0; y < img.getHeight(); y++) {
                    if (sides == 4) {
                        if (x < img.getWidth() / 2) {
                            meanArray.set(2, meanArray.get(2) + getBlue(img, x, y) / (img.getHeight() * (img.getWidth() / 2.0)));
                        } else {
                            meanArray.set(3, meanArray.get(3) + getBlue(img, x, y) / (img.getHeight() * (img.getWidth() / 2.0)));
                        }
                    }
                    if (y < img.getHeight() / 2) {
                        meanArray.set(0, meanArray.get(0) + getBlue(img, x, y) / ((img.getHeight() / 2.0) * img.getWidth()));
                    } else {
                        meanArray.set(1, meanArray.get(1) + getBlue(img, x, y) / ((img.getHeight() / 2.0) * img.getWidth()));
                    }
                }
            }
        } catch (Exception e) {
        }

        double maxValue = Collections.max(meanArray);
        double minValue = Collections.min(meanArray);

        if (maxValue != minValue) {
            int maxPosition = -1;
            int minPosition = -1;
            int nMax = 0;
            int nMin = 0;

            for (int i = 0; i < meanArray.size(); i++) {
                if (meanArray.get(i) == maxValue) {
                    maxPosition = i;
                    nMax++;
                }
                if (meanArray.get(i) == minValue) {
                    minPosition = i;
                    nMin++;
                }
            }

            try {
                if (nMax == 1) {
                    //ponemos el lado maxPosition arriba
                    if (maxPosition == 1) {
                        img = rotate(img, 180.0);
                    } else if (maxPosition == 2) {
                        img = rotate(img, 90.0);
                    } else if (maxPosition == 3) {
                        img = rotate(img, 270.0);
                    }
                }
            } catch (Exception e) {
            }

        }
        return img;
    }

    private BufferedImage resize(BufferedImage image, int width, int height) {
        BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(image, 0, 0, width, height, null);
        g.dispose();
        return resizedImage;
    }

    private BufferedImage borderRemoval(BufferedImage img) {
        int colorTL = getBlue(img, 0, 0);
        int colorTR = getBlue(img, img.getWidth() - 1, 0);
        int colorBL = getBlue(img, 0, img.getHeight() - 1);
        int colorBR = getBlue(img, img.getWidth() - 1, img.getHeight() - 1);

        if (colorTL == colorTR && colorTR == colorBL && colorBL == colorBR) {
            int endBorder1 = img.getHeight() / 10;
            int endBorder2 = img.getHeight() - (img.getHeight() / 10);
            int endBorder3 = img.getWidth() / 10;
            int endBorder4 = img.getWidth() - (img.getWidth() / 10);
            int prevValX = colorTL;
            int prevValY = colorTL;
            for (int i = 0; i < img.getWidth(); i++) {
                for (int j = 0; j < endBorder1; j++) {
                    if (prevValY != getBlue(img, i, j) && (prevValY > getBlue(img, i, j) + 2 || prevValY < getBlue(img, i, j) - 2)) {
                        if (j < endBorder1) {
                            endBorder1 = j;
                        }
                    }
                }
            }
            if (endBorder1 > 0) {
                for (int i = 0; i < img.getWidth(); i++) {
                    for (int j = img.getHeight() - 1; j > endBorder2; j--) {
                        if (prevValY != getBlue(img, i, j) && (prevValY > getBlue(img, i, j) + 2 || prevValY < getBlue(img, i, j) - 2)) {
                            if (j > endBorder2) {
                                endBorder2 = j + 1;
                            }
                        }
                    }
                }
                if (endBorder2 < img.getHeight()) {
                    //left border
                    for (int i = 0; i < img.getHeight(); i++) {
                        for (int j = 0; j < endBorder3; j++) {
                            if (prevValX != getBlue(img, j, i) && (prevValX > (getBlue(img, j, i) + 2) || prevValX < (getBlue(img, j, i) - 2))) {
                                if (j < endBorder3) {
                                    endBorder3 = j;
                                }
                            }
                        }
                    }
                    if (endBorder3 > 0) {
                        //right border
                        for (int i = 0; i < img.getHeight(); i++) {
                            for (int j = img.getWidth() - 1; j > endBorder4; j--) {
                                if (prevValX != getBlue(img, j, i) && (prevValX > (getBlue(img, j, i) + 2) || prevValX < (getBlue(img, j, i) - 2))) {
                                    //System.out.println("Pixel ("+j+","+i+"): Previo: "+prevValX+" - Blue: "+getBlue(img, j, i));
                                    if (j > endBorder4) {
                                        endBorder4 = j + 1;
                                    }
                                }
                            }
                        }
                    } else {
                        //System.out.println(imageNumber+" No tiene borde 3");
                    }
                    if (endBorder4 > 0) {
                        //System.out.println("Si tiene borde");
                        int newWidth = endBorder4 - endBorder3;
                        int newHeight = endBorder2 - endBorder1;
                        //System.out.println("izquierda: "+endBorder3+" Arriba: "+endBorder1+" - Derecha: "+newWidth+" ("+endBorder4+") Abajo: "+newHeight+" ("+endBorder2+")" );
                        img = cropImage(img, endBorder3, endBorder1, newWidth, newHeight);
                    } else {
                        //System.out.println(imageNumber+" No tiene borde 4");
                    }
                } else {
                    //System.out.println(imageNumber+" No tiene borde 2");
                }
            } else {
                //System.out.println(imageNumber+" No tiene borde 1");
            }
        }
        return img;
    }

    private BufferedImage cropImage(BufferedImage src, int originX, int originY, int width, int height) {
        return src.getSubimage(originX, originY, width, height);
    }

    private final ColorConvertOp colorConvert = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);

    private BufferedImage grayscale(BufferedImage img) {
        colorConvert.filter(img, img);
        return img;
    }

    private static int getBlue(BufferedImage img, int x, int y) {
        int pixel = img.getRGB(x,y);
        if( (pixel>>24) == 0x00 ) { //remove transparency bytes
            return 255;
        }else
        {
            return (img.getRGB(x, y)) & 0xff;
        }
    }

    private double[] c;

    private void initCoefficients() {
        c = new double[size];

        for (int i = 1; i < size; i++) {
            c[i] = 1;
        }
        c[0] = 1 / Math.sqrt(2.0);
    }

    private double[][] applyDCT(double[][] f) {
        int N = size;

        double[][] F = new double[N][N];
        for (int u = 0; u < N; u++) {
            for (int v = 0; v < N; v++) {
                double sum = 0.0;
                for (int i = 0; i < N; i++) {
                    for (int j = 0; j < N; j++) {
                        sum += Math.cos(((2 * i + 1) / (2.0 * N)) * u * Math.PI) * Math.cos(((2 * j + 1) / (2.0 * N)) * v * Math.PI) * (f[i][j]);
                    }
                }
                sum *= ((c[u] * c[v]) / 4.0); //TO-DO Creo que la fórmula está mal y debería ser 2.0*N (ver https://cs.stanford.edu/people/eroberts/courses/soco/projects/data-compression/lossy/jpeg/dct.htm)
                F[u][v] = sum;
            }
        }
        return F;
    }

    private void rotateArray(double[][] array, int degrees) {
        if (degrees != 90 && degrees != 180 && degrees != 270) {

        } else if (degrees == 90) {
            //transpuesta
            for (int i = 0; i < array.length; i++) {
                for (int j = i; j < array[0].length; j++) {
                    double temp = array[i][j];
                    array[i][j] = array[j][i];
                    array[j][i] = temp;
                }
            }
            //invertimos el orden de cada elemento por columnas
            for (double[] array1 : array) {
                for (int i = 0; i < array1.length / 2; i++) {
                    double temp = array1[i];
                    array1[i] = array1[array1.length - i - 1];
                    array1[array1.length - i - 1] = temp;
                }
            }
        } else if (degrees == 180) {
            //invertimos el orden de filas
            for (int i = 0; i < array.length / 2; i++) {
                double[] temp = array[i];
                array[i] = array[array.length - i - 1];
                array[array.length - i - 1] = temp;
            }
            //invertimos el orden de cada elemento por columnas
            for (double[] array1 : array) {
                for (int i = 0; i < array1.length / 2; i++) {
                    double temp = array1[i];
                    array1[i] = array1[array1.length - i - 1];
                    array1[array1.length - i - 1] = temp;
                }
            }
        } else if (degrees == 270) {

        }

    }

    /**
     * método para rotar 90 grados
     *
     * @param array
     * @param degrees
     * @return
     */
    private double[][] test(double[][] array, int degrees) {
        double[][] transposedMatrix = new double[array[0].length][array.length];

        for (int i = 0; i < array[0].length; i++) {
            for (int j = 0; j < array.length; j++) {
                transposedMatrix[i][j] = array[j][i];
            }
        }
        for (double[] transposedMatrix1 : transposedMatrix) {
            for (int i = 0; i < transposedMatrix1.length / 2; i++) {
                double temp = transposedMatrix1[i];
                transposedMatrix1[i] = transposedMatrix1[transposedMatrix1.length - i - 1];
                transposedMatrix1[transposedMatrix1.length - i - 1] = temp;
            }
        }
        return transposedMatrix;
    }

    /**
     * método test
     */
    private void testRotate90() {
        double[][] vals = {{1, 1, 1}, {1, 1, 0}, {0, 1, 1}, {0, 0, 0}};
        for (double[] n : vals) {
            for (double m : n) {
                //System.out.printf(String.valueOf(m) + " ");
            }
            //System.out.println();
        }
        double[][] vals2 = test(vals, 90);
        //System.out.println("======");
        for (double[] n : vals2) {
            for (double m : n) {
                //System.out.printf(String.valueOf(m) + " ");
            }
            //System.out.println();
        }
    }

    /**
     * Rotates an image. Actually rotates a new copy of the image.
     *
     * @param image The image to be rotated
     * @param angle The angle in degrees
     * @return The rotated image
     * @throws java.lang.Exception
     */
    public static BufferedImage rotate(BufferedImage image, double angle) throws Exception {
        angle = Math.toRadians(angle);
        double sin = Math.abs(Math.sin(angle)), cos = Math.abs(Math.cos(angle));
        int w = image.getWidth();
        int h = image.getHeight();
        int neww = (int) Math.floor(w * cos + h * sin);
        int newh = (int) Math.floor(h * cos + w * sin);

        BufferedImage result = new BufferedImage(neww, newh, BufferedImage.TYPE_INT_BGR);
        Graphics2D g = result.createGraphics();
        g.translate((neww - w) / 2, (newh - h) / 2);
        g.rotate(angle, w / 2, h / 2);
        g.drawRenderedImage(image, null);
        g.dispose();

        return result;
    }
    
}
