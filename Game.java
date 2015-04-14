import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PShape;
import processing.core.PVector;
import processing.event.MouseEvent;

import java.util.ArrayList;

public class Game extends PApplet {

    float rotateX = 0;
    float rotateY = 0;
    float rotateZ = 0;

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
    Boolean editMode = false;
    boolean editModeAnimation = false;
    boolean leaveEditModeAnimation = false;
    boolean validPosition = false;
    float editModeAnimationAngle = 0;

    float eyeY = 0;
    float eyeZ = 0;

    Mover mover;

    ArrayList<PVector> all_cylinders;
    PVector editorCylinder;

    int DASHBOARD_HEIGHT = 200;
    Dashboard dashboard;


    public void setup() {
        size(WIDTH, HEIGHT, P3D);
        BOX_SIZE = (height / 2 > width) ? width / 2 : height / 2;
        noStroke();
        mover = new Mover(BOX_SIZE);
        cylinder = new Cylinder(CYLINDER_BASE_SIZE, CYLINDER_HEIGHT).getCylinder();
        all_cylinders = new ArrayList<PVector>();
        editorCylinder = new PVector();
        editorCylinder.z = -BOX_HEIGHT/2-CYLINDER_HEIGHT;
        eyeZ = (height/2.0f)/tan(radians(30));

        dashboard = new Dashboard(width, DASHBOARD_HEIGHT);
    }

    public void draw() {
        background(255);

        lights();
        camera(
                0, eyeY, eyeZ,     //eye position, begins at "origin"  "right where our real eyes are"
                0, 0, 0,           //origin of scene
                0, 1, 1
        );

        if(!editMode && !leaveEditModeAnimation && !editModeAnimation) {
            pushMatrix();

            dashboard.draw();
            image(dashboard.context, -width/2, height/2-200);


            popMatrix();
        }

        if (!editMode) {//game mode
            //USER INPUT
            rotateX(rotateX);
            rotateY(rotateY);
            rotateZ(rotateZ);

            mover.update(rotateX, rotateZ);
            mover.checkEdges();
            mover.checkCylinderCollision(all_cylinders, CYLINDER_BASE_SIZE, SPHERE_RADIUS);
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
                println("at the end");
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


    }

    public void mousePressed() {
        if (!editMode) {//rotate plate only if game mode
            originClickX = mouseX;
            originClickY = mouseY;

            originRotateX = rotateX;
            originRotateZ = rotateZ;
        }
    }

    public void mouseDragged() {
        if (!editMode) {
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
            all_cylinders.add(new PVector(editorCylinder.x, editorCylinder.y, editorCylinder.z));
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
        float angle = PI/20;
        switch(keyCode) {
            case SHIFT:
                editModeAnimation = true;
                leaveEditModeAnimation = false;
                noCursor();
                break;
        }
    }

    public void keyReleased() {
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

        final int cylinderResolution = 5;

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

        Dashboard(int dashboardWidth, int dashboardHeight) {
            this.context = createGraphics(dashboardWidth, dashboardHeight, P2D);
        }

        public void draw() {
            context.beginDraw();
            context.noStroke();
            context.background(0);
            context.fill(128);
            context.rect(0, 0, width, 200);
            context.endDraw();
        }
    }

    class Mover {
        PVector location;
        PVector velocity;

        float bound;
        float bounceFactor = 0.8f;

        PVector gravity;
        float gravityConstant;
        float normalForce;
        float mu;
        float frictionMagnitude;

        Mover(float bound) {
            location = new PVector(0, 0, 0);
            velocity = new PVector(0, 0, 0);
            gravity  = new PVector(0, 0, 0);

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

        public void checkEdges() {
            if (location.x >= bound/2) {
                velocity.x *= -bounceFactor;
                location.x = bound/2;
            } else if (location.x <= -bound/2) {
                velocity.x *= -bounceFactor;
                location.x = -bound/2;
            }

            if (location.y >= bound/2) {
                velocity.y *= -bounceFactor;
                location.y = bound/2;
            } else if (location.y <= -bound/2) {
                velocity.y *= -bounceFactor;
                location.y = - bound/2;
            }
        }

        public PVector ballLocation() {
            return location.get(); // make a copy
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
                    checkEdges();
                }
            }
        }
    }

    static public void main(String[] passedArgs) {
        String[] appletArgs = new String[] { "Game" };
        if (passedArgs != null) {
            PApplet.main(concat(appletArgs, passedArgs));
        } else {
            PApplet.main(appletArgs);
        }
    }
}
