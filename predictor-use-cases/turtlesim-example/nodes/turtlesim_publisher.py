import rclpy
from rclpy.node import Node
from std_msgs.msg import String
from geometry_msgs.msg import Twist
import random
import math

CMD_VEL_TX_TOPIC = "/turtle1/cmd_velIN"
TIME_LIMIT = 4

class TurtlesimDriveTrajectory(Node):
    def __init__(self):
        super().__init__('turtlesim_drive_trajectory')
        self.publisher_ = self.create_publisher(Twist, CMD_VEL_TX_TOPIC, 10)
        timer_period = 1.0
        self.timer = self.create_timer(timer_period, self.timer_callback)
        self.i = 0

    def timer_callback(self):
        if self.i < TIME_LIMIT: 
            msg = Twist()
            msg.linear.x = 1.0
            msg.angular.z = 0.0
            #        msg.angular.z = random.random() * math.pi
            self.publisher_.publish(msg)
            self.get_logger().info('Publishing: "%s"' % str(msg))
        self.i += 1

def main(args=None):
    rclpy.init(args=args)
    publisher = TurtlesimDriveTrajectory()
    rclpy.spin(publisher)
    # Destroy the node explicitly
    # (optional - otherwise it will be done automatically
    # when the garbage collector destroys the node object)
    publisher.destroy_node()
    rclpy.shutdown()


if __name__ == '__main__':
    main()
