class Mover {
  PVector location;
  PVector velocity;

  float bound;
  float bounceFactor = 0.8;

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
    mu = 0.5;
    frictionMagnitude = normalForce * mu;
  }

  void update(float rotateX, float rotateZ) {
    //println("rotateX: " + degrees(rotateX) + "; rotateZ: " + degrees(rotateZ));

    gravity.x = sin(rotateZ) * gravityConstant;
    //gravity.z = sin(rotateX) * gravityConstant; //USELESS I THINK, RECHECK
    gravity.y = sin(rotateX) * gravityConstant;

    PVector friction = velocity.get();
    friction.normalize();
    friction.mult(-frictionMagnitude);

    //println(gravity);

    velocity.add(gravity);
    velocity.add(friction);
    location.add(velocity);
  }

  void display(float radius) {
    pushMatrix();
      rotateX(-PI/2); //what...??
      translate(location.x, location.y, -radius -BOX_HEIGHT/2);
      sphere(radius); 
      //drawAxis();
    popMatrix();
  }

  void checkEdges() {
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
  
  PVector ballLocation() {
   return location.get(); // make a copy
  }

  void checkCylinderCollision(ArrayList<PVector> cylinderCenters, float cylinderRadius, float sphereRadius) {
    for (PVector center : cylinderCenters) {
      PVector newCenter = new PVector(center.x, center.y, 0);      //"place center on the same plan/coord systeme than the ball is on"
      //println("ball : "+location+" / center : "+center);
      //println(center.dist(location)+" VS "+cylinderRadius+sphereRadius);
      if (newCenter.dist(location)<=cylinderRadius+sphereRadius)
      {
        println("COLLISION");
        PVector normal = PVector.sub(location, newCenter);
        normal.normalize();

        float v1_dot_normal = velocity.dot(normal);
        velocity.sub(PVector.mult(normal, 2*v1_dot_normal)); 
        velocity.mult(bounceFactor);
        location = PVector.add(newCenter, PVector.mult(normal, cylinderRadius+sphereRadius));//prevent the ball from being trapped in a cylinder + the cylinder from being traversed by ball
        checkEdges();//to fix a "bug", happens when cylinder is near the edge and ball bounce out of plate => blocked out of plate
      }
    }
  }
}

