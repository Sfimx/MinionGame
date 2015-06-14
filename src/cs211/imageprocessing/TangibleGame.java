package cs211.imageprocessing;

import cs211.imageprocessing.*;
import processing.core.*;
import processing.event.MouseEvent;
import processing.video.Movie;

import java.util.*;

public class TangibleGame extends PApplet {

    float rotateX = 0;
    float rotateY = 0;
    float rotateZ = 0;
    
    //When moving the scrollbar we need to keep the plate in the old position 
    float oldRotateX;
    float oldRotateZ;
    float oldRotateY; 

    float originClickX = 0;
    float originClickY = 0;
    float originRotateX = 0;
    float originRotateZ = 0;
    float rotateSpeed = 4;

    int WIDTH = 900;
    int HEIGHT = 700;

    float BOX_SIZE;
    float BOX_HEIGHT = 15;
    float SPHERE_RADIUS = 15;
    float CYLINDER_HEIGHT = 50;
    float CYLINDER_BASE_SIZE = 25;

    float MAX_ANGLE = PI/3;
    PShape cylinder;
    PShape minion;
    Boolean editMode = false;
    boolean editModeAnimation = false;
    boolean leaveEditModeAnimation = false;
    boolean validPosition = false;
    boolean onceKeypressed = true;

    float editModeAnimationAngle = 0;

    float eyeY = 0;
    float eyeZ = 0;

    float cylinderRotation = 0f;

    Mover mover;

    ArrayList<PVector> all_cylinders;
    HashMap<PVector, Float> cylindersRotation;
    PVector editorCylinder;

    int DASHBOARD_HEIGHT = 200;
   
    Dashboard dashboard;
    TopView topView; 
    Scoreboard scoreboard; 
    BarChart barChart;
    TranslatedHScrollbar scroll;

    ImageProcessing pouet;
    TwoDThreeD twoDThreeD;
    
    //List with all the PGraphics that should be on the dashboard
    ArrayList<Element> elements;
    boolean newFrame = true;


    //Capture cam;
    Movie cam;
    boolean displayDashboard;
    boolean videoControl;
    PImage img; 
    
    public void setup() {
        size(WIDTH, HEIGHT, P3D);
        BOX_SIZE = (height / 2 > width) ? width / 2 : height / 2;
        noStroke();
        mover = new Mover(BOX_SIZE);
        //cylinder = new Cylinder(CYLINDER_BASE_SIZE, CYLINDER_HEIGHT).getCylinder();
        cylinder = loadShape("minion.obj");
        cylinder.rotateX(-PI / 2);
        cylinder.rotateY(PI);
        cylinder.scale(1.6f);
        cylinder.translate(0, -29, 0);

        all_cylinders = new ArrayList<PVector>();
        cylindersRotation = new HashMap<>();
        editorCylinder = new PVector();
        editorCylinder.z = -BOX_HEIGHT/2-CYLINDER_HEIGHT;
        eyeZ = (height/2.0f)/tan(radians(30));
        elements = new ArrayList<Element>();
        dashboard = new Dashboard(width, DASHBOARD_HEIGHT, elements);
        topView = new TopView(Math.round(BOX_SIZE/2), mover);
        elements.add(topView);
        scoreboard = new Scoreboard(Math.round(BOX_SIZE/3), Math.round(BOX_SIZE/2), mover);
        elements.add(scoreboard);
        scroll = new TranslatedHScrollbar(this, -115F, HEIGHT/2.0F - 30.0F, 2.4F*WIDTH/4.0F, 20.F, WIDTH/2.0F, HEIGHT/2.0F);
        barChart = new BarChart(width - Math.round(BOX_SIZE), Math.round(BOX_SIZE/2), scoreboard, scroll);
        elements.add(barChart);
        
        img = loadImage("background.jpg");
        
        displayDashboard = false; 
        videoControl = true; 
        
        pouet = new ImageProcessing();
        pouet.g = this.g;
        
        cam = new Movie(this, "C:\\Users\\Sfimx\\Documents\\testvideo.mp4");
        cam.loop();

        twoDThreeD = new TwoDThreeD(cam.width, cam.height);
        
    }

    public void movieEvent(Movie m) {
        if(m == null)
            return;


        if(m.available()) {
            try {
                m.read();
            }
            catch(NullPointerException e) {
                /*
                 * We followed example from processing docs
                 * for movieEvent method, but we still get
                 * NullPointerException from gstreamer even
                 * if the video plays, so we silent them here
                 * after multiple checks (null checking and
                 * availability checking)
                 */
            }
        }

        if(m == cam) {
            newFrame = true;
        }
    }

    public void draw() {
    	
        background(255);
        lights();

        camera(
                0, eyeY - 150, eyeZ + 100,     //eye position, begins at "origin"  "right where our real eyes are"
                0, 0, 0,           //origin of scene
                0, 1, 1
        );

        fill(220, 220, 250);
        if(!editMode && !leaveEditModeAnimation && !editModeAnimation) {
            pushMatrix();
            
            if(!videoControl){
                camera(
                         0, eyeY, eyeZ+600,
                         0, 0, 0,
                         0, 1, 1
                 );
                image(img, -width/2 - 500, -height/2 - 550);
            }
            
            camera(
                    0, eyeY, eyeZ,     //eye position, begins at "origin"  "right where our real eyes are"
                    0, 0, 0,           //origin of scene
                    0, 1, 1
            );  
            
           
            if(videoControl)
            	image(cam, -width/2, -height/2, cam.width/2.5f, cam.height/2.5f);
            
            if(displayDashboard) {
            	dashboard.draw();
            	image(dashboard.context, -width/2, height/2-185, width, 185);
            	scroll.update();
            	scroll.display();
            }
            
            //Buttons to off/on the dashboard
            fill(128);
            int padding = 50;
            rect(width/2 - 100 - padding, -height/2 + 5, 95 + padding, 50) ;
            fill(255);
            text("Turn dashboard", width/2 - 75 - padding, -height/2 + 30 );
            if(!displayDashboard){
                text("on", width/2 - 85, -height/2 + 45 );
            } else {
            	text("off", width/2 - 85, -height/2 + 45 );
            }
            
            //Buttond to off/on the video control
            fill(128);
            rect(width / 2 - 100 - padding, -height / 2 + 60, 95 + padding, 50) ;
            fill(255);
            text("Turn control by movie", width / 2 - 90 - padding, -height / 2 + 85 );
            if(!videoControl){
                text("on", width/2 - 85, -height/2 + 100 );
            } else {
            	text("off", width/2 - 85, -height/2 + 100 );
            }            
            
            camera(
                    0, eyeY - 150, eyeZ - 100,     //eye position, begins at "origin"  "right where our real eyes are"
                    0, 0, 0,           //origin of scene
                    0, 1, 1
            );
          
            popMatrix();
        }

        if (!editMode && !scroll.locked && !scroll.mouseOver) {//game mode
            //USER INPUT
        	oldRotateX = rotateX; 
        	oldRotateY = rotateY; 
        	oldRotateZ = rotateZ; 
        	
        	rotateX(oldRotateX);
            rotateY(oldRotateY);
            rotateZ(oldRotateZ);
            mover.update(rotateX,rotateZ);
            mover.checkEdges();
	        mover.checkCylinderCollision(all_cylinders, CYLINDER_BASE_SIZE, SPHERE_RADIUS);
        }
        //We need to keep the old position while using the scrollbar
        if (scroll.locked || scroll.mouseOver) {
            rotateX(oldRotateX);
            rotateY(oldRotateY);
            rotateZ(oldRotateZ);        	
        }


        //animation when entering edit mode
        if (editModeAnimation || leaveEditModeAnimation) {
            if ((editModeAnimation && editModeAnimationAngle <1) || (leaveEditModeAnimation && editModeAnimationAngle > 0)) {
                if (editModeAnimation) {
                    editModeAnimationAngle += 0.05f;
                } else {
                    editModeAnimationAngle -= 0.05f;
                }
                if (editModeAnimationAngle >= 1) editModeAnimationAngle = 1;
                if (editModeAnimationAngle <= 0) editModeAnimationAngle = 0;
                eyeZ = ( (height/2.0f) / tan(PI*30.0f / 180.0f) ) * (1-editModeAnimationAngle);
                eyeY =  -editModeAnimationAngle * (height/2 / tan(radians(30)) + BOX_HEIGHT/2);
            } else {
                cam.loop();
                editMode = editModeAnimation;
                editModeAnimation = false;
                leaveEditModeAnimation = false;
            }
        }

        fill(220, 220, 250);

        box(BOX_SIZE, BOX_HEIGHT, BOX_SIZE);

        mover.display(SPHERE_RADIUS);


        noFill();
        drawObstacles();

        if(videoControl){
	        //PVector rotation = pouet.getRotation(false, cam, twoDThreeD);
	        if(!editMode && !editModeAnimation && !leaveEditModeAnimation && newFrame) {
	            newFrame = false;
	            PVector rotation = pouet.getRotation(false, cam, twoDThreeD);
	            // println(rotation);
	            if (rotation != null) {
	            	rotateX = map(rotation.x, -PI, PI, -MAX_ANGLE, MAX_ANGLE);
	            	//  rotateY = rotation.z;
	            	rotateZ = map(-rotation.y, -PI, PI, -MAX_ANGLE, MAX_ANGLE);
	                oldRotateX = rotateX; 
	                oldRotateZ = rotateZ;
	            }
	        }
        }
    }

    public void mousePressed() {
        if (!editMode && !scroll.locked && !scroll.mouseOver) {//rotate plate only if game mode
            originClickX = mouseX;
            originClickY = mouseY;

            originRotateX = rotateX;
            originRotateZ = rotateZ;
        }
    }

    public void mouseDragged() {
        if (!editMode && !scroll.locked && !scroll.mouseOver) {
            rotateX = max(min(originRotateX+MAX_ANGLE*rotateSpeed*((originClickY - pmouseY)/displayWidth), MAX_ANGLE), -MAX_ANGLE);
            rotateZ = max(min(originRotateZ-MAX_ANGLE*rotateSpeed*((originClickX - pmouseX)/displayHeight), MAX_ANGLE), -MAX_ANGLE);
        }
    }

    public void mouseWheel(MouseEvent e) {
        if (!editMode) {
            //rotate speed vary between [0;8], 0 impossible to tilt the plane
            rotateSpeed = max(min(rotateSpeed+e.getCount()*0.1f, 8), 0);
        }
    }

    public void mouseClicked() {
        if (editMode && validPosition) {
            PVector key = new PVector(editorCylinder.x, editorCylinder.y, editorCylinder.z);
            all_cylinders.add(key);
            cylindersRotation.put(key, cylinderRotation);
            cylinderRotation = random(0, 2*PI);
        }
      	if (mouseX > width - 100 - 50 && mouseX < width -5
        		&& mouseY > 5 && mouseY < 55){
        		displayDashboard = !displayDashboard;
        }
      	
      	if (mouseX > width - 100 - 50 && mouseX < width -5
        		&& mouseY > 60 && mouseY < 110){
        		videoControl = !videoControl;
        }
      	
      	
    }

    public void mouseMoved() {
        editorCylinder.x = mouseX-width/2;
        editorCylinder.y = -(mouseY-height/2);
    }

    public void drawObstacles() {
        for (PVector obstacleCenter : all_cylinders) {
            cylinder.setFill(color(200));
            drawCylinderAt(obstacleCenter);
        }
        if (editMode) {//"draw a cylinder on the mouse"
            int cylinderColor;
            PVector cursorPosition = new PVector(editorCylinder.x, editorCylinder.y, 0);
            float distance = mover.ballLocation().dist(cursorPosition);

            boolean onPlate = onPlate(editorCylinder.x, editorCylinder.y);
            boolean enoughDistance = distance >= SPHERE_RADIUS + CYLINDER_BASE_SIZE;
            validPosition = onPlate & enoughDistance;

            if(validPosition)
                cylinderColor = color(0, 255, 0, 100); //green if ok
            else
                cylinderColor = color(255, 0, 0, 100); //red if ko

            cylinder.setFill(cylinderColor);
            drawCylinderAt(editorCylinder);
        }
    }

    public void drawCylinderAt(PVector position) {
        pushMatrix();
        rotateX(-PI/2);

        translate(position.x, position.y, position.z);

        if(cylindersRotation.containsKey(position))
            rotateZ(cylindersRotation.get(position));
        else
            rotateZ(cylinderRotation);

        shape(cylinder);

        //drawAxis();
        popMatrix();
    }

    public void drawInfo() {
        textSize(15);
        fill(0, 102, 153, 204);
        text("rotateSpeed = "+rotateSpeed+" in [0;8]", -width/2+15, -height/2+15, 0);
        noFill();
    }

    public void drawAxis() {
        int size = 250;
        stroke(255, 0, 0);
        line(0, 0, 0, size, 0, 0);
        stroke(0, 255, 0);
        line(0, 0, 0, 0, size, 0);
        stroke(0, 0, 255);
        line(0, 0, 0, 0, 0, size);
        noStroke();
    }

    public boolean onPlate(float x, float y) {
        return x>=-BOX_SIZE/2+CYLINDER_BASE_SIZE &&
                x<=BOX_SIZE/2-CYLINDER_BASE_SIZE &&
                y>=-BOX_SIZE/2+CYLINDER_BASE_SIZE &&
                y<=BOX_SIZE/2-CYLINDER_BASE_SIZE;
    }

    public void keyPressed() {
      //  float angle = PI/20;
        switch(keyCode) {
            case SHIFT:
                cam.pause();
                editModeAnimation = true;
                leaveEditModeAnimation = false;

                if(onceKeypressed)
                    cylinderRotation = random(0, PI);
                noCursor();
                break;
        }

        onceKeypressed = false;
    }

    public void keyReleased() {
        onceKeypressed = true;
        switch(keyCode) {
            case SHIFT:
                editModeAnimation = false;
                leaveEditModeAnimation = true;
                cursor(ARROW);
                editMode = false;

                break;
        }
    }

    class Cylinder {

        final int cylinderResolution = 30;

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

        public PShape getCylinder(){
            return closedCylinder;
        }
    }

    class Dashboard {
        public PGraphics context;
        private int distance; 
        private int xPosition; 
        private int yPosition; 
        ArrayList<Element> elements;

        Dashboard(int dashboardWidth, int dashboardHeight, ArrayList<Element> elements) {
            this.context = createGraphics(dashboardWidth, dashboardHeight, P2D);
            this.elements = elements;
            distance = Math.round((context.height - (BOX_SIZE/2))/2);
            xPosition = distance ; 
            yPosition = distance; 
        }

        public void draw() {
        	for (Element e : elements) {	
            	e.draw();
            }
            context.beginDraw();
            context.noStroke();
            context.background(0);
            context.fill(128);
            context.rect(0, 0, width, 200);
            for (Element e : elements) {	
	            context.image(e.context(), xPosition , yPosition);
	            xPosition = xPosition + distance + e.context().width;
            }
            xPosition = distance; 
            context.endDraw(); 
        }
    }

    class Mover {
        PVector location;
        PVector velocity;

        float bound;
        float bounceFactor = 0.9f;

        PVector gravity;
        float gravityConstant;
        float normalForce;
        float mu;
        float frictionMagnitude;

        Mover(float bound) {
            location = new PVector(0f, 0f, 0f);
            velocity = new PVector(0f, 0f, 0f);
            gravity  = new PVector(0f, 0f, 0f);

            this.bound = bound;

            gravityConstant = 3;
            normalForce = 1;
            mu = 0.5f;
            frictionMagnitude = normalForce * mu;
        }

        public void update(float rotateX, float rotateZ) {

            gravity.x = sin(rotateZ) * gravityConstant;
            gravity.y = sin(rotateX) * gravityConstant;

            PVector friction = velocity.get();
            friction.normalize();
            friction.mult(-frictionMagnitude);


            velocity.add(gravity);
            velocity.add(friction);
            location.add(velocity);
        }

        public void display(float radius) {
            pushMatrix();
            rotateX(-PI/2);
            translate(location.x, location.y, -radius -BOX_HEIGHT/2);
            sphere(radius);
            popMatrix();
        }
        
        public PVector ballLocation() {
            return location.get(); // make a copy
        }

        public void checkEdges() {
            if (location.x >= bound/2) {
                velocity.x *= -bounceFactor;
                location.x = bound/2;
                
                //Update the points on the scoreboard
                scoreboard.losePoints();
            } else if (location.x <= -bound/2) {
                velocity.x *= -bounceFactor;
                location.x = -bound/2;
                
                //Update the points on the scoreboard
                scoreboard.losePoints();
            }

            if (location.y >= bound/2) {
                velocity.y *= -bounceFactor;
                location.y = bound/2;
                //Update the points on the scoreboard
                scoreboard.losePoints();
            } else if (location.y <= -bound/2) {
                velocity.y *= -bounceFactor;
                location.y = - bound/2;
                //Update the points on the scoreboard
                scoreboard.losePoints();
            }
        }

        public void checkCylinderCollision(ArrayList<PVector> cylinderCenters, float cylinderRadius, float sphereRadius) {
            for (PVector center : cylinderCenters) {
                PVector newCenter = new PVector(center.x, center.y, 0);      //"place center on the same plan/coord systeme than the ball is on"
                if (newCenter.dist(location)<=cylinderRadius+sphereRadius)
                {
                    PVector normal = PVector.sub(location, newCenter);
                    normal.normalize();

                    float v1_dot_normal = velocity.dot(normal);
                    velocity.sub(PVector.mult(normal, 2*v1_dot_normal));
                    velocity.mult(bounceFactor);
                    location = PVector.add(newCenter, PVector.mult(normal, cylinderRadius+sphereRadius));//prevent the ball from being trapped in a cylinder + the cylinder from being traversed by ball
                    //Update the scoreboard
                    scoreboard.gainPoints();
                    checkEdges();
                }
            }
        }
    }
    
    /**
     * Elements that should be added on the dashboard
     *
     */
    abstract class Element {
    	/**
         * @return the PGraphics context of the element
      	 */
    	public abstract PGraphics context(); 
    	public abstract void draw();
    }
    
    
    class TopView extends Element{

  	  private PVector location;
  	  private Mover mover; 
  	  private PGraphics context;
  	  private int scale; 
  	  public int size; 
  	  
  	  public TopView(int size, Mover mover) {
  		   this.size = size; 
  		   this.mover = mover;
  		   this.scale = Math.round(BOX_SIZE/size);
  		   location = new PVector();
  		   context  = createGraphics(size , size, P2D);
  	  }
  	 
  	  private PVector translateCoordonates(PVector position) {
  		 PVector translatedPosition = new PVector(); 
  		 translatedPosition.x = position.x/scale+ size/2; 
  		 translatedPosition.y = -position.y/scale + size/2; 
  		 return translatedPosition; 
  	  }

  	  private void update() { 
  	    location = translateCoordonates(mover.ballLocation()); 
  	  } 
  	  
  	  private void drawCylinders() { 
  		  for (PVector obstacleCenter : all_cylinders) {  
  			  PVector translatedPosition = translateCoordonates(obstacleCenter); 
  			  context.fill(color(255, 0, 0)); 
  			  context.ellipse(translatedPosition.x, translatedPosition.y, 2*CYLINDER_BASE_SIZE/scale, 2*CYLINDER_BASE_SIZE/scale);
  		  }
  	  }
  	  
  	 public PGraphics context() {
  		 return context; 
  	 }
  	 
  	  public void draw() { 
  		float rayon = SPHERE_RADIUS/scale; 
  	    context.beginDraw();
  	    context.background(0); 
  	    context.noStroke();
  	    update();
  	    drawCylinders();
  	    context.fill(255); 
  	    context.ellipse(location.x, location.y, 2*rayon, 2*rayon);  
  	    context.endDraw(); 
  	 }	  
  }
    
    class Scoreboard extends Element{
    	
   	 private PGraphics context;
   	 private Mover mover; 
   	 private float totalScore = 0; 
   	 private PVector oldLocation;
   	 private float lastScore = 0; 
   	 private int width;
   	 private int height;

        private float lastScoreDisplay;
        private float velocityDispplay;
        private float totalScoreDisplay;


     final private static int COUNTER_FRAME = 5;
     private int counter;
        private PVector velocity;
   	 
   	 public Scoreboard(int width, int height, Mover mover) {
   		 this.width = width;
   		 this.height = height;
   		 this.mover= mover; 
   		 context = createGraphics(width, height, P2D);
   		 this.oldLocation = mover.ballLocation();
         this.counter = 0;
         velocity = mover.velocity;
   	 }
   	 
   	 public void gainPoints() {
   		 if(!oldLocation.equals(mover.location)){
   			 totalScore = totalScore + mover.velocity.magSq();  
   			 lastScore = mover.velocity.magSq();
   			 oldLocation = mover.ballLocation();
   		 }
   	}
   	 
   	 public void losePoints() {
   		 if(!oldLocation.equals(mover.location)){
   			 totalScore = totalScore - mover.velocity.magSq(); 
   			 lastScore = - mover.velocity.magSq();
   			 oldLocation = mover.ballLocation();
   		 }
   	 }
   	 
   	public float totalScore() {
   		return totalScore;
   	}
   	
   	public PGraphics context() {
 		 return context; 
 	 }
   	 
   	 public void draw() {
         // context.clear();
         if(counter == 0) {
             lastScoreDisplay = round(lastScore*10)/10.0f;
             velocityDispplay = round(velocity.magSq()*10)/10.0f;
             totalScoreDisplay = round(totalScore*10)/10.0f;
         }
         counter = (counter + 1) % COUNTER_FRAME;


         context.beginDraw();


    	    
    	    String total = "Total score:";
    	    String vel= "Velocity: "; 
    	    String last = "Last score: ";
    	    context.fill(color(255, 255, 0));
    	    context.rect(0, 0, width, height);
    	    context.fill(50);
    	    context.textSize(17);
    	   //show score
    	    context.text(total, 10, 20);
    	    context.text("" + totalScoreDisplay, 10, 40);
    	    context.text(vel, 10, 80);
    	    context.text("" + velocityDispplay, 10, 100);
    	    context.text(last, 10, 140);
    	    context.text("" + lastScoreDisplay, 10, 160);
    	    context.endDraw();
   	 }
   }
    
    class BarChart extends Element {
    	private PGraphics context; 
    	private int width; 
    	private int height; 
    	private Scoreboard scoreboard; 
    	private final int size = 10; 
    	private final ArrayList<Integer> numberOfSquares = new ArrayList<Integer>(); 
    	private int counter = 0;
    	private TranslatedHScrollbar scroll;
    	
    	public BarChart(int width, int height, Scoreboard scoreboard, TranslatedHScrollbar scroll) {
    		this.width = width; 
    		this.height = height; 
    		this.scoreboard = scoreboard; 
    		this.scroll = scroll; 
    		context = createGraphics(this.width, this.height, P2D);
    	}
    	
    	private void column(int index){
    		float x = index*size*scroll.getPos(); 
    		float y = height; 
    		context.fill(color(255,0,0));
    		
    		for(int i = 0; i < numberOfSquares.get(index); i++) {
    			context.rect(x, y, size*scroll.getPos(), size*scroll.getPos());
    			y = y - size*scroll.getPos(); 
    		}
    	}
    	
		@Override
		public PGraphics context() {
			return context; 
		}

		@Override
		public void draw() {
			
			
			if (counter >= 30) {
				int toInsert; 
				if (scoreboard.totalScore() < 0) toInsert = 0;
				else toInsert = Math.round(scoreboard.totalScore()/40);
				numberOfSquares.add(toInsert); 
				counter = 0; 
			}
			
			counter++;			
			context.beginDraw(); 
			context.fill(100);
			context.rect(0, 0, context.width, context.height);
			for(int i = 0; i < numberOfSquares.size(); i++){
				column(i);
			}
    		
			context.endDraw();
		}
    	
    }

    static public void main(String[] passedArgs) {
        String[] appletArgs = new String[] { "cs211.imageprocessing.TangibleGame" };
        if (passedArgs != null) {
            PApplet.main(concat(appletArgs, passedArgs));
        } else {
            PApplet.main(appletArgs);
        }
    }
}