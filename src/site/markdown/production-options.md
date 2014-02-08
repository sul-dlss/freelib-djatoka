## Production Options

### Running at Port 80

By default, FreeLib-Djatoka runs at port 8888.  Because it can handle cross origin requests, it doesn't need to be put behind an Apache proxy.  However if running at port 80 is desirable, this can be accomplished on Linux with iptables:

    /sbin/iptables -t nat -I PREROUTING -p tcp --dport 80 -j REDIRECT --to-port 8888

If you are on a Windows system, you can configure the Jetty server that runs FreeLib-Djatoka to run at port 80 using Jetty's [SetUID method](http://www.eclipse.org/jetty/documentation/current/setting-port80-access.html#configuring-jetty-setuid-feature).

### Running Behind Apache

If putting FreeLib-Djatoka behind Apache is desired (perhaps to take advantage of Apache's caching module), the following Apache mod_proxy configuration is recommended:

    AllowEncodedSlashes On
    ProxyPreserveHost On
    ProxyRequests Off
    
    ProxyPass / http://localhost:8888/ nocanon
    ProxyPassReverse / http://localhost:8888/ nocanon

If you've changed FreeLib-Djatoka's port you'll, of course, need to change the above port.

### Running Behind Varnish

[Contribute your Varnish configuration here!]