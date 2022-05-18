#!/bin/sh

sudo ps ax | grep EmoldinoGW | awk '{print $1}'| xargs kill

sudo ps ax | grep tail | awk '{print $1}'| xargs kill

exit 0
