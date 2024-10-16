package io.github.moehreag.dtaplot.socket.tcp;

import java.util.List;

public class Calculations extends DataVector {

	public Calculations() {
		super(List.of(
				unknown("Unknown_Calculation_0"),
				unknown("Unknown_Calculation_1"),
				unknown("Unknown_Calculation_2"),
				unknown("Unknown_Calculation_3"),
				unknown("Unknown_Calculation_4"),
				unknown("Unknown_Calculation_5"),
				unknown("Unknown_Calculation_6"),
				unknown("Unknown_Calculation_7"),
				unknown("Unknown_Calculation_8"),
				unknown("Unknown_Calculation_9"),
				celsius("ID_WEB_Temperatur_TVL"),
				celsius("ID_WEB_Temperatur_TRL"),
				celsius("ID_WEB_Sollwert_TRL_HZ"),
				celsius("ID_WEB_Temperatur_TRL_ext"),
				celsius("ID_WEB_Temperatur_THG"),
				celsius("ID_WEB_Temperatur_TA"),
				celsius("ID_WEB_Mitteltemperatur"),
				celsius("ID_WEB_Temperatur_TBW"),
				celsius("ID_WEB_Einst_BWS_akt"),
				celsius("ID_WEB_Temperatur_TWE"),
				celsius("ID_WEB_Temperatur_TWA"),
				celsius("ID_WEB_Temperatur_TFB1"),
				celsius("ID_WEB_Sollwert_TVL_MK1"),
				celsius("ID_WEB_Temperatur_RFV"),
				celsius("ID_WEB_Temperatur_TFB2"),
				celsius("ID_WEB_Sollwert_TVL_MK2"),
				celsius("ID_WEB_Temperatur_TSK"),
				celsius("ID_WEB_Temperatur_TSS"),
				celsius("ID_WEB_Temperatur_TEE"),
				bool("ID_WEB_ASDin"),
				bool("ID_WEB_BWTin"),
				bool("ID_WEB_EVUin"),
				bool("ID_WEB_HDin"),
				bool("ID_WEB_MOTin"),
				bool("ID_WEB_NDin"),
				bool("ID_WEB_PEXin"),
				bool("ID_WEB_SWTin"),
				bool("ID_WEB_AVout"),
				bool("ID_WEB_BUPout"),
				bool("ID_WEB_HUPout"),
				bool("ID_WEB_MA1out"),
				bool("ID_WEB_MZ1out"),
				bool("ID_WEB_VENout"),
				bool("ID_WEB_VBOout"),
				bool("ID_WEB_VD1out"),
				bool("ID_WEB_VD2out"),
				bool("ID_WEB_ZIPout"),
				bool("ID_WEB_ZUPout"),
				bool("ID_WEB_ZW1out"),
				bool("ID_WEB_ZW2SSTout"),
				bool("ID_WEB_ZW3SSTout"),
				bool("ID_WEB_FP2out"),
				bool("ID_WEB_SLPout"),
				bool("ID_WEB_SUPout"),
				bool("ID_WEB_MZ2out"),
				bool("ID_WEB_MA2out"),
				seconds("ID_WEB_Zaehler_BetrZeitVD1"),
				count("ID_WEB_Zaehler_BetrZeitImpVD1"),
				seconds("ID_WEB_Zaehler_BetrZeitVD2"),
				count("ID_WEB_Zaehler_BetrZeitImpVD2"),
				seconds("ID_WEB_Zaehler_BetrZeitZWE1"),
				seconds("ID_WEB_Zaehler_BetrZeitZWE2"),
				seconds("ID_WEB_Zaehler_BetrZeitZWE3"),
				seconds("ID_WEB_Zaehler_BetrZeitWP"),
				seconds("ID_WEB_Zaehler_BetrZeitHz"),
				seconds("ID_WEB_Zaehler_BetrZeitBW"),
				seconds("ID_WEB_Zaehler_BetrZeitKue"),
				seconds("ID_WEB_Time_WPein_akt"),
				seconds("ID_WEB_Time_ZWE1_akt"),
				seconds("ID_WEB_Time_ZWE2_akt"),
				seconds("ID_WEB_Timer_EinschVerz"),
				seconds("ID_WEB_Time_SSPAUS_akt"),
				seconds("ID_WEB_Time_SSPEIN_akt"),
				seconds("ID_WEB_Time_VDStd_akt"),
				seconds("ID_WEB_Time_HRM_akt"),
				seconds("ID_WEB_Time_HRW_akt"),
				seconds("ID_WEB_Time_LGS_akt"),
				seconds("ID_WEB_Time_SBW_akt"),
				heatpumpCode("ID_WEB_Code_WP_akt"),
				bivalenceLevel("ID_WEB_BIV_Stufe_akt"),
				operationMode("ID_WEB_WP_BZ_akt"),
				character("ID_WEB_SoftStand_0"),
				character("ID_WEB_SoftStand_1"),
				character("ID_WEB_SoftStand_2"),
				character("ID_WEB_SoftStand_3"),
				character("ID_WEB_SoftStand_4"),
				character("ID_WEB_SoftStand_5"),
				character("ID_WEB_SoftStand_6"),
				character("ID_WEB_SoftStand_7"),
				character("ID_WEB_SoftStand_8"),
				character("ID_WEB_SoftStand_9"),
				ipv4Address("ID_WEB_AdresseIP_akt"),
				ipv4Address("ID_WEB_SubNetMask_akt"),
				ipv4Address("ID_WEB_Add_Broadcast"),
				ipv4Address("ID_WEB_Add_StdGateway"),
				timestamp("ID_WEB_ERROR_Time0"),
				timestamp("ID_WEB_ERROR_Time1"),
				timestamp("ID_WEB_ERROR_Time2"),
				timestamp("ID_WEB_ERROR_Time3"),
				timestamp("ID_WEB_ERROR_Time4"),
				errorcode("ID_WEB_ERROR_Nr0"),
				errorcode("ID_WEB_ERROR_Nr1"),
				errorcode("ID_WEB_ERROR_Nr2"),
				errorcode("ID_WEB_ERROR_Nr3"),
				errorcode("ID_WEB_ERROR_Nr4"),
				count("ID_WEB_AnzahlFehlerInSpeicher"),
				switchoffFile("ID_WEB_Switchoff_file_Nr0"),
				switchoffFile("ID_WEB_Switchoff_file_Nr1"),
				switchoffFile("ID_WEB_Switchoff_file_Nr2"),
				switchoffFile("ID_WEB_Switchoff_file_Nr3"),
				switchoffFile("ID_WEB_Switchoff_file_Nr4"),
				timestamp("ID_WEB_Switchoff_file_Time0"),
				timestamp("ID_WEB_Switchoff_file_Time1"),
				timestamp("ID_WEB_Switchoff_file_Time2"),
				timestamp("ID_WEB_Switchoff_file_Time3"),
				timestamp("ID_WEB_Switchoff_file_Time4"),
				bool("ID_WEB_Comfort_exists"),
				mainMenuStatusLine1("ID_WEB_HauptMenuStatus_Zeile1"),
				mainMenuStatusLine2("ID_WEB_HauptMenuStatus_Zeile2"),
				mainMenuStatusLine3("ID_WEB_HauptMenuStatus_Zeile3"),
				seconds("ID_WEB_HauptMenuStatus_Zeit"),
				level("ID_WEB_HauptMenuAHP_Stufe"),
				celsius("ID_WEB_HauptMenuAHP_Temp"),
				seconds("ID_WEB_HauptMenuAHP_Zeit"),
				bool("ID_WEB_SH_BWW"),
				icon("ID_WEB_SH_HZ"),
				icon("ID_WEB_SH_MK1"),
				icon("ID_WEB_SH_MK2"),
				unknown("ID_WEB_Einst_Kurzrpgramm"),
				unknown("ID_WEB_StatusSlave_1"),
				unknown("ID_WEB_StatusSlave_2"),
				unknown("ID_WEB_StatusSlave_3"),
				unknown("ID_WEB_StatusSlave_4"),
				unknown("ID_WEB_StatusSlave_5"),
				timestamp("ID_WEB_AktuelleTimeStamp"),
				icon("ID_WEB_SH_MK3"),
				celsius("ID_WEB_Sollwert_TVL_MK3"),
				celsius("ID_WEB_Temperatur_TFB3"),
				bool("ID_WEB_MZ3out"),
				bool("ID_WEB_MA3out"),
				bool("ID_WEB_FP3out"),
				seconds("ID_WEB_Time_AbtIn"),
				celsius("ID_WEB_Temperatur_RFV2"),
				celsius("ID_WEB_Temperatur_RFV3"),
				icon("ID_WEB_SH_SW"),
				unknown("ID_WEB_Zaehler_BetrZeitSW"),
				bool("ID_WEB_FreigabKuehl"),
				voltage("ID_WEB_AnalogIn"),
				unknown("ID_WEB_SonderZeichen"),
				icon("ID_WEB_SH_ZIP"),
				icon("ID_WEB_WebsrvProgrammWerteBeobarten"),
				energy("ID_WEB_WMZ_Heizung"),
				energy("ID_WEB_WMZ_Brauchwasser"),
				energy("ID_WEB_WMZ_Schwimmbad"),
				energy("ID_WEB_WMZ_Seit"),
				flow("ID_WEB_WMZ_Durchfluss"),
				voltage("ID_WEB_AnalogOut1"),
				voltage("ID_WEB_AnalogOut2"),
				seconds("ID_WEB_Time_Heissgas"),
				celsius("ID_WEB_Temp_Lueftung_Zuluft"),
				celsius("ID_WEB_Temp_Lueftung_Abluft"),
				seconds("ID_WEB_Zaehler_BetrZeitSolar"),
				voltage("ID_WEB_AnalogOut3"),
				voltage("ID_WEB_AnalogOut4"),
				voltage("ID_WEB_Out_VZU"),
				voltage("ID_WEB_Out_VAB"),
				bool("ID_WEB_Out_VSK"),
				bool("ID_WEB_Out_FRH"),
				voltage("ID_WEB_AnalogIn2"),
				voltage("ID_WEB_AnalogIn3"),
				bool("ID_WEB_SAXin"),
				bool("ID_WEB_SPLin"),
				bool("ID_WEB_Compact_exists"),
				flow("ID_WEB_Durchfluss_WQ"),
				bool("ID_WEB_LIN_exists"),
				celsius("ID_WEB_LIN_ANSAUG_VERDAMPFER"),
				celsius("ID_WEB_LIN_ANSAUG_VERDICHTER"),
				celsius("ID_WEB_LIN_VDH"),
				kelvin("ID_WEB_LIN_UH"),
				kelvin("ID_WEB_LIN_UH_Soll"),
				pressure("ID_WEB_LIN_HD"),
				pressure("ID_WEB_LIN_ND"),
				bool("ID_WEB_LIN_VDH_out"),
				percent2("ID_WEB_HZIO_PWM"),
				speed("ID_WEB_HZIO_VEN"),
				unknown("ID_WEB_HZIO_EVU2"),
				bool("ID_WEB_HZIO_STB"),
				energy("ID_WEB_SEC_Qh_Soll"),
				energy("ID_WEB_SEC_Qh_Ist"),
				celsius("ID_WEB_SEC_TVL_Soll"),
				unknown("ID_WEB_SEC_Software"),
				secOperationMode("ID_WEB_SEC_BZ"),
				unknown("ID_WEB_SEC_VWV"),
				speed("ID_WEB_SEC_VD"),
				celsius("ID_WEB_SEC_VerdEVI"),
				celsius("ID_WEB_SEC_AnsEVI"),
				kelvin("ID_WEB_SEC_UEH_EVI"),
				kelvin("ID_WEB_SEC_UEH_EVI_S"),
				celsius("ID_WEB_SEC_KondTemp"),
				celsius("ID_WEB_SEC_FlussigEx"),
				celsius("ID_WEB_SEC_UK_EEV"),
				pressure("ID_WEB_SEC_EVI_Druck"),
				voltage("ID_WEB_SEC_U_Inv"),
				celsius("ID_WEB_Temperatur_THG_2"),
				celsius("ID_WEB_Temperatur_TWE_2"),
				celsius("ID_WEB_LIN_ANSAUG_VERDAMPFER_2"),
				celsius("ID_WEB_LIN_ANSAUG_VERDICHTER_2"),
				celsius("ID_WEB_LIN_VDH_2"),
				kelvin("ID_WEB_LIN_UH_2"),
				kelvin("ID_WEB_LIN_UH_Soll_2"),
				pressure("ID_WEB_LIN_HD_2"),
				pressure("ID_WEB_LIN_ND_2"),
				bool("ID_WEB_HDin_2"),
				bool("ID_WEB_AVout_2"),
				bool("ID_WEB_VBOout_2"),
				bool("ID_WEB_VD1out_2"),
				bool("ID_WEB_LIN_VDH_out_2"),
				switchoffFile("ID_WEB_Switchoff2_file_Nr0"),
				switchoffFile("ID_WEB_Switchoff2_file_Nr1"),
				switchoffFile("ID_WEB_Switchoff2_file_Nr2"),
				switchoffFile("ID_WEB_Switchoff2_file_Nr3"),
				switchoffFile("ID_WEB_Switchoff2_file_Nr4"),
				timestamp("ID_WEB_Switchoff2_file_Time0"),
				timestamp("ID_WEB_Switchoff2_file_Time1"),
				timestamp("ID_WEB_Switchoff2_file_Time2"),
				timestamp("ID_WEB_Switchoff2_file_Time3"),
				timestamp("ID_WEB_Switchoff2_file_Time4"),
				celsius("ID_WEB_RBE_RT_Ist"),
				celsius("ID_WEB_RBE_RT_Soll"),
				celsius("ID_WEB_Temperatur_BW_oben"),
				heatpumpCode("ID_WEB_Code_WP_akt_2"),
				frequency("ID_WEB_Freq_VD"),
				celsius("Vapourisation_Temperature"),
				celsius("Liquefaction_Temperature"),
				unknown("Unknown_Calculation_234"),
				unknown("Unknown_Calculation_235"),
				frequency("ID_WEB_Freq_VD_Soll"),
				frequency("ID_WEB_Freq_VD_Min"),
				frequency("ID_WEB_Freq_VD_Max"),
				kelvin("VBO_Temp_Spread_Soll"),
				kelvin("VBO_Temp_Spread_Ist"),
				percent2("HUP_PWM"),
				kelvin("HUP_Temp_Spread_Soll"),
				kelvin("HUP_Temp_Spread_Ist"),
				unknown("Unknown_Calculation_244"),
				unknown("Unknown_Calculation_245"),
				unknown("Unknown_Calculation_246"),
				unknown("Unknown_Calculation_247"),
				unknown("Unknown_Calculation_248"),
				unknown("Unknown_Calculation_249"),
				unknown("Unknown_Calculation_250"),
				unknown("Unknown_Calculation_251"),
				unknown("Unknown_Calculation_252"),
				unknown("Unknown_Calculation_253"),
				flow("Flow_Rate_254"),
				unknown("Unknown_Calculation_255"),
				unknown("Unknown_Calculation_256"),
				power("Heat_Output"),
				majorMinorVersion("RBE_Version"),
				unknown("Unknown_Calculation_259"),
				unknown("Unknown_Calculation_260"),
				unknown("Unknown_Calculation_261"),
				unknown("Unknown_Calculation_262"),
				unknown("Unknown_Calculation_263"),
				unknown("Unknown_Calculation_264"),
				unknown("Unknown_Calculation_265"),
				unknown("Unknown_Calculation_266"),
				celsius("Desired_Room_Temperature")
		));
	}
}
