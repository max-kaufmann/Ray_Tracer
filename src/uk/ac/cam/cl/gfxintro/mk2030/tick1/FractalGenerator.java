package uk.ac.cam.cl.gfxintro.mk2030.tick1;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class FractalGenerator {

    private final int RECURSBOTTOM = 3;
    private final double HeightRatio = Math.sqrt(3)/4;
    private final double PI = Math.PI;


    private final Vector3 i = new Vector3(1,0,0);
    private final Vector3 j = new Vector3(0,1,0);

    //Triangle Constants
    private final Vector3 BOTTOMLEFT = new Vector3 (0,-20,30);
    private final Vector3 UPDIRECTION = new Vector3(-Math.cos(PI/6),0.2,Math.sin(PI/6)).normalised(); //Note that this has to be perpendicular to
    private final Vector3 BASEDIRECTION = (new Vector3(Math.cos(PI/3),0.2,Math.sin(PI/3))).normalised();//This
    private final Vector3 EXPANDIRECTION = (BASEDIRECTION.cross(UPDIRECTION).normalised());//not currently used
    private final double SIDELENGTH = 5;

    //Sphere Constants
    private final int SPHERESPERLINE = 5;
    private final double SHRINKAGE = 0.1;
    private final String COLOUR = "#000000";
    private final double PLACEHOLDER2 = 0;



    //Stack Storing Sphere Location and Properties
    private Stack <Vector3[]> pointStack = new Stack<Vector3[]>();




    public static void main(String[] args) {
        FractalGenerator fractalGen = new FractalGenerator();
        fractalGen.genSpheres();
        Stack<Vector3[]> stack= fractalGen.getStack();
        fractalGen.createXML(stack);
        String[] inputARR = {"--input", "fractal.xml", "--output", "output.png", "--bounces","3"};
        try {
            Tick1.main(inputARR);
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    Stack <Vector3[]> getStack (){
        return this.pointStack;
    }

    private void genSpheres(){
        genSpheres(0,BASEDIRECTION,BOTTOMLEFT,SIDELENGTH);
    }

    private void genSpheres(int recursDepth,Vector3 D,Vector3 O,double s){
        if (recursDepth >= RECURSBOTTOM){
            return;
        }
        else{

            //Line Segments for bottom of new triangles
            Vector3 D1 = D;
            Vector3 O1 = O;
            double s1 = s/2;

            Vector3 D2 = D;
            Vector3 O2 = O.add(D.scale(s/2));
            double s2 = s/2;

            Vector3 D3 = D;
            Vector3 O3 = (O.add(D.scale(s*0.25)).add(UPDIRECTION.scale((s*(Math.sin(PI/3)))/2)));
            double s3 = s/2;

            //Line Segments for sides of current triangle

            Vector3 DL = O3.subtract(O).normalised();
            Vector3 OL = O;
            double sL = s;

            Vector3 DR = DL.reflectIn(UPDIRECTION).normalised();
            Vector3 OR = O1.add(D.scale(s));
            double sR = s;

            populateLine(O,D,s);
            populateLine(OL,DL,sL);
            populateLine(OR,DR,sR);

            genSpheres(recursDepth + 1,D1,O1,s1);//.add(UPDIRECTION.scale(s/2))
            genSpheres(recursDepth+1,D2,O2,s2);
            genSpheres(recursDepth+1,D3,O3,s3);

        }
    }

    void populateLine(Vector3 O, Vector3 D, double s){
        for (int i = 0; i <= (SPHERESPERLINE -1); i++) {

            Vector3[] outArr = new Vector3[2];
            outArr[0] = O.add(D.scale(s*( i / (SPHERESPERLINE - 1) )));
            outArr[1] = new Vector3 (s*(SHRINKAGE),0.1,PLACEHOLDER2);

            this.pointStack.push(outArr);
        }
    }

    void createXML(Stack<Vector3[]> stack){
        try {
            FileWriter file = new FileWriter("fractal.xml",false);
            file.write("<scene>");
            file.write("<point-light x=\"0\" y=\"30\" z=\"20\" colour=\"#FFFFFF\" intensity=\"500\"/>");
            file.write("<plane x=\"0.0\" y=\"-12\" z=\"15\" nx=\"0" +
                    "" + Double.toString(EXPANDIRECTION.x)+" \" ny=\""
                    + Double.toString(EXPANDIRECTION.y) +
                    "\" nz=\""+
                    Double.toString(EXPANDIRECTION.z) + "\" colour=\"#808080\"/> reflectivity = 1");
            file.write(" <ambient-light colour=\"#050505\" intensity=\"20\"/>");
            while (stack.size() > 0) {
                if (stack.size()%100 == 0){
                    file.flush();
                }
                Vector3[] currentInput = stack.pop();

                Vector3 location = currentInput[0];
                Vector3 properties = currentInput[1];

                String x = String.valueOf(location.x);
                String y = String.valueOf(location.y);
                String z = String.valueOf(location.z);

                String radius = String.valueOf(properties.x);
                String colour = Integer.toHexString((int) properties.y);

                String template = "<sphere reflectivity = \"0.1\" x=\"%s\" y=\"%s\" z=\"%s\" radius=\"%s\" colour=\"%s\"/> \n";
                String currentOutput = String.format(template, x, y, z, radius, COLOUR);
                file.write(currentOutput);

                if (stack.size() == 1){
                    file.write("</scene>");
                    file.close();
                }
            }

        }catch(Exception e){
            System.out.println("nope");
        }
    }
}
