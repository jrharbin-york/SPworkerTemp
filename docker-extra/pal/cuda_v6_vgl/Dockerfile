FROM cuda_v6_fixedscript

ARG VGL_GROUP_ID=1002
ARG PAL_USER=user

RUN apt-get update
RUN apt-get install psmisc
COPY extra-files/virtualgl_3.1.1_amd64.deb /home/user/sesame_ws/
RUN dpkg -i /home/user/sesame_ws/virtualgl_3.1.1_amd64.deb
RUN sed -i '2 i export LD_PRELOAD=\"/usr/lib/libdlfaker.so /usr/lib/libvglfaker.so\"' /home/user/sesame_ws/sesame_demo.sh

COPY extra-files/pal_kitchen.world /home/user/sesame_ws/install/share/pal_gazebo_worlds/worlds/pal_kitchen.world
COPY extra-files/tiago_bringup.launch /home/user/sesame_ws/install/share/tiago_bringup/launch/tiago_bringup.launch
COPY extra-files/multitiago_gazebo_navigation_copy_2.launch /home/user/sesame_ws/multitiago_gazebo_navigation_copy_2.launch

RUN rm -rf /tmp/gazebo-nobody-rtshaderlibcache/

COPY .Xauthority /home/user/
RUN chown ${PAL_USER}:host_group /home/user/.Xauthority
RUN groupadd -g ${VGL_GROUP_ID} vglusers && \
    usermod -a -G vglusers ${PAL_USER}
