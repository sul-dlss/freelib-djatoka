## Frequently Asked Questions about FreeLib-Djatoka

* [I get a NoPluginFoundForPrefixException.  What can I do about it?](faqs.html#NoPluginFoundForPrefixException)
* [What does the warning "Ingest image's size is larger than allowed" mean?](faqs.html#ingest-max-size)
* [My images have linear artifacts in them.  What can I do about it?](faqs.html#moire-pattern)
* [I have a question not covered by the FAQs.  Where can I get help?](faqs.html#mailing-list)

### I get a NoPluginFoundForPrefixException.  What can I do about it?

<a name="NoPluginFoundForPrefixException" />A NoPluginFoundForPrefixException is a standard Maven exception.  There are [several things to investigate](https://cwiki.apache.org/confluence/display/MAVEN/NoPluginFoundForPrefixException) when you get a NoPluginFoundForPrefixException, but the first thing to check is that you have included the pluginGroup configuration in your settings.xml file before you try to run one of the FreeLib-Djatoka scripts (djatoka:ingest, djatoka:clean-cache, djatoka:cache-tiles, djatoka:resampled-ingest, etc.)  Your settings.xml file should include only one pluginGroups element with at least the following pluginGroup:

    <pluginGroups>
      <pluginGroup>info.freelibrary</pluginGroup>
    </pluginGroups>

An [example settings.xml](https://github.com/ksclarke/freelib-djatoka/blob/master/src/main/resources/sample-settings.xml) file is available in FreeLib-Djatoka's GitHub repository. To copy it into your Maven conf directory, first rename the file from sample-settings.xml to settings.xml.  To learn more about Maven's settings.xml file, [consult the settings.xml documentation](https://maven.apache.org/settings.html).

### What does the warning "Ingest image's size is larger than allowed" mean?

<a name="ingest-max-size" />There is a configuration option that allows you to set a maximum file size for images to be ingested.  This is to prevent the program from running out of memory while trying to convert a large TIFF file into a JP2.  The value is configurable in the pom.xml file (via the djatoka.ingest.maxSize element).  The default value is 900 MB.  It can be adjusted up or down, depending on the system's available RAM.  The value should be an integer representing the maximum number of megabytes for a file to be ingested.  Like all values in the pom.xml file, it's suggested that the value be overridden in a profile in Maven's [settings.xml](https://maven.apache.org/settings.html) file.

### My images have linear artifacts in them.  What can I do about it?

<a name="moire-pattern" />A [moiré pattern](https://en.wikipedia.org/wiki/Moir%C3%A9_pattern) can occur when printed halftone images are scanned.  The pattern appears as lines, or a shifting pattern, in the resulting image files.  These patterns may not be visible in the archival TIFF files, but may still appear in the derivatives created by FreeLib-Djatoka.  The image zooming interface of the OpenSeadragon viewer can even seem to animate the patterns as the user interacts with the images through the viewer.

There are different ways to address the problem.  Many image scanners provide a "descreen" filter to remove these artifacts at the point of scanning.  There are also manual methods that an image specialist can use to clean up the patterns.  For an automated approach, there are two simple actions that may reduce the problem.  The first is to tilt the image slightly (this can also be done at the point of scanning); the second is to resample the image.

FreeLib-Djatoka provides a CSV ingest script that will resample an image before ingesting it.  The format of the CSV file is the same as the format used by the standard CSV ingest script (and the pluginGroup must be included in the settings.xml file just like with the standard CSV script).  The only difference is the command used:

    mvn djatoka:resampled-ingest -Dcsv.file=src/test/resources/id_map.csv

The default is to resample by 10 pixels.  There is a parameter that can be passed in to change that default value though:

    mvn djatoka:resampled-ingest -Dpixels=5 -Dcsv.file=src/test/resources/id_map.csv

This will degrade the quality of the image slightly, but it should provide a semi-automated way to deal with moiré patterns for places without an in-house image specialist.

### I have a question not covered by the FAQs.  Where can I get help?

<a name="mailing-list" />There is now a (fledgling) [mailing list](https://groups.google.com/forum/#!forum/freelibrary-projects) for all FreeLibrary Projects.  Feel free to ask any FreeLib-Djatoka related questions there.  Asking your questions there will also allow others to benefit from your experience (after all, if you have a problem with FreeLib-Djatoka it's likely that others do too).