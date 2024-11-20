
import java.util.Properties;
import java.util.Random;

import org.apache.flink.util.Collector;
import uk.ac.york.sesame.testing.architecture.data.EventMessage;

import uk.ac.york.sesame.testing.architecture.fuzzingoperations.*;
import uk.ac.york.sesame.testing.architecture.simulator.DeferredAction;
import uk.ac.york.sesame.testing.architecture.simulator.IPropertySetter;
import uk.ac.york.sesame.testing.architecture.simulator.InvalidPropertyType;
import uk.ac.york.sesame.testing.architecture.simulator.RestoreFailed;
import uk.ac.york.sesame.testing.architecture.simulator.SimCore;

// TODO: can we compensate for needed reordering of the rossim connect - dynamically create the ParamSetter later
// Unique IDs need to be included
// Any way to get better performance - have to check on every set
// Only check deferred events before sending a message out

public class DistortVelocity_0_Test_001_06_10_2024_04_35_31_paramtest extends TimeBasedFuzzingOperation {

	private static final long serialVersionUID = 1L;

	Random rng;
	//ROSSimulator rossim;
	//private IPropertySetter setter;

	protected String getUniqueID() {
		return "distortVelocity-0";
	}

	public DistortVelocity_0_Test_001_06_10_2024_04_35_31_paramtest() {
		super("<NULL>", 20.0, 25.0);
		
		this.rng = new Random(0L);
		Properties paramProps = new Properties();
		paramProps.setProperty("componentName", "/turtlesim");
		paramProps.setProperty("paramName", "background_r");
		// TODO: use uniqueID here in the generator - if multiple properties, append index
		SimCore.getInstance().registerPropertySetter("DistortVelocity_0_Test_001_06_10_2024_04_35_31_paramtest", paramProps);
	}

	@Override
	public void processElement1(EventMessage value, Context ctx, Collector<EventMessage> out) throws Exception {
		System.out.println("Processelement");
		if (isReadyNow()) {
			System.out.println("isReadyNow");
			
			IPropertySetter ps = SimCore.getInstance().getPropertySetter("DistortVelocity_0_Test_001_06_10_2024_04_35_31_paramtest");
			if (!ps.isSet()) {
				DeferredAction da = (() -> {
					System.out.println("Running set action");
					try { ps.set(255);
					System.out.println("done...");
					} 
					catch (InvalidPropertyType e) {
						e.printStackTrace();
					}
					});
				// TODO: need unique ID
				SimCore.getInstance().addDeferredAction("DA-START", da);
			}
		}
		
		if (!isReadyNow()) {
			IPropertySetter ps = SimCore.getInstance().getPropertySetter("DistortVelocity_0_Test_001_06_10_2024_04_35_31_paramtest");
			if (ps.isSet() && !ps.isRestored()) {
				DeferredAction da = (() -> {
					System.out.println("Running restore action");
					try { 
						ps.restoreOriginalValue();
					System.out.println("done...");
					} 
					catch (RestoreFailed e) {
						e.printStackTrace();
					}
					});
				// TODO: need unique ID
				SimCore.getInstance().addDeferredAction("DA-END", da);
			}
		}
		
		out.collect(value);
	}
}
