package cs211;

import processing.core.PImage;
import processing.core.PApplet;
import processing.core.PVector;
import processing.video.Capture;

import java.util.*;

/**
 * Created by Sfimx on 28.04.2015.
 */


public class ImageProcessing extends PApplet   {
    PImage img;
    int[][] position;
    PImage[] images;
    Capture cam;
    int[] colors;
    ArrayList<Integer> shapeColors;

    HScrollbar maxBar = new HScrollbar(this, 0, 600, 640, 20);

    HScrollbar minHueThresholdBar = new HScrollbar(this, 0, 640, 640, 20).setPos(100/256.0F);
    HScrollbar maxHueThresholdBar = new HScrollbar(this, 0, 680, 640, 20).setPos(136/256.0F);

    HScrollbar minBrightnessThresholdBar = new HScrollbar(this, 0, 720, 640, 20).setPos(34/256.0F);
    HScrollbar maxBrightnessThresholdBar = new HScrollbar(this, 0, 760, 640, 20).setPos(256/256.0F);

    HScrollbar minSaturationThresholdBar = new HScrollbar(this, 0, 800, 640, 20).setPos(30/256.0F);
    HScrollbar maxSaturationThresholdBar = new HScrollbar(this, 0, 840, 640, 20).setPos(256/256.0F);

    HScrollbar imageSelectionBar = new HScrollbar(this, 0, 880, 640, 20);

    HScrollbar minIntensityTresholdBar = new HScrollbar(this, 0, 920, 640, 20).setPos(50/255f);
    HScrollbar maxIntensityTresholdBar = new HScrollbar(this, 0, 960, 640, 20).setPos(190/255f);

    public void setup() {


        size(1200, 1000);
        //String[] cameras = Capture.list();

        /*if(cameras.length == 0) {
            println("No camera available");
            exit();
        }
        else {
            println("Available cameras :");
            int i = 0;
            for (String camera : cameras) {
                println(i + " " + camera);
                i++;
            }

            cam = new Capture(this, cameras[3]);
            cam.start();
        }*/
        images = new PImage[] {
                loadImage("board1.jpg"),
                loadImage("board2.jpg"),
                loadImage("board3.jpg"),
                loadImage("board4.jpg")
        };


        colors = new int[] {
                color(204, 102, 0),
                color(204, 0, 62),
                color(0, 29, 204),
                color(69, 162, 24)
        };

        shapeColors = new ArrayList<>();


        frameRate(4);
    }

    public void draw() {
        //if(cam.available()) {
        //    cam.read();
        //}
        //img = cam.get();


        float[][] kernel = {
                {1,2,1},
                {2,4,2},
                {1,2,1}
        };

        float[][] kernel2 = {
                {0,0,0},
                {0,1,0},
                {0,0,0}
        };

        // 99
        float[][] blur = {
                {9,12,9},
                {12,15,12},
                {9,12,9}
        };

        float[][] sobelV = {
                {0, 0, 0},
                {1, 0, -1},
                {0, 0, 0}
        };

        float[][] sobelH = {
                {0, 1, 0},
                {0, 0, 0},
                {0, -1, 0}
        };

        maxBar.update();
        minHueThresholdBar.update();
        maxHueThresholdBar.update();

        minBrightnessThresholdBar.update();
        maxBrightnessThresholdBar.update();

        minSaturationThresholdBar.update();
        maxSaturationThresholdBar.update();

        imageSelectionBar.update();

        minIntensityTresholdBar.update();
        maxIntensityTresholdBar.update();

        background(color(255, 255, 255));





        int i = 0;
        int imageIndex = round((imageSelectionBar.getPos() * 4));
        imageIndex = imageIndex == images.length ? imageIndex - 1 : imageIndex;

        img = images[imageIndex];



        PImage threshold = threshold(
                img,
                minHueThresholdBar.getPos() * 256,
                maxHueThresholdBar.getPos() * 256,
                minBrightnessThresholdBar.getPos() * 256,
                maxBrightnessThresholdBar.getPos() * 256,
                minSaturationThresholdBar.getPos() * 256,
                maxSaturationThresholdBar.getPos() * 256
        );
        PImage blurred = convolute(blur, 99, threshold);
        PImage intensityTreshold = threshold(blurred, 0, 256, minIntensityTresholdBar.getPos() * 255, maxIntensityTresholdBar.getPos() * 255, 0, 256);
        PImage sobel = sobel(intensityTreshold);

        image(sobel, 0, 0);
        //image(blurred, 1200, 0);

        ArrayList<PVector> lines = hough (sobel, 0, 0, colors[imageIndex], 180, 10, 6);
        QuadGraph quadGraph = new QuadGraph();
        getIntersections(lines);
        quadGraph.build(lines, img.width, img.height);
        List<int[]> cycles = quadGraph.findCycles();


        float shapeMaxArea = 0f;
        int shapeCount = 0;
        int shapeRawCount = 0;

        for(int[] quad : cycles) {
            if(quad.length == 4) {
                shapeRawCount++;

                PVector l1 = lines.get(quad[0]);
                PVector l2 = lines.get(quad[1]);
                PVector l3 = lines.get(quad[2]);
                PVector l4 = lines.get(quad[3]);

                PVector c12 = intersection(l1, l2);
                PVector c23 = intersection(l2, l3);
                PVector c34 = intersection(l3, l4);
                PVector c41 = intersection(l4, l1);

                if (
                       QuadGraph.isConvex(c12, c23, c34, c41)
                    && QuadGraph.nonFlatQuad(c12, c23, c34, c41)
                ) {
                    float shapeArea = c12.dist(c23) * c12.dist(c41);

                    if(shapeArea < shapeMaxArea) {
                        continue;
                    }

                    shapeMaxArea = shapeArea;



                    if (shapeCount >= shapeColors.size()) {
                        Random random = new Random();
                        int newColor = color(
                                min(255, random.nextInt(300)),
                                min(255, random.nextInt(300)),
                                min(255, random.nextInt(300)),
                                50
                        );
                        shapeColors.add(newColor);
                        fill(newColor);
                    } else {
                        fill(shapeColors.get(shapeCount));
                    }
                    shapeCount++;
                    quad(c12.x, c12.y, c23.x, c23.y, c34.x, c34.y, c41.x, c41.y);

                }
            }
        }

        println("Raw count: " + shapeRawCount + ", Count: " + shapeCount);

        //ArrayList<PVector> lines = hough(sobel, 0, 0, colors[imageIndex], 180, 10, 6);
        //getIntersections(lines);



        maxBar.display();
        minHueThresholdBar.display();
        maxHueThresholdBar.display();

        minBrightnessThresholdBar.display();
        maxBrightnessThresholdBar.display();

        minSaturationThresholdBar.display();
        maxSaturationThresholdBar.display();

        imageSelectionBar.display();

        minIntensityTresholdBar.display();
        maxIntensityTresholdBar.display();





        //image(sobel, 0, 0);



        //println(thresholdBar.getPos() * 255);
    }

    public PImage convolute(float[][] kernel, float weight, PImage img) {

        PImage result = createImage(img.width, img.height, ALPHA);

        for(int x = 1; x < img.width - 1; x++) {
            for(int y = 1; y < img.height - 1; y++) {
                float rConvolution = 0;
                int kernelX;
                int kernelY;

                for(int xKernel = x - 1; xKernel <= x + 1; xKernel++) {
                    for(int yKernel = y - 1; yKernel <= y + 1; yKernel++) {
                        kernelX = xKernel - x + 1;
                        kernelY = yKernel - y + 1;

                        int color = img.get(xKernel, yKernel);
                        float kernelValue = kernel[kernelY][kernelX];


                        rConvolution +=  brightness(color) * kernelValue;
                    }
                }

                rConvolution = constrain(rConvolution  / weight, 0, 255);


                result.pixels[y * img.width + x] = color(rConvolution);
            }
        }

        return result;
    }

    public PImage sobel(PImage img) {
        float[][] hKernel = {
                {0, 1, 0},
                {0, 0, 0},
                {0, -1, 0}
        };

        float[][] vKernel = {
                {0, 0, 0},
                {1, 0, -1},
                {0, 0, 0}
        };

        PImage result = createImage(img.width, img.height, ALPHA);

        for (int i = 0; i < img.width * img.height; i++) {
            result.pixels[i] = color(0);
        }

        //float max = 0;
        float[] buffer = new float[img.width * img.height];

        PImage hConvoluted = convolute(hKernel, 1, img);
        PImage vConvoluted = convolute(vKernel, 1, img);

        float max = 0;
        for (int x = 0; x < img.width; x++) {
            for (int y = 0; y < img.height; y++) {
                float h = brightness(hConvoluted.pixels[y * img.width + x]);
                float v = brightness(vConvoluted.pixels[y * img.width + x]);

                float sum = sqrt(
                        h * h + v * v
                );
                if(sum > max) {
                    max = sum;
                }
                buffer[y * img.width + x] = sum;

            }
        }

        for (int y = 2; y < img.height - 2; y++) {
            for (int x = 2; x < img.width - 2; x++) {
                if (buffer[y * img.width + x] > (max * 0.3f)) {
                    result.pixels[y * img.width + x] = color(255);
                } else {
                    result.pixels[y * img.width + x] = color(0);
                }
            }
        }

        return result;
    }


    public PImage threshold(
            PImage img,
            float hueMin, float hueMax,
            float brighnessMin, float brightnessMax,
            float saturationMin, float saturationMax
    ) {
        PImage result = createImage(img.width, img.height, RGB);

        for(int i = 0; i < img.width * img.height; i++) {
            float hue = hue(img.pixels[i]);
            float brightness = brightness(img.pixels[i]);
            float saturation = saturation(img.pixels[i]);
            result.pixels[i] =
                    hue >= hueMin  && hue <= hueMax
                            && brightness >= brighnessMin  && brightness <= brightnessMax
                            && saturation >= saturationMin  && saturation <= saturationMax
                            ? color(255) : color(0);
        }

        return result;
    }



    public ArrayList<PVector> hough(PImage edgeImg, int leftOffset, int topOffset, int color, int minVotes, int neighbourhood, int nLines) {
        float discretizationStepsPhi = 0.06f;
        float discretizationStepsR = 2.5f;

        int phiDim = (int) (Math.PI / discretizationStepsPhi);
        int rDim = (int) (((edgeImg.width + edgeImg.height) * 2 + 1) / discretizationStepsR);

        int[] accumulator = new int[(phiDim + 2) * (rDim + 2)];

        for(int y = 0; y < edgeImg.height; y++) {
            for(int x = 0; x < edgeImg.width; x++) {
                if(brightness(edgeImg.pixels[y * edgeImg.width + x]) != 0) {
                    for(int accPhi = 0; accPhi < phiDim; accPhi++) {
                        float truePhi = accPhi * discretizationStepsPhi;
                        float r = x * cos(truePhi) + y * sin(truePhi);

                        float accR = ((r / discretizationStepsR) + (rDim + 2) / 2);


                        accumulator[(int) (accR + (accPhi + 1) * (rDim + 2) + 1)]++;

                    }
                }
            }
        }

        PImage houghImg = createImage(rDim + 2, phiDim + 2, ALPHA);

        for(int i = 0; i < accumulator.length; i++) {
            houghImg.pixels[i] = color(min(255, accumulator[i]));
        }

        houghImg.resize(400, 300);
        image(houghImg, 800, 0);

        ArrayList<Integer> bestCandidates = new ArrayList<Integer>();

        int count = 0;
        neighbourhood = 10;
        minVotes = 200;


        for(int accR = 0; accR < rDim; accR++) {
            for (int accPhi = 0; accPhi < phiDim; accPhi++) {

                int idx = (accPhi + 1) * (rDim + 2) + accR + 1;

                if (accumulator[idx] > minVotes) {

                    boolean bestCandidate = true;

                    for (int dPhi = -neighbourhood / 2; dPhi < neighbourhood / 2 + 1; dPhi++) {
                        if(accPhi+dPhi < 0 || accPhi + dPhi >= phiDim)
                            continue;

                        for (int dR = -neighbourhood / 2; dR < neighbourhood / 2 + 1; dR++) {
                            if(accR + dR < 0 || accR + dR >= rDim)
                                continue;

                            int neighbourIdx = (accPhi + dPhi + 1) * (rDim + 2) + accR + dR + 1;

                            if(accumulator[idx] < accumulator[neighbourIdx]) {
                                bestCandidate = false;
                                break;
                            }
                        }
                        if(!bestCandidate) break;
                    }

                    if(bestCandidate) {
                        bestCandidates.add(idx);
                    }

                }
            }
        }

        Collections.sort(bestCandidates, new HoughComparator(accumulator));
        ArrayList<PVector> lines = new ArrayList<>();


        int maxLine = min(bestCandidates.size(), nLines);
        for(int i = 0; i < maxLine; i++) {
            int idx = bestCandidates.get(i);
            count++;
            int accPhi = (int) (idx / (rDim + 2)) - 1;
            int accR = idx - (accPhi + 1) * (rDim + 2) - 1;
            float r = (accR - (rDim - 1) * 0.5f) * discretizationStepsR;
            float phi = accPhi * discretizationStepsPhi;

            lines.add(new PVector(r, phi));


            int x0 = 0;
            int y0 = (int) (r / sin(phi));



            int x1 = (int) (r / cos(phi));

            int y1 = 0;


            int x2 = edgeImg.width;
            int y2 = (int) (-cos(phi) / sin(phi) * x2 + r / sin(phi));


            int y3 = edgeImg.width;
            int x3 = (int) (-(y3 - r / sin(phi)) * (sin(phi) / cos(phi)));


            stroke(color);

            if (y0 > 0) {
                if (x1 > 0) {
                    line(x0, y0, x1, y1);
                }
                else if (y2 > 0)
                    line(x0, y0, x2, y2);
                else
                    line(x0, y0, x3, y3);
            } else {
                if (x1 > 0) {
                    if (y2 > 0)
                        line(x1, y1, x2, y2);
                    else
                        line(x1, y1, x3, y3);
                } else
                    line(x2, y2, x3, y3);
            }
        }

        return lines;
    }


    public ArrayList<PVector> getIntersections(List<PVector> lines) {
        ArrayList<PVector> intersections = new ArrayList<>();


        for(int i = 0; i < lines.size() - 1; i++) {
            PVector line1 = lines.get(i);

            for(int j = i + 1; j < lines.size(); j++) {
                PVector intersect = intersection(line1, lines.get(j));

                intersections.add(intersect);

                fill(255, 128, 0);
                ellipse(intersect.x, intersect.y, 10, 10);
            }
        }

        return intersections;
    }

    public PVector intersection(PVector line1, PVector line2) {
        float r1 = line1.x;
        float r2 = line2.x;

        float phi1 = line1.y;
        float phi2 = line2.y;

        float d = cos(phi2) * sin(phi1) - cos(phi1) * sin(phi2);
        float x = (r2 * sin(phi1) - r1 * sin(phi2)) / d;
        float y = (-r2 * cos(phi1) + r1 * cos(phi2)) / d;

        return new PVector(x, y);
    }

    static public void main(String[] passedArgs) {
        String[] appletArgs = new String[] { "cs211.ImageProcessing" };
        if (passedArgs != null) {
            PApplet.main(concat(appletArgs, passedArgs));
        } else {
            PApplet.main(appletArgs);
        }
    }
}
