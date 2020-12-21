package app.editors.manager.managers.tools;

import android.content.Context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import io.michaelrocks.libphonenumber.android.PhoneNumberUtil;
import io.michaelrocks.libphonenumber.android.Phonenumber;

public class CountriesCodesTool {

    public static class Codes {

        public final String mName;
        public final String mCode;
        public final int mNumber;

        public Codes(final String name, final String code, final int number) {
            mName = name;
            mCode = code;
            mNumber = number;
        }
    }

    private final PhoneNumberUtil mPhoneNumberUtil;
    private final List<Codes> mCodesList;
    private final CodesComparator mCodesComparator;

    public CountriesCodesTool(final Context context) {
        mPhoneNumberUtil = PhoneNumberUtil.createInstance(context);
        mCodesList = new ArrayList<>();
        mCodesComparator = new CodesComparator();
        initCodes();
    }

    /*
    * Match for region/code/country
    * */
    private void initCodes() {
        final Set<String> supportedRegions = new TreeSet<>(mPhoneNumberUtil.getSupportedRegions());
        final Set<String> countries = new TreeSet<>(Arrays.asList(Locale.getISOCountries()));
        final String defaultLanguage = Locale.getDefault().getLanguage();

        for (String region : supportedRegions) {
            if (countries.contains(region)) {
                final Locale locale = new Locale(defaultLanguage, region);
                final String name = locale.getDisplayCountry();
                final int code = mPhoneNumberUtil.getCountryCodeForRegion(region);
                final Codes codes = new Codes(name, region, code);
                mCodesList.add(codes);
            }
        }

        Collections.sort(mCodesList, mCodesComparator);
    }

    /*
    * Check number for valid format for region
    * */
    public String getPhoneE164(final String phone, final String region) {
        String phoneE164 = null;
        try {
            final Phonenumber.PhoneNumber phoneNumber = mPhoneNumberUtil.parse(phone, region);
            if (phoneNumber != null && mPhoneNumberUtil.isValidNumber(phoneNumber)) {
                phoneE164 = mPhoneNumberUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.E164);
            }
        } catch (Exception e) {
            // No need handle
        }

        return phoneE164;
    }

    public CountriesCodesTool.Codes getCodeByRegion(final String region) {
        for (int i = 0; i < mCodesList.size(); i++) {
            if (mCodesList.get(i).mCode.equalsIgnoreCase(region)) {
                return mCodesList.get(i);
            }
        }

        return null;
    }

    public CountriesCodesTool.Codes getCountryByCode(final int number) {
        for (int i = 0; i < mCodesList.size(); i++) {
            if (mCodesList.get(i).mNumber == number) {
                return mCodesList.get(i);
            }
        }

        return null;
    }

    public List<Codes> getCodes() {
        return mCodesList;
    }

    private class CodesComparator implements Comparator<Codes> {

        @Override
        public int compare(Codes o1, Codes o2) {
            return o1.mName.compareTo(o2.mName);
        }
    }

}
