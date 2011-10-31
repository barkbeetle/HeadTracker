#! /bin/msh

# Go into the directory of the application.
P=`readlink -f "$0"`
cd "`dirname "$P"`" || exit $?

cp inetd.conf /etc
killall inetd
