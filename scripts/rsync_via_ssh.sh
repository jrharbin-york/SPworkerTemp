#!/bin/sh
rsync -avz -e "ssh" $1 $2
chmod 700 $2/scripts/*.sh
