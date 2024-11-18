import rclpy
import time
from rclpy.node import Node
from geometry_msgs.msg import Twist
from rosgraph_msgs.msg import Clock

# Using the IN version so the testting platform can read it and
# republish the fuzzed version on the cmd_vel topic
CMD_VEL_TX_TOPIC = "/turtle1/cmd_velIN"

CLOCK_TOPIC = "/clock"
VEL_MSG_COUNT_LIMIT = 4

# Must be less than 1 second
TIME_GRANULARITY_SECS = 0.1

# The time in the nanosecond frame at which the
# vel message is introduced, e.g 1e8 here will
# inject it at 0.1 seconds
TIME_NSEC_INJECT_VEL_MSG = 1e8

def main():
    rclpy.init()
    node = rclpy.create_node('sim_time_publisher')
    pub_clock = node.create_publisher(Clock, CLOCK_TOPIC, 1)
    pub_velmsg = node.create_publisher(Twist, CMD_VEL_TX_TOPIC, 1)

    sec = 0
    vel_message_sent_this_second = False
    nanosec = 0
    msg_count = 0

    start_time = time.time()

    while True:
        msg = Clock()
        msg.clock.sec = sec
        msg.clock.nanosec = nanosec
        pub_clock.publish(msg)
        node.get_logger().info('Publishing: Sim-Time Message:  sec: {}, nanosec: {}'.format(sec, nanosec))

        # Send the velocity message if ready in time...
        if nanosec >= TIME_NSEC_INJECT_VEL_MSG:
            # And if we have not sent too many messages
            if (msg_count < VEL_MSG_COUNT_LIMIT and (not vel_message_sent_this_second)):
                vel_msg = Twist()
                vel_msg.linear.x = 1.0
                vel_msg.angular.z = 0.0
                node.get_logger().info('Publishing vel_msg: "%s"' % str(vel_msg))
                pub_velmsg.publish(vel_msg)
                vel_message_sent_this_second = True
                msg_count += 1

        time.sleep(TIME_GRANULARITY_SECS)
        current = time.time() - start_time
        nanosec += int(TIME_GRANULARITY_SECS*1e9)
        if nanosec >= (1e9):
            sec += 1
            nanosec = 0
            vel_message_sent_this_second = False

if __name__ == "__main__":
    main()
