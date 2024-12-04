

import java.util.Map;
import java.util.Random;
import java.util.HashMap;
import datatypes.DoubleRange;

import org.apache.flink.util.Collector;
import org.json.simple.*;

import uk.ac.york.sesame.testing.architecture.utilities.ParsingUtils;
import uk.ac.york.sesame.testing.architecture.utilities.UpdateLambda;
import uk.ac.york.sesame.testing.architecture.data.EventMessage;

import uk.ac.york.sesame.testing.architecture.fuzzingoperations.*;
import uk.ac.york.sesame.testing.architecture.simulator.SimCore;


public class DistortVelocity_0_Test_004_08_11_2024_04_40_57 extends TimeBasedFuzzingOperation {

	private static final long serialVersionUID = 1L;
	
	Random rng;
	
	protected String getUniqueID() {
		return "distortVelocity-0";
	}

	public DistortVelocity_0_Test_004_08_11_2024_04_40_57() {
 
		super("/cmd_vel_nav", 67.43565163605926, 77.04423735149764);
		
		
		
 
		this.rng = new Random(-4754275276634555265L);
 	
		
	}
	
	

	@Override
	public void processElement1(EventMessage value, Context ctx, Collector<EventMessage> out) throws Exception { 
			if (value.getTopic().equals(topic) && isReadyNow()) {
				Object obj = JSONValue.parse(value.getValue().toString());
	      		JSONObject jo = (JSONObject)obj;
	      		double genVal1 = new DoubleRange(0.5,0.5).generateInRange(rng);
				jo = ParsingUtils.updateJSONObject(jo, "msg.angular.z", genVal1);
	      		EventMessage valueOut = new EventMessage(value);
	      		valueOut.setValue(jo.toString());
	      		double time = SimCore.getInstance().getTime();
	      		System.out.println("fuzzRangeLog," + time + "," + value.toString() + "," + valueOut.toString() + "\n");
				out.collect(valueOut);
			} else {
				out.collect(value);
			}
			
	
	}	
}

