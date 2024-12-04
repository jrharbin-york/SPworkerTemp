package metrics.custom;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.Collector;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import uk.ac.york.sesame.testing.architecture.data.EventMessage;
import uk.ac.york.sesame.testing.architecture.data.Point;
import uk.ac.york.sesame.testing.architecture.metrics.Metric;

import uk.ac.york.sesame.testing.architecture.utilities.ParsingUtils;

public class distanceToPoint3DMetric extends Metric {

	private static final long serialVersionUID = 1L;
	   
    public void open(Configuration parameters) throws Exception {

    }
    
    public Point getTargetPoint() {
    	return new Point(1.0, 0.5, 0.0);
    }
    
    public void processElement1(EventMessage msg, Context ctx, Collector<Double> out) throws Exception {
    	System.out.println("distanceToPoint3DMetric");
    	
    	if (msg.getTopic().contains("/odom")) {
            Object value = msg.getValue();
            Object obj = JSONValue.parse(value.toString());
            JSONObject jo = (JSONObject) obj;
            Double x = (Double)ParsingUtils.getField(jo, "pose.pose.position.x");
            Double y = (Double)ParsingUtils.getField(jo, "pose.pose.position.y");
            Double z = (Double)ParsingUtils.getField(jo, "pose.pose.position.z");
    		
    		if ((x != null) && (y != null)) {
    			Point current = new Point(x,y,0.0);
    			double dist = current.distanceTo(getTargetPoint());
    			System.out.println("x=" + x + ",y=" + y + ",dist = " + dist);
    			out.collect(dist);
    		}
    	}
    }
}