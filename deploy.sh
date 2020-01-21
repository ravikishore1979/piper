#!/bin/bash 
# Using bash instead of sh as arrays are supported only in bash
if [ $# != 2 ]; then
   echo "usage: ./deploy.sh <env: local/dev/demo> <apps to deploy: rw/rss/all>"
   exit 1;
fi
localCreds="admin:admin"
devCreds="saparate:SapaRate@8325"
qaCreds="saparate:SapaRate@8325"
demoServerCreds="saparate:SapaRate@8325"
localIP="192.168.56.101:8080"
devIP="3.232.119.28:8080"
qaIP="52.7.53.120:8080"
demoServerIP1="52.203.169.98:8080"
demoServerIP2="x.x.x.x:8080"
rwLoc="/d/repos/piper"
rssLoc="/d/repos/saparate"
case $1 in
  local)
	creds=$localCreds
	ipToDeploy=($localIP)
	;;
  dev)
	creds=$devCreds
	ipToDeploy=($devIP)
	;;
  demo)
	creds=$demoServerCreds
	ipToDeploy=($demoServerIP1)
	;;
  qa)
	creds=$qaCreds
	ipToDeploy=($qaIP)
	;;
esac

for ipi in "${ipToDeploy[@]}"
do
    set -x
    if [ $2 = 'rw' ]; then
       curl -u $creds "http://$ipi/manager/text/undeploy?path=/rateworkflow" || exit 1
       curl -u $creds "http://$ipi/manager/text/deploy?path=/rateworkflow&update=true" --upload-file $rwLoc/target/rateworkflow.war  || exit 1
    elif [ $2 = 'rss' ]; then
       curl -u $creds "http://$ipi/manager/text/undeploy?path=/saparate" || exit 1
       curl -u $creds "http://$ipi/manager/text/deploy?path=/saparate&update=trueE" --upload-file $rssLoc/rate-server/target/saparate.war || exit 1
    else
       curl -u $creds "http://$ipi/manager/text/undeploy?path=/rateworkflow" || exit 1
       curl -u $creds "http://$ipi/manager/text/undeploy?path=/saparate" || exit 1
       curl -u $creds "http://$ipi/manager/text/deploy?path=/rateworkflow&update=true" --upload-file $rwLoc/target/rateworkflow.war || exit 1
       curl -u $creds "http://$ipi/manager/text/deploy?path=/saparate&update=true" --upload-file $rssLoc/rate-server/target/saparate.war || exit 1
    fi
    set +x
done

