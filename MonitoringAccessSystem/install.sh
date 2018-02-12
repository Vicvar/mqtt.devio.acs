#!/bin/bash

LOCAL=$(pwd)
function build_module(){
pwd;
	cd $LOCAL/$ant_module && ant && ant install clean;
}

cp -rf thirparties/*jar $INTROOT/lib;

for ant_module in TMCAgent TMCStats TMCDistributor
do
	build_module
done




