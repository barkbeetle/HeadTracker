#! /bin/msh

IFS= read i

/sbin/netviewd -s "$i"
