#!/bin/sh
rsync -avz -e "ssh -p $3" $1 $2

