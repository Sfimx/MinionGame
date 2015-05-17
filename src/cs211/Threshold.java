package cs211;


public enum Threshold {
    BINARY,
    BINARYINVERTED;
    
    private float minHue, maxHue, minSat, maxSat, minBrightness, maxBrightness;
    
    boolean validToTake(float brightness, float hue, float saturation)
    {
        boolean valid;
        
        //brightness validity
//        switch(this)
//        {
//            case BINARY:
//                valid = brightness > brightnessThresh;
//                break;
//            case BINARYINVERTED:
//                valid = brightness <= brightnessThresh;
//                break;                           
//        }
        
        //hue validity 115 135
        valid = minBrightness <= brightness && brightness <= maxBrightness &&
                minHue <= hue && hue <= maxHue &&
                minSat <= saturation && saturation <= maxSat;
        
        return valid;
    }
    
    Threshold setThresh(float minBrightness, float maxBrightness, float minSat, float maxSat, float minHue, float maxHue)
    {
        this.minBrightness = minBrightness;
        this.maxBrightness = maxBrightness;
        this.minSat = minSat;
        this.maxSat = maxSat;
        this.minHue = minHue;
        this.maxHue = maxHue;
        return this;
    }
}