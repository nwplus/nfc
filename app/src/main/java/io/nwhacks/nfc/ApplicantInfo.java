package io.nwhacks.nfc;

import java.util.Map;

/**
 * Created by rice on 11/30/17.
 */

public class ApplicantInfo {
    public String firstName;
    public String lastName;
    public String email;
    public Map<String, Integer> events;
    public static Map<String, String> applicantMap;

    static {
        applicantMap.put("hacker", "hacker_short_info");
        applicantMap.put("volunteer", "volunteer_short_info");
        applicantMap.put("mentor", "mentor_short_info");
    }
    public ApplicantInfo() {}
}
