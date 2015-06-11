package cs211.imageprocessing;

import processing.core.PImage;
import processing.core.PApplet;
import processing.core.PVector;
import processing.video.Movie;

import java.awt.event.KeyEvent;
import java.util.*;

public class ImageProcessing extends PApplet {
    //Capture cam;
    Movie cam;
    ArrayList<Integer> shapeColors = new ArrayList<>();
    ArrayList<int[]> corners = new ArrayList<int[]>();

    Random random = new Random();

    int offset = 25;
    int houghMinVotes = 145;
    int houghNeighborough = 21;
    int houghNbLines = 6;
    int minQuadArea =  42800;
    int maxQuadArea = 164000;

    HScrollbar minHueThresholdBar = new HScrollbar(this, 0, 480, 640, 20).setPos(80/256.0F);
    HScrollbar maxHueThresholdBar = new HScrollbar(this, 0, 480 + 1 * offset, 640, 20).setPos(137/256.0F);

    HScrollbar minBrightnessThresholdBar = new HScrollbar(this, 0, 480 + 2 * offset, 640, 20).setPos(73/256.0F);
    HScrollbar maxBrightnessThresholdBar = new HScrollbar(this, 0, 480 + 3 * offset, 640, 20).setPos(236/256.0F);

    HScrollbar minSaturationThresholdBar = new HScrollbar(this, 0, 480 + 4 * offset, 640, 20).setPos(65/256.0F);
    HScrollbar maxSaturationThresholdBar = new HScrollbar(this, 0, 480 + 5 * offset, 640, 20).setPos(256/256.0F);

    HScrollbar minIntensityTresholdBar = new HScrollbar(this, 0, 480 + 6 * offset, 640, 20).setPos(66/255f);
    HScrollbar maxIntensityTresholdBar = new HScrollbar(this, 0, 480 + 7 * offset, 640, 20).setPos(222/255f);

    boolean playing = true;

    public void setup() {
        size(1280, 745);
        frameRate(60);
        cam = new Movie(this, "C:\\Users\\Sfimx\\Documents\\testvideo.mp4");
        cam.loop();
    }

    public void keyPressed() {
        if (keyCode == ENTER) {
            if(playing) cam.pause(); else cam.loop();
            playing = !playing;
        } else if (key == KeyEvent.VK_PERIOD) {
            houghNbLines--;
        } else if (key == KeyEvent.VK_MINUS) {
            houghNbLines++;
        } else if (key == 'q') {
            minQuadArea-=100;
        } else if (key == 'w') {
            minQuadArea+=100;
        } else if (key == 'a') {
            maxQuadArea-=100;
        } else if (key == 's') {
            maxQuadArea+=100;
        }

        else if (key == CODED) {
          if (keyCode == UP) {
            houghMinVotes += 5;
          } else if (keyCode == DOWN) {
              houghMinVotes -= 5;
          } else if (keyCode == LEFT) {
              houghNeighborough -= 2;
          } else if (keyCode == RIGHT) {
              houghNeighborough += 2;
          }
        }
      }

    public void draw() {
        cam.read();
        getRotation(true, cam, new TwoDThreeD(cam.width, cam.height));
    }


    public PVector getRotation(boolean display, PImage img, TwoDThreeD twoDThreeD) {
        //println("cam.available(): " + cam.available());
        //if(cam.available()) {
        //    cam.read();
        //}
        //if(playing) cam.read();
        //cam.loadPixels();
        //img = cam;
        //img = cam.get();

        if(img == null)
            return null;


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

        minHueThresholdBar.update();
        maxHueThresholdBar.update();

        if(display) {

            minHueThresholdBar.update();
            maxHueThresholdBar.update();

        minBrightnessThresholdBar.update();
        maxBrightnessThresholdBar.update();

        minSaturationThresholdBar.update();
        maxSaturationThresholdBar.update();

        minIntensityTresholdBar.update();
        maxIntensityTresholdBar.update();

            background(color(255, 255, 255));
            fill(0, 0, 0);


            text("minHueTresholdBar : " + minHueThresholdBar.getPos() * 256, 650f, 495f);
            text("maxHueThresholdBar : " + maxHueThresholdBar.getPos() * 256, 650f, 495f + 1 * offset);
            text("minBrightnessThresholdBar : " + minBrightnessThresholdBar.getPos() * 256, 650f, 495f + 2 * offset);
            text("maxBrightnessThresholdBar : " + maxBrightnessThresholdBar.getPos() * 256, 650f, 495f + 3 * offset);
            text("minSaturationThresholdBar : " + minSaturationThresholdBar.getPos() * 256, 650f, 495f + 4 * offset);
            text("maxSaturationThresholdBar : " + maxSaturationThresholdBar.getPos() * 256, 650f, 495f + 5 * offset);
            text("minIntensityTresholdBar : " + minIntensityTresholdBar.getPos() * 256, 650f, 495f + 6 * offset);
            text("maxIntensityTresholdBar : " + maxIntensityTresholdBar.getPos() * 256, 650f, 495f + 7 * offset);
            text("houghMinVotes : " + houghMinVotes, 650f, 495f + 8 * offset - 10);
            text("houghNeighborough : " + houghNeighborough, 650f, 495f + 9 * offset - 20);
            text("houghNbLines : " + houghNbLines, 650f, 495f + 10 * offset - 30);

            text("minQuadArea : " + minQuadArea, 800f, 495f + 8 * offset - 10);
            text("maxQuadArea : " + maxQuadArea, 800f, 495f + 9 * offset - 20);

            
        }


        img.loadPixels();
        PImage threshold = threshold(
                img,
                minHueThresholdBar.getPos() * 256,
                maxHueThresholdBar.getPos() * 256,
                minBrightnessThresholdBar.getPos() * 256,
                maxBrightnessThresholdBar.getPos() * 256,
                minSaturationThresholdBar.getPos() * 256,
                maxSaturationThresholdBar.getPos() * 256
        );
        threshold.loadPixels();
        PImage blurred = convolute(blur, 99, threshold);
        blurred.loadPixels();;
        PImage intensityTreshold = threshold(blurred, 0, 255f, minIntensityTresholdBar.getPos() * 255, maxIntensityTresholdBar.getPos() * 255, 0, 255f);
        intensityTreshold.loadPixels();
        PImage sobel = sobel(intensityTreshold);
        sobel.loadPixels();

        if(display) image(img, 0, 0);

        ArrayList<PVector> lines = new ArrayList<>();
        PImage accumulator = hough(sobel, houghMinVotes, houghNeighborough, houghNbLines, lines);
        QuadGraph quadGraph = new QuadGraph();
        quadGraph.build(lines, img.width, img.height);
        List<int[]> cycles = quadGraph.findCycles();


        int shapeCount = 0;
        ArrayList<ArrayList<PVector>> corners = new ArrayList<>();

        float currentMaxArea = 0;
        ArrayList<int[]> newQuad = new ArrayList<int[]>();
        
        for (int[] quad : cycles) {
            
            PVector l1 = lines.get(quad[0]);
            PVector l2 = lines.get(quad[1]);
            PVector l3 = lines.get(quad[2]);
            PVector l4 = lines.get(quad[3]);

            PVector c12 = intersection(l1, l2);
            PVector c23 = intersection(l2, l3);
            PVector c34 = intersection(l3, l4);
            PVector c41 = intersection(l4, l1);
            
            
            float area = QuadGraph.area(c12, c23, c34, c41);
            if(area <= currentMaxArea) {
                continue;
            } else {
                currentMaxArea = area;
            }
            
          //TODO check if adjustement needed here
            if (
                    QuadGraph.isConvex(c12, c23, c34, c41) &&
                            //QuadGraph.nonFlatQuad(c12, c23, c34, c41) &&
                            QuadGraph.validArea(area, maxQuadArea, minQuadArea)
                    ) {
                newQuad.add(quad);
            }
        }
        
        for (int[] quad : newQuad) {             
                
                // draw once what we keep

                
                PVector l1 = lines.get(quad[0]);
                PVector l2 = lines.get(quad[1]);
                PVector l3 = lines.get(quad[2]);
                PVector l4 = lines.get(quad[3]);

                PVector c12 = intersection(l1, l2);
                PVector c23 = intersection(l2, l3);
                PVector c34 = intersection(l3, l4);
                PVector c41 = intersection(l4, l1);

                if(display) {
                    fill(255, 128, 0);
                    stroke(255, 128, 0);

                    drawLine(l1.x, l1.y, img.width);
                    drawLine(l2.x, l2.y, img.width);
                    drawLine(l3.x, l3.y, img.width);
                    drawLine(l4.x, l4.y, img.width);
                }



                ArrayList<PVector> temp = new ArrayList<>();

                temp.add(c12);
                temp.add(c23);
                temp.add(c34);
                temp.add(c41);

                corners.add(temp);

                if(display) {
                    noStroke();

                    if (shapeCount >= shapeColors.size()) {
                        Random random = new Random();
                        int newColor = color(
                                min(255, random.nextInt(300)),
                                min(255, random.nextInt(300)),
                                min(255, random.nextInt(300)),
                                120
                        );
                        shapeColors.add(newColor);
                        fill(newColor);
                    } else {
                        fill(shapeColors.get(shapeCount));
                    }

                    quad(c12.x, c12.y, c23.x, c23.y, c34.x, c34.y, c41.x, c41.y);
                }

                shapeCount++;
            }
        

        ArrayList<PVector> selected = null;

        
        while(!corners.isEmpty() && (selected == null || selected.size() != 4)) {
            if(selected != null && selected.size() != 4) {
                corners.remove(selected);
            }
            selected = corners.get(random.nextInt(corners.size()));
        }

        if(display) {
            accumulator.resize(300, 600);
            //image(accumulator, 800, 0);
            image(sobel, 640, 0);
        }


        PVector rotations = null;
        List<PVector> sorted = null;

        if (selected != null && selected.size() == 4) {
            rotations = twoDThreeD.get3DRotations(selected);

            println("rx: " + degrees(rotations.x));
            println("ry: " + degrees(rotations.y));
            println("rz: " + degrees(rotations.z));

            sorted = sortCorners(selected);
            if(display) {
                int i = 0;
                for(PVector point : sorted) {
                    fill(255, 0, 0);
                    ellipse(point.x, point.y, 10, 10);
                    text("papayaaaaaa" + i, point.x, point.y);
                    i++;
                }
            }
        }

        if(display) {
            minHueThresholdBar.display();
            maxHueThresholdBar.display();

            minBrightnessThresholdBar.display();
            maxBrightnessThresholdBar.display();

            minSaturationThresholdBar.display();
            maxSaturationThresholdBar.display();

            minIntensityTresholdBar.display();
            maxIntensityTresholdBar.display();
        }
        
        
        
       /* if(selected != null) {
         // Sort corners so that they are ordered clockwise
            PVector a = selected.get(0);
            PVector b = selected.get(2);
            PVector c = selected.get(3); 
            PVector d = selected.get(1);
            PVector center = new PVector((a.x+b.x+c.x +d.x)/4,(a.y+b.y+c.y + d.y)/4);
            Collections.sort(selected,new CWComparator(center));
            Collections.sort(selected, new CWComparator(center));
        }*/
        return sorted == null ? null : twoDThreeD.get3DRotations(sorted);
    }

    public PImage convolute(float[][] kernel, float weight, PImage img) {

        PImage result = createImage(img.width, img.height, RGB);

        for(int x = 1; x < img.width - 1; x++) {
            for(int y = 1; y < img.height - 1; y++) {
                float rConvolution = 0;
                int kernelX;
                int kernelY;


                for(int xKernel = x - 1; xKernel <= x + 1; xKernel++) {
                    for(int yKernel = y - 1; yKernel <= y + 1; yKernel++) {
                        kernelX = xKernel - x + 1;
                        kernelY = yKernel - y + 1;

                        int color = img.pixels[yKernel * img.width + xKernel];

                        float kernelValue = kernel[kernelY][kernelX];

                        rConvolution +=  brightness(color) * kernelValue;
                    }
                }

                rConvolution = constrain(rConvolution  / weight, 0, 255);


                result.pixels[y * img.width + x] = color(rConvolution);
            }
        }

        result.updatePixels();
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

        //TODO check max
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

        PImage result = createImage(img.width, img.height, ALPHA);


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

    public PImage hough(PImage edgeImg, int minVotes, int neighbourhood, int nLines, ArrayList<PVector> lines) {
        float discretizationStepsPhi = 0.06f;
        float discretizationStepsR = 2.5f;

        int phiDim = (int) (Math.PI / discretizationStepsPhi);
        int rDim = (int) (((edgeImg.width + edgeImg.height) * 2 + 1) / discretizationStepsR);

        int[] accumulator = new int[(phiDim + 2) * (rDim + 2)];

     // pre-compute the sin and cos values
        float[] tabSin = new float[phiDim];
        float[] tabCos = new float[phiDim];
        float ang = 0;
        float inverseR = 1.f / discretizationStepsR;
        for (int accPhi = 0; accPhi < phiDim; ang += discretizationStepsPhi, accPhi++)
        {
            // we can also pre-multiply by (1/discretizationStepsR) since we need it in the Hough loop
            tabSin[accPhi] = (float) (Math.sin(ang) * inverseR);
            tabCos[accPhi] = (float) (Math.cos(ang) * inverseR);
        }

        for(int y = 0; y < edgeImg.height; y++) {
            for(int x = 0; x < edgeImg.width; x++) {// Are we on an edge?
                if(brightness(edgeImg.pixels[y * edgeImg.width + x]) != 0) {
                    // ...determine here all the lines (r, phi) passing through
                    // pixel (x,y), convert (r,phi) to coordinates in the
                    // accumulator, and increment accordingly the accumulator.
                    for(int accPhi = 0; accPhi < phiDim; accPhi++) {
                        double kindOfR = x*tabCos[accPhi] + y*tabSin[accPhi];
                        double accR =  kindOfR + (rDim + 2)/2;//+2 car marges dans acc
                        accumulator[(int) (accR+1+(accPhi+1)*(rDim+2))] += 1;
                    }
                }
            }
        }

        PImage houghImg = createImage(rDim + 2, phiDim + 2, ALPHA);

        for(int i = 0; i < accumulator.length; i++) {
            houghImg.pixels[i] = color(min(255, accumulator[i]));
        }


        ArrayList<Integer> bestCandidates = new ArrayList<Integer>();


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

        int maxLine = min(bestCandidates.size(), nLines);
        for(int i = 0; i < maxLine; i++) {
            int idx = bestCandidates.get(i);
            int accPhi = (int) (idx / (rDim + 2)) - 1;
            int accR = idx - (accPhi + 1) * (rDim + 2) - 1;
            float r = (accR - (rDim - 1) * 0.5f) * discretizationStepsR;
            float phi = accPhi * discretizationStepsPhi;

            lines.add(new PVector(r, phi));

        }

        return houghImg;
    }

    public void drawLine(float r, float phi, int imageWidth) {
        int x0 = 0;
        int y0 = (int) (r / sin(phi));



        int x1 = (int) (r / cos(phi));

        int y1 = 0;


        int x2 = imageWidth;
        int y2 = (int) (-cos(phi) / sin(phi) * x2 + r / sin(phi));


        int y3 = imageWidth;
        int x3 = (int) (-(y3 - r / sin(phi)) * (sin(phi) / cos(phi)));



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


    public static List<PVector> sortCorners(List<PVector> quad){
        if(quad.isEmpty())
            return new ArrayList<>();

    	// Sort corners so that they are ordered clockwise
    	PVector a = quad.get(0);
    	PVector b = quad.get(2);
    	PVector c = quad.get(3); 
    	PVector d = quad.get(1);
    	PVector center = new PVector((a.x+b.x+c.x +d.x)/4,(a.y+b.y+c.y + d.y)/4);
    	Collections.sort(quad,new CWComparator(center));
    	// TODO:
    	// Re-order the corners so that the first one is the closest to the
    	// origin (0,0) of the image.
    	//
    	// You can use Collections.rotate to shift the corners inside the quad.

    	int index = 0;
    	PVector origine = new PVector(0,0,0);
    	float min = Integer.MAX_VALUE;

    	for(int i = 0; i < quad.size(); i++){
    		if (origine.dist(quad.get(i))< min) {
    			min = origine.dist(quad.get(i));
    			index = i;
    		}
    	}

    	Collections.rotate(quad, -index);

    	return quad;
    }

    static public void main(String[] passedArgs) {
        String[] appletArgs = new String[] { "cs211.imageprocessing.ImageProcessing" };
        if (passedArgs != null) {
            PApplet.main(concat(appletArgs, passedArgs));
        } else {
            PApplet.main(appletArgs);
        }
    }
}
