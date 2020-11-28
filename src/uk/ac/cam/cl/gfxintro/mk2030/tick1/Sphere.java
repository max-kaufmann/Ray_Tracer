package uk.ac.cam.cl.gfxintro.mk2030.tick1;

public class Sphere extends SceneObject {

    // Sphere coefficients
    private final double SPHERE_KD = 0.8;
    private final double SPHERE_KS = 1.2;
    private final double SPHERE_ALPHA = 10;
    private final double SPHERE_REFLECTIVITY = 0.001;

    // The world-space position of the sphere
    private Vector3 position;

    public Vector3 getPosition() {
        return position;
    }

    // The radius of the sphere in world units
    private double radius;

    public Sphere(Vector3 position, double radius, ColorRGB colour) {
        this.position = position;
        this.radius = radius;
        this.colour = colour;

        this.phong_kD = SPHERE_KD;
        this.phong_kS = SPHERE_KS;
        this.phong_alpha = SPHERE_ALPHA;
        this.reflectivity = SPHERE_REFLECTIVITY;
    }

    public Sphere(Vector3 position, double radius, ColorRGB colour, double kD, double kS, double alphaS, double reflectivity) {
        this.position = position;
        this.radius = radius;
        this.colour = colour;

        this.phong_kD = kD;
        this.phong_kS = kS;
        this.phong_alpha = alphaS;
        this.reflectivity = reflectivity;
    }

    /*
     * Calculate intersection of the sphere with the ray. If the ray starts inside the sphere,
     * intersection with the surface is also found.
     */
    public RaycastHit intersectionWith(Ray ray) {

        // Get ray parameters
        Vector3 O = ray.getOrigin();
        Vector3 D = ray.getDirection();

        RaycastHit empty = new RaycastHit();
        // Get sphere parameters
        Vector3 C = position;
        double r = radius;

        // Calculate quadratic coefficients
        double a = D.dot(D);
        double b = 2 * D.dot(O.subtract(C));
        double c = (O.subtract(C)).dot(O.subtract(C)) - Math.pow(r, 2);
        double disc = b*b - 4*a*c;
        double s1;
        double s2;
        double distance;

        if (disc < 0 ) {
            return empty;
        } else if (disc == 0) {
             distance = (-b) / (2*a);
        } else{
             s1 = ((-b) - Math.sqrt(disc))/ 2*a;
             s2 = ((-b) + Math.sqrt(disc)) / 2*a;
            if ((s1<0)&&(s2<0)) {
                return empty;
            } else if (s1 > s2) {
                distance = s2;
            }else{
                distance = s1;
            }
            }

        Vector3 point = ray.evaluateAt(distance);
        Sphere sphere = this;
        Vector3 intersectionPosition = O.add(D.scale(distance));
        Vector3 normal = this.getNormalAt(intersectionPosition);

        RaycastHit answer = new RaycastHit(sphere,distance,point,normal);
        return answer;
    }

    // Get normal to surface at position
    public Vector3 getNormalAt(Vector3 positionIn) {
        return (positionIn).subtract(this.position).normalised();
    }
}
