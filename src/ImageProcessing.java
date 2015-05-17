import processing.core.PApplet;
import processing.core.PImage;

public class ImageProcessing extends PApplet {
	
	private PImage img;
	private HScrollbar thresholdStartColor; 
	private HScrollbar thresholdEndColor; 
	private HScrollbar thresholdStartBrightness; 
	private HScrollbar thresholdEndBrightness;
	private HScrollbar thresholdStartSaturation; 
	private HScrollbar thresholdEndSaturation;
	
	private PImage im; 
	
	@Override
	public void setup() {
		size(800, 600);
		
		thresholdStartSaturation = new HScrollbar(this, 0, 455, 800, 20);
		thresholdEndSaturation = new HScrollbar(this, 0, 480, 800, 20);
		thresholdStartBrightness = new HScrollbar(this, 0, 505, 800, 20);
		thresholdEndBrightness = new HScrollbar(this, 0, 530, 800, 20);
		thresholdStartColor= new HScrollbar(this, 0, 555, 800, 20);
	    thresholdEndColor = new HScrollbar(this, 0, 580, 800, 20);
		
		//Loads the image
		img = loadImage("board1.jpg");
		//noLoop(); // no interactive behaviour: draw() will be called only once.
	}
	
	/**
	 * Simple transformation of image with fixed threshold of 128
	 * 
	 * @param imgage to be transformed
	 * @return the transformed image
	 */
	public PImage transformation(PImage img) {
		PImage result = createImage(width, height, RGB);
		// create a new, initially transparent, 'result' image
		for(int i = 0; i < img.width * img.height; i++) {
		// do something with the pixel img.pixels[i]
			if(brightness(img.pixels[i]) <= 128)
				result.pixels[i] = color(0, 0,0); 
			else
				result.pixels[i] = color(255,255,255);
		}
		return result; 
	}
	
	/**
	 * Simple inverse transformation with fixed threshold of 128
	 * 
	 * @param img image to be transformed
	 * @return the transformed image
	 */
	public PImage inverseTransformation(PImage img) {
		PImage result = createImage(width, height, RGB);
		// create a new, initially transparent, 'result' image
		
		for(int i = 0; i < img.width * img.height; i++) {
			if(brightness(img.pixels[i]) > 128)
				result.pixels[i] = color(0,0,0); 
			else
				result.pixels[i] = color(255,255,255);
		}
		return result; 
	}
	
	/**
	 * Simple transformation without fixed threshold
	 * 
	 * @param img image to be transformed
	 * @param threshold number in the range [0..1] 
	 * @return the transformed image with the given threshold
	 */
	public PImage transformation(PImage img, float threshold) {
		PImage result = createImage(width, height, RGB);
		// create a new, initially transparent, 'result' image
		for(int i = 0; i < img.width * img.height; i++) {
			if(hue(img.pixels[i]) >= threshold)
				result.pixels[i] = color(0,0,0); 
			else
				result.pixels[i] = color(255,255,255);
		}
		return result; 
	}
	
	
	/**
	 * Simple transformation without fixed threshold
	 * 
	 * @param img image to be transformed
	 * @param hscrollbarOne number in the range [0..1] 
	 * @param hscrollbarTwo number in the range [0..1] 
	 * @return the transformed image with conserved colors that are in the range
	 */
	public PImage transformation(PImage img, float startColor, float endColor, float startBrightness, float endBrightness, float startSaturation, float endSaturation) {
		PImage result = createImage(width, height, RGB);
		// create a new, initially transparent, 'result' image
		for(int i = 0; i < img.width * img.height; i++) {
			if(hue(img.pixels[i]) <= endColor && hue(img.pixels[i]) >= startColor && brightness(img.pixels[i]) <= endBrightness 
					&& brightness(img.pixels[i]) >= startBrightness && saturation(img.pixels[i])<= endSaturation && saturation(img.pixels[i])>= endSaturation)
				result.pixels[i] = color(255,255,255); 
			else 
				result.pixels[i] = color(0,0,0);
		}
		return result; 
	}
		
	public PImage convolute(PImage img) {
		float[][] kernel = {{0, 0, 0 },
							{ 1, 0, -1 },
							{ 0, 0, 0 }};
		float weight = 1.f;
		// create a greyscale image (type: ALPHA) for output
			PImage result = createImage(img.width, img.height, ALPHA);
			// kernel size N = 3
			//
			// for each (x,y) pixel in the image:
			// - multiply intensities for pixels in the range
			// (x - N/2, y - N/2) to (x + N/2, y + N/2) by the
			// corresponding weights in the kernel matrix
			// - sum all these intensities and divide it by the weight
			// - set result.pixels[y * img.width + x] to this value
			
			for(int y = 0; y < img.height; y++){
				for(int x = 0; x < img.width; x++ ){
					float sum = 0;
					for(int i = 0; i < 3; i++){
						int column = x-1+i;
						for(int j = 0; j < 3; j++){
							int row = y-1+j;
							if(column >= 0 && row >= 0 && column < img.width && row < img.height) sum = sum + brightness(img.pixels[row * img.width + column])* kernel[i][j];
						}
					}
					result.pixels[y*img.width + x] = color(sum/weight);
				} 
			}
			return result;
		}
	
	public PImage sobel(PImage img) {
		float[][] hKernel = { { 0, 1, 0 },
							{ 0, 0, 0 },
							{ 0, -1, 0 } };
		float[][] vKernel = { { 0, 0, 0 },
							{ 1, 0, -1 },
							{ 0, 0, 0 } };
		
		PImage result = createImage(img.width, img.height, ALPHA);
		
		// clear the image
		for (int i = 0; i < img.width * img.height; i++) {
			result.pixels[i] = color(0);
		}
		
		float max=0;
		float[] buffer = new float[img.width * img.height];
		
		// *************************************
		// Implement here the double convolution
		// *************************************
		for(int y = 0; y < img.height; y++){
			for(int x = 0; x < img.width; x++ ){
				float sum_h = 0;
				float sum_v = 0; 
				float sum = 0; 
				for(int i = 0; i < 3; i++){
					int column = x-1+i;
					for(int j = 0; j < 3; j++){
						int row = y-1+j;
						if(column >= 0 && row >= 0 && column < img.width && row < img.height) {
							sum_h = sum_h + (img.pixels[row * img.width + column])* hKernel[i][j];
							sum_v = sum_v + (img.pixels[row * img.width + column])* vKernel[i][j];
						}
					}
				}
				sum = sqrt(pow(sum_h,2)+ pow(sum_v, 2)); 
				if (max < sum) max = sum; 
				buffer[y*img.width + x] = sum; 
			} 
		}
		
		for (int y = 2; y < img.height - 2; y++) { // Skip top and bottom edges
			for (int x = 2; x < img.width - 2; x++) { // Skip left and right
				if (buffer[y * img.width + x] > (int)(max*0.3)) { // 30% of the max
					result.pixels[y * img.width + x] = color(255);
				} else {
					result.pixels[y * img.width + x] = color(0);
				}
			}
		}
		return result;
	}
	
	
	public void hough(PImage edgeImg) {
		float discretizationStepsPhi = 0.06f;
		float discretizationStepsR = 2.5f;	
		
		// dimensions of the accumulator
		int phiDim = (int) (Math.PI / discretizationStepsPhi);
		int rDim = (int) (((edgeImg.width + edgeImg.height) * 2 + 1) / discretizationStepsR);
		// our accumulator (with a 1 pix margin around)
		int[] accumulator = new int[(phiDim + 2) * (rDim + 2)];
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
					 for(int accPhi=0; accPhi<phiDim; ++accPhi)
	                 {
	                        float phi = accPhi*discretizationStepsPhi;
	                        double r = x*Math.cos(phi) + y*Math.sin(phi);
	                        double accR =  r / discretizationStepsR + ((rDim + 2)/2);//+2 car marges dans acc
	                        accumulator[(int) (accR+1+(accPhi+1)*(rDim+2))] += 1;
	                 }
				}
			}
		}
		
		PImage houghImg = createImage(rDim + 2, phiDim + 2, ALPHA);
		for (int i = 0; i < accumulator.length; i++) {
			houghImg.pixels[i] = color(min(255, accumulator[i]));
		}
		
		houghImg.updatePixels();
		
		for (int idx = 0; idx < accumulator.length; idx++) {
			if (accumulator[idx] > 200) {
				// first, compute back the (r, phi) polar coordinates:
				int accPhi = (int) (idx / (rDim + 2)) - 1;
				int accR = idx - (accPhi + 1) * (rDim + 2) - 1;
				float r = (accR - (rDim - 1) * 0.5f) * discretizationStepsR;
				float phi = accPhi * discretizationStepsPhi;
				
				// Cartesian equation of a line: y = ax + b
				// in polar, y = (-cos(phi)/sin(phi))x + (r/sin(phi))
				// => y = 0 : x = r / cos(phi)
				// => x = 0 : y = r / sin(phi)
				// compute the intersection of this line with the 4 borders of
				// the image
				
				int x0 = 0;
				int y0 = (int) (r / sin(phi));
				int x1 = (int) (r / cos(phi));
				int y1 = 0;
				int x2 = edgeImg.width;
				int y2 = (int) (-cos(phi) / sin(phi) * x2 + r / sin(phi));
				int y3 = edgeImg.width;
				int x3 = (int) (-(y3 - r / sin(phi)) * (sin(phi) / cos(phi)));
				// Finally, plot the lines
				stroke(204,102,0);
				if (y0 > 0) {
					if (x1 > 0)
						line(x0, y0, x1, y1);
					else if (y2 > 0)
						line(x0, y0, x2, y2);
					else
						line(x0, y0, x3, y3);
				}
				else {
					if (x1 > 0) {
						if (y2 > 0)
							line(x1, y1, x2, y2);
						else
							line(x1, y1, x3, y3);
					}
					else
						line(x2, y2, x3, y3);
					}
				}
			}
	}

	@Override
	public void draw() {
		im = sobel(transformation(img, thresholdStartColor.getPos()*255, thresholdEndColor.getPos()*255, thresholdStartBrightness.getPos()*255, thresholdEndBrightness.getPos()*255, thresholdStartSaturation.getPos()*255, thresholdEndSaturation.getPos()*255));
		hough(im);
		image(im, 0, 0);	
		thresholdStartColor.display();
		thresholdStartColor.update();
		thresholdEndColor.display();
		thresholdEndColor.update();
		thresholdStartBrightness.display(); 
		thresholdStartBrightness.update(); 
		thresholdEndBrightness.display(); 
		thresholdEndBrightness.update(); 
		thresholdStartSaturation.display();
		thresholdStartSaturation.update();
		thresholdEndSaturation.display();
		thresholdEndSaturation.update();
		
	}
	
	 static public void main(String[] passedArgs) {
	        String[] appletArgs = new String[] { "ImageProcessing" };
	        if (passedArgs != null) {
	            PApplet.main(concat(appletArgs, passedArgs));
	        } else {
	            PApplet.main(appletArgs);
	        }
	}
}