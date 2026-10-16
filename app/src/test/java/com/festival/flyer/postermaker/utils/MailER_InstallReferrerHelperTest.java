package com.festival.flyer.postermaker.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class MailER_InstallReferrerHelperTest {

    @Test
    public void testCase1_GoogleAdsGclid() {
        String referrer = "gclid=Cj0KCQjwmOm3BhC8ARIsABlb45T_xyz123";
        assertEquals("paid", MailER_InstallReferrerHelper.classifyReferrer(referrer));
    }

    @Test
    public void testCase2_GoogleAdsGadSource() {
        String referrer = "gad_source=1&utm_source=google&utm_medium=cpc";
        assertEquals("paid", MailER_InstallReferrerHelper.classifyReferrer(referrer));
    }

    @Test
    public void testCase3_GoogleSearchAd() {
        String referrer = "utm_source=google&utm_medium=cpc";
        assertEquals("paid", MailER_InstallReferrerHelper.classifyReferrer(referrer));
    }

    @Test
    public void testCase4_GoogleDisplayAd() {
        String referrer = "utm_source=googleads&utm_medium=display";
        assertEquals("paid", MailER_InstallReferrerHelper.classifyReferrer(referrer));
    }

    @Test
    public void testCase5_OrganicPlayStoreSearch() {
        String referrer = "utm_source=google-play&utm_medium=organic";
        assertEquals("organic", MailER_InstallReferrerHelper.classifyReferrer(referrer));
    }

    @Test
    public void testCase6_OrganicSearchWithKeywords() {
        String referrer = "utm_source=google-play&utm_medium=organic&utm_term=poster";
        assertEquals("organic", MailER_InstallReferrerHelper.classifyReferrer(referrer));
    }

    @Test
    public void testCase7_OrganicPlayWebDomain() {
        String referrer = "utm_source=play.google.com&utm_medium=organic";
        assertEquals("organic", MailER_InstallReferrerHelper.classifyReferrer(referrer));
    }

    @Test
    public void testCase8_EmptyOrNullReferrer() {
        assertEquals("unknown", MailER_InstallReferrerHelper.classifyReferrer(""));
        assertEquals("unknown", MailER_InstallReferrerHelper.classifyReferrer(null));
        assertEquals("unknown", MailER_InstallReferrerHelper.classifyReferrer("   "));
    }

    @Test
    public void testCase9_ThirdPartyUnrecognized() {
        String referrer = "utm_source=friend_referral&utm_medium=whatsapp";
        assertEquals("unknown", MailER_InstallReferrerHelper.classifyReferrer(referrer));
    }

    @Test
    public void testCase10_MalformedCorruptedData() {
        String referrer = "corrupted_data_string_without_delimiters";
        assertEquals("unknown", MailER_InstallReferrerHelper.classifyReferrer(referrer));
    }

    @Test
    public void testCase11_UrlEncodedReferrer() {
        String referrer = "utm_source%3Dgoogle-play%26utm_medium%3Dorganic";
        assertEquals("organic", MailER_InstallReferrerHelper.classifyReferrer(referrer));
    }
}
