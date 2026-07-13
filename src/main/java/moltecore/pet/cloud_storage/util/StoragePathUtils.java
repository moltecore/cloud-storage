package moltecore.pet.cloud_storage.util;

public class StoragePathUtils {

    public static String buildStoragePath(int id, String path) {

        if (path == null) {
            path = "";
        }

        path = path.strip();

        if (path.startsWith("/")) {
            path = path.substring(1);
        }

        return "user-" + id + "-files/" + path;
    }

    public static String getParent(String path) {

        path = normalize(path);
        int index = path.lastIndexOf("/");

        if (index == -1) {
            return "";
        }

        return path.substring(0, index + 1);
    }

    public static String getName(String path) {

        path = normalize(path);

        int index = path.lastIndexOf("/");

        return path.substring(index + 1);
    }

    private static String normalize(String path) {
        if (path.endsWith("/")) {
            return path.substring(0, path.length() - 1);
        }
        return path;
    }

    public static String normalizeDirectoryPath(String path) {

        if (path.endsWith("/")) {
            return path;
        }

        return path + "/";
    }
}
