# FreeLib-Djatoka [![Build Status](https://travis-ci.org/ksclarke/freelib-djatoka.png?branch=master)](https://travis-ci.org/ksclarke/freelib-djatoka)

This project is a fork of the aDORe-djatoka JP2 image server.  It was created to simplify the image server's use.  In the process, additional caching and image management options were added.  The OpenSeadragon UI was also added as the project's preferred user interface (see the default page for an example of its use).  FreeLib-Djatoka can be used on its own or it can be integrated into an external digital asset management system.  It's currently tested against Islandora.

OpenJDK (or OracleJDK), 1.7 or later, and Maven, 3.x or later, are required to run the Freelib-Djatoka server.

### Getting Started

To run freelib-djatoka, just [download](https://github.com/ksclarke/freelib-djatoka/archive/master.zip) (and unzip) or clone the project:

    git clone https://github.com/ksclarke/freelib-djatoka.git

Change into the project's base directory:

    cd freelib-djatoka

And run Maven by typing:

    mvn jetty:run-forked

That's it.  You should then be able to go to the freelib-djatoka test page to confirm that the server is up:

    http://localhost:8888

When you want to stop the server, type the following from within the project's base directory:

    mvn jetty:stop

### Image Ingest

FreeLib-Djatoka can load its images in two different ways.  The first is by loading TIFF images from the file system (and converting them into JP2s).  The second is by loading images on-demand, as they are requested through the Web interface.  Images loaded on-demand can either be images to convert into JP2s or JP2s that are ready to be served in the OpenSeadragon interface (like JP2s from Islandora).

There are four configuration options related to image ingest in the project's pom.xml file.  To override these, it's best to create a new (or modify an existing) settings.xml file and assign new values to the properties in there.  Details about Maven's [settings.xml file](http://maven.apache.org/settings.html "Maven's settings.xml file") can be found on the Maven website.  The properties that should be added to the settings.xml file are:

	<djatoka.ingest.data>/usr/local/data/images/tiffs</djatoka.ingest.data>
	<djatoka.jp2.data>/usr/local/data/images/jp2s</djatoka.jp2.data>
	<openurl.cache.dir>/usr/local/data/images/tmpcache</openurl.cache.dir>
	<djatoka.view.cache>/usr/local/data/images/cache</djatoka.view.cache>

The first directory is for TIFF images that will be loaded in batch mode.  The second directory is for converted JP2s (FreeLib-Djatoka writes to this directory).  The next two directories are two different types of caches.  The first of these is for aDORe-djatoka's LRU cache and the second is for a permanent PairTree file system cache.  With the OpenSeadragon interface, there are a fixed number of tiles that need to be generated for any given image.  These can be pre-generated and permanently stored in the PairTree file system.

For the TIFF file system ingest, a batch ingest script is run when the image server is started.  It can also be run periodically by configuring a cron job to hit the /ingest URL.  This process can also be triggered by a person.  When run from a cron job, the parameter "unattended" should be passed in (e.g., http://localhost:8888/ingest?unattended or http://localhost:8888/ingest?unattended=true).  When run by a person (without the "unattended" parameter), an update on the ingest process can be retrieved by revisiting the /ingest page.  Using this method, the URL should be checked until the "Finished: # ingested (# MB available on the disk)" message is displayed (at which point the session will be closed).

When files are ingested from the file system, they are put into the PairTree file system (just like files which are ingested with an identifier).  For these files, their path is used as an identifier in the PairTree file system.  These identifiers are prefixed with a "--" to distinguish them from files that are ingested with an explicit identifier.

All identifiers (implicit or explicit) must be URL encoded.  So, for instance, a file named "ms0332\_gra\_21503" in the root directory of the TIFF folder would be accessed as a standard sized image from the browser using the path: "http://localhost:8888/view/image/--%2Fms0332\_gra\_21503".  A file with the file system path of "tickets/ms0332\_gra\_21350" would be accessed as a standard sized image from the browser using the path: "http://localhost:8888/view/image/--%2Ftickets%2Fms0332\_gra\_21350".

Files can also be ingested from the file system with an explicit identifier.  There is an ingest script that makes this easier.  This script will take a CSV file with TIFF image paths and identifiers and convert the TIFFs into JP2s.  It then copies the JP2s into the PairTree structure using the identifiers.  The format of the CSV file (using ARKs as example identifiers) is:

    "/usr/local/data/images/tiffs/HERB GREENE/ms0334_pho_0031.tif","ark:/38305/g4bp04wm/is/1"
    "/usr/local/data/images/tiffs/HERB GREENE/ms0334_pho_0032.tif","ark:/38305/g46w9d66/is/1"
    "/usr/local/data/images/tiffs/HERB GREENE/ms0334_pho_0165.tif","ark:/38305/g4b27xd9/is/1"
    "/usr/local/data/images/tiffs/HERB GREENE/ms0334_pho_0029.tif","ark:/38305/g4m32xv2/is/1"

The script to run the ingest process is:

    mvn exec:java -Dexec.mainClass="org.gdao.metadata.FileCopy" \
    -Dexec.args="/usr/local/data/ARK_MAP_2.csv /usr/local/data/images/jp2s"

It should be run from within the project's base directory.  If the CSV file contains references to TIFFs in the `djatoka.ingest.data` directory, and the batch ingest script is also run, JP2s will exist in the PairTree structure in two different locations: at their path based identifier and at their file supplied identifier.  There is no problem with this; it's just worth mentioning... It's also worth mentioning that neither the batch ingest nor the ingest script need to be used in order to use FreeLib-Djatoka.  The other option is to use the on-demand image ingest.

The on-demand image ingest is an extension of the file loading mechanism that was built in to the original aDORe-djatoka.  By giving aDORe-djatoka an HTTP protocol URL, images (e.g., JPEG, PNG, JP2, etc.) could be loaded into the image server and then served up through subsequent requests.  This is still true with FreeLib-Djatoka, but an extra identifier parsing mechanism has been added to optionally store these JP2 images in the PairTree structure.  This is accomplished through two configuration options in the pom.xml file:

    <!-- List of image sources (used when a URL is passed in) -->
    <djatoka.ingest.sources>^http://localhost/islandora/object/([a-zA-Z]*(%3A|:)[0-9a-zA-Z]*)/datastream/JP2/view.*$
    ^http://memory.loc.gov/gmd/gmd433/g4330/g4330/([a-z0-9A-Z]*).jp2$</djatoka.ingest.sources>

    <!-- List of image source guesses (used when ID can't be otherwise resolved) -->
    <djatoka.ingest.guesses>http://localhost/islandora/object/{}/datastream/JP2/view
    http://memory.loc.gov/gmd/gmd433/g4330/g4330/{}.jp2</djatoka.ingest.guesses>

Like the other configuration options above, it's recommended to override these in a settings.xml file.

What these two configuration options do is provide a way to extract identifiers from external systems that expose them through URL patterns.  So, in the examples above, Islandora object IDs are embedded in the URL.  The first, `djatoka.ingest.sources`, takes a URL with an embedded regular expression representing the identifier.  When a resolvable URL that matches the pattern is supplied to FreeLib-Djatoka, the identifier is extracted as passed into download process so that the JP2 can be stored in the PairTree file system using that identifier.  Subsequent requests to FreeLib-Djatoka can then be made using the identifier.

The second configuration option will check incoming identifiers against known URL patterns.  They are first checked against identifiers in the system, of course, but if they can't be found they are then checked against known URL patterns in the hope that a resolvable image will be found (and can be downloaded and stored).

Both `djatoka.ingest.sources` and `djatoka.ingest.guesses` can take multiple, whitespace-delimited patterns.  The first configuration option includes a regular expression pattern and the second includes a simple pair of curly braces (e.g., {}) where the identifier would be found.  These patterns are only really useful if the embedded identifiers are unique.  I don't think, for instance, that the LoC patterns above represent standalone identifiers, but it's included as djatoka's standard test item.

### Tile Cache Utilities

Something that I added for the GDAO project but never documented was the tile pre-generation utility.  It can be run from the command line to pre-generate tiles for images listed in a CSV file.  You pass in the CSV file containing the IDs of the images (and titles, etc.), and the OpenSeadragon tiles will be auto-magically generated for you.  Run it by typing:

    mvn exec:java -Dexec.mainClass="info.freelibrary.djatoka.TileCache" \
    -Dexec.args="src/test/resources/id_map.csv 2"

The "exec.args" would of course be changed to your CSV file and the position of the ID in the (1-based) column order (i.e., start counting columns at 1 not 0) of the CSV file.

### Running freelib-djatoka at Port 80

By default, FreeLib-Djatoka runs at port 8888.  Because it can handle cross origin requests, it doesn't need to be put behind an Apache proxy.  If it needs to be run at port 80 for institutional firewall or other reasons, this can be accomplished on a Linux system through ipchains or iptables:

    /sbin/ipchains -I input --proto TCP --dport 80 -j REDIRECT 8888

or

    /sbin/iptables -t nat -I PREROUTING -p tcp --dport 80 -j REDIRECT --to-port 8888

If you are on a Windows system, you can configure the Jetty server that runs FreeLib-Djatoka to run at port 80 using the SetUID method documented at:

    http://www.eclipse.org/jetty/documentation/current/setting-port80-access.html

### Future Directions

With all the talk of the PairTree file system above, I should note that FreeLib-Djatoka should also work as a straight aDORe-djatoka replacement.  The original intention of the project was just to make the djatoka server easier to use (and more resistant to crashes and other performance issues).  The project's direction then shifted to providing a different RESTful API from the OpenURL one that aDORe-djatoka provides.  The longer term goal is to have FreeLib-Djatoka support the [International Image Interoperability Framework (IIIF)](http://iiif.io/ "International Image Interoperability Framework"), use the [OpenJPEG JP2 library](http://www.openjpeg.org/ "OpenJPEG"), and also have an easy to use administrative Web interface for managing TIFF and JP2 images.

### License

This package, freelib-djatoka, like its upstream project, adore-djatoka, is available under the LGPL license (version 2.1 or later).

It contains the Kakadu binaries which are provided under a NON-COMMERCIAL USE ONLY license.  See the [adore-djatoka page](http://djatoka.sourceforge.net/ "The aDORe-djatoka Home Page") for more details.

### Contact

If you have questions about the freelib-djatoka project, feel free to <a href="mailto:ksclarke@gmail.com">email me</a>, Kevin S. Clarke, or to [file an issue](https://github.com/ksclarke/freelib-djatoka/issues "GitHub Issues Queue") in the GitHub issues queue.
