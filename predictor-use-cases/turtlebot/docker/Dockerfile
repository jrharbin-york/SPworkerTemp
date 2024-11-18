ARG ROS_DISTRO=humble
FROM osrf/ros:${ROS_DISTRO}-desktop
ENV ROS_DISTRO=${ROS_DISTRO}

LABEL package.date=2024-11-05

RUN apt-get -y update
RUN apt-get -y install nano emacs xterm less ack gv
RUN apt-get -y install ros-humble-gazebo-dev ros-humble-gazebo-ros
RUN apt-get -y update && apt-get -y install ros-humble-turtlebot3-* 
RUN apt-get -y update && apt-get -y install ros-humble-rosbridge-*

#RUN apt-get -y install ros-humble-nav2-*
#RUN apt-get install -y ros-humble-imu-sensor-broadcaster ros-humble-mapviz ros-humble-mapviz-plugins ros-humble-tile-map

RUN mkdir -p /home/jharbin/turtlebot3_ws/src

COPY simulation-contents/ /home/jharbin/turtlebot3_ws/src

# Put the new launch file with remapping in place
COPY simulation-contents/navigation_launch.py /opt/ros/humble/share/nav2_bringup/launch/navigation_launch.py

RUN useradd -ms /bin/bash jharbin
RUN chown -R jharbin /home/jharbin/

USER jharbin

WORKDIR /home/jharbin/turtlebot3_ws/src
SHELL ["/bin/bash", "-c"]
RUN git clone -b ${ROS_DISTRO}-devel https://github.com/ROBOTIS-GIT/turtlebot3_simulations.git
RUN . /opt/ros/humble/setup.bash && colcon build

