package metrics.custom;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.Collector;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import uk.ac.york.sesame.testing.architecture.data.EventMessage;
import uk.ac.york.sesame.testing.architecture.metrics.Metric;

public class waypointCompletionMetric extends Metric {

        private static final long serialVersionUID = 1L;

        public void open(Configuration parameters) throws Exception {

        }

        public void processElement1(EventMessage msg, Context ctx, Collector<Double> out) throws Exception {
        	 String topic = msg.getTopic();
        	 if (topic.contains("/next_waypoint_count")) {
        		 Object value = msg.getValue();
        		 Object obj = JSONValue.parse(value.toString());
        		 JSONObject jo = (JSONObject) obj;
        		 try {
        			 Long v = (Long)jo.get("data");
        			 Double dv = v.doubleValue();
              		 System.out.println("follow_waypoints feedback object value = " + v.toString() + " - raw message = " + jo.toJSONString());
              		 out.collect(dv);
        		 } catch (Exception e) {
        			 System.err.println("Exception processing JSON data");
        			 e.printStackTrace(System.err);
        		 }
          	 }
        }
}
