### Running as a System Service on Linux

Included with freelib-djatoka is an init.d script that will allow the image server to be started and stopped in the standard Linux way.

The [script](https://github.com/ksclarke/freelib-djatoka/blob/master/etc/init.d/djatoka) assumes:

 1. You've checked out the FreeLib-Djatoka project into /opt (i.e., /opt/freelib-djatoka)
 2. Maven's bin (i.e., mvn) is on the system $PATH
 3. The system's Web user ('apache' on RHEL/CentOS/etc. and 'www-data' on Ubuntu/Debian) has write access to the project
 
 If your system has a different configuration, you will need to adjust the script.  You can also add custom MAVEN_OPTIONS to the script (like increasing the amount of memory available to the server).

For what it's worth, you don't *need* to use this script (especially if you're just kicking the project's tires). It's really just for the case where you have put FreeLib-Djatoka into production.

To install the script, copy it into the system's /etc/init.d directory; to do this, from within the freelib-djatoka project's home directory, type:

    sudo cp etc/init.d/djatoka /etc/init.d/djatoka

Next, you should run update-rc.d to add the djatoka service to the desired system runlevels:

    sudo update-rc.d -f djatoka start 80 2 3 4 5 . stop 30 0 1 6 .

Once this is done, you should be able to start and stop the service in the standard way:

    sudo service djatoka start
    sudo service djatoka status
    sudo service djatoka stop

If you need to, you can remove the service from the system's runlevels by typing:

    sudo update-rc.d -f djatoka remove

For what it's worth, this script also ensures that the djatoka logs in the project's 'target' directory are symlinked from the standard location: /var/log/djatoka

If you are putting freelib-djatoka into production, you might also want to check out the [experimental Nagios script](https://github.com/ksclarke/freelib-djatoka/blob/master/bin/djNagios).  It allows Nagios to check a very simple image server health status.  There is much room for improvement here, but it's a start.