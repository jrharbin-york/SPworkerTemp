#!/bin/sh
rsync -avz -e "ssh -p $3" $1 $2
chmod 700 $2/scripts/*.sh
