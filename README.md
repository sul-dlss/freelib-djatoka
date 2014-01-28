# FreeLib-Djatoka [![Build Status](https://travis-ci.org/ksclarke/freelib-djatoka.png?branch=master)](https://travis-ci.org/ksclarke/freelib-djatoka)

This project is a fork of the [aDORe-djatoka JPEG 2000 image server](http://sourceforge.net/apps/mediawiki/djatoka/index.php?title=Main_Page).  It was originally created to simplify the image server's use. Over time, additional features were added and older code was cleaned up.  For detailed information about the project, please visit its [project page](http://projects.freelibrary.info/freelib-djatoka).  This README contains just the basic [TL;DR](http://en.wiktionary.org/wiki/TLDR) project information.

### Getting Started

OpenJDK or OracleJDK (1.7 or later) and Maven (3.x or later) are required to run the Freelib-Djatoka server.  Please install and configure them before proceeding with the FreeLib-Djatoka installation.

To install and run freelib-djatoka, [download](https://github.com/ksclarke/freelib-djatoka/archive/master.zip) (and unzip) or clone the project:

    git clone https://github.com/ksclarke/freelib-djatoka.git

Change into the project's base directory:

    cd freelib-djatoka

And run Maven by typing:

    mvn jetty:run-forked

That's it.  You should then be able to go to the freelib-djatoka test page to confirm that the server is up:

    http://localhost:8888

When you want to stop the server, type the following from within the project's base directory:

    mvn jetty:stop

### Using FreeLib-Djatoka with Islandora

Islandora's OpenSeadragon module can be configured to work with a remote FreeLib-Djatoka instance (it doesn't need to be proxied by the same Apache that serves Islandora).  For use with Islandora, it's advisable to change two properties in the FreeLib-Djatoka pom.xml file (or use a Maven settings.xml file to override the pom's values -- see the [project page](http://projects.freelibrary.info/freelib-djatoka) for more details).

The properties to change are:

    <!-- Image sources checked when a URL is passed in -->
    <djatoka.ingest.sources>
        ^http://YOUR_HOST/islandora/object/([a-zA-Z]*(%3A|:)[0-9a-zA-Z]*)/datastream/JP2/view.*$
    </djatoka.ingest.sources>
    <!-- Image sources to try when an ID can't be resolved -->
    <djatoka.ingest.guesses>
        http://YOUR_HOST/islandora/object/{}/datastream/JP2/view
    </djatoka.ingest.guesses>

After configuring Islandora's OpenSeadragon module, and making the changes to the properties above, you should be in business.  There are, of course, other configuration options documented on Djatoka's project page if you want to delve deeper. Feel free to ask me any questions that arise.

### License

This package, freelib-djatoka, like its upstream project, aDORe-djatoka, is available under the LGPL license.

This package, also like its upstream project, contains the Kakadu binaries which are provided under a NON-COMMERCIAL USE ONLY license. 

Per Definition 3 of the Kakadu's Non-Commercial License Agreement:

> 3. The Licensee shall have the right to Deployment of the Kakadu
> software, provided that such Deployment does not result in any
> direct or indirect financial return to the Licensee or any other
> Third Party which further supplies or otherwise uses such
> Applications.  All copies of Applications shall contain notification
> that they were developed using the Kakadu software.

Developers who would like to obtain the Kakadu Software source code modifications used in djatoka will need to purchase a license from Kakadu Software <http://www.kakadusoftware.com/>.  It's worth noting that the licensing of Kakadu has changed since the release that was packaged with the original aDORe-djatoka (and is now repackaged with FreeLib-Djatoka).  It's not know whether the latest license would be compatible with the LGPL or whether the code would even work with Djatoka.

FreeLib-Djatoka plans to incorporate the OpenJPEG JPEG 2000 library in a future release, providing an open source alternative to the proprietary Kakadu library.

### Contact

If you have a question about the FreeLib-Djatoka project, feel free to <a href="mailto:ksclarke@gmail.com">email me</a> or [file an issue](https://github.com/ksclarke/freelib-djatoka/issues "GitHub Issues Queue") in the project's GitHub issues queue.
