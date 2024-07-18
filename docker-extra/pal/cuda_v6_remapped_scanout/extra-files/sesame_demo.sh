#!/bin/bash
cd ~/sesame_ws
source ./install/setup.bash
roslaunch multitiago_gazebo_navigation.launch &
sleep 15
roslaunch multitiago_gazebo_navigation_copy.launch &
sleep 5
roslaunch multitiago_gazebo_navigation_copy_1.launch &
sleep 5
roslaunch multitiago_gazebo_navigation_copy_2.launch &
sleep 5
wait
kill $(jobs -p)
