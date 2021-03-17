package com.example.david.myapplication;

public interface SettingsConstants {

    String[] adcNames = {
            "ADC_INV_TIA_GAIN",
            "ADC_VBUS_RATIO",
            "ADC_THERM_FIXED_R",
            "ADC_THERM_R25",
            "ADC_THERM_B"};
    Integer[] adcFormats = {
            SettingsAdapter.SettingsTypes.TYPE_FLOAT,
            SettingsAdapter.SettingsTypes.TYPE_FLOAT,
            SettingsAdapter.SettingsTypes.TYPE_FLOAT,
            SettingsAdapter.SettingsTypes.TYPE_FLOAT,
            SettingsAdapter.SettingsTypes.TYPE_FLOAT};
    Integer[] adcIDs = {
            0x0001,
            0x0002,
            0x0003,
            0x0004,
            0x0005};

    String[] focNames = {
            "FOC_KP",
            "FOC_KI",
            "FOC_KD",
            "FOC_KC",
            "FOC_PWM_FREQ",
            "FOC_PWM_DEADTIME"};
    Integer[] focFormats = {
            SettingsAdapter.SettingsTypes.TYPE_FLOAT,
            SettingsAdapter.SettingsTypes.TYPE_FLOAT,
            SettingsAdapter.SettingsTypes.TYPE_FLOAT,
            SettingsAdapter.SettingsTypes.TYPE_FLOAT,
            SettingsAdapter.SettingsTypes.TYPE_32BIT,
            SettingsAdapter.SettingsTypes.TYPE_32BIT};
    Integer[] focIDs = {
            0x0101,
            0x0102,
            0x0103,
            0x0104,
            0x0105,
            0x0106};

    String[] mainNames = {
            "MAIN_RAMP_SPEED",
            "MAIN_COUNTS_TO_FOC",
            "MAIN_SPEED_TO_FOC",
            "MAIN_SWITCH_EPS",
            "MAIN_NUM_USB_OUTPUTS",
            "MAIN_USB_SPEED",
            "MAIN_USB_CHOICE_1",
            "MAIN_USB_CHOICE_2",
            "MAIN_USB_CHOICE_3",
            "MAIN_USB_CHOICE_4",
            "MAIN_USB_CHOICE_5",
            "MAIN_USB_CHOICE_6",
            "MAIN_USB_CHOICE_7",
            "MAIN_USB_CHOICE_8",
            "MAIN_USB_CHOICE_9",
            "MAIN_USB_CHOICE_10"};
    Integer[] mainFormats = {
            SettingsAdapter.SettingsTypes.TYPE_FLOAT,
            SettingsAdapter.SettingsTypes.TYPE_32BIT,
            SettingsAdapter.SettingsTypes.TYPE_FLOAT,
            SettingsAdapter.SettingsTypes.TYPE_FLOAT,
            SettingsAdapter.SettingsTypes.TYPE_16BIT,
            SettingsAdapter.SettingsTypes.TYPE_16BIT,
            SettingsAdapter.SettingsTypes.TYPE_16BIT,
            SettingsAdapter.SettingsTypes.TYPE_16BIT,
            SettingsAdapter.SettingsTypes.TYPE_16BIT,
            SettingsAdapter.SettingsTypes.TYPE_16BIT,
            SettingsAdapter.SettingsTypes.TYPE_16BIT,
            SettingsAdapter.SettingsTypes.TYPE_16BIT,
            SettingsAdapter.SettingsTypes.TYPE_16BIT,
            SettingsAdapter.SettingsTypes.TYPE_16BIT,
            SettingsAdapter.SettingsTypes.TYPE_16BIT,
            SettingsAdapter.SettingsTypes.TYPE_16BIT};
    Integer[] mainIDs = {
            0x0201,
            0x0202,
            0x0203,
            0x0204,
            0x0205,
            0x0206,
            0x0207,
            0x0208,
            0x0209,
            0x020A,
            0x020B,
            0x020C,
            0x020D,
            0x020E,
            0x020F,
            0x0210};

    String[] throttleNames = {
            "THRT_TYPE1",
            "THRT_MIN1",
            "THRT_MAX1",
            "THRT_HYST1",
            "THRT_FILT1",
            "THRT_RISE1",
            "THRT_TYPE2",
            "THRT_MIN2",
            "THRT_MAX2",
            "THRT_HYST2",
            "THRT_FILT2",
            "THRT_RISE2"};
    Integer[] throttleFormats = {
            SettingsAdapter.SettingsTypes.TYPE_16BIT,
            SettingsAdapter.SettingsTypes.TYPE_FLOAT,
            SettingsAdapter.SettingsTypes.TYPE_FLOAT,
            SettingsAdapter.SettingsTypes.TYPE_FLOAT,
            SettingsAdapter.SettingsTypes.TYPE_FLOAT,
            SettingsAdapter.SettingsTypes.TYPE_FLOAT,
            SettingsAdapter.SettingsTypes.TYPE_16BIT,
            SettingsAdapter.SettingsTypes.TYPE_FLOAT,
            SettingsAdapter.SettingsTypes.TYPE_FLOAT,
            SettingsAdapter.SettingsTypes.TYPE_FLOAT,
            SettingsAdapter.SettingsTypes.TYPE_FLOAT,
            SettingsAdapter.SettingsTypes.TYPE_FLOAT};
    Integer[] throttleIDs = {
            0x0301,
            0x0302,
            0x0303,
            0x0304,
            0x0305,
            0x0306,
            0x0307,
            0x0308,
            0x0309,
            0x030A,
            0x030B,
            0x030C};

    String[] limitNames = {
            "LMT_VOLT_FAULT_MIN",
            "LMT_VOLT_FAULT_MAX",
            "LMT_CUR_FAULT_MAX",
            "LMT_VOLT_SOFTCAP",
            "LMT_VOLT_HARDCAP",
            "LMT_PHASE_CUR_MAX",
            "LMT_PHASE_REGEN_MAX",
            "LMT_BATT_CUR_MAX",
            "LMT_BATT_REGEN_MAX",
            "LMT_FET_TEMP_SOFTCAP",
            "LMT_FET_TEMP_HARDCAP",
            "LMT_MOTOR_TEMP_SOFTCAP",
            "LMT_MOTOR_TEMP_HARDCAP"};
    Integer[] limitFormats = {
            SettingsAdapter.SettingsTypes.TYPE_FLOAT,
            SettingsAdapter.SettingsTypes.TYPE_FLOAT,
            SettingsAdapter.SettingsTypes.TYPE_FLOAT,
            SettingsAdapter.SettingsTypes.TYPE_FLOAT,
            SettingsAdapter.SettingsTypes.TYPE_FLOAT,
            SettingsAdapter.SettingsTypes.TYPE_FLOAT,
            SettingsAdapter.SettingsTypes.TYPE_FLOAT,
            SettingsAdapter.SettingsTypes.TYPE_FLOAT,
            SettingsAdapter.SettingsTypes.TYPE_FLOAT,
            SettingsAdapter.SettingsTypes.TYPE_FLOAT,
            SettingsAdapter.SettingsTypes.TYPE_FLOAT,
            SettingsAdapter.SettingsTypes.TYPE_FLOAT,
            SettingsAdapter.SettingsTypes.TYPE_FLOAT};
    Integer[] limitIDs = {
            0x0401,
            0x0402,
            0x0403,
            0x0404,
            0x0405,
            0x0406,
            0x0407,
            0x0408,
            0x0409,
            0x040A,
            0x040B,
            0x040C,
            0x040D};

    String[] motorNames = {
            "MOTOR_HALL1",
            "MOTOR_HALL2",
            "MOTOR_HALL3",
            "MOTOR_HALL4",
            "MOTOR_HALL5",
            "MOTOR_HALL6",
            "MOTOR_POLEPAIRS",
            "MOTOR_GEAR_RATIO",
            "MOTOR_WHEEL_SIZE",
            "MOTOR_KV"};
    Integer[] motorFormats = {
            SettingsAdapter.SettingsTypes.TYPE_FLOAT,
            SettingsAdapter.SettingsTypes.TYPE_FLOAT,
            SettingsAdapter.SettingsTypes.TYPE_FLOAT,
            SettingsAdapter.SettingsTypes.TYPE_FLOAT,
            SettingsAdapter.SettingsTypes.TYPE_FLOAT,
            SettingsAdapter.SettingsTypes.TYPE_FLOAT,
            SettingsAdapter.SettingsTypes.TYPE_16BIT,
            SettingsAdapter.SettingsTypes.TYPE_FLOAT,
            SettingsAdapter.SettingsTypes.TYPE_FLOAT,
            SettingsAdapter.SettingsTypes.TYPE_FLOAT};
    Integer[] motorIDs = {
            0x0501,
            0x0502,
            0x0503,
            0x0504,
            0x0505,
            0x0506,
            0x0507,
            0x0508,
            0x0509,
            0x050A};

    String[] drvNames = {
            "DRV_GATE_STRENGTH",
            "DRV_VDS_LIMIT",
            "DRV_CSA_GAIN"};
    Integer[] drvFormats = {
            SettingsAdapter.SettingsTypes.TYPE_32BIT,
            SettingsAdapter.SettingsTypes.TYPE_8BIT,
            SettingsAdapter.SettingsTypes.TYPE_8BIT};
    Integer[] drvIDs = {
            0x0601,
            0x0602,
            0x0603};

    String[] bmsNames = {
            "BMS_IS_CONNECTED",
            "BMS_NUMBATTS",
            "BMS_BATVOLT_N",
            "BMS_BATSTAT_N"};
    Integer[] bmsFormats = {
            SettingsAdapter.SettingsTypes.TYPE_8BIT,
            SettingsAdapter.SettingsTypes.TYPE_16BIT,
            SettingsAdapter.SettingsTypes.TYPE_FLOAT,
            SettingsAdapter.SettingsTypes.TYPE_32BIT};
    Integer[] bmsIDs = {
            0x1A01,
            0x1A02,
            0x1A03,
            0x1A04};
}
