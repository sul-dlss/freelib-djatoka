## Introduction

This project is a fork of the [aDORe-djatoka JPEG 2000 image server](http://sourceforge.net/apps/mediawiki/djatoka/index.php?title=Main_Page). It was created to simplify the server's use. Over time, additional features (like support for [OpenSeadragon](http://openseadragon.github.io/) and the [International Image Interoperability Framework (IIIF)'s Image API](http://iiif.io) were added.  A sample server (running on a small AWS instance) is available at [djatoka.freelibrary.info](http://djatoka.freelibrary.info).

While FreeLib-Djatoka is a fork, it is not 100% backwards-compatible with aDORe-djatoka.  The main difference is the [identifier resolver](identifier-resolver.md); it's no longer a pluggable class, but one whose behavior is defined. It's also worth noting that support for the OpenURL API has been deprecated. It will be removed in a future version. 

FreeLib-Djatoka can be used on its own or it can be integrated into an external digital asset management system. It's currently tested against [Islandora](http://islandora.ca/).

Note that [OpenJDK](http://openjdk.java.net/) or [OracleJDK](http://www.oracle.com/technetwork/java/javase/downloads/index.html) (version 1.7 or later) and [Maven](http://maven.apache.org/) (version 3.x or later) are required to run Freelib-Djatoka. These should be installed prior to running FreeLib-Djatoka.  All the other dependencies are automatically resolved by the project's Maven configuration.

The original [aDORe-djatoka docs](http://sourceforge.net/apps/mediawiki/djatoka/index.php?title=Main_Page) are still available at that project's Sourceforge site.  The documentation on this site supersedes the original project's documentation.  Please [open an issue](https://github.com/ksclarke/freelib-djatoka/issues?state=open) if you find an area of this site's documentation that needs improvement.
<br/><br/>
_***Project Administrivia:*** The 'djatoka' in aDORe-djatoka is pronounced J-2-K (like the JPEG 2000 extension). The 'djatoka' in FreeLib-Djatoka is pronounced dja-TOE-kuh (similar to Dakota). But, don't worry if you mistakenly call it the Dakota Image Server... it's a real tongue twister._

## Getting Started

To run FreeLib-Djatoka, just [download](https://github.com/ksclarke/freelib-djatoka/archive/master.zip) (and unzip) or clone the project:

    git clone https://github.com/ksclarke/freelib-djatoka.git

Change into the project's base directory and install FreeLib-Djatoka:

    cd freelib-djatoka
    mvn install

Then start the server by typing:

    mvn jetty:run-forked

That's it. You should then be able to go to the FreeLib-Djatoka test page to confirm that the server is up:

    http://localhost:8888

When you want to stop the server, type the following from within the project's base directory:

    mvn jetty:stop
<br/>
**Putting Djatoka into Production**

If you want to run FreeLib-Djatoka in a production Linux environment, you might want to use the project's [init.d script](as-a-service.html).  You can also choose to run the FreeLib-Djatoka server behind an Apache proxy or at port 80 using iptables. Consult the [production options](production-options.html) page for more details.

## Configuring Djatoka

There are a few configuration options available to users of FreeLib-Djatoka.

Setting available system memory can be done via the [init.d script](as-a-service.html) or directly on the command line:

    MAVEN_OPTS=-Xmx2048m mvn jetty:run-forked

Changing the port Djatoka runs at can be done by changing the `jetty.port` property in the [pom.xml](https://github.com/ksclarke/freelib-djatoka/blob/master/pom.xml) file; it can also be set on the command line:

    mvn -Djetty.port=9999 jetty:run-forked

There are also some important file system paths that may be reconfigured.  These are found in the [pom.xml](https://github.com/ksclarke/freelib-djatoka/blob/master/pom.xml) file's `properties` element:

    <!-- The source image (TIFFs, for instance) file system -->
    <djatoka.ingest.data>
      ${basedir}/src/test/resources/images
    </djatoka.ingest.data>
    
    <!-- The source JP2 images (in a Pairtree file system) -->
    <djatoka.jp2.data>
      ${basedir}/target/images
    </djatoka.jp2.data>
    
    <!-- The OpenURL interface's LRU cache location -->
    <openurl.cache.dir>
      ${basedir}/target/tmpcache
    </openurl.cache.dir>
    
    <!-- The permanent cache of the JP2 tile derivatives -->
    <djatoka.view.cache>
      ${basedir}/target/cache
    </djatoka.view.cache>

The _first_ is the directory that contains TIFF images to be converted into JP2s.  The _second_ is where JP2s (that have been converted from the files in the first location) are stored.  You can also load native JP2s into this location.

The _third_ is the place where the original aDORe-djatoka [LRU cache](http://en.wikipedia.org/wiki/Cache_algorithms#Least_Recently_Used) lives.  The _fourth_ is where derivatives of the JP2s (JPEG images, for instance) are permanently stored.  Both the _second_ and the _fourth_ contain a [Pairtree](https://wiki.ucop.edu/display/Curation/PairTree) file system.

As stated above, these properties _can_ be changed in the pom.xml file, but it's really better to take advantage of [Maven's Settings configuration](http://maven.apache.org/settings.html).

Using a settings.xml file (that lives outside of the project directory) will allow you to override pom.xml's values without actually changing the file.

For example, you might put the following in your system's settings.xml file:

    <profiles>
      <profile>
        <id>djatoka-settings</id>
        <activation>
          <property>
            <name>!ignoreDjatokaSettings</name>
          </property>
        </activation>
        <properties>
          <djatoka.ingest.data>
            /usr/local/tiffs
          </djatoka.ingest.data>
          <djatoka.jp2.data>
            /usr/local/jp2s
          </djatoka.jp2.data>
          <openurl.cache.dir>
            /tmp/djatoka-cache
          </openurl.cache.dir>
          <djatoka.view.cache>
            /usr/local/djatoka/cache
          </djatoka.view.cache>
        </properties>
      </profile>
    </profiles>

This will override the default values _unless_ you pass `ignoreDjatokaSettings` in on the command line (which you probably won't do); for instance:

    mvn -DignoreDjatokaSettings=true jetty:run-forked

For what it's worth, you might also want to include a `jetty.stop.key` property in your external settings.xml file; for instance:

    <jetty.stop.key>YOUR_TEXT_HERE</jetty.stop.key>

This is a secret key that Maven uses to shutdown a running Djatoka server.  There is a default, but it's more secure to change the value on your server to something unique.

The last configuration option worth mentioning is the color space setting.  FreeLib-Djatoka assumes all images have been encoded with the same color space.  The supported color spaces are "", "sRGB", "sLUM", "sYCC", "iccLUM" and "iccRGB". The one that Djatoka will use is configured in the following property (what's shown is the default; it can be overridden, if needed, in the settings.xml file):

    <djatoka.ingest.color.space>
      sRGB
    </djatoka.ingest.color.space>

In the future, FreeLib-Djatoka will take a more dynamic approach to working with color spaces, but the above represents the current state.
<br/><br/>
**Configuration Questions**

Please feel free to [submit an issue](https://github.com/ksclarke/freelib-djatoka/issues?state=open) if you have a question about any of the configuration options.  There is definitely room for improvement in the code and in the documentation. Getting tickets will help highlight the areas where improvement is needed.

## Ingesting Images

Once you have FreeLib-Djatoka up and running, probably the next thing you'll want to do is to ingest some images.  There are several ways to do this.
<br/><br/>
**CSV Import**

Perhaps the easiest method, at the moment, is to use the Djatoka Ingest script.  Before using it, you'll want to add the following to your settings.xml file (see the "Configuring Djatoka" section for more details about this file):

    <pluginGroups>
      <pluginGroup>info.freelibrary</pluginGroup>
    </pluginGroups>

Once you've done that (and started Djatoka), you can load images using a spreadsheet.  To do this, from within the project directory, type:

    mvn djatoka:ingest -Dcsv.file=src/test/resources/id_map.csv

The path to the CSV file can be absolute or relative to the project directory.  By default, the plugin expects the file system path to the image to be in the first column and the identifier of the image to be in the second.

This can be reconfigured if you have a spreadsheet with different column assignments; for instance, the opposite of the default assumption would be:

    mvn djatoka:ingest -Dcsv.file=src/test/resources/id_map.csv -Dcsv.id=0 -Dcsv.path=1

The column-count is zero-based rather than one-based.  So the "first" column is actually column 0.  The "second" column would be column 1.

If the CSV file contains TIFF images (rather than JP2 images), they will be converted to JP2s first and then loaded into Djatoka's Pairtree file system.
<br/><br/>
**Unattended Batch Ingest**

You can also configure FreeLib-Djatoka to automatically ingest TIFF images that are placed in the `djatoka.ingest.data` directory (or any subdirectory of it).  To do this, add something like the following to your system's crontab:

    0 * * * * curl -s 'http://localhost:8888/ingest?unattended=true' >>/tmp/djatoka-ingest.log 2>&1

This will instruct the ingest script to crawl the specified directory.  If it finds a TIFF image, it will check the name of the file and look for an XML file with the same name. So, for instance, if the image file is named W10025.tif, the script will look for a file named W10025.xml.

If the script finds that file, it will look in it for an XML element named `id`.  It will take the first of these that it finds and use that value as the image's ID, loading the image into the Pairtree file system using that ID.  If the script doesn't find a corresponding XML file, it will load the image into the Pairtree file system using its file system path as the identifier.

So, for instance, a file with the path "tickets/ms0332_gra_21350" (relative to the `djatoka.ingest.data` directory), will be put into the Pairtree file system with the identifier: \-\-%2Ftickets%2Fms0332_gra_21350.  All directory levels are escaped with "%2F" and the identifier string is prefixed with "\-\-%2F" (to distinguish it from images that are loaded with an explicit identifier).

The logs for this ingest process are written to: `target/logs/ingest*` (or `/var/log/djatoka/ingest*` if you are using the init.d script).
<br/><br/>
**Attended Batch Ingest**

The batch process can also be watched by an individual; it can be manually triggered by visiting the following URL:

    http://localhost:8888/ingest

If triggered by an individual, the URL should be refreshed until the message on the screen indicates the job is done.
<br/><br/>
**Dynamically Loaded Images**

The original aDORe-djatoka identifier resolver has a method of loading JP2 images on demand.  FreeLib-Djatoka has enhanced this method to take advantage of its permanent Pairtree file system cache.  This approach uses two configuration options in the pom.xml file (which, like others, can be overridden in an external settings.xml file):

    <!-- Image sources checked when a URL is passed in -->
    <djatoka.ingest.sources>
        ^http://localhost/islandora/object/([a-zA-Z]*(%3A|:)[0-9a-zA-Z]*)/datastream/JP2/view.*$
        ^http://memory.loc.gov/gmd/gmd433/g4330/g4330/([a-z0-9A-Z]*).jp2$
    </djatoka.ingest.sources>
    <!-- Image sources to try when an ID can't be resolved -->
    <djatoka.ingest.guesses>
        http://localhost/islandora/object/{}/datastream/JP2/view
        http://memory.loc.gov/gmd/gmd433/g4330/g4330/{}.jp2
    </djatoka.ingest.guesses>

Normally, when Djatoka gets a URL as the identifier, it tries to resolve the URL.  In the case where that URL references a JP2 file, FreeLib-Djatoka will check whether the supplied URL conforms to a known regular expression pattern.  Using that pattern, FreeLib-Djatoka can discern the true ID of the JP2 and use that to load it into the local Pairtree file system.

So, for instance, take the Islandora URL above.  The ID of the JP2 is captured by the regular expression: `([a-zA-Z]*(%3A|:)[0-9a-zA-Z]*)`.  It can then be used as the key to store the image in the local JP2 cache.

Likewise, if an image ID is received that FreeLib-Djatoka can't locally resolve, it'll be checked against `djatoka.ingest.guesses` to see whether its image can be retrieved using one of the known URL patterns (the "{}" in the pattern is replaced with the ID).  Most sites will probably not need a space delimited list of patterns, but that option is available if needed.

For FreeLib-Djatoka, the local Pairtree cache is the ultimate source of information.  This is checked first for each request.  Once a JP2 image has been retrieved remotely the first time, it will be available for retrieval from the local Pairtree cache (despite being requested with a URL) for every time thereafter.  For more information on this, read the [identifier resolver](identifier-resolver.md) page.