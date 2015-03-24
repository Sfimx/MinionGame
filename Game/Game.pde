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


void setup() {
  size(WIDTH, HEIGHT, P3D);
  BOX_SIZE = (height / 2 > width) ? width / 2 : height / 2;
  noStroke();
  mover = new Mover(BOX_SIZE); 
  cylinder = new Cylinder(CYLINDER_BASE_SIZE, CYLINDER_HEIGHT).getCylinder();
  all_cylinders = new ArrayList<PVector>();
  editorCylinder = new PVector();
  editorCylinder.z = -BOX_HEIGHT/2-CYLINDER_HEIGHT;
  eyeZ = (height/2.0)/tan(radians(30));
}

void draw() {
  background(255);

  lights();
  camera(
    0, eyeY, eyeZ,     //eye position, begins at "origin"  "right where our real eyes are"
    0, 0, 0,           //origin of scene
    0, 1, 1 
  );

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
    if ((editModeAnimation && editModeAnimationAngle <=1) || (leaveEditModeAnimation && editModeAnimationAngle > 0)) {
      if (editModeAnimation) {
        editModeAnimationAngle += 0.05;
      } else {
        editModeAnimationAngle -= 0.05;
      }
      if (editModeAnimationAngle >= 1) editModeAnimationAngle = 1;
      if (editModeAnimationAngle <= 0) editModeAnimationAngle = 0;
      eyeZ = ( (height/2.0) / tan(PI*30.0 / 180.0) ) * (1-editModeAnimationAngle);
      eyeY =  -editModeAnimationAngle * (height/2 / tan(radians(30)) + BOX_HEIGHT/2);
    } else {
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

void mousePressed() {
  if (!editMode) {//rotate plate only if game mode
    originClickX = mouseX;
    originClickY = mouseY;

    originRotateX = rotateX;
    originRotateZ = rotateZ;
  }
}

void mouseDragged() { 
  if (!editMode) {  
    rotateX = max(min(originRotateX+MAX_ANGLE*rotateSpeed*((originClickY - pmouseY)/displayWidth), MAX_ANGLE), -MAX_ANGLE);
    rotateZ = max(min(originRotateZ-MAX_ANGLE*rotateSpeed*((originClickX - pmouseX)/displayHeight), MAX_ANGLE), -MAX_ANGLE);
  }
}

void mouseWheel(MouseEvent e) {
  if (!editMode) {
    //rotate speed vary between [0;8], 0 impossible to tilt the plane
    rotateSpeed = max(min(rotateSpeed+e.getCount()*0.1, 8), 0); 
  }
}

void mouseClicked() {
  if (editMode && validPosition) {
    all_cylinders.add(new PVector(editorCylinder.x, editorCylinder.y, editorCylinder.z));
  }
}

void mouseMoved() {  
  editorCylinder.x = mouseX-width/2;
  editorCylinder.y = -(mouseY-height/2);
}

void drawObstacles() {
  for (PVector obstacleCenter : all_cylinders) {
    cylinder.setFill(color(200));
    drawCylinderAt(obstacleCenter);
  }
  if (editMode) {//"draw a cylinder on the mouse"
    color cylinderColor;
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

void drawCylinderAt(PVector position) {
  pushMatrix();
  rotateX(-PI/2);
  translate(position.x, position.y, position.z);    
  shape(cylinder);
  //drawAxis();
  popMatrix();
}

void drawInfo() {
  textSize(15);
  fill(0, 102, 153, 204);
  text("rotateSpeed = "+rotateSpeed+" in [0;8]", -width/2+15, -height/2+15, 0);
  noFill();
}

void drawAxis() {
  int size = 250;
  stroke(255, 0, 0);
  line(0, 0, 0, size, 0, 0);
  stroke(0, 255, 0);
  line(0, 0, 0, 0, size, 0);
  stroke(0, 0, 255);
  line(0, 0, 0, 0, 0, size);
  noStroke();
}

boolean onPlate(float x, float y) {
  return x>=-BOX_SIZE/2+CYLINDER_BASE_SIZE && 
         x<=BOX_SIZE/2-CYLINDER_BASE_SIZE && 
         y>=-BOX_SIZE/2+CYLINDER_BASE_SIZE &&
         y<=BOX_SIZE/2-CYLINDER_BASE_SIZE;
}

void keyPressed() {
  float angle = PI/20;
  switch(keyCode) {
  case SHIFT:
    editMode = true;          //enter edit mode
    editModeAnimation = true;
    leaveEditModeAnimation = false;
    noCursor();
    break;
  }
}

void keyReleased() {
  switch(keyCode) {
  case SHIFT:
    editMode = false; //quit edit mode
    editModeAnimation = false;
    leaveEditModeAnimation = true;
    cursor(ARROW);
    break;
  }
}
