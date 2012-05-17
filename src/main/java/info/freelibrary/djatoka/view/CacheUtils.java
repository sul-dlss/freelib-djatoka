package info.freelibrary.djatoka.view;

public class CacheUtils {

    public static final String getFileName(String aLevel, String aScale,
	    String aRegion) {
	StringBuilder cfName = new StringBuilder("image_");
	String region = isEmpty(aRegion) ? "all" : aRegion.replace(',', '-');

	if (aLevel != null && !aLevel.equals("") && !aLevel.equals("-1")) {
	    cfName.append(aLevel);
	}
	else {
	    cfName.append(aScale).append('_').append(region);
	}

	return cfName.append(".jpg").toString();
    }

    private static boolean isEmpty(String aString) {
	return aString == null || aString.equals("");
    }
}
