class Cylinder {
  
  final int cylinderResolution = 40;
  
  private PShape closedCylinder = new PShape();
   private PShape openCylinder = new PShape();
   private PShape faceA = new PShape();
   private PShape faceB = new PShape(); 
  
  Cylinder(float cylinderBaseSize, float cylinderHeight) {
    float angle;
    float[] x = new float[cylinderResolution + 1];
    float[] y = new float[cylinderResolution + 1];
    
    //get the x and y position on a circle for all the sides
    for(int i = 0; i < x.length; i++) {
      angle = (TWO_PI / cylinderResolution) * i;
      x[i] = sin(angle) * cylinderBaseSize;
      y[i] = cos(angle) * cylinderBaseSize;
    }
    
    closedCylinder = createShape(GROUP);   
    
      openCylinder = createShape();
      //openCylinder.setFill(color(200));
      openCylinder.beginShape(QUAD_STRIP);
      openCylinder.noStroke();
      //draw the border of the cylinder
      for(int i = 0; i < x.length; i++) {
        openCylinder.vertex(x[i], y[i], cylinderHeight);
        openCylinder.vertex(x[i], y[i] , 0);
        
      }  
      openCylinder.endShape();
    
      faceA = createShape();
      //faceA.setFill(color(200));  
      faceA.beginShape(TRIANGLE_FAN);
      faceA.noStroke();
      faceA.vertex(0, 0, 0);
      
      faceB = createShape();  
      //faceB.setFill(color(200));
      faceB.beginShape(TRIANGLE_FAN);
      faceB.noStroke();
      faceB.vertex(0, 0, cylinderHeight); 
      
      for(int i = 0; i < x.length; i++) {
        faceB.vertex(x[i], y[i] , cylinderHeight);
        faceA.vertex(x[i], y[i] , 0);
        
      }        
      faceA.endShape();
      faceB.endShape();
    
    closedCylinder.addChild(faceB);
    closedCylinder.addChild(openCylinder);
    closedCylinder.addChild(faceA);   
  }
  
  PShape getCylinder(){
    return closedCylinder;
  }  
}
