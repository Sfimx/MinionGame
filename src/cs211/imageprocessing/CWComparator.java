package cs211.imageprocessing;

import java.util.Comparator;
import processing.core.PVector;

public class CWComparator implements Comparator<PVector> {
	PVector center;
	public CWComparator(PVector center) {
		this.center = center;
	}
	@Override
	public int compare(PVector b, PVector d) {
		if(Math.atan2(b.y-center.y,b.x-center.x)<Math.atan2(d.y-center.y,d.x-center.x))
			return -1;
		else return 1;
	}
	
	/*public int compare(PVector x, PVector y) {
	    if (x.y < center.y && y.y < center.y) {
	        if (x.x < y.x) return -1; 
	        else return 1; 
	    } else  if (x.y>center.y && y.y >center.y) {
            if (x.x < y.x) return 1; 
            else return -1; 
	    } else if(x.y<center.y && y.y >center.y){
	        return -1; 
	    }else {
	        return 1;
	    }
	}*/
}