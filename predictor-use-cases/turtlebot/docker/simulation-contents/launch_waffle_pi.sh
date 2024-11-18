export TURTLEBOT3_MODEL=waffle_pi
source /usr/share/gazebo/setup.bash

#LOC=world
xterm -bg black -fg white -hold -e /bin/bash -c "source ./install/setup.bash && export TURTLEBOT3_MODEL=waffle_pi && ros2 launch turtlebot3_gazebo turtlebot3_world.launch.py" &
sleep 1
xterm -bg black -fg white -hold -e /bin/bash -c "source ./install/setup.bash && ros2 launch rosbridge_server rosbridge_websocket_launch.xml" &
sleep 9
xterm -bg black -fg white -hold -e /bin/bash -c "source ./install/setup.bash && export TURTLEBOT3_MODEL=waffle_pi && ros2 launch turtlebot3_cartographer cartographer.launch.py use_sim_time:=True" &
sleep 5
xterm -bg black -fg white -hold -e /bin/bash -c "source ./install/setup.bash && export TURTLEBOT3_MODEL=waffle_pi && ros2 run turtlebot3_teleop teleop_keyboard" &
sleep 5
xterm -bg black -fg white -hold -e /bin/bash -c "source ./install/setup.bash && export TURTLEBOT3_MODEL=waffle_pi && ros2 launch turtlebot3_navigation2 navigation2.launch.py use_sim_time:=True map:=$HOME/map.yaml" &
sleep 10
xterm -bg black -fg white -hold -e /bin/bash -c "source ./install/setup.bash && TURTLEBOT3_MODEL=waffle_pi && ros2 topic pub /goal_pose geometry_msgs/PoseStamped \"{header: {stamp: {sec: 0}, frame_id: 'map'}, pose: {position: {x: 3.0, y: 1.0, z: 0.0}, orientation: {w: 1.0}}}\"" &
xterm -bg black -fg white
