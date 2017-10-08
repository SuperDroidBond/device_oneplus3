# for HIDL related packages
PRODUCT_PACKAGES += \
    android.hardware.audio@2.0-impl \
    android.hardware.audio.effect@2.0-impl \
    android.hardware.soundtrigger@2.0-impl

# Bluetooth
PRODUCT_PACKAGES += \
    android.hardware.bluetooth@1.0-impl

PRODUCT_PACKAGES += \
    android.hardware.camera.provider@2.4-impl \
    camera.device@3.2-impl

# Display/Graphics
PRODUCT_PACKAGES += \
    android.hardware.graphics.allocator@2.0-impl \
    android.hardware.graphics.allocator@2.0-service \
    android.hardware.graphics.composer@2.1-impl \
    android.hardware.graphics.mapper@2.0-impl

PRODUCT_PACKAGES += \
    android.hardware.memtrack@1.0-impl \
    android.hardware.light@2.0-impl

# DRM
PRODUCT_PACKAGES += \
    android.hardware.drm@1.0-impl \
    android.hardware.drm@1.0-service

# Fingerprint sensor
PRODUCT_PACKAGES += \
    android.hardware.biometrics.fingerprint@2.1-service

#Enable AOSP KEYMASTER and GATEKEEPER HIDLs
PRODUCT_PACKAGES += \
    android.hardware.gatekeeper@1.0-impl \
    android.hardware.keymaster@3.0-impl

# GPS
PRODUCT_PACKAGES += \
    android.hardware.gnss@1.0-impl

#Nfc
PRODUCT_PACKAGES += \
    android.hardware.nfc@1.0-impl

# Power
PRODUCT_PACKAGES += \
    android.hardware.power@1.0-impl

# health
PRODUCT_PACKAGES += \
    android.hardware.health@1.0-impl

# RenderScript HAL
PRODUCT_PACKAGES += \
    android.hardware.renderscript@1.0-impl

# Sensors
PRODUCT_PACKAGES += \
    android.hardware.sensors@1.0-impl

# USB
PRODUCT_PACKAGES += \
    android.hardware.usb@1.0-service

# Vibrator
PRODUCT_PACKAGES += \
    android.hardware.vibrator@1.0-impl

# Wifi
PRODUCT_PACKAGES += \
    android.hardware.wifi@1.0-service \
    android.hardware.wifi@1.0-supplicant

#VR
PRODUCT_PACKAGES += \
    android.hardware.vr@1.0-impl \
    android.hardware.vr@1.0-service

#Thermal
PRODUCT_PACKAGES += \
    android.hardware.thermal@1.0-impl \
    android.hardware.thermal@1.0-service
