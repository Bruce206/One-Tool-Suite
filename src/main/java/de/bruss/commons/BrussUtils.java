package de.bruss.commons;

import org.apache.commons.lang3.StringUtils;

public class BrussUtils {
	/**
	 * replaces \ with / and adds a / in first and last place if not present
	 * 
	 * @param path
	 * @return
	 */
	public static String formatPath(String path, boolean leadingSlash, boolean endingSlash) {

		if (StringUtils.isNotBlank(path)) {

			path = path.replace("\\", "/");

			if (leadingSlash && !path.startsWith("/")) {
				path = "/" + path;
			}
			
			if (!leadingSlash && path.startsWith("/")) {
				path = path.substring(1);
			}

			if (endingSlash && !path.endsWith("/")) {
				path += "/";
			}
			
			if (!endingSlash && path.endsWith("/")) {
				path = path.substring(0, path.length() - 1);
			}
		}

		return path;
	}

}
