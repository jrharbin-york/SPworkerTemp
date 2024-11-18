import math

from geometry_msgs.msg import Twist

import tf2_geometry_msgs

from nav_msgs.msg import Odometry
import rclpy
from rclpy.node import Node
from tf2_ros import TransformException
from tf2_ros.buffer import Buffer
from tf2_ros.transform_listener import TransformListener

from turtlesim.srv import Spawn

class FrameListener(Node):

    def __init__(self):
        super().__init__('map_frame_listener')

        self.tf_buffer = Buffer()
        self.tf_listener = TransformListener(self.tf_buffer, self)

        # Create turtle2 velocity publisher
        self.publisher = self.create_publisher(Twist, '/map_pose', 1)

        # Call on_timer function every second
        #self.timer = self.create_timer(1.0, self.on_timer)
        self.subscriber = self.create_subscription(Odometry, '/odom', self.callback, 1)

    def callback(self, msg):
        # Store frame names in variables that will be used to
        # compute transformations
        from_frame_rel = 'base_footprint'
        to_frame_rel = 'map'
        
        try:
            t = self.tf_buffer.lookup_transform(to_frame_rel, from_frame_rel, rclpy.time.Time())
        except TransformException as ex:
            self.get_logger().info(f'Could not transform {to_frame_rel} to {from_frame_rel}: {ex}')
            return

        # = t.transform(msg.pose)
        out_pose = self.tf_buffer.transform(msg.pose.pose, to_frame_rel)
        
        self.publisher.publish(out_pose)
        self.get_logger().info("Published out_msg")

def main():
    rclpy.init()
    node = FrameListener()
    try:
        rclpy.spin(node)
    except KeyboardInterrupt:
        pass

    rclpy.shutdown()

main()
