#!/bin/bash

ROS2_SRC=/opt/ros/humble/setup.bash

xterm -e /bin/bash -c "source ${ROS2_SRC} && ros2 run turtlesim turtlesim_node" &
sleep 1
xterm -e /bin/bash -c "source ${ROS2_SRC} && ros2 run turtlesim turtle_teleop_key" &
sleep 1
xterm -e /bin/bash -c "source ${ROS2_SRC} && ./start_rosbridge.sh" &
sleep 15
xterm -e /bin/bash -c "source ${ROS2_SRC} && ./start_combined_node.sh"
