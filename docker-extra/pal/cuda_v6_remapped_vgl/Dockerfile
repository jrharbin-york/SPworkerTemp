FROM cuda_v6_remapped_scanout

ARG VGL_GROUP_ID=1002
ARG PAL_USER=user

RUN apt-get update
RUN apt-get install psmisc
COPY extra-files/virtualgl_3.1.1_amd64.deb /home/user/sesame_ws/
RUN dpkg -i /home/user/sesame_ws/virtualgl_3.1.1_amd64.deb
RUN sed -i '2 i export LD_PRELOAD=\"/usr/lib/libdlfaker.so /usr/lib/libvglfaker.so\"' /home/user/sesame_ws/sesame_demo.sh

RUN groupadd -g ${VGL_GROUP_ID} vglusers && \
    usermod -a -G vglusers ${PAL_USER}
