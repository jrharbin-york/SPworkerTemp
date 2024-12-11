package metrics.custom;

import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.Collector;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import datatypes.custom.Point3D;
import uk.ac.york.sesame.testing.architecture.data.EventMessage;
import uk.ac.york.sesame.testing.architecture.metrics.Metric;
import uk.ac.york.sesame.testing.architecture.utilities.ParsingUtils;

public class distanceToHuman1Metric extends Metric {

        private static final long serialVersionUID = 1L;
		private static final double CLOSE_APPROACH_THRESHOLD_R1 = 3.0;
		private static final boolean ALWAYS_SEND_DISTANCE = true;
        private ValueState<Point3D> humanLoc;

        public void open(Configuration parameters) throws Exception {
                humanLoc = getRuntimeContext().getState(new ValueStateDescriptor<>("humanLoc1", Point3D.class));
        }

        public void processElement1(EventMessage msg, Context ctx, Collector<Double> out) throws Exception {
                String topic = msg.getTopic();
                // Check for human location messages
                if (topic.contains("/humans/citizen_extras_female_03")) {
                        Object value = msg.getValue();
                        Object obj = JSONValue.parse(value.toString());
                        JSONObject jo = (JSONObject) obj;
                        Double x = (Double) ParsingUtils.getField(jo, "x");
                        Double y = (Double) ParsingUtils.getField(jo, "y");
                        Double z = (Double) ParsingUtils.getField(jo, "z");
                        Point3D currentHuman = new Point3D(x, y, z);
                        // Store them in the current location tracking state
                        humanLoc.update(currentHuman);
                }

                // Check for ETERRY robot odom messages
                if (topic.contains("/odom")) {
                        if (humanLoc.value() != null) {
                                Object value = msg.getValue();
                                Object obj = JSONValue.parse(value.toString());
                                JSONObject jo = (JSONObject) obj;
                                Double x = (Double) ParsingUtils.getField(jo, "pose.pose.position.x");
                                Double y = (Double) ParsingUtils.getField(jo, "pose.pose.position.y");
                                Double z = (Double) ParsingUtils.getField(jo, "pose.pose.position.z");
                                Point3D eterryRobot = new Point3D(x, y, z);
                                Point3D lastHumanPos = humanLoc.value();
                                double dist = eterryRobot.distanceToOther(lastHumanPos);
                                if (ALWAYS_SEND_DISTANCE) {
                                	System.out.println("eterryRobot dist=" + dist);
                                	out.collect(dist);
                                }
                                
                                if (dist < CLOSE_APPROACH_THRESHOLD_R1) {
                                	System.out.println("eterryRobot close approach: dist=" + dist);
                                	out.collect(dist);
                                }
                        }
                }
        }
}
