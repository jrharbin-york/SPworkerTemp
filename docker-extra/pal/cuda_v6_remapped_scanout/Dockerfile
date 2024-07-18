FROM cuda_v6_fixedscript

RUN rm /etc/apt/sources.list
COPY extra-files/sesame_demo.sh /home/user/sesame_ws
RUN chmod 755 /home/user/sesame_ws/sesame_demo.sh
RUN echo 'deb http://packages.ros.org/ros/ubuntu focal main' >> /etc/apt/sources.list
RUN echo 'deb http://archive.ubuntu.com/ubuntu/ focal main restricted universe multiverse' >> /etc/apt/sources.list
RUN apt-get update
RUN apt-get -y install tree less ack emacs xterm

# Edit the laser scanner
RUN sed -i '65s/scan_raw/scan_rawOUT/' /opt/pal/gallium/share/pmb2_2dnav_gazebo/launch/navigation.launch
RUN sed -i '82s/scan_raw/scan_rawOUT/' /opt/pal/gallium/share/omni_base_2dnav_gazebo/launch/navigation.launch
# For some reason, Tiago is controlled from config files in the home directory...
#RUN sed -i '96s/scan_raw/scan_rawOUT/' /opt/pal/gallium/share/tiago_2dnav_gazebo/launch/navigation.launch
RUN sed -i '96s/scan_raw/scan_rawOUT/' /home/user/sesame_ws/install/share/tiago_2dnav_gazebo/launch/navigation.launch

# Edit AMCL
RUN sed -i '3s/scan_raw/scan_rawOUT/' /home/user/sesame_ws/install/share/pal_navigation_cfg_pmb2/launch/localization_amcl.launch
RUN sed -i '3s/scan_raw/scan_rawOUT/' /home/user/sesame_ws/install/share/pal_navigation_cfg_omni_base/launch/localization_amcl.launch
RUN sed -i '3s/scan_raw/scan_rawOUT/' /home/user/sesame_ws/install/share/pal_navigation_cfg_tiago/launch/localization_amcl.launch


