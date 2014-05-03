## Introduction

This project is a fork of the [aDORe-djatoka JPEG 2000 image server](http://sourceforge.net/apps/mediawiki/djatoka/index.php?title=Main_Page). It was created to simplify the server's use. Over time additional features (like support for [OpenSeadragon](http://openseadragon.github.io/ "OpenSeadragon User Interface") and the International Image Interoperability Framework's [Image API](http://iiif.io "IIIF's Image API")) were added.

FreeLib-Djatoka can be used on its own or it can be integrated into a third party digital asset management system.

[OpenJDK](http://openjdk.java.net/) or [OracleJDK](http://www.oracle.com/technetwork/java/javase/downloads/index.html) (version 1.7 or later) and [Maven](http://maven.apache.org/) (version 3.x or later) are required to run Freelib-Djatoka. These should be installed prior to running FreeLib-Djatoka.  All the other dependencies are automatically resolved by the project's Maven configuration.<br/><img style="padding-left: 15px; padding-top: 15px;" src="images/djatoka-elephant-normal.png" />

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

    mvn -q jetty:stop
<br/>
**Putting Djatoka into Production**

If you want to run FreeLib-Djatoka in a production Linux environment, you might want to use the project's [init.d script](as-a-service.html).  You can also choose to run the FreeLib-Djatoka server behind an Apache proxy or at port 80 using iptables. Consult the [production options](production-options.html) page for more details.

## Configuring Djatoka

There are a few configuration options available to users of FreeLib-Djatoka.

Setting available system memory can be done via the [init.d script](as-a-service.html) or directly on the command line:

    MAVEN_OPTS="-Xmx2048m" mvn jetty:run-forked

Changing the port Djatoka runs at can be done by changing the 'jetty.port' property in the [pom.xml](https://github.com/ksclarke/freelib-djatoka/blob/master/pom.xml) file; it can also be set on the command line:

    mvn -Djetty.port=9999 jetty:run-forked

There are also some important file system paths that may be reconfigured.  These are found in the [pom.xml](https://github.com/ksclarke/freelib-djatoka/blob/master/pom.xml) file's 'properties' element:

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
          <djatoka.ingest.data>/usr/local/tiffs</djatoka.ingest.data>
          <djatoka.jp2.data>/usr/local/jp2s</djatoka.jp2.data>
          <openurl.cache.dir>/tmp/djatoka-cache</openurl.cache.dir>
          <djatoka.view.cache>/usr/local/ptcache</djatoka.view.cache>
        </properties>
      </profile>
    </profiles>

This will override the default values _unless_ you pass 'ignoreDjatokaSettings' in on the command line (which you probably won't do); for instance:

    mvn -DignoreDjatokaSettings=true jetty:run-forked

You'll, of course, want configured locations to exist and for the user running FreeLib-Djatoka to be able to write to them.  It's okay for the location in the djatoka.ingest.data setting to be read-only (to preserve the integrity of the TIFF files).

For what it's worth, you'll also want to include a 'jetty.stop.key' property in your settings.xml file; for instance:

    <jetty.stop.key>YOUR_TEXT_HERE</jetty.stop.key>

This is a secret key that Maven uses to shutdown a running Djatoka server.  There is a default, but it's more secure to change the value on your server to something unique.

The last configuration option worth mentioning is the color space setting.  FreeLib-Djatoka assumes all images have been encoded with the same color space.  The supported color spaces are "", "sRGB", "sLUM", "sYCC", "iccLUM" and "iccRGB". The one that Djatoka will use is configured in the following property (what's shown is the default; it can be overridden, if needed, in the settings.xml file):

    <djatoka.ingest.color.space>
      sRGB
    </djatoka.ingest.color.space>

In the future, FreeLib-Djatoka will take a more dynamic approach to working with color spaces, but the above represents the current state.
<br/><br/>

## Ingesting Images

Once you have FreeLib-Djatoka up and running, probably the next thing you'll want to do is to ingest some images.
<br/><br/>
**CSV Imports**

Perhaps the easiest method, at the moment, is to use the Djatoka CSV ingest script.  Before using it, you'll want to add the following to your settings.xml file (see the "Configuring Djatoka" section for more details about this file):

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

Normally, when Djatoka gets a URL as the identifier, it tries to resolve the URL.  In the case where that URL references a JP2 file, FreeLib-Djatoka will check whether the supplied URL conforms to a known regular expression pattern.  Using that pattern, FreeLib-Djatoka can discern the unique ID of the JP2 and use that to load it into the local Pairtree file system.

So, for instance, take the Islandora URL above.  The ID of the JP2 in Islandora is captured by the regular expression: ([a-zA-Z]*(%3A|:)[0-9a-zA-Z]*).  It can then be used as the key to store the image in the local JP2 cache.  You may need to tweak the regular expression used if your IDs have non-alphanumeric characters.  The supplied pattern is just an example.

Likewise, if an image ID is received that FreeLib-Djatoka can't locally resolve, it'll be checked against 'djatoka.ingest.guesses' to see whether its image can be retrieved using one of the known URL patterns (the "{}" in the pattern is replaced with the ID).  Most sites will probably not need a space delimited list of patterns (like in the examples), but that option is available if needed.

For FreeLib-Djatoka, the local Pairtree cache is the preferred source of information.  This is checked first for each request.  Once a JP2 image has been retrieved remotely, it will be available for retrieval the next time from the local Pairtree cache (despite being requested with a URL).  For more information on this, read the [identifier resolver](identifier-resolver.md) page.
<br/><br/>
**Batch Ingests**

You can also configure FreeLib-Djatoka to automatically ingest TIFF images that are placed in the 'djatoka.ingest.data' directory (or any subdirectory of it).  To do this, add the following to your system's crontab:

    0 * * * * curl -s 'http://localhost:8888/ingest?unattended=true' >>/tmp/djatoka-ingest.log 2>&1

This will instruct the ingest script to crawl the specified directory.  If it finds a TIFF image, it will check the name of the file and look for an XML file with the same name. So, for instance, if the image file is named W10025.tif, the script will look for a file named W10025.xml.

If the script finds that file, it will look in it for an XML element named 'id'.  It will take the first of these that it finds and use that value as the image's ID, loading the image into the Pairtree file system using that ID.  If the script doesn't find a corresponding XML file, it will load the image into the Pairtree file system using its file system path as the identifier.

Without an explicit ID, a file with the path "tickets/ms0332_gra_21350", will be put into the Pairtree file system with the identifier: \-\-%2Ftickets%2Fms0332_gra_21350.  All directory levels are escaped with "%2F" and the path identifier is prefixed with "\-\-%2F" (to distinguish it from images that are loaded with an explicit identifier).

The logs for this ingest process are written to: target/logs/ingest* (or /var/log/djatoka/ingest* if you are using the init.d script).

## Questions

If you give FreeLib-Djatoka a try, feel free to share your experiences with it.  If you find something about the project (or its documentation) that can be improved (or is just plain broken), [open an issue](https://github.com/ksclarke/freelib-djatoka/issues?state=open) in the issue queue. If filing an issue seems too formal, you can also just [send an email](http://projects.freelibrary.info/freelib-djatoka/team-list.html) with your experiences and/or questions.

## Caching OpenSeadragon Tiles

The intended user interface for FreeLib-Djatoka is [OpenSeadragon](http://openseadragon.github.io/), which uses tiles to provide a zoomable interface.  To improve performance, FreeLib-Djatoka allows these tiles to be pre-generated and cached.  To accomplish this, a caching plugin is provided.  To use the plugin (like with the CSV ingest script above), the 'info.freelibrary' pluginGroup must be added to the settings.xml file.

After that, running the plugin is as easy as starting FreeLib-Djatoka and running the below:

    mvn djatoka:cache-tiles
    
The plugin will scan all the JP2 files in the server's pairtree directory structure and generate tiles for each one.  The number of tiles generated depends on the height and width of the JP2 image being tiled.  Note that Djatoka needs to be running for the tiles to be generated (unlike with the CSV import plugin).  There is also a plugin that can clean the cache of the tiles for a particular image or for all the cached images.

To run this plugin, type:

    mvn djatoka:clean-cache

or

    mvn djatoka:clean-cache -Did=SOME_ID
    
The first will clean all tiles from the cache and the second will clean just the tiles associated with the supplied ID.  Rerunning the tile cacher will not overwrite previously generated tiles.  To create new tiles, the old ones must first be removed.