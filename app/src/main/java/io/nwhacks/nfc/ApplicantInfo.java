package io.nwhacks.nfc;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * Created by rice on 11/30/17.
 */

public class ApplicantInfo {
    public String firstName;
    public String lastName;
    public String email;
    public Map<String, Integer> events;
    public static Map<String, String> applicantMap = ImmutableMap.of(
            "hacker", "hacker_short_info",
            "volunteer", "volunteer_short_info",
            "mentor", "mentor_short_info"
    );
    public ApplicantInfo() {}
}
