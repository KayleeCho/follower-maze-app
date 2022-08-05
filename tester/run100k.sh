#!/bin/bash -ex

this_dir=$(dirname $0)
time totalEvents=100000 java -server -Xmx1G -jar $this_dir/follower-maze-2.0.jar
