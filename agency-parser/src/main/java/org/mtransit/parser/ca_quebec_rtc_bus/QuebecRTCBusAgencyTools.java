package org.mtransit.parser.ca_quebec_rtc_bus;

import static org.mtransit.commons.Constants.EMPTY;
import static org.mtransit.commons.RegexUtils.ANY;
import static org.mtransit.commons.RegexUtils.BEGINNING;
import static org.mtransit.commons.RegexUtils.END;
import static org.mtransit.commons.RegexUtils.any;
import static org.mtransit.commons.RegexUtils.group;
import static org.mtransit.commons.RegexUtils.mGroup;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mtransit.commons.CleanUtils;
import org.mtransit.commons.StringUtils;
import org.mtransit.commons.provider.RTCQuebecProviderCommons;
import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.MTLog;
import org.mtransit.parser.gtfs.data.GSpec;
import org.mtransit.parser.gtfs.data.GStop;
import org.mtransit.parser.gtfs.data.GTrip;
import org.mtransit.parser.mt.data.MAgency;
import org.mtransit.parser.mt.data.MRoute;
import org.mtransit.parser.mt.data.MTrip;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

// https://www.rtcquebec.ca/donnees-ouvertes
// https://cdn.rtcquebec.ca/Site_Internet/DonneesOuvertes/googletransit.zip
public class QuebecRTCBusAgencyTools extends DefaultAgencyTools {

	public static void main(@NotNull String[] args) {
		new QuebecRTCBusAgencyTools().start(args);
	}

	@Nullable
	@Override
	public List<Locale> getSupportedLanguages() {
		return LANG_FR;
	}

	@Override
	public boolean defaultExcludeEnabled() {
		return true;
	}

	@NotNull
	@Override
	public String getAgencyName() {
		return "RTC";
	}

	@NotNull
	@Override
	public Integer getAgencyRouteType() {
		return MAgency.ROUTE_TYPE_BUS;
	}

	@Override
	public boolean defaultRouteIdEnabled() {
		return true;
	}

	@Override
	public boolean useRouteShortNameForRouteId() {
		return true;
	}

	@NotNull
	@Override
	public String cleanRouteShortName(@NotNull String routeShortName) {
		return routeShortName.toUpperCase(getFirstLanguageNN()); // USED BY RTC QUEBEC REAL-TIME API
	}

	private static final Pattern NULL = Pattern.compile("([\\- ]*null[ \\-]*)", Pattern.CASE_INSENSITIVE);

	@Override
	public boolean defaultRouteLongNameEnabled() {
		return true;
	}

	@Override
	public boolean tryRouteDescForMissingLongName() {
		return true; // route long name NOT provided
	}

	@NotNull
	@Override
	public String cleanRouteLongName(@NotNull String routeLongName) {
		routeLongName = CleanUtils.SAINT.matcher(routeLongName).replaceAll(CleanUtils.SAINT_REPLACEMENT);
		routeLongName = CleanUtils.CLEAN_PARENTHESIS1.matcher(routeLongName).replaceAll(CleanUtils.CLEAN_PARENTHESIS1_REPLACEMENT);
		routeLongName = CleanUtils.CLEAN_PARENTHESIS2.matcher(routeLongName).replaceAll(CleanUtils.CLEAN_PARENTHESIS2_REPLACEMENT);
		routeLongName = NULL.matcher(routeLongName).replaceAll(EMPTY);
		return CleanUtils.cleanLabel(routeLongName);
	}

	@Override
	public boolean defaultAgencyColorEnabled() {
		return true;
	}

	// https://www.rtcquebec.ca/en/media/logos-and-photos
	@SuppressWarnings("unused")
	private static final String COLOR_GREEN_OFFICIAL_1 = "7DBA00"; // GREEN / PMS 376
	@SuppressWarnings("unused")
	private static final String COLOR_GREEN_OFFICIAL_2 = "A4C300"; // GREEN / RGB / RVB 164-195-0
	@SuppressWarnings("unused")
	private static final String COLOR_GREEN_OFFICIAL_3 = "8Cff00"; // GREEN / CMYK / CMJN 45-0-100-0

	private static final String COLOR_GREEN = "A3C614";
	private static final String AGENCY_COLOR = COLOR_GREEN;

	@NotNull
	@Override
	public String getAgencyColor() {
		return AGENCY_COLOR;
	}

	@Override
	public void setTripHeadsign(@NotNull MRoute mRoute, @NotNull MTrip mTrip, @NotNull GTrip gTrip, @NotNull GSpec gtfs) { // DIRECTION ID USED BY REAL-TIME API
		int directionId;
		final String tripHeadsign1 = gTrip.getTripHeadsignOrDefault();
		if (ENDS_WITH_N_.matcher(tripHeadsign1).find()) {
			directionId = RTCQuebecProviderCommons.REAL_TIME_API_N; // DIRECTION ID USED BY REAL-TIME API
		} else if (ENDS_WITH_S_.matcher(tripHeadsign1).find()) {
			directionId = RTCQuebecProviderCommons.REAL_TIME_API_S; // DIRECTION ID USED BY REAL-TIME API
		} else if (ENDS_WITH_E_.matcher(tripHeadsign1).find()) {
			directionId = RTCQuebecProviderCommons.REAL_TIME_API_E; // DIRECTION ID USED BY REAL-TIME API
		} else if (ENDS_WITH_O_.matcher(tripHeadsign1).find()) {
			directionId = RTCQuebecProviderCommons.REAL_TIME_API_O; // DIRECTION ID USED BY REAL-TIME API
		} else {
			throw new MTLog.Fatal("Unexpected trip head-sign '%s'!", gTrip);
		}
		mTrip.setHeadsignString(
				cleanTripHeadsign(gTrip.getTripHeadsignOrDefault()),
				directionId
		);
	}

	@Override
	public boolean directionFinderEnabled() {
		return true;
	}

	private static final Pattern ENDS_WITH_N_ = Pattern.compile(group(
			BEGINNING + group(any(ANY)) + group(" \\(" + "nord" + "\\)") + END
	), Pattern.CASE_INSENSITIVE);
	private static final String ENDS_WITH_N_REPLACEMENT = "N-" + mGroup(2);
	private static final Pattern ENDS_WITH_S_ = Pattern.compile(group(
			BEGINNING + group(any(ANY)) + group(" \\(" + "sud" + "\\)") + END
	), Pattern.CASE_INSENSITIVE);
	private static final String ENDS_WITH_S_REPLACEMENT = "S-" + mGroup(2);
	private static final Pattern ENDS_WITH_E_ = Pattern.compile(group(
			BEGINNING + group(any(ANY)) + group(" \\(" + "est" + "\\)") + END
	), Pattern.CASE_INSENSITIVE);
	private static final String ENDS_WITH_E_REPLACEMENT = "E-" + mGroup(2);
	private static final Pattern ENDS_WITH_O_ = Pattern.compile(group(
			BEGINNING + group(any(ANY)) + group(" \\(" + "ouest" + "\\)") + END
	), Pattern.CASE_INSENSITIVE);
	private static final String ENDS_WITH_O_REPLACEMENT = "O-" + mGroup(2);

	@NotNull
	@Override
	public String cleanTripHeadsign(@NotNull String tripHeadsign) {
		tripHeadsign = ENDS_WITH_N_.matcher(tripHeadsign).replaceAll(ENDS_WITH_N_REPLACEMENT);
		tripHeadsign = ENDS_WITH_S_.matcher(tripHeadsign).replaceAll(ENDS_WITH_S_REPLACEMENT);
		tripHeadsign = ENDS_WITH_E_.matcher(tripHeadsign).replaceAll(ENDS_WITH_E_REPLACEMENT);
		tripHeadsign = ENDS_WITH_O_.matcher(tripHeadsign).replaceAll(ENDS_WITH_O_REPLACEMENT);
		return RTCQuebecProviderCommons.cleanTripHeadsign(tripHeadsign);
	}

	@NotNull
	@Override
	public String cleanStopName(@NotNull String gStopName) {
		gStopName = CleanUtils.cleanBounds(Locale.FRENCH, gStopName);
		gStopName = CleanUtils.cleanStreetTypesFRCA(gStopName);
		return CleanUtils.cleanLabelFR(gStopName);
	}

	@Override
	public int getStopId(@NotNull GStop gStop) {
		return Integer.parseInt(gStop.getStopCode()); // using stop code as stop ID
	}

	@NotNull
	@Override
	public String getStopCode(@NotNull GStop gStop) { // USED BY RTC QUEBEC REAL-TIME API
		if (StringUtils.isEmpty(gStop.getStopCode())) {
			//noinspection deprecation
			return gStop.getStopId(); // using stop ID as stop code
		}
		return super.getStopCode(gStop);
	}
}
