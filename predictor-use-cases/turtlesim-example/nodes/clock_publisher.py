import rclpy
import time
from rclpy.node import Node
from rosgraph_msgs.msg import Clock

# Must be less than 1 second
TIME_GRANULARITY_SECS = 0.1

def main():
    rclpy.init()
    node = rclpy.create_node('sim_time_publisher')
    pub = node.create_publisher(Clock, '/clock', 1)

    sec = 0
    nanosec = 0

    start_time = time.time()

    while True:
        msg = Clock()
        msg.clock.sec = sec
        msg.clock.nanosec = nanosec

        pub.publish(msg)
        node.get_logger().info('Publishing: Sim-Time Message:  sec: {}, nanosec: {}'.format(sec, nanosec))
        time.sleep(TIME_GRANULARITY_SECS)
        current = time.time() - start_time
        nanosec += int(TIME_GRANULARITY_SECS*1e9)
        
        if nanosec >= (1e9):
            sec += 1
            nanosec = 0

if __name__ == "__main__":
    main()
