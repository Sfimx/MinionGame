Mover mover;
float angle = 0; 
float rotateX = 0;
float rotateY = 0;
float rotateZ = 0;
float gravityConstant = 0.3;

float originClickX = 0;
float originClickY = 0;
float originRotateX = 0;
float originRotateZ = 0;

float BOX_SIZE = 400;
float BOX_HEIGHT = 20;
float speed = 1.0; 

float MAX_ANGLE = PI / 3;

void setup() {
  size(1000, 1000, P3D);
  mover = new Mover();
  noStroke();
}

void draw() {
  if (editMode == true){
    camera (500, -100, 0 , width/2.0, height/2.0, 0, 0, 1, 1);
  } else 
    camera(width/2,width/2 , (height/2.0) / tan(PI*30.0 / 180.0) , width/2.0, height/2.0, 0, 0, 1, 1);
 
  directionalLight(50, 100, 125, 0, -1, 0);
  ambientLight(102, 102, 102);
  
  background(200); 
  
  translate(width/2, height/2); 
  rotateY(angle);  
  rotateZ(rotateZ); 
  rotateX(rotateX);
  
  box(400, 20, 400);

  rotateX(-PI/2);
  //translate(-BOX_SIZE/2,-BOX_SIZE/2,-25 - BOX_HEIGHT / 2);
  translate(0, 0, -25 - BOX_HEIGHT / 2);
  
  mover.checkEdges();
  mover.update();
  mover.display();
}

void keyPressed() {
  if (key == CODED) {
    if (keyCode == LEFT) {
      angle-=radians(1); 
    }
    else if (keyCode == RIGHT) {
      angle+=radians(1);
    }
  }
  switch(keyCode) {
  case SHIFT:
    editMode= true;
    break;
  }
}

void keyReleased() {
  switch(keyCode) {
  case SHIFT:
    editMode = false;
    break;
  }
}


void mousePressed() {
  originClickX = mouseX;
  originClickY = mouseY;
  
  originRotateX = rotateX;
  originRotateZ = rotateZ;
}

void mouseDragged() {
 
   rotateX = max(min(originRotateX + speed * MAX_ANGLE * ((originClickY - mouseY) / (0.25*displayWidth)), MAX_ANGLE), -MAX_ANGLE);
   rotateZ = max(min(originRotateZ - speed * MAX_ANGLE * ((originClickX - mouseX) / (0.25*displayHeight)), MAX_ANGLE), -MAX_ANGLE);
   
   println("rotateX: " + degrees(rotateX) + "; rotateY: " + degrees(rotateY));
}

boolean editMode = false; 
  

void mouseWheel(MouseEvent event) {
  float e = event.getCount();
  speed += 0.01*e;
  println(speed); 
}

class Mover {
  PVector location;
  PVector velocity;
  PVector gravityForce; 
  
Mover() {
  gravityForce= new PVector();
  location = new PVector(0,0,0);
  velocity = new PVector(0, 0,0);
}
void update() {
  gravityForce.x =sin(rotateZ) * gravityConstant;
  gravityForce.y = sin(rotateX) * gravityConstant;
  gravityForce.z = sin(rotateX) * gravityConstant;

  float normalForce = 1;
  float mu = 0.01;
  float frictionMagnitude = normalForce * mu;
  PVector friction = velocity.get();
  friction.mult(-1);
  friction.normalize();
  friction.mult(frictionMagnitude);

  velocity.add(gravityForce);
  velocity.add(friction);
  location.add(velocity);
}

void display() {
  
  pushMatrix();
  translate(location.x, location.y);
  sphere(25);
  popMatrix();
}

void checkEdges() {
  if (location.x >= BOX_SIZE/2) {
    velocity.x = -velocity.x;
    location.x = BOX_SIZE/2;
  }else if (location.x <= -BOX_SIZE/2) {
    velocity.x = -velocity.x;
    location.x = -BOX_SIZE/2;
  } 
  
 if (location.y >= BOX_SIZE/2) {
    velocity.y = -velocity.y;
    location.y = BOX_SIZE/2;
  }else if (location.y <= -BOX_SIZE/2) {
    velocity.y = -velocity.y;
    location.y = -BOX_SIZE/2;
  } 
}
}


