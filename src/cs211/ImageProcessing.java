package cs211;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import processing.core.PImage;
import processing.core.PApplet;
import processing.core.PVector;
import processing.video.Capture;

@SuppressWarnings("serial")
public class ImageProcessing extends PApplet 
{
    private PImage img1, img2, img3, img4;
    private Capture cam;
    private Boolean camEnabled = false;
    private HScrollbar minBrightnessBar, maxBrightnessBar, minSatBar, maxSatBar, minHueBar, maxHueBar, imgSelect, minIntensityBar,  maxIntensityBar;
    private List<Integer[]> colors = new ArrayList<>();
    
    public void setup()
    {
        size(640, 480);
        int interBarSpace = 3;
        int barHeight = 20;
        int barWidth = 640;
        
        // 6/180/10 ou 4/150/10 (hough parameters)
        minIntensityBar = new HScrollbar(this, 0, height-8*barHeight-7*interBarSpace, barWidth, barHeight).setPos(0);
        maxIntensityBar = new HScrollbar(this, 0, height-9*barHeight-8*interBarSpace, barWidth, barHeight).setPos(1);
        imgSelect = new HScrollbar(this, 0, height-7*barHeight-6*interBarSpace, barWidth, barHeight).setPos(0.76f);
        maxBrightnessBar = new HScrollbar(this , 0, height-6*barHeight-5*interBarSpace, barWidth, barHeight).setPos(1);//244
        minBrightnessBar = new HScrollbar(this , 0, height-5*barHeight-4*interBarSpace, barWidth, barHeight).setPos(0);//réduire ou pas..
        
        maxSatBar = new HScrollbar(this , 0, height-4*barHeight-3*interBarSpace, barWidth, barHeight).setPos(1);
        minSatBar = new HScrollbar(this , 0, height-3*barHeight-2*interBarSpace, barWidth, barHeight).setPos(0.5f);
        
        maxHueBar = new HScrollbar(this , 0, height-2*barHeight-interBarSpace, barWidth, barHeight).setPos(160/256.0F);
        minHueBar = new HScrollbar(this, 0, height-barHeight, barWidth, barHeight).setPos(100/256.0F);
        
        String[] cameras = Capture.list();
        if (cameras.length == 0) 
        {
            println("There are no cameras available for capture.");
            exit();
        } else
        {
            println("Available cameras:");
            for (int i = 0; i < cameras.length; i++) {
                println(cameras[i]);
            }
            cam = new Capture(this, cameras[0]);
            cam.start();
        }
    }
    
    
    public void draw() {    
        if(camEnabled)
        {
            if (cam.available() == true)
            {
                cam.read();
            }
            img1 = cam.get();
        }
        else
        {//select img 
            if(imgSelect.getPos()>=0.75)
                img1 = loadImage("board4.jpg");
            else if(imgSelect.getPos()>=0.5)
                img1 = loadImage("board3.jpg");
            else if(imgSelect.getPos()>=0.25)
                img1 = loadImage("board2.jpg");
            else
                img1 = loadImage("board1.jpg");
        }
        img1.resize(width, height);
        
        float minBrightness = minBrightnessBar.getPos()*256;
        float maxBrightness = maxBrightnessBar.getPos()*256;
        float minSaturation = minSatBar.getPos()*256;
        float maxSaturation = maxSatBar.getPos()*256;
        float minHue = minHueBar.getPos()*256;
        float maxHue = maxHueBar.getPos()*256;
        
        float minIntensity = minIntensityBar.getPos()*256;
        float maxIntensity = maxIntensityBar.getPos()*256;
        
        PImage firstThresh = threshold(img1, Threshold.BINARY.setThresh(minBrightness, maxBrightness, 
                                                                  minSaturation, maxSaturation, 
                                                                  minHue, maxHue));
        PImage blur = convolute(firstThresh);
        PImage secondThresh = transformation(blur, minIntensity, maxIntensity);
        PImage sobel = sobel(secondThresh);
        image(sobel, 0, 0);
        
      /*  PImage firstThresh = threshold(img2, Threshold.BINARY.setThresh(minBrightness, maxBrightness, 
                minSaturation, maxSaturation, 
                minHue, maxHue));
        PImage blur = convolute(firstThresh);
        PImage secondThresh = transformation(blur,minIntensity, maxIntensity);
        PImage sobel = sobel(secondThresh);
        image(blur, width/2, 0);*/
        
        List<PVector> lines = hough(sobel, 6, 180, 10); //nb lignes, min votes, local max neighborough /// 4,150,10 VS 6,180,10...
        getIntersections(lines);
          
          //draw quad
          QuadGraph graph = new QuadGraph();
          graph.build(lines, width, height);
          List<int[]> quads = graph.findCycles();
          float max = 0f; 
          
          for (int[] quad : quads) 
          {
              PVector l1 = lines.get(quad[0]);
              PVector l2 = lines.get(quad[1]);
              PVector l3 = lines.get(quad[2]);
              PVector l4 = lines.get(quad[3]);
              
              // (intersection() is a simplified version of the
              // intersections() method you wrote last week, that simply
              // return the coordinates of the intersection between 2 lines)
              PVector c12 = intersection(l1, l2);
              PVector c23 = intersection(l2, l3);
              PVector c34 = intersection(l3, l4);
              PVector c41 = intersection(l4, l1);
              
              //plot the quad if valid
              if (l1.mag()*l2.mag() > max) max = l1.mag()*l2.mag() ;
              if(QuadGraph.isConvex(c12, c23, c34, c41) &&
                 QuadGraph.validArea(c12, c23, c34, c41, 0.9f*width*width, max) &&//trouver valeur qui ont du sens.....
                 QuadGraph.nonFlatQuad(c12, c23, c34, c41))
              {
               // Choose a random, semi-transparent colour
                  Random random = new Random();
                  Integer[] color;
                  try {
                      color = colors.get(quads.indexOf(quad));
                  }catch(IndexOutOfBoundsException e)
                  {
                      color = new Integer[3];
                      color[0] = random.nextInt(300);
                      color[1] = random.nextInt(300);
                      color[2] = random.nextInt(300);
                      colors.add(color);
                  }             
                  
                  fill(color(Math.min(255, color[0]),
                  Math.min(255, color[1]),
                  Math.min(255, color[2]), 100));
                  image(sobel, 0, 0);
                  quad(c12.x,c12.y,c23.x,c23.y,c34.x,c34.y,c41.x,c41.y);                   
              }                         
          }
          
          
        System.out.println("minBright: "+minBrightness+", maxBright: "+maxBrightness+
                            ", minSat:"+minSaturation+", maxSat: "+maxSaturation+
                              ", minHue:"+minHue+", maxHue:"+maxHue);          
      // maxSatBar.display();
      // maxSatBar.update();
        
      //  minSatBar.display();
       // minSatBar.update();
        
       // maxBrightnessBar.display();
      //  maxBrightnessBar.update();
//        
     //  minBrightnessBar.display();
     //  minBrightnessBar.update();
        
      //  imgSelect.display();
      //  imgSelect.update();   
        
        minIntensityBar.display();
        minIntensityBar.update();
        
        maxIntensityBar.display();
        maxIntensityBar.update();
        
        maxHueBar.display();
        maxHueBar.update();
//         
        minHueBar.display();
        minHueBar.update();
    }
    
    
    /**
	 * Simple transformation without fixed threshold
	 * 
	 * @param img image to be transformed
	 * @param threshold number in the range [0..1] 
	 * @return the transformed image with the given threshold
	 */
	public PImage transformation(PImage img, float thresholdMin, float thresholdMax) {
		PImage result = createImage(width, height, ALPHA);
		// create a new, initially transparent, 'result' image
		for(int i = 0; i < img.width * img.height; i++) {
			if(hue(img.pixels[i]) >= thresholdMin && hue(img.pixels[i]) <= thresholdMax)
				result.pixels[i] = img.pixels[i]; 
			else
				result.pixels[i] = color(0);
		}
		result.updatePixels();
		return result; 
	}
	
    /**
     * Transformation that selects only colors in a specific range of hue, brightness and saturation value
     * and transform it in white, otherwise in black 
     * @param img The image to be transformed
     * @param threshMethod
     * @return new image with the applied transformation 
     */
    public PImage threshold(PImage img, Threshold threshMethod) {
        PImage result = createImage(img.width, img.height, ALPHA);
        for(int i=0; i<result.width*result.height; ++i) {
            int pixel = img.pixels[i];
            //With valid to take we varify if the color should be transformed in white or black 
            //with testing the hue, brightness and saturation value for the given pixel
            if(threshMethod.validToTake(brightness(pixel), hue(pixel), saturation(pixel)))
                result.pixels[i] = color(255); //color(hue(pixel), saturation(pixel), brightness(pixel)); //ATTENTION  if the threshold is not white/black sobel doesn't work
            else
                result.pixels[i] = color(0);
        }
        
        result.updatePixels();
        return result;
    }
    
    /**
     * The convultion operation can be understood as: for each pixel of the image, we compute
     * its updated value by using the kernel as the matrix of weights of surrounding pixels
     * @param img The image to be transformed
     * @return new image with the applied transformation 
     */
    public PImage convolute(PImage img)
    {
        PImage result = createImage(img.width, img.height, ALPHA);
        //Kernel1 and kernel2 are here just to make tests of the effects they do 
        float[][] kernel1 = { { 0, 0, 0 },
                             { 0, 2, 0 },
                            { 0, 0, 0 }};
        
        float[][] kernel2 = { { 0, 1, 0 },
                             { 1, 0, 1 },
                             { 0, 1, 0 }};
        
        float[][] gaussianKernel = { { 9, 12,  9},
                                     {12, 15, 12},
                                     { 9, 12,  9}};
        
        
        float weight = 1.f;
        int kernelSize = 3;
        
        for(int x=0; x<img.width; ++x) {
            for(int y=0; y<img.height; ++y) {
                int convResult = 0;
                //convolution
                for(int i=0; i<kernelSize; ++i) {
                    for(int j=0; j<kernelSize; ++j) {
                        //clamp the pixel coordinates
                        int xCandidate = x + i - (kernelSize/2); 
                        int yCandidate = y + j - (kernelSize/2);
                        int clampedX = (xCandidate < 0) ? 0 : 
                                    ((xCandidate > img.width-1) ? img.width-1 : xCandidate);
                        int clampedY = (yCandidate < 0)? 0 :
                                    ((yCandidate > img.height-1) ? img.height-1 : yCandidate);
                        
                        //do the actual computation
                        convResult += (img.pixels[clampedX + img.width*clampedY])*gaussianKernel[i][j];                                 
                    }
                }
                result.pixels[y*img.width+x] = ((int)(convResult/weight));
                }
        }    
        result.updatePixels();
        return result;
    }

    /**
     * The Sobel algorithm is the most basic edge detection algorithm (edges in white, other  in black) 
     * @param img The image on which we want to detect the edges
     * @return new Image with clearly visible edges
     */
    public PImage sobel(PImage img)
    {
        float[][] hKernel = { { 0,  1, 0 },
                              { 0,  0, 0 },
                              { 0, -1, 0 } };
        
        float[][] vKernel = { { 0, 0,  0 },
                              { 1, 0, -1 },
                              { 0, 0,  0 } };
        PImage result = createImage(img.width, img.height, ALPHA);
        
        double max=0;
        int kernelSize = 3;
        double[] buffer = new double[img.width * img.height];
        
        for(int x=0; x<img.width; ++x) {
            for(int y=0; y<img.height; ++y) {
                double sum_h = 0, sum_v = 0;
                double sum = 0;
                //convolution
                for(int i=0; i<kernelSize; ++i) {
                    for(int j=0; j<kernelSize; ++j) {
                        //clamp the pixel coordinates
                        int xCandidate = x+i-(kernelSize/2); 
                        int yCandidate = y+j-(kernelSize/2);
                        int clampedX = (xCandidate < 0) ? 0 : 
                                    ( (xCandidate > img.width-1) ? img.width-1 : xCandidate);
                        int clampedY = (yCandidate < 0)? 0 :
                                    ( (yCandidate > img.height-1) ? img.height-1 : yCandidate);
                        
                        //do the actual computation
                        sum_h += (img.pixels[clampedY*img.width + clampedX])*hKernel[i][j]; 
                        sum_v += (img.pixels[clampedY*img.width + clampedX])*vKernel[i][j]; 
                    }
                }
                sum = Math.sqrt(sum_h*sum_h + sum_v*sum_v);
                if(sum>max) max = sum;
                buffer[y*img.width + x] = sum;
                //result.pixels[y*width+x] = (int) (convResult/weight);
            }
        }
        
        for (int i = 0; i < img.width * img.height; ++i) {
            if (buffer[i] > (max * 0.30)) { // 30% of the max 
                result.pixels[i] = color(255);           
            } else {
                result.pixels[i] = color(0);   
            }
        }
        result.updatePixels();
        return result;        
    }
    
    
    public ArrayList<PVector> hough(PImage edgeImg, int nLines, int minVotes, int neighbourhood) 
    {
        float discretizationStepsPhi = 0.06f;
        float discretizationStepsR = 2.5f;
     // dimensions of the accumulator
        int phiDim = (int) (Math.PI / discretizationStepsPhi);
        int rDim = (int) (((edgeImg.width + edgeImg.height) * 2 + 1) / discretizationStepsR);
        
        // our accumulator (with a 1 pix margin around)
        int[] accumulator = new int[(phiDim + 2) * (rDim + 2)];
        
        ArrayList<Integer> bestCandidates = new ArrayList<Integer>();
        ArrayList<PVector> bestLines = new ArrayList<PVector>();
        
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
        
        // Fill the accumulator: on edge points (ie, white pixels of the edge
        // image), store all possible (r, phi) pairs describing lines going
        // through the point.
        for (int y = 0; y < edgeImg.height; y++) {
            for (int x = 0; x < edgeImg.width; x++) {
                // Are we on an edge?
                if (brightness(edgeImg.pixels[y * edgeImg.width + x]) != 0) {
                // ...determine here all the lines (r, phi) passing through
                // pixel (x,y), convert (r,phi) to coordinates in the
                // accumulator, and increment accordingly the accumulator.
                    for(int accPhi=0; accPhi<phiDim; ++accPhi) {
//                        float phi = accPhi*discretizationStepsPhi;
//                        double r = x*Math.cos(phi) + y*Math.sin(phi);
//                        double accR =  r / discretizationStepsR + ((rDim + 2)/2);//+2 car marges dans acc
                        double kindOfR = x*tabCos[accPhi] + y*tabSin[accPhi];
                        double accR =  kindOfR + (rDim + 2)/2;//+2 car marges dans acc
                        accumulator[(int) (accR+1+(accPhi+1)*(rDim+2))] += 1;
                    }
                }
            }
        }
        PImage houghImg = createImage(rDim + 2, phiDim + 2, ALPHA);
        for (int i = 0; i < accumulator.length; i++) {
            houghImg.pixels[i] = color(min(255, accumulator[i]));
        }
        
        //houghImg.resize(800, 800);
       // image(houghImg, 0, 0);
        
        for (int accR = 0; accR < rDim; accR++) {
            for (int accPhi = 0; accPhi < phiDim; accPhi++) {
                // compute current index in the accumulator
                int idx = (accPhi + 1) * (rDim + 2) + accR + 1;
                if (accumulator[idx] > minVotes) {
                    boolean bestCandidate=true;
                    
                    for(int dPhi=-neighbourhood/2; dPhi < neighbourhood/2+1; dPhi++) {
                        // check we are not outside the image
                        if( accPhi+dPhi < 0 || accPhi+dPhi >= phiDim) continue;
                        for(int dR=-neighbourhood/2; dR < neighbourhood/2 +1; dR++) {
                         // check we are not outside the image
                            if(accR+dR < 0 || accR+dR >= rDim) continue;
                            int neighbourIdx = (accPhi + dPhi + 1) * (rDim + 2) + accR + dR + 1;
                            if(accumulator[idx] < accumulator[neighbourIdx]) {
                                // the current idx is not a local maximum!
                                bestCandidate=false;
                                break;
                            }
                        }
                        if(!bestCandidate) break;
                    }
                    if(bestCandidate) {
                        // the current idx *is* a local maximum
                        bestCandidates.add(idx);
                    }
                }
            }
        }
            
        Collections.sort(bestCandidates, new HoughComparator(accumulator));
        for(int i=0; i < Math.min(nLines, bestCandidates.size()); ++i)
        {
            int idx = bestCandidates.get(i);
         // first, compute back the (r, phi) polar coordinates:
            int accPhi = (int) (idx / (rDim + 2)) - 1;
            int accR = idx - (accPhi + 1) * (rDim + 2) - 1;
            float r = (accR - (rDim - 1) * 0.5f) * discretizationStepsR;
            float phi = accPhi * discretizationStepsPhi;
            bestLines.add(new PVector(r, phi));
            
//            // Cartesian equation of a line: y = ax + b
//            // in polar, y = (-cos(phi)/sin(phi))x + (r/sin(phi))
//            // => y = 0 : x = r / cos(phi)
//            // => x = 0 : y = r / sin(phi)
//            // compute the intersection of this line with the 4 borders of
//            // the image
//            int x0 = 0;
//            int y0 = (int) (r / sin(phi));
//            int x1 = (int) (r / cos(phi));
//            int y1 = 0;
//            int x2 = edgeImg.width;
//            int y2 = (int) (-cos(phi) / sin(phi) * x2 + r / sin(phi));
//            int y3 = edgeImg.width;
//            int x3 = (int) (-(y3 - r / sin(phi)) * (sin(phi) / cos(phi)));
//            
//            
//            // Finally, plot the lines
//            stroke(204,102,0);
//            if (y0 > 0) 
//            {
//                if (x1 > 0)
//                    line(x0, y0, x1, y1);
//                else if (y2 > 0)
//                    line(x0, y0, x2, y2);
//                else
//                    line(x0, y0, x3, y3);
//            }
//            else 
//            {
//                if (x1 > 0) 
//                {
//                    if (y2 > 0)
//                        line(x1, y1, x2, y2);
//                    else
//                        line(x1, y1, x3, y3);
//                }
//                else
//                    line(x2, y2, x3, y3);
//            }            
        }
        return bestLines;
    }   
    
    public ArrayList<PVector> getIntersections(List<PVector> lines) {
        ArrayList<PVector> intersections = new ArrayList<PVector>();
        for (int i = 0; i < lines.size() - 1; i++) {
            
            PVector line1 = lines.get(i);
            for (int j = i + 1; j < lines.size(); j++) {
              //vectorx = r, vectory = phi
                PVector line2 = lines.get(j);
            // compute the intersection and add it to 'intersections'
                
                PVector intersection = intersection(line1, line2);
                intersections.add(intersection);
               
                // draw the intersection
                    fill(255, 128, 0);
                    ellipse(intersection.x, intersection.y, 10, 10);
                
            }
        }
        return intersections;
    }
    
    public PVector intersection(PVector line1, PVector line2)
    {
        float d = cos(line2.y)*sin(line1.y) - cos(line1.y)*sin(line2.y);
        float x =  ((line2.x*sin(line1.y) - line1.x*sin(line2.y))/d);
        float y = (line1.x*cos(line2.y) - line2.x*cos(line1.y))/d;
        return new PVector(x, y);
    }

    static public void main(String[] passedArgs)
    {
        String[] appletArgs = new String[] { "cs211.ImageProcessing" };
        if (passedArgs != null) 
        {
            PApplet.main(concat(appletArgs, passedArgs));
        } else 
        {
            PApplet.main(appletArgs);
        }
    }
}

