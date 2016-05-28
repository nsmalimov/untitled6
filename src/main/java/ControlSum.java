public class ControlSum {

    public static boolean checkControlSum(String controlsum, String ip) {
        String[] partsIP = ip.split("\\.");

        int controlsumNew = 0;

        for (int i = 0; i < partsIP.length; i++) {
            controlsumNew += Integer.parseInt(partsIP[i]);
        }

        controlsumNew = controlsumNew + 2001;

        if (Integer.toString(controlsumNew).equals(controlsum)) {
            return true;
        }

        return false;
    }

    public static String createControlSum(String ip) {
        String[] partsIP = ip.split("\\.");

        int controlsum = 0;

        for (int i = 0; i < partsIP.length; i++) {
            controlsum += Integer.parseInt(partsIP[i]);
        }

        controlsum = controlsum + 2001;

        return Integer.toString(controlsum);
    }
}
