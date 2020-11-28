package uk.ac.cam.cl.gfxintro.mk2030.tick1;

import java.awt.image.BufferedImage;
import java.util.List;

public class Renderer {

    // The width and height of the image in pixels
    private int width, height;

    // Bias factor for reflected and shadow rays
    private final double EPSILON = 0.0001;

    // The number of times a ray can bounce for reflection
    private int bounces;

    // Background colour of the image
    private ColorRGB backgroundColor = new ColorRGB(0.001);

    public Renderer(int width, int height, int bounces) {
        this.width = width;
        this.height = height;
        this.bounces = bounces;
    }

    /*
     * Trace the ray through the supplied scene, returning the colour to be rendered.
     * The bouncesLeft parameter is for rendering reflective surfaces.
     */
    protected ColorRGB trace(Scene scene, Ray ray, int bouncesLeft) {

        // Find closest intersection of ray in the scene
        RaycastHit closestHit = scene.findClosestIntersection(ray);

        // If no object has been hit, return a background colour
        SceneObject object = closestHit.getObjectHit();
        if (object == null) {
            return backgroundColor;
        }

        // Otherwise calculate colour at intersection and return
        // Get properties of surface at intersection - location, surface normal
        Vector3 P = closestHit.getLocation();
        Vector3 N = closestHit.getNormal();
        Vector3 O = ray.getOrigin();
        Vector3 D = ray.getDirection();

        // Illuminate the surface
        ColorRGB directIllumination = this.illuminate(scene, object, P, N, O);

        double reflectivity = object.getReflectivity();


        if (bouncesLeft == 0||reflectivity==0){
            return directIllumination;
        } else  {
                ColorRGB reflectedIllumination;
                Vector3 bouncedDirection = D.scale(-1).reflectIn(N).normalised();
                Vector3 bouncedLocation = P.add(bouncedDirection.scale(EPSILON));
                Ray reflectedRay = new Ray(bouncedLocation,bouncedDirection);

                reflectedIllumination = trace(scene, reflectedRay, bouncesLeft - 1);

                directIllumination = directIllumination.scale(1.0-reflectivity);
                reflectedIllumination = reflectedIllumination.scale(reflectivity);

                return directIllumination.add(reflectedIllumination);



        }


    }

    /*
     * Illuminate a surface on and object in the scene at a given position P and surface normal N,
     * relative to ray originating at O
     */
    private ColorRGB illuminate(Scene scene, SceneObject object, Vector3 P, Vector3 N, Vector3 O) {

        ColorRGB I_a = scene.getAmbientLighting(); // Ambient illumination intensity

        ColorRGB C_diff = object.getColour(); // Diffuse colour defined by the object

        // Get Phong coefficients
        double k_d = object.getPhong_kD();
        double k_s = object.getPhong_kS();
        double alpha = object.getPhong_alpha();

        ColorRGB amb_light = C_diff.scale(I_a);
        ColorRGB diff_light = new ColorRGB(0);
        ColorRGB spec_light = new ColorRGB(0);

        Vector3 V = O.subtract(P).normalised(); //Direction to point you are viewing from
        // Loop over each point light source
        List<PointLight> pointLights = scene.getPointLights();
        for (int i = 0; i < pointLights.size(); i++) {
            PointLight light = pointLights.get(i); // Select point light
            Vector3 lightPos = light.getPosition();

            // Calculate point light constants
            double distanceToLight = lightPos.subtract(P).magnitude();



            ColorRGB C_spec = light.getColour();
            ColorRGB I = light.getIlluminationAt(distanceToLight);
            Vector3 L = lightPos.subtract(P).normalised(); // Direction to light
            Vector3 R = L.reflectIn(N).normalised();//Refection of rays from light source
            RaycastHit hit = new RaycastHit();

            Ray shadowRay = new Ray(P.add(L.scale(EPSILON)), L);
            hit = scene.findClosestIntersection(shadowRay);
            Vector3 hitL = hit.getLocation();


            if (hit.getLocation() == null || hit.getDistance() > distanceToLight){
                diff_light = diff_light.add(C_diff.scale(k_d * (Math.max(0,N.dot(L)))).scale(I)) ;
                spec_light = spec_light.add(C_spec.scale(k_s*(Math.pow(Math.max(0,R.dot(V)),alpha))).scale(I));
            }

            // Calculate L, V, R
            // Calculate ColorRGB diffuse and ColorRGB specular terms
            // Add these terms to colourToReturn


        }
          ColorRGB colourToReturn = amb_light.add(diff_light).add(spec_light);
        return colourToReturn;
    }

    // Render image from scene, with camera at origin
    public BufferedImage render(Scene scene) {

        // Set up image
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // Set up camera
        Camera camera = new Camera(width, height);

        // Loop over all pixels
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                Ray ray = camera.castRay(x, y); // Cast ray through pixel
                ColorRGB linearRGB = trace(scene, ray, bounces); // Trace path of cast ray and determine colour
                ColorRGB gammaRGB = tonemap(linearRGB);
                image.setRGB(x, y, gammaRGB.toRGB()); // Set image colour to traced colour
            }
            // Display progress every 10 lines
            if (y % 10 == 0)
                System.out.println(String.format("%.2f", 100 * y / (float) (height - 1)) + "% completed");
        }
        return image;
    }


    // Combined tone mapping and display encoding
    public ColorRGB tonemap(ColorRGB linearRGB) {
        double invGamma = 1. / 2.2;
        double a = 2;  // controls brightness
        double b = 1.3; // controls contrast

        // Sigmoidal tone mapping
        ColorRGB powRGB = linearRGB.power(b);
        ColorRGB displayRGB = powRGB.scale(powRGB.add(Math.pow(0.5 / a, b)).inv());

        // Display encoding - gamma
        ColorRGB gammaRGB = displayRGB.power(invGamma);

        return gammaRGB;
    }


}
