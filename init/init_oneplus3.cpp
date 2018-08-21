/*
   Copyright (c) 2016, The CyanogenMod Project
             (c) 2017, The LineageOS Project
   Redistribution and use in source and binary forms, with or without
   modification, are permitted provided that the following conditions are
   met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above
      copyright notice, this list of conditions and the following
      disclaimer in the documentation and/or other materials provided
      with the distribution.
    * Neither the name of The Linux Foundation nor the names of its
      contributors may be used to endorse or promote products derived
      from this software without specific prior written permission.
   THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESS OR IMPLIED
   WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
   MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT
   ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
   BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
   CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
   SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
   BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
   WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
   OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN
   IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

#include <cstdlib>
#include <unistd.h>
#include <fcntl.h>
#include <string>
#include "android-base/logging.h"
#include <android-base/properties.h>

#define _REALLY_INCLUDE_SYS__SYSTEM_PROPERTIES_H_
#include <sys/_system_properties.h>

#include "vendor_init.h"
#include "property_service.h"

using android::base::GetProperty;
using android::init::property_set;
using android::init::import_kernel_cmdline;

void property_override(char const prop[], char const value[])
{
    prop_info *pi;

    pi = (prop_info*) __system_property_find(prop);
    if (pi)
        __system_property_update(pi, value, strlen(value));
    else
        __system_property_add(prop, strlen(prop), value, strlen(value));
}

void property_override_dual(char const system_prop[], char const vendor_prop[], char const value[])
{
    property_override(system_prop, value);
    property_override(vendor_prop, value);
}

static int read_file2(const char *fname, char *data, int max_size)
{
    int fd, rc;

    if (max_size < 1)
        return 0;

    fd = open(fname, O_RDONLY);
    if (fd < 0) {
        LOG(ERROR) << "failed to open" << fname << "\n";
        return 0;
    }

    rc = read(fd, data, max_size - 1);
    if ((rc > 0) && (rc < max_size))
        data[rc] = '\0';
    else
        data[0] = '\0';
    close(fd);

    return 1;
}

void init_alarm_boot_properties()
{
    char const *alarm_file = "/proc/sys/kernel/boot_reason";
    char buf[64];

    if(read_file2(alarm_file, buf, sizeof(buf))) {

    /*
     * Setup ro.alarm_boot value to true when it is RTC triggered boot up
     * For existing PMIC chips, the following mapping applies
     * for the value of boot_reason:
     *
     * 0 -> unknown
     * 1 -> hard reset
     * 2 -> sudden momentary power loss (SMPL)
     * 3 -> real time clock (RTC)
     * 4 -> DC charger inserted
     * 5 -> USB charger insertd
     * 6 -> PON1 pin toggled (for secondary PMICs)
     * 7 -> CBLPWR_N pin toggled (for external power supply)
     * 8 -> KPDPWR_N pin toggled (power key pressed)
     */
        if(buf[0] == '3')
            property_override("ro.alarm_boot", "true");
        else
            property_override("ro.alarm_boot", "false");
    }
}

void load_op3(const char *model) {
    property_override("ro.product.model", model);
    property_override("ro.build.product", "OnePlus3");
    property_override("ro.product.device", "OnePlus3");
    property_override("ro.build.description", "OnePlus3-user 8.0.0 OPR6.170623.013 77 release-keys");
    property_override("ro.build.fingerprint", "OnePlus/OnePlus3/OnePlus3:8.0.0/OPR6.170623.013/10250816:user/release-keys");
}

void load_op3t(const char *model) {
    property_override("ro.product.model", model);
    property_override("ro.build.product", "OnePlus3");
    property_override("ro.product.device", "OnePlus3T");
    property_override("ro.build.description", "OnePlus3-user 8.0.0 OPR6.170623.013 83 release-keys");
    property_override("ro.build.fingerprint", "OnePlus/OnePlus3/OnePlus3T:8.0.0/OPR6.170623.013/10250816:user/release-keys");
    property_set("ro.power_profile.override", "power_profile_3t");
}

static void import_panel_prop(const std::string& key, const std::string& value, bool for_emulator) {
    if (key.empty()) return;

    if (key.compare("mdss_mdp.panel") == 0) {
        if (value.find("s6e3fa3") != std::string::npos)
            property_override("ro.product.panel", "samsung_s6e3fa3_1080p");
        if (value.find("s6e3fa5") != std::string::npos)
            property_override("ro.product.panel", "samsung_s6e3fa5_1080p");
    }
}

void vendor_load_properties() {
    std::string rf_version = GetProperty("ro.boot.rf_version", "");

    if (rf_version == "11") {
        /* China */
        load_op3("ONEPLUS A3000");
        property_override("ro.telephony.default_network", "22");
        property_override("telephony.lteOnCdmaDevice", "1");
        property_override("persist.vendor.radio.force_on_dc", "true");
        /*property_override("ro.rf_version", "TD-SCDMA");
        property_override("ro.build.display.full_id", "ONEPLUS A3000");
        property_override("ro.build.display.id", "ONEPLUS A3000");*/
    } else if (rf_version == "21") {
        /* Europe / Asia model */
        load_op3("ONEPLUS A3003");
        property_override("ro.telephony.default_network", "9");
        /*property_override("ro.rf_version", "TDD_FDD_Eu");
        property_override("ro.build.display.full_id", "ONEPLUS A3003");
        property_override("ro.build.display.id", "ONEPLUS A3003");*/
    } else if (rf_version == "31") {
        /* Americas */
        load_op3("ONEPLUS A3000");
        property_override("ro.telephony.default_network", "22");
        property_override("telephony.lteOnCdmaDevice", "1");
        property_override("persist.vendor.radio.force_on_dc", "true");
        /*property_override("ro.rf_version", "TDD_FDD_Am");
        property_override("ro.build.display.full_id", "ONEPLUS A3000");
        property_override("ro.build.display.id", "ONEPLUS A3000");*/
    } else if (rf_version == "12") {
        /* China model */
        load_op3t("ONEPLUS A3010");
        property_override("ro.telephony.default_network", "22");
        property_override("telephony.lteOnCdmaDevice", "1");
        property_override("persist.vendor.radio.force_on_dc", "true");
        /*property_override("ro.rf_version", "TD-SCDMA");
        property_override("ro.build.display.full_id", "ONEPLUS A3010");
        property_override("ro.build.display.id", "ONEPLUS A3010");*/
    } else if (rf_version == "22") {
        /* Europe / Asia model */
        load_op3t("ONEPLUS A3003");
        property_override("ro.telephony.default_network", "9");
        /*property_override("ro.rf_version", "TDD_FDD_Eu");
        property_override("ro.build.display.full_id", "ONEPLUS A3003");
        property_override("ro.build.display.id", "ONEPLUS A3003");*/
    } else if (rf_version == "32") {
        /* North America model */
        load_op3t("ONEPLUS A3000");
        property_override("ro.telephony.default_network", "22");
        property_override("telephony.lteOnCdmaDevice", "1");
        property_override("persist.vendor.radio.force_on_dc", "true");
        /*property_override("ro.rf_version", "TDD_FDD_Am");
        property_override("ro.build.display.full_id", "ONEPLUS A3000");
        property_override("ro.build.display.id", "ONEPLUS A3000");*/
    } else {
        LOG(INFO) << __func__ << "unexcepted rf version!\n";
    }

    init_alarm_boot_properties();

    import_kernel_cmdline(false, import_panel_prop);
}
