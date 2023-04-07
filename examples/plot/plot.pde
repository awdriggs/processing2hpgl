import processing.serial.*;
import processing2hpgl.*;

Serial myPort;    // Create object from Serial class
Plotter plotter;  // Create a plotter object
int val;          // Data received from the serial port, needed?

final boolean PLOTTING_ENABLED = true; //talk to the pen plotter, you cna use this to only run commands if you are wanting to print
final boolean DEBUG = true; //will print HPGL commands to console if true

//Plotter dimensions
int xMin, yMin, xMax, yMax;

float scale = 10; //used to keep processing window in proportion to paper size
                  //note on scaling. 1px gets scaled to 1 plotter unit. 1 plotter unit = 0.025mm?
                  //test this out 10px = 100plots = 2.5mm

void settings(){
  // Set the paper size first, this allows the preview window to be in proportion
  // "A" = letter size, "B" = tabloid size, "A4" = metric A4, "A3" = metric A3
  setPaper("A");

  if(DEBUG) println("print dimensions", xMin, yMin, xMax, yMax);

  // Calculate the processing canvas size, proportional to paper size
  float screenWidth = (xMax - xMin)/scale;
  float screenHeight = (yMax - yMin)/scale;

  //set the canvas size depending on the paper size that will be used...
  size(int(screenWidth), int(screenHeight));
  if(DEBUG) println("screen dimensions", width, height);
}

void setup(){
  if(PLOTTING_ENABLED){
    println(Serial.list()); //Print all serial ports to the console
    String portName = Serial.list()[5]; //You'll need to play around to see which is the correct port
    println("Plotting to port: " + portName);

    myPort = new Serial(this, portName, 9600); //opens the port

    plotter = new Plotter(this, myPort, xMin, yMin, xMax, yMax, scale, DEBUG); //create a plotter object, let the printer know the papersize and scale
  }

  noLoop(); //kill the loop, otherwise your print will never end, but maybe that's what you want ;)
}

void draw(){
  println("drawing");

  float locX, locY;

  /* plotter.selectPen(1); //pick a pen, 1-6 */


  //draw some lines
  for(int i = 0; i < 7; i++){
    if(i > 0){
      plotter.lineType(i);
    }

    locX = 10;
    locY = (i+1)*48;

    line(locX, locY, locX + 240, locY);
    plotter.drawLine(locX, locY, locX + 240, locY);
  }

  plotter.lineType(); //resets line to solid

  delay(10000); //if you send to many commands at once the plotter will bug out. Use delay to drip feed the plotter commands

  //plot some text
  locX = 260;
  locY = 0;

  stroke(0);
  textSize(12);
  text("hello", locX + 10, locY + 40);
  plotter.label("hello", locX + 10, locY + 40, 12); //text, x, y, size

  textSize(20);
  text("hello", locX + 10, locY + 60);
  plotter.label("hello", locX + 10, locY + 80, 20);

  textSize(40);
  text("hello", locX, locY);
  plotter.label("hello", locX + 10, locY + 120, 20);

  delay(10000);

  //plot some shapes
  //rects
  locX = 625; //going to draw rects around this center point
  locY = 180;

  rect(locX, locY, 100, 100);
  plotter.drawRect(locX, locY, 100, 100);

  //some fills!
  fill(0);
  rect(locX - 40, locY, 40, 40);
  plotter.drawRect(locX - 40, locY, 40, 40);
  plotter.fillRect(locX - 40, locY, 40, 40, 1); //mode 1 is solid fill

  fill(120);
  rect(locX, locY - 80, 40, 80);
  plotter.drawRect(locX, locY - 80, 40, 80);
  plotter.fillRect(locX, locY - 80, 40, 80, 3, 2, 45); //mode 3 is hatch, needs a space and angle param

  delay(10000);

  locX = 875; //going to draw cirlces around this center point
  locY = 180;

  noFill();
  circle(locX - 75, locY - 75, 150);
  plotter.drawCircle(locX - 75, locY -75, 150);

  fill(180);
  circle(locX + 32.5, locY + 75, 150);
  plotter.drawCircle(locX + 32.5, locY + 75, 150);
  plotter.fillCircle(locX + 32.5, locY + 75, 150, 3, 4, -45);

  fill(100);
  circle(locX + 45, locY - 45, 90);
  plotter.drawCircle(locX + 45, locY - 45, 90);
  plotter.fillCircle(locX + 45, locY - 45, 90, 4, 3, 90);

  delay(40000); //fill take forever!

  //polylines
  PVector[] pointList = new PVector[50];

  //generate some random points
  for(int i = 0; i < pointList.length; i++){
    float x = round(random(10, 240));
    float y = round(random(370, 710));

    pointList[i] = new PVector(x,y);
  }

  plotter.drawLines(pointList); //will accept an array or arrayList of PVectors

  delay(10000);

  //polygons
  locX = 625;
  locY = 540;

  //drawpoly with an array
  PVector poly[] = new PVector[7];
  poly[0] = new PVector(locX + 10, locY + 10);
  poly[1] = new PVector(locX - 100, locY + 125);
  poly[2] = new PVector(locX - 60, locY - 40);
  poly[3] = new PVector(locX - 120, locY - 90);
  poly[4] = new PVector(locX + 110, locY - 70);
  poly[5] = new PVector(locX + 55, locY + 2);
  poly[6] = new PVector(locX + 00, locY + 50);

  plotter.drawPoly(poly);  //will take an array or arrayList

  delay(5000);

  //arcs
  locX = 375;
  locY = 540;

  float angle1 = 0;
  float angle2 = 0.75 * PI;

  arc(locX, locY, 225, 225, angle1, angle2);
  plotter.drawArc(locX, locY, 225, angle1, angle2);

  angle1 = 1.5 * PI;
  angle2 = TWO_PI;
  arc(locX, locY, 100, 100, angle1, angle2);
  plotter.drawArc(locX, locY, 100, angle1, angle2);

  angle1 = 0.5 * PI;
  angle2 = TWO_PI;
  arc(locX, locY, 70, 70, angle1, angle2);
  plotter.drawArc(locX, locY, 70, angle1, angle2);

  delay(10000);

  //wedges
  locX = 875;
  locY = 540;

  //Note on wedges
  //first angle is where to start, second is how much to sweep, different than an arc
  //moves around counter clockwise
  plotter.drawWedge(locX, locY, 200.0, 0, 0.75 * PI);
  plotter.fillWedge(locX, locY, 150.0, 0.75 * PI, QUARTER_PI, 1); //solid fill
  plotter.fillWedge(locX, locY, 200.0, PI, HALF_PI, 4, 0.75, 45); //will without an edge
  plotter.drawWedge(locX, locY, 220.0, 1.5 * PI, HALF_PI);
  plotter.fillWedge(locX, locY, 220.0, 1.5 * PI, HALF_PI, 3, 2, 90);

  // Or you can send HPGL commands direct to the printer
  // plotter.write("CT0;PA0,0;CI5;"); //draws a circle at the origin
}

// set the global paper size
void setPaper(String size){
  if (size == "A") {
    xMin = 250;
    yMin = 596;
    xMax = 10250;
    yMax = 7796;
  } else if (size == "B") {
    xMin = 522;
    yMin = 259;
    xMax = 15722;
    yMax = 10259;
  } else if (size == "A3") {
    xMin = 170;
    yMin = 602;
    xMax = 15370;
    yMax = 10602;
  } else if (size == "A4") {
    xMin = 603;
    yMin = 521;
    xMax = 10603;
    yMax = 7721;
  } else {
    println("A valid paper size wasn't given to your Plotter object.");
  }
}

void keyPressed(){
  //e key will cause errors to be printed
  if(keyCode == 69){
    plotter.write("OE;");
  }

  // escape key will exit
  if(keyCode == 27){
    if(PLOTTING_ENABLED) myPort.stop();
    exit();
  }
}

//trying to catch some errors
String msgs;
void serialEvent(Serial p) {
  msgs = p.readString();
  println("heard from plotter... " + msgs);
}

