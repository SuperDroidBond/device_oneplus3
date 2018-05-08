# Copyright (C) 2010 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

#
# This file is the build configuration for a full Android
# build for grouper hardware. This cleanly combines a set of
# device-specific aspects (drivers) with a device-agnostic
# product configuration (apps).
#

# Inherit from the common Open Source product configuration
$(call inherit-product, $(SRC_TARGET_DIR)/product/core_64_bit.mk)
$(call inherit-product, $(SRC_TARGET_DIR)/product/aosp_base_telephony.mk)

# Inherit some common Nitrogen stuff.
$(call inherit-product, vendor/nitrogen/products/common.mk)

# Inherit from hardware-specific part of the product configuration
$(call inherit-product, device/oneplus/oneplus3/device.mk)

# Boot animation
TARGET_SCREEN_HEIGHT := 1920
TARGET_SCREEN_WIDTH := 1080

# Discard inherited values and use our own instead.
PRODUCT_NAME := nitrogen_oneplus3
PRODUCT_DEVICE := oneplus3
PRODUCT_BRAND := OnePlus
PRODUCT_MODEL := OnePlus
PRODUCT_MANUFACTURER := OnePlus

BUILD_FINGERPRINT=OnePlus/OnePlus3/OnePlus3T:8.0.0/OPR6.170623.013/10250816:user/release-keys
