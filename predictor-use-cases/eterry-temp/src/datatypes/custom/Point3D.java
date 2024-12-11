package datatypes.custom;

public class Point3D {
	private double x;
	private double y;
	private double z;
	
	public Point3D() {

	}
	
    public Point3D(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public double getX() {
    	return x;
    }
    
    public double getY() {
    	return y;
    }
    
    public double getZ() {
    	return z;
    }
    
    // Needed for Flink to serialize according to POJO
    // 
    public void setX(double x) {
    	this.x = x; 
    }
    
    public void setY(double y) {
    	this.y = y; 
    }
    
    public void setZ(double z) {
    	this.z = z; 
    }
	
	public double distanceSqrToOther(Point3D other) {
		double diff_x = other.x - this.x;
		double diff_y = other.y - this.y;
		double diff_z = other.z - this.z;
		double distSqr = diff_x*diff_x + diff_y*diff_y + diff_z*diff_z;
		return distSqr;
	}
	
	public double magnitudeSquared() {
		double sq = this.x * this.x + this.y * this.y + this.z * this.z;
		return sq;
	}
	
	public double magnitude() {
		double sq = this.x * this.x + this.y * this.y + this.z * this.z;
		return sq;
	}
	
	public String toString() {
		return "[" + this.x + "," + this.y + "," + this.z + "]";
	}
	
	public String toCSV() {
		return this.x + "," + this.y + "," + this.z;
	}
	
	public double distanceToOther(Point3D other) {
		return Math.sqrt(distanceSqrToOther(other));
	}

	public Point3D add(double x2, double y2, double z2) {
		return new Point3D(this.x + x2, this.y + y2, this.z + z2);
	}
	
}