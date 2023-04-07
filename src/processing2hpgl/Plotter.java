
package processing2hpgl;

import processing.core.*;
import processing.serial.*;
import java.util.ArrayList; // import the ArrayList class

/**
 * This is a library for interfacing with Pen Plotters that use HPGL.  
 * The goal of the library enable communication with a pen plotter while closely mimicking Processing functions.
 * See the Plot example to see a sketch that showcase the current functionality of the library.
 * Testing with a HP7475A Pen Plotter connected to Macbook via FTDI Usb cable. 
 *
 * @example Plot
 */

public class Plotter {
  // myParent is a reference to the parent sketch
  PApplet myParent;
  public final static String VERSION = "##library.prettyVersion##";

  boolean DEBUG; //for deugging

  //class properties
  Serial port;
  int xMin, yMin, xMax, yMax;
  float scale; //this is used to stay in porportion with proceessing

  /**
   * Plotter Constructor,
   *
   * @example plotTest
   * @param theParent the parent PApplet
   */
  public Plotter(PApplet theParent, Serial _port,  int _xMin, int _yMin, int _xMax, int _yMax, float _scale, boolean _debug) {
    port = _port;
    xMin = _xMin;
    yMin = _yMin;
    xMax = _xMax;
    yMax = _yMax;
    scale = _scale;
    write("IN;"); //init the printer

    DEBUG = _debug;
  }

  /**
   *
   * @param hpgl the string to send to the plotter
   */
  public void write(String hpgl) {
    port.write(hpgl);
  }

  private float convert(float value) { //convert pixel value to plot value
    return value * scale;
  }

  private float convertX(float value) { //convert pixel x value to plot x value
    return convert(value)+xMin;
  }

  private float convertY(float value) { //convert pixel y value to plot y value
    return convert(value)+yMin;
  }

  //Plotter Utility Methods

  /**
   *
   * @param selectPen which pen? an int between 0 and 6
   */
  public void selectPen(int slot) {
    if (slot >= 0 && slot <= 6 ) {
      write("SP" + slot + ";");
    } else {
      System.out.println("Your pen selection of " + slot + " isn't a valid pen slot. Using default pen instead.");
    }
  }

  /**
   * Sends the pen to a location
   * @param x the x location to move to
   * @param y the y lcoation to move to
   */
  public void sendTo(float x, float y){
    String statement = "PU" + convertX(x) + "," + convertY(y) + ";";

    if(DEBUG) System.out.println(statement);
    write(statement);
  }

  /**
   * Change to a solid line
   *
   */
  public void lineType(){ //no params, reset to default
    String statement = "LT;";
    if(DEBUG) System.out.println(statement);
    write(statement);
  }

  /**
   * Change the type of line to draw
   * @param mode int between 0 and 6 to set line mode, https://www.isoplotec.co.jp/HPGL/eHPGL.htm#-LT(Line%20Type)
   */
  public void lineType(int mode){
    String statement = "LT" + mode + ";"; //no mode means a 4% space
    if(DEBUG) System.out.println(statement);
    write(statement);
  }

  /**
   * Change the type of line to draw
   * @param mode int between 0 and 6 to set line mode, https://www.isoplotec.co.jp/HPGL/eHPGL.htm#-LT(Line%20Type)
   * @param space float representing spacing. A percent of the total line length, i.e. 10 would space the dashes 10% of the total line length
   */
  public void lineType(int mode, float space){ //space is a percent from p1 to p2
    String statement = "LT" + mode + "," + space + ";";
    if(DEBUG) System.out.println(statement);
    write(statement);
  }

  /**
   * Set the Fill Type, 1 or 2
   * @param model Fill model, 1 or 2
   */
  public String fillType(int model){ //fill type 1 or 2, solid fill based on specified pen thickness
    String statement = "FT"+model+";";
    return statement;
  }

  /**
   * Set the Fill Type, 3 or 4
   * @param model 3 or 4
   * @param space The spacing of the fill
   * @param angle the angle of the fil
   */
  public String fillType(int model, float space, float angle){ //fill type 3(hatching) or 4(crosshatch),
    String statement = "FT" + model + "," + convert(space) + "," + angle +";";
    return statement;
  }

  /**
   * Change the rotation of the plotter's coordinate system,
   * @param theta the angle of rotation expressed as an int
   */
  public void rotatePlotter(int theta){ //right now only works with 90 degrees and messes up p2
    String statement ="RO" + theta  + ";";
    if(DEBUG) System.out.println(statement);
    write(statement);
  }

  //Drawing Commands

  /**
   * Draw a single line, point to point
   * @param xStart the horizontal starting point of the line
   * @param yStart the vertical starting point of the line
   * @param xEnd the horizontal ending point of the line
   * @param yEnd the vertical ending point of the line
   */
  public void drawLine(float xStart, float yStart, float xEnd, float yEnd) {
    //build a statement string so that only one write needs to be made to the plotter
    //start the command, pen up, move to start location
    String statement = "PU" + convertX(xStart) + "," + convertY(yStart) + ";";

    //pen down, move to end location, put pen up
    statement += "PD" + convertX(xEnd) + "," + convertY(yEnd) + ";PU;";

    if(DEBUG) System.out.println(statement);
    write(statement); //send the statement to the plotter
  }

  /**
   * Draw a line between from current location to a new point
   * @param x x location of drawing point
   * @param y y location of drawing point
   */
  public void drawTo(float x, float y){
    //problem here???????
    String statement = "PD;PA" + convertX(x) + "," + convertY(y) + ";";
    if(DEBUG) System.out.println(statement);
    write(statement);
  }

  /**
   * draw a series of connected lines
   * @param vertices An array of PVectors
   */
  public void drawLines(PVector[] vertices) {
    //start the statement, pen up and move to first location, pen down, ready for next location
    String statement = "PU"+ convertX(vertices[0].x) + "," + convertY(vertices[0].y) + ";PD";

    //loop through the rest of the locations, add the x and y coordinate for each to the statement
    for (int i = 1; i < vertices.length; i++) {
      float x = convertX(vertices[i].x);
      float y = convertY(vertices[i].y);

      statement += (x + "," + y);

      //if we aren't at the end, add a comma to add the next location
      if(i != vertices.length -1){
        statement += ","; //
      }
    }

    statement += ";PU;"; //close statement and pen up

    if(DEBUG) System.out.println(statement);
    write(statement); //send the statement to the plotter
  }

  /**
   * draw a series of connected lines
   * @param vertices An arrayList of PVectors
   */
  public void drawLines(ArrayList<PVector> vertices) {
    PVector origin = vertices.get(0);
    String statement = "PU;PA"+ convertX(origin.x) + "," + convertY(origin.y) + ";";
    statement += "PD;"; //clear any polygon

    for (int i = 1; i < vertices.size(); i++) {
      float x = convertX(vertices.get(i).x);
      float y = convertY(vertices.get(i).y);

      statement += (x + "," + y);

      //if we aren't at the end, add a comma to add the next location
      if(i != vertices.size() -1){
        statement += ","; //
      }
    }

    statement += ";PU;"; //close statement and pen up

    if(DEBUG) System.out.println(statement);
    write(statement);
  }

  /**
   * Draw a circle
   * @param x the x point for the center
   * @param y the y point for the center
   * @param diam the diameter of the circle
   */
  public void drawCircle(float x, float y, float diam) {
    //convert the given pixel dimension to the printer dimensions
    float radius = convert(diam/2);
    //put pen at x,y, draw a circle with specified radius
    String statement = "PA" + convertX(x) + "," + convertY(y) + ";" + "CI" + radius + ";";
    if(DEBUG) System.out.println(statement);
    write(statement);
  }

  /**
   * Draw a circle at x, y with input resolution
   * @param x the x point for the center
   * @param y the y point for the center
   * @param diam the diameter of the circle
   * @param res resolution of the circle
   */
  public void drawCircle(float x, float y, float diam, float res) {
    //convert the given pixel dimension to the printer dimensions
    float radius = convert(diam/2);
    //put pen at x,y, draw a circle with specified radius
    String statement = "PA" + convertX(x) + "," + convertY(y) + ";" + "CI" + radius + "," + res + ";";
    if(DEBUG) System.out.println(statement);
    write(statement);
  }

  /**
   * Fill a circle at x an y, filltype 1 or 2
   * @param x the x point for the circle
   * @param y the y point for the circle
   * @param model the fill type, 1 or 2
   */
  public void fillCircle(float _x, float _y, float diam, int model) {
    float radius = convert(diam/2);
    float x = convertX(_x);
    float y = convertY(_y);

    String statement = "PU;PA" + x + "," + y + ";"; //put pen at circle center xy
    statement += fillType(model); //setup fill
    statement += "WG" + radius + ",0,360;"; //uses the wedge command to draw a circle

    if(DEBUG) System.out.println(statement);
    write(statement);
  }

  /**
   * Fill a circle at x an y, filltype 1 or 2
   * @param _x the x point for the circle
   * @param _y the y point for the circle
   * @param model the fill type, 3 or 4
   * @param space the spacing of the fill
   * @param angle the angle of the fill
   */
  public void fillCircle(float _x, float _y, float diam, int model, float space, float angle){
    float radius = convert(diam/2);
    float x = convertX(_x);
    float y = convertY(_y);

    String statement = "PU;PA" + x + "," + y + ";"; //put pen at circle center xy
    statement += fillType(model,convert(space),angle); //setup fill
    statement += "WG" + radius + ",0,360;"; //uses the wedge command to draw a circle

    if(DEBUG) System.out.println(statement);
    write(statement);
  }

  /**
   * Draw a wedge
   * @param _x The x location of the center
   * @param _y The y location of the center
   * @param _dia the diamter of the circle
   * @param _startAngle the start angle of the arc in radians
   * @param _sweepAngle the end angle of the arc in radians
   */
  public void drawWedge(float _x, float _y, float _dia, float _startAngle, float _sweepAngle){
    float x = convertX(_x);
    float y = convertY(_y);
    float radius = convert(_dia)/2;

    //assume that angles are in radians
    int startAngle = (int) PApplet.degrees(_startAngle); //convert from radians to degrees
    int sweepAngle = (int) PApplet.degrees(_sweepAngle); //this is the sweep of the angle

    String statement = "PU; PA" + x + "," + y + ";";
    statement += "EW" + radius + "," + startAngle + "," + sweepAngle + ";";

    if(DEBUG) System.out.println(statement);
    write(statement);
  }

  /**
   * Draw a wedge
   * @param _x The x location of the center
   * @param _y The y location of the center
   * @param _dia the diamter of the circle
   * @param _startAngle the start angle of the arc in radians
   * @param _sweepAngle the end angle of the arc in radians
   * @param model Fill type, 1 or 2
   */
  public void fillWedge(float _x, float _y, float _dia, float _startAngle, float _sweepAngle, int model) {
    float x = convertX(_x);
    float y = convertY(_y);
    float radius = convert(_dia)/2;

    int startAngle = (int) PApplet.degrees(_startAngle); //convert from radians to degrees
    int sweepAngle = (int) PApplet.degrees(_sweepAngle); //this is the sweep of the angle

    String statement = "PU;PA" + x + "," + y + ";"; //put pen at circle center xy
    statement += fillType(model); //setup fill
    statement += "WG" + radius + "," + startAngle + "," + sweepAngle + ";";

    if(DEBUG) System.out.println(statement);
    write(statement);
  }

  /**
   * Draw a wedge, fill type 3 and 4
   * @param _x The x location of the center
   * @param _y The y location of the center
   * @param _dia the diamter of the circle
   * @param _startAngle the start angle of the arc
   * @param _sweepAngle the end angle of the arc
   * @param model Fill type, 1 or 2
   * @param space space of the fill
   * @param angle angle of the fill
   */
  public void fillWedge(float _x, float _y, float _dia, float _startAngle, float _sweepAngle, int model, float space, float angle) {
    float x = convertX(_x);
    float y = convertY(_y);
    float radius = convert(_dia)/2;

    int startAngle = (int) PApplet.degrees(_startAngle); //convert from radians to degrees
    int sweepAngle = (int) PApplet.degrees(_sweepAngle); //this is the sweep of the angle

    String statement = "PU;PA" + x + "," + y + ";"; //put pen at circle center xy
    statement += fillType(model,convert(space),angle); //setup fill
    statement += "WG" + radius + "," + startAngle + "," + sweepAngle + ";";

    if(DEBUG) System.out.println(statement);
    write(statement);
  }

  /**
   * Draws a rectangle
   * @param x The x position of the rectangle
   * @param y The y position of the rectangle
   * @param w The width of the rectangle
   * @param h the height of the rectangle
   */
  public void drawRect(float x, float y, float w, float h){
    String statement = "";
    float xStart = convertX(x);
    float yStart = convertY(y);
    float xEnd = convert(w);
    float yEnd = convert(h);

    statement += "PU;PA" + xStart + "," + yStart + ";PD;" + "ER" + xEnd + "," + yEnd + ";PU;";

    if(DEBUG) System.out.println(statement);
    write(statement);
  }

  //fill rect, fill types 1 and 2
  /**
   * Draws a filled rectangle
   * @param x The x location of the rectangle
   * @param y The y location of the rectangle
   * @param w The width of the rectangle
   * @param h The height of the model
   * @param model The fill type, 1 or 2
   */
  public void fillRect(float x, float y, float w, float h, int model){
    //setup the filltype
    String statement = "";
    statement += fillType(model);

    //setup the coordinates
    float xStart = convertX(x);
    float yStart = convertY(y);
    float xEnd = convert(w);
    float yEnd = convert(h);

    statement += "PU;PA" + xStart + "," + yStart + ";PD;";
    statement += "RR" + xEnd + "," + yEnd + ";PU;";

    if(DEBUG) System.out.println(statement);
    write(statement);
  }

  //fill rect, for filltypes 3 and 4 which need a spaceing and angle
  /**
   * Draws a filled rectangle, filltypes 3 and 4
   * @param x The x location of the rectangle
   * @param y The y location of the rectangle
   * @param w The width of the rectangle
   * @param h The height of the model
   * @param model The fill type, 1 or 2
   * @param space The spacing of the fill
   * @param angle The angle of the fill
   */
  public void fillRect(float x, float y, float w, float h, int model, float space, float angle){
    //setup the filltype
    String statement = "";
    statement += fillType(model,convert(space),angle);

    //setup the coordinates
    float xStart = convertX(x);
    float yStart = convertY(y);
    float xEnd = convert(w);
    float yEnd = convert(h);

    statement += "PU:" + xStart + "," + yStart + ";PD";
    statement += "RR" + xEnd + "," + yEnd + ";PU;";

    if(DEBUG) System.out.println(statement);
    write(statement);
  }

  /**
   * Draw a unfilled polygon
   * @param vertices An array of PVectors for the cordinates
   */
  public void drawPoly(PVector[] vertices) {
    String statement = "PU;PA"+ convertX(vertices[0].x) + "," + convertY(vertices[0].y) + ";";
    statement += "PM0;PD;"; //clear any polygon

    /* //loop through the rest of the locations, add the x and y coordinate for each to the statement */
    for (int i = 0; i < vertices.length; i++) {
      float x = convertX(vertices[i].x);
      float y = convertY(vertices[i].y);

      statement += ("PA" + x + "," + y + ";");
    }

    //return to start
    statement += "PA"+ convertX(vertices[0].x) + "," + convertY(vertices[0].y) + ";";
    statement += "PU;PM2;EP;"; //pen up, close polygon

    if(DEBUG) System.out.println(statement);
    write(statement); //send the statement to the plotter
  }

  /**
   * Draw a unfilled polygon
   * @param vertices An ArrayList of PVectors for the cordinates
   */
  public void drawPoly(ArrayList<PVector> vertices) {
    PVector origin = vertices.get(0);
    String statement = "PU;PA" + convertX(origin.x) + "," + convertY(origin.y) + ";";
    statement += "PM0;PD;"; //clear any polygon

    for (PVector v : vertices) {
      statement += ("PA" + convertX(v.x) + "," + convertY(v.y) + ";");
    }

    //return to start
    statement += "PA" + convertX(origin.x) + "," + convertY(origin.y) + ";";
    statement += "PU;PM2;EP;"; //pen up, close polygon

    if(DEBUG) System.out.println(statement);
    write(statement); //send the statement to the plotter
  }

  /**
   * Draw a polygon that takes in a pShape, untested
   * @param s PShape to draw
   */
  public void drawShape(PShape s){
    PVector origin = s.getVertex(0);
    String statement = "PU;PA" + convertX(origin.x) + "," + convertY(origin.y) + ";";
    statement += "PM0;PD;"; //clear any polygon and start polygon mode

    //getVertexCount()  Returns the total number of vertices as an int
    for (int i = 0; i < s.getVertexCount(); i++) {
      PVector v = s.getVertex(i); //current vertex
      statement += ("PA" + convertX(v.x) + "," + convertY(v.y) + ";");
    }

    //return to start
    statement += "PA" + convertX(origin.x) + "," + convertY(origin.y) + ";";
    statement += "PU;PM2;EP;"; //pen up, close polygon

    if(DEBUG) System.out.println(statement);
    write(statement); //send the statement to the plotter
  }

  //doesn't seem to work with HPGL1 and the 7475A
  /**
   * Draw a filled in polygon, not working with HPGL1 plotters
   * @param vertices An array of vertices
   * @param model Fill model
   * @param space The spacing of the fill
   * @param angle The angle of the fill
   */
  public void fillPoly(PVector[] vertices, int model, float space, float angle){
    //define poly with pen up, then fill?
    String statement = "PU;PA"+ convertX(vertices[0].x) + "," + convertY(vertices[0].y) + ";";

    statement += "PM0;"; //clear any polygon, don't put pen down?

    for (int i = 1; i < vertices.length; i++) {
      float x = convertX(vertices[i].x);
      float y = convertY(vertices[i].y);

      statement += ("PA" + x + "," + y + ";");
    }

    statement += "PM2;"; //pen up, fill polygon
    statement += fillType(model, convert(space), angle);
    statement += "FP;"; //pen up, fill polygon

    if(DEBUG) System.out.println(statement);
    write(statement); //send the statement to the plotter

  }

  /**
   * Draw a filled in polygon, not working with HPGL1 plotters
   * @param vertices An ArrayList of vertices
   * @param model Fill model
   * @param space The spacing of the fill
   * @param angle The angle of the fill in degrees
   */
  public void fillPoly(ArrayList<PVector> vertices, int model, float space, float angle){
    //define poly with pen up, then fill?
    PVector origin = vertices.get(0);
    String statement = "PU;PA" + convertX(origin.x) + "," + convertY(origin.y) + ";";
    statement += "PM0;"; //clear any polygon, don't put pen down?

    for (PVector v : vertices) {
      statement += ("PA" + convertX(v.x) + "," + convertY(v.y) + ";");
    }

    int spaceInt = (int) convert(space);
    statement += "PA" + convertX(origin.x) + "," + convertY(origin.y) + ";";
    statement += "PM1;"; //pen up, fill polygon
    statement += "PM2;"; //pen up, fill polygon
    statement += fillType(model, spaceInt, angle);
    statement += "FP;"; //pen up, fill polygon

    if(DEBUG) System.out.println(statement);
    write(statement); //send the statement to the plotter

  }

  /**
   * Draw a filled in polygon, not working with HPGL1 plotters
   * @param vertices An ArrayList of vertices
   * @param model Fill model 1 or 2
   */
  public void fillPoly(ArrayList<PVector> vertices, int model){
    //define poly with pen up, then fill?
    PVector origin = vertices.get(0);
    String statement = "PU;PA" + convertX(origin.x) + "," + convertY(origin.y) + ";";
    statement += "PM0;"; //clear any polygon, don't put pen down?

    for (PVector v : vertices) {
      statement += ("PA" + convertX(v.x) + "," + convertY(v.y) + ";");
    }

    statement += "PA" + convertX(origin.x) + "," + convertY(origin.y) + ";";
    statement += "PM1;"; //pen up, fill polygon
    statement += "PM2;"; //pen up, fill polygon
    statement += fillType(model);
    statement += "FP;"; //pen up, fill polygon

    if(DEBUG) System.out.println(statement);
    write(statement);

  }

  /**
   * Draw and arc
   * @param _x The x location of the arc
   * @param _y The y location of the arc
   * @param _size The size of the arc
   * @param _start The start of the arc in radians
   * @param _end The end of the arc in radians
   */
  public void drawArc(float _x, float _y, float _size, float _start, float _end) {
    float x = convertX(_x);
    float y = convertY(_y);
    float radius = convert(_size)/2;

    int sweep = (int) PApplet.degrees(_end - _start); //this is the swep of the angle in degrees

    //in hpgl present location becomes the start of the sweep
    //calculate where the pen should start
    float yStart = PApplet.sin(_start) * radius + y;
    float xStart = PApplet.cos(_start) * radius + x;

    //send the pen to the start location
    String statement = "PU;PA" + xStart + "," + yStart + ";PD;";
    statement += "AA" + x + "," + y + "," + sweep + ";PU;";

    if(DEBUG) System.out.println(statement);
    write(statement);
  }

  //Labels

  /**
   * Draw text labels
   * @param text The text to write
   * @param _x The x location of the label
   * @param _y The y location of the label
   */
  public void label(String text, float _x, float _y, float _size){
    System.out.println(_x);
    System.out.println(_y);
    System.out.println("before converting");
    float x = convertX(_x);
    float y = convertY(_y);
    float tWidth = _size * 0.0264f;  //set label width to global text size, pixel to cm conversion
    float tHeight = tWidth * 1.32f; //based on HPGL default, height is 1.32 times the width, so testing that
    System.out.println("after converting");
    
    System.out.println("starting string");
    String statement = "PU;PA" + x + "," + y + ";";
    statement += "SS;";
    
    // System.out.println(tWidth);
    // System.out.println(tHeight);
    statement += "SI" + tWidth + "," + tHeight + ";";
    System.out.println("here?");
    System.out.println(statement);
    statement += "LB" + text + (char) 3;
    System.out.println(statement);

    if(DEBUG) System.out.println(statement);
    write(statement);
    System.out.println("end of label func");
  }
}
