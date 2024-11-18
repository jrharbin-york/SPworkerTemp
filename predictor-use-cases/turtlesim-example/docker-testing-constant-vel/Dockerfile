ARG ROS_DISTRO=humble

FROM osrf/ros:${ROS_DISTRO}-desktop
ENV ROS_DISTRO=${ROS_DISTRO}

RUN apt-get -y update
RUN apt-get -y install ros-humble-turtlesim ros-humble-rosbridge-*
RUN apt-get -y install xterm
RUN apt-get -y install nano

RUN mkdir -p /turtlesim/src
# Copy nodes into the Docker

COPY extra-files/launch_turtlesim.sh /turtlesim/
COPY extra-files/combined_clock_publisher.py /turtlesim/
COPY extra-files/start_rosbridge.sh /turtlesim/
COPY extra-files/start_combined_node.sh /turtlesim/

RUN useradd -ms /bin/bash turtle
RUN chown -R turtle /turtlesim/* 

USER turtle
WORKDIR /turtlesim
