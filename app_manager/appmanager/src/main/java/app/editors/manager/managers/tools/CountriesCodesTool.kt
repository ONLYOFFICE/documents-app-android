package app.editors.manager.managers.tools

import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import java.util.*
import javax.inject.Inject

class CountriesCodesTool @Inject constructor(
    private val phoneNumberUtil: PhoneNumberUtil
) {
    data class Codes(val name: String, val code: String, val number: Int)

    private val codesList: MutableList<Codes> = ArrayList()

    private val codesComparator: Comparator<Codes> =
        Comparator { o1, o2 -> o1?.name?.compareTo(o2?.name ?: "") ?: -1 }

    init {
        initCodes()
    }

    /*
    * Match for region/code/country
    * */
    private fun initCodes() {
        val supportedRegions: Set<String> = TreeSet(phoneNumberUtil.supportedRegions)
        val countries: Set<String> = TreeSet(listOf(*Locale.getISOCountries()))
        val defaultLanguage = Locale.getDefault().language
        for (region in supportedRegions) {
            if (countries.contains(region)) {
                val locale = Locale(defaultLanguage, region)
                val name = locale.displayCountry
                val code = phoneNumberUtil.getCountryCodeForRegion(region)
                val codes = Codes(name, region, code)
                codesList.add(codes)
            }
        }
        Collections.sort(codesList, codesComparator)
    }

    /*
    * Check number for valid format for region
    * */
    fun getPhoneE164(phone: String?, region: String?): String? {
        var phoneE164: String? = null
        try {
            val phoneNumber = phoneNumberUtil.parse(phone, region)
            if (phoneNumber != null && phoneNumberUtil.isValidNumber(phoneNumber)) {
                phoneE164 = phoneNumberUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.E164)
            }
        } catch (e: Exception) {
            // No need handle
        }
        return phoneE164
    }

    fun getCodeByRegion(region: String?): Codes? {
        for (i in codesList.indices) {
            if (codesList[i].code.equals(region, ignoreCase = true)) {
                return codesList[i]
            }
        }
        return null
    }

    fun getCountryByCode(number: Int): Codes? {
        for (i in codesList.indices) {
            if (codesList[i].number == number) {
                return codesList[i]
            }
        }
        return null
    }

    val codes: List<Codes>
        get() = codesList

}