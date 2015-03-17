void setup() {
  size(400, 400, P2D);
}

void draw() {
  My3DPoint eye = new My3DPoint(-100, -100, -5000);
  My3DPoint origin = new My3DPoint(0, 0, 0); //The first vertex of your cuboid
  My3DBox input3DBox = new My3DBox(origin, 100,150 ,300);
  
  // float[] point =  matrixProduct(scaleMatrix(x/xPressed, y/yPressed, 1.0), {100, 150, 300, 1});
  
  float[] point = matrixProduct(scaleMatrix(x/xPressed, y/yPressed, 1.0), new float[] {100, 150, 300, 1});
  
  projectBox(eye, new My3DBox(origin, point[0], point[1], point[2])).render();
}

class My2DPoint {
  float x;
  float y;
  My2DPoint(float x, float y) {
    this.x = x;
    this.y = y;
  }
}

class My3DPoint {
  float x;
  float y;
  float z;
  My3DPoint(float x, float y, float z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }
}


My2DPoint projectPoint(My3DPoint eye, My3DPoint p) {
  float xp = (p.x - eye.x) * eye.z / (eye.z - p.z);
  float yp = (p.y - eye.y) * eye.z / (eye.z - p.z);
  
  return  new My2DPoint(xp, yp);
}

class My2DBox {
  My2DPoint[] s;
  My2DBox(My2DPoint[] s) {
  this.s = s;
  }

  void render(){
  // Complete the code! use only line(x1, y1, x2, y2) built-in function.
    for(int i = 0; i < 3; i++) {
      line(s[i].x, s[i].y, s[i + 1 ].x, s[i+1 ].y);
    }
    line(s[3].x, s[3].y, s[0].x, s[0].y);
    
    for(int i = 4; i < 7; i++) {
      line(s[i].x, s[i].y, s[i + 1 ].x, s[i+1 ].y);
    }
    line(s[7].x, s[7].y, s[4].x, s[4].y);
    line(s[0].x, s[0].y, s[4].x, s[4].y);
    line(s[3].x, s[3].y, s[7].x, s[7].y);
    line(s[1].x, s[1].y, s[5].x, s[5].y);
    line(s[2].x, s[2].y, s[6].x, s[6].y);
  }
}

class My3DBox {
  My3DPoint[] p;
  My3DBox(My3DPoint origin, float dimX, float dimY, float dimZ){
  float x = origin.x;
  float y = origin.y;
  float z = origin.z;
  this.p = new My3DPoint[]{new My3DPoint(x,y+dimY,z+dimZ),
                           new My3DPoint(x,y,z+dimZ),
                           new My3DPoint(x+dimX,y,z+dimZ),
                           new My3DPoint(x+dimX,y+dimY,z+dimZ),
                           new My3DPoint(x,y+dimY,z),
                           origin,
                           new My3DPoint(x+dimX,y,z),
                           new My3DPoint(x+dimX,y+dimY,z)
                          };
  }
  My3DBox(My3DPoint[] p) {
  this.p = p;
  }
}

My2DBox projectBox (My3DPoint eye, My3DBox box) {
// Complete the code!
  My3DPoint[] points3D = box.p;
  My2DPoint[] points2D = new My2DPoint[points3D.length];
  
  for (int i = 0; i < points3D.length; i++) {
    points2D[i] = projectPoint(eye, points3D[i]);
  }
  
  return new My2DBox(points2D);
}

// Part 2

float[] homogeneous3DPoint (My3DPoint p) {
  float[] result = {p.x, p.y, p.z , 1};
  return result;
}

float[][] rotateXMatrix(float angle) {
  return(new float[][] {{1, 0 , 0 , 0},
                        {0, cos(angle), sin(angle) , 0},
                        {0, -sin(angle) , cos(angle) , 0},
                        {0, 0 , 0 , 1}});
}

float[][] rotateYMatrix(float angle) {
  return(new float[][] {{cos(angle), 0 , sin(angle) , 0},
                        {0, 1, 0 , 0},
                        {-sin(angle), 0, cos(angle) , 0},
                        {0, 0 , 0 , 1}});
}
float[][] rotateZMatrix(float angle) {
   return(new float[][] {{cos(angle), -sin(angle) , 0 , 0},
                        {sin(angle), cos(angle), 0 , 0},
                        {0, 0, 1, 0},
                        {0, 0 , 0 , 1}});
}
float[][] scaleMatrix(float x, float y, float z) {
  return(new float[][] {{x, 0 , 0 , 0},
                        {0, y, 0 , 0},
                        {0, 0, z, 0},
                        {0, 0 , 0 , 1}});
}

float[][] translationMatrix(float x, float y, float z) {
  return(new float[][] {{1, 0 , 0 , x},
                        {0, 1, 0 , y},
                        {0, 0, 1, z},
                        {0, 0 , 0 , 1}});
}

float[] matrixProduct(float[][] a, float[] b) {
  float[] product = new float[a.length];
  
  for(int i = 0; i< a.length; i++) {
    for(int j = 0; j < b.length; j++) {
      product[i] = product[i] + a[i][j] * b[j];
    }
  }
  return product;
}

My3DBox transformBox(My3DBox box, float[][] transformMatrix) {
  My3DPoint[] points = box.p;
  My3DPoint[] transformedPoints = new My3DPoint[points.length]; 
  
  for (int i = 0; i < points.length; i++) {
    transformedPoints[i] = euclidian3DPoint(matrixProduct(transformMatrix, new float[] {points[i].x, points[i].y, points[i].z, 1}));
  }
  
  return new My3DBox(transformedPoints);
}

My3DPoint euclidian3DPoint (float[] a) {
  My3DPoint result = new My3DPoint(a[0]/a[3], a[1]/a[3], a[2]/a[3]);
return result;
}

// Assignment 3
float x;
float y; 
float z; 
float yPressed; 
float xPressed;

void mousePressed()
{
  yPressed = pmouseX;
  xPressed = pmouseY;
}
void mouseDragged() 
{
  x = mouseX;
  y = mouseY;
}

